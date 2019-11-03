import java.io.IOException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Date;


import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.io.*;
import java.nio.charset.Charset;
    
public class TwitterAutoBot {

    public static void main(String[] args) {
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


        tweetLines();
    }
    private static void tweetLines() {
        String line;
        try {
            //try (
            //InputStream fis = new FileInputStream("C:\\Users\\Satya\\IdeaProjects\\TwitterAutoBot\\src\\main\\resources\\tweets.txt");
            //InputStreamReader isr = new InputStreamReader(fis, Charset.forName("Cp1252"));
            //BufferedReader br = new BufferedReader(isr);
            //) {
            //while ((line = br.readLine()) != null) {

            // Deal with the line

            line = "Current number of trees planted: " + totalTreeCount + ".\nPercentage Complete: " + percent + ".\nPredicted Completion Date: " + completeDate + ".\n Chart link in bio.";
            System.out.println("Tweeting: " + line + "...");
            sendTweet(line);
            try {
                System.out.println("Sleeping for 30 minutes...");
                Thread.sleep(1800000); // every 30 minutes
                // Thread.sleep(10000); // every 10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //}
            //}
        } catch (IOException e) {
            e.printStackTrace();
        }

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

}