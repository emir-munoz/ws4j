package edu.cmu.lti.ws4j.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.ws4j.RelatednessCalculator;

public class MatrixCalculator {
	
	public static double[][] getSimilarityMatrix( String[] words1, String[] words2,
			RelatednessCalculator rc ) {
		double[][] result = new double[words1.length][words2.length];
		for ( int i=0; i<words1.length; i++ ) {
			for ( int j=0; j<words2.length; j++ ) {
				double score = rc.calcRelatednessOfWords(words1[i], words2[j]);
				result[i][j] = score;
			}
		}
		return result;
	}
	
	public static double[][] getNormalizedSimilarityMatrix( 
			String[] words1, String[] words2, RelatednessCalculator rc ) {
		double[][] scores = getSimilarityMatrix( words1, words2, rc );
		double bestScore = 1; // normalize if max is above 1
		for ( int i=0; i<scores.length; i++ ) {
			for ( int j=0; j<scores[i].length; j++ ) {
				if ( scores[i][j] > bestScore && scores[i][j] != Double.MAX_VALUE ) {
					bestScore = scores[i][j];
				}
			}
		}
		
		for ( int i=0; i<scores.length; i++ ) {
			for ( int j=0; j<scores[i].length; j++ ) {
				
				if ( scores[i][j] == Double.MAX_VALUE ) {
					scores[i][j] = 1;
				} else {
					scores[i][j] /= bestScore;
				}
			}
		}
		return scores;
	}
	
	//write test case with 
	//String[] n1 = {"電子管"};
	//String[] n2 = {"熱電子管"};
	public static double[][] getSynonymyMatrix( String[] words1, String[] words2 ) {
		List<Set<String>> synonyms1 = new ArrayList<Set<String>>( words1.length );
		List<Set<String>> synonyms2 = new ArrayList<Set<String>>( words2.length );
		
		for ( int i=0; i<words1.length; i++ ) {
			Set<String> synonyms = new LinkedHashSet<String>();
			for ( POS pos : POS.values() ) {
				synonyms.addAll( JAWJAW.findSynonyms( words1[i], pos ) );
			}
			synonyms1.add( synonyms );
		}
		for ( int j=0; j<words2.length; j++ ) {
			Set<String> synonyms = new LinkedHashSet<String>();
			for ( POS pos : POS.values() ) {
				synonyms.addAll( JAWJAW.findSynonyms( words2[j], pos ) );
			}
			synonyms2.add( synonyms );
		}
		
		double[][] result = new double[words1.length][words2.length];
		for ( int i=0; i<words1.length; i++ ) {
			for ( int j=0; j<words2.length; j++ ) {
				String w1 = words1[i];
				String w2 = words2[j];
				if ( w1.equals( w2 ) ) {
					result[i][j] = 1D;
					continue;
				}
				Set<String> s1 = synonyms1.get(i);
				Set<String> s2 = synonyms2.get(j);
				
				result[i][j] = (s1.contains(w2) || s2.contains(w1))?1:0;
			}
		}
		return result;
	}
}
