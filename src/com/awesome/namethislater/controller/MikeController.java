package com.awesome.namethislater.controller;

import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.model.Block;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.Direction;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.awesome.namethislater.view.Renderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class MikeController {
	private static final String TAG = "MikeController";

	public enum Keys {
		DOWN, LEFT, UP, RIGHT, JUMP
	}

	private static final float ACCELERATION = 20f;	// The speed of walking
	private static final float JUMP_ACCELERATION = ACCELERATION / 1.5f;
	private static final float GRAVITY = -20f;		// The gravity of the room
	private static final float MAX_JUMP_SPEED = 8f;	// The speed of a jump
	private static final float DAMP = 0.90f;		// Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;
	private static final long JUMP_DURATION = 600;	// The maximum time allowed for Mike to be in the air
	private static final long FPS = 10;

	private float time;
	private float jumpTime;							// The starting time of Mike's jump
	private float jumpDegree;
	private float jump;
	private float jumpStartY;						// The Y coordinate that Mike is at when he starts a jump
	private float jumpStartX;						// The X coordinate that Mike is at when he starts a jump
	private float jumpHeight;						// The height of Mike's jump

	private boolean jumping = false;
	private boolean falling = false;				// Used to determine whether Mike is falling from his jump
	private boolean jumpPressed = false;			// Used to prevent auto-jump by holding down jump button

	// This is the rectangle pool used in collision detection
	// Good to avoid instantiation each frame
	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	static Map<Keys, Boolean> keys = new HashMap<MikeController.Keys, Boolean>();
	static {
		keys.put(Keys.DOWN, false);
		keys.put(Keys.LEFT, false);
		keys.put(Keys.UP, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.JUMP, false);
	}
	private float delta;
	private World world;
	private Mike mike;
	// Blocks that can be collided with in any frame
	private Array<Block> collidable = new Array<Block>();

	public MikeController(World world) {
		this.world = world;
		this.mike = world.getMike();
	}

	public void update(float delta) {
		processInput(delta);

		// Multiply by the delta to convert acceleration to frame units
		mike.getAcceleration().mul(delta);
		// Add the acceleration to the velocity
		mike.getVelocity().add(mike.getAcceleration().x, mike.getAcceleration().y);

		// Check if Mike is currently jumping. If he is, check the time he has been in
		// the air and drop him after the maximum time allowed for his jump.
		if (mike.getState().equals(State.JUMPING)) {
			time += delta;
			jumpTime = (float) 60 / FPS;
			if (jumpTime >= time) {
				double radian = jumpDegree * (Math.PI / 180);
				float velocity = (float) Math.sin(radian);
				jump = jumpStartY + velocity;
				float d = jump;
				if (velocity < 0)
					velocity = 0;
				mike.getPosition().y = jump;
				jumpDegree += 5;
				if (jumpDegree > 180) {
					mike.setState(State.IDLE);
				}
			}
		}

		checkCollisions(delta);

		// Dampen Mike's movement so it appears smoother
		if (!mike.getState().equals(State.JUMPING)) {
			mike.getVelocity().x *= DAMP;
			mike.getVelocity().y *= DAMP;
		}

		// Ensure terminal velocity is not exceeded
		if (mike.getVelocity().x > MAX_VEL) {
			mike.getVelocity().x = MAX_VEL;
		}
		if (mike.getVelocity().x < -MAX_VEL) {
			mike.getVelocity().x = -MAX_VEL;
		}
		if (mike.getVelocity().y > MAX_VEL) {
			mike.getVelocity().y = MAX_VEL;
		}
		if (mike.getVelocity().y < -MAX_VEL) {
			mike.getVelocity().y = -MAX_VEL;
		}

		mike.update(delta);
	}

	private boolean processInput(float delta) {
		if (keys.get(Keys.JUMP)) {
			if (!jumpPressed) {
				// If Mike is not jumping, set his state to JUMPING, get the starting jump time and y coordinate, and
				// set his maximum jump height. Then set his velocity to increase based on the max jump speed.
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.JUMPING);
					jumpPressed = true;
					jumping = true;
					falling = false;
					jumpDegree = 0;
					jumpTime = System.currentTimeMillis();
					jumpStartY = mike.getPosition().y;
					// jumpStartX = mike.getPosition().x; // TODO JUMP DISTANCE!!!!!!!!!
					// jumpHeight = (mike.getBounds().y * 1.2f); // TODO Should this be bounds.y + bounds.height?
					// mike.getVelocity().y = MAX_JUMP_SPEED;
					// System.out.println("Jump starting at: " + jumpTime + "\nStarting Y: " + jumpStartY
					// + "\nMax height of jump: " + jumpHeight);

				}
			}
		}
		if (keys.get(Keys.DOWN)) {
			if (keys.get(Keys.LEFT)) {
				mike.setDirection(Direction.DOWN_LEFT);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = -ACCELERATION;
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.getState().equals(State.JUMPING)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					if (!falling) {
						mike.getAcceleration().y = -ACCELERATION * 2;
						mike.getAcceleration().x = -ACCELERATION * 2;
						jumpStartY = jumpStartY - (jumpStartY - mike.getPosition().y);
					}
				}
			} else if (keys.get(Keys.RIGHT)) {
				mike.setDirection(Direction.DOWN_RIGHT);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = ACCELERATION;
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.getState().equals(State.JUMPING)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					if (!falling) {
						mike.getAcceleration().y = -ACCELERATION * 2;
						mike.getAcceleration().x = ACCELERATION * 2;
						jumpStartY = jumpStartY - (jumpStartY - mike.getPosition().y);
					}
				}
			} else {
				mike.setDirection(Direction.DOWN);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().y = -ACCELERATION;
				} else if (mike.getState().equals(State.JUMPING)) {
					// If Mike is in the JUMPING state and not yet falling, then move him downwards.
					if (!falling) {
						mike.getAcceleration().y = -ACCELERATION * 2;
						jumpStartY -= (mike.getPosition().y - jumpStartY);
					}
				}
			}
		} else if (keys.get(Keys.UP)) {
			if (keys.get(Keys.LEFT)) {
				mike.setDirection(Direction.UP_LEFT);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = -ACCELERATION;
					mike.getAcceleration().y = ACCELERATION;
				}
			} else if (keys.get(Keys.RIGHT)) {
				mike.setDirection(Direction.UP_RIGHT);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().x = ACCELERATION;
					mike.getAcceleration().y = ACCELERATION;
				}
			} else {
				mike.setDirection(Direction.UP);
				if (!mike.getState().equals(State.JUMPING)) {
					mike.setState(State.RUNNING);
					mike.getAcceleration().y = ACCELERATION;
				} else if (mike.getState().equals(State.JUMPING)) {
					mike.getAcceleration().y = ACCELERATION;
					if (jumping) {
						jumpStartY = jumpStartY - (jumpStartY - mike.getPosition().y);
					}
				}
			}
		} else if (keys.get(Keys.LEFT)) {
			mike.setDirection(Direction.LEFT);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
				mike.getAcceleration().x = -ACCELERATION;
			} else if (mike.getState().equals(State.JUMPING)) {
				mike.getAcceleration().x = -JUMP_ACCELERATION;
			}
		} else if (keys.get(Keys.RIGHT)) {
			mike.setDirection(Direction.RIGHT);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
				mike.getAcceleration().x = ACCELERATION;
			} else if (mike.getState().equals(State.JUMPING)) {
				mike.getAcceleration().x = JUMP_ACCELERATION;
			}
		} else if (keys.get(Keys.DOWN) && keys.get(Keys.LEFT)) {
			mike.setDirection(Direction.DOWN_LEFT);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
			}
			mike.getAcceleration().x = -ACCELERATION;
			mike.getAcceleration().y = -ACCELERATION;
		} else {
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.IDLE);
				mike.getVelocity().x = 0;
				mike.getVelocity().y = 0;
			}
		}
		return false;
	}

	private void checkCollisions(float delta) {
		// Multiply by the delta to convert velocity to frame units
		mike.getVelocity().mul(delta);

		// Get the width and height of the level
		int width = world.getLevel().getWidth();
		int height = world.getLevel().getHeight();

		// Obtain Mike's rectangle from the pool of rectangles instead of instantiating every frame. Then set the
		// bounds of the rectangle.
		Rectangle mikeRect = rectPool.obtain();
		mikeRect.set(mike.getBounds());

		// Set Mike's collision rect to include his X and Y velocity
		mikeRect.x += mike.getVelocity().x;
		mikeRect.y += mike.getVelocity().y;

		// Check for collisions on the horizontal X axis
		int startX, endX;
		int startY = (int) mike.getBounds().y;
		int endY = (int) (mike.getBounds().y + (mike.getBounds().height / 2));
		// Check for collisions with blocks on the left and right
		if (mike.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(mike.getBounds().x + mike.getVelocity().x);
		} else {
			startX = endX = (int) Math.floor(mike.getBounds().x + mike.getBounds().width + mike.getVelocity().x);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		// Clear the collision rectangles in the world
		world.getCollisionRects().clear();

		// If Mike collides, set his horizontal velocity to 0
		for (Block block : collidable) {
			if (block == null)
				continue;
			if (!mike.getState().equals(State.JUMPING)) {
				if (mikeRect.overlaps(block.getBounds())) {
					mike.getVelocity().x = 0;
					world.getCollisionRects().add(block.getBounds());
				}
			}
		}

		// Check for collisions on the vertical Y axis
		startX = (int) mike.getBounds().x;
		endX = (int) (mike.getBounds().x + mike.getBounds().width);
		if (mike.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(mike.getBounds().y + mike.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(mike.getBounds().y + (mike.getBounds().height / 2) + mike.getVelocity().y);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		for (Block block : collidable) {
			if (block == null)
				continue;
			if (!mike.getState().equals(State.JUMPING)) {
				if (mikeRect.overlaps(block.getBounds())) {
					mike.getVelocity().y = 0;
					world.getCollisionRects().add(block.getBounds());
					break;
				}
			}
		}
		// Check for collisions with the left and right sides of the level
		if (mikeRect.x <= 0 || mikeRect.x > width - mikeRect.width - mike.getVelocity().x) {
			mike.getVelocity().x = 0;
		}
		// Check for collisions with the bottom and top sides of the levels
		if (mikeRect.y <= 0 || mikeRect.y > height - mikeRect.height - mike.getVelocity().y) {
			mike.getVelocity().y = 0;
		}

		// Reset Mike's collision rect with his position
		mikeRect.x = mike.getPosition().x;
		mikeRect.y = mike.getPosition().y;

		// Update the position
		mike.getPosition().add(mike.getVelocity());
		mike.getBounds().x = mike.getPosition().x;
		mike.getBounds().y = mike.getPosition().y;
		// Un-scale the velocity so that it is no longer in frame time
		mike.getVelocity().mul(1 / delta);
	}

	/**
	 * Populate the collidable array with the blocks found in the enclosing coordinates
	 * 
	 * @param startX
	 *            The starting X coordinate.
	 * @param startY
	 *            The starting Y coordinate.
	 * @param endX
	 *            The ending X coordinate.
	 * @param endY
	 *            The ending Y coordinate.
	 */
	private void populateCollidableBlocks(int startX, int startY, int endX, int endY) {
		collidable.clear();
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				if (x >= 0 && x < world.getLevel().getWidth() && y >= 0 && y < world.getLevel().getHeight()) {
					collidable.add(world.getLevel().getBlockAt(x, y));
				}
			}
		}
	}

	/** Touch Events and Key Presses **/

	public boolean onTouch(int x, int y, Renderer renderer) {
		float width = renderer.width;
		float height = renderer.height;

		float radius = (width / 5) / 2;						// The radius of the touch pad
		float cx = (width / 12) + radius;					// Get the center X of the touch pad
		float cy = height - (height / 10) - radius;			// Get the center Y of the touch pad
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
				releaseAll();
				rightPressed();
			}
			// Move up-right
			if (degree > 23 && degree < 68) {
				releaseAll();
				upPressed();
				rightPressed();
			}
			// Move up
			if (degree >= 68 && degree <= 113) {
				releaseAll();
				upPressed();
			}
			// Move up-left
			if (degree > 113 && degree < 158) {
				releaseAll();
				upPressed();
				leftPressed();
			}
			// Move left
			if (degree >= 158 && degree <= 203) {
				releaseAll();
				leftPressed();
			}
			// Move down-left
			if (degree > 203 && degree < 258) {
				releaseAll();
				downPressed();
				leftPressed();
			}
			// Move down
			if (degree >= 258 && degree <= 293) {
				releaseAll();
				downPressed();
			}
			// Move down-right
			if (degree > 293 && degree < 338) {
				releaseAll();
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

	public void downPressed() {
		keys.get(keys.put(Keys.DOWN, true));
	}

	public void leftPressed() {
		keys.get(keys.put(Keys.LEFT, true));
	}

	public void upPressed() {
		keys.get(keys.put(Keys.UP, true));
	}

	public void rightPressed() {
		keys.get(keys.put(Keys.RIGHT, true));
	}

	public void jumpPressed() {
		keys.get(keys.put(Keys.JUMP, true));
	}

	public void downReleased() {
		keys.get(keys.put(Keys.DOWN, false));
	}

	public void leftReleased() {
		keys.get(keys.put(Keys.LEFT, false));
	}

	public void upReleased() {
		keys.get(keys.put(Keys.UP, false));
	}

	public void rightReleased() {
		keys.get(keys.put(Keys.RIGHT, false));
	}

	public void jumpReleased() {
		keys.get(keys.put(Keys.JUMP, false));
		jumpPressed = false;
	}

	public void releaseAll() {
		downReleased();
		leftReleased();
		rightReleased();
		upReleased();
		jumpReleased();
	}

}
