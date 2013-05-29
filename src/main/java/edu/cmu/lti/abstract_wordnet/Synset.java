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

/**
 * A conceptual node e.g. WordNet synset.
 * 
 * @author Hideki Shima
 *
 */
public class Synset implements Cloneable {

  /**
   * Offset used as unique id.
   * format "00628390-n" preferred over "n00628390"
   */
  private String synsetId; 

  private POS pos;
  
  /**
   * Original word lemma may be stored out of  
   * multiple word lemmas associated with this synset.
   * 
   * for two purposes
   * 
   * 1. For logging purpose - only one lemma is displayed in the human readable form e.g. "dog#n#1"
   * 2. Some relations (e.g. pertainym, antonym) are defined only on specific lemma in a synset. 
   * HSO/LESK uses such relations.
   * 
   */
  private String word;

  public Synset(String synsetId) {
    this.synsetId = synsetId;
  }

  public Synset(String synsetId, POS pos) {
    this.synsetId = synsetId;
    this.pos = pos;
  }

  public Synset(String synsetId, String word, POS pos) {
    this.synsetId = synsetId;
    this.word = word;
    this.pos = pos;
  }

  /**
   * Dump the content into String in JSON format
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{ ");
    sb.append("\"synsetId\":\"" + synsetId + "\", ");
    sb.append("\"word\":\"" + word + "\", ");
    sb.append("\"pos\":\"" + pos + "\"");
    sb.append(" }");
    return sb.toString();
  }

  /**
   * @return the synset
   */
  public String getSynsetId() {
    return synsetId;
  }

  /**
   * @param synsetId
   *          the synset to set
   */
  public void setSynset(String synsetId) {
    this.synsetId = synsetId;
  }

  /**
   * @return the pos
   */
  public POS getPos() {
    if (pos == null) {
      // Synset realSynset = SynsetDAO.findSynsetBySynset( getSynset() );
      // setName( realSynset.getName() );
      // setPos( realSynset.getPos() );
      // setSrc( realSynset.getSrc() );
    }
    return pos;
  }

  /**
   * @param pos
   *          the pos to set
   */
  public void setPos(POS pos) {
    this.pos = pos;
  }
  
  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }

  @Override
  public Synset clone() {
    return new Synset(synsetId, POS.valueOf(pos.toString()));
  }
}
