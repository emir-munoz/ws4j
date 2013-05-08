package edu.cmu.lti.ram_wordnet;

import edu.cmu.lti.abstract_wordnet.Link;

public class LinkedWords {
  private int[] word;
  private Link[] link;
  private int[][] linkedSynsets;
  private int[][] linkedWords;
  public LinkedWords(int[] word, Link[] link, int[][] linkedSynsets, int[][] linkedWords) {
    this.word = word;
    this.link = link;
    this.linkedSynsets = linkedSynsets;
    this.linkedWords = linkedWords;
  }
  public int[] getWord() {
    return word;
  }
  public Link[] getLink() {
    return link;
  }
  public int[][] getLinkedSynsets() {
    return linkedSynsets;
  }
  public int[][] getLinkedWords() {
    return linkedWords;
  }
  public int size() {
    return word.length;
  }
}