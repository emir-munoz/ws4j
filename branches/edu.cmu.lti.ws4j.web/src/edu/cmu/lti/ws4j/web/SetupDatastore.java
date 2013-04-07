package edu.cmu.lti.ws4j.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


@SuppressWarnings("serial")
public class SetupDatastore extends HttpServlet {

  private final static String COLUMN1 = "synsets";
  private final static String CLASS = "hypernym";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) 
          throws IOException {
    resp.setContentType("text/plain");
    resp.getWriter().println("Hello, world");
    resp.getWriter().println(deleteAll());
//    int count = store();
//    resp.getWriter().println("Stored "+count+" items.");
    resp.getWriter().println(retrieve());
  }
  
  public String deleteAll() {
    long t0 = System.currentTimeMillis();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(COLUMN1);
    PreparedQuery pq = datastore.prepare(query);
    for (Entity entity : pq.asIterable()) {
      datastore.delete(entity.getKey());
    }
    long t1 = System.currentTimeMillis();
    return "Deleted all data in "+(t1-t0)+" msec.";
  }
  
  public int store() {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//    Transaction txn = datastore.beginTransaction();
    int count = 0;
    try {
      InputStreamReader isr = new InputStreamReader(
              getClass().getResourceAsStream("/dump-hypernyms.txt"), "utf-8");
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ( (line = br.readLine())!=null ) {
        String[] items = line.split("<>");
        if (items.length!=2) continue;
        ++count;
        Key key = KeyFactory.createKey(CLASS, items[0]);
        Entity entity = new Entity(key);
        entity.setProperty(COLUMN1, items[1]);
        datastore.put(entity);
//        txn.commit();
      }
      br.close();
      isr.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
//      if (txn.isActive()) {
//        txn.rollback();
//      }
    }
    return count;
  }

  public void storeDemo() {
    // Two (out of five) ways to create Entity object:
    Entity alice = new Entity(CLASS, "Alice");
    alice.setProperty("gender", "female");
    
    alice.setProperty("age", 20);

    Key bobKey = KeyFactory.createKey(CLASS, "Bob");
    Entity bob = new Entity(bobKey);
    bob.setProperty("gender", "male");
    bob.setProperty("age", "23");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(alice);
    datastore.put(bob);
  }

  private String retrieve() {
    Key testKey = KeyFactory.createKey(CLASS, "04420461-n");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity;

    StringBuilder sb = new StringBuilder();
    try {
      entity = datastore.get(testKey);

      String hypernymSynsetIds = entity.getProperty(COLUMN1).toString();
      sb.append("Result: " + hypernymSynsetIds+"\n");
    } catch (EntityNotFoundException e) {
      e.printStackTrace();
    }
    return sb.toString();
  }

}
