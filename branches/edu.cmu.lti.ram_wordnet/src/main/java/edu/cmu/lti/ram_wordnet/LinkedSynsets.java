package edu.cmu.lti.ram_wordnet;

import edu.cmu.lti.abstract_wordnet.Link;


/**
 * Memory efficient implementation of Map<Integer,Integer[]>
 * 
 * @author Hideki Shima
 *
 */
public class LinkedSynsets {
  private Link[] links;
  private int[][] synsetIndices;
  public LinkedSynsets(int size) {
    links = new Link[size];
    synsetIndices = new int[size][];
  }
  public Link[] getLinks() {
    return links;
  }
  public int[][] getSynsetIndices() {
    return synsetIndices;
  }
  public int size() {
    return links.length;
  }
}
