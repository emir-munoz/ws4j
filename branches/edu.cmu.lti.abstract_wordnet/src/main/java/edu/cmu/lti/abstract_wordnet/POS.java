/*
 * Copyright 2009-2013 Carnegie Mellon University
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package edu.cmu.lti.abstract_wordnet;

/**
 * Enum class for the POS (Part of Speech) that is one of Noun, Verb, Adjective and Adverb.
 * 
 * @author Hideki Shima
 *
 */
public enum POS {
  a, // adjective
  r, // adverb
  n, // noun
  v; // verb
}
/* my %pos_map = ('noun'      => 'n',
         'n'         => 'n',
         '1'         => 'n',
         ''          => 'n',
         'verb'      => 'v',
         'v'         => 'v',
         '2'         => 'v',
         'adjective' => 'a',
         'adj'       => 'a',
         'a'         => 'a',
         # Adj satellite is essentially just an adjective
         's'         => 'a',
         '3'         => 'a',
         '5'         => 'a', # adj satellite
         'adverb'    => 'r',
         'adv'       => 'r',
         'r'         => 'r',
         '4'         => 'r');
*/