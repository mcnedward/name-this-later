package com.awesome.namethislater.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class World {

	/** The player **/
	Mike mike;
	/** The current level **/
	Level level;
	/** The room of the level **/
	Room room;
	/** The collision boxes **/
	Array<Rectangle> collisionRects = new Array<Rectangle>();

	public World() {
		createDemoWorld();
	}

	private void createDemoWorld() {
		level = new Level();
		room = new Room();
		mike = new Mike(new Vector2(2, 2));
	}

	public List<Block> getWaterBlocks(int width, int height) {
		int x = (int) mike.getPosition().x - width;
		int y = (int) mike.getPosition().y - height;
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		int x2 = x + 2 * width;
		int y2 = y + 2 * height;
		if (x2 > room.getWidth()) {
			x2 = room.getWidth() - 1;
		}
		if (y2 > room.getHeight()) {
			y2 = room.getHeight() - 1;
		}

		List<Block> blocks = new ArrayList<Block>();
		Block block;
		for (int col = y; col <= y2; col++) {
			for (int row = x; row <= x2; row++) {
				block = room.getWaterBlocks()[col][row];
				if (block != null) {
					blocks.add(block);
				}
			}
		}
		return blocks;
	}

	public List<Block> getGrassBlocks(int width, int height) {
		int x = (int) mike.getPosition().x - width;
		int y = (int) mike.getPosition().y - height;
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}
		int x2 = x + 2 * width;
		int y2 = y + 2 * height;
		if (x2 > room.getWidth()) {
			x2 = room.getWidth() - 1;
		}
		if (y2 > room.getHeight()) {
			y2 = room.getHeight() - 1;
		}

		List<Block> blocks = new ArrayList<Block>();
		Block block;
		for (int row = y; row <= y2; row++) {
			for (int col = x; col <= x2; col++) {
				block = room.getGrassBlocks()[row][col];
				if (block != null) {
					blocks.add(block);
				}
			}
		}
		return blocks;
	}

	public Array<Rectangle> getCollisionRects() {
		return collisionRects;
	}

	public Mike getMike() {
		return mike;
	}

	public Level getLevel() {
		return level;
	}

	public Room getRoom() {
		return room;
	}

}
