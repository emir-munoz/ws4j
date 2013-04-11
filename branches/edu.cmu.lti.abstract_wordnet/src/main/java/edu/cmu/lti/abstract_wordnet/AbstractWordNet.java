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
import java.util.LinkedHashSet;
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
    if (synsets==null) return null; 
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
   * Given a synset s and link l, first find synsets S
   * that are connected to s with a link relation l, then
   * return gloss (dictionary definition) for each members of S  
   * @param synsetId
   * @param linkString
   * @return glosses or empty collection if N/A
   */
  public abstract List<String> getLinkedSynsets(String synsetId, String linkString);

  /**
   * Given a synset s, find gloss (dictionary definition) of s  
   * 
   * @param synsetId
   * @return gloss
   */
  public abstract String getGloss( String synsetId );

  /**
   * Given a synset id (e.g. "00512691-r"), return name of the synset (e.g. "adjectivally") 
   * 
   * Used by Lesk and for tracing.
   * 
   * @param synsetId 
   * @return name of synset
   */
  public String getNameOfSynset( String synsetId ) {
    if ( synsetId==null ) return null;
    if ( synsetId.equals("0") ) return "*ROOT*";
    return _getNameOfSynset(synsetId);
  }
  
  protected abstract String _getNameOfSynset( String synsetId );
  
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
   * direct hypernym(s) of s.
   * @param synsetId
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<String> getHypernyms(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.hype} );
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
  public List<String> getHorizontals(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.also, Link.ants, Link.attr, Link.pert, Link.sim} );
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
   * @param synsetId
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<String> getUpwards(String synsetId) {
    return getLinkedSynsets( synsetId, 
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
   * @param synsetId
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<String> getDownwards(String synsetId) {
    return getLinkedSynsets( synsetId,
            new Link[]{Link.hmem, Link.hsub, Link.hprt,//holo 
            Link.hypo, Link.hasi, //hypos
            Link.caus, Link.enta, } );//etc
  }
  
  public List<String> getHypes(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.hype, Link.inst} );
  }
  public List<String> getHypos(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.hypo, Link.hasi} );
  }
  public List<String> getAllMeronyms(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.mmem, Link.msub,Link.mprt} );
  }
  public List<String> getAllHolonyms(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.hmem,Link.hsub,Link.hprt} );
  }
  public List<String> getAllDomain(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.dmnc,Link.dmnu,Link.dmnr} );
  }
  public List<String> getAllMemberOfDomain(String synsetId) {
    return getLinkedSynsets( synsetId, 
            new Link[]{Link.dmtc,Link.dmtu,Link.dmtr} );
  }
  
  private List<String> getLinkedSynsets( String synsetId, Link[] links ) {
    Set<String> retval = new LinkedHashSet<String>();
    for ( Link link : links ) {
      retval.addAll( getLinkedSynsets( synsetId, link.toString() ) );
    }
    return new ArrayList<String>(retval);
  }
  
  /**
   * Given a synset s and link l, first find synsets S
   * that are connected to s with a link relation l, then
   * return gloss (dictionary definition) for each members of S  
   * @param synsetId
   * @param linkString
   * @return glosses or empty collection if N/A
   */
  public List<String> getGloss(String synsetId, String linkString) {
    List<String> linkedSynsetIds = new ArrayList<String>();
    Link link = null;
    try {
      link = Link.valueOf(linkString);
      linkedSynsetIds = findLinkedSynsetIds(synsetId, link);
    } catch (IllegalArgumentException e) {
      // I know it's not a good use of catching
      // this is how normal gloss is obtained
      // note: use of try-catch slows down
      linkedSynsetIds.add(synsetId);
    }
    
    if (linkString.equals("syns")) {
      
    }

    List<String> glosses = new ArrayList<String>(linkedSynsetIds.size());
    for (String linkedSynsetId : linkedSynsetIds) {
      String gloss = null;
      if (Link.syns.equals(link)) {
        // Special case when you want name assigned to the synset, not the gloss.
        gloss = getNameOfSynset(synsetId);
        if (gloss == null) {
          gloss = getNameOfSynset(linkedSynsetId);
        }
      } else { // This path is more common than above
        gloss = getGloss( linkedSynsetId );
      }

      if (gloss == null ) continue; 
      
      //postprocess
      //gloss = gloss.replaceAll("[^a-zA-Z0-9]", " ");  
      gloss = gloss.replaceAll("[.;:,?!(){}\"`$%@<>]", " ");
      gloss = gloss.replaceAll("&", " and ");
      gloss = gloss.replaceAll("_", " ");
      gloss = gloss.replaceAll("[ ]+", " ");
      gloss = gloss.replaceAll("(?<!\\w)'", " ");
      gloss = gloss.replaceAll("'(?!\\w)", " ");
      gloss = gloss.replaceAll("--", " ");
      gloss = gloss.toLowerCase();
      
      glosses.add( gloss );
    }
    return glosses;
  }

  private List<String> findLinkedSynsetIds(String synsetId, Link link)
          throws IllegalArgumentException {
    List<String> linkedSynsetIds = new ArrayList<String>();
    if (link.equals(Link.mero)) {
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.mmem.toString()));
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.msub.toString()));
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.mprt.toString()));
    } else if (link.equals(Link.holo)) {
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.hmem.toString()));
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.hsub.toString()));
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, Link.hprt.toString()));
    } else if (link.equals(Link.syns)) {
      linkedSynsetIds.add(synsetId);
    } else {
      linkedSynsetIds.addAll(getLinkedSynsets(synsetId, link.toString()));
    }
    return linkedSynsetIds;
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

// public static List<Synset> wordToSynsets( String word, POS pos ) {
//    List<Word> words = WordDAO.findWordsByLemmaAndPos(word, pos);
//    List<Synset> results = new ArrayList<Synset>();
//    for ( Word wordObj : words ) {
//      int wordid = wordObj.getWordid();
//      List<Sense> senses = SenseDAO.findSensesByWordid( wordid );
//      for ( Sense sense : senses ) {
//        Synset synset = new Synset( sense.getSynset(), null, null, null );
//        results.add( synset );
//      }
//    }
//    return results;
//  }
//
//  public static List<Word> synsetToWords( String synset ) { 
//    List<Word> words = new ArrayList<Word>();
//    List<Sense> senses = SenseDAO.findSensesBySynset( synset );
//    for ( Sense sense : senses ) {
//      Word word = WordDAO.findWordByWordid( sense.getWordid() );
//      words.add( word );
//    }
//    return words;
//  }

//  public static Set<String> findSynonyms( String word, POS pos, boolean translate ) {
//    Set<String> results = new LinkedHashSet<String>();
//    List<Synset> synsets = WordNetUtil.wordToSynsets( word, pos );
//    Lang srcLang = findLang( word );
//    Lang anotherLang = srcLang.equals(Lang.jpn)?Lang.eng:Lang.jpn;
//    Lang targetLang = translate?anotherLang:srcLang;
//    for ( Synset synset : synsets ) {
//      List<Sense> moreSenses = SenseDAO.findSensesBySynsetAndLang(synset.getSynset(), targetLang);
//      for ( Sense moreSense : moreSenses ) {
//        Word synonym = WordDAO.findWordByWordid( moreSense.getWordid() );
//        results.add( synonym.getLemma() );
//      }
//    }
//    // remove the original if any
//    results.remove( word );
//    return results;
//  }
  
    
}
