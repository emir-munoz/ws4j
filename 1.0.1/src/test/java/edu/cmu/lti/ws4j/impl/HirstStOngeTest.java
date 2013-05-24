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
public class HirstStOngeTest extends RelatednessCalculatorTest {

	private static RelatednessCalculator rc;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		ILexicalDatabase db = new NictWordNet();
		rc = new HirstStOnge(db);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.impl.HirstStOnge#calcRelatedness(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnSynsets() {
		// both japanese and english!, n and v
		
		// English pair
		assertEquals(4,  rc.calcRelatednessOfSynset(n1Synsets.get(1), n2Synsets.get(0)).getScore(), 0.0001 );
	 	assertEquals(0,  rc.calcRelatednessOfSynset(n1Synsets.get(0), n2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(16, rc.calcRelatednessOfSynset(v1Synsets.get(0), v2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(4,  rc.calcRelatednessOfSynset(v1Synsets.get(1), v2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(6,  rc.calcRelatednessOfSynset(a1Synsets.get(0), a2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(16, rc.calcRelatednessOfSynset(r1Synsets.get(0), r2Synsets.get(0)).getScore(), 0.0001 );
	}

	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWords() {
		// at least two japanese and english!, n and v

		assertEquals(4,  rc.calcRelatednessOfWords(n1, n2), 0.0001D);
		assertEquals(16, rc.calcRelatednessOfWords(v1, v2), 0.0001D);
		assertEquals(6,  rc.calcRelatednessOfWords(a1, a2), 0.0001D);
		assertEquals(16, rc.calcRelatednessOfWords(r1, r2), 0.0001D);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWordsWithPOS() {
		assertEquals(5, rc.calcRelatednessOfWords(nv1+"#n", nv2+"#n"), 0.0001D);
		assertEquals(0, rc.calcRelatednessOfWords(nv1+"#n", nv2+"#v"), 0.0001D);
		assertEquals(0, rc.calcRelatednessOfWords(nv1+"#v", nv2+"#n"), 0.0001D);
		assertEquals(6, rc.calcRelatednessOfWords(nv1+"#v", nv2+"#v"), 0.0001D);
		assertEquals(0, rc.calcRelatednessOfWords(nv1+"#other", nv2+"#other"), 0.0001D);
	}

	@Test
	public void testOnSameSynsets() {
		assertEquals(HirstStOnge.max, rc.calcRelatednessOfSynset(n1Synsets.get(0), n1Synsets.get(0)).getScore(), 0.0001 );
	}
	
	@Test
	public void testOnUnknownSynsets() {
		assertEquals(HirstStOnge.min, rc.calcRelatednessOfSynset(null, n1Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(HirstStOnge.min, rc.calcRelatednessOfWords(null, n1), 0.0001 );
		assertEquals(HirstStOnge.min, rc.calcRelatednessOfWords("", n1), 0.0001 );
	}
}
