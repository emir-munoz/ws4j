package edu.cmu.lti.ws4j.ramwn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    POSAndSynsets ps = wn.word2synsets[wn.dictW.get(word)];
    int[] indices = null;
    for ( int i=0; i<ps.size(); i++ ) {
      if (ps.getPos()[i]==pos) {
        indices = ps.getSynsets()[i];
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
    return wn.synset2gloss[wn.dictS.get(synsetId)];
  }

  @Override
  public List<String> getLinkedSynsets(String synsetId, String linkString) {
    List<String> retval = new ArrayList<String>();
    LinkedSynsets ls = wn.synset2synset[wn.dictS.get(synsetId)];
    if (ls==null) return retval;
    for (int i = 0; i < ls.size(); i++) {
      if (ls.getLinkIndex()[i] == wn.dictL.get(linkString)) {
        for (int sidIndex : ls.getSynsetIndices()[i]) {
          retval.add(wn.dictS.inverse().get(sidIndex));
        }
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
}
