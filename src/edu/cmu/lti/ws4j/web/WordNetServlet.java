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
import edu.cmu.lti.ram_wordnet.InMemoryWordNet;
import edu.cmu.lti.ram_wordnet.InMemoryWordNetAPI;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.washington.cs.knowitall.morpha.MorphaStemmer;

@SuppressWarnings("serial")
public class WordNetServlet extends HttpServlet {

  private AbstractWordNet wn = null;
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
          throws IOException {
    res.setContentType("text/plain; charset=utf-8");
    String mode = req.getParameter("mode");
    String query = req.getParameter("q");
    if (mode==null || mode.length()==0 || query==null || query.length()==0) {
      res.getWriter().print("");
      return;
    }
    if ( mode.equals("validate") ) {
      try {
        res.getWriter().print(validation(query));
      } catch (Exception e) {
        //e.printStackTrace();
        res.getWriter().print("<i class=\"red\">Invalid input</i>: <b>"+query+"</b> not found in WordNet");
      }
    } else if ( mode.equals("def") ) {
      try {
        res.getWriter().print(getDefinition(query));
      } catch (Exception e) {
        //e.printStackTrace();
        res.getWriter().print("");
      }
    }
    res.getWriter().flush();
  }
  
  private String validation(String q) throws Exception {
    if (wn==null) lazyinit();
    q = InMemoryWordNet.cannonicalize(q.trim());
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
        return getInvalidMessage(q);
      }
    } else if (split.length==2) {
      String w = split[0];
      POS p = POS.valueOf(split[1]);
      List<Synset> synsets = wn.getSynsets(w, p);
      if (synsets.size()>0) {
        return "<i class=\"green\">Valid input</i>: WordNet contains "+
                q+"#"+p+(synsets.size()==1 ? "1" : "{1-"+synsets.size()+"}");
      } else {
        return getInvalidMessage(q);
      }
    } else if (split.length==3) {
      String w = split[0];
      POS p = POS.valueOf(split[1]);
      int num = Integer.parseInt(split[2]);
      List<Synset> synsets = wn.getSynsets(w, p);
      if (num<=synsets.size()) {
        return "<i class=\"green\">Valid input</i>: WordNet contains <b>"+q+"</b>";
      } else {
        return getInvalidMessage(q);
      }
    } else {
      return getInvalidMessage(q);
    }
  }
  
  private String getInvalidMessage( String q ) {
    String stem = MorphaStemmer.stemToken(q);
    boolean stemmingAvailable = false;
    if (!stem.equals(q)) {
      for ( POS p : POS.values() ) {
        List<Synset> synsets = wn.getSynsets(stem, p);
        if (synsets.size()>0) {
          stemmingAvailable = true;
          break;
        }
      }
    }
    return "<i class=\"red\">Invalid input</i>:  <b>"+q+"</b> not found in WordNet."
       +(stemmingAvailable? " Try with its lemma <b>"+stem+"</b> instead.":"");
  }

  private String getDefinition(String q) throws Exception {
    if (wn==null) lazyinit();
    String[] items = q.trim().split("#");
    String w = items[0];
    Synset s = wn.getSynset(w, POS.valueOf(items[1]), Integer.parseInt(items[2]));
    StringBuilder sb = new StringBuilder();
    List<String> lemmas = wn.getWordLemmas(s.getSynsetId());
    for ( String lemma : lemmas ) {
      sb.append(sb.length()>0?", ":"");
      sb.append(lemma.equalsIgnoreCase(w) ? "<b>"+lemma+"</b>":lemma);
    }
    String ge = wn.getGloss(s.getSynsetId());//gloss and example
    int pos = ge.indexOf("\"");
    if (pos>0) {
      sb.append("<br>("+ge.substring(0,pos)+") <i>"+ge.substring(pos)+"</i>");
    } else {
      sb.append("<br>("+ge+")");
    }
    return "S: ("+s.getPos()+") "+sb.toString();
  }
  
  public void lazyinit() throws ServletException {
    long t0 = System.currentTimeMillis();
    try {
      WS4JConfiguration.getInstance().setMFS(false);
      WS4JConfiguration.getInstance().setLeskNormalize(false);
      WS4JConfiguration.getInstance().setCache(true);
      wn = WordNetFactory.getCachedInstanceForName(InMemoryWordNetAPI.class.getCanonicalName());
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    long diff = t1-t0;
    if (diff > 500) System.err.println("Warming up done in "+diff+" msec.");
  }
}
