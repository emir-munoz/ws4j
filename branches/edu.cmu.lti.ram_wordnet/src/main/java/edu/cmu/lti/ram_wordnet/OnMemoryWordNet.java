package edu.cmu.lti.ram_wordnet;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.cmu.lti.abstract_wordnet.Link;
import edu.cmu.lti.abstract_wordnet.POS;

/**
 * 
 * @author Hideki Shima
 *
 */
public class OnMemoryWordNet {

  public final String SEP1 = "\t";//key and value are separated with this character
  public final String SEP2 = ",";//inside key or value, multiple items are concatenated with this
  
  private final static boolean BENCHMARK = true;

  private final static String F_W2S = "/wordnet/word-synsets.txt";
  private final static String F_S2S = "/wordnet/synset-link-synsets.txt";
  private final static String F_S2G = "/wordnet/synset-gloss.txt";
  private final static String F_S2W = "/wordnet/synset-words.txt";
  private final static String F_W2W = "/wordnet/word-link-words.txt";
  
  //Dict are for saving memory space
  public BiMap<String,Integer> dictS;//synset id to integer index
  public BiMap<String,Integer> dictW;//(non-lc'ed) word to integer index
  public POSAndSynsets[] word2synsets;
  public int[][] synset2words;
  public LinkedSynsets[] synset2synset;
  public int[] synset2name;
  public char[][] synset2gloss;
  public LinkedWords[] word2words;
  
  //00004475-n  being,deri,02614181-v,be  organism,deri,02986509-a,organismic,01679459-a,organic,01093142-a,organic
  
  //force word entries to be lower case?
  //must be true 
  public final static boolean LC_KEY = true;
  
