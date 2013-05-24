package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.PathFinder.Subsumer;

/**
 * (from lch.pm) This module computes the semantic relatedness of word senses according
to a method described by Leacock and Chodorow (1998). This method counts up
the number of edges between the senses in the 'is-a' hierarchy of WordNet.
The value is then scaled by the maximum depth of the WordNet 'is-a'
hierarchy. A relatedness value is obtained by taking the negative log
of this scaled value.
 * @author Hideki
 *
 */
public class LeacockChodorow extends RelatednessCalculator {

	protected static double min = 0; // or -Double.MAX_VALUE ?
	protected static double max = Double.MAX_VALUE;

	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
	}};
	
	public LeacockChodorow(ILexicalDatabase db) {
		super(db);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		//Don't short-circuit!
		//if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max );
		
		StringBuilder subTracer = enableTrace ? new StringBuilder() : null;
		List<Subsumer> lcsList = pathFinder.getLCSByPath( synset1, synset2, subTracer );
		if ( lcsList.size() == 0 ) return new Relatedness( min );
		
		//TODO: investigate if these values are always valid for wn-jpn-0.9.0
		int maxDepth = 1;
		if ( synset1.getPos().equals( POS.n ) ) {
			maxDepth = 20;
		} else if ( synset1.getPos().equals( POS.v ) ) {
			maxDepth = 14;
		}
		
		//System.out.println(lcsList);
		int length = lcsList.get( 0 ).length;
		
//	 	int maxDepth = -1;	
//		for ( Depth lcs : lcsList ) {
//			
//			List<String> roots = getTaxonomies( lcs );
//			for ( String root : roots ) {
//				int depth = getTaxonomyDepth( root );
//				if ( depth > maxDepth ) maxDepth = depth;
//			}
//			
//		}
		
		double score = -Math.log( (double)length / (double)( 2 * maxDepth ) );
		
		if ( enableTrace ) {
			tracer.append( subTracer.toString() );
			for ( Subsumer lcs : lcsList ) {
				tracer.append( "Lowest Common Subsumer(s): ");
				tracer.append( db.conceptToString( lcs.subsumer.getSynset() )+" (Length="+lcs.length+")\n" );
			}
		}
				
		return new Relatedness( score, tracer.toString(), null );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
