package edu.cmu.lti.ram_wordnet;


/**
 * Memory efficient implementation of Map<Integer,Integer[]>
 * 
 * @author Hideki Shima
 *
 */
public class LinkedSynsets {
  private int[] linkIndex;
  private int[][] synsetIndices;
  public LinkedSynsets(int size) {
    linkIndex = new int[size];
    synsetIndices = new int[size][];
  }
  public int[] getLinkIndex() {
    return linkIndex;
  }
  public int[][] getSynsetIndices() {
    return synsetIndices;
  }
  public int size() {
    return linkIndex.length;
  }
}
