package edu.cmu.lti.ws4j.impl;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.RelatednessCalculatorTest;

/**
 * @author Hideki Shima
 *
 */
public class WuPalmerTest extends RelatednessCalculatorTest {

	private static RelatednessCalculator rc;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		ILexicalDatabase db = new NictWordNet();
		rc = new WuPalmer(db);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.impl.WuPalmer#calcRelatedness(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnSynsets() {
		assertEquals(0.9565, rc.calcRelatednessOfSynset(n1Synsets.get(1), n2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(0.6957, rc.calcRelatednessOfSynset(n1Synsets.get(0), n2Synsets.get(0)).getScore(), 0.0001 );
		
		assertEquals(0.8571, rc.calcRelatednessOfSynset(v1Synsets.get(0), v2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(0.5714, rc.calcRelatednessOfSynset(v1Synsets.get(1), v2Synsets.get(0)).getScore(), 0.0001 );
	}

	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWords() {
		assertEquals(0.9565, rc.calcRelatednessOfWords(n1, n2), 0.0001D);
		assertEquals(0.8571, rc.calcRelatednessOfWords(v1, v2), 0.0001D);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWordsWithPOS() {
		assertEquals(0.8750,rc.calcRelatednessOfWords(nv1+"#n", nv2+"#n"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#n", nv2+"#v"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#v", nv2+"#n"), 0.0001D);
		assertEquals(0.8333,rc.calcRelatednessOfWords(nv1+"#v", nv2+"#v"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#other", nv2+"#other"), 0.0001D);
	}

	@Test
	public void testOnSameSynsets() {
		assertEquals(WuPalmer.max, rc.calcRelatednessOfSynset(n1Synsets.get(0), n1Synsets.get(0)).getScore(), 0.0001 );
	}
	
	@Test
	public void testOnUnknownSynsets() {
		assertEquals(WuPalmer.min, rc.calcRelatednessOfSynset(null, n1Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(WuPalmer.min, rc.calcRelatednessOfWords(null, n1), 0.0001 );
		assertEquals(WuPalmer.min, rc.calcRelatednessOfWords("", n1), 0.0001 );
	}
}
