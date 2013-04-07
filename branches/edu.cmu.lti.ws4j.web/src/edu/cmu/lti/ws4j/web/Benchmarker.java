package edu.cmu.lti.ws4j.web;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;


public class Benchmarker {

//  AbstractWordNet wn = new DataStoreWordNet();
//  public final String r1 = "huge";
//  public final String r2 = "massive";
//  public final List<Concept> r1Synsets = toSynsets(r1, "a");
//  public final List<Concept> r2Synsets = toSynsets(r2, "a");
  
  public void run() {
    DemoServlet ds = new DemoServlet();
    long t0 = System.currentTimeMillis();
    try {
      ds.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
    long t1 = System.currentTimeMillis();
    System.out.println("Warming up done in "+(t1-t0)+" msec.");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter pw = new PrintWriter(out);
    long t2 = System.currentTimeMillis();
    ds.runOnSentences(pw, 
            DemoServlet.sample1, 
            DemoServlet.sample2);
    pw.flush();
    long t3 = System.currentTimeMillis();
    System.out.println("Calculation done in "+(t3-t2)+" msec.");
    
    System.out.println(out.toString());
    
//    WS4JConfiguration.getInstance().setTrace(true);
//    AbstractWordNet wn = new DataStoreWordNet();
//    RelatednessCalculator rc = new HirstStOnge(wn);
//    for ( Concept c1 : r1Synsets ) {
//      for ( Concept c2 : r2Synsets ) {
//        Relatedness r = rc.calcRelatednessOfSynset(c1, c2);
//        System.out.println(r);
//      }
//    }
  }
  
  public static void main(String[] args) {
    new Benchmarker().run();
  }
  
//  private List<Concept> toSynsets( String word, String posText ) {
//    edu.cmu.lti.jawjaw.pobj.POS pos2 = edu.cmu.lti.jawjaw.pobj.POS.valueOf(posText);
//    List<Concept> synsets = wn.getConcepts(word, posText);
//    return synsets;
//  }
}
