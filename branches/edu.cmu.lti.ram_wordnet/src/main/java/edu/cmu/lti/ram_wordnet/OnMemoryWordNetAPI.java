package edu.cmu.lti.ram_wordnet;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class OnMemoryWordNetAPI extends AbstractWordNet {

//  private final static boolean FAST_MODE = true; // uses more memory but runs faster
  
  private OnMemoryWordNet wn = new OnMemoryWordNet();
  
  @Override
  public List<Synset> getSynsets(String word, POS pos) {
    List<Synset> retval = new ArrayList<Synset>();
    if (word==null || pos==null || word.length()==0 ) return retval;
    String lcWord = OnMemoryWordNet.cannonicalize(word);
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
          Synset s = new Synset(strSynset, lcWord, pos);
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
  public List<Synset> getLinkedSynsets(Synset synset, Link link) {
    List<Synset> retval = new ArrayList<Synset>();
    if (!link.isDefinedAmongSynsets()) return retval;
    LinkedSynsets ls = wn.synset2synset[wn.dictS.get(synset.getSynsetId())];
    if (ls==null) return retval;
    for (int i = 0; i < ls.size(); i++) {
      if (ls.getLinks()[i] == link) {
        for (int sidIndex : ls.getSynsetIndices()[i]) {
          retval.add(new Synset(wn.dictS.inverse().get(sidIndex)));
        }
        break;
      }
    }
    return retval;
  }
  
  @Override
  public List<Synset> getLinkedWords(String synsetId, String word, Link link) {
    List<Synset> retval = new ArrayList<Synset>();
    if (!link.isDefinedAmongWords()) return retval;
    LinkedWords lw = wn.word2words[wn.dictS.get(synsetId)];
    if (lw==null) return retval;
//    Integer linkIndex = wn.dictL.get(linkString);
    boolean lenientMode = word==null;
    Integer wordIndex = null;
    if (!lenientMode) {
      word = OnMemoryWordNet.cannonicalize(word);
      wordIndex = wn.dictW.get(word);
      if (wordIndex==null) {
        if (OnMemoryWordNet.LC_KEY) {
          return retval;
        }
        wordIndex = wn.dictW.get(word.toLowerCase());
        if (wordIndex==null) {
          return retval;
        }
      }
    }
    for (int i = 0; i < lw.size(); i++) {
      if (lw.getLink()[i]!=link) continue;
      if (!lenientMode && lw.getWord()[i] != wordIndex) continue;
      for ( int j=0; j<lw.getLinkedSynsets()[i].length; j++ ) {
        int sid = lw.getLinkedSynsets()[i][j];
        int wid = lw.getLinkedWords()[i][j];
        Synset synset = new Synset( 
                wn.dictS.inverse().get(sid),
                wn.dictW.inverse().get(wid),
                null);
        retval.add( synset );
      }
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
      OnMemoryWordNetAPI wn = new OnMemoryWordNetAPI();
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
      System.out.println(wn.getSynset("stellar", POS.a, 2).getSynsetId());
      String sid = wn.getSynset("stellar", POS.a, 2).getSynsetId();
      System.out.println(wn.getLinkedWords(sid, "stellar", Link.pert));
      System.out.println(wn.getLinkedWords("00002325-v", "respire", Link.deri));
      System.out.println(">> "+wn.getWordLemmas("00001981-r"));
      System.out.println("horiz = "+wn.getHorizontals(wn.getSynset("good_health", POS.n, 1)));
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println("Warming up done in "+(t1-t0)+" msec.");
  }

}
