package com.awesome.namethislater.model;

import java.util.Iterator;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Level {

	private int width, height;
	private Block[][] grassBlocks;
	private Block[][] waterBlocks;

	private Enemy enemy;

	private TiledMap map;

	private Vector2 startingPosition;

	public Level() {
		loadMap();
	}

	private void loadMap() {
		TmxMapLoader loader = new TmxMapLoader();
		map = loader.load("data/world/level/level3.tmx");

		startingPosition = new Vector2(2, 2);
		enemy = new Enemy(new Vector2(0, 4));

		width = map.getProperties().get("width", Integer.class);
		height = map.getProperties().get("height", Integer.class);

		// Animated tile frames
		Array<StaticTiledMapTile> grassFrameTiles = new Array<StaticTiledMapTile>(2);
		Array<StaticTiledMapTile> waterFrameTiles = new Array<StaticTiledMapTile>(2);

		// Get each frame for the animated tiles
		Iterator<TiledMapTile> tiles = map.getTileSets().getTileSet("level").iterator();
		while (tiles.hasNext()) {
			TiledMapTile tile = tiles.next();
			if (tile.getProperties().containsKey("animation")) {
				if (tile.getProperties().get("animation", String.class).equals("grass")) {
					grassFrameTiles.add((StaticTiledMapTile) tile);
				}
				if (tile.getProperties().get("animation", String.class).equals("water")) {
					waterFrameTiles.add((StaticTiledMapTile) tile);
				}
			}
		}

		// AnimatedTiledMapTile animatedGrass = new AnimatedTiledMapTile(2f, grassFrameTiles);
		AnimatedTiledMapTile animatedWater = new AnimatedTiledMapTile(1f, waterFrameTiles);

		// for (TiledMapTile tile : grassFrameTiles)
		// animatedGrass.getProperties().putAll(tile.getProperties());
		for (TiledMapTile tile : waterFrameTiles)
			animatedWater.getProperties().putAll(tile.getProperties());

		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("background");

		for (int x = 0; x < layer.getWidth(); x++) {
			for (int y = 0; y < layer.getHeight(); y++) {
				Cell cell = layer.getCell(x, y);
				if (cell.getTile().getProperties().containsKey("animation")
						&& cell.getTile().getProperties().get("animation", String.class).equals("grass")) {
					// cell.setTile(animatedGrass);
				}
				if (cell.getTile().getProperties().containsKey("animation")
						&& cell.getTile().getProperties().get("animation", String.class).equals("water")) {
					cell.setTile(animatedWater);
				}
			}
		}
	}

	private void loadDemoLevel() {
		loadMap();
		startingPosition = new Vector2(2, 2);
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

		enemy = new Enemy(new Vector2(0, 4));
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

	public Block getBlockAt(int x, int y) {
		return waterBlocks[x][y];
	}

	public Block getGrassBlocks(int x, int y) {
		return grassBlocks[x][y];
	}

	public TiledMap getMap() {
		return map;
	}

	public void setMap(TiledMap map) {
		this.map = map;
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

	/**
	 * @return the startingPoint
	 */
	public Vector2 getStartingPosition() {
		return startingPosition;
	}

	/**
	 * @param startingPoint
	 *            the startingPoint to set
	 */
	public void setStartingPosition(Vector2 startingPoint) {
		this.startingPosition = startingPoint;
	}

}
