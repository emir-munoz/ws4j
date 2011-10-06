package edu.cmu.lti.ws4j.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class OverlapFinder {
	
	private final static String MARKER = "###";
		
	public static Overlaps getOverlaps( String gloss1, String gloss2 ) {
		
		String string0 = gloss1;
		String string1 = gloss2;

		Overlaps overlaps = new Overlaps();
		
		String[] words0 = string0.split("\\s+");
		String[] words1 = string1.split("\\s+");
		
		words0 = StopWordRemover.getInstance().removeStopWords( words0 ); 
		words1 = StopWordRemover.getInstance().removeStopWords( words1 );

		// for tracing
		gloss1 = CollectionUtil.join(" ", words0);
		gloss2 = CollectionUtil.join(" ", words1);
		
		overlaps.length1 = words0.length;
		overlaps.length2 = words1.length;
		
		Map<Integer,Integer> overlapsLengths = new HashMap<Integer,Integer>(); // TODO: Init
		
		
		int matchStartIndex = 0;
		int currIndex = -1;
		
		while ( currIndex < words0.length-1 ) {
			currIndex++;
			//System.out.print( "currIndex = " + currIndex+", matchStartIndex = "+matchStartIndex );
			if ( contains( words1, words0, matchStartIndex, currIndex ) ) {
				//System.out.println( ", contains = true" );
				continue;
			} else {
				//System.out.println( ", overlapsLengths.get(matchStartIndex) = "+overlapsLengths.get(matchStartIndex)+", contains = false" );
				overlapsLengths.put(matchStartIndex, currIndex - matchStartIndex);
				if ( overlapsLengths.get(matchStartIndex) != null && overlapsLengths.get(matchStartIndex) > 0 ) currIndex--;
				matchStartIndex++;
			}
		}
		for ( int i=matchStartIndex; i <= currIndex; i++ ) {
			overlapsLengths.put(i, currIndex - i + 1);
		}

		int longestOverlap = -1;
		for ( int length : overlapsLengths.values() ) {
			if ( longestOverlap < length ) longestOverlap = length; 
		}
		overlaps.overlapsHash = new ConcurrentHashMap<String,Integer>( overlapsLengths.size() );
		
		while ( longestOverlap > 0 ) {
			for ( int i=0; i<=overlapsLengths.size()-1; i++ ) {
				if ( overlapsLengths.get(i) < longestOverlap ) continue;
				int stringEnd = i + longestOverlap - 1;
				
				//TODO: if (1 #!doStop($temp) && containsReplace (@words1, @words0[$i..$stringEnd]))
				if ( containsReplace( words1, words0, i, stringEnd ) ) {
					List<String> words0Sub = new ArrayList<String>( stringEnd - i + 1 );
					for ( int j=i; j<=stringEnd; j++ ) {
						words0Sub.add( words0[j] );
					}
					String temp = CollectionUtil.join(" ", words0Sub);
					synchronized ( overlaps.overlapsHash ) {
						int v = overlaps.overlapsHash.get(temp)!=null?overlaps.overlapsHash.get(temp):0;
						overlaps.overlapsHash.put(temp, v+1);
					}
					
					for ( int j=i; j < i+longestOverlap; j++ ) {
						overlapsLengths.put(j, 0);
					}
					for ( int j=i-1; j>=0; j-- ) {
						if ( overlapsLengths.get(j) <= i-j ) break;
						overlapsLengths.put(j, i-j);
					}
				} else {
					int k = longestOverlap - 1;
					while ( k > 0 ) {
						int stringEndNew = i + k - 1;
						if ( contains( words1, words0, i, stringEndNew ) ) {
							break;
						}
						k--;
					}
					overlapsLengths.put(i, k);
				}
			}
			// update longestOverlap
			longestOverlap = -1;
			for ( int length : overlapsLengths.values() ) {
				if ( longestOverlap < length ) longestOverlap = length; 
			}
		}
		//System.out.println( "2>>"+overlapsLengths );
		
		return overlaps;
	}
	
	//perl -e 'sub c(\@@){$a2=shift; @a1=@_; print $a1[0] } @w1=(1,2,3);@w2=(4,5,6);c(@w1,@w2);' => 4
	private static boolean contains( String[] words1, String[] words2, int begin, int end ) {
		return contains( words1, words2, begin, end, false );
	}
	
	private static boolean containsReplace( String[] words1, String[] words2, int begin, int end ) {
		return contains( words1, words2, begin, end, true );
	}

	private static boolean contains( String[] words1, String[] words2, 
			int begin, int end, boolean doReplace ) {
		// creating sub array
		String[] words2Sub = new String[ end - begin + 1 ];
		for ( int i=0; i<end-begin+1; i++ ) {
			words2Sub[ i ] = words2[ begin+i ];
		}
		words2 = words2Sub;
				
		if ( words1.length < words2.length ) return false;
		
		for ( int j=0; j<=words1.length - words2.length; j++ ) {
			if ( words1[j].equals( MARKER ) ) continue;
			if ( words2[0].equals( words1[j] ) ) {
				boolean match = true;
				for ( int i=1; i<words2.length; i++ ) {
					if ( words1[ j+i ].equals( MARKER ) 
							|| !words2[i].equals( words1[ j+i ] ) ) {
						match = false;
						break;
					}
				}
				// match found, remove match and return true
				if ( match ) {
					if ( doReplace ) {
						for ( int k=j; k<j+words2.length; k++ ) {
							words1[k] = MARKER;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public static class Overlaps {
		public ConcurrentMap<String,Integer> overlapsHash;
		public int length1;
		public int length2;
	}
	
}
