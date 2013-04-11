package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.POS;
import edu.cmu.lti.abstract_wordnet.Synset;
import edu.cmu.lti.abstract_wordnet.WordNetFactory;
import edu.cmu.lti.ram_wordnet.OnMemoryWordNet;


@SuppressWarnings("serial")
public class WordSuggestionServlet extends HttpServlet {

  private AbstractWordNet wn = WordNetFactory.
          getCachedInstanceForName("edu.cmu.lti.ram_wordnet.OnMemoryWordNetAPI");
  private Set<String> words = wn.dumpWords();
  private final static int SUGGEST_NUM = 30;
  private final static int MAX_DEF_LENGTH = 100; //in characters
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("application/json; charset=utf-8");
    String query = req.getParameter("term");
    if (query==null) return;
    res.getWriter().print(suggest(query));
    res.getWriter().flush();
  }
  
  private String suggest(String q) {
    List<String> matchedWords = new ArrayList<String>();
    q = OnMemoryWordNet.cannonicalize(q).trim();
    String[] split = q.split("#");
    try {
      if (split.length==1) {
        if (q.endsWith("#")) {
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
        } else {
          for (String w : words) {
            if (w.startsWith(q)) {
              matchedWords.add(w);
            }
          }
          if (matchedWords.size()==0) return "[]";
          Collections.sort(matchedWords);
          int size = Math.min(matchedWords.size(), SUGGEST_NUM);
          List<String> suggestions = new ArrayList<String>(size);
          for ( int i=0; i<size; i++ ) {
            suggestions.add(matchedWords.get(i));
          }
          StringBuilder sb = new StringBuilder();
          for ( String s : suggestions ) {
            sb.append( sb.length() > 0 ? "," : "" );
            sb.append("{\"label\":\""+s+"\"," +
                "\"value\":\""+s+"\"," +
                "\"desc\":\"\"}");
          }
          return "["+sb+"]";
        }
      } else if (split.length==2) {
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
    } catch (Exception e) {
      return "[]";
    }
    return "[]";
  }
  
  private String process( String def ) {
    String s = def.replaceAll("\"", "'");
    return (s.length()<MAX_DEF_LENGTH) ? s : s.substring(0, MAX_DEF_LENGTH)+" ...";
  }
}
