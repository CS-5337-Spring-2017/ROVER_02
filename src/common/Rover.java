package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import enums.RoverConfiguration;
import enums.RoverDriveType;
import enums.Terrain;

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

    public Coord startLocation = null;

    public Coord targetLocation = null;

    public ArrayList<String> equipment = new ArrayList<String>();

    // Hardcoded port number for the CS-5337 class
    protected static final int PORT_ADDRESS = 9537;

    // TODO add code to the move methods to check for impassable terrain
    protected void moveNorth() {

        sendTo_RCP.println( "MOVE N" );
    }

    protected void moveSouth() {

        sendTo_RCP.println( "MOVE S" );
    }

    protected void moveEast() {

        sendTo_RCP.println( "MOVE E" );
    }

    protected void moveWest() {

        sendTo_RCP.println( "MOVE W" );
    }

    protected Coord getStartLocation() throws IOException {

        String line = null;
        sendTo_RCP.println( "START_LOC" );
        line = receiveFrom_RCP.readLine();
        if ( line == null ) {
            System.out.println( rovername + " check connection to server" );
            line = "";
        }
        if ( line.startsWith(
            "START_LOC" ) ) { return extractLocationFromString( line ); }
        return null;
    }

    protected Coord getTargetLocation() throws IOException {

        String line = null;
        sendTo_RCP.println( "TARGET_LOC" );
        line = receiveFrom_RCP.readLine();
        if ( line == null ) {
            System.out.println( rovername + " check connection to server" );
            line = "";
        }
        if ( line.startsWith(
            "TARGET_LOC" ) ) { return extractLocationFromString( line ); }
        return null;
    }

    protected Coord getCurrentLocation() throws IOException {

        String line = null;
        sendTo_RCP.println( "LOC" );
        line = receiveFrom_RCP.readLine();
        if ( line == null ) {
            System.out.println( "ROVER_00 check connection to server" );
            line = "";
        }
        if ( line
            .startsWith( "LOC" ) ) { return extractLocationFromString( line ); }
        return null;
    }

    protected void clearReadLineBuffer() throws IOException {

        while ( receiveFrom_RCP.ready() ) {
            receiveFrom_RCP.readLine();
        }
    }

    protected String getTimeRemaining() throws IOException {

        String line;
        String timeRemaining = null;
        sendTo_RCP.println( "TIMER" );
        line = receiveFrom_RCP.readLine();
        if ( line == null ) {
            System.out.println( rovername + " check connection to server" );
            line = "";
        }
        if ( line.startsWith( "TIMER" ) ) {
            timeRemaining = line.substring( 6 );
            System.out
                .println( rovername + " timeRemaining: " + timeRemaining );
        }
        return timeRemaining;
    }

    // method to retrieve a list of the rover's EQUIPMENT from the server
    protected ArrayList<String> getEquipment() throws IOException {

        Gson gson = new GsonBuilder().setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();
        sendTo_RCP.println( "EQUIPMENT" );

        String jsonEqListIn = receiveFrom_RCP.readLine(); // grabs the string
                                                          // that was returned
                                                          // first
        if ( jsonEqListIn == null ) {
            jsonEqListIn = "";
        }
        StringBuilder jsonEqList = new StringBuilder();

        if ( jsonEqListIn.startsWith( "EQUIPMENT" ) ) {
            while ( !(jsonEqListIn = receiveFrom_RCP.readLine())
                .equals( "EQUIPMENT_END" ) ) {
                if ( jsonEqListIn == null ) {
                    break;
                }
                jsonEqList.append( jsonEqListIn );
                jsonEqList.append( "\n" );
            }
        } else {
            // in case the server call gives unexpected results
            clearReadLineBuffer();
            return null; // server response did not start with "EQUIPMENT"
        }

        String jsonEqListString = jsonEqList.toString();
        ArrayList<String> returnList;
        returnList = gson.fromJson( jsonEqListString,
            new TypeToken<ArrayList<String>>() {
            }.getType() );
        return returnList;
    }

    // sends a SCAN request to the server and puts the result in the scanMap
    // array
    protected ScanMap doScan() throws IOException {

        Gson gson = new GsonBuilder().setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .create();
        sendTo_RCP.println( "SCAN" );

        String jsonScanMapIn = receiveFrom_RCP.readLine(); // grabs the string
                                                           // that was returned
                                                           // first
        if ( jsonScanMapIn == null ) {
            System.out.println( "ROVER_00 check connection to server" );
            jsonScanMapIn = "";
        }
        StringBuilder jsonScanMap = new StringBuilder();
        System.out.println( "ROVER_00 incomming SCAN result - first readline: "
            + jsonScanMapIn );

        if ( jsonScanMapIn.startsWith( "SCAN" ) ) {
            while ( !(jsonScanMapIn = receiveFrom_RCP.readLine())
                .equals( "SCAN_END" ) ) {
                jsonScanMap.append( jsonScanMapIn );
                jsonScanMap.append( "\n" );
            }
        } else {
            // in case the server call gives unexpected results
            clearReadLineBuffer();
            return null; // server response did not start with "SCAN"
        }

        String jsonScanMapString = jsonScanMap.toString();
        // convert from the json string back to a ScanMap object
        return gson.fromJson( jsonScanMapString, ScanMap.class );
    }

    // this takes the server response string, parses out the x and x values and
    // returns a Coord object
    protected static Coord extractLocationFromString( String sStr ) {

        int indexOf;
        indexOf = sStr.indexOf( " " );
        sStr = sStr.substring( indexOf + 1 );
        if ( sStr.lastIndexOf( " " ) != -1 ) {
            String xStr = sStr.substring( 0, sStr.lastIndexOf( " " ) );
            String yStr = sStr.substring( sStr.lastIndexOf( " " ) + 1 );
            return new Coord( Integer.parseInt( xStr ),
                Integer.parseInt( yStr ) );
        }
        return null;
    }

    // Added by ROVER03 team
    protected ScienceDetail analyzeAndGetSuitableScience() {

        ScienceDetail minDistanceScienceDetail = null;
        try {
            Communication communication = new Communication(
                "http://localhost:3000/api", rovername, "open_secret" );

            ScienceDetail[] scienceDetails = communication
                .getAllScienceDetails();
            RoverDetail[] roverDetails = communication.getAllRoverDetails();

            if ( roverDetails == null || roverDetails.length == 0 ) {
                if ( scienceDetails != null
                    && scienceDetails.length > 0 ) { return analyzeAndGetSuitableScienceForCurrentRover(
                        scienceDetails ); }
            }

            if ( roverDetails != null && scienceDetails != null ) {
                Map<ScienceDetail, SortedMap<Integer, RoverDetail>> scienceToDistanceSortedRoverMap = getScienceToDistanceSortedRoverMap(
                    roverDetails, scienceDetails );
                System.out.println( "scienceToDistanceSortedRoverMap = "
                    + scienceToDistanceSortedRoverMap );
                for ( ScienceDetail scienceDetail : scienceDetails ) {
                    if ( !scienceToDistanceSortedRoverMap.get( scienceDetail )
                        .isEmpty() ) {
                        // Choose science where this rover is the most nearest,
                        // else
                        // do not gather any science since there are other
                        // rovers
                        // that are most nearest to all sciences.
                        Integer firstKey = scienceToDistanceSortedRoverMap
                            .get( scienceDetail ).firstKey();
                        if ( rovername.equals(
                            scienceToDistanceSortedRoverMap.get( scienceDetail )
                                .get( firstKey )
                                .getRoverName() ) ) {
                            minDistanceScienceDetail = scienceDetail;
                            break;
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
        return minDistanceScienceDetail;
    }

    private Map<ScienceDetail, SortedMap<Integer, RoverDetail>> getScienceToDistanceSortedRoverMap(
        RoverDetail[] roverDetails, ScienceDetail[] scienceDetails ) {

        Map<ScienceDetail, SortedMap<Integer, RoverDetail>> scienceToDistanceSortedRoverMap = new HashMap<>();
        try {
            if ( scienceDetails != null ) {
                for ( ScienceDetail scienceDetail : scienceDetails ) {
                    scienceToDistanceSortedRoverMap.put( scienceDetail,
                        new TreeMap<>() );
                    if ( roverDetails != null ) {
                        for ( RoverDetail roverDetail : roverDetails ) {
                            // If not yet gathered by other rover
                            if ( scienceDetail.getGatheredByRover() == -1 ) {
                                RoverConfiguration curRoverConfig = RoverConfiguration
                                    .valueOf( roverDetail.getRoverName() );
                                // Choose science terrain based on current rover
                                // drive type
                                if ( RoverDriveType.TREADS.name().equals(
                                    curRoverConfig.getMembers().get( 0 ) ) ) {
                                    if ( scienceDetail
                                        .getTerrain() != Terrain.ROCK
                                        && scienceDetail
                                            .getTerrain() != Terrain.FLUID ) {
                                        int distance = calculateDistance(
                                            roverDetail.getX(),
                                            roverDetail.getY(), scienceDetail );
                                        // TODO: Need another check on tools
                                        // before
                                        // distance
                                        scienceToDistanceSortedRoverMap
                                            .get( scienceDetail )
                                            .put( distance, roverDetail );
                                    }
                                } else if ( RoverDriveType.WHEELS.name().equals(
                                    curRoverConfig.getMembers().get( 0 ) ) ) {
                                    if ( scienceDetail
                                        .getTerrain() != Terrain.SAND
                                        && scienceDetail
                                            .getTerrain() != Terrain.SOIL
                                        && scienceDetail
                                            .getTerrain() != Terrain.FLUID
                                        && scienceDetail
                                            .getTerrain() != Terrain.GRAVEL ) {
                                        int distance = calculateDistance(
                                            roverDetail.getX(),
                                            roverDetail.getY(), scienceDetail );
                                        // TODO: Need another check on tools
                                        // before
                                        // distance
                                        scienceToDistanceSortedRoverMap
                                            .get( scienceDetail )
                                            .put( distance, roverDetail );
                                    }
                                } else if ( RoverDriveType.WALKER.name().equals(
                                    curRoverConfig.getMembers().get( 0 ) ) ) {
                                    if ( scienceDetail
                                        .getTerrain() != Terrain.FLUID ) {
                                        int distance = calculateDistance(
                                            roverDetail.getX(),
                                            roverDetail.getY(), scienceDetail );
                                        // TODO: Need another check on tools
                                        // before
                                        // distance
                                        scienceToDistanceSortedRoverMap
                                            .get( scienceDetail )
                                            .put( distance, roverDetail );
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
        return scienceToDistanceSortedRoverMap;
    }

    protected ScienceDetail analyzeAndGetSuitableScienceForCurrentRover(
        ScienceDetail[] scienceDetails ) {

        int minDistance = Integer.MAX_VALUE;
        ScienceDetail minDistanceScienceDetail = null;
        try {
            for ( ScienceDetail scienceDetail : scienceDetails ) {
                // If not yet gathered by other rover
                if ( scienceDetail.getGatheredByRover() == -1 ) {
                    RoverConfiguration curRoverConfig = RoverConfiguration
                        .valueOf( rovername );
                    // Choose science terrain based on current rover drive
                    // type
                    if ( RoverDriveType.TREADS.name()
                        .equals( curRoverConfig.getMembers().get( 0 ) ) ) {
                        if ( scienceDetail.getTerrain() != Terrain.ROCK
                            && scienceDetail.getTerrain() != Terrain.FLUID ) {
                            int distance = calculateDistance(
                                getCurrentLocation().xpos,
                                getCurrentLocation().ypos, scienceDetail );
                            // TODO: Need another check on tools before
                            // distance
                            if ( distance < minDistance ) {
                                minDistance = distance;
                                minDistanceScienceDetail = scienceDetail;
                            }
                        }
                    } else if ( RoverDriveType.WALKER.name()
                        .equals( curRoverConfig.getMembers().get( 0 ) ) ) {
                        if ( scienceDetail.getTerrain() != Terrain.FLUID ) {
                            int distance = calculateDistance(
                                getCurrentLocation().xpos,
                                getCurrentLocation().ypos, scienceDetail );
                            // TODO: Need another check on tools before
                            // distance
                            if ( distance < minDistance ) {
                                minDistance = distance;
                                minDistanceScienceDetail = scienceDetail;
                            }
                        }
                    } else if ( RoverDriveType.WHEELS.name()
                        .equals( curRoverConfig.getMembers().get( 0 ) ) ) {
                        if ( scienceDetail.getTerrain() != Terrain.SAND
                            && scienceDetail.getTerrain() != Terrain.ROCK
                            && scienceDetail.getTerrain() != Terrain.FLUID
                            && scienceDetail.getTerrain() != Terrain.GRAVEL ) {
                            int distance = calculateDistance(
                                getCurrentLocation().xpos,
                                getCurrentLocation().ypos, scienceDetail );
                            // TODO: Need another check on tools before
                            // distance
                            if ( distance < minDistance ) {
                                minDistance = distance;
                                minDistanceScienceDetail = scienceDetail;
                            }
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
        return minDistanceScienceDetail;
    }

    private int calculateDistance( int x, int y, ScienceDetail scienceDetail ) {

        int xDis = Math.abs( x - scienceDetail.getX() );
        int yDis = Math.abs( y - scienceDetail.getY() );
        int distance = (int) Math.sqrt( Math.abs( xDis * xDis - yDis * yDis ) );
        return distance;
    }

    protected void postScanMapTiles( Coord currentLoc,
        MapTile[][] scanMapTiles ) {

        try {
            Communication communication = new Communication(
                "http://localhost:3000/api", rovername, "open_secret" );
            communication.postScanMapTiles( currentLoc, scanMapTiles );
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
    }

    protected void sendRoverDetail( RoverDetail roverDetail ) {

        try {
            Communication communication = new Communication(
                "http://localhost:3000/api", rovername, "open_secret" );
            communication.sendRoverDetail( roverDetail );
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
    }

    protected RoverDetail[] getAllRoverDetails() {

        try {
            Communication communication = new Communication(
                "http://localhost:3000/api", rovername, "open_secret" );
            return communication.getAllRoverDetails();
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
        return null;
    }

    protected void gatherScience( Coord coord ) {

        try {
            Communication communication = new Communication(
                "http://localhost:3000/api", rovername, "open_secret" );
            communication.markScienceForGather( coord );
        } catch ( Exception e ) {
            System.err.println(
                "Communication server communication failed with error: "
                    + e.getClass() + ": " + e.getMessage() );
        }
    }
}