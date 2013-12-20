package com.awesome.namethislater.model;

import com.badlogic.gdx.math.Vector2;

public class Level {

	private int width, height;
	private Block[][] grassBlocks;
	private Block[][] waterBlocks;

	public Level() {
		loadDemoLevel();
	}

	private void loadDemoLevel() {
		width = 10;
		height = 7;
		grassBlocks = new Block[width][height];
		waterBlocks = new Block[width][height];
		for (int col = 0; col < width; col++) {
			for (int row = 0; row < height; row++) {
				grassBlocks[col][row] = new Block(new Vector2(col, row));
			}
		}

		for (int col = 0; col < 10; col++) {
			waterBlocks[col][0] = new Block(new Vector2(col, 0));
			waterBlocks[col][6] = new Block(new Vector2(col, 6));
			if (col > 2) {
				waterBlocks[col][1] = new Block(new Vector2(col, 1));
			}
		}
		waterBlocks[9][2] = new Block(new Vector2(9, 2));
		waterBlocks[9][3] = new Block(new Vector2(9, 3));
		waterBlocks[9][4] = new Block(new Vector2(9, 4));
		waterBlocks[9][5] = new Block(new Vector2(9, 5));

		waterBlocks[6][3] = new Block(new Vector2(6, 3));
		waterBlocks[6][4] = new Block(new Vector2(6, 4));
		waterBlocks[6][5] = new Block(new Vector2(6, 5));

		waterBlocks[7][3] = new Block(new Vector2(7, 3));
		waterBlocks[8][3] = new Block(new Vector2(8, 3));
	}

	public Block getBlockAt(int x, int y) {
		return waterBlocks[x][y];
	}

	public Block getGrassBlocks(int x, int y) {
		return grassBlocks[x][y];
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
	 * @return the blocks
	 */
	public Block[][] getBlocks() {
		return waterBlocks;
	}

	public Block[][] getGrassBlocks() {
		return grassBlocks;
	}

	/**
	 * @param blocks
	 *            the blocks to set
	 */
	public void setBlocks(Block[][] blocks) {
		this.waterBlocks = blocks;
	}

}
