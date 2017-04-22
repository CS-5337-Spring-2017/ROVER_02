package common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import enums.Science;
import testUtillities.ParseStrings;











/**
 * Created by samskim on 5/12/16.
 * 
 * This class is the interface to the SwarmCommunicationServer nodejs/javascript program
 */
public class Communication {

    private String url;
    JSONParser parser;
    private String rovername;
    private String corp_secret;

    /** Coordinates of all the Science discovered by this ROVER */
    private List<Coord> discoveredSciences = new ArrayList<Coord>();

    ParseStrings toString ;
    
    public void displayAllDiscoveries() {
        System.out.println(rovername + " SCIENCE-DISCOVERED: "
                + toProtocolString(discoveredSciences));
        System.out.println(rovername + " TOTAL-NUMBER-OF-SCIENCE-DISCOVERED: "
                + discoveredSciences.size());
    }
    
    private String toProtocolString(List<Coord> coords) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = coords.size() - 1; i >= 0; i--) {
            sb.append(coords.get(i).toString() + " ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public List<Coord> updateDiscoveries(List<Coord> detectedSciences) {
       
    	List<Coord> new_sciences = new ArrayList<Coord>();
        for (Coord c : detectedSciences) {
            if (!discoveredSciences.contains(c)) {
                discoveredSciences.add(c);
                new_sciences.add(c);
            }
        }
        return new_sciences;
    }

    public List<Coord> detectScience(MapTile[][] map, Coord rover_coord, int sightRange) {
      
    	List<Coord> scienceCoords = new ArrayList<Coord>();
    	Science scienceObj = null;
    	
        /* iterate through every MapTile Object in the 2D Array. If the MapTile
         * contains science, calculate and save the coordinates of the tiles. */
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {

                MapTile mapTile = map[x][y];

                System.out.println("map science: " + mapTile.getScience().getSciString() + " crystal: " + scienceObj.CRYSTAL.name());
                if (mapTile.getScience().getSciString() == scienceObj.CRYSTAL.name()) {
                	
                    int tileX = rover_coord.xpos + (x - sightRange);
                    int tileY = rover_coord.ypos + (y - sightRange);
                    Coord eachCoord =  new Coord(tileX, tileY);
                    updateDiscoveries(scienceCoords);
                    scienceCoords.add(eachCoord);
                }
            }
        }
        return scienceCoords;
    }

    
    
    public Communication(String url, String rovername, String corp_secret) {
        this.url = url;
        this.parser = new JSONParser();
        this.rovername = rovername;
        this.corp_secret = corp_secret;

    }
    
    

    public String postScanMapTiles(Coord currentLoc, MapTile[][] scanMapTiles) {
        JSONArray data = convertScanMapTiles(currentLoc, scanMapTiles);

        String charset = "UTF-8";
        URL obj = null;
        try {
            obj = new URL(url + "/global");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Rover-Name", rovername);
            con.setRequestProperty("Corp-Secret", corp_secret);
            con.setRequestProperty("Location", currentLoc.toString());
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Location", currentLoc.toString());

            byte[] jsonBytes = data.toString().getBytes("UTF-8");

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(jsonBytes);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private JSONArray convertScanMapTiles(Coord currentLoc, MapTile[][] scanMapTiles) {
        int edgeSize = scanMapTiles.length;
        int centerIndex = (edgeSize - 1) / 2;

        JSONArray tiles = new JSONArray();
        for (int row = 0; row < scanMapTiles.length; row++) {
            for (int col = 0; col < scanMapTiles[row].length; col++) {

                MapTile mapTile = scanMapTiles[col][row];

                int xp = currentLoc.xpos - centerIndex + col;
                int yp = currentLoc.ypos - centerIndex + row;
                Coord coord = new Coord(xp, yp);
                JSONObject tile = new JSONObject();
                tile.put("x", xp);
                tile.put("y", yp);
                tile.put("terrain", mapTile.getTerrain().toString());
                tile.put("science", mapTile.getScience().toString());
                tiles.add(tile);
            }
        }
        return tiles;
    }

    // for requesting global map
    public JSONArray getGlobalMap() {

        URL obj = null;
        String responseStr = "";
        try {
            obj = new URL(url + "/global");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("Rover-Name", rovername);
            con.setRequestProperty("Corp-Secret", corp_secret);
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            responseStr = response.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parseResponseStr(responseStr);
    }

    public JSONArray parseResponseStr(String response) {
        JSONArray data = null;
        try {
            data = (JSONArray) parser.parse(response);

            for (Object obj : data) {
                JSONObject json = (JSONObject) obj;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;
    }

    public String markTileForGather(Coord coord){
        int x = coord.xpos;
        int y = coord.ypos;

        String charset = "UTF-8";
        URL obj = null;
        try {
            obj = new URL(url + "/science/gather/" + x + "/" + y);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Rover-Name", rovername);
            con.setRequestProperty("Corp-Secret", corp_secret);
            con.setRequestProperty("Content-Type", "application/json");
            
            
            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    
    

    
    
    
    
}
