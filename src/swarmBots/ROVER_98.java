//package swarmBots;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.lang.reflect.Type;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;
//
//import common.Coord;
//import common.MapTile;
//import common.Rover;
//import common.ScanMap;
//import enums.Terrain;
//
///**
// * The seed that this program is built on is a chat program example found here:
// * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
// * publishing their code examples
// */
//
//public class ROVER_98 extends Rover {
//
//
//	public ROVER_98() {
//		// constructor
//		System.out.println("ROVER_98 rover object constructed");
//		rovername = "ROVER_98";
//	}
//	
//	public ROVER_98(String serverAddress) {
//		// constructor
//		System.out.println("ROVER_98 rover object constructed");
//		rovername = "ROVER_98";
//		SERVER_ADDRESS = serverAddress;
//	}
//
//	/**
//	 * Connects to the server then enters the processing loop.
//	 */
//	private void run() throws IOException, InterruptedException {
//
//		// Make connection to SwarmServer and initialize streams
//		Socket socket = null;
//		try {
//			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
//
//			receiveFrom_RCP = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			sendTo_RCP = new PrintWriter(socket.getOutputStream(), true);
//			
//			// Need to allow time for the connection to the server to be established
//			sleepTime = 300;
//			
//			// Process all messages from server, wait until server requests Rover ID
//			// name - Return Rover Name to complete connection
//			while (true) {
//				String line = receiveFrom_RCP.readLine();
//				if (line.startsWith("SUBMITNAME")) {
//					//This sets the name of this instance of a swarmBot for identifying the thread to the server
//					sendTo_RCP.println(rovername); 
//					break;
//				}
//			}
//	
//	
//			
//			/**
//			 *  ### Setting up variables to be used in the Rover control loop ###
//			 */
//			String line = "";
//			boolean stuck = false; 
//			boolean blocked = false;
//
//		
//			/**
//			 *  ### Retrieve static values from RCP ###
//			 */		
//			// **** get equipment listing ****			
//			equipment = getEquipment();
//			System.out.println(rovername + " equipment list results " + equipment + "\n");
//			
//			
//			// **** Request START_LOC Location from SwarmServer **** this might be dropped as it should be (0, 0)
//			StartLocation = getStartLocation();
//			System.out.println(rovername + " START_LOC " + StartLocation);
//			
//			
//			// **** Request TARGET_LOC Location from SwarmServer ****
//			TargetLocation = getTargetLocation();
//			System.out.println(rovername + " TARGET_LOC " + TargetLocation);
//			
//			
//	
//
//			/**
//			 *  ####  Rover controller process loop  ####
//			 */
//			while (true) {                     //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
//		
//				// **** Request Rover Location from RCP ****
//				currentLoc = getCurrentLocation();
//				System.out.println(rovername + " currentLoc at start: " + currentLoc);				
//				// after getting location set previous equal current to be able to check for stuckness and blocked later
//				previousLoc = currentLoc;		
//							
//				// ***** do a SCAN *****
//				// gets the scanMap from the server based on the Rover current location
//				scanMap = doScan(); 
//				// prints the scanMap to the Console output for debug purposes
//				scanMap.debugPrintMap();
//										
//				// ***** get TIMER time remaining *****
//				timeRemaining = getTimeRemaining();
//				
//	
//				
//				// ***** MOVING *****
//				// TODO - logic to calculate where to move
//	
//				
//				// this is the Rovers HeartBeat, it regulates how fast the Rover cycles through the control loop
//				Thread.sleep(sleepTime);
//				
//				System.out.println("ROVER_98 ------------ bottom process control --------------"); 
//			}  // END of Rover control While(true) loop
//		
//		// This catch block closes the open socket connection to the server
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//	        if (socket != null) {
//	            try {
//	            	socket.close();
//	            } catch (IOException e) {
//	            	System.out.println("ROVER_98 problem closing socket");
//	            }
//	        }
//	    }
//
//	} // END of Rover run thread
//	
//	// ####################### Support Methods #############################
//	
//
//	
//
//
//	/**
//	 * Runs the client
//	 */
//	public static void main(String[] args) throws Exception {
//		ROVER_98 client;
//    	// if a command line argument is present it is used
//		// as the IP address for connection to SwarmServer instead of localhost 
//		
//		if(!(args.length == 0)){
//			client = new ROVER_98(args[0]);
//		} else {
//			client = new ROVER_98();
//		}
//		
//		client.run();
//	}
//}