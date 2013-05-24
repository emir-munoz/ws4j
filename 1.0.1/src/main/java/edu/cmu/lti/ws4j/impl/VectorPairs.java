package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;

//TODO: finish implementation
public class VectorPairs extends RelatednessCalculator {

	protected static double min = 0;
	// max unknown...
	
	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
	}};

	public VectorPairs(ILexicalDatabase db) {
		super(db);
		// TODO Auto-generated constructor stub
	}

	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		//Don't short-circuit
		//if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max ); 
		
		return new Relatedness( 0 );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