  public OnMemoryWordNet() {
    MemoryMonitor m = new MemoryMonitor();
    long t0 = System.currentTimeMillis();
    try {
      {
        Map<String,String> synset2glossTemp = loadKV( F_S2G );
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2gloss [temp]");
        dictS = createDict(synset2glossTemp.keySet());
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... dictS");
        synset2gloss  = initSynset2Gloss( synset2glossTemp, dictS );
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2gloss");
        synset2glossTemp = null;
        m.currentTotal();
      }
      synset2synset = initSynset2Synset( F_S2S, dictS );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2synset");
      {
        Map<Integer,String[]> synset2wordStrings = initSynset2Words( F_S2W, dictS );
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2wordStrings [temp]");
        dictW = createDict(synset2wordStrings);
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... dictW");
        synset2words = initSynset2Words(synset2wordStrings, dictW);
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2words");
        synset2wordStrings.keySet().size();
        synset2wordStrings = null;
        m.currentTotal();
      }
      word2synsets = initWord2Synsets( F_W2S, dictS, dictW );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... word2synsets");
      word2words = initWord2Words( F_W2W, dictS, dictW );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... word2words");      
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println(m.currentTotal()+" bytes used now. Loading done in "+(t1-t0)+" msec.");
  }
  
  private BiMap<String,Integer> createDict( Map<Integer,String[]> map ) {
    Set<String> uniq = new HashSet<String>();
    for ( String[] words : map.values() ) {
      for ( String w : words ) {
        // Input is already in LC, but dict must have both entries
//        if (LC_KEY) {
//          uniq.add(w.toLowerCase());
//        } else {
          uniq.add(w);
          String lc = w.toLowerCase();
          if (!w.equals(lc)) {
            uniq.add(lc);
          }
//        }
      }
    }
    int i=0;
    BiMap<String,Integer> retval = HashBiMap.create(uniq.size());
    for ( String w : uniq ) {
      retval.put(w, i++);
    }
    return retval;
  }
  
  public static <T> BiMap<String,Integer> createDict( Collection<T> entries ) {
    BiMap<String,Integer> dict = HashBiMap.create(entries.size());
    int i=0;
    for ( T t : entries ) {
      dict.put( t.toString(), i++ );
    }
    return dict;
  }
  
  private POSAndSynsets[] initWord2Synsets( String path, 
          BiMap<String,Integer> dictS, BiMap<String,Integer> dictW ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    //capacity is a larger estimate
    POSAndSynsets[] retval = new POSAndSynsets[dictW.size()];
    for ( String line : lines ) {
      String[] kvs = line.split(SEP1);
      if (kvs.length<=1) continue;
      String origWord = kvs[0];
      String lcWord = origWord.toLowerCase();
//      String lcWord = cannonicalize(origWord);
      Integer wordIndex = dictW.get(lcWord);
      if (wordIndex==null) {
        if (LC_KEY) {
          System.err.println("word not in index: "+kvs[0]+" -> "+lcWord);
        } else {
          wordIndex = dictW.get(lcWord.toLowerCase());
          if (wordIndex==null) {
            System.err.println("word not in index: "+kvs[0]+" -> "+lcWord);
          }
        }
      }
      POSAndSynsets posAndSynsets = new POSAndSynsets(kvs.length-1);
      for ( int i=1; i<kvs.length; i++ ) {
        String[] items = kvs[i].split(SEP2);
        int[] synsets = new int[items.length-1];
        for ( int j=1; j<items.length; j++ ) {
          synsets[j-1] = dictS.get(items[j]);
        }
//        posAndSynsets.getWord()[i-1] = dictW.get(origWord);
        posAndSynsets.getPos()[i-1] = POS.valueOf(items[0]);
        posAndSynsets.getSynsets()[i-1] = synsets;
      }
//      POSAndSynsets bu = retval[wordIndex];
//      if (bu!=null) {
//        bu.add(posAndSynsets);
//        retval[wordIndex] = bu;
//      } else {
        retval[wordIndex] = posAndSynsets;
//      }
    }
    return retval;
  }
  
  private LinkedWords[] initWord2Words( String path, 
          BiMap<String,Integer> dictS, BiMap<String,Integer> dictW ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    //capacity is a larger estimate
    LinkedWords[] retval = new LinkedWords[dictS.size()];
    for ( String line : lines ) {
      String[] kvs = line.split(SEP1);
      if (kvs.length<=1) continue;
      int[] word = new int[kvs.length-1];
      Link[] link = new Link[kvs.length-1];
      int[][] synsets = new int[kvs.length-1][];
      int[][] words = new int[kvs.length-1][];
      for ( int i=1; i<kvs.length; i++ ) {
        String[] items = kvs[i].split(SEP2);
        word[i-1] = dictW.get(cannonicalize(items[0]));
        link[i-1] = Link.valueOf(items[1]);
        int n = (items.length-2)/2;
        synsets[i-1] = new int[n];
        words[i-1]   = new int[n];
        for ( int j=0; j<n; j++ ) {
          synsets[i-1][j] = dictS.get(items[2*j+2]);
          words[i-1][j]   = dictW.get(cannonicalize(items[2*j+3]));
        }
//        LinkedWords linkedWords = new LinkedWords(wIndex, link, synsets, words);
      }
      String sidStr = kvs[0];
      Integer sid = dictS.get(sidStr);
      if (sid==null) System.err.println("word not in index!!");
      LinkedWords linkedWords = new LinkedWords(word, link, synsets, words);
      retval[sid] = linkedWords;
    }
    return retval;
  }

  //11443721-n  hypo<>11467018-n,11519450-n,11521145-n
  private LinkedSynsets[] initSynset2Synset( String path, 
          BiMap<String,Integer> dictS ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    LinkedSynsets[] retval = new LinkedSynsets[dictS.size()];
    for ( String line : lines ) {
      String[] items = line.split(SEP1);
      if (items.length<=1) continue;
      LinkedSynsets ls = new LinkedSynsets(items.length-1);
      for ( int i=1; i<items.length; i++ ) {
        String[] items2 = items[i].split(SEP2);
        int[] synsets = new int[items2.length-1];
        for ( int j=1; j<items2.length; j++ ) {
          synsets[j-1] = dictS.get(items2[j]);
        }
        ls.getSynsetIndices()[i-1] = synsets;
        ls.getLinks()[i-1] = Link.valueOf(items2[0]);
      }
      Integer idx = dictS.get(items[0]);//Synset id
      retval[idx] = ls;
    }
    return retval;
  }
  
  private char[][] initSynset2Gloss( Map<String,String> synset2glossTemp, 
          BiMap<String,Integer> dictS ) throws IOException {
    char[][] result = new char[synset2glossTemp.size()][];
    for ( Entry<String,String> e : synset2glossTemp.entrySet() ) {
      Integer index = dictS.get(e.getKey());
      result[index] = e.getValue().toCharArray();
    }
    return result;
  }
  
  private Map<Integer,String[]> initSynset2Words( String path, BiMap<String,Integer> dictS ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    Map<Integer,String[]> result = new HashMap<Integer,String[]>( lines.size() * 4/3 + 1 );
    for ( String line : lines ) {
      String[] items = line.split(SEP1);
      if (items.length!=2) continue; 
      Integer index = dictS.get(items[0]);
      String[] words = items[1].split(",");
//commented out 5/7
//      for ( int i=0; i<words.length; i++ ) {
//        words[i] = cannonicalize(words[i]);
//      }
      result.put(index, words);
    }
    return result;
  }

  private int[][] initSynset2Words( Map<Integer,String[]> synset2words, 
          BiMap<String,Integer> dictW ) throws IOException {
    int[][] result = new int[ synset2words.size() ][];
    for ( Entry<Integer,String[]> e : synset2words.entrySet() ) {
      int[] wordIndices = new int[e.getValue().length];
      for (int i=0; i<e.getValue().length; i++) {
        wordIndices[i] = dictW.get(e.getValue()[i]);
      }
      result[e.getKey()] = wordIndices;
    }
    return result;
  }
  
  public Map<String,String> loadKV( String path ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    Map<String,String> pairs = new HashMap<String,String>(lines.size() * 4/3 + 1);
    for ( String line : lines ) {
      String[] items = line.split(SEP1);
      if (items.length==2) pairs.put(items[0],items[1]);
    }
    return pairs;
  }
  
  /**
   * Below are the cannonicalization rules
   * Rule 1: hypes and underscores are recognized as same
   * Rule 2: case insensitive matching by making the word lower-case
   * 
   * @param word
   * @return cannonicalized word
   */
  public static String cannonicalize( String s ) {
    s = s != null ? s.replaceAll(" ", "_") : null;
    return LC_KEY ? s.toLowerCase() : s;
  }

  public static void main(String[] args) {
    OnMemoryWordNet wn = new OnMemoryWordNet();
    int idx=0;
    System.out.println(wn.synset2words[idx]);
    System.out.println(wn.synset2synset[idx]);
    System.out.println(wn.synset2name[idx]);
    System.out.println(wn.synset2gloss[idx]);
  }
  
}
