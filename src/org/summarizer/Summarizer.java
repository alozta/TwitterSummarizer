package org.summarizer;

import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class Summarizer {

	public static void main(String[] args) throws UnknownHostException, IOException{
		final int PRIME = 94418953;
        final int SIZE = 10000;
        final PrintWriter pw = new PrintWriter(new FileWriter(new File("result.txt"), false));
        final Hashtable<Integer, String> myTable = new Hashtable<Integer, String>(PRIME, (float) 0.7); //prime number, occupancy
        final Hashtable<Integer, Integer> frequency = new Hashtable<Integer, Integer>(PRIME, (float) 0.7);
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          //ENTER YOUR KEYS
          .setOAuthConsumerKey("
          .setOAuthConsumerSecret("")
          .setOAuthAccessToken("")
          .setOAuthAccessTokenSecret("");
        
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        StatusListener listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                if(status.getLang().equals("en")){
                    int tempHash;
                    String tokens[] = status.getText().split("[ ,\"_\\?[0-9]]|(https?://.*)");
                
                    for(int i=0; i<tokens.length; ++i){
                        tokens[i] = tokens[i].replaceAll("((\\A[_\\W]*)|([_\\W]*\\z))","");
                        
                        if((!isMention(tokens[i]))){
                            try {
                                BufferedReader stopWords = new BufferedReader(new FileReader("stopWords.txt"));
                                String line = null;
                                if(!isStopWord(tokens[i].toLowerCase(), line, stopWords)){
                                //if not a stop word
                                    //convert hash code to acceptable number
                                    if(tokens[i].hashCode() > PRIME)
                                        tempHash = modHash(tokens[i].hashCode());
                                    else if(tokens[i].hashCode() < 0){
                                        if(tokens[i].hashCode() > PRIME*-1)
                                            tempHash = tokens[i].hashCode() * -1;
                                        else
                                            tempHash = modHash(tokens[i].hashCode()*-1);
                                    }
                                    else
                                        tempHash = tokens[i].hashCode();
                                
                                    //add word or update frequency
                                    if(myTable.put(tempHash, tokens[i].toLowerCase()) != null){
                                        //if a word exist in hash increase frequency by 1
                                        frequency.put(tempHash, (int)frequency.get(tempHash) +1);
                                    } else{
                                        //if a word isn't in hash puts it, starting frequency is 1 with taking same hash code
                                        frequency.put(tempHash, 1);
                                    }
                                }
                            } catch (FileNotFoundException e) {
                            	e.printStackTrace();
                            } catch (IOException e) {
                            	e.printStackTrace();
                            }
                            System.out.println(myTable.size() + " out of " + SIZE);
                        }
                    }
                    if(myTable.size() > SIZE){
                        bubbleSort(myTable, frequency);
                        System.exit(0);
                    }
                }
            }
            
            private void bubbleSort(Hashtable<Integer, String> myTable, Hashtable<Integer, Integer> frequency) {
                Information info[] = new Information[frequency.size()];
                Enumeration<String> word = myTable.elements();
                Enumeration<Integer> freq = frequency.elements();
                int i=0;
                //put data to information class array
                while((word.hasMoreElements()) && (freq.hasMoreElements())){
                    info[i] = new Information((String)word.nextElement(), (int)freq.nextElement());
                    ++i;
                }
                //bubble sort
                Information temp;
                for(int j=0; j<SIZE; ++j){
                    for(int k=0; k<SIZE; ++k){
                        if(info[k].frequency < info[k+1].frequency){
                            temp = info[k+1];
                            info[k+1] = info[k];
                            info[k] = temp;
                        }
                    }
                }
                //print to file
                for(int j=0; j<info.length; ++j){
                    pw.println(info[j].toString());
                }
            }
            
            private boolean isMention(String s){
                if((s.length() > 0) && (s.charAt(0) == '@'))
                    return true;
                else
                    return false;
            }
            
            private int modHash(int number){
                return number % PRIME;
            }
            
            private boolean isStopWord(String word, String line, BufferedReader stopWords) throws IOException{
                while((line = stopWords.readLine()) != null){
                    if(word.equals(line))
                        return true;
                }
                return false;
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
        twitterStream.addListener(listener);
        twitterStream.sample();
	}

}
