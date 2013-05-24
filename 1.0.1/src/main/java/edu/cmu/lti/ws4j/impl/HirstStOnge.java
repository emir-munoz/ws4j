package edu.cmu.lti.ws4j.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.Traverser;


public class HirstStOnge extends RelatednessCalculator {

	protected static double min = 0;
	protected static double max = 16;

	@SuppressWarnings("serial")
	private static List<POS[]> posPairs = new ArrayList<POS[]>(){{
		add(new POS[]{POS.n,POS.n});
		add(new POS[]{POS.v,POS.v});
		add(new POS[]{POS.a,POS.a});
		add(new POS[]{POS.r,POS.r});
	}};
	
	public HirstStOnge(ILexicalDatabase db) {
		super(db);
	}

	@Override
	protected Relatedness calcRelatedness( Concept synset1, Concept synset2 ) {
		StringBuilder tracer = new StringBuilder();
		if ( synset1 == null || synset2 == null ) return new Relatedness( min, null, illegalSynset );
		if ( synset1.getSynset().equals( synset2.getSynset() ) ) return new Relatedness( max, identicalSynset, null );
		
		Set<String> horizontal1 = Traverser.getHorizontalSynsets( synset1.getSynset() );	
		Set<String> horizontal2 = Traverser.getHorizontalSynsets( synset2.getSynset() );
		
		boolean inHorizon = horizontal2.contains(synset1.getSynset()) || horizontal1.contains(synset2.getSynset());
		
		if ( inHorizon ) return new Relatedness( max );
		
		Set<String> upward2 = Traverser.getUpwardSynsets( synset2.getSynset() );
		Set<String> downward2 = Traverser.getDownwardSynsets( synset2.getSynset() );
		
		if ( enableTrace ) {
			tracer.append( "Horizontal Links of "+synset1.getSynset()+": "+horizontal1+"\n");
			tracer.append( "Horizontal Links of "+synset2.getSynset()+": "+horizontal1+"\n");
			tracer.append( "Upward Links of "+synset2.getSynset()+": "+upward2+"\n");
			tracer.append( "Downward Links of "+synset2.getSynset()+": "+downward2+"\n");
		}
		
		// Compare all possible surface forms related to the synset (improved from the Wordnet::Similarity)
		boolean contained = Traverser.contained( synset1, synset2 );
		// We don't need if synset2 is contained in upward1 or downward1!
		boolean inUpOrDown = upward2.contains( synset1.getSynset() ) || downward2.contains( synset1.getSynset() );
		if ( contained && inUpOrDown ) {
			tracer.append( "Strong Rel (Compound Word Match).\n" );
			return new Relatedness( max, tracer.toString(), null );
		}
		
		MedStrong medStrong = new MedStrong();
		int score = medStrong.medStrong( 0, 0, 0, synset1.getSynset(), synset1.getSynset(), synset2.getSynset() );
		
		return new Relatedness( score, tracer.toString(), null );
	}

	@Override
	public List<POS[]> getPOSPairs() {
		return posPairs;
	}

	private static class MedStrong {
		
		// Not available
		// public String bestPath;
		
		/**
		 * Ported from perl implementation in WordNet::Similarity package.
		 * 
		 * .. The state of the state machine.
		 * Similarity (links) covered thus far.
		 * Number of changes in direction thus far.
		 * Current node.
		 * Path so far.
		 * Last offset.
		 * 
		 * 
		 *  _medStrong returns the maximum length of a legal path that was found in a
		 *   given subtree of the recursive search. These return values are used by
		 *    _medStrong and the highest of these is returned.
		 * @return
		 */
		public int medStrong( int state, int distance, int chdir, String from, String path, String endSynset ) {
			if ( from.equals(endSynset) && distance > 1 ) {
				return 8 - distance - chdir;
			}
			if ( distance >= 5 ) {
				return 0;
			}
			
			//speed up -- don't process unless needed
			Set<String> horizontal = ( state!=4 || state!=7 )?Traverser.getHorizontalSynsets( from ):null;
			Set<String> upward     = ( state==0 || state==1 )?Traverser.getUpwardSynsets( from ):null;
			Set<String> downward   = ( state!=6 )?Traverser.getDownwardSynsets( from ):null;
			
			if ( state==0 ) {
				int retU = findU( upward,     1, distance, 0, path, endSynset );
				int retD = findD( downward,   2, distance, 0, path, endSynset );
				int retH = findH( horizontal, 3, distance, 0, path, endSynset );
				if ( retU > retD && retU > retH ) return retU; 
				if ( retD > retH ) return retD;
				return retH;
			} else if ( state==1 ) {
				int retU = findU( upward,     1, distance, 0, path, endSynset );
				int retD = findD( downward,   4, distance, 1, path, endSynset );
				int retH = findH( horizontal, 5, distance, 1, path, endSynset );
				if ( retU > retD && retU > retH ) return retU; 
				if ( retD > retH ) return retD;
				return retH;
			} else if ( state==2 ) {
				int retD = findD( downward,   2, distance, 0, path, endSynset );
				int retH = findH( horizontal, 6, distance, 0, path, endSynset );
				return retD > retH ? retD : retH;
			} else if ( state==3 ) {
				int retD = findD( downward,   7, distance, 0, path, endSynset );
				int retH = findH( horizontal, 3, distance, 0, path, endSynset );
				return retD > retH ? retD : retH;
			} else if ( state==4 ) {
				int retD = findD( downward,   4, distance, 1, path, endSynset );
				return retD;
			} else if ( state==5 ) {
				int retD = findD( downward,   4, distance, 2, path, endSynset );
				int retH = findH( horizontal, 5, distance, 1, path, endSynset );
				return retD > retH ? retD : retH;
			} else if ( state==6 ) {
				int retH = findH( horizontal, 6, distance, 1, path, endSynset );
				return retH;
			} else if ( state==7 ) {
				int retD = findD( downward,   7, distance, 1, path, endSynset );
				return retD;
			}
			return 0;
		}
		
		private int findD( Set<String> downward, int state, int distance, int chdir, String path, String endSynset ) {
			return find( downward, state, distance, chdir, path, endSynset, "D" );
		}
		
		private int findU( Set<String> upward, int state, int distance, int chdir, String path, String endSynset ) {
			return find( upward, state, distance, chdir, path, endSynset, "U" );
		}
		
		private int findH( Set<String> horizontal, int state, int distance, int chdir, String path, String endSynset ) {
			return find( horizontal, state, distance, chdir, path, endSynset, "H" );
		}
		
		private int find( Set<String> synsetGroup, int state, int distance, int chdir, String path, String endSynset, String abbreviation ) {
			int ret = 0;
			for ( String synset : synsetGroup ) {
				int retT = medStrong(state, distance+1, chdir, synset, path+" ["+abbreviation+"] "+synset, endSynset);
				if ( retT > ret ) {
					ret = retT;
				}
			}
			return ret;
		}
	}
}
