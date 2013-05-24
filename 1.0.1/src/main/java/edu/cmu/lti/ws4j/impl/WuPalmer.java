package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.DepthFinder.Depth;

/**
 * @author Hideki
 *
 */
public class WuPalmer extends RelatednessCalculator {

	protected static double min = 0;
	protected static double max = 1;
	
	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
	}};

	public WuPalmer(ILexicalDatabase db) {
		super(db);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max, identicalSynset, null );
		
		StringBuilder subTracer = enableTrace ? new StringBuilder() : null;
		
		List<Depth> lcsList = depthFinder.getRelatedness( synset1, synset2, subTracer );
		if ( lcsList.size() == 0 ) return new Relatedness( min );
		
		int depth = lcsList.get(0).depth; // sorted by depth (asc)
		int depth1 = depthFinder.getShortestDepth( synset1 );
		int depth2 = depthFinder.getShortestDepth( synset2 );
		double score = 0;
		if (depth1>0 && depth2 >0) { 
			score = (double)( 2 * depth ) / (double)( depth1 + depth2 );
		}
		
		if ( enableTrace ) {
			tracer.append( subTracer.toString() );
			for ( Depth lcs : lcsList ) {
				tracer.append( "Lowest Common Subsumer(s): ");
				tracer.append( db.conceptToString( lcs.leaf )+" (Depth="+lcs.depth+")\n" );
			}
			tracer.append( "Depth1( "+db.conceptToString(synset1.getSynset())+" ) = "+depth1+"\n" );
			tracer.append( "Depth2( "+db.conceptToString(synset2.getSynset())+" ) = "+depth2+"\n" );
		}
		
		return new Relatedness( score, tracer.toString(), null );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
