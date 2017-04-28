package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.Communication;
import common.Coord;
import common.MapTile;
import common.Rover;
import common.RoverDetail;
import common.ScienceDetail;
import enums.RoverConfiguration;
import enums.RoverDriveType;
import enums.RoverToolType;
import enums.Terrain;
import rover_logic.Astar;


/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_02 extends Rover {


	//Scan Crystal 
	List<Coord> crystalCoordinates = new ArrayList<Coord>();

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
			startLocation = getStartLocation();
			System.out.println(rovername + " START_LOC " + startLocation);


			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = getTargetLocation();
			System.out.println(rovername + " TARGET_LOC " + targetLocation);

			Astar aStar = new Astar();


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

				//adding RoverDetail and ScienceDetail as per the unified Comm server
				RoverDetail roverDetail = new RoverDetail();
				ScienceDetail scienceDetail = analyzeAndGetSuitableScience();

				//adding science/harvest
				if (scienceDetail!=null){
					//check for science
					System.out.println("####### Science detail: "+scienceDetail+" ############");
					if (scienceDetail != null) {

						System.out.println("FOUND SCIENCE TO GATHER: " + scienceDetail);
					}
					if ( scienceDetail.getX() == getCurrentLocation().xpos
							&& scienceDetail.getY() == getCurrentLocation().ypos ) {
						gatherScience( getCurrentLocation() );
						System.out.println( "$$$$$> Gathered science "
								+ scienceDetail.getScience() + " at location "
								+ getCurrentLocation() );
					}
					else {

						RoverConfiguration roverConfiguration = RoverConfiguration
								.valueOf( rovername );
						RoverDriveType driveType = RoverDriveType.valueOf(
								roverConfiguration.getMembers().get( 0 ) );
						RoverToolType tool1 = RoverToolType.getEnum(
								roverConfiguration.getMembers().get( 1 ) );
						RoverToolType tool2 = RoverToolType.getEnum(
								roverConfiguration.getMembers().get( 2 ) );

						aStar.addScanMap( doScan(), getCurrentLocation(), tool1,
								tool2 );

						char dirChar = aStar.findPath( getCurrentLocation(),
								new Coord( scienceDetail.getX(),
										scienceDetail.getY() ),
								driveType );
						System.out.println("from astar dirChar is: "+dirChar);
						//deciding direction based on the response from astar
						if(dirChar == 'S'){
							System.out.println("moving South, because I'm directed to go: "+dirChar);
							moveSouth();
						}
						if(dirChar == 'W'){
							System.out.println("moving West, because I'm directed to go: "+dirChar);
							moveWest();
						}
						if(dirChar == 'E'){
							System.out.println("moving East, because I'm directed to go: "+dirChar);
							moveEast();
						}
						if(dirChar == 'N'){
							System.out.println("moving North, because I'm directed to go: "+dirChar);
							moveNorth();
						}
					}

				} // end primary addition of science/harvest

				//following else portion is for when scienceDetail is not found, this is our default movement
				else{ //START TEST 
					if (blocked) {
						if(stepCount > 0){
							if(southBlocked() == true && westBlocked() == false){
								//System.out.println("-----HELP ME I AM BLOCKED FROM SOUTH!!-----");
								moveWest();
								stepCount -=1;
							}
							else if(southBlocked() == true && westBlocked() == true){
								//System.out.println("-----HELP ME I AM BLOCKED FROM SOUTH!!-----");
								moveEast();
								stepCount -=1;
							}
							else if(southBlocked() == true && eastBlocked() == true){
								//System.out.println("-----HELP ME I AM BLOCKED FROM SOUTH!!-----");
								moveWest();
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
						//communication.detectScience(scanMapTiles, currentLoc, centerIndex);
						//communication.displayAllDiscoveries();
						//communication.detectCrystalScience(scanMapTiles,currentLoc);
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
								System.out.println(">>>>>>>EAST BLOCKED<<<<<<<<");
								blocked = true;
								stepCount = 5;  //side stepping
							} else {
								// request to server to move
								moveEast();			
							}					
						}
					}
				}//end bracket for ELSE/DEFAULT MOVEMENT 

				//END following else portion is for when scienceDetail is not found, this is our default movement


				// another call for current location
				currentLoc = getCurrentLocation();



				// test for stuckness
				stuck = currentLoc.equals(previousLoc);	

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
		// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
		if (scanMapTiles[centerIndex][centerIndex+1].getHasRover() 
				|| scanMapTiles[centerIndex][centerIndex+1].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex][centerIndex+1].getTerrain() == Terrain.NONE) {
			System.out.println(">>>>>>>SOUTH BLOCKED<<<<<<<<");
			return true;
		} else {
			// request to server to move
			return false;
		}
	}
	//end check moving south

	//checking if moving east is allowed
	public boolean eastBlocked(){
		// pull the MapTile array out of the ScanMap object
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
		if (scanMapTiles[centerIndex+1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex+1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex+1][centerIndex].getTerrain() == Terrain.NONE) {
			System.out.println(">>>>>>>EAST BLOCKED<<<<<<<<");
			return true;
		} else {
			// request to server to move
			return false;
		}
	}
	//end check moving east

	//checking if moving west is allowed
	public boolean westBlocked(){
		// pull the MapTile array out of the ScanMap object
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
		if (scanMapTiles[centerIndex-1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.NONE) {
			System.out.println(">>>>>>>WEST BLOCKED<<<<<<<<");
			return true;
		} else {
			// request to server to move
			return false;
		}
	}
	//end check moving west	


	//checking if moving north is allowed
	public boolean northBlocked(){
		// pull the MapTile array out of the ScanMap object
		MapTile[][] scanMapTiles = scanMap.getScanMap();
		int centerIndex = (scanMap.getEdgeSize() - 1)/2;
		// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
		if (scanMapTiles[centerIndex-1][centerIndex].getHasRover() 
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex-1][centerIndex].getTerrain() == Terrain.NONE) {
			System.out.println(">>>>>>>NORTH BLOCKED<<<<<<<<");
			return true;
		} else {
			// request to server to move
			return false;
		}
	}
	//end check moving north	

	//		public void detectCrystalScience(MapTile[][] scanMapTiles) {       
	//			
	//			int centerIndex = (scanMap.getEdgeSize() - 1) / 2;	
	//			int xPos = currentLoc.xpos - centerIndex;
	//			int yPos = currentLoc.ypos - centerIndex;
	//			
	//			//This gives the current location 
	//			System.out.println("X: "+ xPos +" Y: "+ yPos);
	//			 
	//			 int crystalXPosition, crystalYPosition;
	//			 
	//			 //Iterating through X coordinate
	//			 for (int x = 0; x < scanMapTiles.length; x++){
	//	           
	//				//Iterating through Y coordinate
	//				 for (int y = 0; y < scanMapTiles.length; y++){
	//					//Checking for crystal Science and locating the crystal	         
	//					 if (scanMapTiles[x][y].getScience() == Science.CRYSTAL) {
	//	              
	//						 crystalXPosition = xPos + x;
	//						 crystalYPosition = yPos + y;
	//			            
	//	                   	Coord coord = new Coord(crystalXPosition ,crystalYPosition);//Coordination class constructor with two arguments
	//			                System.out.println("Crystal position discovered:In "+ scanMapTiles[x][y].getTerrain() +" at the position "+coord);
	//			                crystalCoordinates.add(coord);
	//	               }
	//	           }
	//		     }
	//		 }





	//method for the JSON object
	public static void sendJSON(){

	}
	//end JSON method


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