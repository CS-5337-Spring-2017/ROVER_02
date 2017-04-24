package common;

import java.io.BufferedReader;
import java.io.DataInputStream;
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

import enums.RoverDriveType;
import enums.RoverMode;
import enums.RoverToolType;
import enums.Science;
import enums.Terrain;

/**
 * Created by samskim on 5/12/16.
 * 
 * This class is the interface to the SwarmCommunicationServer nodejs/javascript
 * program
 */
public class Communication {

	private String url;
	JSONParser parser;
	private String rovername;
	private String corp_secret;

	public Communication(String url, String rovername, String corp_secret) {
		this.url = url;
		this.parser = new JSONParser();
		this.rovername = rovername;
		this.corp_secret = corp_secret;

	}

	@SuppressWarnings("unchecked")
	public void sendRoverDetail(RoverDetail roverDetail) throws IOException {
		if (roverDetail == null) {
			throw new NullPointerException("roverDetail is null");
		}
		JSONObject roverDetailMsg = new JSONObject();
		roverDetailMsg.put("roverName", roverDetail.getRoverName());
		roverDetailMsg.put("x", roverDetail.getX());
		roverDetailMsg.put("y", roverDetail.getY());
		if (roverDetail.getDriveType() != null) {
			roverDetailMsg.put("driveType", roverDetail.getDriveType().name());
		}
		if (roverDetail.getRoverMode() != null) {
			roverDetailMsg.put("roverMode", roverDetail.getRoverMode().name());
		}
		if (roverDetail.getToolType1() != null) {
			roverDetailMsg.put("toolType1", roverDetail.getToolType1().name());
		}
		if (roverDetail.getToolType2() != null) {
			roverDetailMsg.put("toolType2", roverDetail.getToolType2().name());
		}
		roverDetailMsg.put("targetX", roverDetail.getTargetX());
		roverDetailMsg.put("targetY", roverDetail.getTargetY());

		sendRoverDetailJSONDataToServer(roverDetailMsg);
	}

	private void sendRoverDetailJSONDataToServer(JSONObject jsonObject) throws IOException, IOException {
		System.out.println("\nSending 'POST' request to URL : " + url + "/rover/detail");
		HttpURLConnection con = (HttpURLConnection) new URL(url + "/rover/detail").openConnection();

		// add request header
		con.setDoOutput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Rover-Name", rovername);
		con.setRequestProperty("Content-Type", "application/json");

		byte[] jsonBytes = jsonObject.toString().getBytes("UTF-8");

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.write(jsonBytes);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		con.disconnect();
	}

	public RoverDetail[] getAllRoverDetails() throws IOException {
		JSONObject jsonObj = readRoverDetailJSONDataFromServer();

		List<RoverDetail> roverDetails = new ArrayList<>();
		for (Object roverName : jsonObj.keySet()) {
			JSONObject roverDetailJson = (JSONObject) jsonObj.get(roverName);
			RoverDetail roverDetail = new RoverDetail();
			if (roverDetailJson.get("roverName") != null) {
				roverDetail.setRoverName((String) roverDetailJson.get("roverName"));
			}
			if (roverDetailJson.get("x") != null) {
				roverDetail.setX(((Long) roverDetailJson.get("x")).intValue());
			}
			if (roverDetailJson.get("y") != null) {
				roverDetail.setY(((Long) roverDetailJson.get("y")).intValue());
			}
			if (roverDetailJson.get("driveType") != null) {
				roverDetail.setDriveType(RoverDriveType.valueOf((String) roverDetailJson.get("driveType")));
			}
			if (roverDetailJson.get("roverMode") != null) {
				roverDetail.setRoverMode(RoverMode.valueOf((String) roverDetailJson.get("roverMode")));
			}
			if (roverDetailJson.get("toolType1") != null) {
				roverDetail.setToolType1(RoverToolType.valueOf((String) roverDetailJson.get("toolType1")));
			}
			if (roverDetailJson.get("toolType2") != null) {
				roverDetail.setToolType2(RoverToolType.valueOf((String) roverDetailJson.get("toolType2")));
			}
			if (roverDetailJson.get("targetX") != null) {
				roverDetail.setTargetX(((Long) roverDetailJson.get("targetX")).intValue());
			}
			if (roverDetailJson.get("y") != null) {
				roverDetail.setTargetY(((Long) roverDetailJson.get("targetY")).intValue());
			}
			roverDetails.add(roverDetail);
		}

		return roverDetails.toArray(new RoverDetail[roverDetails.size()]);
	}

