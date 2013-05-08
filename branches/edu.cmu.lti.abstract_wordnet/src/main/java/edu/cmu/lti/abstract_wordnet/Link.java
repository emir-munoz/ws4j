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
	
	also("^"), //See also 2692
	hype("@"), //Hypernyms 89089
	inst("@i"), //Instances 8577 //Instance Hypernym
	hypo("~"), //Hyponym   89089
	hasi("~i"), //Has Instance 8577 //Instance Hyponym
	mero("%"), //Meronyms 0
	mmem("%m"), //Meronyms --- Member 12293
	msub("%s"), //Meronyms --- Substance 979
	mprt("%p"), //Meronyms --- Part 9097
	holo("#"), //Holonyms 0
	hmem("#m"), //Holonyms --- Member 12293
	hsub("#s"), //Holonyms --- Substance 797
	hprt("#p"), //Holonyms -- Part 9097
	attr("="), //Attributes 1278
	 sim("&"),  //Similar to 21386
	enta("*"), //Entails 408
	caus(">"), //Causes 220
	dmnc("-c"), //Domain --- Category/TOPIC 6643 / (Domain of synset - TOPIC)
	dmnu("-u"), //Domain --- Usage 967 / (Domain of synset - USAGE)
	dmnr("-r"), //Domain --- Region 1345 / (Domain of synset - REGION)
	dmtc(";c"), //In Domain --- Category 6643 / (Member of this domain - TOPIC)
	dmtu(";u"), //In Domain --- Usage 967 / (Member of this domain - USAGE)
	dmtr(";r"), //In Domain --- Region 1345 / (Member of this domain - REGION)
	ants("!"), //Antonyms 0
	vgrp("$"), //Verb Group
  deri("+"), //Derivationally related form
  part("<"), //Participle of verb
	//defa("\\"), //Derived from adjective: finer-grained pert used in jwi
	pert("\\"), //Pertainym (adj pertains to nouns)
	syns(null), //(words sharing) the same synset
	;
	
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
	
	private String symbol;
	Link( String symbol ) {
	  this.symbol = symbol;
	}
  public String getSymbol() {
    return symbol;
  }
}
