Thanks for downloading WS4J (http://code.google.com/p/ws4j).

---- Introduction ----
This software provides APIs for several semantic relatedness 
algorithms for, in theory, any WordNet instance. The codebase
has been mostly ported from WordNet-Similarity-2.05 
(http://wn-similarity.sourceforge.net/). We also use the data 
files from WordNet-Similarity-2.05 and WordNet-InfoContent-3.0, 
as seen in src/main/resources. 

We tested WS4J with the JAWJAW on NICT Japanese 
WordNet (http://nlpwww.nict.go.jp/wn-ja/index.en.html) 
with which you can analyze English and Japanese (in 
Princeton WordNet 3.0 compatible synsets).

---- Preparation ----
By default, requirement for compilation are:
 - JDK 5+ 
 - Maven 2 (or "m2e" plugin on eclipse)
 - JAWJAW (Java Wrapper for NICT Japanese/English WordNet; http://code.google.com/p/jawjaw).

It's NORMAL that you see a build error in eclipse as JAWJAW is 
not contained in lib/ for the source code distribution.
Before using WS4J, compile and package JAWJAW and put the
jar file under the lib directory in this project: ./lib/jawjaw.jar
Until you do this, WS4J does not compile.

When packaging JAWJAW, you may want to consider enabling
on-memory DB mode which is disabled by default.

In case you want to use another WordNet API + instance, 
implement a WordNet wrapper following the real example for NICT wordnet + JAWJAW 
in edu.cmu.lti.lexical_db.NictWordNet

---- Testing ----
You can verify that the preparation is correctly done by running
JUnit test cases. 

Test cases:
  src/test/*
   
Maven command:
  mvn test

Launch file for Eclipse + m2e:
  launches/WS4J_Run_All_JUnitTests.launch

The expected results from the test cases are compatible with the 
original WordNet::Similarity in Perl (http://wn-similarity.sourceforge.net/).

---- Packaging ----
To customize WS4J, edit src/main/config/similarity.conf.

Here's a way to create a jar file including resource and config files.

Maven command:
  mvn install

Launch file for Eclipse + m2e:
  launches/WS4J_package_m2e.launch

Output jar file (may need a refresh on the directory):
  target/ws4j.jar
  
---- Using WS4J ----
See working examples in the following files.

Demos:
  src/main/java/edu/cmu/lti/ws4j/demo/SimilarityCalculationDemo.java

When using the WS4J jar package from other projects, make sure to 
also include depending libraries, i.e. junit, sqlite-jdbc, jawjaw.
In maven's pom file, these dependencies can be written such as: 

  <dependencies>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.0</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>org.xerial</groupId>
      <artifactId>sqlite-jdbc</artifactId>
      <version>3.7.2</version>
    </dependency>
    <dependency>
      <groupId>edu.cmu.lti</groupId>
      <artifactId>jawjaw</artifactId>
      <version>1.0.0</version>
      <scope>system</scope> 
      <systemPath>${basedir}/lib/jawjaw.jar</systemPath>
    </dependency>
    <dependency>
      <groupId>edu.cmu.lti</groupId>
      <artifactId>ws4j</artifactId>
      <version>1.0.0</version>
      <scope>system</scope> 
      <systemPath>${basedir}/lib/ws4j.jar</systemPath>
    </dependency>
  </dependencies> 
 
