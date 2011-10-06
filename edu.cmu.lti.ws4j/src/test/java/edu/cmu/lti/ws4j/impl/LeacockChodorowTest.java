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
public class LeacockChodorowTest extends RelatednessCalculatorTest {

	private static RelatednessCalculator rc;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		ILexicalDatabase db = new NictWordNet();
		rc = new LeacockChodorow(db);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.impl.LeacockChodorow#calcRelatedness(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnSynsets() {
		// both japanese and english!, n and v
		
		// English pair
		assertEquals(2.9957, rc.calcRelatednessOfSynset(n1Synsets.get(1), n2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(1.6094, rc.calcRelatednessOfSynset(n1Synsets.get(0), n2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(2.6391, rc.calcRelatednessOfSynset(v1Synsets.get(0), v2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(1.9459, rc.calcRelatednessOfSynset(v1Synsets.get(1), v2Synsets.get(0)).getScore(), 0.0001 );
	}

	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWords() {
		assertEquals(2.9957, rc.calcRelatednessOfWords(n1, n2), 0.0001D);
		assertEquals(2.6391, rc.calcRelatednessOfWords(v1, v2), 0.0001D);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWordsWithPOS() {
		assertEquals(2.5902,rc.calcRelatednessOfWords(nv1+"#n", nv2+"#n"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#n", nv2+"#v"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#v", nv2+"#n"), 0.0001D);
		assertEquals(2.2335,rc.calcRelatednessOfWords(nv1+"#v", nv2+"#v"), 0.0001D);
		assertEquals(0.0000,rc.calcRelatednessOfWords(nv1+"#other", nv2+"#other"), 0.0001D);
	}

	@Test
	public void testOnUnknownSynsets() {
		assertEquals(LeacockChodorow.min, rc.calcRelatednessOfSynset(null, n1Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(LeacockChodorow.min, rc.calcRelatednessOfWords(null, n1), 0.0001 );
		assertEquals(LeacockChodorow.min, rc.calcRelatednessOfWords("", n1), 0.0001 );
	}
}
