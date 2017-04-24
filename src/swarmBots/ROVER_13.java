//package swarmBots;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.PriorityQueue;
//import java.util.Random;
//import java.util.Stack;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonIOException;
//import com.google.gson.reflect.TypeToken;
//
//import common.Communication;
//import common.CommunicationHelper;
//import common.Coord;
//import common.MapTile;
//import common.PlanetMap;
//import common.ScanMap;
//import enums.RoverDriveType;
//import enums.Terrain;
//import enums.RoverToolType;
//import enums.Science;
//import rover_logic.Astar;
//import rover_logic.DStarLite;
//import rover_logic.SearchLogic;
//import rover_logic.State;
//
///**
// * The seed that this program is built on is a chat program example found here:
// * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
// * publishing their code examples
// */
//
////TODO: Update destinations - Coords that we can reach to extract science
////TODO: Update global map
//
//
//public class ROVER_13 {
//
//	BufferedReader in;
//	PrintWriter out;
//	String rovername;
//	ScanMap scanMap;
//	int sleepTime;
//	String SERVER_ADDRESS = "localhost";
//
//	ArrayList<String> radioactiveLocations = new ArrayList<String>();
//	static final int PORT_ADDRESS = 9537;
//	// Keep personal map when traversing - upload for each movement
//	public static Map<Coord, MapTile> globalMap = new HashMap<Coord, MapTile>();
//	char cardinals[] = { 'N', 'E', 'S', 'W' };
//	List<String> equipment = new ArrayList<String>();
//	// Rover has it's own logic class
//	public static DStarLite dsl;
//	// List of destinations to travel to i.e. science targets
//	public List<Coord>destinations = new ArrayList<Coord>();
//	private boolean initializedDSL = false;
//	// Communication variables
//	Communication comms;
//	private String corpSecret = "gz5YhL70a2";
//	private String url = "http://localhost:3000/api";
//	long steps = 1;
//
//	int maxX;
//	int maxY;
//
//	Coord currentLoc;
//
//	public ROVER_13() {
//		System.out.println("ROVER_03 rover object constructed");
//		rovername = "ROVER_03";
//		SERVER_ADDRESS = "localhost";
//		// in milliseconds - smaller is faster, but the server will cut
//		// connection if too small
//		sleepTime = 300;
//	}
//
//	public ROVER_13(String serverAddress) {
//		// constructor
//		System.out.println("ROVER_03 rover object constructed");
//		rovername = "ROVER_03";
//		SERVER_ADDRESS = serverAddress;
//		sleepTime = 300; // in milliseconds - smaller is faster, but the server
//							// will cut connection if it is too small
//	}
//
//	/**
//	 * Connects to the swarm server then enters the processing loop.
//	 */
//	private void run() throws IOException, InterruptedException {
//
//		// Make connection to SwarmServer and initialize streams
//		Socket socket = null;
//		try {
//			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
//
//			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//			out = new PrintWriter(socket.getOutputStream(), true);
//
//			// Process all messages from server, wait until server requests
//			// Rover ID
//			// name - Return Rover Name to complete connection
//			while (true) {
//				String line = in.readLine();
//				if (line.startsWith("SUBMITNAME")) {
//					out.println(rovername); // This sets the name of this
//											// instance
//											// of a swarmBot for identifying the
//											// thread to the server
//					break;
//				}
//			}
//
//			// ********* Rover logic setup *********
//			String line = "";
//			Coord rovergroupStartPosition = null;
//			Coord targetLocation = null;
//			// **** get equipment listing ****
//			equipment = new ArrayList<String>();
//			equipment = getEquipment();
//			System.out.println(rovername + " equipment list results " + equipment + "\n");
//			
//			
//			// **** Request START_LOC Location from SwarmServer ****
//			out.println("START_LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println(rovername + " check connection to server");
//				line = "";
//			}
//			if (line.startsWith("START_LOC")) {
//				rovergroupStartPosition = extractLocationFromString(line);
//			}
//			System.out.println(rovername + " START_LOC " + rovergroupStartPosition);
//			// **** Request TARGET_LOC Location from SwarmServer ****
//			out.println("TARGET_LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println(rovername + " check connection to server");
//				line = "";
//			}
//			if (line.startsWith("TARGET_LOC")) {
//				targetLocation = extractLocationFromString(line);
//			}
//			System.out.println(rovername + " TARGET_LOC " + targetLocation);
//			// Instantiate Communications
//			comms = new Communication(url, rovername, corpSecret);
//			
//
//			// First destination coordinate for mapping. Will be SE corner of rover's scanner range
//			//targetLocation = new Coord(rovergroupStartPosition.xpos + 3, rovergroupStartPosition.ypos + 3);
//
//
//			/*****************************************************
//			 * MOVEMENT METHODS ASTAR OR DSTAR -- COMMENT OUT ONE
//			 ***************************************************/
//			// moveDStar(line, rovergroupStartPosition, targetLocation);
//			moveAStar(line, rovergroupStartPosition, targetLocation);
//
//			// This catch block closes the open socket connection to the server
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally {
//			if (socket != null) {
//				try {
//					socket.close();
//				} catch (IOException e) {
//					System.out.println("ROVER_03 problem closing socket");
//				}
//			}
//		}
//
//	}
//
//	/*****************************
//	 * A_STAR STUFF
//	 ****************************/
//
//	@SuppressWarnings("unused")
//	public void moveAStar(String line, Coord startLoc, Coord targetLoc) throws IOException, InterruptedException {
//
//		System.out.println("Starting A Star");
//		Astar astar = new Astar(1000, 1000, startLoc, targetLoc);
//		currentLoc = null;
//		Coord previousLoc = null;
//		boolean destReached = false;
//		char dir = ' ';
//		destinations.add(targetLoc);
//		RoverDriveType driveType = RoverDriveType.getEnum(equipment.get(0));
//		RoverToolType tool_1 = RoverToolType.getEnum(equipment.get(1));
//		RoverToolType tool_2 = RoverToolType.getEnum(equipment.get(2));
//		
//		while (true) {
//
//			// **** location call ****
//			out.println("LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println("ROVER_03 check connection to server");
//				line = "";
//			}
//			if (line.startsWith("LOC")) {
//				currentLoc = extractLocationFromString(line);
//			}
//			previousLoc = currentLoc;
//			if (currentLoc.equals(targetLoc)) {
//				
//				//gather twice to double check
//				if(globalMap.get(targetLoc).getScience() != null){
//					//attempt gathering 4 times before moving on
//					for(int x = 4; x >= 0; x--){
//						Thread.sleep(sleepTime);
//						out.println("GATHER");
//					}
//				}
//				destReached = true;
//				System.out.println("Destination REACHED!!!");
//				System.out.println("Destinations left: " + destinations.size());
//			}
//
//			System.out.println("Current Loc: " + currentLoc.toString());
//
//			this.doScan();
//			astar.addScanMap(scanMap, currentLoc, tool_1, tool_2); 
//			scanMap.debugPrintMap();
//			//every 5 steps, get update from global map
//			if(steps % 5 == 1){
//				updateglobalMap(astar.getCom().getGlobalMap());
//				astar.updatePlanet(globalMap);
//			}
//			//walk
//			if (!destReached) {
//				updateMinMax(currentLoc);
//				System.out.println("DEBUG: maxX= " + maxX + " maxY= " + maxY);
//				dir = astar.findPath(currentLoc, targetLoc, driveType);
//			} else {
//				//dir = wander(line, dir);	//Until we can fix mapping function, we will use 'wander' function
//				cleanDestinations();
//				targetLoc = newTargetLoc();
//				steps++;
//				destReached = false;
//				Thread.sleep(sleepTime);
//				System.out.println("Switched target, skipping to new loop iteration...");
//				continue;
//			}
//			if (dir != 'U') {
//				out.println("MOVE " + dir);
//				System.out.println("DEBUG: MOVING TOWARDS " + targetLoc);
//			}else{
//				targetLoc = newTargetLoc();
//				System.out.println("Unreachable target, SWITCHING TARGETS");
//			}
//			
//			
//			steps++;
//			Thread.sleep(sleepTime);
//			System.out.println("ROVER_03 ------------ bottom process control --------------");
//		}
//
//	}
//	
//	//Update destinatios array, when it's empty to store
//	//unknown locations. Try search from 4 corners from outward corners to the center
//	public void updateDestinations(){
//		//look for unknown in first quadrant
//		quad1:
//		for(int x = 0; x < maxX/2; x++){
//			for(int y = 0; y < maxY/2; y++){
//				Coord possibleDest = new Coord(x, y);
//				if(!globalMap.containsKey(possibleDest)){
//					System.out.println("Adding new unknown Coordinate: " + possibleDest.toString());
//					destinations.add(possibleDest);
//					break quad1;
//				}
//			}
//		}
//		//look for unknown in second quadrant
//		quad2:
//		for(int x = maxX; x >= maxX/2; x--){
//			for(int y = 0; y < maxY/2; y++){
//				Coord possibleDest = new Coord(x, y);
//				if(!globalMap.containsKey(possibleDest)){
//					System.out.println("Adding new unknown Coordinate: " + possibleDest.toString());
//					destinations.add(possibleDest);
//					break quad2;
//				}
//			}
//		}
//		//look for unknown in third quadrant
//		quad3:
//		for(int x = 0; x < maxX/2; x++){
//			for(int y = maxY; y >= maxY/2; y--){
//				Coord possibleDest = new Coord(x, y);
//				if(!globalMap.containsKey(possibleDest)){
//					System.out.println("Adding new unknown Coordinate: " + possibleDest.toString());
//					destinations.add(possibleDest);
//					break quad3;
//				}
//			}
//		}
//		//look for unknown in fourth quadrant
//		quad4:
//		for(int x = maxX; x >= maxX/2; x++){
//			for(int y = maxY; y >= maxY/2; y++){
//				Coord possibleDest = new Coord(x, y);
//				if(!globalMap.containsKey(possibleDest)){
//					System.out.println("Adding new unknown Coordinate: " + possibleDest.toString());
//					destinations.add(possibleDest);
//					break quad4;
//				}
//			}
//		}
//	}
//	
//	public Coord newTargetLoc() {
//		if (destinations.isEmpty()) {	//if destinations list is empty, add more coordinates		
//			
//			System.out.println(">>>>>>>>DEBUG: Adding more coordinates to destinations list.");
//			updateDestinations();
//		}
////		System.out.println("### New Destination acquired ### " + destinations.peek().toString());
////		return destinations.poll();
//		int min = Integer.MAX_VALUE;
//		int index = 0;
//		Coord current = null;
//		for(int x = 0; x < destinations.size(); x++){
//			current = destinations.get(x);
//			int manHat = manhattanDistance(current);
//			if(manHat < min){
//				index = x;
//				min = manHat;
//				System.out.println("New min dist: " + min + " with coordinates: " + current.toString());
//			}
//		}
//		current = destinations.get(index);
//		destinations.remove(index);
//		System.out.println("returning new target -------> " + current.toString());
//		return current;
//	}
//	
//	private int manhattanDistance(Coord coord){
//		int distance = Math.abs(currentLoc.xpos - coord.xpos) + Math.abs(currentLoc.ypos - coord.ypos);
//		return distance;
//	}
//	
//	//Update min max from each scan
//	public void updateMinMax(Coord current){
//	    MapTile[][] scanMapTiles = scanMap.getScanMap();
//	    int centerRow = (scanMapTiles.length - 1) / 2;
//	    for(int row = 0; row < scanMapTiles.length; row++){
//	        for(int col = 0; col < scanMapTiles.length; col++){
//	            int xPos = findCoordinate(col, current.xpos, centerRow);
//	            int yPos = findCoordinate(row, current.ypos, centerRow);
//	            //look at each tile and update min/max if it's not a "NULL" value
//	            //to make sure we stay inside the map when updating target.
//	            if(scanMapTiles[col][row].getTerrain() == Terrain.SOIL || scanMapTiles[col][row].getTerrain() == Terrain.GRAVEL){
//	                if(xPos > maxX)
//	                    maxX = xPos;
//	                if(yPos > maxY)
//	                    maxY= yPos;
//	            }
//	        }
//	    }
//	}
//
//	public char wander(String line, char dir) {
//		Random rand = new Random();
//		if (steps % 20 == 0) {
//			List<String> dirsCons = new ArrayList<>();
//			char dirOpposite = getOpposite(dir);
//			for (int i = 0; i < cardinals.length; i++) {
//				if (cardinals[i] != dirOpposite) {
//					dirsCons.add(String.valueOf(cardinals[i]));
//				}
//			}
//			dir = dirsCons.get(rand.nextInt(3)).charAt(0);
//		}
//		steps++;
//		MapTile[][] scanMapTiles = scanMap.getScanMap();
//		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
//		System.out.println(dir);
//		switch (dir) {
//		case 'N':
//			if (northBlocked(scanMapTiles, centerIndex)) {
//				dir = resolveNorth(scanMapTiles, centerIndex);
//			}
//			break;
//		case 'S':
//			if (southBlocked(scanMapTiles, centerIndex)) {
//				dir = resolveSouth(scanMapTiles, centerIndex);
//			}
//			break;
//		case 'E':
//			System.out.println("E");
//			if (eastBlocked(scanMapTiles, centerIndex)) {
//				dir = resolveEast(scanMapTiles, centerIndex);
//			}
//			break;
//		case 'W':
//			System.out.println("W");
//			if (westBlocked(scanMapTiles, centerIndex)) {
//				dir = resolveWest(scanMapTiles, centerIndex);
//			}
//			break;
//		}
//		System.out.println("Going: " + dir);
//		return dir;
//	}
//
//	public char getOpposite(char dir) {
//		char opposite = ' ';
//		switch (dir) {
//		case 'N':
//			opposite = 'S';
//			break;
//		case 'S':
//			opposite = 'N';
//			break;
//		case 'E':
//			opposite = 'W';
//			break;
//		case 'W':
//			opposite = 'E';
//			break;
//		}
//		System.out.println("Opposite of " + dir + " is " + opposite);
//		return opposite;
//	}
//
//	// for north
//	public char resolveNorth(MapTile[][] scanMapTiles, int centerIndex) {
//		String currentDir = "N";
//		if (!eastBlocked(scanMapTiles, centerIndex))
//			currentDir = "E";
//		else if (!westBlocked(scanMapTiles, centerIndex))
//			currentDir = "W";
//		else
//			currentDir = "S";
//		return currentDir.charAt(0);
//	}
//
//	// for south
//	public char resolveSouth(MapTile[][] scanMapTiles, int centerIndex) {
//		String currentDir = "S";
//		if (!westBlocked(scanMapTiles, centerIndex))
//			currentDir = "W";
//		else if (!eastBlocked(scanMapTiles, centerIndex))
//			currentDir = "E";
//		else {
//			currentDir = "N";
//		}
//		return currentDir.charAt(0);
//	}
//
//	// east
//	public char resolveEast(MapTile[][] scanMapTiles, int centerIndex) {
//		String currentDir = "E";
//		if (!southBlocked(scanMapTiles, centerIndex))
//			currentDir = "S";
//		else if (!northBlocked(scanMapTiles, centerIndex))
//			currentDir = "N";
//		else
//			currentDir = "W";
//		return currentDir.charAt(0);
//	}
//
//	// west
//	public char resolveWest(MapTile[][] scanMapTiles, int centerIndex) {
//		String currentDir = "W";
//		if (!northBlocked(scanMapTiles, centerIndex))
//			currentDir = "N";
//		else if (!southBlocked(scanMapTiles, centerIndex))
//			currentDir = "S";
//		else
//			currentDir = "E";
//		return currentDir.charAt(0);
//	}
//
//	// for northblocked
//	public boolean northBlocked(MapTile[][] scanMapTiles, int centerIndex) {
//		return (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
//				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
//				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
//				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND);
//	}
//
//	// for southblocked
//	public boolean southBlocked(MapTile[][] scanMapTiles, int centerIndex) {
//		return (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
//				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
//				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
//				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND);
//	}
//
//	// for eastblocked
//	public boolean eastBlocked(MapTile[][] scanMapTiles, int centerIndex) {
//		return (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
//				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
//				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
//				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND);
//	}
//
//	// for westblocked
//	public boolean westBlocked(MapTile[][] scanMapTiles, int centerIndex) {
//		return (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
//				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
//				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
//				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND);
//	}
//
//	public int getRandom(int length) {
//		Random random = new Random();
//		return random.nextInt(length);
//	}
//
//	/******************************************
//	 * D_STAR STUFF
//	 ****************************************/
//	public void moveDStar(String line, Coord start, Coord target) throws Exception {
//		Coord curTarget = target;
//		dsl = new DStarLite(RoverDriveType.getEnum(equipment.get(0)));
//		boolean stuck = false;
//		boolean blocked = false;// could be velocity limit or obstruction etc.
//		Coord currentLoc = null;
//		Coord previousLoc = null;
//		int stuckCount = 0;
//
//		/**
//		 * #### Rover controller process loop ####
//		 */
//		while (true) {
//			// **** Request Rover Location from SwarmServer ****
//			out.println("LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println(rovername + " check connection to server");
//				line = "";
//			}
//			if (line.startsWith("LOC")) {
//				// loc = line.substring(4);
//				currentLoc = extractLocationFromString(line);
//			}
//			if (initializedDSL)
//				dsl.updateStart(currentLoc);
//			if (!initializedDSL) {
//				dsl.goal_c = target;
//				dsl.start_c = currentLoc;
//				dsl.init(currentLoc, target);
//				dsl.replan();
//				initializedDSL = true;
//			}
//
//			System.out.println(rovername + " currentLoc at start: " + currentLoc);
//
//			previousLoc = currentLoc;
//			// if we've reached our destination, get a new one
//			// for now go to (4, 4) on the map
//			if (currentLoc.equals(curTarget)) {
//				curTarget = new Coord(4, 4);
//				dsl.updateGoal(curTarget);
//			}
//			// ***** do a SCAN *****
//			doScan();
//			// prints the scanMap to the Console output for debug purposes
//			scanMap.debugPrintMap();
//			// ***** get TIMER remaining *****
//			checkTime(line);
//			MapTile[][] scanMapTiles = scanMap.getScanMap();
//			// update/add new mapTiles to dsl hashMaps
//			updateScannedStates(scanMapTiles, currentLoc);
//			// find path from current node to goal
//			dsl.replan();
//			char move = getMoveFromPath(currentLoc);
//			// try to move
//			System.out.println("Requesting to move " + move);
//			out.println("MOVE " + move);
//			Thread.sleep(sleepTime);
//
//			// another call for current location
//			out.println("LOC");
//			line = in.readLine();
//			if (line == null) {
//				System.out.println("ROVER_03 check connection to server");
//				line = "";
//			}
//			if (line.startsWith("LOC")) {
//				currentLoc = extractLocationFromString(line);
//			}
//
//			// test for stuckness - if stuck for too long try switching
//			// positions
//			stuck = currentLoc.equals(previousLoc);
//			if (stuck)
//				stuckCount += 1;
//			else
//				stuckCount = 0;
//			if (stuckCount >= 10)
//				out.println("MOVE " + move);
//
//			System.out.println("ROVER_03 blocked test " + blocked);
//			// this is the Rovers HeartBeat, it regulates how fast the Rover
//			// cycles through the control loop
//			Thread.sleep(sleepTime);
//
//			System.out.println("ROVER_03 ------------ bottom process control --------------");
//		}
//	}
//
//	public void checkTime(String line) throws IOException {
//		out.println("TIMER");
//		line = in.readLine();
//		if (line == null) {
//			System.out.println(rovername + " check connection to server");
//			line = "";
//		}
//		if (line.startsWith("TIMER")) {
//			String timeRemaining = line.substring(6);
//			System.out.println(rovername + " timeRemaining: " + timeRemaining);
//		}
//	}
//
//	/*
//	 * This method feeds maptils from scan to DStarLite object for updating
//	 * states/nodes have to find coordinates for each tile, given that the
//	 * center is current location
//	 */
//	public void updateScannedStates(MapTile[][] tiles, Coord current) {
//		int centerRow = (tiles.length - 1) / 2;
//		int centerCol = (tiles[0].length - 1) / 2;
//		// System.out.println("rows: " + tiles.length + " cols: " +
//		// tiles[0].length + " centers: " + centerRow);
//		for (int row = 0; row < tiles.length; row++) {
//			for (int col = 0; col < tiles[0].length; col++) {
//				int xPos = findCoordinate(col, current.xpos, centerCol);
//				int yPos = findCoordinate(row, current.ypos, centerRow);
//				Coord newCoord = new Coord(xPos, yPos);
//				// updateCell also adds new cells if they're not already in
//				// tables
//				if (newCoord.equals(current))
//					continue;
//				dsl.updateCell(newCoord, tiles[col][row]);
//			}
//		}
//		System.out.println("Updated neighbors to center");
//		dsl.scanElem += 4;
//	}
//
//	/*
//	 * Given the scan map, it finds the coordinates of each tile relative to
//	 * rover's position - for updating States.
//	 */
//	public int findCoordinate(int n, int pivot, int centerIndex) {
//		int pos;
//		int diff = Math.abs(n - centerIndex);
//		if (n > centerIndex)
//			pos = pivot + diff;
//		else if (n < centerIndex)
//			pos = pivot - diff;
//		else
//			pos = pivot;
//		return pos;
//	}
//
//	/*
//	 * Gets 2nd element from the path, first is current position. returns the
//	 * cardinal index to move to that position.
//	 */
//	public char getMoveFromPath(Coord current) {
//		int index = 0;
//		State nextState;
//		while (true) {
//			if (current.equals(dsl.getPath().get(index).getCoord())) {
//				index++;
//				nextState = dsl.getPath().get(index);
//				System.out.println("Using index: " + index);
//				break;
//			} else {
//				index++;
//			}
//		}
//		int newX = nextState.getCoord().xpos;
//		int newY = nextState.getCoord().ypos;
//		if (newX > current.xpos)
//			return cardinals[1];
//		else if (newX < current.xpos)
//			return cardinals[3];
//		else if (newY > current.ypos)
//			return cardinals[2];
//		else
//			return cardinals[0];
//	}
//
//	/**********
//	 * End D_Star
//	 ********/
//
//	// ####################### Support Methods #############################
//	private void clearReadLineBuffer() throws IOException {
//		while (in.ready()) {
//			// System.out.println("ROVER_03 clearing readLine()");
//			in.readLine();
//		}
//	}
//	
//	//clean destinations
//	public void cleanDestinations(){
//		Iterator<Coord> iter = destinations.iterator();
//		while(iter.hasNext()){
//			MapTile tile = globalMap.get(iter.next());
//			//if it's not in globalMap yet, keep it
//			if(tile == null ){
//				continue;
//			}else{
//				//if it is in globalMap, discard it, unless it has science
//				if(tile.getScience() == Science.NONE )
//					iter.remove();
//			}
//		}
//	}
//	
//	//borrowed from aStar
//	private boolean isBlocked(Terrain ter){
//		RoverDriveType driveType = RoverDriveType.getEnum(equipment.get(0));			
//        if(ter == Terrain.NONE) {
//            return true;
//        }
//        if(ter == Terrain.SAND && driveType != RoverDriveType.TREADS) {
//            return true;
//        }
//        if(ter == Terrain.ROCK && driveType != RoverDriveType.WALKER) {
//            return true;
//        }
//        return false;
//	}
//
//	/**************************
//	 * Communications Functions
//	 ***************************/
//	// get data from server and update field map
//	private void updateglobalMap(JSONArray data) {
//
//		for (Object o : data) {
//
//			JSONObject jsonObj = (JSONObject) o;
//			boolean marked = (jsonObj.get("g") != null) ? true : false;
//			int x = (int) (long) jsonObj.get("x");
//			int y = (int) (long) jsonObj.get("y");
//			Coord coord = new Coord(x, y);
//
//			// only bother to save if our globalMap doesn't contain the
//			// coordinate
//			MapTile tile = CommunicationHelper.convertToMapTile(jsonObj);
//			if (!globalMap.containsKey(coord)) {
//				globalMap.put(coord, tile);	
//			}
//			if (tile.getScience() != Science.NONE && !isBlocked(tile.getTerrain())) {
//				// then add to the destination
//				if (!destinations.contains(coord) && !marked){
//					System.out.println("#####Added new destination for gathering: " + coord.toString());
//					comms.markTileForGather(coord);
//					destinations.add(coord);
//				}
//			}
//		}
//		//System.out.println("map continats " + globalMap.size() + " tiles!");
//	}
//
//	// method to retrieve a list of the rover's EQUIPMENT from the server
//	private ArrayList<String> getEquipment() throws IOException {
//		// System.out.println("ROVER_03 method getEquipment()");
//		Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
//		out.println("EQUIPMENT");
//
//		String jsonEqListIn = in.readLine(); // grabs the string that was
//												// returned first
//		if (jsonEqListIn == null) {
//			jsonEqListIn = "";
//		}
//		StringBuilder jsonEqList = new StringBuilder();
//		// System.out.println("ROVER_03 incomming EQUIPMENT result - first
//		// readline: " + jsonEqListIn);
//
//		if (jsonEqListIn.startsWith("EQUIPMENT")) {
//			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
//				jsonEqList.append(jsonEqListIn);
//				jsonEqList.append("\n");
//				// System.out.println("ROVER_03 doScan() bottom of while");
//			}
//		} else {
//			// in case the server call gives unexpected results
//			clearReadLineBuffer();
//			return null; // server response did not start with "EQUIPMENT"
//		}
//
//		String jsonEqListString = jsonEqList.toString();
//		ArrayList<String> returnList;
//		returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>() {
//		}.getType());
//		// System.out.println("ROVER_03 returnList " + returnList);
//
//		return returnList;
//	}
//
//	// sends a SCAN request to the server and puts the result in the scanMap
//	// array
//	public void doScan() throws IOException {
//		// System.out.println("ROVER_03 method doScan()");
//		Gson gson = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create();
//		out.println("SCAN");
//
//		String jsonScanMapIn = in.readLine(); // grabs the string that was
//												// returned first
//		if (jsonScanMapIn == null) {
//			System.out.println("ROVER_03 check connection to server");
//			jsonScanMapIn = "";
//		}
//		StringBuilder jsonScanMap = new StringBuilder();
//		System.out.println("ROVER_03 incomming SCAN result - first readline: " + jsonScanMapIn);
//
//		if (jsonScanMapIn.startsWith("SCAN")) {
//			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
//				// System.out.println("ROVER_03 incomming SCAN result: " +
//				// jsonScanMapIn);
//				jsonScanMap.append(jsonScanMapIn);
//				jsonScanMap.append("\n");
//				// System.out.println("ROVER_03 doScan() bottom of while");
//			}
//		} else {
//			// in case the server call gives unexpected results
//			clearReadLineBuffer();
//			return; // server response did not start with "SCAN"
//		}
//		// System.out.println("ROVER_03 finished scan while");
//		String jsonScanMapString = jsonScanMap.toString();
//		// convert from the json string back to a ScanMap object
//		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
//	}
//
//	// this takes the server response string, parses out the x and x values and
//	// returns a Coord object
//	public static Coord extractLocationFromString(String sStr) {
//		int indexOf;
//		indexOf = sStr.indexOf(" ");
//		sStr = sStr.substring(indexOf + 1);
//		if (sStr.lastIndexOf(" ") != -1) {
//			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
//			// System.out.println("extracted xStr " + xStr);
//
//			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
//			// System.out.println("extracted yStr " + yStr);
//			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
//		}
//		return null;
//	}
//
//	/**
//	 * Runs the client
//	 */
//	public static void main(String[] args) throws Exception {
//
//		ROVER_13 client;
//		// if a command line argument is included it is used as the map filename
//		// if present uses an IP address instead of localhost
//
//		if (!(args.length == 0)) {
//			client = new ROVER_13(args[0]);
//		} else {
//			client = new ROVER_13();
//		}
//
//		client.run();
//	}
//	
//}