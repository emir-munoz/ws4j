/*
 * Copyright 2013 Carnegie Mellon University
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Responsible for creating WordNet instance.
 * An instance is created using reflection given a class name 
 * that extends edu.cmu.lti.abstract_wordnet.AbstractWordNet.
 * Like singleton, an instance is created only once for each 
 * class. Initialization is done in a on demand, lazy fashion as
 * this class can work with any child class of AbstractWordNet.
 * 
 * @author Hideki Shima
 *
 */
public class WordNetFactory {

  private static ConcurrentMap<String,AbstractWordNet> instances 
    = new ConcurrentHashMap<String, AbstractWordNet>();

  /**
   * 
   * @param wnClassName a package and class name of a class 
   * that has edu.cmu.lti.abstract_wordnet.AbstractWordNet as a super class, 
   * such as "edu.cmu.lti.ram_wordnet.OnMemoryWordNetAPI".
   * @return the AbstractWordNet object associated with the class with the given string name. 
   */
  public synchronized static AbstractWordNet getCachedInstanceForName( String wnClassName ) {
    AbstractWordNet wn = instances.get(wnClassName);
    if (wn==null) { //lazy init
      wn = create( wnClassName );
      instances.put(wnClassName, wn);
    }
    return wn;
  }
  
  private synchronized static AbstractWordNet create( String wnClassName ) {
    Class<AbstractWordNet> clazz = getClassForName(wnClassName);
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  
  @SuppressWarnings("unchecked")
  private static <T> Class<T> getClassForName(String className) {
    try {
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
