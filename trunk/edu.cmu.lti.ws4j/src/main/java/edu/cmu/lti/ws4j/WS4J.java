package edu.cmu.lti.ws4j;

import edu.cmu.lti.jawjaw.JAWJAW;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.MatrixCalculator;

/**
 * Java Wrapper for Japanese WordNet.
 * 
 * This is a facade class that provides simple APIs for end users.
 * For doing more complicated stuff, use DAO classes under the package edu.cmu.lti.jawjaw.dao
 * 
 * @author Hideki Shima
 *
 */
public class WS4J extends JAWJAW {
	private static ILexicalDatabase db = new NictWordNet();
	// similarity calculators
	private static RelatednessCalculator lin = new Lin(db);
	private static RelatednessCalculator wup = new WuPalmer(db);
	private static RelatednessCalculator hso = new HirstStOnge(db);
	private static RelatednessCalculator lch = new LeacockChodorow(db);
	private static RelatednessCalculator jcn = new JiangConrath(db);
	private static RelatednessCalculator lesk = new Lesk(db);
	private static RelatednessCalculator path = new Path(db);
	private static RelatednessCalculator res = new Resnik(db);
	
	/**
	 * Calculates the Hirst–St-Onge relatedness score between two synsets. 
	 * Following definition is cited from (Budanitsky & Hirst, 2001).
	 * <blockquote>
	 * Hirst–St-Onge: The idea behind Hirst and St-Onge’s 
	 * (1998) measure of semantic relatedness is that two lexicalized 
	 * concepts are semantically close if their WordNet
	 * synsets are connected by a path that is not too long and
	 * that “does not change direction too often”. The strength
	 * of the relationship is given by:
	 * <div style="padding:20px"><code>rel<sub>HS</sub>(c<sub>1</sub>, c<sub>2</sub>) = C - path_length - k * d.</code></div>
	 * where d is the number of changes of direction in the
	 * path, and C and k are constants; if no such path exists,
	 * rel_HS(c1, c2) is zero and the synsets are deemed unrelated.
	 * </blockquote>
	 * 
	 * From WS:
	 * Unless a problem occurs, the return value is the relatedness
	 * score, which is greater-than or equal-to 0 and less-than or equal-to 16.
	 * If an error occurs, then the error level is set to non-zero and an error
	 * string is created (see the description of getError()).
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runHSO( String word1, String word2 ) {
		return hso.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Calculates the Leacock–Chodorow similarity score between two synsets. 
	 * Following definition is cited from (Budanitsky & Hirst, 2001).
	 * <blockquote>
	 * Leacock–Chodorow: Leacock and Chodorow (1998) 
	 * also rely on the length len(c1; c2) of the shortest path between 
	 * two synsets for their measure of similarity. However, 
	 * they limit their attention to IS-A links and scale the 
	 * path length by the overall depth D of the taxonomy:
	 * <div style="padding:20px"><code>sim<sub>LC</sub>(c<sub>1</sub>, c<sub>2</sub>) = -log( len(c<sub>1</sub>, c<sub>2</sub>) / 2D ).</code></div>
	 * 
	 * </blockquote>
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runLCH( String word1, String word2 ) {
		return lch.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Calculates the Resnik's similarity score between two synsets.
	 * Following definition is cited from (Budanitsky & Hirst, 2001).
	 * <blockquote>
	 * Resnik: Resnik’s (1995) approach was, to our knowledge,
	 * the first to bring together ontology and corpus.
	 * Guided by the intuition that the similarity between a
	 * pair of concepts may be judged by “the extent to which
	 * they share information”,Resnik defined the similarity between
	 * two concepts lexicalized in WordNet to be the information
	 * content of their lowest super-ordinate (most
	 * specific common subsumer) lso(c1; c2):
	 * <div style="padding:20px"><code>sim<sub>R</sub>(c<sub>1</sub>, c<sub>2</sub>) = -log p(lso(c<sub>1</sub>, c<sub>2</sub>)).</code></div>
	 * where p(c) is the probability of encountering an instance
	 * of a synset c in some specific corpus.
	 * </blockquote>
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runRES( String word1, String word2 ) {
		return res.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Calculates the Jiang-Conrath distance score between two synsets.
	 * Following definition is cited from (Budanitsky & Hirst, 2001).
	 * <blockquote>
	 * Jiang–Conrath: Jiang and Conrath’s (1997) approach
	 * also uses the notion of information content, but in the
	 * form of the conditional probability of encountering an instance
	 * of a child-synset given an instance of a parent synset.
	 * Thus the information content of the two nodes, as
	 * well as that of their most specific subsumer, plays a part.
	 * Notice that this formula measures semantic distance, the
	 * inverse of similarity.
	 * <div style="padding:20px"><code>dist<sub>JS</sub>(c<sub>1</sub>, c<sub>2</sub>) = 2 * log( p(lso(c<sub>1</sub>, c<sub>2</sub>)) ) - ( log(p(c<sub>1</sub>))+log(p(c<sub>2</sub>) ) ).</code></div>
	 * </blockquote>
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runJCN( String word1, String word2 ) {
		return jcn.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Calculates the Lin's similarity score between two synsets.
	 * Following definition is cited from (Budanitsky & Hirst, 2001).
	 * <blockquote>
	 * Lin: Lin’s (1998) similarity measure follows from his
	 * theory of similarity between arbitrary objects. It uses the
	 * same elements as distJC, but in a different fashion:
	 * <div style="padding:20px"><code>sim<sub>L</sub>(c<sub>1</sub>, c<sub>2</sub>) = 2 * log p(lso(c<sub>1</sub>, c<sub>2</sub>)) / ( log p(c<sub>1</sub>) + log p(c<sub>2</sub>) ).</code></div>
	 * </blockquote>
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runLIN( String word1, String word2 ) {
		return lin.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Banerjee and Pedersen (2002) -- a method that adapts the Lesk approach to WordNet.  
	 * 
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runLESK( String word1, String word2 ) {
		return lesk.calcRelatednessOfWords( word1, word2 );
	}
	
	/**
	 * Computing semantic relatedness of word senses by counting 
	 * nodes in the noun and verb WordNet 'is-a' hierarchies.  
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runPATH( String word1, String word2 ) {
		return path.calcRelatednessOfWords( word1, word2 );
	}

	/**
	 * Computing semantic relatedness of word senses using 
	 * the edge counting method of the of Wu & Palmer (1994)
	 * @param word1 word lemma in Japanese or English
	 * @param word2 word lemma in Japanese or English
	 * @return semantic relatedness of two words
	 */
	public static double runWUP( String word1, String word2 ) {
		return wup.calcRelatednessOfWords( word1, word2 );
	}
				
	public static double[][] getSynonymyMatrix( String[] words1, String[] words2 ) {
		return MatrixCalculator.getSynonymyMatrix( words1, words2 );
	}
	
}
