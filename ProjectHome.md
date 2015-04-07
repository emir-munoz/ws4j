# WS4J  #

**The developer (hideki.shima AT gmail.com) is extremely busy preparing for his PhD thesis defense, and cannot reply to any emails until Sep 2014.**

<a href='Hidden comment: 
'></a>

## Introduction ##

WS4J (WordNet Similarity for Java) provides a pure **Java API for several published [Semantic Relatedness/Similarity](http://en.wikipedia.org/wiki/Semantic_similarity) algorithms** listed in the table below. Download a jar file from the "Downloads" tab and you can immediately use WS4J on Princeton's English WordNet 3.0 & NICT's Japanese WordNet 0.9, from your Java program. The codebase is mostly a Java re-implementation of [WordNet-Similarity-2.05](http://wn-similarity.sourceforge.net/) (Perl), with some test cases for verifying the same logic. WS4J designed to be thread-safe.

## Demo (NEW!) ##

URL: http://ws4jdemo.appspot.com/

(Last updated: May 14, 2013)

Try out the latest WS4J from your web browser! The WS4J on the demo is the latest snapshot version which will be included in the [#Next\_release](#Next_release.md). The demo may temporarily be down when the instance hour of Google App Engine reaches the daily quota.

Note that the demo gives the correct debugged scores. We will release the code for demo most likely Fall 2014.

## Semantic Relatedness Metrics Available ##

| ID | Publication | Description |
|:---|:------------|:------------|
| [HSO](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/hso.pm) | [(Hirst & St-Onge, 1998)](http://scholar.google.com/scholar?q=Lexical+chains+as+representations+of+context+for+the+detection+and+correction+of+malapropisms) | Two lexicalized concepts are semantically close if their WordNet synsets are connected by a path that is not too long and that "does not change direction too often". |
| [LCH](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/lch.pm) | [(Leacock & Chodorow, 1998)](http://scholar.google.com/scholar?q=Combining+local+context+and+WordNet+similarity+for+word+sense+identification) | This measure relies on the length of the shortest path between two synsets for their measure of similarity. They limit their attention to IS-A links and scale the path length by the overall depth D of the taxonomy|
| [LESK](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/lesk.pm) | [(Banerjee & Pedersen, 2002)](http://scholar.google.com/scholar?q=An+Adapted+Lesk+Algorithm+for+Word+Sense+Disambiguation+Using+WordNet) | Lesk (1985) proposed that the relatedness of two words is proportional to to the extent of overlaps of their dictionary definitions. Banerjee and Pedersen (2002) extended this notion to use WordNet as the dictionary for the word definitions.|
| [WUP](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/wup.pm) | [(Wu & Palmer, 1994)](http://scholar.google.com/scholar?q=Verb+semantics+and+lexical+selection) | The Wu & Palmer measure calculates relatedness by considering the depths of the two synsets in the WordNet taxonomies, along with the depth of the LCS|
| [RES](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/res.pm) | [(Resnik, 1995)](http://scholar.google.com/scholar?q=Using+information+content+to+evaluate+semantic+similarity+in+a+taxonomy) | Resnik defined the similarity between two synsets to be the information content of their lowest super-ordinate (most specific common subsumer)|
| [JCN](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/jcn.pm) | [(Jiang & Conrath, 1997)](http://scholar.google.com/scholar?q=Semantic+similarity+based+on+corpus+statistics+and+lexical+taxonomy) | Also uses the notion of information content, but in the form of the conditional probability of encountering an instance of a child-synset given an instance of a parent synset: 1 / jcn\_distance, where jcn\_distance is equal to IC(synset1) + IC(synset2) - 2 `*` IC(lcs).|
| [LIN](http://search.cpan.org/dist/WordNet-Similarity/lib/WordNet/Similarity/lin.pm) | [(Lin, 1998)](http://scholar.google.com/scholar?q=An+information-theoretic+definition+of+similarity) | Math equation is modified a little bit from Jiang and Conrath: 2 `*` IC(lcs) / (IC(synset1) + IC(synset2)). Where IC(x) is the information content of x. One can observe, then, that the relatedness value will be greater-than or equal-to zero and less-than or equal-to one. |

The descriptions above are extracted either from each paper or from [WordNet-Similarity CPAN documentation](http://search.cpan.org/dist/WordNet-Similarity/).

## Distributions ##

(Note that neither of below work exactly in the same way as the version in the demo. The demo uses the unreleased latest snapshot version which is drastically different in software architecture. We are currently working hard toward [#Next\_release](#Next_release.md).)

  * **Off-the-shelf WS4J package** user just needs to download the jar file (class files archived in jar, with all dependent jar files and WordNet DB included) from the [Downloads](https://code.google.com/p/ws4j/downloads/list) link.

  * **WS4J source code** is viewable [from a web browser](https://code.google.com/p/ws4j/source/browse/#svn%2Ftrunk%2Fedu.cmu.lti.ws4j), or downloadable [by Subversion's checkout/export](http://code.google.com/p/ws4j/source/checkout) (with a subversion client software).

## Requirement ##

  * **Off-the-shelf WS4J package**
    * [JDK 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later

  * **Building WS4J from source**: You need the following additional software.
    * _(optional)_ [Eclipse](http://www.eclipse.org/downloads/) + [Subclipse](http://subclipse.tigris.org/)
    * [Maven 2](http://maven.apache.org/) or [m2e](http://eclipse.org/m2e/download/) plugin for eclipse
    * _(For Princeton's English WordNet 3.0 / NICT's Japanese WordNet 0.9)_ [JAWJAW](https://code.google.com/p/jawjaw/downloads/list) in your local maven repository, or under lib (i.e. lib/jawjaw.jar) with maven system-dependency.
    * _(For other WordNet DB)_ Any WordNet instance can be used in WS4J if it implements the interface [ILexicalDatabase](http://code.google.com/p/jawjaw/source/browse/trunk/edu.cmu.lti.jawjaw/src/main/java/edu/cmu/lti/lexical_db/ILexicalDatabase.java) in JAWJAW project. We used this approach to use a proprietary WordNet DB in WS4J.


## Instructions to run sample codes ##

First of all, run all [JUnit test cases](https://code.google.com/p/ws4j/source/browse/#svn%2Ftrunk%2Fedu.cmu.lti.ws4j%2Fsrc%2Ftest%2Fjava%2Fedu%2Fcmu%2Flti%2Fws4j%2Fimpl) to verify that you get the same result as the Perl version of WordNet::Similarity. This can be done by launching a file `./launches/WS4J_Run_All_JUnitTests.launch`

Then start playing with the facade API [edu.cmu.lti.ws4j.WS4J](https://code.google.com/p/ws4j/source/browse/trunk/edu.cmu.lti.ws4j/src/main/java/edu/cmu/lti/ws4j/WS4J.java) and a simple demo class [edu.cmu.lti.ws4j.demo.SimilarityCalculationDemo](https://code.google.com/p/ws4j/source/browse/trunk/edu.cmu.lti.ws4j/src/main/java/edu/cmu/lti/ws4j/demo/SimilarityCalculationDemo.java).

## Version History ##
  * **1.0.1 (2013-04-01)** - Incorporated JAWJAW 1.0.2 which initialization is drastically faster than before. Mainly due to the initialization speed-up which reads WordNet DB from jar without a temporary file extraction, running SimilarityCalculationDemo which used to take 2.6 sec is now 1.5 sec (Intel i5-3320M 2.60 GHz; SSD). The codebase is now compatible with m2e (older m2eclipse is deprecated).
  * **1.0.0 (2011-10-16)** - Initial release at Project Hosting on Google Code

## Coming up next ##
### Next release ###
  * Bug fixes in wup, hso and lesk where some new special test cases fail.
  * On-memory DB-less WordNet API (working in the demo; yes, it's portable enough to run on GAE!)
  * Test cases from WordNet::Similarity
  * Test with other English WordNet DB (original Princeton's WordNet 3.0 working in the demo version)
### Future release ###
  * Another measure: <a href='http://aclweb.org/anthology/D/D11/D11-1093.pdf'>Wang & Hirst, EMNLP 2011.</a>
  * Documentation - FAQ on each measure

## FAQ ##
  * What's the relationship between WS4J and [WordNet::Similarity](http://wn-similarity.sourceforge.net/)?
    * WordNet::Similarity is the original Perl implementation from Prof. Ted Pedersen's group in University of Minnesota in Duluth. In CMU, we reimplemented their code in Java. So don't ask WS4J-specific questions to them or vice versa. We also use their data files from WordNet-Similarity-2.05 and WordNet-InfoContent-3.0, as seen in src/main/resources. Our JUnit test cases check if the expected similarity scores from WordNet::Similarity are also obtained from WS4J.

  * Who maintains the code?
    * [Hideki Shima](http://www.cs.cmu.edu/~hideki) at Carnegie Mellon University (USA) wrote and currently maintains WS4J. Let him know if you have any questions, suggestions, or patches to contribute (sorry for slow responses).

  * By its design, WordNet has limited coverage on proper nouns. Is there any knowledge source that links proper nouns to WordNet synsets?
    * See [YAGO Project](http://www.mpi-inf.mpg.de/yago-naga/yago/index.html) by Max Planck Institut Informatik (Germany).

  * Is there any Java package for approximate string comparison operators, like Levenstein's edit distance, Jaccard, Jaro etc?
    * See CMU's [SecondString Project](http://secondstring.sourceforge.net/) led by Prof. William Cohen.

## Links ##

  * [WordNet::Similarity](http://wn-similarity.sourceforge.net/) - thanks to Prof. Ted Pedersen for releasing and maintaining such a wonderful software!
  * [JAWJAW](http://code.google.com/p/jawjaw) (Java Wrapper for Japanese WordNet)