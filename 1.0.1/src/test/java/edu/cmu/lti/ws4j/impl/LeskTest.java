package edu.cmu.lti.ws4j.impl;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.RelatednessCalculatorTest;

/**
 * The expected values are taken from the actual output by WordNet-Similarity-2.05.
 * Note that lesk score in the web demo are slightly different --  
 * the demo seems to be using special cannonicalization methods to do a conversion e.g.
 * <ul>
 * <li>"studies" => "study"</li> 
 * <li>"one's" => "one s" </li>
 * </ul>
 * 
 * @author Hideki Shima
 *
 */
public class LeskTest extends RelatednessCalculatorTest {

	private static RelatednessCalculator rc;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		ILexicalDatabase db = new NictWordNet();
		rc = new Lesk(db);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.impl.Lesk#calcRelatedness(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnSynsets() {
		// both japanese and english!, n and v
		
		// 184 => 213 in the web demo
		assertEquals(184,  rc.calcRelatednessOfSynset(n1Synsets.get(1), n2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(3,    rc.calcRelatednessOfSynset(n1Synsets.get(0), n2Synsets.get(0)).getScore(), 0.0001 );
	
		// 73 => 90 in the web demo
		assertEquals(73,   rc.calcRelatednessOfSynset(v1Synsets.get(0), v2Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(4,    rc.calcRelatednessOfSynset(v1Synsets.get(1), v2Synsets.get(0)).getScore(), 0.0001 );
		
		//link type sim is not available in wn-ja, so we get much lower scores in the test case below
		//assertEquals(38,   rc.calcRelatednessOfSynsets(a1Synsets.get(0), a2Synsets.get(0)).getScore(), 0.0001 );
		
		assertEquals(36,   rc.calcRelatednessOfSynset(r1Synsets.get(0), r2Synsets.get(0)).getScore(), 0.0001 );
		
		//combo!
		assertEquals(4,    rc.calcRelatednessOfSynset(n3Synsets.get(1), v3Synsets.get(0)).getScore(), 0.0001 );
	}

	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWords() {
		assertEquals(184, rc.calcRelatednessOfWords(n1, n2), 0.0001D);
		assertEquals( 73, rc.calcRelatednessOfWords(v1, v2), 0.0001D);
		assertEquals( 36, rc.calcRelatednessOfWords(r1, r2), 0.0001D);
		assertEquals(  4, rc.calcRelatednessOfWords(n3, v3), 0.0001D);
	}
	
	/**
	 * Test method for {@link edu.cmu.lti.similarity.RelatednessCalculator#calcRelatednessOfWords(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testHappyPathOnWordsWithPOS() {
		assertEquals(72, rc.calcRelatednessOfWords(nv1+"#n", nv2+"#n"), 0.0001D);
		assertEquals( 3, rc.calcRelatednessOfWords(nv1+"#n", nv2+"#v"), 0.0001D);
		assertEquals( 5, rc.calcRelatednessOfWords(nv1+"#v", nv2+"#n"), 0.0001D);
		assertEquals( 8, rc.calcRelatednessOfWords(nv1+"#v", nv2+"#v"), 0.0001D);
		assertEquals( 0, rc.calcRelatednessOfWords(nv1+"#other", nv2+"#other"), 0.0001D);
	}
	
	@Test
	public void testOnUnknownSynsets() {
		assertEquals(Lesk.min, rc.calcRelatednessOfSynset(null, n1Synsets.get(0)).getScore(), 0.0001 );
		assertEquals(Lesk.min, rc.calcRelatednessOfWords(null, n1), 0.0001 );
		assertEquals(Lesk.min, rc.calcRelatednessOfWords("", n1), 0.0001 );
	}
}
