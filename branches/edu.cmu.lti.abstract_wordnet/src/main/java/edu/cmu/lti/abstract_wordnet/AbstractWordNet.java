/*
 * Copyright 2009-2013 Carnegie Mellon University
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.cmu.lti.abstract_wordnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractWordNet {

  /**
   * Given word and pos, find the synsets associated with them.
   * @param wordLemma
   * @param pos
   * @return synsets or empty collection if N/A
   */
  public abstract List<Synset> getSynsets(String wordLemma, POS pos);
  
//  public abstract List<String> getSynsetsByWordLemma( String wordLemma );

  public Synset getSynset( String wordLemma, POS pos, int senseId ) {
    List<Synset> synsets = getSynsets(wordLemma, pos);
    if ( synsets==null ) return null;
    if ( synsets.size() < senseId ) return null;
    return synsets.get(senseId-1);
  }

  /**
   * Get word lemmas given synset.
   * 
   * The result should ideally be sorted by frequency (in descending order),
   * for tracing purpose. See getSynsetLabel().
   * 
   * Used by HSO algorithm and for tracing purpose.
   * 
   * @see getSynsetLabel()
   * @param synsetId
   * @return word lemmas associated with the given synset
   */
  public abstract List<String> getWordLemmas(String synsetId);

  /**
   * Given a synset s and link l, find synsets S
   * that are connected to s with a link relation l
   * @param synsetId
   * @param link
   * @return glosses or empty collection if N/A
   */
  public abstract List<Synset> getLinkedSynsets(Synset synset, Link link);
  
  /**
   * Given a synset s, word w and link l, first find synsets and words
   * that are connected to s with a link relation l.
   * 
   * The following links are defined over (synset,word) and (synset,word)
   * rather than (synset) and (synset): 
   * also, dmnc, dmnu, dmnr, dmtc, dmtu, dmtr, ants, vgrp, deri, part, defa, pert
   * 
   * When the word is null, return synsets assuming corresponding word is given.
   * 
   * @param synsetId
   * @param word
   * @param link
   * @return glosses or empty collection if N/A
   */
  public abstract List<Synset> getLinkedWords(String synsetId, String word, Link link);

  /**
   * Given a synset s, find gloss (dictionary definition) of s  
   * 
   * @param synsetId
   * @return gloss
   */
  public abstract String getGloss( String synsetId );

  /**
   * Given word and pos, find the most frequent synset
   * (in wordnet, it's the top item).
   * @param word
   * @param pos
   * @return most frequent synset or null if N/A
   */
  public Synset getMostFrequentConcept(String wordLemma, POS pos) {
    List<Synset> result = getSynsets(wordLemma, pos);
    return result.size()>0 ? result.get(0) : null;
  }

  /**
   * Given a synset id s, find synset(s) of  
   * direct horizontal term(s) of s.
   * 
   * Such terms are linked with relations such as: ants, attr, sim.
   * 
   * @param synsetId
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getHorizontals(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.also, Link.attr, Link.sim, 
            Link.ants, Link.pert} );
  }
  
  /**
   * Given a synset id s, find synset(s) of  
   * direct upward term(s) of s.
   * 
   * Such terms are linked with relations such as: 
   * hype, mero, mmem, mprt, msub
   * 
   * Used by HSO algorithm.
   * 
   * @param synset
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getUpwards(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.hype, Link.inst,//hypes 
            Link.mmem, Link.msub,Link.mprt } );//mero 
  }

  /**
   * Given a synset id s, find synset(s) of  
   * direct downward term(s) of s.
   * 
   * Such terms are linked with relations such as: 
   * caus, enta, holo, hmem, hsub, hprt, hypo.
   * 
   * Used by HSO algorithm.
   * 
   * @param synset
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getDownwards(Synset synset) {
    return getLinkedSynsets( synset,
            new Link[]{Link.hmem, Link.hsub, Link.hprt,//holo 
            Link.hypo, Link.hasi, //hypos
            Link.caus, Link.enta, } );//etc
  }
  
  public List<Synset> getHypes(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.hype, Link.inst} );
  }
  public List<Synset> getHypos(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.hypo, Link.hasi} );
  }
  public List<Synset> getAllMeronyms(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.mmem, Link.msub,Link.mprt} );
  }
  public List<Synset> getAllHolonyms(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.hmem,Link.hsub,Link.hprt} );
  }
  public List<Synset> getAllDomain(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.dmnc,Link.dmnu,Link.dmnr} );
  }
  public List<Synset> getAllMemberOfDomain(Synset synset) {
    return getLinkedSynsets( synset, 
            new Link[]{Link.dmtc,Link.dmtu,Link.dmtr} );
  }
  
  //FIXME: before abstract wn release
  boolean lenient = true;
  
  protected List<Synset> getLinkedSynsets( Synset synset, Link[] links ) {
    List<Synset> retval = new ArrayList<Synset>();
    for ( Link link : links ) {
      if (link.isDefinedAmongSynsets()) {
        retval.addAll( getLinkedSynsets( synset, link ) );
      }
      if (link.isDefinedAmongWords() && (lenient || synset.getWord()!=null)) {
        retval.addAll( getLinkedWords( synset.getSynsetId(), synset.getWord(), link ) );
      }
    }
    Set<String> history = new HashSet<String>();
    List<Synset> duplicated = new ArrayList<Synset>();
    for ( Synset s : retval ) {
      if (history.contains(s.getSynsetId())) {
        duplicated.add(s);
      } else {
        history.add(s.getSynsetId());
      }
    }
    retval.removeAll(duplicated);
    return retval;
  }
  
  /**
   * Given a synset id, get human-readable synset label e.g. "jogging#n#1"
   * 
   * There can be N ways to represent such label given synset with N words.
   * This method uses the most frequent word out of N words, assuming that
   * getWordLemmas returns a sorted list by frequency.
   * 
   * @see getWordLemmas()
   * @param synsetId
   * @return glosses or empty collection if N/A
   */
  public String getSynsetLabel( String synsetId ) {
    if (synsetId.equals("0")) return "*ROOT*";
//    String name = getNameOfSynset(synsetId);
    String mfw = getWordLemmas(synsetId).get(0);
    return getSynsetLabel( synsetId, mfw );
  }
  
  /**
   * Given a synset id, get human-readable synset label e.g. "jogging#n#1"
   * 
   * There can be N ways to represent such label given synset with N words.
   * This method uses the most frequent word out of N words, assuming that
   * getWordLemmas returns a sorted list by frequency.
   * 
   * @see getWordLemmas()
   * @param synsetId
   * @return glosses or empty collection if N/A
   */
  public String getSynsetLabel( String synsetId, String lemma ) {
    if (synsetId.equals("0")) return "*ROOT*";
    POS pos = getPOS(synsetId);
    List<Synset> synsets = getSynsets(lemma, pos);
    int num = -1;
    for ( int i=0; i<synsets.size(); i++ ) {
      Synset s = synsets.get(i);
      if (synsetId.equals(s.getSynsetId())) {
        num = i+1;
        break;
      }
    }
    return lemma+"#"+pos.toString()+"#"+num;
    
//    Below is wrong implementation
//    
//    String name = getNameOfSynset(synsetId);
//    List<String> words = getWordLemmas(synsetId);
//    int num = -1;
//    for ( int i=0; i<words.size(); i++ ) {
//      String w = words.get(i);
//      if (w.equals(name)) {
//        num = i+1;
//        break;
//      }
//    }
//    return name+"#"+getPOS(synsetId)+"#"+num;
  }
  
  /**
   * Given synsetId, get POS.
   * 
   * Below assumes format "00628390-n"
   * 
   * In case you prefer the format "n00628390", 
   * override this method to get the first character instead.
   * 
   * @param synsetId
   * @return POS
   */
  public POS getPOS( String synsetId ) {
    try {
      return POS.valueOf(synsetId.charAt(synsetId.length()-1)+"");
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("SynsetID format \""+synsetId+"\" is different from the default. Please override getPOS() method properly.");
      return null;
    }
  }
  
  /**
   * (Optional method to implement)
   *  
   * @return all words (lemmas) available for look up in WordNet
   */
  public abstract Set<String> dumpWords();

}
