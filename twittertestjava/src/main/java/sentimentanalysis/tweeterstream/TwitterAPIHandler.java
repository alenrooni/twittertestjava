
package sentimentanalysis.tweeterstream;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import scala.Option;
import scala.collection.Iterator;
import scala.util.parsing.json.JSONObject;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class TwitterAPIHandler {
	private static List<String> languages = Arrays.asList("en");

  public static void twitterStream(String consumerKey, String consumerSecret, String token, String secret) throws InterruptedException {
    BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
    StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
    // add some track terms
    ArrayList<String> terms = Lists.newArrayList("perseverance", "perseverance");
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
  public static void searchTwitter(String consumerKey, String consumerSecret, String token, String secret, List<String> terms) throws FileNotFoundException, UnsupportedEncodingException, InterruptedException{
	  //output file
	  PrintWriter printer = new PrintWriter("tweets.txt", "UTF-8");
	  Twitter twitter = new TwitterFactory().getInstance();
	  AccessToken accessToken = new AccessToken(token, secret);
	  twitter.setOAuthConsumer(consumerKey, consumerSecret);
	  twitter.setOAuthAccessToken(accessToken);
	  for(int index = 0; index < terms.size(); index++){
		  String term = terms.get(index);
		  int i = 0;
		  try {
		      Query query = new Query(term);
		      QueryResult result;
		      query.setCount(100);
		      query.setLang("en");
		      while(query != null){
		    	  Thread.sleep(6000);
		    	  result = twitter.search(query);
		    	  List<Status> tweets = result.getTweets();
		    	  for (Status tweet : tweets) {
		    		  //System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
		    		  printer.write(tweet.getText()+"\n");
		    		  i++;
		    	  }
		    	  query = result.nextQuery();
		      }
		      System.out.println("fetched " + i + " tweets for term = " + term);
		  }
		  
		  catch (TwitterException te) {
		      te.printStackTrace();
		      System.out.println("Failed to search tweets: " + te.getMessage());
		      if(te.getErrorCode() == 88){
		    	  Thread.sleep(900000);
		    	  index--;
		      }
		  }
		  printer.write("\n");
	  }
	  printer.close();
  }
  public static void main(String [] args) throws InterruptedException, IOException{
	  String consumer_key = "o9ltlEXN4wEXlHq4Jxiig";
	  String consumer_secret = "XA63EAvDQ8iNvbQMnEa0YpbiqArEN8QqQBuyUAFsY";
	  String token = "2205031009-AOJm8dD1kNogHy6Sg9QMhWuc4mQwvzxoAsCYNMk";
	  String secret = "peCZKgAZuMjwvCYCWbuJcmRDBF4DLblecbQYS484RkfW5";
	  //TwitterAPIHandler.twitterStream(consumer_key, consumer_secret, token, secret);	
	  String queryTermsFile = "/home/af/Documents/toeflwords.txt";
	  List<String> queryTerms = new ArrayList<String>();
	  File file = new File(queryTermsFile);
	  FileReader fileReader = new FileReader(file);
	  BufferedReader bufferedReader = new BufferedReader(fileReader);
	  String line;
	  while ((line = bufferedReader.readLine()) != null) {
		  queryTerms.add(line);
	  }
	  fileReader.close();
	  TwitterAPIHandler.searchTwitter(consumer_key, consumer_secret, token, secret, queryTerms);	
  }
}

