package com.awesome.namethislater.controller;

import java.util.HashMap;
import java.util.List;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
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
	private final Array<Rectangle> tiles = new Array<Rectangle>();
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
	protected List<Enemy> enemies;
	private final TiledMap map;

	public Controller(World world) {
		this.world = world;
		this.level = world.getLevel();
		this.mike = world.getMike();
		enemies = level.getEnemies();
		map = level.getMap();
	}

	public abstract void update(float delta);

	public abstract void checkCollisions(float delta);

	protected boolean checkForTiles(Drawable drawable, Rectangle bounds) {
		boolean collide = false;
		// Check for collisions on the horizontal X axis
		int startX, endX;
		int startY = (int) bounds.y;
		int endY = (int) (bounds.y + bounds.height);
		// Check for collisions with blocks on the left and right
		if (drawable.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(bounds.x + drawable.getVelocity().x);
		} else {
			startX = endX = (int) Math.floor(bounds.x + bounds.width + drawable.getVelocity().x);
		}

		getWaterTiles(startX, endX, startY, endY);

		// If enemy collides, set his horizontal velocity to 0
		for (Rectangle tile : tiles) {
			if (tile == null) {
				continue;
			}
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
		startX = (int) bounds.x;
		endX = (int) (bounds.x + bounds.width);
		if (drawable.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(bounds.y + drawable.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(bounds.y + (bounds.height) + drawable.getVelocity().y);
		}

		getWaterTiles(startX, endX, startY, endY);

		for (Rectangle tile : tiles) {
			if (tile == null) {
				continue;
			}
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
					Cell cell = layer.getCell((x), (y));
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
			if (!mike.isJumping() && !mike.isAttacking() && !mike.getState().equals(State.DAMAGE)
					&& !mike.getState().equals(State.DYING)) {
				mike.getAcceleration();
				mike.setState(State.IDLE);
				mike.getVelocity().x = 0;
				mike.getVelocity().y = 0;
			}
		}
		return false;
	}

	/** Touch Events and Key Presses **/

	public boolean onTouch(float x, float y, Actor touchpad) {
		float width = touchpad.getWidth();
		float height = touchpad.getHeight();

		float radius = (width / 5) / 2; // The radius of the touch pad
		float cx = (width / 12) + radius; // Get the center X of the touch pad
		float cy = height - (height / 10) - radius; // Get the center Y of the touch pad
		// Get the maximum and minimum x and y coordinates allowed for the touch pad
		float maxX = cx + radius;
		float minX = Math.abs(cx - radius);
		float maxY = cy + radius;
		float minY = Math.abs(cy - radius);
		// Get the maximum and minimum x and y coordinates allowed for the jump button
		float jRadius = (width / 7) / 2;
		float jx = width - (width / 5) + jRadius;
		float jy = height - (height / 6) - jRadius;
		float jMaxX = jx + jRadius;
		float jMinX = Math.abs(jx - jRadius);
		float jMaxY = jy + jRadius;
		float jMinY = Math.abs(jy - jRadius);

		// Find the degree that the touch event is at
		double degree = (Math.atan2(y - cy, x - cx) / (Math.PI / 180));
		degree *= -1;
		degree += 360;
		if (degree > 360) {
			degree -= 360;
		}

		// Find the distance from the center of the circle using Pythagorian Theorem
		// TODO This can be used later to increase the speed of the sprite
		double a = x - cx;
		double b = y - cy;
		double distance = Math.abs(Math.sqrt((a * a) + (b * b)));
		distance = distance > (maxX - minX) / 2 ? (maxX - minX) / 2 : distance;

		// If the touch event is inside the touch pad, move the sprite by first releasing any keys that may be pressed
		// down. Then set the correct keys to be pressed down that correspond to the area of the touch pad that is
		// being touched at the moment.
		if (x <= maxX && x >= minX && y <= maxY && y >= minY) {
			// Move right
			if (degree <= 23 && degree >= 0 || degree >= 338 && degree <= 360) {
				releaseAllMovement();
				rightPressed();
			}
			// Move up-right
			if (degree > 23 && degree < 68) {
				releaseAllMovement();
				upPressed();
				rightPressed();
			}
			// Move up
			if (degree >= 68 && degree <= 113) {
				releaseAllMovement();
				upPressed();
			}
			// Move up-left
			if (degree > 113 && degree < 158) {
				releaseAllMovement();
				upPressed();
				leftPressed();
			}
			// Move left
			if (degree >= 158 && degree <= 203) {
				releaseAllMovement();
				leftPressed();
			}
			// Move down-left
			if (degree > 203 && degree < 258) {
				releaseAllMovement();
				downPressed();
				leftPressed();
			}
			// Move down
			if (degree >= 258 && degree <= 293) {
				releaseAllMovement();
				downPressed();
			}
			// Move down-right
			if (degree > 293 && degree < 338) {
				releaseAllMovement();
				downPressed();
				rightPressed();
			} else {
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.IDLE);
				}
				mike.getAcceleration().x = 0;
				mike.getAcceleration().y = 0;
			}
		}
		if (x <= jMaxX && x >= jMinX && y <= jMaxY && y >= jMinY) {
			jumpPressed();
		}
		return false;
	}

	public void onTouchUp(int x, int y, Actor touchpad) {
		float width = touchpad.getWidth();
		float height = touchpad.getHeight();

		float radius = (width / 5) / 2; // The radius of the touch pad
		float cx = (width / 12) + radius; // Get the center X of the touch pad
		float cy = height - (height / 10) - radius; // Get the center Y of the touch pad
		// Get the maximum and minimum x and y coordinates allowed for the touch pad
		float maxX = cx + radius;
		float minX = Math.abs(cx - radius);
		float maxY = cy + radius;
		float minY = Math.abs(cy - radius);
		// Get the maximum and minimum x and y coordinates allowed for the jump button
		float jRadius = (width / 7) / 2;
		float jx = width - (width / 5) + jRadius;
		float jy = height - (height / 6) - jRadius;
		float jMaxX = jx + jRadius;
		float jMinX = Math.abs(jx - jRadius);
		float jMaxY = jy + jRadius;
		float jMinY = Math.abs(jy - jRadius);

		if (x <= maxX && x >= minX && y <= maxY && y >= minY) {
			releaseAllMovement();
		}
		if (x <= jMaxX && x >= jMinX && y <= jMaxY && y >= jMinY) {
			jumpReleased();
		}
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
