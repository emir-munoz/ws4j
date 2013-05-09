package edu.cmu.lti.ws4j.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.POS;
import edu.cmu.lti.abstract_wordnet.Synset;
import edu.cmu.lti.abstract_wordnet.WordNetFactory;
import edu.cmu.lti.ram_wordnet.OnMemoryWordNet;
import edu.cmu.lti.ram_wordnet.OnMemoryWordNetAPI;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

@SuppressWarnings("serial")
public class WordNetServlet extends HttpServlet {

  private AbstractWordNet wn = null;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("application/json; charset=utf-8");
    String query = req.getParameter("q");
    if (query==null || query.length()==0) {
      res.getWriter().print("");
      return;
    }
    try {
      res.getWriter().print(wnValidation(query));
    } catch (Exception e) {
      res.getWriter().print("<i class=\"red\">Invalid input</i>: <b>"+query+"</b> not found in WordNet");
    }
    res.getWriter().flush();
  }
  
  private String wnValidation(String q) throws Exception {
    if (wn==null) lazyinit();
    q = OnMemoryWordNet.cannonicalize(q).trim();
    if (q.endsWith("#")) {
      q = q.substring(0, q.length()-1);
    }
    String[] split = q.split("#");
    StringBuilder sb = new StringBuilder();
    if (split.length==1) {//when a word is typed
      POS[] poses = POS.values();
      for ( POS p : poses ) {
        List<Synset> synsets = wn.getSynsets(q, p);
        if (synsets.size()>0) {
          sb.append( sb.length()>0?" and ":"<i class=\"green\">Valid input</i>: WordNet contains " );
          if (synsets.size()==1) {
            sb.append( "<b>"+q+"#"+p+"#1</b>" );
          } else {
            sb.append( "<b>"+q+"#"+p+"#{1-"+synsets.size()+"}</b>" );
          }
        }
      }
      if (sb.length()>0) {
        return sb.toString();
      } else {
        return "<i class=\"red\">Invalid input</i>: <b>"+q+"</b> not found in WordNet.";
      }
    } else if (split.length==2) {
      String w = split[0];
      POS p = POS.valueOf(split[1]);
      List<Synset> synsets = wn.getSynsets(w, p);
      if (synsets.size()>0) {
        return "<i class=\"green\">Valid input</i>: WordNet contains "+
                q+"#"+p+(synsets.size()==1 ? "1" : "{1-"+synsets.size()+"}");
      } else {
        return "<i class=\"red\">Invalid input</i>: <b>"+q+"</b> not found in WordNet.";
      }
    } else if (split.length==3) {
      String w = split[0];
      POS p = POS.valueOf(split[1]);
      int num = Integer.parseInt(split[2]);
      List<Synset> synsets = wn.getSynsets(w, p);
      if (num<=synsets.size()) {
        return "<i class=\"green\">Valid input</i>: WordNet contains <b>"+q+"</b>";
      } else {
        return "<i class=\"red\">Invalid input</i>:  <b>"+q+"</b> not found in WordNet.";
      }
    } else {
      return "<i class=\"red\">Invalid input</i>:  <b>"+q+"</b> not found in WordNet.";
    }
  }
  
//  @Override
  public void lazyinit() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
      WS4JConfiguration.getInstance().setMFS(false);
      WS4JConfiguration.getInstance().setLeskNormalize(false);
      WS4JConfiguration.getInstance().setCache(true);
      wn = WordNetFactory.getCachedInstanceForName(OnMemoryWordNetAPI.class.getCanonicalName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.err.println("Warming up done in "+(t1-t0)+" msec.");
  }
}
