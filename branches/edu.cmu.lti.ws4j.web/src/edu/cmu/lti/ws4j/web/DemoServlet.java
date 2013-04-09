package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.io.PrintWriter;
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
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.RelatednessCalculator.Parameters;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.ramwn.OnMemoryWordNetAPI;
import edu.washington.cs.knowitall.morpha.MorphaStemmer;

@SuppressWarnings("serial")
public class DemoServlet extends HttpServlet {

  public final static String sample1 = "Eventually, a huge cyclone hit the entrance of my house.";
  public final static String sample2 = "Finally, a massive hurricane attacked my home.";
  
  private static final NumberFormat nf = NumberFormat.getNumberInstance();
  static {
    nf.setMaximumFractionDigits(3);
//    nf.setMinimumFractionDigits(3);
  }
  
  private AbstractWordNet wn;
  private Map<String,RelatednessCalculator> rcs;
//  private KStemmer stemmer;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("text/html");
    res.getWriter().println( getHeader() );
    res.getWriter().println( "<h1>WS4J Demo</h1>\n" +
    		"WS4J (WordNet Similarity for Java) measures semantic similarity/relatedness between words.<br><br>\n" );

    String w1 = req.getParameter("w1");
    String w2 = req.getParameter("w2");
    String measure = req.getParameter("measure");
    String s1 = req.getParameter("s1");
    String s2 = req.getParameter("s2");
    if ( w1!=null && w2!=null ) {
      runOnWords( res.getWriter(), w1, w2, measure );
    } else {//default or when s1 & s1 exist
      runOnSentences( res.getWriter(), s1, s2 );
    }
    
    res.getWriter().println( "<hr><a href=\"https://code.google.com/p/ws4j/\">WS4J</a> demo is maintained by <a href=\"http://www.cs.cmu.edu/~hideki\">Hideki Shima</a>.\n" );
    res.getWriter().println( "</body>\n</html>\n" );
  }

  public void runOnWords( PrintWriter out, String w1, String w2, String measure ) {
//    WS4JConfiguration.getInstance().setTrace(true);//doesn't work here
//    long t00 = System.currentTimeMillis();
    List<String> measures;
    if (measure!=null) {
      measures = new ArrayList<String>();
      measures.add(measure);
    } else {
      measures = new ArrayList<String>(rcs.keySet());
    }
    RelatednessCalculator.enableTrace = true;
    for ( String m : measures ) {
      RelatednessCalculator rc = rcs.get(m);
//      long t0 = System.currentTimeMillis();
      StringBuilder sbResult = new StringBuilder();
      sbResult.append( "<h2>"+m+"</h2>\n" );
      if (m.equals("LESK")) {
        sbResult.append(m+" demo is coming soon.");
      } else {
        Relatedness r = rc.calcRelatednessOfWords(w1, w2, true);
        String log = r.getTrace().replaceAll("\n", "<br>\n");
        StringBuffer sb = new StringBuffer();
        Pattern patter = Pattern.compile("((^|\\n)[a-z]{3}\\(.+?\\) = [0-9.Ee-]+)");
        Matcher matcher = patter.matcher(log);
        while ( matcher.find() ) {
          matcher.appendReplacement(sb, "<h3>"+matcher.group(1)+"</h3>");
        }
        matcher.appendTail(sb);
        sbResult.append(sb.toString());
//        long t1 = System.currentTimeMillis();
//        sbResult.append("<textarea rows=\"6\" cols=\"70\">");
        sbResult.append("<br><h3>Parameters</h3><ul>\n");
        Parameters param = rc.dumpParameters();
        for ( String key : param.keySet() ) sbResult.append( "<li>"+key+" = "+param.get(key)+"</li>\n" );  
        sbResult.append("</ul>");
      }
      out.println( sbResult );
    }
    RelatednessCalculator.enableTrace = false;
    
//    WS4JConfiguration.getInstance().setTrace(false);
//    long t01 = System.currentTimeMillis();
  }
  
  public void runOnSentences( PrintWriter out, String s1, String s2 ) {
    StringBuilder sbForm = new StringBuilder();
    sbForm.append("<form action=\"/\" method=\"get\">\n");
    sbForm.append("Type in sentences to process, or use example sentences by clicking on:\n");
    sbForm.append("&nbsp;<input type=\"button\" value=\"  insert sample sentences  \" onclick=\"insert_sample()\"><br><br>\n");
    sbForm.append("&nbsp;<textarea rows=\"4\" cols=\"40\" id=\"s1\" name=\"s1\">"+(s1==null?"":s1)+"</textarea><br>\n");
    sbForm.append("&nbsp;<textarea rows=\"4\" cols=\"40\" id=\"s2\" name=\"s2\">"+(s2==null?"":s2)+"</textarea><br>\n");
    sbForm.append("&nbsp;<input type=\"submit\" value=\"  Calculate  \"><br>\n");
    sbForm.append("</form>\n");
    if (s1==null || s2==null) {
      out.println( sbForm );
      return;
    }
    
    sbForm.append("[Tips] In this demo, the following preprocessing are done before WS4J: tokenization, POS tagging, lemmatization.<br>\n" +
        "If you see unexpected results due to errors in preprocessing, simply type in a list of lemmatized words.<br>\n");
    out.println( sbForm );
    
    String[] words1 = OpenNLPSingleton.INSTANCE.tokenize(s1);
    String[] words2 = OpenNLPSingleton.INSTANCE.tokenize(s2);
    String[] postag1 = OpenNLPSingleton.INSTANCE.postag(words1);
    String[] postag2 = OpenNLPSingleton.INSTANCE.postag(words2);
    long t00 = System.currentTimeMillis();
    for ( String m : rcs.keySet() ) {
      RelatednessCalculator rc = rcs.get(m);
      long t0 = System.currentTimeMillis();
      StringBuilder sbResult = new StringBuilder();
      sbResult.append( "<h2>"+m+"</h2>\n" );
      if (m.equals("LESK")) {
        sbResult.append(m+" demo is coming soon.");
      } else {
        sbResult.append("<table border=1><tr><td class=\"th\">&nbsp;</td>");
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
              Relatedness r = rcs.get(m).calcRelatednessOfWords(w1+"#"+p1.toString(), w2+"#"+p2.toString(), true);
              d = r.getScore();
              error = r.getError().length()>0;
              errorMsg = r.getError();
              popup = m+"('"+(r.getSynset1()==null?w1+"#"+p1:r.getSynset1())+"', '"+(r.getSynset2()==null?w2+"#"+p2:r.getSynset2())+"')";
            }
            String dText;
            String url = "ws4j?w1="+w1+"&w2="+w2+"&measure="+m;
            if ( d <= 0 ) {
              if (error) {
                dText = "<span class=\"g\" title=\""+popup+" = "+errorMsg+"\">-</span>";
              } else {
                dText = "<span class=\"g\" title=\""+popup+" = 0\"><a href=\""+url+"\" target=\"_blank\">0</a></span>";
              }
            } else {
              if ( Double.MAX_VALUE - d < 10e-9 ) {//MAX
                dText = "<span title=\""+popup+" = INF\"><i><a href=\""+url+"\" target=\"_blank\">MAX</a></i></span>";
              } else {
                dText = "<span title=\""+popup+" = "+d+"\"><a href=\""+url+"\" target=\"_blank\">"+nf.format(d)+"</a></span>";
              }
            }
            sbResult.append("<td>"+dText+"</td>\n");
//            sbResult.append("<td>"+w1+"."+w2+"</td>");
          }
          sbResult.append("</tr>\n");
        }
        sbResult.append("</table><br>\n");
        long t1 = System.currentTimeMillis();
