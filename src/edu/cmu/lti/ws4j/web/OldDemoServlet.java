package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.POS;
import edu.cmu.lti.abstract_wordnet.WordNetFactory;
import edu.cmu.lti.ram_wordnet.InMemoryWordNetAPI;
import edu.cmu.lti.ws4j.Factory;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.RelatednessCalculator.Parameters;
import edu.cmu.lti.ws4j.data.Measure;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.washington.cs.knowitall.morpha.MorphaStemmer;

@SuppressWarnings("serial")
public class OldDemoServlet extends HttpServlet {

  private final static DecimalFormat df = new DecimalFormat("0.0000");
  
  public final static String sample1 = "Eventually, a huge cyclone hit the entrance of my house.";
  public final static String sample2 = "Finally, a massive hurricane attacked my home.";

  private final static Pattern pSynsetLabel = Pattern.compile("\\b([^\\s#]+#[nvar]#[0-9]{1,2})\\b");
  
  private static final NumberFormat nf = NumberFormat.getNumberInstance();
  static {
    nf.setMaximumFractionDigits(3);
//    nf.setMinimumFractionDigits(3);
  }
  
  private Map<Measure,RelatednessCalculator> rcs = null;
//  private KStemmer stemmer;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("text/html; charset=UTF-8");
    res.getWriter().println( getHeader() );
    res.getWriter().println( getHeader2() );
    
