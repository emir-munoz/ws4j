package edu.cmu.lti.ws4j.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;

public class WordSimilarityCalculator {

	private static WS4JConfiguration c;
	public static boolean enableTrace;
	private static int MAX_CACHE_SIZE;
	
	// To be overridden
	protected static double min = 0;
	protected static double max = Double.MAX_VALUE;
	
	// Don't make it static, as inter-metric cache space collision happens.
	private ConcurrentMap<String,Double> cache;
		
	static {
		c = WS4JConfiguration.getInstance();
		enableTrace = c.useTrace();
		MAX_CACHE_SIZE = c.getMaxCacheSize();	
	}
	
	public WordSimilarityCalculator() {
		if (WS4JConfiguration.getInstance().useCache()) {
			cache = new ConcurrentHashMap<String,Double>( MAX_CACHE_SIZE );
		}
	}
	
	public double calcRelatednessOfWords( 
			String word1, String word2, RelatednessCalculator rc ) {
		String key = word1+" & "+word2;
		
		// Return the max possible value anyway.
		if ( word1!=null && word2!=null && word1.equals(word2) ) return max;
		// Short circuit
		if ( word1==null || word2==null || word1.length()==0 || word2.length()==0 ) return min;
	
		POS pos1 = null;
		int offset1 = word1.indexOf("#");
		if ( offset1 != -1 ) {
			try {
				pos1 = POS.valueOf( word1.substring(offset1+1) );
			} catch ( IllegalArgumentException e ) {
				return min;
			}
			word1 = word1.substring(0,offset1);
		}
		POS pos2 = null;
		int offset2 = word2.indexOf("#");
		if ( offset2 != -1 ) {
			try {
				pos2 = POS.valueOf( word2.substring(offset2+1) );
			} catch ( IllegalArgumentException e ) {
				return min;
			}
			word2 = word2.substring(0,offset2);
		}
		
		if ( WS4JConfiguration.getInstance().useCache() ) {
			Double cachedObj = cache.get(key);
			if ( cachedObj != null ) return cachedObj;
		}
		
		double maxScore = -1D;
		//supported pos pair depends on the metric. Mostly, n:n and v:v only.
		List<POS[]> posPairs = rc.getPOSPairs();
		for ( POS[] posPair : posPairs ) {
			if ( pos1!=null && pos1 != posPair[0] ) continue;
			if ( pos2!=null && pos2 != posPair[1] ) continue;
			
			List<Concept> synsets1 = (List<Concept>)rc.getDB().getAllConcepts(word1, posPair[0].toString());
			List<Concept> synsets2 = (List<Concept>)rc.getDB().getAllConcepts(word2, posPair[1].toString());
			
			LOOP: 
			for ( Concept synset1 : synsets1 ) {
				for ( Concept synset2 : synsets2 ) {
					Relatedness relatedness = rc.calcRelatednessOfSynset( synset1, synset2 );
					double score = relatedness.getScore();
					if ( score > maxScore ) maxScore = score;
					if ( WS4JConfiguration.getInstance().useMFS() ) break LOOP;
				}
			}
		}
		
		if ( maxScore == -1D ) {
			// TODO: let's do something here
			maxScore = 0; 
		}
		
		if ( WS4JConfiguration.getInstance().useCache() ) {
//			synchronized ( cache ) {
				if ( cache.size() >= WS4JConfiguration.getInstance().getMaxCacheSize() ) {
					cache.remove( cache.keySet().iterator().next() );
				}
				cache.put(key, maxScore);
//			}
		}
		
		return maxScore;
	}
	
}
