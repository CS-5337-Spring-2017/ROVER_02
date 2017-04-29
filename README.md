# Rover Swarm Project - Rover 02

This project is a simulation of a set of autonomous rovers that are exploring, mapping, and harvesting science on an alien planet. The rovers have very limited capabilities individually and therefore have to operate as a swarm to fulfill their objectives.

![image of mars rovers](http://i.imgur.com/8n6arMu.jpg)

## Group-2

**1.What are the movement commands?  What are the scan commands?**

The basic movement commands are the `moveNorth()`,`moveEast()`,`moveSouth()` and `moveWest()`. These methods are called from `Rover.java` which prints a statement with help of a [PrintWriter](https://docs.oracle.com/javase/7/docs/api/java/io/PrintWriter.html) object.

This is how the `moveNorth()` method is implemented:

    	protected void moveNorth() {
		    sendTo_RCP.println("MOVE N");
	    }

  All the Rovers are inside the (swarmBots) package inside the project files. All the implementation for each rover will be inside this package. Each rover will have different way to move based on the purpose and the tools of each Rover.

  ![image of swarmBots directory](http://i.imgur.com/LCKuaw4.png)

  When the rover’s tools and abilities have been assigned, new movement for the rover or further improvement on it can be done. Rover-02, for instance, has these pre-assigned abilities and tools:

  **Rover02: (The Walker)**

  1.Walkers move much slower than Wheels and a little slower than Treads.

  2.Walkers can travel over Soil, Gravel, and Rocks.

  3.Walkers will immediately get stuck upon entering Sand terrain.

  4.SPECTRAL_SENSOR -> Crystal Science

  Spectral sensor four is exclusive to Rover-02's, other rovers can’t catch Crystal Science on the map. Crystal objects can be detected by finding the letter ( C ) on the raw map in the project folder in eclipse.


The movement of the rover is indicated by four letters as East, West, North, South as the following:

            String[] cardinals = new String[4];
  			cardinals[0] = "N";
  			cardinals[1] = "E";
  			cardinals[2] = "S";
  			cardinals[3] = "W";

  To move the rover to any of these directions, a simple call to one of the functions corresponding to that direction would suffice. This class has all the four function which each function will send the indicated letter matched the movement to the server to move your rover.

  Each rover is designed with a unique movement algorithm. This algorithm make sure that the rover knows where to go before moving. For example, the function below is making sure that the rover is checking whether the next move is allowed or not:

          if (scanMapTiles[centerIndex +1][centerIndex].getHasRover()
              || scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.SAND
              || scanMapTiles[centerIndex +1][centerIndex].getTerrain() == Terrain.NONE) {
            System.out.println(">>>>>>>EAST BLOCKED<<<<<<<<");
            blocked = true;
            stepCount = 5;  //side stepping
          } else {
            moveEast();		

  ROVER-02 is not allowed to step over sand and over another rover. These conditions are also pre-assigned to each rover. However, since the rover is checking each move, the rover has to communicate with the communication server to send whether this move is allowed or not, if allowed, then move. If this move is not allowed, then based on algorithm that this rover has, get a different direction move and check.

  All these commands and communication is inside the while loop which makes the rover keep going unless the rover is stuck somewhere.

The `ScanMap.java` is the main controller responsible for scanning the entire map. This program contains a scanArray, size and the coordinates as the parameters which is initialized to null if the rover is at the start position.

```
public ScanMap(){
  this.scanArray = null;
  this.edgeSize = 0;
  this.centerPoint = null;		
}

public ScanMap(MapTile[][] scanArray, int size, Coord centerPoint){
  this.scanArray = scanArray;
  this.edgeSize = size;
  this.centerPoint = centerPoint;		
}
```
As it moves it stores details of the coordinate in a `scanArray` which is used to get the details of a particular coordinate. Another purpose of this program is that, this is input for creating the map by the rover, as the details of the coordinates are stored in an array. This helps to create the map which can be used to share the information with the communication server as well other rovers.

**3.What are the communication commands?**

The `communication.java` contains the required methods for communicating with the server as well as other rovers. This program contains code for getting the details of the rover as in what are the features it contains, the coordinate location of the rover it is approaching, the science details. The point to be noted is that, this communication will perform these activities for communicating with other rovers as well as to the server.

**4.How are the pathfinding classes used?**

pending

**5.What are some design approaches to be considered for mapping behavior and harvesting behavior and when/how to switch from one to the other? Also, what are some approaches to
not getting the rovers stuck in a corner?**

From doc

**6.What equipment is available to a rover and how is it configured?**

From doc

**7.Make some recommendations on how to improve the implementation of the project. Make some recommendations on additional features and functions to add to the simulation such as, liquid terrain features, hex vs. square map tiles, power limitations (solar, battery, etc.), towing, chance of break downs, etc**

From doc
