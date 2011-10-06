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

public class Lin extends RelatednessCalculator {
	
	protected static double min = 0; // or -Double.MAX_VALUE ?
	protected static double max = 1;
	
	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
	}};

	public Lin(ILexicalDatabase db) {
		super(db);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max, identicalSynset, null );
		
		StringBuilder subTracer = enableTrace ? new StringBuilder() : null;
		List<Subsumer> lcsList = ICFinder.getInstance().getLCSbyIC( pathFinder, synset1, synset2, subTracer );
		if ( lcsList.size() == 0 ) return new Relatedness( min, tracer.toString(), null );	
		
		double ic1 = ICFinder.getInstance().ic( pathFinder, synset1 );
		double ic2 = ICFinder.getInstance().ic( pathFinder, synset2 );
		double score = ( ic1>0 && ic2>0 ) 
				? (2D * lcsList.get(0).ic / ( ic1 + ic2 ) ) 
				: 0D;
		
		if ( enableTrace ) {
			tracer.append( subTracer.toString() );
			for ( Subsumer lcs : lcsList ) {
				tracer.append( "Lowest Common Subsumer(s): ");
				tracer.append( db.conceptToString( lcs.subsumer.getSynset() )+" (IC="+lcs.ic+")\n" );
			}
			tracer.append( "Concept1: "+db.conceptToString(synset1.getSynset())+" (IC="+ic1+")\n" );
			tracer.append( "Concept2: "+db.conceptToString(synset2.getSynset())+" (IC="+ic2+")\n" );
		}
				
		return new Relatedness( score, tracer.toString(), null );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
