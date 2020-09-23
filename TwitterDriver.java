/**
 * Twitter Driver and Client
 * 
 * @author Ria Galanos
 * @author Tony Potter
 * Original idea by Ria Galanos, whose documentation and source can be found at
 * https://github.com/riagalanos/cs1-twitter
 * 
 **/

import twitter4j.TwitterException;

import java.io.IOException;
import java.io.PrintStream;

public class TwitterDriver
{
   private static PrintStream consolePrint;

   public static void main (String []args) throws TwitterException, IOException
   {
      Twitterer bigBird = new Twitterer(consolePrint);

      String message = "I'm testing out the twitter4j API for Java.  Thanks @cscheerleader! "
                     + "You can find out more at https://github.com/riagalanos/cs1-twitter";
      //bigBird.tweetOut(message);
   }//main

}//class
         
   