package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Rover {

	// setup the RoverCommandProcessor links
	protected BufferedReader receiveFrom_RCP;
	protected PrintWriter sendTo_RCP;
	
	public String rovername;
	public ScanMap scanMap;
	public int sleepTime;
	public String SERVER_ADDRESS = "localhost";
	
	public String timeRemaining;
	public Coord currentLoc = null;
	public Coord previousLoc = null;
	public Coord StartLocation = null;
	public Coord TargetLocation = null;
	
	public ArrayList<String> equipment = new ArrayList<String>();
	
	// Hardcoded port number for the CS-5337 class
	protected static final int PORT_ADDRESS = 9537;
	
	//TODO add code to the move methods to check for impassable terrain
	protected void moveNorth(){
		sendTo_RCP.println("MOVE N");
	}
	
	protected void moveSouth(){
		sendTo_RCP.println("MOVE S");
	}
	
	protected void moveEast(){
		sendTo_RCP.println("MOVE E");
	}
	
	protected void moveWest(){
		sendTo_RCP.println("MOVE W");
	}
	
	protected Coord getStartLocation() throws IOException{
		String line = null;
		sendTo_RCP.println("START_LOC");
		line = receiveFrom_RCP.readLine();
	    if (line == null) {
	    	System.out.println(rovername + " check connection to server");
	    	line = "";
	    }
		if (line.startsWith("START_LOC")) {
			return extractLocationFromString(line);
		}
		return null;
	}
	
	protected Coord getTargetLocation() throws IOException{
		String line = null;
		sendTo_RCP.println("TARGET_LOC");
		line = receiveFrom_RCP.readLine();
	    if (line == null) {
	    	System.out.println(rovername + " check connection to server");
	    	line = "";
	    }
		if (line.startsWith("TARGET_LOC")) {
			return extractLocationFromString(line);
		}
		return null;
	}
	
	protected Coord getCurrentLocation() throws IOException{
		String line = null;
		sendTo_RCP.println("LOC");
		line = receiveFrom_RCP.readLine();
		if(line == null){
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			return extractLocationFromString(line);				
		}
		return null;
	}
	
	protected void clearReadLineBuffer() throws IOException{
		while(receiveFrom_RCP.ready()){
			receiveFrom_RCP.readLine();	
		}
	}
	
	protected String getTimeRemaining() throws IOException{
		String line;
		String timeRemaining = null;
		sendTo_RCP.println("TIMER");
		line = receiveFrom_RCP.readLine();
        if (line == null) {
        	System.out.println(rovername + " check connection to server");
        	line = "";
        }
		if (line.startsWith("TIMER")) {
			timeRemaining = line.substring(6);
			System.out.println(rovername + " timeRemaining: " + timeRemaining);
		}
		return timeRemaining;
	}
	
	// method to retrieve a list of the rover's EQUIPMENT from the server
	protected ArrayList<String> getEquipment() throws IOException {
		Gson gson = new GsonBuilder()
    			.setPrettyPrinting()
    			.enableComplexMapKeySerialization()
    			.create();
		sendTo_RCP.println("EQUIPMENT");
		
		String jsonEqListIn = receiveFrom_RCP.readLine(); //grabs the string that was returned first
		if(jsonEqListIn == null){
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		
		if(jsonEqListIn.startsWith("EQUIPMENT")){
			while (!(jsonEqListIn = receiveFrom_RCP.readLine()).equals("EQUIPMENT_END")) {
				if(jsonEqListIn == null){
					break;
				}
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}
		
		String jsonEqListString = jsonEqList.toString();		
		ArrayList<String> returnList;		
		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>(){}.getType());		
		return returnList;
	}
	
	
	
	
	// sends a SCAN request to the server and puts the result in the scanMap array
	protected ScanMap doScan() throws IOException {
		Gson gson = new GsonBuilder()
    			.setPrettyPrinting()
    			.enableComplexMapKeySerialization()
    			.create();
		sendTo_RCP.println("SCAN");

		String jsonScanMapIn = receiveFrom_RCP.readLine(); //grabs the string that was returned first
		if(jsonScanMapIn == null){
			System.out.println(rovername + " check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println( rovername + " incomming SCAN result - first readline: " + jsonScanMapIn);
		
		if(jsonScanMapIn.startsWith("SCAN")){	
			while (!(jsonScanMapIn = receiveFrom_RCP.readLine()).equals("SCAN_END")) {
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "SCAN"
		}

		String jsonScanMapString = jsonScanMap.toString();
		// convert from the json string back to a ScanMap object
		return gson.fromJson(jsonScanMapString, ScanMap.class);
	}
	
	//adding method for gathering science
	protected void gatherScience(){
        sendTo_RCP.println("GATHER");
    }
	

	// this takes the server response string, parses out the x and y values and
	// returns a Coord object	
	protected static Coord extractLocationFromString(String sStr) {
		int indexOf;
		indexOf = sStr.indexOf(" ");
		sStr = sStr.substring(indexOf +1);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}
}
