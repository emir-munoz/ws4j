package edu.cmu.lti.ws4j.web;

import java.io.InputStream;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public enum OpenNLPSingleton {
  INSTANCE;
  
  private TokenizerME tokenizer;
  private POSTaggerME posTagger;
//  private ChunkerME chunker;
//  private Dictionary dict;
  
  private OpenNLPSingleton() {
    try {
      InputStream inTokenizer = OpenNLPSingleton.class.getResourceAsStream("/opennlp/en-token.bin");
      InputStream inPOS = OpenNLPSingleton.class.getResourceAsStream("/opennlp/en-pos-maxent.bin");
//      InputStream inChunker = OpenNLPSingleton.class.getResourceAsStream("/opennlp/en-chunker.bin");
      TokenizerModel modelTokenizer = new TokenizerModel(inTokenizer);
      POSModel modelPOS = new POSModel(inPOS);
//      ChunkerModel chunkerPOS = new ChunkerModel(inChunker);
      tokenizer = new TokenizerME(modelTokenizer);
      posTagger = new POSTaggerME(modelPOS);
//      chunker = new ChunkerME(chunkerPOS);
//      dict = Dictionary.getInstance();.lookupIndexWord(pos, toks.get(i))
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public String[] tokenize( String sentence ) {
    return tokenizer.tokenize( sentence );
  }
  public String[] postag( String[] tokens ) {
    return posTagger.tag( tokens );
  }
//  public String[] chunk( String[] tokens, String[] pos ) {
//    return chunker.chunk( tokens, pos );
//  }
}
