package edu.cmu.lti.ram_wordnet;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
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
    List<Synset> result = new ArrayList<Synset>();
    if (word==null || pos==null || word.length()==0 ) return result;
    Integer wordIndex = wn.dictW.get(OnMemoryWordNet.cannonicalize(word));
    if (wordIndex==null) return result;
    POSAndSynsets ps = wn.word2synsets[wordIndex];
    int[] indices = null;
    for ( int i=0; i<ps.size(); i++ ) {
      if (ps.getPos()[i]==pos) {
        indices = ps.getSynsets()[i];
        break;
      }
    }
    if (indices==null || indices.length==0) return result;
    for ( Integer index : indices ) {
      result.add( new Synset(wn.dictS.inverse().get(index), pos) );
    }
    return result;
  }

  @Override
  public String getGloss(String synsetId) {
    return String.valueOf(wn.synset2gloss[wn.dictS.get(synsetId)]);
  }

  @Override
  public List<String> getLinkedSynsets(String synsetId, String linkString) {
    List<String> retval = new ArrayList<String>();
    LinkedSynsets ls = wn.synset2synset[wn.dictS.get(synsetId)];
    if (ls==null) return retval;
    Integer linkIndex = wn.dictL.get(linkString);
    for (int i = 0; i < ls.size(); i++) {
      if (ls.getLinkIndex()[i] == linkIndex) {
        for (int sidIndex : ls.getSynsetIndices()[i]) {
          retval.add(wn.dictS.inverse().get(sidIndex));
        }
        break;
      }
    }
    return retval;
  }

  @Override
  protected String _getNameOfSynset(String synsetId) {
    return wn.dictW.inverse().get(wn.synset2name[wn.dictS.get(synsetId)]);
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
      System.out.println(new OnMemoryWordNetAPI().getSynset("stellar", POS.a, 2).getSynsetId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println("Warming up done in "+(t1-t0)+" msec.");
  }
}
