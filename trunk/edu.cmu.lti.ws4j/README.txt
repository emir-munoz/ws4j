Thanks for downloading WS4J ( http://code.google.com/p/ws4j ).

Requirement for compilation are:
JDK 5+, Maven 2 (or m2eclipse plugin on eclipse), JAWJAW.
The Jar version of WS4J only requires JDK 5+ for runtime.

This software provides APIs for several semantic relatedness 
algorithms for, in theory, any WordNet instance. The codebase
has been mostly ported from WordNet-Similarity-2.05 
( http://wn-similarity.sourceforge.net/ ). We also use data 
files from WordNet-Similarity-2.05 and WordNet-InfoContent-3.0, 
as seen in src/main/resources. 

We tested WS4J with the JAWJAW (Java Wrapper for Japanese 
WordNet; http://code.google.com/p/jawjaw ) on NICT Japanese 
WordNet ( http://nlpwww.nict.go.jp/wn-ja/index.en.html ) 
with which you can analyze English and Japanese (in 
Princeton WordNet 3.0 compatible synsets).

In case you would like to use another WordNet instance,
create another WordNet wrapper following the real example
edu.cmu.lti.lexical_db.NictWordNet.

To customize WS4J, edit src/main/config/similarity.conf.
If you are using JAWJAW, you may want to consider enabling
on-memory DB mode which is disabled by default.
