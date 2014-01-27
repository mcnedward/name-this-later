package com.awesome.namethislater.controller;

import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.model.Drawable;
import com.awesome.namethislater.model.Drawable.Direction;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public abstract class Controller {
	private static final float ACCELERATION = 40f; // The speed of walking
	private static final float JUMP_ACCELERATION = ACCELERATION / 1.5f; // The acceleration of a jump
	private static final float SHADOW_ACCELERATION = 0.07f; // The acceleration of the base of the jump

	public enum Keys {
		DOWN, LEFT, UP, RIGHT, JUMP, ATTACK
	}

	static Map<Keys, Boolean> keys = new HashMap<MikeController.Keys, Boolean>();
	static {
		keys.put(Keys.DOWN, false);
		keys.put(Keys.LEFT, false);
		keys.put(Keys.UP, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.JUMP, false);
		keys.put(Keys.ATTACK, false);
	}

	// Blocks that can be collided with in any frame
	private Array<Rectangle> tiles = new Array<Rectangle>();
	// This is the rectangle pool used in collision detection
	// Good to avoid instantiation each frame
	protected Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	protected float jumpDegree;
	protected float shadowPercentage;
	protected boolean jumpPressed = false; // Used to prevent auto-jump by holding down jump button
	protected boolean attackPressed = false;

	protected World world;
	protected Level level;
	protected final Mike mike;
	protected final Enemy enemy;
	private final TiledMap map;

	public Controller(World world) {
		this.world = world;
		this.level = world.getLevel();
		this.mike = world.getMike();
		this.enemy = level.getEnemy();
		map = level.getMap();
	}

	public abstract void update(float delta);

	public abstract void checkCollisions(float delta);

	protected boolean checkForTiles(Drawable drawable, Rectangle bounds) {
		boolean collide = false;
		// Check for collisions on the horizontal X axis
		int startX, endX;
		int startY = (int) drawable.getDamageBounds().y;
		int endY = (int) (drawable.getDamageBounds().y + drawable.getDamageBounds().height);
		// Check for collisions with blocks on the left and right
		if (drawable.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(drawable.getDamageBounds().x + drawable.getVelocity().x);
		} else {
			startX = endX = (int) Math.floor(drawable.getDamageBounds().x + drawable.getDamageBounds().width
					+ drawable.getVelocity().x);
		}

		getWaterTiles(startX, endX, startY, endY);

		// If enemy collides, set his horizontal velocity to 0
		for (Rectangle tile : tiles) {
			if (tile == null)
				continue;
			if (bounds.overlaps(tile)) {
				// Stop all movement and set enemy's state to dying. Then reset the degree used to change the float
				// angle and get the starting x and y coordinates for the death.
				drawable.getVelocity().x = 0;
				drawable.getVelocity().y = 0;
				collide = true;
				break;
			}
		}

		// Check for collisions on the vertical Y axis
		startX = (int) drawable.getDamageBounds().x;
		endX = (int) (drawable.getDamageBounds().x + drawable.getDamageBounds().width);
		if (drawable.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(drawable.getDamageBounds().y + drawable.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(drawable.getDamageBounds().y + (drawable.getDamageBounds().height)
					+ drawable.getVelocity().y);
		}

		getWaterTiles(startX, endX, startY, endY);

		for (Rectangle tile : tiles) {
			if (tile == null)
				continue;
			if (bounds.overlaps(tile)) {
				// Stop all movement and set enemy's state to dying. Then reset the degree used to change the float
				// angle and get the starting x and y coordinates for the death.
				drawable.getVelocity().x = 0;
				drawable.getVelocity().y = 0;
				collide = true;
				break;
			}
		}
		return collide;
	}

	private void getWaterTiles(int startX, int endX, int startY, int endY) {
		try {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
			rectPool.freeAll(tiles);
			tiles.clear();
			for (int x = startX; x <= endX; x++) {
				for (int y = startY; y <= endY; y++) {
					Cell cell = layer.getCell((int) (x), (int) (y));
					if (cell != null) {
						if (cell.getTile().getProperties().containsKey("water")) {
							Rectangle rect = new Rectangle();
							rect.set(x, y, 1, 1);
							tiles.add(rect);
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERROR!!! " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	protected boolean processInput(float delta) {
		if (keys.get(Keys.JUMP)) {
			if (!jumpPressed) {
				// If Mike is not jumping, set his state to JUMPING, get the starting jump time and y coordinate, and
				// set his maximum jump height. Then set his velocity to increase based on the max jump speed.
				if (!mike.isJumping()) {
					mike.setState(State.JUMPING);
					mike.setGrounded(false);
					jumpPressed = true; // The button for jumping is pressed
					jumpDegree = 0; // Reset the degree for the jump angle
					mike.getShadowVelocity().y = mike.getPosition().y; // The starting y-coordinate of the jump
					shadowPercentage = 100.0f;
				}
			}
		}
		if (keys.get(Keys.ATTACK)) {
			// Start the jump animation
			if (mike.isJumping()) {
				mike.setState(State.JUMP_ATTACK);
			} else {
				mike.setState(State.ATTACKING);
			}
		} else if (keys.get(Keys.DOWN)) {
			if (keys.get(Keys.LEFT)) {
				mike.setDirection(Direction.DOWN_LEFT);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = -ACCELERATION;
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					mike.getAcceleration().x = -JUMP_ACCELERATION;
					mike.getAcceleration().y = -JUMP_ACCELERATION;
					mike.getShadowVelocity().y -= SHADOW_ACCELERATION;
				}
			} else if (keys.get(Keys.RIGHT)) {
				mike.setDirection(Direction.DOWN_RIGHT);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = ACCELERATION;
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					mike.getAcceleration().x = JUMP_ACCELERATION;
					mike.getAcceleration().y = -JUMP_ACCELERATION;
					mike.getShadowVelocity().y -= SHADOW_ACCELERATION;
				}
			} else {
				mike.setDirection(Direction.DOWN);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					mike.getAcceleration().y = -JUMP_ACCELERATION;
					mike.getShadowVelocity().y -= SHADOW_ACCELERATION;
				}
			}
		} else if (keys.get(Keys.UP)) {
			if (keys.get(Keys.LEFT)) {
				mike.setDirection(Direction.UP_LEFT);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = -ACCELERATION;
					mike.getAcceleration().y = ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					mike.getAcceleration().x = -JUMP_ACCELERATION;
					mike.getAcceleration().y = JUMP_ACCELERATION;
					mike.getShadowVelocity().y += SHADOW_ACCELERATION;
				}
			} else if (keys.get(Keys.RIGHT)) {
				mike.setDirection(Direction.UP_RIGHT);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = ACCELERATION;
					mike.getAcceleration().y = ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					mike.getAcceleration().x = JUMP_ACCELERATION;
					mike.getAcceleration().y = JUMP_ACCELERATION;
					mike.getShadowVelocity().y += SHADOW_ACCELERATION;
				}
			} else {
				mike.setDirection(Direction.UP);
				if (!mike.isJumping()) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().y = ACCELERATION;
				} else if (mike.isJumping() && !mike.getState().equals(State.DAMAGE)) {
					mike.getAcceleration().y = JUMP_ACCELERATION;
					mike.getShadowVelocity().y += SHADOW_ACCELERATION;
				}
			}
		} else if (keys.get(Keys.LEFT)) {
			mike.setDirection(Direction.LEFT);
			if (!mike.isJumping()) {
				mike.setState(State.RUNNING);
				mike.getAcceleration().x = -ACCELERATION;
			} else if (mike.isJumping()) {
				mike.getAcceleration().x = -JUMP_ACCELERATION;
			}
		} else if (keys.get(Keys.RIGHT)) {
			mike.setDirection(Direction.RIGHT);
			if (!mike.isJumping()) {
				mike.setState(State.RUNNING);
				mike.getAcceleration().x = ACCELERATION;
			} else if (mike.isJumping()) {
				mike.getAcceleration().x = JUMP_ACCELERATION;
			}
		} else {
			if (!mike.isJumping() && !mike.getState().equals(State.DYING) && !mike.isAttacking()) {
				mike.setState(State.IDLE);
				mike.getVelocity().x = 0;
				mike.getVelocity().y = 0;
			}
		}
		return false;
	}

	public void downPressed() {
		keys.get(keys.put(Keys.DOWN, true));
	}

	public void upPressed() {
		keys.get(keys.put(Keys.UP, true));
	}

	public void leftPressed() {
		keys.get(keys.put(Keys.LEFT, true));
	}

	public void rightPressed() {
		keys.get(keys.put(Keys.RIGHT, true));
	}

	public void jumpPressed() {
		keys.get(keys.put(Keys.JUMP, true));
	}

	public void attackPressed() {
		keys.get(keys.put(Keys.ATTACK, true));
	}

	public void downReleased() {
		keys.get(keys.put(Keys.DOWN, false));
	}

	public void upReleased() {
		keys.get(keys.put(Keys.UP, false));
	}

	public void leftReleased() {
		keys.get(keys.put(Keys.LEFT, false));
	}

	public void rightReleased() {
		keys.get(keys.put(Keys.RIGHT, false));
	}

	public void jumpReleased() {
		keys.get(keys.put(Keys.JUMP, false));
		jumpPressed = false;
		float diff = Math.abs(90 - jumpDegree);
		jumpDegree = 90 + diff;
	}

	public void attackReleased() {
		keys.get(keys.put(Keys.ATTACK, false));
		attackPressed = false;
	}

	public void releaseAllMovement() {
		downReleased();
		upReleased();
		leftReleased();
		rightReleased();
	}

	public void releaseAll() {
		downReleased();
		upReleased();
		leftReleased();
		rightReleased();
		jumpReleased();
	}

}
