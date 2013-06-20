package edu.cmu.lti.ram_wordnet;

import java.io.IOException;
import java.util.ArrayList;
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
  
  //11443721-n  hypo<>11467018-n,11519450-n,11521145-n
  public static LinkedSynsets[] initSynset2Synset( String path, 
          BiMap<String,Integer> dictS ) throws IOException {
    List<String> lines = IOUtils.readLines(LinkedSynsets.class.getResourceAsStream(path));
    LinkedSynsets[] retval = new LinkedSynsets[dictS.size()];
    for ( String line : lines ) {
      String[] items = line.split(InMemoryWordNet.SEP1);
      if (items.length<=1) continue;
      LinkedSynsets ls = new LinkedSynsets(items.length-1);
      for ( int i=1; i<items.length; i++ ) {
        String[] items2 = items[i].split(InMemoryWordNet.SEP2);
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

  public static Map<Link, List<Synset>> getLinkedSynsets(InMemoryWordNet wn, 
          Synset synset, List<Link> links) {
    List<Link> remainingLinks = new ArrayList<Link>(links);
    LinkedSynsets ls = wn.synset2synset[wn.dictS.get(synset.getSynsetId())];
    if (ls == null)
      return new LinkedHashMap<Link, List<Synset>>();
    int size = ls.size();
    Map<Link, List<Synset>> retval = new LinkedHashMap<Link, List<Synset>>(size * 4 / 3 + 1);
    for (int i = 0; i < size; i++) {
      if (remainingLinks.size() == 0)
        break;
      Link link = ls.getLinks()[i];
      boolean suc = remainingLinks.remove(link);
      if (!suc)
        continue;
      List<Synset> synsets = new ArrayList<Synset>(ls.getSynsetIndices()[i].length);
      for (int sidIndex : ls.getSynsetIndices()[i]) {
        synsets.add(new Synset(wn.dictS.inverse().get(sidIndex)));
      }
      retval.put(link, synsets);
    }
    return retval;
  }
  
  @Deprecated
  public static Map<Link, List<Synset>> getLinkedSynsets(InMemoryWordNet wn, 
          Synset synset) {
    LinkedSynsets ls = wn.synset2synset[wn.dictS.get(synset.getSynsetId())];
    if (ls == null)
      return new LinkedHashMap<Link, List<Synset>>();
    Map<Link, List<Synset>> retval = new LinkedHashMap<Link, List<Synset>>(ls.size() * 4 / 3 + 1);
    for (int i = 0; i < ls.size(); i++) {
      Link link = ls.getLinks()[i];
      List<Synset> synsets = new ArrayList<Synset>(ls.getSynsetIndices()[i].length);
      for (int sidIndex : ls.getSynsetIndices()[i]) {
        synsets.add(new Synset(wn.dictS.inverse().get(sidIndex)));
      }
      retval.put(link, synsets);
    }
    return retval;
  }
}