//        sbResult.append("<div title=\"tokenization, pos tagging, stemming, semantic similarity calculation, table generation\">\n</div>");
        sbResult.append("<textarea rows=\"6\" cols=\"70\">");
        sbResult.append("Done in "+(t1-t0)+" msec.\n\nParameters:\n");
        Parameters p = rc.dumpParameters();
        for ( String key : p.keySet() ) sbResult.append( " - "+key+" = "+p.get(key)+"\n" );  
        sbResult.append("</textarea>");
      }
      out.println( sbResult );
    }
    long t01 = System.currentTimeMillis();
    
    out.println("<br><br><hr>All jobs done in "+(t01-t00)+" msec (including NLP preprocessing).<br>\n" +
    		"Google App Engine Performance Settings:<br>\n" +
    		"Frontend Instance Class: F2 (1200Hz, 256MB)<br>\n" +
    		"Max Idle Instances: 1<br>\n" +
    		"Min Pending Latency: 15.0s <br><br><br>\n");
  }
  
  @Override
  public void init() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
//      stemmer = new KStemmer();
      wn = new OnMemoryWordNetAPI();
      rcs = new LinkedHashMap<String,RelatednessCalculator>();
      rcs.put("WUP",  new WuPalmer(wn));
      rcs.put("RES",  new Resnik(wn));
      rcs.put("JCN",  new JiangConrath(wn));
      rcs.put("LIN",  new Lin(wn));
      rcs.put("LCH",  new LeacockChodorow(wn));
      rcs.put("HSO",  new HirstStOnge(wn));
      rcs.put("LESK", new Lesk(wn));
//      SQL.getInstance();
      OpenNLPSingleton.INSTANCE.tokenize("");
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.err.println("Warming up done in "+(t1-t0)+" msec.");
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
    sb.append("<!--<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"\" />-->\n");
    sb.append("<meta charset=\"utf-8\" />\n");
    sb.append("<style>\n");
    sb.append("body,td {\n");
    sb.append("  font-size: 11px;\n");
    sb.append("  font-family: 'ヒラギノ角ゴ Pro W3','Hiragino Kaku Gothic Pro','メイリオ',Meiryo,'ＭＳ Ｐゴシック',sans-serif;\n");
    sb.append("}\n");
    sb.append("h1 { font-weight: bold; font-size: 24px; margin-bottom: 0px; }\n");
    sb.append("h2 { font-weight: bold; font-size: 20px; border-bottom: 1px solid #000000; margin-top: 30px; }\n");
    sb.append("h3 { font-size: 14px; }\n");
    sb.append("table { border:1px solid #666; border-collapse: collapse; border-spacing: 0px; }\n");
    sb.append("td { margin:0px; text-align:right; font-size:11px; border:1px solid #666; border-collapse: collapse; border-spacing: 0px; }\n");
    sb.append("td.th { padding-left:5px; padding-right:5px; font-size:10px; font-weight: bold; text-align:left; padding-left:2px; background-color:rgb(255, 255, 173) }\n");
    sb.append(".g { color:#666666; }\n");
    sb.append("</style>\n");
    sb.append("<script>\n");
    sb.append("function insert_sample() {\n");
    sb.append("  document.getElementById('s1').value = \""+sample1+"\";\n");
    sb.append("  document.getElementById('s2').value = \""+sample2+"\";\n");
    sb.append("}\n");
    sb.append("</script>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    return sb.toString();
  }
}