	private JSONObject readRoverDetailJSONDataFromServer() throws IOException, IOException {
		System.out.println("Sending 'GET' request to URL : " + url + "/rover/detail/all");
		HttpURLConnection con = (HttpURLConnection) new URL(url + "/rover/detail/all").openConnection();

		// add request header
		con.setDoOutput(false);
		con.setRequestMethod("GET");
		con.setRequestProperty("Rover-Name", rovername);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");

		DataInputStream in = new DataInputStream(con.getInputStream());
		StringBuffer jsonBuf = new StringBuffer();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			jsonBuf.append(new String(buffer, 0, bytesRead));
		}

		JSONObject jsonObj = convertToJsonObject(jsonBuf.toString());

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		con.disconnect();

		return jsonObj;
	}

	public ScienceDetail[] getAllScienceDetails() throws IOException {
		JSONArray jsonArray = readScienceDetailJSONDataFromServer();

		List<ScienceDetail> scienceDetails = new ArrayList<>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject roverDetailJson = (JSONObject) jsonArray.get(i);
			ScienceDetail scienceDetail = new ScienceDetail();
			if (roverDetailJson.get("x") != null) {
				scienceDetail.setX(((Long) roverDetailJson.get("x")).intValue());
			}
			if (roverDetailJson.get("y") != null) {
				scienceDetail.setY(((Long) roverDetailJson.get("y")).intValue());
			}
			if (roverDetailJson.get("hasrover") != null) {
				scienceDetail.setHasRover(((Boolean) roverDetailJson.get("hasrover")).booleanValue());
			}
			if (roverDetailJson.get("science") != null) {
				scienceDetail.setScience(Science.valueOf((String) roverDetailJson.get("science")));
			}
			if (roverDetailJson.get("terrain") != null) {
				scienceDetail.setTerrain(Terrain.valueOf((String) roverDetailJson.get("terrain")));
			}
			if (roverDetailJson.get("f") != null) {
				scienceDetail.setFoundByRover(((Long) roverDetailJson.get("f")).intValue());
			}
			if (roverDetailJson.get("g") != null) {
				scienceDetail.setGatheredByRover(((Long) roverDetailJson.get("g")).intValue());
			}

			scienceDetails.add(scienceDetail);
		}

		return scienceDetails.toArray(new ScienceDetail[scienceDetails.size()]);
	}

	private JSONArray readScienceDetailJSONDataFromServer() throws IOException, IOException {
		System.out.println("Sending 'GET' request to URL : " + url + "/science/all");
		HttpURLConnection con = (HttpURLConnection) new URL(url + "/science/all").openConnection();

		// add request header
		con.setDoOutput(false);
		con.setRequestMethod("GET");
		con.setRequestProperty("Rover-Name", rovername);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept", "application/json");

		DataInputStream in = new DataInputStream(con.getInputStream());
		StringBuffer jsonBuf = new StringBuffer();
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) != -1) {
			jsonBuf.append(new String(buffer, 0, bytesRead));
		}

		JSONArray jsonArray = convertToJsonArray(jsonBuf.toString());

		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);

		con.disconnect();

		return jsonArray;
	}

	public String postScanMapTiles(Coord currentLoc, MapTile[][] scanMapTiles) {
		JSONArray data = convertScanMapTiles(currentLoc, scanMapTiles);

		String charset = "UTF-8";
		URL obj = null;
		try {
			obj = new URL(url + "/global");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Rover-Name", rovername);
			con.setRequestProperty("Corp-Secret", corp_secret);
			con.setRequestProperty("Content-Type", "application/json");

			byte[] jsonBytes = data.toString().getBytes("UTF-8");

			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(jsonBytes);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
				tile.put("hasrover", ((xp == currentLoc.xpos) && (yp == currentLoc.ypos)));
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

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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

		return convertToJsonArray(responseStr);
	}

	public JSONArray convertToJsonArray(String response) {
		try {
			return (JSONArray) parser.parse(response);
		} catch (ParseException e) {
			throw new RuntimeException("Parsing JSON data response to JSONArray failed! Data: " + response, e);
		}
	}

	public JSONObject convertToJsonObject(String response) {
		try {
			return (JSONObject) parser.parse(response);
		} catch (ParseException e) {
			throw new RuntimeException("Parsing JSON data response to JSONObject failed! Data: " + response, e);
		}
	}

	public String markScienceForGather(Coord coord) {
		int x = coord.xpos;
		int y = coord.ypos;

		String charset = "UTF-8";
		URL obj = null;
		try {
			obj = new URL(url + "/science/gather/" + x + "/" + y);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Rover-Name", rovername);
			con.setRequestProperty("Corp-Secret", corp_secret);
			con.setRequestProperty("Content-Type", "application/json");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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