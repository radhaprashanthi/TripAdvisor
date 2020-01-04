package hotelapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jettyServer.HotelBaseServlet;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class responsible for getting tourist attractions near each hotel from the Google Places API.
 *  Also scrapes some data about hotels from expedia html webpage.
 */
public class TouristAttractionFinder {

    private static final String host = "maps.googleapis.com";
    private static final String path = "/maps/api/place/textsearch/json";
    private static String myAPIKey;

    // Add instance variables as needed (for example, store a reference to ThreadSafeHotelData)
    private ThreadSafeHotelData hdata;
    // FILL IN CODE: add data structures to store attractions
    // Alternatively, you can store these data structures in ThreadSafeHotelData
    private ExecutorService exec;
    
    
    /** Constructor for TouristAttractionFinder
     *
     * @param hdata
     */
    public TouristAttractionFinder(ThreadSafeHotelData hdata) {
        // FILL IN CODE
        this.hdata = hdata;
        exec = Executors.newSingleThreadExecutor();
    }


    /**
     * Creates a secure socket to communicate with Google Places API server,
     * sends a GET request (to find attractions close to
     * the hotel within a given radius), and gets a response as a string.
     * Removes headers from the response string and parses the remaining json to
     * get Attractions info. Adds attractions to the corresponding data structure that supports
     * efficient search for tourist attractions given the hotel id.
     *
     * @return
     */
    public String fetchAttractions(String hotelID, int radiusInMiles) {
        // FILL IN CODE
        // This method should call getRequest method
        if (hotelID != null) {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = null;
            try {
                jsonObject = (JsonObject) jsonParser.parse(new FileReader("input/config.json"));
                myAPIKey = jsonObject.get("apikey").getAsString();
                if (myAPIKey.isEmpty()) {
                    throw new IllegalArgumentException("API key missing in config.json file");
                }
        
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (IllegalArgumentException e) {
                System.out.println(e);
            }
    
            PrintWriter out = null;
            BufferedReader in = null;
            SSLSocket socket = null;
            String response = "";
    
            try {
                HotelBaseServlet hbServlet = new HotelBaseServlet();
                HotelDetails hotelDetails = hbServlet.getHotelById(hotelID);
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                String query = "?query=tourist%20attractions+in+";
                String radius = String.valueOf(Math.round(radiusInMiles * 1609.34));
                String params = hotelDetails.getCity().replace(" ", "%20")+"&location="+hotelDetails.getLatitude()+","+hotelDetails.getLongitude()+"&radius="+radius+"&key="+myAPIKey;
                String urlString = "https://"+host+path+query+params;
        
                URL url = new URL(urlString);
                // HTTPS uses port 443
                socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
                // output stream for the secure socket
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                String request = getRequest(url.getHost(), url.getPath()+"?"+url.getQuery());
        
                out.println(request); // send a request to the server
                out.flush();
        
                // input stream for the secure socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
                // use input stream to read server's response
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
        
                String s = sb.toString();
        
                /**Pattern to strip response header*/
                Pattern p1 = Pattern.compile("(.*?)\\{(.*)");
        
                Matcher m1 = p1.matcher(s);
        
                if (m1.find()) {
                    response = "{ "+m1.group(2);
                }
            } catch (IOException e) {
                System.out.println(
                        "An IOException occured while writing to the socket stream or reading from the stream: "+e);
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    // close the streams and the socket
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("An exception occured while trying to close the streams or the socket: "+e);
                }
            }
            parseTouristAttractions(hotelID, response);
            return response;
        }
        
        return null;
    }
    
    /**
     * Takes a host and a string containing path/resource/query and creates a
     * string of the HTTP GET request
     *
     * @param host
     * @param pathResourceQuery
     * @return
     */
    private String getRequest(String host, String pathResourceQuery) {
        String request = "GET " + pathResourceQuery + " HTTP/1.1" + System.lineSeparator() // GET
                         // request
                         + "Host: " + host + System.lineSeparator() // Host header required for HTTP/1.1
                         + "Connection: close" + System.lineSeparator() // make sure the server closes the
                         // connection after we fetch one page
                         + System.lineSeparator();
        return request;
    }
    
