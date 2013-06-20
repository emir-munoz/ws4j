package edu.cmu.lti.imw;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.abstract_wordnet.Link;
import edu.cmu.lti.abstract_wordnet.POS;
import edu.cmu.lti.abstract_wordnet.Synset;

/**
 * 
 * @author Hideki Shima
 *
 */
public class InMemoryWordNetAPI extends AbstractWordNet {

//  private final static boolean FAST_MODE = true; // uses more memory but runs faster
  
  private InMemoryWordNet wn = new InMemoryWordNet();
  
  @Override
  public List<Synset> getSynsets(String word, POS pos) {
    List<Synset> retval = new ArrayList<Synset>();
    if (word==null || pos==null || word.length()==0 ) return retval;
    String lcWord = InMemoryWordNet.cannonicalize(word);
//    String lc = word.toLowerCase();//duplicate
    Integer wordIndex = wn.dictW.get(lcWord);
    if (wordIndex==null) return retval;
    POSAndSynsets ps = wn.word2synsets[wordIndex];
    if (ps==null) return retval;
    for ( int i=0; i<ps.size(); i++ ) {
      if (ps.getPos()[i]==pos) {
        int[] indices = ps.getSynsets()[i];
//          String csWord = wn.dictW.inverse().get(ps.getWord()[i]);//case sensitive
        for (Integer index : indices) {
          String strSynset = wn.dictS.inverse().get(index);
          Synset s = new Synset(strSynset, word, pos);
          retval.add(s);
        }
        break;//once found, no need to look further
      }
    }
    return retval;
  }
  
  @Override
  public String getGloss(String synsetId) {
    return String.valueOf(wn.synset2gloss[wn.dictS.get(synsetId)]);
  }
  
  @Override
  public Map<Link,List<Synset>> getLinkedSynsets(Synset synset, List<Link> links) {
    if (InMemoryWordNet.useArrayOrMap) { 
      return LinkedSynsets.getLinkedSynsets(wn, synset, links);
    } else {
      return LinkedSynsetsMap.getLinkedSynsets(wn, synset, links);
    }
  }
  
//  @Override
  @Deprecated
  public Map<Link,List<Synset>> getLinkedWords(String synsetId, String word) {
    LinkedWords lw = wn.word2words[wn.dictS.get(synsetId)];
    if (lw==null) return new LinkedHashMap<Link,List<Synset>>();
    Map<Link,List<Synset>> retval = new LinkedHashMap<Link,List<Synset>>(lw.size()*4/3+1);
    boolean lenientMode = word==null;
    Integer wordIndex = null;
    if (!lenientMode) {
      word = InMemoryWordNet.cannonicalize(word);
      wordIndex = wn.dictW.get(word);
      if (wordIndex==null) {
        if (InMemoryWordNet.LC_KEY) {
          return retval;
        }
        wordIndex = wn.dictW.get(word.toLowerCase());
        if (wordIndex==null) {
          return retval;
        }
      }
    }
    for (int i = 0; i < lw.size(); i++) {
      Link link = lw.getLink()[i];
      if (!lenientMode && lw.getWord()[i] != wordIndex) continue;
      List<Synset> synsets = new ArrayList<Synset>(lw.getLinkedSynsets()[i].length);
      for ( int j=0; j<lw.getLinkedSynsets()[i].length; j++ ) {
        int sid = lw.getLinkedSynsets()[i][j];
        int wid = lw.getLinkedWords()[i][j];
        Synset synset = new Synset( 
                wn.dictS.inverse().get(sid),
                wn.dictW.inverse().get(wid),
                null);
        synsets.add(synset);
      }
      retval.put( link, synsets );
    }
    return retval;
  }

