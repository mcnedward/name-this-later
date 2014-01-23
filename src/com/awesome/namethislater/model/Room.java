package com.awesome.namethislater.model;

import com.badlogic.gdx.math.Vector2;

public class Room {
	
	private int width, height;
	private int[][] map = {
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, },
			{ 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, },
			{ 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, },
			{ 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, },
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, },
			{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, }
			};
	private Block[][] grassBlocks;
	private Block[][] waterBlocks;
	private Enemy enemy;
	private Vector2 startingPosition;

	public Room() {
		width = 10;
		height = 7;
		
		startingPosition = new Vector2(3, 3);
		enemy = new Enemy(new Vector2(4, 4));
		
		grassBlocks = new Block[width][height];
		waterBlocks = new Block[width][height];
		
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				// For bottom and top row, add water
				if (row == 0 || row == 6) {
					waterBlocks[col][row] = new Block(new Vector2(col, row));
				}
				else if (col == 9) {
					waterBlocks[col][row] = new Block(new Vector2(col, row));
				}
				else if ((col == 6 && row == 1) || (col == 6 && row == 2)) { 
					waterBlocks[col][row] = new Block(new Vector2(col, row));
				}
				else if (row == 3 && (col == 6 || col == 7 || col == 8)) {
					waterBlocks[col][row] = new Block(new Vector2(col, row));
				} else {
				grassBlocks[col][row] = new Block(new Vector2(col, row));
				}
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int[][] getMap() {
		return map;
	}

	public void setMap(int[][] map) {
		this.map = map;
	}

	public Block[][] getGrassBlocks() {
		return grassBlocks;
	}

	public Block getGrassBlockAt(int x, int y) {
		return grassBlocks[x][y];
	}

	public void setGrassBlocks(Block[][] grassBlocks) {
		this.grassBlocks = grassBlocks;
	}

	public Block[][] getWaterBlocks() {
		return waterBlocks;
	}
	
	public Block getWaterBlockAt(int x, int y) {
		return waterBlocks[x][y];
	}

	public void setWaterBlocks(Block[][] waterBlocks) {
		this.waterBlocks = waterBlocks;
	}

	public Enemy getEnemy() {
		return enemy;
	}

	public void setEnemy(Enemy enemy) {
		this.enemy = enemy;
	}

	public Vector2 getStartingPosition() {
		return startingPosition;
	}

	public void setStartingPosition(Vector2 startingPosition) {
		this.startingPosition = startingPosition;
	}
}
