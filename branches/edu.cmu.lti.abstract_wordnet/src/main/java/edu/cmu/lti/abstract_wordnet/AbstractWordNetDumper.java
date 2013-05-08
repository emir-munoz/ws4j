package edu.cmu.lti.abstract_wordnet;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * This abstract class specializes in dumping the WordNet database content
 * that are needed for Semantic Relatedness/Similarity calculation.
 * 
 * It assumes the data will be loaded by a key-value store such as
 * Google App Engine's Datastore.  
 * 
 * @author Hideki Shima
 */
public abstract class AbstractWordNetDumper {

  protected final static String SEP1 = "\t";//key and value are separated with this character
  protected final static String SEP2 = ",";//inside key or value, multiple items are concatenated with this
  
  /**
   * Dump all combinations of word and pos.
   * 
   * NG (freq order of sense lost):
   * CAT  n,00901476-n
   * cat  n,02121620-n,10153414-n,09900153-n,03608870-n,02985606-n,02983507-n,02127808-n  v,01411870-v,00076400-v
   * 
   * 
   * @return unique entries of word and pos concatenated with tab
   */
  public abstract Map<String,String> dumpWord2Synsets();

  /**
   * Dump glosses.
   * @return synset id to glosses (definition) 
   */
  public abstract Map<String,String> dumpSynset2Gloss();
  
  public abstract Map<String,String> dumpSynsetLinkSynsets();

//  public abstract Map<String,String> dumpSynset2Name();
  
  public abstract Map<String,String> dumpSynset2WordLemmas();

  /**
   * Format: synset id, lemma, link [tab] linked synset id, linked word lemma
   * 00001740-v,breathe,deri 04250850-n,breather,00831191-n,breathing
   * 
   * @return
   */
  public abstract Map<String,String> dumpWordLinkWords();
  
  public void dump( String filename, Map<String,String> map ) throws Exception {
    FileOutputStream fos = new FileOutputStream( filename );
    OutputStreamWriter osw = new OutputStreamWriter(fos);
    BufferedWriter bw = new BufferedWriter(osw);
    for ( String sid : map.keySet() ) {
      bw.write(sid+SEP1+map.get(sid)+"\n");
    }
    bw.close();
    osw.close();
    fos.close();
  }

  public void dumpWordNet() throws Exception {
    dump( "target/word-synsets.txt", dumpWord2Synsets() );
    dump( "target/word-link-words.txt", dumpWordLinkWords() );
    dump( "target/synset-link-synsets.txt", dumpSynsetLinkSynsets() );
    dump( "target/synset-gloss.txt", dumpSynset2Gloss() );
//    dump( "target/synset-name.txt", dumpSynset2Name() );
    dump( "target/synset-words.txt", dumpSynset2WordLemmas() );
  }
  
}
