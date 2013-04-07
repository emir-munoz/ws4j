package edu.cmu.lti.ws4j.ramwn;

import edu.cmu.lti.abstract_wordnet.AbstractWordNet;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class DevelopmentTest {
  
  public void run() {
    AbstractWordNet wn = new OnMemoryWordNetAPI();
    String w1 = "dog";
    String w2 = "wolf";
//    String pos = "n";

    WS4JConfiguration.getInstance().setTrace(true);
//    RelatednessCalculator rc = new WuPalmer(wn);
    RelatednessCalculator rc = new JiangConrath(wn);
    Relatedness r = rc.calcRelatednessOfWords(w1, w2, true);
    System.out.println(r);
  }
  
  public static void main(String[] args) {
    new DevelopmentTest().run();
  }
  
}
