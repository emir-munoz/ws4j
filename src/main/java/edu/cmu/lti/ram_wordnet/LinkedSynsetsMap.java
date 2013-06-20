package edu.cmu.lti.ram_wordnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.BiMap;

import edu.cmu.lti.abstract_wordnet.Link;
import edu.cmu.lti.abstract_wordnet.Synset;


/**
 * Memory efficient implementation of Map<Integer,Integer[]>
 * 
 * @author Hideki Shima
 *
 */
public class LinkedSynsetsMap extends HashMap<Link,Integer[]> {
  private static final long serialVersionUID = 1L;
  public LinkedSynsetsMap(int size) {
    super( size * 4/3 + 1 );
  }
  
  public static LinkedSynsetsMap[] initSynset2SynsetMap( String path, 
          BiMap<String,Integer> dictS ) throws IOException {
    List<String> lines = IOUtils.readLines(LinkedSynsetsMap.class.getResourceAsStream(path));
    LinkedSynsetsMap[] retval = new LinkedSynsetsMap[dictS.size()];
    for ( String line : lines ) {
      String[] items = line.split(InMemoryWordNet.SEP1);
      if (items.length<=1) continue;
      LinkedSynsetsMap ls = new LinkedSynsetsMap(items.length-1);
      for ( int i=1; i<items.length; i++ ) {
        String[] items2 = items[i].split(InMemoryWordNet.SEP2);
        Integer[] synsets = new Integer[items2.length-1];
        for ( int j=1; j<items2.length; j++ ) {
          synsets[j-1] = dictS.get(items2[j]);
        }
        ls.put(Link.valueOf(items2[0]), synsets);
      }
      Integer idx = dictS.get(items[0]);//Synset id
      retval[idx] = ls;
    }
    return retval;
  }
  
  public static Map<Link,List<Synset>> getLinkedSynsets(InMemoryWordNet wn, 
          Synset synset, List<Link> links) {
    LinkedSynsetsMap ls = wn.synset2synsetMap[wn.dictS.get(synset.getSynsetId())];
    if (ls==null) return new LinkedHashMap<Link,List<Synset>>();
    int size = ls.size();
    Map<Link,List<Synset>> retval = new LinkedHashMap<Link,List<Synset>>(size*4/3+1);
    for (int i = 0; i < links.size(); i++) {
      Link link = links.get(i);
      Integer[] synsetIndices = ls.get(link);
      if (synsetIndices==null) continue;
      List<Synset> synsets = new ArrayList<Synset>(synsetIndices.length);
      for (int sidIndex : synsetIndices) {
        synsets.add(new Synset(wn.dictS.inverse().get(sidIndex)));
      }
      retval.put(link, synsets);
    }
    return retval;
  }
}
