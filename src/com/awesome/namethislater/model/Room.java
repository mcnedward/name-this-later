package com.awesome.namethislater.model;

import com.badlogic.gdx.math.Vector2;

public class Room {

	private int width, height;
	private Block[][] grassBlocks;
	private Block[][] waterBlocks;
	private int[][] map = {
			{
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }, {
					0, 0, 0, 0, 0, 0, 1, 0, 0, 1, }, {
					0, 0, 0, 0, 0, 0, 1, 0, 0, 1, }, {
					0, 0, 0, 0, 0, 0, 1, 0, 0, 1, }, {
					0, 0, 0, 0, 0, 0, 1, 1, 1, 1, }, {
					0, 0, 0, 0, 0, 0, 0, 0, 0, 1, }, {
					1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }, };

	private Enemy enemy;

	private Vector2 startingPosition;

	public Room() {
		width = 10;
		height = 7;
		startingPosition = new Vector2(3, 3);
		enemy = new Enemy(new Vector2(4, 4));
		grassBlocks = new Block[height][width];
		waterBlocks = new Block[height][width];

		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				row = Math.abs(row - height + 1);
				int tile = map[row][col];
				if (tile == 0)
					grassBlocks[row][col] = new Block(new Vector2(col, row));
				if (tile == 1)
					waterBlocks[row][col] = new Block(new Vector2(col, row));
			}
		}
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the grassBlocks
	 */
	public Block[][] getGrassBlocks() {
		return grassBlocks;
	}

	/**
	 * @param grassBlocks
	 *            the grassBlocks to set
	 */
	public void setGrassBlocks(Block[][] grassBlocks) {
		this.grassBlocks = grassBlocks;
	}

	/**
	 * @return the waterBlocks
	 */
	public Block[][] getWaterBlocks() {
		return waterBlocks;
	}

	public Block getWaterBlockAt(int x, int y) {
		return waterBlocks[x][y];
	}

	/**
	 * @param waterBlocks
	 *            the waterBlocks to set
	 */
	public void setWaterBlocks(Block[][] waterBlocks) {
		this.waterBlocks = waterBlocks;
	}

	/**
	 * @return the map
	 */
	public int[][] getMap() {
		return map;
	}

	/**
	 * @param map
	 *            the map to set
	 */
	public void setMap(int[][] map) {
		this.map = map;
	}

	/**
	 * @return the enemy
	 */
	public Enemy getEnemy() {
		return enemy;
	}

	/**
	 * @param enemy
	 *            the enemy to set
	 */
	public void setEnemy(Enemy enemy) {
		this.enemy = enemy;
	}

	/**
	 * @return the startingPosition
	 */
	public Vector2 getStartingPosition() {
		return startingPosition;
	}

	/**
	 * @param startingPosition
	 *            the startingPosition to set
	 */
	public void setStartingPosition(Vector2 startingPosition) {
		this.startingPosition = startingPosition;
	}

}
