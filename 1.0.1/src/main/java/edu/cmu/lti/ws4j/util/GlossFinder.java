package edu.cmu.lti.ws4j.util;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.data.Concept;

public class GlossFinder {

	/*
	 * following combination is applied by default??
also-also
also-attr
also-glos
also-holo
also-hype
also-hypo
also-mero
also-pert
also-sim
attr-also
attr-attr
attr-glos
attr-holo
attr-hype
attr-hypo
attr-mero
attr-pert
attr-sim
example-example
example-glos
example-syns
glos-also
glos-attr
glos-example
glos-glos
glos-holo
glos-hype
glos-hypo
glos-mero
glos-pert
glos-sim
glos-syns
holo-also
holo-attr
holo-glos
holo-holo
holo-hype
holo-hypo
holo-mero
holo-pert
holo-sim
hype-also
hype-attr
hype-glos
hype-holo
hype-hype
hype-hypo
hype-mero
hype-pert
hype-sim
hypo-also
hypo-attr
hypo-glos
hypo-holo
hypo-hype
hypo-hypo
hypo-mero
hypo-pert
hypo-sim
mero-also
mero-attr
mero-glos
mero-holo
mero-hype
mero-hypo
mero-mero
mero-pert
mero-sim
pert-also
pert-attr
pert-glos
pert-holo
pert-hype
pert-hypo
pert-mero
pert-pert
pert-sim
sim-also
sim-attr
sim-glos
sim-holo
sim-hype
sim-hypo
sim-mero
sim-pert
sim-sim
syns-example
syns-glos
	*/
	private static String[] pairs = { 
		    "    :    ", "    :hype", "    :hypo", "    :mero", "    :holo",
			"hype:    ", "hype:hype", "hype:hypo", "hype:mero", "hype:holo",
			"hypo:    ", "hypo:hype", "hypo:hypo", "hypo:mero", "hypo:holo",
			"mero:    ", "mero:hype", "mero:hypo", "mero:mero", "mero:holo",
			"syns:    ", "syns:hype", "syns:hypo", "syns:mero", "syns:holo"};
	
	private ILexicalDatabase db;
	
	public GlossFinder( ILexicalDatabase db ) {
		this.db = db;
	}
	
	/* Discussion: should we make it non-static?
	 */
	
	public List<SuperGloss> getSuperGlosses( Concept synset1, Concept synset2 ) {
		
		List<SuperGloss> glosses = new ArrayList<SuperGloss>( pairs.length );
		
		for ( String pair : pairs ) {
			String[] links = pair.split(":");
			SuperGloss sg = new SuperGloss();
			sg.gloss1 = (List<String>)db.getGloss( synset1, links[0] );
			sg.gloss2 = (List<String>)db.getGloss( synset2, links[1] );
			sg.link1  = links[0];
			sg.link2  = links[1];
			sg.weight = 1D;
			glosses.add( sg );
		}
		
//		for ( Link link : Link.values() ) {
//			Map<String,Integer> seth = new HashMap<String,Integer>();
//			List<String> arguments = new ArrayList<String>();//TODO:
//			seth.put( wps , 1 );
//			//for (  ) {//TODO
//			//	arguments;//TODO 
//			//}
//			glosses.add( arguments.get(0) );
//		}
		return glosses;
	}
				
	public static class SuperGloss {
		public List<String> gloss1;
		public List<String> gloss2;
		public String link1;
		public String link2;		
		public double weight;
	}	
	
}