    String w1 = req.getParameter("w1");
    String w2 = req.getParameter("w2");
    String measure = req.getParameter("measure");
    String s1 = req.getParameter("s1");
    String s2 = req.getParameter("s2");
    String mode = req.getParameter("mode");
    boolean sentenceMode = mode!=null && mode.equals("s");
    boolean wordMode = !sentenceMode;
    showForm(res.getWriter(), s1, s2, w1, w2, measure, wordMode);
    try {
      if ( wordMode ) {
        if (w1!=null && w2!=null && rcs==null) lazyinit();
        runOnWords( res.getWriter(), w1, w2, measure );
      } else {//default or when s1 & s1 exist
        if (s1!=null && s2!=null && rcs==null) lazyinit();
        runOnSentences( res.getWriter(), s1, s2 );
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    res.getWriter().println( "<div class=\"hr\">&nbsp;</div><a href=\"https://code.google.com/p/ws4j/\">WS4J</a> demo is maintained by <a href=\"http://www.cs.cmu.edu/~hideki\">Hideki Shima</a>.\n" );
    res.getWriter().println( "</body>\n</html>\n" );
  }
  
  private void showForm(PrintWriter out, String s1, String s2, 
          String w1, String w2, String measure, boolean wordMode) {
    StringBuilder sbForm = new StringBuilder();
    sbForm.append("<form action=\"/\" method=\"get\" onsubmit=\"return validate()\">\n");
    sbForm.append("Type in texts below, or use:\n");
    sbForm.append("&nbsp;<input type='button' value='  example words  ' onclick='insert_sampleW()'>&nbsp;\n");
    sbForm.append("&nbsp;<input type='button' value='  example sentences  ' onclick='insert_sampleS()'><br><br>\n");
    sbForm.append("<table class='form'>\n");
    sbForm.append("<tr><td class='th1'>1. </td><td class='th1'>Input mode</td><td>\n");
    sbForm.append("<input type='radio' name='mode' value='w' id='w_mode' onclick='wordMode()' "+(wordMode?"checked":"")+"><label for='w_mode'>Word</label>");
    sbForm.append("&nbsp;&nbsp;&nbsp;\n");
    sbForm.append("<input type='radio' name='mode' value='s' id='s_mode' onclick='sentMode()' "+(wordMode?"":"checked")+"><label for='s_mode'>Sentence</label>");
    sbForm.append("</td></tr>\n");
    sbForm.append("<tr><td>2. </td><td><span class='mode_label'>Sentence</span> 1</td><td>\n");
    sbForm.append("<div id='s1wrapper' style='display:"+(wordMode?"none":"block")+"'><textarea rows='4' cols='40' id='s1' name='s1' placeholder='the first sentence goes here'>"+(s1==null?"":s1)+"</textarea></div>\n");
    sbForm.append("<div id='w1wrapper' style='display:"+(wordMode?"block":"none")+"'><input id='w1' name='w1' placeholder='the first word' value='"+(w1==null?"":w1)+"' title='Type in the first word, word+pos, or word+pos+sense number.\n\nexample: \"bank\" or \"bank#n\" or \"bank#n#2\"'><div class='info'></div></div></td></tr>\n");
    sbForm.append("<tr><td>3. </td><td><span class='mode_label'>Sentence</span> 2</td><td>\n");
    sbForm.append("<div id='s2wrapper' style='display:"+(wordMode?"none":"block")+"'><textarea rows='4' cols='40' id='s2' name='s2' placeholder='the second sentence goes here'>"+(s2==null?"":s2)+"</textarea></div>\n");
    sbForm.append("<div id='w2wrapper' style='display:"+(wordMode?"block":"none")+"'><input id='w2' name='w2' placeholder='the second word' value='"+(w2==null?"":w2)+"' title='Similarly, type in another one.\n\nexample: \"hedge_fund\" or \"hedge_fund#n\" or \"hedge_fund#n#1\"'><div class='info'></div></div></td></tr>\n");
    sbForm.append("<tr><td>4. </td><td>Submit</td><td><div style='text-align:center'><input type='submit' value='       Calculate Semantic Similarity       '></div></td></tr>\n");
    sbForm.append("</table></form>\n");
    out.println( sbForm );
  }

  public void runOnWords( PrintWriter out, String w1, String w2, String measure ) {
    if (w1==null || w2==null) {
      return;
    }
    w1 = w1.trim().replaceFirst("#+$", "");
    w2 = w2.trim().replaceFirst("#+$", "");
//    WS4JConfiguration.getInstance().setTrace(true);//doesn't work here
//    long t00 = System.currentTimeMillis();
    List<Measure> measures;
    if (measure!=null) {
      measures = new ArrayList<Measure>();
      measures.add(Measure.valueOf(measure.toUpperCase()));
    } else {
      measures = new ArrayList<Measure>(rcs.keySet());
    }
    StringBuilder sbSummary = new StringBuilder("<h2>Summary</h2>\n");
    StringBuilder sbResult = new StringBuilder();
    for ( Measure m : measures ) {
      RelatednessCalculator rc = rcs.get(m);
//      long t0 = System.currentTimeMillis();
      sbResult.append( "<h2 id=\""+m.toString().toLowerCase()+"\">"+m+"</h2>\n" );
      Relatedness r = null;
      r = rc.calcRelatednessOfWords(w1, w2, true, true);
      String log = r.getTrace();//(r.getError().length()>0?r.getError()+"\n\n":"")+
      log = log.replaceAll(" < "," &lt; ").replaceAll("\n", "<br>\n");
      StringBuffer sb = new StringBuffer();
      {
        Pattern patter = Pattern.compile("((^|\\n)(?i)"+m.toString()+"\\(.+?\\) = [0-9.Ee-]+)");
        Matcher matcher = patter.matcher(log);
        while ( matcher.find() ) {
          matcher.appendReplacement(sb, "<h3>"+matcher.group(1)+"</h3>\n");
        }
        matcher.appendTail(sb);
        log = sb.toString();
      }
      {
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
      String id = m.toString().toLowerCase();
      if (r.getScore()<0) {
        input1 = w1;
        input2 = w2;
        score = "-1 <span class=\"error_reason\">["+r.getError()+"]</span>";
      } else {
        input1 = "<span class=\"synset\">"+r.getSynset1()+"</span>";
        input2 = "<span class=\"synset\">"+r.getSynset2()+"</span>";
        if (m==Measure.HSO||m==Measure.LESK) {
          score = (int)r.getScore()+"";
        } else {
          score = df.format(r.getScore());
        }
      }
      sbSummary.append("<h3><a class=\"scrolltolink\" href=\"javascript:scrollTo('"+id+"')\">"+id+"</a>"
              +"( "+input1+" , "+input2+" ) = "+score+"</h3>\n");
      sbResult.append(log);
//        long t1 = System.currentTimeMillis();
//        sbResult.append("<textarea rows=\"6\" cols=\"70\">");
      sbResult.append("<br><h3>Description of "+m+"</h3>\n"+m.getDescription().replaceAll("\n", "<br>\n")+"\n\n");
      sbResult.append("<br><h3>Parameters</h3>\n<ul>\n");
      Parameters param = rc.dumpParameters();
      for ( String key : param.keySet() ) sbResult.append( "<li>"+key+" = "+param.get(key)+"</li>\n" );  
      sbResult.append("</ul>");
    }
    out.println( sbSummary );
    out.println( sbResult );
    out.flush();
    
//    WS4JConfiguration.getInstance().setTrace(false);
//    long t01 = System.currentTimeMillis();
  }
  
  public void runOnSentences( PrintWriter out, String s1, String s2 ) {
    if (s1==null || s2==null) {
      return;
    }
//    sbForm.append("[Tips] In this demo, the following preprocessing are done before WS4J: tokenization, POS tagging, lemmatization.<br>\n" +
//        "If you see unexpected results due to errors in preprocessing, simply type in a list of lemmatized words.<br>\n");

    String[] words1 = OpenNLPSingleton.INSTANCE.tokenize(s1);
    String[] words2 = OpenNLPSingleton.INSTANCE.tokenize(s2);
    String[] postag1 = OpenNLPSingleton.INSTANCE.postag(words1);
    String[] postag2 = OpenNLPSingleton.INSTANCE.postag(words2);
    long t00 = System.currentTimeMillis();
    for ( Measure m : rcs.keySet() ) {
      RelatednessCalculator rc = rcs.get(m);
      long t0 = System.currentTimeMillis();
      StringBuilder sbResult = new StringBuilder();
      sbResult.append( "<h2>"+m+"</h2>\n" );
      sbResult.append("<table border=1 class=\"data\"><tr><td class=\"th\">&nbsp;</td>");
      for ( int i=0; i<words1.length; i++ ) {
        sbResult.append("<td class=\"th\">"+words1[i]+"<br><span class=\"g\">/"+postag1[i]+"</span></td>\n");
      }
      sbResult.append("</tr>\n");
      for ( int j=0; j<words2.length; j++ ) {
        String pt2 = postag2[j];
        String w2 = MorphaStemmer.stemToken(words2[j].toLowerCase(), pt2);
        POS p2 = mapPOS( pt2 );
        sbResult.append("<tr>");
        sbResult.append("<td class=\"th\">"+words2[j]+"<span class=\"g\">/"+pt2+"</span></td>\n");
        for ( int i=0; i<words1.length; i++ ) {
          String pt1 = postag1[i];
          String w1 = MorphaStemmer.stemToken(words1[i].toLowerCase(), pt1);
          POS p1 = mapPOS( pt1 );
          boolean error = true;
          String errorMsg = null;
          double d = -1;
          String popup = m+"('"+w1+"#"+(p1!=null?p1:"INVALID_POS")+"', '"+w2+"#"+(p2!=null?p2:"INVALID_POS")+"')";
          if ( p1!=null && p2!=null ) {
//              List<Synset> synsets1 = wn.getSynsets(w1, p1);
//              List<Synset> synsets2 = wn.getSynsets(w2, p2);
              Relatedness r = rc.calcRelatednessOfWords(w1+"#"+p1.toString(), w2+"#"+p2.toString(), true, false);
              d = r.getScore();
              error = r.getError().length()>0;
              errorMsg = r.getError();
              popup = m+"('"+(r.getSynset1()==null?w1+"#"+p1:r.getSynset1())+"', '"+(r.getSynset2()==null?w2+"#"+p2:r.getSynset2())+"')";
          }
          String dText;
          String url = "ws4j?w1="+w1+"&w2="+w2+"&measure="+m+"&mode=w";
          if ( d <= 0 ) {
            if (error) {
              dText = "<span class=\"g\" title=\""+popup+" = "+errorMsg+"\">-</span>";
            } else {
              dText = "<span class=\"g\" title=\""+popup+" = 0\"><a href=\""+url+"\" target=\"_blank\">0</a></span>";
            }
          } else {
            if ( Double.MAX_VALUE - d < 10e-9 ) {//MAX
              dText = "<span class=\"num\" title=\""+popup+" = INF\"><i><a href=\""+url+"\" target=\"_blank\">INF</a></i></span>";
            } else {
              dText = "<span class=\"num\" title=\""+popup+" = "+d+"\"><a href=\""+url+"\" target=\"_blank\">"+nf.format(d)+"</a></span>";
            }
          }
          sbResult.append("<td>"+dText+"</td>\n");
        }
        sbResult.append("</tr>\n");
      }
      sbResult.append("</table><br>\n");
      long t1 = System.currentTimeMillis();
//        sbResult.append("<div title=\"tokenization, pos tagging, stemming, semantic similarity calculation, table generation\">\n</div>");
      sbResult.append("<textarea rows=\"6\" cols=\"70\">");
      sbResult.append("Done in "+(t1-t0)+" msec.\n\n");
      sbResult.append(m+": "+m.getDescription()+"\n\n");
      sbResult.append("Parameters:\n");
      Parameters p = rc.dumpParameters();
      for ( String key : p.keySet() ) sbResult.append( " - "+key+" = "+p.get(key)+"\n" );  
      sbResult.append("</textarea>");
      out.println( sbResult );
      out.flush();
    }
    long t01 = System.currentTimeMillis();
    
    out.println("<br><br><div class=\"hr\">&nbsp;</div>All jobs done in "+(t01-t00)+" msec (including NLP preprocessing).<br>\n" +
        "Google App Engine Performance Settings:<br>\n" +
        "Frontend Instance Class: F2 (1200Hz, 256MB)<br>\n" +
        "Max Idle Instances: 1<br>\n" +
        "Min Pending Latency: 15.0s <br><br><br>\n");
  }
  
//  @Override
  public void lazyinit() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
      WS4JConfiguration.getInstance().setMFS(false);
      WS4JConfiguration.getInstance().setLeskNormalize(false);
      WS4JConfiguration.getInstance().setCache(true);
      AbstractWordNet wn = WordNetFactory.getCachedInstanceForName(InMemoryWordNetAPI.class.getCanonicalName());
      Factory f = new Factory(wn);
      Measure[] measures = {Measure.WUP, Measure.RES, Measure.JCN, 
              Measure.LIN, Measure.LCH, Measure.PATH, Measure.LESK, 
              Measure.HSO};
      rcs = new LinkedHashMap<Measure,RelatednessCalculator>();
      for (Measure m : measures) {
        rcs.put(m, f.create(m));
      }
      OpenNLPSingleton.INSTANCE.tokenize("");
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    long diff = t1-t0;
    if (diff > 500) System.err.println("Warming up done in "+diff+" msec.");
  }
  
  private POS mapPOS( String pennTreePosTag ) {
    if (pennTreePosTag.indexOf("NN")==0) return POS.n;
    if (pennTreePosTag.indexOf("VB")==0) return POS.v;
    if (pennTreePosTag.indexOf("JJ")==0) return POS.a;
    if (pennTreePosTag.indexOf("RB")==0) return POS.r;
    return null;
  }
  
  private String getHeader() {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("<title>WS4J Demo</title>\n");
    sb.append("<meta charset=\"utf-8\" />\n");
    sb.append("<link rel=\"stylesheet\" href=\"/css/demo.css\">\n");
    sb.append("<link rel=\"stylesheet\" href=\"/css/tipsy.css\">\n");
    sb.append("<link rel=\"stylesheet\" href=\"http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css\" />\n");
    sb.append("<link rel=\"stylesheet\" href=\"/css/bootstrap.css\">\n");
    sb.append("<script type=\"text/javascript\" src=\"//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js\"></script>\n");
    sb.append("<script type=\"text/javascript\" src=\"//ajax.googleapis.com/ajax/libs/jqueryui/1.10.2/jquery-ui.min.js\"></script>\n");
    sb.append("<script type=\"text/javascript\" src=\"/js/demo.js\"></script>\n");
    sb.append("<script type=\"text/javascript\" src=\"/js/jquery.tipsy.js\"></script>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    return sb.toString();
  }
  
  private String getHeader2() {
    StringBuilder sb = new StringBuilder();
    sb.append( "<h1><a href=\"/\" style=\"text-decoration:none;color:#333333\">WS4J Demo</a></h1>\n" +
        "WS4J (WordNet Similarity for Java) measures semantic similarity/relatedness between words.<br><br>\n" );
    sb.append("<div id=\"progress_container\">WordNet loading status <div style=\"width:220px; display:inline-block\">\n");  
    sb.append("<div class=\"progress progress-info progress-striped active\" style=\"margin-top: 4px;margin-bottom: 0px;\">\n");  
    sb.append("<div id=\"progress\" class=\"bar\"></div>\n");
    sb.append("</div>\n</div>\n</div>\n");  
    return sb.toString();
  }
}