  @Override
  public Map<Link, List<Synset>> getLinkedWords(String synsetId, String word, List<Link> links) {
    List<Link> remainingLinks = new ArrayList<Link>(links);
    LinkedWords lw = wn.word2words[wn.dictS.get(synsetId)];
    if (lw==null) return new LinkedHashMap<Link,List<Synset>>();
    int size1 = lw.size();
    Map<Link,List<Synset>> retval = new LinkedHashMap<Link,List<Synset>>(size1*4/3+1);
    boolean lenientMode = word==null;
    Integer wordIndex = null;
    if (!lenientMode) {
      word = InMemoryWordNet.cannonicalize(word);
      wordIndex = wn.dictW.get(word);
      if (wordIndex==null) {
        if (InMemoryWordNet.LC_KEY) {
          return retval;
        }
        wordIndex = wn.dictW.get(word.toLowerCase());
        if (wordIndex==null) {
          return retval;
        }
      }
    }
    for (int i = 0; i < size1; i++) {
      if (remainingLinks.size()==0) break; 
      Link link = lw.getLink()[i];
      if (!lenientMode && lw.getWord()[i] != wordIndex) continue;
      //order important
      boolean suc = remainingLinks.remove(link);
      if (!suc) continue;
      int size2 = lw.getLinkedSynsets()[i].length;
      List<Synset> synsets = new ArrayList<Synset>(size2);
      for ( int j=0; j<size2; j++ ) {
        int sid = lw.getLinkedSynsets()[i][j];
        int wid = lw.getLinkedWords()[i][j];
        String strSid = wn.dictS.inverse().get(sid);
        POS pos = getPOS(strSid);
        Synset synset = new Synset( 
                strSid,
                wn.dictW.inverse().get(wid),
                pos);
        synsets.add(synset);
      }
      retval.put( link, synsets );
    }
    return retval;
  }

  @Override
  public List<String> getWordLemmas(String synsetId) {
    return Arrays.asList(getWordFast(synsetId));
  }

  private String[] getWordFast(String synsetId) {
    int[] indices = wn.synset2words[wn.dictS.get(synsetId)];
    if (indices==null) return new String[0];
    String[] retval = new String[indices.length];
    for (int i = 0; i < indices.length; i++) {
      retval[i] = wn.dictW.inverse().get(indices[i]);
    }
    return retval;
  }
  
  @Override
  public Set<String> dumpWords() {
    return wn.dictW.keySet();
  }
  
  public static void main(String[] args) {
    long t0 = System.currentTimeMillis();
    try {
      InMemoryWordNetAPI wn = new InMemoryWordNetAPI();
      {
        List<Synset> synsets = wn.getSynsets("earth", POS.n);
        for ( int i=0; i<synsets.size(); i++ ) {
          System.out.println("earth#"+(i+1)+" = "+wn.getWordLemmas(synsets.get(i).getSynsetId()));
        }
      }
      System.out.println("cat#n#(1-7) -> "+wn.getSynsets("cat", POS.n));
      System.out.println("cat#v#(1-2) -> "+wn.getSynsets("cat", POS.v));
      System.out.println("Canis familiaris#n#1 -> "+wn.getSynset("Canis familiaris", POS.n, 1));
      System.out.println("Canis_familiaris#n#1 -> "+wn.getSynset("Canis_familiaris", POS.n, 1));
      System.out.println("canis familiaris#n#1 -> "+wn.getSynset("canis familiaris", POS.n, 1));
      System.out.println("canis_familiaris#n#1 -> "+wn.getSynset("canis_familiaris", POS.n, 1));
      System.out.println("'s_gravenhage -> "+wn.getSynsets("'s_gravenhage", POS.n));
      System.out.println("'s_Gravenhage -> "+wn.getSynsets("'s_Gravenhage", POS.n));
      System.out.println("Google#n -> "+wn.getSynsets("Google", POS.n));
      System.out.println("google#n -> "+wn.getSynsets("google", POS.n));
      System.out.println("Google#v -> "+wn.getSynsets("Google", POS.v));
      System.out.println("google#v -> "+wn.getSynsets("google", POS.v));
      System.out.println("helix -> "+wn.getSynsets("helix", POS.n));
      System.out.println("Helix -> "+wn.getSynsets("Helix", POS.n));
      String sid = wn.getSynset("stellar", POS.a, 2).getSynsetId();
      System.out.println(sid);
      System.out.println(wn.getLinkedWords(sid, "stellar"));
      System.out.println(wn.getLinkedWords(sid, "stellar", new ArrayList<Link>(Arrays.asList(Link.pert))));
      System.out.println(wn.getLinkedWords("00002325-v", "respire"));
      System.out.println(">> "+wn.getWordLemmas("00001981-r"));
      
      edu.cmu.lti.abstract_wordnet.LinkedSynsets linked  = new edu.cmu.lti.abstract_wordnet.LinkedSynsets(wn, 
              wn.getSynset("good_health", POS.n, 1), 
              Arrays.asList(edu.cmu.lti.abstract_wordnet.LinkedSynsets.horizLinks));
      System.out.println("horiz = "+linked.getHorizontals());
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println("Warming up done in "+(t1-t0)+" msec.");
  }

}
