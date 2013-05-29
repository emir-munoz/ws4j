/*
 * Copyright 2013 Carnegie Mellon University
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractWordNet {

  protected final static String MSG_INVALID_POS   = "Invalid POS";
  protected final static String MSG_INVALID_SENSE = "Invalid Sense Number";
  protected final static String MSG_INVALID_WORD  = "Word not found in WordNet";
  
  /**
   * Given a <code>word lemma</code> and a {@link POS}, 
   * find an ordered list of synsets associated with them.
   * 
   * @param wordLemma
   *   lemma of a word that exists in WordNet
   * @param pos
   *   part-of-speech
   * @return synsets sorted by frequency count in descending order, 
   * or empty collection if N/A
   */
  public abstract List<Synset> getSynsets(String wordLemma, POS pos);
  
  /**
   * Find an ordered list of word lemma  
   * given a <code>synsetId</code>.
   * 
   * The result should ideally be sorted by frequency (in descending order),
   * for tracing purpose. See getSynsetLabel().
   * 
   * For WS4J users: this is used by HSO and LESK algorithm for tracing purpose.
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
  public abstract Map<Link,List<Synset>> getLinkedSynsets(Synset synset, List<Link> links);
//public abstract Map<Link,List<Synset>> getLinkedSynsets(Synset synset);

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
   public abstract Map<Link,List<Synset>> getLinkedWords(String synsetId, String word, List<Link> links);
//    public abstract Map<Link,List<Synset>> getLinkedWords(String synsetId, String word);

  /**
   * Given a synset s, find gloss (dictionary definition) of s  
   * 
   * @param synsetId
   * @return gloss
   */
  public abstract String getGloss( String synsetId );

  /**
   * 
   * (Optional method to implement)
   *  
   * Used for input completion/suggestion.
   *  
   * @return all words (lemmas) available for look up in WordNet
   */
  public abstract Set<String> dumpWords();

//  /**
//   * Given word and pos, find the most frequent synset
//   * (in wordnet, it's the top item).
//   * @param word
//   * @param pos
//   * @return most frequent synset or null if N/A
//   */
//  public Synset getMostFrequentConcept(String wordLemma, POS pos) {
//    List<Synset> result = getSynsets(wordLemma, pos);
//    return result.size()>0 ? result.get(0) : null;
//  }

//  private final static boolean lenient = true;
  
//  public Map<Link,List<Synset>> getLinkedSynsetsAndWords( Synset synset ) {
//    Map<Link,List<Synset>> retval = new LinkedHashMap<Link,List<Synset>>(Link.values().length*4/3+1);
//    retval.putAll( getLinkedSynsets( synset ) );
//    retval.putAll( getLinkedWords( synset.getSynsetId(), synset.getWord() ) );
//    
//    Set<String> history = new HashSet<String>();
//    List<Synset> duplicated = new ArrayList<Synset>();
//    for ( Synset s : retval ) {
//      if (history.contains(s.getSynsetId())) {
//        duplicated.add(s);
//      } else {
//        history.add(s.getSynsetId());
//      }
//    }
//    retval.removeAll(duplicated);
//    return retval;
//  }

  public Map<Link,List<Synset>> getLinkedSynsetsAndWords( Synset synset, List<Link> links ) {
    int s = links.size();
    Map<Link,List<Synset>> retval = new LinkedHashMap<Link,List<Synset>>(s*4/3+1);
    List<Link> synsetLinks = new ArrayList<Link>(s);
    List<Link> wordLinks = new ArrayList<Link>(s);
    for ( Link link : links ) {
//      if (!link.isDefinedOnPOS(synset.getPos())) continue; //do it in batch to save time
      if (link.isDefinedAmongSynsets()) {
        synsetLinks.add( link );
      }
      //else if
      if (link.isDefinedAmongWords()) {
  //    if (link.isDefinedAmongWords() && (lenient || synset.getWord()!=null)) {
        wordLinks.add( link );
      }
    }
    if (synsetLinks.size()>0) {
      retval.putAll( getLinkedSynsets( synset, synsetLinks ) );
    }
    if (wordLinks.size()>0) {
      retval.putAll( getLinkedWords( synset.getSynsetId(), synset.getWord(), wordLinks ) );
    }
    return retval;
  }
  
  /**
   * Given a synset id (e.g. 00628390-n), 
   * get human-readable synset label e.g. "jogging#n#1"
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

  public Synset getSynset( String wordLemma, POS pos, int senseId ) throws IllegalArgumentException {
    List<Synset> synsets = getSynsets(wordLemma, pos);
    if ( synsets==null ) throw new IllegalArgumentException(MSG_INVALID_WORD);
    if ( synsets.size() < senseId ) throw new IllegalArgumentException(MSG_INVALID_SENSE);
    return synsets.get(senseId-1);
  }

  /**
   * Input can be a word_lemma (cat), word_lamma+pos (cat#n), 
   * word_lemma+pos+sense_num (cat#n#1). Not synsetId(00002039-n).
   * 
   * @param wps
   * @return synsets
   */
  public List<Synset> getSynsetsFromWPS( String wps ) throws IllegalArgumentException {
    List<Synset> retval = new ArrayList<Synset>();
    if (wps==null || wps.trim().length()==0) throw new IllegalArgumentException(MSG_INVALID_WORD);
    try {
      String[] split = wps.split("#");
      if ( split.length==3 ) {
        String lemma = split[0];
        POS pos = null; 
        try {
          pos = POS.valueOf(split[1]);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(MSG_INVALID_POS);
        }
        int sense = Integer.parseInt(split[2]);
        List<Synset> synsets = getSynsets(lemma, pos);
        if (synsets.size()==0) throw new IllegalArgumentException(MSG_INVALID_WORD);
        if (synsets.size()<sense) throw new IllegalArgumentException(MSG_INVALID_SENSE);
        retval.add(synsets.get(sense-1));
        return retval;
      } else if ( split.length==2 ) {
        String lemma = split[0];
        POS pos = null; 
        try {
          pos = POS.valueOf(split[1]);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException(MSG_INVALID_POS);
        }
        retval = getSynsets(lemma, pos);
        if (retval.size()==0) throw new IllegalArgumentException(MSG_INVALID_WORD);
        return retval;
      } else if ( split.length==1 ) {
        for ( POS pos : POS.values() ) {
          retval.addAll(getSynsets(wps, pos));
        }
        if (retval.size()==0) throw new IllegalArgumentException(MSG_INVALID_WORD);
        return retval;
      }
    } catch ( NumberFormatException e ) {
      throw new IllegalArgumentException(MSG_INVALID_SENSE);
    } catch ( IndexOutOfBoundsException e ) {
      throw new IllegalArgumentException(MSG_INVALID_SENSE);
    }
    return retval;
  }
  
}
