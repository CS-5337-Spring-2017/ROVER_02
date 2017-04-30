package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import common.Rover;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_00 extends Rover {

	public ROVER_00() {
		// constructor
		System.out.println("ROVER_00 rover object constructed");
		rovername = "ROVER_00";
	}

	public ROVER_00(String serverAddress) {
		// constructor
		System.out.println("ROVER_00 rover object constructed");
		rovername = "ROVER_00";
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

			// Need to allow time for the connection to the server to be
			// established
			sleepTime = 900;

			// Process all messages from server, wait until server requests
			// Rover ID
			// name - Return Rover Name to complete connection
			while (true) {
				String line = receiveFrom_RCP.readLine();
				if (line.startsWith("SUBMITNAME")) {
					// This sets the name of this instance of a swarmBot for
					// identifying the thread to the server
					sendTo_RCP.println(rovername);
					break;
				}
			}

			/**
			 * ### Setting up variables to be used in the Rover control loop ###
			 */
			int stepCount = 0;
			String line = "";
			boolean goingSouth = false;
			boolean stuck = false; // just means it did not change locations
									// between requests,
									// could be velocity limit or obstruction
									// etc.
			boolean blocked = false;

			String[] cardinals = new String[4];
			cardinals[0] = "N";
			cardinals[1] = "E";
			cardinals[2] = "S";
			cardinals[3] = "W";
			String currentDir = cardinals[0];

			/**
			 * ### Retrieve static values from RCP ###
			 */
			// **** get equipment listing ****
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results " + equipment + "\n");

			// **** Request START_LOC Location from SwarmServer **** this might
			// be dropped as it should be (0, 0)
			startLocation = getStartLocation();
			System.out.println(rovername + " START_LOC " + startLocation);

			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = getTargetLocation();
			System.out.println(rovername + " TARGET_LOC " + targetLocation);

			/**
			 * #### Rover controller process loop ####
			 */
			while (true) { // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

				try {
					System.out.println("All Rover details from comm server: " + Arrays.asList(getAllRoverDetails()));
				} catch (Exception e) {
					System.out.println("Comm server failure: " + e.getClass().getName() + ":" + e.getMessage());
				}
				//
				// // **** Request Rover Location from RCP ****
				// currentLoc = getCurrentLocation();
				// System.out.println(rovername + " currentLoc at start: " +
				// currentLoc);
				//
				// // after getting location set previous equal current to be
				// able to check for stuckness and blocked later
				// previousLoc = currentLoc;
				//
				//
				//
				// // ***** do a SCAN *****
				// // gets the scanMap from the server based on the Rover
				// current location
				// scanMap = doScan();
				// // prints the scanMap to the Console output for debug
				// purposes
				// scanMap.debugPrintMap();
				//
				//
				//
				// // ***** get TIMER time remaining *****
				// timeRemaining = getTimeRemaining();
				//
				//
				//
				// // ***** MOVING *****
				// // try moving east 5 block if blocked
				// if (blocked) {
				// if(stepCount > 0){
				// moveEast();
				// stepCount -= 1;
				// }
				// else {
				// blocked = false;
				// //reverses direction after being blocked and side stepping
				// goingSouth = !goingSouth;
				// }
				//
				// } else {
				//
				// // pull the MapTile array out of the ScanMap object
				// MapTile[][] scanMapTiles = scanMap.getScanMap();
				// int centerIndex = (scanMap.getEdgeSize() - 1)/2;
				// // tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
				//
				// if (goingSouth) {
				// // check scanMap to see if path is blocked to the south
				// // (scanMap may be old data by now)
				// if (scanMapTiles[centerIndex][centerIndex +1].getHasRover()
				// || scanMapTiles[centerIndex][centerIndex +1].getTerrain() ==
				// Terrain.ROCK
				// || scanMapTiles[centerIndex][centerIndex +1].getTerrain() ==
				// Terrain.SAND
				// || scanMapTiles[centerIndex][centerIndex +1].getTerrain() ==
				// Terrain.NONE) {
				// blocked = true;
				// stepCount = 5; //side stepping
				// } else {
				// // request to server to move
				// moveSouth();
				//
				// }
				//
				// } else {
				// // check scanMap to see if path is blocked to the north
				// // (scanMap may be old data by now)
				//
				// if (scanMapTiles[centerIndex][centerIndex -1].getHasRover()
				// || scanMapTiles[centerIndex][centerIndex -1].getTerrain() ==
				// Terrain.ROCK
				// || scanMapTiles[centerIndex][centerIndex -1].getTerrain() ==
				// Terrain.SAND
				// || scanMapTiles[centerIndex][centerIndex -1].getTerrain() ==
				// Terrain.NONE) {
				// blocked = true;
				// stepCount = 5; //side stepping
				// } else {
				// // request to server to move
				// moveNorth();
				// }
				// }
				// }
				//
				// // another call for current location
				// currentLoc = getCurrentLocation();
				//
				//
				// // test for stuckness
				// stuck = currentLoc.equals(previousLoc);
				//
				// // this is the Rovers HeartBeat, it regulates how fast the
				// Rover cycles through the control loop
				Thread.sleep(sleepTime);
				//
				// System.out.println("ROVER_00 ------------ bottom process
				// control --------------");
			} // END of Rover control While(true) loop

			// This catch block closes the open socket connection to the server
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("ROVER_00 problem closing socket");
				}
			}
		}

	} // END of Rover run thread

	// ####################### Support Methods #############################

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_00 client;
		// if a command line argument is present it is used
		// as the IP address for connection to SwarmServer instead of localhost

		if (!(args.length == 0)) {
			client = new ROVER_00(args[0]);
		} else {
			client = new ROVER_00();
		}

		client.run();
	}
}