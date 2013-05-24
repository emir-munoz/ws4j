package edu.cmu.lti.ws4j.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.util.PathFinder.Subsumer;

/**
 * IC: information content
 * 
 * @author Hideki
 *
 */
final public class ICFinder {

	private static final ICFinder instance = new ICFinder();
	
	// To be static?
	private ConcurrentMap<Integer,Integer> freqV;
	private ConcurrentMap<Integer,Integer> freqN;

	private final static int rootFreqN = 128767; // sum of all root freq of n in 
	private final static int rootFreqV = 95935;  // sum of all root freq of v in ic-semcor.dat
	
	/**
	 * Private constructor 
	 */
	private ICFinder(){
		try {
			loadIC();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Singleton pattern
	 * @return singleton object
	 */
	public static ICFinder getInstance(){
		return ICFinder.instance;
	}
	
	private synchronized void loadIC() throws IOException {
		String icFilename = WS4JConfiguration.getInstance().getInfoContent();
		freqV = new ConcurrentHashMap<Integer,Integer>();
		freqN = new ConcurrentHashMap<Integer,Integer>();
		InputStream stream = ICFinder.class.getResourceAsStream( "/"+icFilename ); 
		InputStreamReader isr = new InputStreamReader( stream );
		BufferedReader br = new BufferedReader( isr );
		String line = null;
		while ( ( line = br.readLine() ) != null ) {
			String[] elements = line.split(" ");
			if ( elements.length >= 2 ) {
				String e = elements[0];
				POS pos = POS.valueOf( e.substring( e.length()-1 ) );
				int id = Integer.parseInt( e.substring( 0, e.length()-1 ) );
				int freq = Integer.parseInt( elements[1] );
				if ( pos.equals( POS.n ) ) {
					freqN.put(id, freq);
				} else if ( pos.equals( POS.v ) ) {
					freqV.put(id, freq);
				}
			}
		}
		br.close();
		isr.close();
	}
	
	public List<Subsumer> getLCSbyIC( PathFinder pathFinder, 
		Concept synset1, Concept synset2, StringBuilder tracer ) {
		List<Subsumer> paths = pathFinder.getAllPaths( synset1, synset2, tracer );
		if ( paths == null || paths.size()==0 ) return null;
		
		for ( Subsumer path : paths ) {
			path.ic = ic( pathFinder, path.subsumer );
		}
		
		// in desc order (b <==> a)
		Collections.sort( paths, new Comparator<Subsumer>() {
			public int compare( Subsumer s1, Subsumer s2 ) {
				if ( s1.ic < s2.ic ) {
					return 1;
				} else if ( s1.ic > s2.ic ) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		List<Subsumer> results = new ArrayList<Subsumer>( paths.size() );  
		
		// determine which subsumers have the highest info content; do some tracing as well
		for ( Subsumer path : paths ) {
			if ( path.ic == paths.get(0).ic ) {
				results.add( path );
			}
		}
		
		return results;
	}
	
	//TODO: what to do for root?
	public double ic( PathFinder pathFinder, Concept synset ) {
		POS pos = synset.getPos();
		
		if ( pos.equals(POS.n) || pos.equals(POS.v) ) {
			double prob = probability( pathFinder, synset );
			return prob>0 ? -Math.log(prob) : 0D;
		} else {
			return 0D;
		}
	}
	
	private static int synsetToId( String synset ) {
		return Integer.parseInt( synset.replaceFirst("^[0]+","").replaceFirst("-[nvar]", "") );
	}
	

	//TODO: ok to use single root? what happens if multiple?
	private double probability( PathFinder pathFinder, Concept synset ) {
		Concept rootSynset = pathFinder.getRoot( synset.getSynset() );
				
		int rootFreq = 0;
		if ( RelatednessCalculator.useRootNode ) {
			if ( synset.getPos().equals( POS.n ) ) {
				rootFreq = rootFreqN; // sum of all root freq of n in 
			} else if ( synset.getPos().equals( POS.v ) ) {
				rootFreq = rootFreqV; // sum of all root freq of v in ic-semcor.dat
			}
		} else {
			rootFreq = getFrequency( rootSynset );
		}
		int offFreq  = getFrequency( synset );
		
		if ( offFreq <= rootFreq ) {
			return (double)offFreq / (double)rootFreq; // happy scenario
		}
		return 0D; // unhappy scenario
	}
	
	public int getFrequency( Concept synset ) {
		if ( synset.getSynset().equals("0") ) {
			if ( synset.getPos().equals( POS.n ) ) {
				return rootFreqN; 
			} else if ( synset.getPos().equals( POS.v ) ) {
				return rootFreqV;
			}
		}
		int synsetId = synsetToId( synset.getSynset() );
		int freq = 0;
		if ( synset.getPos().equals( POS.n ) ) {
			Integer freqObj = freqN.get( synsetId );
			freq  = freqObj!=null ? freqObj : 0;
		} else if ( synset.getPos().equals( POS.v ) ) {
			Integer freqObj = freqV.get( synsetId );
			freq  = freqObj!=null ? freqObj : 0;
		}
		return freq;
	}
	
}
