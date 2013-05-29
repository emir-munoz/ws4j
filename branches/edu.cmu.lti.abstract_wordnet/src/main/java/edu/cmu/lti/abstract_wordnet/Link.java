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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Enum class for supported lexical relationships.
 * Note that not all are supported in a specific version of WordNet
 * 
 * For the definition of each relationship, see http://wordnet.princeton.edu/man/wngloss.7WN.html
 *
 * Symbol definitions can be found in http://wordnet.princeton.edu/wordnet/man/wninput.5WN.html
 * 
 * Also see query data http://search.cpan.org/dist/WordNet-QueryData/QueryData.pm
 * 
 * @author Hideki Shima
 *
 */
public enum Link {
  // Comments include description and the number of instances found in v0.9
  
  also("^", "Also See", POS.v, POS.a), //See also 2692
  hype("@", "Hypernym", POS.n, POS.v), //Hypernyms 89089
  inst("@i", "Instance hypernym", POS.n), //Instances 8577 //Instance Hypernym
  hypo("~", "Hyponym", POS.n, POS.v), //Hyponym   89089
  hasi("~i","Instance hyponym", POS.n), //Has Instance 8577 //Instance Hyponym
  mero("%", "Meronym", POS.n), //Meronyms 0//FIXME
  mmem("%m", "Member meronym", POS.n), //Meronyms --- Member 12293
  msub("%s", "Substance meronym", POS.n), //Meronyms --- Substance 979
  mprt("%p", "Part meronym", POS.n), //Meronyms --- Part 9097
  holo("#", "Holonym", POS.n), //Holonyms 0//FIXME
  hmem("#m", "Member holonym", POS.n), //Holonyms --- Member 12293
  hsub("#s", "Substance holonym", POS.n), //Holonyms --- Substance 797
  hprt("#p", "Part holonym", POS.n), //Holonyms -- Part 9097
  attr("=", "Attribute", POS.n, POS.a), //Attributes 1278
   sim("&", "Similar To", POS.a),  //Similar to 21386
  enta("*", "Entailment", POS.v), //Entails 408
  caus(">", "Cause", POS.v), //Causes 220
  dmnc("-c", "Domain of synset - TOPIC",  POS.n, POS.v, POS.a, POS.r), //Domain --- Category/TOPIC 6643 / (Domain of synset - TOPIC)
  dmnu("-u", "Domain of synset - USAGE",  POS.n, POS.v, POS.a, POS.r), //Domain --- Usage 967 / (Domain of synset - USAGE)
  dmnr("-r", "Domain of synset - REGION", POS.n, POS.v, POS.a, POS.r), //Domain --- Region 1345 / (Domain of synset - REGION)
  dmtc(";c", "Member of this domain - TOPIC",  POS.n), //In Domain --- Category 6643 / (Member of this domain - TOPIC)
  dmtu(";u", "Member of this domain - USAGE",  POS.n), //In Domain --- Usage 967 / (Member of this domain - USAGE)
  dmtr(";r", "Member of this domain - REGION", POS.n), //In Domain --- Region 1345 / (Member of this domain - REGION)
  ants("!", "Antonym", POS.n, POS.v, POS.a, POS.r), //Antonyms 0
  vgrp("$", "Verb Group", POS.v), //Verb Group
  deri("+", "Derivationally related form", POS.n, POS.v, POS.a, POS.r), //Derivationally related form
  part("<", "Participle", POS.a), //Participle of verb
  //defa("\\"), //Derived from adjective: finer-grained pert used in jwi
  pert("\\", "Pertainym (pertains to nouns) / Derived from adjective", POS.a, POS.r), //Pertainym (adj pertains to nouns)
  syns(null, null), //(words sharing) the same synset//FIXME: remove if unused
  ;
  
  private String symbol;
  private String longName;
  private Set<POS> acceptablePOS;
  
  Link( String symbol, String name, POS ... acceptablePOS ) {
    this.symbol = symbol;
    this.longName = name;
    this.acceptablePOS = new LinkedHashSet<POS>(Arrays.asList(acceptablePOS));
  }
  
  private static final Set<Link> wordLinks = 
          new LinkedHashSet<Link>(Arrays.asList(new Link[]{
            also, 
            dmnc, dmnu, dmnr, dmtc, dmtu, dmtr,
            ants, vgrp, deri, part, pert }));
  
  private static final Set<Link> synsetLinks = 
          new LinkedHashSet<Link>(Arrays.asList(new Link[]{
              also, 
              hype, inst, hypo, hasi, mmem, msub, mprt, 
              hmem, hsub, hprt, attr, sim, enta, caus,
              dmnu, dmnr, dmnc, dmtu, dmtr, dmtc, vgrp }));
  
  public boolean isDefinedAmongWords() {
    return wordLinks.contains(this);
  }
  
  public boolean isDefinedAmongSynsets(){
    return synsetLinks.contains(this);
  }
  
  /**
   * Check if the link is defined on a given POS.
   * When a null value is given, it returns true!
   * 
   * @param pos 
   *   Part-of-speech to check
   * @return whether a link is defined on the pos or not, or true if arg is null
   */
  public boolean isDefinedOnPOS( POS pos ) {
    return pos==null || acceptablePOS.contains(pos);
  }
  
  public String getSymbol() {
    return symbol;
  }

  public String getLongName() {
    return longName;
  }
  
  
}
