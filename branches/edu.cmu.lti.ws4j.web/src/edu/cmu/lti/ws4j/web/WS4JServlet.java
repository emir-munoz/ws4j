package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.WordNetFactory;
import edu.cmu.lti.ram_wordnet.OnMemoryWordNetAPI;
import edu.cmu.lti.ws4j.Factory;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.data.Measure;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

/**
 * API for retrieving WS4J result in JSON.
 * 
 * @author Hideki
 */
@SuppressWarnings("serial")
public class WS4JServlet extends HttpServlet {

  private final static DecimalFormat df5 = new DecimalFormat("0.0000");
  private final static DecimalFormat df1 = new DecimalFormat("0.0");
  
  public final static Pattern pSynsetLabel = Pattern.compile("\\b([^\\s*#]+#[nvar]#[0-9]{1,2})\\b");
  
  private Map<Measure,RelatednessCalculator> rcs = null;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("application/json; charset=UTF-8");

    String measure = req.getParameter("measure");
    String args    = req.getParameter("args");
    String batchId = req.getParameter("batch_id");
    String trace = req.getParameter("trace");

    if (args==null || measure==null ) return;
    String[] pairs = args.split(",");
    
    boolean enableTrace = trace!=null 
            && !trace.equalsIgnoreCase("false")
            && !trace.equalsIgnoreCase("0");
    
    JSONObject retval = new JSONObject();
    JSONArray result = new JSONArray();
    try {
      if (batchId!=null) retval.put("batch_id", batchId);
      retval.put("measure", measure.toLowerCase());
      if (rcs==null) lazyinit(); 
      int size = pairs.length;
      for ( int i=0; i<size; i++ ) {
        String[] pair = pairs[i].split("::");
        if (pair.length!=2) {
          res.getWriter().println("Invalid args: "+args);
          System.err.println("Invalid args: "+args);
          return;
        }
        JSONObject json = runOnWords( pair[0], pair[1], measure, enableTrace );
        result.put(json);
      }
      retval.put("result", result);
    } catch (Exception e) {
      e.printStackTrace();
    }
    res.getWriter().println(retval);
    res.getWriter().flush();
  }
  
  public JSONObject runOnWords( String w1, String w2, 
          String measure, boolean enableTrace ) throws Exception {
    Measure m = Measure.valueOf(measure.toUpperCase());
    w1 = w1.trim().replaceFirst("#+$", "");
    w2 = w2.trim().replaceFirst("#+$", "");
    RelatednessCalculator rc = rcs.get(m);
    long t0 = System.nanoTime();
    Relatedness r = rc.calcRelatednessOfWords(w1, w2, true, enableTrace);
    String log = r.getTrace();//(r.getError().length()>0?r.getError()+"\n\n":"")+
    if (enableTrace) {
      log = log.replaceAll(" < "," &lt; ").replaceAll("\n", "<br>\n").replaceAll("    ", "&nbsp;&nbsp;&nbsp;&nbsp;");
      StringBuffer sb = new StringBuffer();
      Pattern patter = Pattern.compile("((^|\\n)(?i)"+m.toString()+"\\(.+?\\) = [0-9.Ee-]+)");
      Matcher matcher = patter.matcher(log);
      while ( matcher.find() ) {
        matcher.appendReplacement(sb, "<h3>"+matcher.group(1)+"</h3>\n");
      }
      matcher.appendTail(sb);
      log = sb.toString();
    }
    if (enableTrace) {
      StringBuffer sb2 = new StringBuffer();
      Matcher mSynsetLabel = pSynsetLabel.matcher(log);
      while ( mSynsetLabel.find() ) {
        if (mSynsetLabel.group(0).split("#").length==3) { 
          mSynsetLabel.appendReplacement(sb2, "<span class=\"synset\">"+mSynsetLabel.group(0)+"</span>");
        }
      }
      mSynsetLabel.appendTail(sb2);
      log = sb2.toString();
    }
    String input1 = null, input2 = null, score = null;
    boolean isError = r.getScore()<0;
    if (isError) {
      input1 = w1;
      input2 = w2;
      score = "-1";
    } else {
      input1 = r.getSynset1();
      input2 = r.getSynset2();
      if (m==Measure.HSO||m==Measure.LESK) {
        score = (int)r.getScore()+"";
      } else {
        if (r.getScore()>1000000) {
          score = df1.format(r.getScore());
        } else {
          score = df5.format(r.getScore());          
        }
      }
    }
    long t1 = System.nanoTime();
    int sn1 = r.getInput1SynsetNum();
    int sn2 = r.getInput2SynsetNum();
    JSONObject json = new JSONObject();
    json.put("score", score);
    json.put("input1", input1);
    json.put("input2", input2);
    json.put("input1_num", sn1);
    json.put("input2_num", sn2);
    if (enableTrace) json.put("trace", log);
    if (r.getError().length()>0) json.put("error", r.getError());
    double time = (t1-t0)/1000000D;
    json.put("time", time);//msec
    return json;
  }
  
//  @Override
  public void lazyinit() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
      WS4JConfiguration.getInstance().setMFS(false);
      WS4JConfiguration.getInstance().setLeskNormalize(false);
      WS4JConfiguration.getInstance().setCache(true);
      AbstractWordNet wn = WordNetFactory.getCachedInstanceForName(OnMemoryWordNetAPI.class.getCanonicalName());
      Factory f = new Factory(wn);
      Measure[] measures = {Measure.WUP, Measure.RES, Measure.JCN, 
              Measure.LIN, Measure.LCH, Measure.PATH, Measure.LESK, 
              Measure.HSO};
      rcs = new LinkedHashMap<Measure,RelatednessCalculator>();
      for (Measure m : measures) {
        rcs.put(m, f.create(m));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    long diff = t1-t0;
    if (diff > 500) System.err.println("Warming up done in "+diff+" msec.");
  }
  
}
