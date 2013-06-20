package edu.cmu.lti.imw;


import java.util.Arrays;

import edu.cmu.lti.abstract_wordnet.POS;

/**
 * Memory efficient implementation of Map<String,Integer[]>
 * 
 * @author Hideki Shima
 */
public class POSAndSynsets {
  //Example:
  //CAT n,00901476-n
  //cat n,02121620-n,10153414-n,09900153-n,03608870-n,02985606-n,02983507-n,02127808-n  v,01411870-v,00076400-v
  
//  private int[] word;// original case sensitive word with no lower-case processing
  private POS[] pos;
  private int[][] synsets;
  public POSAndSynsets( int size ) {
//    word = new int[size];
    pos = new POS[size];
    synsets = new int[size][];
  }
//  public int[] getWord() {
//    return word;
//  }
  public POS[] getPos() {
    return pos;
  }
  public int[][] getSynsets() {
    return synsets;
  }
  public int size() {
    return pos.length;
  }
  public void add( POSAndSynsets pas ) {
    int oldSize = size();
    int deltaSize = pas.size();
    int newSize = oldSize + deltaSize;
//    word = Arrays.copyOf( word, newSize );
    pos = Arrays.copyOf( pos, newSize );
    synsets = Arrays.copyOf( synsets, newSize );
    for ( int i=0; i<deltaSize; i++ ) {
//      word[oldSize+i]    = pas.word[i];
      pos[oldSize+i]     = pas.pos[i];
      synsets[oldSize+i] = pas.synsets[i];
    }
  }
  @Override
  public String toString() {
    return "POSAndSynsets [" +
//        "word=" + Arrays.toString(word) + ", " +
            "pos=" + Arrays.toString(pos)
            + ", size()=" + size() + "]";
  }
  
}
