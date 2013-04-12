package edu.cmu.lti.ram_wordnet;


import edu.cmu.lti.abstract_wordnet.POS;

/**
 * Memory efficient implementation of Map<String,Integer[]>
 * 
 * @author Hideki Shima
 */
public class POSAndSynsets {
  private POS[] pos;
  private int[][] synsets;
  public POSAndSynsets( int size ) {
    pos = new POS[size];
    synsets = new int[size][];
  }
  public POS[] getPos() {
    return pos;
  }
  public int[][] getSynsets() {
    return synsets;
  }
  public int size() {
    return pos.length;
  }
}
