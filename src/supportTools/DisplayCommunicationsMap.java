package supportTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import common.Communication;
import common.CommunicationHelper;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;

public class DisplayCommunicationsMap {


	
	private Map<Coord, MapTile> globalMap = new HashMap<>();
	private MapTile[][] globalMapArray;
	private String url;
	private String corp_secret;		
	private String rovername;
	Communication com = new Communication(url, rovername, corp_secret);
	
	JSONArray globalMapJSON = new JSONArray();
	
	DisplayCommunicationsMap(String url, String corp_secret, String rovername){
		this.url = "http://localhost:3000/api";
		this.corp_secret = "gz5YhL70a2";		
		this.rovername = "ROVER_02";
		com = new Communication(url, rovername, corp_secret);
		
	}
	
    private void printGlobalMap(JSONArray data) {
    	int maxX = 0;
    	int minX = 0;
    	int maxY = 0;
    	int minY = 0;
    	MapTile tempTile;
    	globalMap.clear();
    	
        for (Object o : data) {
            JSONObject jsonObj = (JSONObject) o;
            boolean marked = (jsonObj.get("g") != null) ? true : false;
            int x = (int) (long) jsonObj.get("x");
            int y = (int) (long) jsonObj.get("y");
            Coord coord = new Coord(x, y);
            
            if(x < minX)
            	minX = x;
            if(x > maxX)
            	maxX = x;
            if(y < minY)
            	minY = y;
            if(y > maxY)
            	maxY = y;
            
            MapTile tile = CommunicationHelper.convertToMapTile(jsonObj);
            globalMap.put(coord, tile); 
        }
        
        System.out.println("minX= " + minX + "  maxX= " + maxX);
        System.out.println("minY= " + minY + "  maxY= " + maxY);
        
        for(int j = minY -1; j <= maxY +1; j++){
        	for(int i = minX -1; i <=maxX +1; i++){
        		if(i == minX-1){
        			System.out.print("|");
        		} else if(i == maxX+1){
        			System.out.println("|");
        		} else if(j == minY-1 || j == maxY+1){
        			System.out.print("--");
        		} else {
 		
	        		tempTile = globalMap.get(new Coord(i, j));
	        		
	        		if(tempTile == null){
	        			System.out.print("~ ");
	        		 
	        			//check and print edge of map has first priority
	        		} else if(tempTile.getTerrain().toString().equals("NONE")){
	    					System.out.print("XX");
	    					
    				// next most important - print terrain and/or science locations
    					//terrain and science
    				} else if(!(tempTile.getTerrain().toString().equals("SOIL"))
    						&& !(tempTile.getScience().toString().equals("NONE"))){
    					// has both terrain and science
    					
    					System.out.print(tempTile.getTerrain().toString().substring(0,1) + tempTile.getScience().getSciString());
    					//just terrain
    				} else if(!(tempTile.getTerrain().toString().equals("SOIL"))){
    					System.out.print(tempTile.getTerrain().toString().substring(0,1) + " ");
    					//just science
    				} else if(!(tempTile.getScience().toString().equals("NONE"))){
    					System.out.print(" " + tempTile.getScience().getSciString());
    					
    				// if still empty check for rovers and print them
    				} else if(tempTile.getHasRover()){
    					System.out.print("[]");
    				} else {
    					System.out.print("  ");	
	        		}
        		}
        	}
        }
        
    }
    
    public void displayGlobalMap(){
    	//System.out.println("global map output here");
    	globalMapJSON = com.getGlobalMap();
    	//System.out.println(globalMapJSON);
    	printGlobalMap(globalMapJSON);	
    }
    
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String url = "http://localhost:3000/api";
		String corp_secret = "gz5YhL70a2";		
		String rovername = "ROVER_02";
		
		DisplayCommunicationsMap dcm = new DisplayCommunicationsMap(url, corp_secret, rovername);
		
		while (true){
			System.out.println("displaying global map");
			dcm.displayGlobalMap();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
