package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.PathFinder.Subsumer;

public class Path extends RelatednessCalculator {

	protected static double min = 0; // actually, (0, 1]
	protected static double max = 1;
	
	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
	}};

	public Path(ILexicalDatabase db) {
		super(db);
	}

	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max, identicalSynset, null );
		
		StringBuilder subTracer = enableTrace ? new StringBuilder() : null;
		List<Subsumer> shortestPaths = pathFinder.getShortestPaths( synset1, synset2, subTracer );
		if ( shortestPaths.size() == 0 ) return new Relatedness( min ); // TODO message
		Subsumer path = shortestPaths.get(0);
		int dist = path.length;
		double score;
		if ( dist > 0 ) {
			score = 1D / (double)dist;
		} else {
			score = -1;
		}
		
		if ( enableTrace ) {
			tracer.append( subTracer.toString() );
			tracer.append( "Shortest path: "+path+"\n" );
			tracer.append( "Path length = "+dist+"\n" );
		}
				
		return new Relatedness( score, tracer.toString(), null );
	}
	
	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}
}
