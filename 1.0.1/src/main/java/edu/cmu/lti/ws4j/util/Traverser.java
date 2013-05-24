package edu.cmu.lti.ws4j.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.cmu.lti.jawjaw.db.SynlinkDAO;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.Word;
import edu.cmu.lti.jawjaw.util.WordNetUtil;
import edu.cmu.lti.lexical_db.data.Concept;

//TODO: separate util into synsetutil and misc util?
public class Traverser {

	/* Discussion: should we make it non-static?*/
	private static ConcurrentMap<String,Set<String>> horizonCache;
	private static ConcurrentMap<String,Set<String>> upwardCache;
	private static ConcurrentMap<String,Set<String>> downwardCache;
	public static int capacity;
	
	static {
		if ( WS4JConfiguration.getInstance().useCache() ) {
			capacity = WS4JConfiguration.getInstance().getMaxCacheSize();
			horizonCache = new ConcurrentHashMap<String,Set<String>>( capacity );
			upwardCache = new ConcurrentHashMap<String,Set<String>>( capacity );
			downwardCache = new ConcurrentHashMap<String,Set<String>>( capacity );
		}
	}
	
	/**
	 * Identify surface text level inclusion given two synsets.
	 * Original algorithm takes two original words whereas this
	 * implementation takes care of all surface forms related to the synsets.
	 * 
	 * @param synset1 synset
	 * @param synset2 another synset
	 * @return if haystack synset name is including needle synset name 
	 */
	public static boolean contained( Concept synset1, Concept synset2 ) {
		if ( synset1==null || synset2==null ) return false;
		List<Word> wordsH = WordNetUtil.synsetToWords( synset1.getSynset() );
		List<Word> wordsN = WordNetUtil.synsetToWords( synset2.getSynset() );
		
		for ( Word wordH : wordsH ) {
			for ( Word wordN : wordsN ) {
				if ( wordH.getLemma().indexOf( wordN.getLemma() ) != -1 || 
					 wordN.getLemma().indexOf( wordH.getLemma() ) != -1	) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * All horizontal links specified  are --
	 * Also See, Antonymy, Attribute, Pertinence, Similarity.
	 */
	public static Set<String> getHorizontalSynsets( String synset ) {
		String key = synset;
		if ( WS4JConfiguration.getInstance().useCache() ) {
			Set<String> cachedObj = horizonCache.get(key);
			if ( cachedObj != null ) return cachedObj;
		}
		
		List<Link> links = new ArrayList<Link>();
		links.add(Link.ants);
		links.add(Link.attr);
		links.add(Link.sim);
		
		Set<String> result = getGroupedSynsets( synset, links );
		if ( WS4JConfiguration.getInstance().useCache() ) {
//			synchronized ( horizonCache ) {
				if ( horizonCache.size() >= WS4JConfiguration.getInstance().getMaxCacheSize() ) {
					horizonCache.remove( horizonCache.keySet().iterator().next() );
				}
				if (result!=null) horizonCache.put(key, result); // CLONE!?
//			}
		}
		return result;
	}
	
	/**
	 * Upward link types -- Hypernymy, Meronymy
	 */
	public static Set<String> getUpwardSynsets( String synset ) {
		String key = synset;
		if ( WS4JConfiguration.getInstance().useCache() ) {
			Set<String> cachedObj = upwardCache.get(key);
			if ( cachedObj != null ) return cachedObj;
		}
		
		List<Link> links = new ArrayList<Link>();
		links.add(Link.hype);
		links.add(Link.mero);
		links.add(Link.mmem);
		links.add(Link.mprt);
		links.add(Link.msub);
		
		Set<String> result = getGroupedSynsets( synset, links );
		if ( WS4JConfiguration.getInstance().useCache() ) {
//			synchronized ( upwardCache ) {
				if ( upwardCache.size() >= WS4JConfiguration.getInstance().getMaxCacheSize() ) {
					upwardCache.remove( upwardCache.keySet().iterator().next() );
				}
				if (result!=null) upwardCache.put(key, result); // CLONE!?
//			}
		}
		return result;
	}
	
	/**
	 * subroutine that returns all offsetPOSs that are linked
	 * to a given synset by downward links. Downward link types --
	 * Cause, Entailment, Holonymy, Hyponymy.
	 */
	public static Set<String> getDownwardSynsets( String synset ) {
		String key = synset;
		if ( WS4JConfiguration.getInstance().useCache() ) {
			Set<String> cachedObj = downwardCache.get(key);
			if ( cachedObj != null ) return cachedObj;
		}
		
		List<Link> links = new ArrayList<Link>();
		links.add(Link.caus);
		links.add(Link.enta);
		links.add(Link.holo);
		links.add(Link.hmem);
		links.add(Link.hsub);
		links.add(Link.hprt);
		links.add(Link.hypo);
		
		Set<String> result = getGroupedSynsets( synset, links );
		if ( WS4JConfiguration.getInstance().useCache() ) {
//			synchronized ( downwardCache ) {
				if ( downwardCache.size() >= WS4JConfiguration.getInstance().getMaxCacheSize() ) {
					downwardCache.remove( downwardCache.keySet().iterator().next() );
				}
				if (result!=null) downwardCache.put(key, result); // CLONE!?
//			}
		}
		return result;
	}
	
	private static Set<String> getGroupedSynsets( String synset, List<Link> links ) {
		List<Synlink> synlinks = new ArrayList<Synlink>(); 
		for ( Link link : links ) {
			synlinks.addAll( SynlinkDAO.findSynlinksBySynsetAndLink(synset, link) );
		}
		Set<String> synsets = new LinkedHashSet<String>( synlinks.size() );
		for ( Synlink synlink : synlinks ) {
			synsets.add( synlink.getSynset2() );
		}
		// in case original synset is included...
		//synsets.remove( synset );
		return synsets;
	}
}
