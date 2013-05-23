package edu.cmu.lti.abstract_wordnet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LinkedSynsets extends LinkedHashMap<Link,List<Synset>> {

  private static final long serialVersionUID = 1L;
  
  public static final Link[] horizLinks = new Link[]{Link.also, Link.attr, Link.sim, 
          Link.ants, Link.pert};
  public static final Link[] upLinks = new Link[]{Link.hype, Link.inst,//hypes 
          Link.mmem, Link.msub,Link.mprt };//mero ;
  public static final Link[] downLinks = 
          new Link[]{Link.hmem, Link.hsub, Link.hprt,//holo 
          Link.hypo, Link.hasi, //hypos
          Link.caus, Link.enta, };//etc;
  public static final Link[] hypeLinks = new Link[]{Link.hype, Link.inst};
  public static final Link[] hypoLinks = new Link[]{Link.hypo, Link.hasi};
  public static final Link[] meroLinks = new Link[]{Link.mmem, Link.msub, Link.mprt};
  public static final Link[] holoLinks = new Link[]{Link.hmem, Link.hsub, Link.hprt};
  public static final Link[] domLinks  = new Link[]{Link.dmnc, Link.dmnu, Link.dmnr};
  public static final Link[] memLinks  = new Link[]{Link.dmtc, Link.dmtu, Link.dmtr};
  
  public LinkedSynsets( AbstractWordNet wn, Synset synset, List<Link> links ) {
    super(wn.getLinkedSynsetsAndWords( synset, links ));
  }

  public LinkedSynsets( Map<Link,List<Synset>> linkedSynsets ) {
    super(linkedSynsets);
  }
  
  /**
   * Given a synset id s, find synset(s) of  
   * direct horizontal term(s) of s.
   * 
   * Such terms are linked with relations such as: ants, attr, sim.
   * 
   * @param synsetId
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getHorizontals() {
    return getLinkedSynsetsAndWords( horizLinks );
  }
  
  /**
   * Given a synset id s, find synset(s) of  
   * direct upward term(s) of s.
   * 
   * Such terms are linked with relations such as: 
   * hype, mero, mmem, mprt, msub
   * 
   * Used by HSO algorithm.
   * 
   * @param synset
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getUpwards() {
    return getLinkedSynsetsAndWords( upLinks );
  }

  /**
   * Given a synset id s, find synset(s) of  
   * direct downward term(s) of s.
   * 
   * Such terms are linked with relations such as: 
   * caus, enta, holo, hmem, hsub, hprt, hypo.
   * 
   * Used by HSO algorithm.
   * 
   * @param synset
   * @return synsets of direct hypernym or empty collection if N/A
   */
  public List<Synset> getDownwards() {
    return getLinkedSynsetsAndWords( downLinks );
  }
  
  //FIXME Change name?
  public List<Synset> getHypes() {
    return getLinkedSynsetsAndWords( hypeLinks );
  }
  
  public List<Synset> getHypos() {
    return getLinkedSynsetsAndWords( hypoLinks );
  }
  
  public List<Synset> getAllMeronyms() {
    return getLinkedSynsetsAndWords( meroLinks );
  }
  
  public List<Synset> getAllHolonyms() {
    return getLinkedSynsetsAndWords( holoLinks );
  }
  
  public List<Synset> getAllDomain() {
    return getLinkedSynsetsAndWords( domLinks );
  }
  
  public List<Synset> getAllMemberOfDomain() {
    return getLinkedSynsetsAndWords( memLinks );
  }
  
  public List<Synset> getLinkedSynsetsAndWords( Link ... links ) {
    List<Synset> retval = new ArrayList<Synset>();
    for ( Link link : links ) {
      List<Synset> synsets = get(link);
      if (synsets!=null) {
        retval.addAll(synsets);
      }
    }
    return retval;
  }
}
