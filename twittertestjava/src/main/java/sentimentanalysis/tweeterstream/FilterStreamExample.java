
package sentimentanalysis.tweeterstream;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import scala.Option;
import scala.collection.Iterator;
import scala.util.parsing.json.JSONObject;

public class FilterStreamExample {
	private static List<String> languages = Arrays.asList("en");

  public static void oauth(String consumerKey, String consumerSecret, String token, String secret) throws InterruptedException {
    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
    StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
    // add some track terms
    ArrayList<String> terms = Lists.newArrayList("apple", "sumsung");
    //terms.add("iran");
    //terms.add("sweden");
    
    endpoint.trackTerms(terms);
    endpoint.languages(languages);

    Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
    // Authentication auth = new BasicAuth(username, password);

    // Create a new BasicClient. By default gzip is enabled.
    Client client = new ClientBuilder()
      .hosts(Constants.STREAM_HOST)
      .endpoint(endpoint)
      .authentication(auth)
      .processor(new StringDelimitedProcessor(queue))
      .build();

    // Establish a connection
    client.connect();

    // Do whatever needs to be done with messages
    for (int msgRead = 0; msgRead < 100; msgRead++) {
      String msg = queue.take();
      org.json.JSONObject json = new org.json.JSONObject(msg);
      String text = (String) json.get("text");
      
      System.out.println(text);
    }

    client.stop();

  }
  public static void main(String [] args) throws InterruptedException{
	  String consumer_key = "o9ltlEXN4wEXlHq4Jxiig";
	  String consumer_secret = "XA63EAvDQ8iNvbQMnEa0YpbiqArEN8QqQBuyUAFsY";
	  String token = "2205031009-AOJm8dD1kNogHy6Sg9QMhWuc4mQwvzxoAsCYNMk";
	  String secret = "peCZKgAZuMjwvCYCWbuJcmRDBF4DLblecbQYS484RkfW5";
	  FilterStreamExample.oauth(consumer_key, consumer_secret, token, secret);		
  }
}

