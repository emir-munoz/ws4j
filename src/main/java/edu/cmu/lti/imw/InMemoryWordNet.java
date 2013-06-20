package edu.cmu.lti.imw;


import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
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
public class InMemoryWordNet {

  public final static String SEP1 = "\t";//key and value are separated with this character
  public final static String SEP2 = ",";//inside key or value, multiple items are concatenated with this
  
  private final static boolean BENCHMARK = false;

  private final static String F_S2S = "/wordnet/synset-link-synsets.txt";
  private final static String F_S2G = "/wordnet/synset-gloss.txt";
  private final static String F_S2W = "/wordnet/synset-words.txt";
  private final static String F_W2S = "/wordnet/word-synsets.txt";
  private final static String F_W2W = "/wordnet/word-link-words.txt";
  
  public final static boolean useArrayOrMap = true;
  
  //Dict are for saving memory space
  public BiMap<String,Integer> dictS;//synset id to integer index
  public BiMap<String,Integer> dictW;//(non-lc'ed) word to integer index
  //Not using collections to save memory.
  public int[][] synset2words;
  public LinkedSynsets[] synset2synset;
  public LinkedSynsetsMap[] synset2synsetMap;
  public char[][] synset2gloss;
  public POSAndSynsets[] word2synsets;
  public LinkedWords[] word2words;
  
  //00004475-n  being,deri,02614181-v,be  organism,deri,02986509-a,organismic,01679459-a,organic,01093142-a,organic
  
  //force word entries to be lower case?
  //must be true 
  public final static boolean LC_KEY = true;
  
  public InMemoryWordNet() {
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
      if (useArrayOrMap) {
        synset2synset = LinkedSynsets.initSynset2Synset( F_S2S, dictS );
      } else {
        synset2synsetMap = LinkedSynsetsMap.initSynset2SynsetMap( F_S2S, dictS );
      }
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2synset");
      int[] wordCount = new int[2];//size of lower-case entry and non-lc entry.
      {
        Map<Integer,String[]> synset2wordStrings = initSynset2Words( F_S2W, dictS );
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2wordStrings [temp]");
        dictW = createDict(synset2wordStrings, wordCount);
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... dictW");
        synset2words = initSynset2Words(synset2wordStrings, dictW);
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2words");
        synset2wordStrings.keySet().size();
        synset2wordStrings = null;
        m.currentTotal();
      }
      word2synsets = initWord2Synsets( F_W2S, dictS, dictW, wordCount[0] );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... word2synsets");
      word2words = initWord2Words( F_W2W, dictS, dictW );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... word2words");      
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    if (BENCHMARK) System.out.println(m.currentTotal()+" bytes used now. Loading done in "+(t1-t0)+" msec.");
  }
  
  private BiMap<String,Integer> createDict( Map<Integer,String[]> map, int[] count ) {
    Set<String> lcUnique = new LinkedHashSet<String>(map.size());
    Set<String> nonlcUnique = new LinkedHashSet<String>(map.size());
    for ( String[] words : map.values() ) {
      for ( String w : words ) {
        // Input is already in LC, but dict must have both entries
        String lc = w.toLowerCase();
        lcUnique.add(lc);
        if (!lc.equals(w)) {//if w is non LC
          nonlcUnique.add( w );
        }
      }
    }
    count[0] = lcUnique.size();
    count[1] = nonlcUnique.size();
    BiMap<String,Integer> retval = HashBiMap.create(lcUnique.size()+nonlcUnique.size());
    int i=0;
    for ( String w : lcUnique ) {//LC first!
      retval.put(w, i++);
    }
    for ( String w : nonlcUnique ) {
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
  
  private POSAndSynsets[] initWord2Synsets( String path, BiMap<String,Integer> dictS, 
          BiMap<String,Integer> dictW, int lcCount ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    //capacity is a larger estimate
    POSAndSynsets[] retval = new POSAndSynsets[lcCount];
    for ( String line : lines ) {
      String[] kvs = line.split(SEP1);
      if (kvs.length<=1) continue;
      String origWord = kvs[0];
      String lcWord = origWord.toLowerCase();
//      String lcWord = cannonicalize(origWord);
      Integer wordIndex = dictW.get(lcWord);
      if (wordIndex==null) {
        System.err.println("word not in index: "+kvs[0]+" -> "+lcWord);
        System.exit(-1);
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
  
  private LinkedWords[] initWord2Words( String path, BiMap<String,Integer> dictS, 
          BiMap<String,Integer> dictW ) throws IOException {
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
      if (sid==null) {
        System.err.println("word not found in index!!: There is a problem with this input:" + line);
        System.exit(-1);
      }
      LinkedWords linkedWords = new LinkedWords(word, link, synsets, words);
      retval[sid] = linkedWords;
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
    if (s!=null) {
      s = s.replaceAll(" ", "_");
      s = (LC_KEY ? s.toLowerCase() : s);
    }
    return s;
  }

}
