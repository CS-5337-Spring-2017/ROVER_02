package common;

import enums.RoverDriveType;
import enums.RoverMode;
import enums.RoverToolType;

public class RoverDetail {
	private String roverName;
	private int x;
	private int y;
	private RoverMode roverMode = RoverMode.EXPLORE;
	private int targetX = -1;
	private int targetY = -1;
	private RoverDriveType driveType;
	private RoverToolType toolType1;
	private RoverToolType toolType2;

	public String getRoverName() {
		return roverName;
	}

	public void setRoverName(String roverName) {
		this.roverName = roverName;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public RoverMode getRoverMode() {
		return roverMode;
	}

	public void setRoverMode(RoverMode roverMode) {
		this.roverMode = roverMode;
	}

	public int getTargetX() {
		return targetX;
	}

	public void setTargetX(int targetX) {
		this.targetX = targetX;
	}

	public int getTargetY() {
		return targetY;
	}

	public void setTargetY(int targetY) {
		this.targetY = targetY;
	}

	public RoverDriveType getDriveType() {
		return driveType;
	}

	public void setDriveType(RoverDriveType driveType) {
		this.driveType = driveType;
	}

	public RoverToolType getToolType1() {
		return toolType1;
	}

	public void setToolType1(RoverToolType toolType1) {
		this.toolType1 = toolType1;
	}

	public RoverToolType getToolType2() {
		return toolType2;
	}

	public void setToolType2(RoverToolType toolType2) {
		this.toolType2 = toolType2;
	}

	@Override
	public String toString() {
		return roverName + "[driveType=" + driveType + " ; x=" + x + " ; y=" + y + " ; targetX=" + targetX
				+ " ; targetY=" + targetY + " ; toolType1=" + toolType1 + " ; toolType2=" + toolType2 + " ; roverMode="
				+ roverMode + "]";
	}
}