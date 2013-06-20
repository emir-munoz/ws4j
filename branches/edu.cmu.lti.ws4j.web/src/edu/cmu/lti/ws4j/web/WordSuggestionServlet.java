package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.POS;
import edu.cmu.lti.abstract_wordnet.Synset;
import edu.cmu.lti.abstract_wordnet.WordNetFactory;
import edu.cmu.lti.ram_wordnet.InMemoryWordNet;
import edu.cmu.lti.ram_wordnet.InMemoryWordNetAPI;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

@SuppressWarnings("serial")
public class WordSuggestionServlet extends HttpServlet {

  private AbstractWordNet wn = null;
  private List<String> words;
  private final static int SUGGEST_NUM = 30;
  private final static int MAX_DEF_LENGTH = 100; //in characters
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("application/json; charset=utf-8");
    String query = req.getParameter("term");
    if (query==null||query.length()==0) {
      try {
        if (wn==null) init();
      } catch (ServletException e) {
        e.printStackTrace();
      }
      return;
    }
    try {
      res.getWriter().print(suggest(query));
    } catch (Exception e) {
      res.getWriter().print("[]");
    }
    res.getWriter().flush();
  }
  
  private String suggest(String q) throws Exception {
    if (wn==null) lazyinit();
    q = InMemoryWordNet.cannonicalize(q.trim());
    String[] split = q.split("#");
    if (split.length==1) {//when a word is typed
      if (q.endsWith("#")) {//word input complete, now suggest each sense with gloss
        String word = q.replaceFirst("#", "");
        StringBuilder sb = new StringBuilder();
        for ( POS pos : POS.values() ) {
          List<Synset> synsets = wn.getSynsets(word, pos);
          if (synsets.size()==0) continue;
          for ( int i=0; i<synsets.size(); i++ ) {
            Synset s = synsets.get(i);
            String label = word+"#"+pos+"#"+(i+1);
            sb.append( sb.length() > 0 ? "," : "" );
            sb.append("{\"label\":\""+label+"\"," +
                "\"value\":\""+label+"\"," +
                "\"desc\":\""+process(wn.getGloss(s.getSynsetId()))+"\"}");
          }
        }
        return "["+sb+"]";
      } else {//word input not complete, suggest possible lemma entries in wordnet
        List<String> suggestions = new ArrayList<String>(SUGGEST_NUM);
        boolean matched = false;
        for (String w : words) {
          if (w.startsWith(q)) {
            suggestions.add(w);
            matched = true;
          } else {
            //short circuit:
            //when prev matched and not anymore, stop loop
            if (matched) break; 
          }
          if (suggestions.size()==SUGGEST_NUM) break;
        }
        if (suggestions.size()==0) return "[]";
//          Collections.sort(matchedWords);
//          int size = Math.min(matchedWords.size(), SUGGEST_NUM);
//          List<String> suggestions = new ArrayList<String>(size);
//          for ( int i=0; i<size; i++ ) {
//            suggestions.add(matchedWords.get(i));
//          }
        StringBuilder sb = new StringBuilder();
        for ( String s : suggestions ) {
          sb.append( sb.length() > 0 ? "," : "" );
          sb.append("{\"label\":\""+s+"\"," +
              "\"value\":\""+s+"\"," +
              "\"desc\":\"\"}");
        }
        return "["+sb+"]";
      }
    } else if (split.length==2) {//when a word+pos is typed, suggest each sense with gloss
      String word = split[0];
      if (!split[1].matches("[nvar]")) return "[]";
      POS p = POS.valueOf(split[1]);
      List<Synset> synsets = wn.getSynsets(word, p);
      if (synsets.size()==0) return "[]";
      StringBuilder sb = new StringBuilder();
      for ( int i=0; i<synsets.size(); i++ ) {
        Synset s = synsets.get(i);
        String label = word+"#"+p+"#"+(i+1);
        sb.append( sb.length() > 0 ? "," : "" );
        sb.append("{\"label\":\""+label+"\"," +
            "\"value\":\""+label+"\"," +
            "\"desc\":\""+process(wn.getGloss(s.getSynsetId()))+"\"}");
      }
      return "["+sb+"]";
    }
    return "[]";
  }
  

//  @Override
  // Warming up!
  public void lazyinit() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
      OpenNLPSingleton.INSTANCE.toString();//warm up
      WS4JConfiguration.getInstance().setMFS(false);
      WS4JConfiguration.getInstance().setLeskNormalize(false);
      WS4JConfiguration.getInstance().setCache(true);
      //bug in caching?
      wn = WordNetFactory.getCachedInstanceForName(InMemoryWordNetAPI.class.getCanonicalName());
      words = new ArrayList<String>(wn.dumpWords());
      Collections.sort(words);
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    long diff = t1-t0;
    if (diff > 500) System.err.println("Warming up done in "+diff+" msec.");
  }
  
  private String process( String def ) {
    String s = def.replaceAll("\"", "'");
    return (s.length()<MAX_DEF_LENGTH) ? s : s.substring(0, MAX_DEF_LENGTH)+" ...";
  }
}