    /**
     * Read the json data with information about the tourist attractions near to a hotel and load it into the
     * appropriate data structure(s). Using JsonParser to parse the response.
     * @param touristAttractionsJson JSON response
     */
    public void parseTouristAttractions(String hotelID, String touristAttractionsJson) {
        // FILL IN CODE (from lab 1)
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(touristAttractionsJson);
        JsonArray touristAttractions = jsonObject.getAsJsonArray("results");
        
        String id = "";
        String name = "";
        double rating = 0.0;
        String address = "";
        ThreadSafeHotelData localData = new ThreadSafeHotelData();
        
        for(int i = 0; i < touristAttractions.size(); i++){
            JsonObject attraction = touristAttractions.get(i).getAsJsonObject();
            name = attraction.get("name").getAsString();
            id = attraction.get("id").getAsString();
            rating = attraction.get("rating").getAsDouble();
            address = attraction.get("formatted_address").getAsString();
            
            localData.addAttraction(id, name, rating, address);
        }
        if(localData != null) {
            hdata.combineAllAttractions(hotelID, localData);
        }
    }
    
    
    /** Print attractions near the hotels to a file.
     * The format is described in the project description.
     *
     * @param filename
     */
    public void printAttractions(Path filename) {
        // FILL IN CODE
        hdata.printAttractions(filename);
    }
    
    
    /**
     * Creates a secure socket to communicate with Google Places API server,
     * sends a GET request (to find attractions close to
     * the hotel within a given radius), and gets a response as a string.
     * Removes headers from the response string and parses the remaining json to
     * get Attractions info. Adds attractions to the corresponding data structure that supports
     * efficient search for tourist attractions given the hotel id.
     *
     * @return
     */
    public String fetchDescriptions(String hotelID) {
        // FILL IN CODE
        // This method should call getRequest method
        if (hotelID != null) {
            /*JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = null;
            try {
                jsonObject = (JsonObject) jsonParser.parse(new FileReader("input/config.json"));
                myAPIKey = jsonObject.get("apikey").getAsString();
                if (myAPIKey.isEmpty()) {
                    throw new IllegalArgumentException("API key missing in config.json file");
                }
                
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (IllegalArgumentException e) {
                System.out.println(e);
            }
            */
            PrintWriter out = null;
            BufferedReader in = null;
            SSLSocket socket = null;
            String response = "";
            
            try {
                HotelDetails hotelDetails = hdata.findHotelById(hotelID);
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                //String query = "?query=tourist%20attractions+in+";
                //String radius = String.valueOf(Math.round(radiusInMiles * 1609.34));
                //String params = hotelDetails.getCity().replace(" ", "-")+"&location="+hotelDetails.getLatitude()+","+hotelDetails.getLongitude()+"&radius="+radius+"&key="+myAPIKey;
                String urlString = "https://www.expedia.com/h" + hotelID + ".Hotel-Information";
                URL url = new URL(urlString);
                System.out.println(url);
                // HTTPS uses port 443
                socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
                // output stream for the secure socket
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                String request = getRequest(url.getHost(), url.getPath());
                System.out.println(request);
                out.println(request); // send a request to the server
                out.flush();
                
                // input stream for the secure socket.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                // use input stream to read server's response
                String line;
                StringBuffer sb = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                    System.out.println(line);
                }
                
                String s = sb.toString();
                System.out.println(response);
                
                /**Pattern to strip response header*/
                Pattern p1 = Pattern.compile("(.*?)\\{(.*)");
                
                Matcher m1 = p1.matcher(s);
                
                if (m1.find()) {
                    response = "{ "+m1.group(2);
                }
            } catch (IOException e) {
                System.out.println(
                        "An IOException occured while writing to the socket stream or reading from the stream: "+e);
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                try {
                    // close the streams and the socket
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException e) {
                    System.out.println("An exception occured while trying to close the streams or the socket: "+e);
                }
            }
            
            //parseTouristAttractions(hotelID, response);
            return response;
        }
        
        return null;
    }
    
    
    /**
     * Takes an html file from expedia for a particular hotelId, and scrapes it for some data about this hotel:
     * About this area and About this property descriptions. Stores this information in ThreadSafeHotelData so that
     * we are able to efficiently access it given the hotel Id.
     */
    public void parseHTML(String hotelId) {
        String response = fetchDescriptions(hotelId);
        System.out.println(response);
            /*BufferedReader br = new BufferedReader(new FileReader(filename.toString()));
    
            // use input stream to read server's response
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String s = sb.toString();
             */
        HotelDetails hotelDetails = hdata.findHotelById(hotelId);
        if(hotelDetails != null) {
            Pattern p1 = Pattern.compile("(About this area)(.*?)<h4(.*?)>(.*?)<\\/h4>(.*?)<p(.*?)>(.*?)<\\/p>");
            String areaDescription = "";
            Matcher m1 = p1.matcher(response);
            if (m1.find()) {
                areaDescription = m1.group(4);
                areaDescription += System.lineSeparator()+System.lineSeparator();
                areaDescription += m1.group(7);
                areaDescription = areaDescription.replace("&#x27;", "'");
            }
            hotelDetails.setAreaDescription(areaDescription);

            Pattern p2 = Pattern.compile("(About this property)(.*?)<h4(.*?)>(.*?)<\\/h4>(.*?)<p(.*?)>(.*?)<\\/p>");
            String propertyDescription = "";
            Matcher m2 = p2.matcher(response);
            if (m2.find()) {
                propertyDescription = m2.group(4);
                propertyDescription += System.lineSeparator()+System.lineSeparator();
                propertyDescription += m2.group(7);
                propertyDescription = propertyDescription.replace("&#x27;", "'");
            }
            hotelDetails.setPropertyDescription(propertyDescription);
            hdata.addDescription(hotelId, hotelDetails);
        } else {
            System.out.println("Hotel doesn't exist with id-" + hotelId);
        }
    
    
    }
    // FILL IN CODE: add other helper methods as needed

    /** Prints property descriptions and area descriptions for each hotel from
     * the ThreadSafeHotelData to the given file. Format specified in the project description.
     * @param filename output file
     */
    public void printDescriptions(Path filename) {
        // FILL IN CODE
        hdata.printDescriptions(filename);
    }
}
