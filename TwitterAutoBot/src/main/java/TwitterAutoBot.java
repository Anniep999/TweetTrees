import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.*;
import java.nio.charset.Charset;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCursor;
import com.mongodb.DB;
import com.mongodb.Mongo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TwitterAutoBot {

    public static void main(String[] args) {
    	int count = 10;
    	
    	while(true) {
    		String totalTreeCount = "";
            try {
                org.jsoup.nodes.Document websiteCode = Jsoup.connect("https://teamtrees.org").get();
                Elements totalTreesClass = websiteCode.select("div#totalTrees");
                totalTreeCount = totalTreesClass.select("div.counter").attr("data-count");
                System.out.println(totalTreeCount);
            } catch (IOException e) {
                System.out.println("Website not found");
            }
            
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM HH:mm:ss");
            Date date = new Date();
            System.out.println(dateFormatter.format(date));
            MongoClientURI uri = new MongoClientURI(
                    "mongodb+srv://tweettreesuser:Welovetrees123@tweettrees-lrnic.azure.mongodb.net/test?retryWrites=true&w=majority");
            MongoClient mongoClient = new MongoClient(uri);
            MongoDatabase database = mongoClient.getDatabase("TweetTreesDatabase");
            MongoCollection<org.bson.Document> collection2 = database.getCollection("Time-Amount");
            // org.bson.Document doc = new Document(totalTreeCount, "hello");
            org.bson.Document doc = new org.bson.Document("Total Trees", totalTreeCount).append("Date", date);
            // dateFormatter.format(date).toString()
            System.out.println("Printing collection items");
            collection2.insertOne(doc);
            System.out.println("!!!!!!!");
            
            
            if(count == 10) {
            	count = 0;
            	String prediction = getPrediction(collection2);
            	System.out.println("Thirty minutes have passed: Tweeting...");
            	tweetLines(Integer.parseInt(totalTreeCount), prediction);
            }
            count ++;
            try {
                System.out.println("Sleeping for 1 minute...");
                Thread.sleep(60000); // every 1 minute
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    	}
    	
    }

    private static void tweetLines(int totalTreeCount, String prediction) {
        String line;
        Double percentNum = ((double)totalTreeCount / (double)20000000) * (double)100;
    	String percent = String.format("%.2f", percentNum);
    	
    	
    	
    	line = "Current number of trees planted: " + totalTreeCount + ".\nPercentage Complete: " + percent + "%\nPredicted Completion Date: " + prediction + "\nChart link in bio.";
        System.out.println("Tweeting: " + line + "...");
        sendTweet(line);
        
    }

    private static void sendTweet(String line) {
        Twitter twitter = TwitterFactory.getSingleton();
        Status status;
        try {
            status = twitter.updateStatus(line);
            System.out.println(status);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }
    
    private static String getPrediction(MongoCollection<org.bson.Document> collection)
    {
    	String predictionDate = "";
    	int size = 0;
    	double[] money = new double[50];
    	double[] time = new double[50];
    	
    	MongoCursor<org.bson.Document> cursor = collection.find().iterator();
    	MongoCursor<org.bson.Document> cursor2 = collection.find().iterator();
    	
    	try
    	{
    		while(cursor.hasNext())
    		{
    			size++;
    			cursor.next();
    		}
    	} 
    	finally
    	{
    		cursor.close();
    	}
    	
    	for(int i = 0; i < (size - 50); i++)
    	{
    		cursor2.next();
    	}
    	
    	int j = 0;
    	while(cursor2.hasNext())
    	{
    		String output = cursor2.next().toString();
    		int moneyIndex = output.indexOf("Trees=") + 6;
    		int moneyAmnt = Integer.parseInt(output.substring(moneyIndex, moneyIndex + 8));
    		money[j] = moneyAmnt;
    		
    		int monthIndex = output.indexOf("Date=") + 9;
    		int secondsSinceBeginning;
    		if(output.substring(monthIndex, monthIndex + 1) == "N")
    		{
    			secondsSinceBeginning = 604800;
    		}
    		else
    		{
    			secondsSinceBeginning = 3196800;
    		}
    		
    		int date = Integer.parseInt(output.substring(monthIndex + 4, monthIndex + 6));
    		int hour = Integer.parseInt(output.substring(monthIndex + 7, monthIndex + 9));
    		int minutes = Integer.parseInt(output.substring(monthIndex + 10, monthIndex + 12));
    		int seconds = Integer.parseInt(output.substring(monthIndex + 13, monthIndex + 15));
    		
    		secondsSinceBeginning += date * 86400;
    		secondsSinceBeginning += hour * 3600;
    		secondsSinceBeginning += minutes * 60;
    		secondsSinceBeginning += seconds;
    		
    		time[j] = secondsSinceBeginning;
    		j++;
    		
    	}
    	
    	LinearRegression lr = new LinearRegression(money, time);
    	
    	double prediction = lr.predict(20000000.0);
    	
    	System.out.println(prediction);
    	
    	if(prediction < 5875200)
    	{
    	
	    	if(prediction >= 3196800)
	    	{
	    		predictionDate += "December ";
	    		prediction -= 3196800;
	    	}
	    	else
	    	{
	    		predictionDate += "November ";
	    		prediction -= 604800;
	    	}
	    	
	    	int numDays = ((int) prediction) / 86400;
	    	prediction -= numDays * 86400;
	    	
	    	System.out.println(numDays);
	    	
	    	int numHours = ((int) prediction) / 3600;
	    	prediction -= numHours * 3600;
	    	
	    	System.out.println(numHours);
	    	
	    	int numMinutes = ((int) prediction) / 60;
	    	prediction -= numMinutes * 60;
	    	
	    	System.out.println(numMinutes);
	    	
	    	int numSeconds = (int) prediction;
	    	
	    	System.out.println(numSeconds);
	    	
	    	predictionDate = predictionDate + numDays + " at " + numHours + ":" + numMinutes + ":" + numSeconds + " KEEP DONATING!";
   
    	}
    	else
    	{
    		predictionDate = "It will not happen by January 2020. DONATE!!!";
    	}
    	return predictionDate;
    }

}