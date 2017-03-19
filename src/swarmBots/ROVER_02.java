package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import common.Communication;
import common.MapTile;
import common.Rover;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_02 extends Rover {


	public ROVER_02() {
		// constructor
		System.out.println("ROVER_02 rover object constructed");
		rovername = "ROVER_02";
	}
	
	public ROVER_02(String serverAddress) {
		// constructor
		System.out.println("ROVER_02 rover object constructed");
		rovername = "ROVER_02";
		SERVER_ADDRESS = serverAddress;
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

			receiveFrom_RCP = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			sendTo_RCP = new PrintWriter(socket.getOutputStream(), true);
			
			// Need to allow time for the connection to the server to be established
			sleepTime = 300;
			
			// Process all messages from server, wait until server requests Rover ID
			// name - Return Rover Name to complete connection
			
			// Initialize communication server connection to send map updates
			Communication communication = new Communication("http://localhost:3000/api", rovername, "open_secret");

			while (true) {
				String line = receiveFrom_RCP.readLine();
				if (line.startsWith("SUBMITNAME")) {
					//This sets the name of this instance of a swarmBot for identifying the thread to the server
					sendTo_RCP.println(rovername); 
					break;
				}
			}
	
	
			
			/**
			 *  ### Setting up variables to be used in the Rover control loop ###
			 */
			int stepCount = 0;	
			String line = "";	
			boolean goingWest = false;
			
			
			boolean stuck = false; // just means it did not change locations between requests,
									// could be velocity limit or obstruction etc.
			
			boolean blocked = false;
	
			String[] cardinals = new String[4];
			cardinals[0] = "N";
			cardinals[1] = "E";
			cardinals[2] = "S";
			cardinals[3] = "W";	
			String currentDir = cardinals[2];		
			
			/**
			 *  ### Retrieve static values from RCP ###
			 */		
			// **** get equipment listing ****			
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results " + equipment + "\n");
			
			
			// **** Request START_LOC Location from SwarmServer **** this might be dropped as it should be (0, 0)
			StartLocation = getStartLocation();
			System.out.println(rovername + " START_LOC " + StartLocation);
			
			
			// **** Request TARGET_LOC Location from SwarmServer ****
			TargetLocation = getTargetLocation();
			System.out.println(rovername + " TARGET_LOC " + TargetLocation);
			
			
	

			/**
			 *  ####  Rover controller process loop  ####
			 */
			while (true) {                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		
				// **** Request Rover Location from RCP ****
				currentLoc = getCurrentLocation();
				System.out.println(rovername + " currentLoc at start: " + currentLoc);
				
				// after getting location set previous equal current to be able to check for stuckness and blocked later
				previousLoc = currentLoc;		
				
				

				// ***** do a SCAN *****
				// gets the scanMap from the server based on the Rover current location
				scanMap = doScan(); 
				// prints the scanMap to the Console output for debug purposes
				scanMap.debugPrintMap();
				
		
							
				// ***** get TIMER time remaining *****
				timeRemaining = getTimeRemaining();
				
	
				
				// ***** MOVING *****
				
				if (blocked) {
					if(stepCount > 0){
						if(southBlocked()){	
							moveEast();
							stepCount -=1;
						}
						else{
							moveSouth();
							stepCount -=1;
						}
					}
					else {
						blocked = false;
						//reverses direction after being blocked and side stepping
						goingWest = !goingWest;
					}
					
				} else {
	
					// pull the MapTile array out of the ScanMap object
					MapTile[][] scanMapTiles = scanMap.getScanMap();
					int centerIndex = (scanMap.getEdgeSize() - 1)/2;
					
					communication.postScanMapTiles(currentLoc, scanMapTiles);
					// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
	
					if (goingWest) {
						// check scanMap to see if path is blocked to the West
						// (scanMap may be old data by now)
						if (scanMapTiles[centerIndex -1][centerIndex].getHasRover() 
								|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.SAND
								|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.NONE) {
							blocked = true;
							stepCount = 5;  //side stepping
						} else {
							// request to server to move
							moveWest();
						}
						
					} else {
						// check scanMap to see if path is blocked to the East
						// (scanMap may be old data by now)
						
						if (scanMapTiles[centerIndex +1][centerIndex].getHasRover() 
								|| scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.SAND
								|| scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.NONE) {
							
							blocked = true;
							stepCount = 5;  //side stepping
							} else {
							// request to server to move
							moveEast();			
						}					
					}
				}
			
	
				// another call for current location
				currentLoc = getCurrentLocation();

	
				// test for stuckness
				stuck = currentLoc.equals(previousLoc);	
				
				
				// Naif, My idea is to get the (x,y) of the rover when stuck and then move one step back and find alternative paths. 
//				if(stuck){
//					
//				int x = getCurrentLocation().xpos;
//				int y = getCurrentLocation().ypos;
//				
//					
//				}
				
				
				// this is the Rovers HeartBeat, it regulates how fast the Rover cycles through the control loop
				Thread.sleep(sleepTime);
				
				System.out.println(rovername + " ------------ bottom process control --------------"); 
			}  // END of Rover control While(true) loop
		
		// This catch block closes the open socket connection to the server
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        if (socket != null) {
	            try {
	            	socket.close();
	            } catch (IOException e) {
	            	System.out.println(rovername + " problem closing socket");
	            }
	        }
	    }

	} // END of Rover run thread
	
	// ####################### Support Methods #############################
	
	//checking if moving south is allowed
	public boolean southBlocked(){
		// pull the MapTile array out of the ScanMap object
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		
		if (scanMapTiles[centerIndex -1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex -1][centerIndex].getTerrain() == Terrain.NONE) {
		
			return true;
		} else {
			// request to server to move
			return false;
		}
	}
	//end check moving south

	


	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_02 client;
    	// if a command line argument is present it is used
		// as the IP address for connection to SwarmServer instead of localhost 
		
		if(!(args.length == 0)){
			client = new ROVER_02(args[0]);
		} else {
			client = new ROVER_02();
		}
		
		client.run();
	}
}