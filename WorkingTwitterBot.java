import net.aksingh.owmjapis.api.APIException;
import net.aksingh.owmjapis.core.OWM;
import net.aksingh.owmjapis.model.CurrentWeather;
import net.aksingh.owmjapis.model.param.Wind;
import twitter4j.TwitterException;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WorkingTwitterBot {

    private static String greetingTime = "";
    private static PrintStream consolePrint;

    public static void main(String[] args)
            throws APIException, TwitterException, IOException{

        Timer timer = new Timer();


        OWM owm = new OWM("removed for privacy");

        // getting current weather data for Marysville wa
        CurrentWeather cwd = owm.currentWeatherByCityId(5802570);
        // checking data retrieval was successful or not
        if (cwd.hasRespCode() && cwd.getRespCode() == 200) {

            if (cwd.hasCityName()) {
                //printing city name from the retrieved data
                System.out.println("City: " + cwd.getCityName());

            }

            //todo remove time reference the API is always off with minutes API if read from multiple times is off, if just 1 call seems to work
            getTimeOfDay(cwd); // being lazy since greetingTime is set by this method only right now
            System.out.println(runBotText(cwd));

            Twitterer bigBird = new Twitterer(consolePrint);

            TimerTask tt = new TimerTask() {
                public void run() {
                    Calendar cal = Calendar.getInstance();
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    System.out.println("hour is: " + hour);
                    if(hour >= 10) {
                        System.out.println("Doing the selected task ");
                        try {
                            bigBird.tweetOut(runBotText(cwd));
                        } catch (TwitterException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            timer.schedule(tt,1000, (long)(7.2e6));

            }
        }


    public static String runBotText(CurrentWeather cwd) {
        getTimeOfDay(cwd); // being lazy since greetingTime is set by this method only right now
        String message = "";

        message += openingText(getGreetingTime(),cwd) + "\r\n";
        message += getTimeOfDay(cwd) + "\r\n";
        message += getTemperatureReadout(cwd) + "\r\n";
        message += getWeatherReadout(cwd) + "\r\n";
        message += getCurrentWindReadout(cwd) + "\r\n";
        message += boilerplate() + "\r\n";
        return message;

    }

    public static String boilerplate() {
        return"This is a personal coding project, if you see bugs please message me";
    }

    public static String openingText(String time,CurrentWeather cw) {
        String timeOfDay = time;
        return "Good " + timeOfDay +" #" + cw.getCityName() +" #Wa";
    }

    public static void setGreetingTime(double time) {
        if(time > 20 || time < 6) {
            greetingTime = "Night";
        }
        if(time > 6 && time < 12) {
            greetingTime = "Morning";
        }
        if(time > 12 && time <17) {
            greetingTime = "Afternoon";
        }
        if(time > 17 && time < 20) {
            greetingTime = "Evening";
        } else {
            greetingTime = "Day";
        }
}

    public static String getGreetingTime() {
        return greetingTime;
    }

    /**
     * method outputs the current wind speed and direction as a string
     * @param cw
     * @return
     */
    public static String getCurrentWindReadout(CurrentWeather cw) {
        Wind windData = cw.getWindData();
        Double windDegree;
        Double windGust;
        Double windSpeed;
        String out;

        if (windData != null) {
            if(windData.hasDegree() && windData.hasGust() && windData.hasSpeed() ) {
                windDegree = windData.getDegree();
                windGust = windData.getGust();
                windSpeed = windData.getSpeed();
            } else if(windData.hasDegree() && windData.hasSpeed()){
                windDegree = windData.getDegree();
                windSpeed = windData.getSpeed();
            } else {
                System.out.println("error fetching in windData");
                return "";
            }
        } else {
            System.out.println("error fetching windData");
            return "";
        }
        windSpeed *= 2.237;
        DecimalFormat df = new DecimalFormat("#.##");

        return "Wind spd:" +df.format(windSpeed)+ " MPH, Wind dir:" + degreeCompass(windSpeed);
    }

    /**
     * method gives out the current weather data for the day as a string
     * @param cw
     * @return
     */
    public static String getWeatherReadout(CurrentWeather cw) {

       List weatherList = cw.getWeatherList();
       String weather = weatherList.get(0).toString();
       String[] split = weather.split(" ");
       String[] splitEqual = split[1].split("=");
       int length = splitEqual[1].length();
       String weatherData = splitEqual[1].substring(0,length-1);

            return "Today's weather:" + weatherData;
        }
    public static String getTemperatureReadout(CurrentWeather cw) {
            double currentTemp;
            double highTemp;
            double lowTemp;
        try {
            currentTemp = cw.getMainData().getTemp();
            currentTemp = kelvinConverter(currentTemp);
        }catch (NullPointerException e) {
            System.out.println("error in getting current temp");
            currentTemp = -1.0;
        }
            try {
                highTemp = cw.getMainData().getTempMax();
                highTemp = kelvinConverter(highTemp);
            }catch (NullPointerException e) {
                System.out.println("error in getting max temp");
               highTemp = -1.0;
            }
            try {
                lowTemp = cw.getMainData().getTempMin();
                lowTemp = kelvinConverter(lowTemp);
            }catch (NullPointerException e) {
                System.out.println("error in getting min temp");
                lowTemp = -1.0;
            }



        return "Current temp:" + (int)currentTemp + "°F, " + "high and low temps today: High:" + (int)highTemp + "°F,low of:" + (int)lowTemp + "°F";
        }

    /**
     * method converts a input of degrees into the cardinal directions of a compass
     * @param in double input as degrees
     * @return the direction on a compass the degrees matched with as a String
     */
    public static String degreeCompass(double in) {
         String[] compass =  {"N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW","N"};
         double moduloConvert = in % 360;
         double arrayIndex = (moduloConvert/22.5);
         return compass[(int)arrayIndex];
     }

    /**
     * converts kelvin to Fahrenheit
     * @param in kelvins
     * @return F
     */
    public static double kelvinConverter(double in) {

            double out = in-273.15;
            out = (out *9/5) +32;
            return out;


        }


    /**
     * method formats the CW class date data into a more readable format for laymen, converts date,day and time to common format
     * @param cw current weather class for specific city
     * @return string formatted for date and time
     */

    public static String getTimeOfDay(CurrentWeather cw) {
            String fullData = cw.getDateTime().toString();
            String[] split = fullData.split(" ");
            String time = milTimeConvert(split[3]);
            String out = "Time:" + time + " " + dayConvert(split[0]) + " "
                    + monthConvert(split[1]) + "/" + split[2] + "/" + split[5];
            String[] splitMil = split[3].split(":");
            Double hour = Double.parseDouble(splitMil[0]);
            setGreetingTime(hour);
            return out;

        }

    /**
     * API holds time in military time, and is off by around 4-ish minutes this adjusts and formats to a 12 hour clock
     * @param in formated String from CurrentWeather getter
     * @return formatted date and time string
     */
    public static String milTimeConvert(String in) {
        boolean AM = true;
            String[] split = in.split(":");
            int hour = Integer.parseInt(split[0]);
            int min = Integer.parseInt(split[1]);
            min+=0; //bug:time in muinutes is off randomly
            if(hour > 12) {
                hour -= 12;
               return hour + ":" + min+ " PM";
            } else {
                return hour + ":" + min + " AM";
            }
    }

        public static String dayConvert(String in) {
            switch(in) {
                case "Sun":
                    return "Sunday";
                case "Mon":
                    return "Monday";
                case "Tue":
                    return "Tuesday";
                case "Wed":
                    return "Wednesday";
                case "Thu":
                    return "Thursday";
                case "Fri":
                    return "Friday";
                case "Sat":
                    return "Saturday";
                default:
                    return "Nonesday, if ya see this I messed up lol";
            }

        }


        public static int monthConvert(String in) {
        switch(in) {
            case "Jan":
                return 1;
            case "Feb":
                return 2;
            case "Mar":
                return 3;
            case "Apr":
                return 4;
            case "May":
                return 5;
            case "Jun":
                return 6;
            case "Jul":
                return 7;
            case "Aug":
                return 8;
            case "Sep":
                return 9;
            case "Oct":
                return 10;
            case "Nov":
                return 11;
            case "Dec":
                return 12;
            default:
                return 0;
            }
        }

    }
