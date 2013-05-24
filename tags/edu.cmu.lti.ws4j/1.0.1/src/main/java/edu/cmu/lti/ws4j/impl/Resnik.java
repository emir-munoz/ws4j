package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.ICFinder;
import edu.cmu.lti.ws4j.util.PathFinder.Subsumer;

public class Resnik extends RelatednessCalculator {

	protected static double min = 0;
	protected static double max = Double.MAX_VALUE;
	
	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
	}};

	public Resnik(ILexicalDatabase db) {
		super(db);
	}

	@Override
	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		//Don't short-circuit!
		//if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max );
		
		StringBuilder subTracer = enableTrace ? new StringBuilder() : null;
		
		List<Subsumer> lcsList = ICFinder.getInstance().getLCSbyIC( pathFinder, synset1, synset2, subTracer );
		if ( lcsList.size() == 0 ) return new Relatedness( min, tracer.toString(), null );	
		if ( enableTrace ) {
			tracer.append( subTracer.toString() );
			for ( Subsumer lcs : lcsList ) {
				tracer.append( "Lowest Common Subsumer(s): ");
				tracer.append( db.conceptToString( lcs.subsumer.getSynset() )+" (IC="+lcs.ic+")\n" );
			}
		}
		
		Subsumer subsumer = lcsList.get(0);
		double score = subsumer.ic;
		
		return new Relatedness( score, tracer.toString(), null );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
