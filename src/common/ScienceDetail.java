package common;

import enums.Science;
import enums.Terrain;

public class ScienceDetail {
	private Science science;
	private boolean hasRover;
	private int x;
	private int y;
	private Terrain terrain;
	private int foundByRover;
	private int gatheredByRover = -1;

	public Science getScience() {
		return science;
	}

	public void setScience(Science science) {
		this.science = science;
	}

	public boolean isHasRover() {
		return hasRover;
	}

	public void setHasRover(boolean hasRover) {
		this.hasRover = hasRover;
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

	public Terrain getTerrain() {
		return terrain;
	}

	public void setTerrain(Terrain terrain) {
		this.terrain = terrain;
	}

	public int getFoundByRover() {
		return foundByRover;
	}

	public void setFoundByRover(int foundByRover) {
		this.foundByRover = foundByRover;
	}

	public int getGatheredByRover() {
		return gatheredByRover;
	}

	public void setGatheredByRover(int gatheredByRover) {
		this.gatheredByRover = gatheredByRover;
	}

	@Override
	public String toString() {
		return science + "[hasRover=" + hasRover + " ; x=" + x + " ; y=" + y + " ; terrain=" + terrain
				+ " ; foundByRover=" + foundByRover + " ; gatheredByRover=" + gatheredByRover + "]";
	}
}