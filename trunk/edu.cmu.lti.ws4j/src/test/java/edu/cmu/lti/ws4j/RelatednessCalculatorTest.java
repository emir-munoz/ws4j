package edu.cmu.lti.ws4j;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.util.Configuration;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

abstract public class RelatednessCalculatorTest {
	{
		WS4JConfiguration.getInstance().setLeskNormalize( false );
		WS4JConfiguration.getInstance().setMFS( false );
		Configuration.getInstance().setMemoryDB(false);
	}
	
	public String N = "n";
	public String V = "v";
	public String A = "a";
	public String R = "r";
	
	//init
	public String n1 = "cyclone";
	public String n2 = "hurricane";
	public List<Concept> n1Synsets = toSynsets(n1, N);
	public List<Concept> n2Synsets = toSynsets(n2, N);

	public final String v1 = "migrate";
	public final String v2 = "emigrate";
	public final List<Concept> v1Synsets = toSynsets(v1, V);
	public final List<Concept> v2Synsets = toSynsets(v2, V);
	
	public final String a1 = "huge";
	public final String a2 = "tremendous";
	public final List<Concept> a1Synsets = toSynsets(a1, A);
	public final List<Concept> a2Synsets = toSynsets(a2, A);
	
	public final String r1 = "eventually";
	public final String r2 = "finally";
	public final List<Concept> r1Synsets = toSynsets(r1, R);
	public final List<Concept> r2Synsets = toSynsets(r2, R);
	
	public final String n3 = "manuscript";
	public final String v3 = "write_down";
	public final List<Concept> n3Synsets = toSynsets(n3, N);
	public final List<Concept> v3Synsets = toSynsets(v3, V);
	
	public final String nv1 = "chat";
	public final String nv2 = "talk";
	
	private List<Concept> toSynsets( String word, String posText ) {
		POS pos2 = POS.valueOf(posText); 
		List<Synset> synsets = WordNetUtil.wordToSynsets(word, pos2);
		List<Concept> concepts = new ArrayList<Concept>(synsets.size());
		for ( Synset synset : synsets ) {
			concepts.add( new Concept(synset.getSynset(), POS.valueOf(posText)) );
		}
		return concepts;
	}
	
	abstract public void testHappyPathOnSynsets();
	abstract public void testHappyPathOnWords();
	abstract public void testHappyPathOnWordsWithPOS();
	abstract public void testOnUnknownSynsets();
}
