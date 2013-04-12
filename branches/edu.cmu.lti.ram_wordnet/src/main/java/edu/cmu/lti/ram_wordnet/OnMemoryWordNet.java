package edu.cmu.lti.ram_wordnet;


import java.io.IOException;
import java.util.Arrays;
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
  private final static String F_S2N = "/wordnet/synset-name.txt";
  private final static String F_S2G = "/wordnet/synset-gloss.txt";
  private final static String F_S2W = "/wordnet/synset-words.txt";
  
  //Dict are for saving memory space
  public BiMap<String,Integer> dictS;//synset id to integer index
  public BiMap<String,Integer> dictW;//word to integer index
  public BiMap<String,Integer> dictL;//link names to integer index
  public POSAndSynsets[] word2synsets;
  public int[][] synset2words;
  public LinkedSynsets[] synset2synset;
  public int[] synset2name;
  public String[] synset2gloss;
  
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
        m.currentTotal();
      }
      dictL = createDict(Arrays.asList(Link.values()));
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... dictL");
      synset2synset = initSynset2Synset( F_S2S, dictS, dictL );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2synset");
      {
        Map<Integer,String[]> synset2wordStrings = initSynset2Words( F_S2W, dictS );
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2wordStrings [temp]");
        dictW = createDict(synset2wordStrings);
        if (BENCHMARK) System.out.println(m.measure()+ " bytes ... dictW");
        synset2words = initSynset2Words(synset2wordStrings, dictW);
        if (BENCHMARK) System.out.println(m.measure()+" bytes ... synset2words");
        synset2wordStrings.keySet().size();
        m.currentTotal();
      }
      word2synsets = initWord2Synsets( F_W2S, dictS, dictW );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... word2synsets");
      synset2name   = initSynset2Name( F_S2N, dictS, dictW );
      if (BENCHMARK) System.out.println(m.measure()+ " bytes ... synset2name");
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println(m.currentTotal()+" bytes used now. Loading done in "+(t1-t0)+" msec.");
  }
  
  private BiMap<String,Integer> createDict( Map<Integer,String[]> map ) {
    Set<String> uniq = new HashSet<String>();
    for ( String[] words : map.values() ) for ( String w : words ) uniq.add(w);
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
  
//  private Map<Integer,List<String>> initSW() {
//    Map<Integer,List<String>> synset2words = new HashMap<Integer,List<String>>();
//    System.out.println(dict1.size());
//    int c=0;
//    for (Entry<String,Integer> e1 : dict1.entrySet()) {
//      if(c++%1001==1000)System.out.println(c+"\n");
//      List<String> wordLemmas = new ArrayList<String>();
//      Integer argIndex = e1.getValue();
//      for ( Entry<String,List<Integer>> e : word2synsets.entrySet() ) {
//        if (e.getValue().contains(argIndex)) {
//          String[] items = e.getKey().split(SEP1);
//          wordLemmas.add(items[0]);
//        }
//      }
//      synset2words.put(e1.getValue(), wordLemmas);
//    }
//    return synset2words;
//  }
  
  //  typing_paper  n eng<>15082382-n
//  private int[][] initWord2Synsets( String path, BiMap<String,Integer> dictS ) throws IOException {
//    String text = IoUtil.readStream(getClass().getResourceAsStream(path));
//    String[] lines = text.split("\n");
//    int[][] retval = new int[ lines.length ][];
//    for ( String line : lines ) {
//      String[] items = line.split(SEP1);
//      if (items.length!=2) continue; 
//      String[] values = items[1].split(",");
//      int[] indices = new Integer[values.length];
////      for ( String v : values ) indices.add( dict1.get(v) );
//      for (int i = 0; i < values.length; i++) {
//        indices[i] = dictS.get(values[i]);
//      }
//      retval[items[0]] = indices;
//    }
//    return retval;
//  }
  

  private POSAndSynsets[] initWord2Synsets( String path, 
          BiMap<String,Integer> dictS, BiMap<String,Integer> dictW ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    //capacity is a larger estimate
    Map<Integer,Map<String,Integer[]>> result = new HashMap<Integer,Map<String,Integer[]>>( lines.size() * 4/3 + 1 );
    for ( String line : lines ) {
      String[] kv = line.split(SEP1);
      if (kv.length!=2) continue; 
      String[] keys = kv[0].split(SEP2);
      String word = cannonicalize(keys[0]);
      String pos = keys[1];
      Integer wordIndex = dictW.get(word);
      if (wordIndex==null) System.err.println("word not in index!!");
      Map<String,Integer[]> pos2synsets = result.get(wordIndex);
      if (pos2synsets == null) pos2synsets = new HashMap<String,Integer[]>();
      String[] values = kv[1].split(SEP2);
      Integer[] indices = new Integer[values.length];
      for (int i = 0; i < values.length; i++) {
        indices[i] = dictS.get(values[i]);
      }
      pos2synsets.put(pos, indices);
      result.put(wordIndex, pos2synsets);
    }
    return compact(result);
  }

  private POSAndSynsets[] compact( Map<Integer,Map<String,Integer[]>> word2synset ) {
    POSAndSynsets[] retval = new POSAndSynsets[dictW.size()];
    for ( Entry<Integer,Map<String,Integer[]>> e : word2synset.entrySet() ) {
      int wordIndex = e.getKey();
      Map<String,Integer[]> posSynsetMap = e.getValue();
      POSAndSynsets posAndSynsets = new POSAndSynsets( posSynsetMap.size() );
      int i=0;
      for ( Entry<String,Integer[]> posSynsets : posSynsetMap.entrySet() ) {
        posAndSynsets.getPos()[i] = POS.valueOf(posSynsets.getKey());
        int[] ss = new int[posSynsets.getValue().length];
        for (int j=0; j<posSynsets.getValue().length; j++) {
          ss[j] = posSynsets.getValue()[j];
        }
        posAndSynsets.getSynsets()[i] = ss;
        i++;
      }
      retval[wordIndex] = posAndSynsets;
    }
    return retval;
  }
  
  //11443721-n  hypo<>11467018-n,11519450-n,11521145-n
  private LinkedSynsets[] initSynset2Synset( String path, 
          BiMap<String,Integer> dictS, BiMap<String,Integer> dictL ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    Map<Integer,Map<Integer,Integer[]>> result = new HashMap<Integer,Map<Integer,Integer[]>>( lines.size() * 4/3 + 1);
    for ( String line : lines ) {
      String[] items = line.split(SEP1);
      if (items.length!=2) continue; 
      String[] items1 = items[0].split(SEP2);
      Integer key1 = dictS.get(items1[0]);//Synset id
      Integer key2 = dictL.get(items1[1]);//Link type key
      if (key2==null) System.err.println("Invalid link: "+key2);
      String[] targetSynsets = items[1].split(",");
      Integer[] values = new Integer[targetSynsets.length];
      for ( int i=0; i<targetSynsets.length; i++ ) {
        values[i] = dictS.get(targetSynsets[i]);
      }
      Map<Integer,Integer[]> v = result.get(key1);
      if (v==null) v = new HashMap<Integer,Integer[]>(4 * 4/3 +1);
      v.put(key2, values);
      result.put(key1, v);
    }
    return compact(result, dictS);
  }

  private LinkedSynsets[] compact(Map<Integer,Map<Integer,Integer[]>> synset2synset,
          BiMap<String,Integer> dictS) {
    LinkedSynsets[] retval = new LinkedSynsets[ dictS.size() ];
    for ( Entry<Integer,Map<Integer,Integer[]>> e : synset2synset.entrySet() ) {
      Map<Integer,Integer[]> map = e.getValue();
      LinkedSynsets ls = new LinkedSynsets(map.size());
      int i = 0;
      for (Entry<Integer,Integer[]> e2 : map.entrySet()) {
        ls.getLinkIndex()[i] = e2.getKey();
        ls.getSynsetIndices()[i] = new int[e2.getValue().length];
        for (int j = 0; j < e2.getValue().length; j++) {
          ls.getSynsetIndices()[i][j] = e2.getValue()[j];
        }
        i++;
      }
      retval[e.getKey()] = ls;
    }
    return retval;
  }
  
  private int[] initSynset2Name( String path, 
          BiMap<String,Integer> dictS, BiMap<String,Integer> dictW ) throws IOException {
    List<String> lines = IOUtils.readLines(getClass().getResourceAsStream(path));
    int[] retval = new int[ lines.size() ];
    for ( String line : lines ) {
      String[] items = line.split(SEP1);
      if (items.length!=2) continue; 
      Integer sidIndex = dictS.get(items[0]);
      Integer wordIndex = dictW.get(cannonicalize(items[1]));
      if (wordIndex==null) System.err.println("OOD: "+items[1]);
      retval[sidIndex] = wordIndex;
    }
    return retval;
  }
  
  private String[] initSynset2Gloss( Map<String,String> synset2glossTemp, 
          BiMap<String,Integer> dictS ) throws IOException {
    String[] result = new String[synset2glossTemp.size()];
    for ( Entry<String,String> e : synset2glossTemp.entrySet() ) {
      Integer index = dictS.get(e.getKey());
      result[index] = e.getValue();
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
      for ( int i=0; i<words.length; i++ ) {
        words[i] = cannonicalize(words[i]);
      }
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
   * Rule 1: case insensitive
   * Rule 2: hypes and underscores are recognized as same 
   * 
   * @param s
   * @return
   */
  public static String cannonicalize( String s ) {
    return s != null ? s.replaceAll(" |-", "_").toLowerCase() : null;
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
