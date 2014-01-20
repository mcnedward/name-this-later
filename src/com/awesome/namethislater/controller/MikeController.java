package com.awesome.namethislater.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.awesome.namethislater.model.Block;
import com.awesome.namethislater.model.Chakram;
import com.awesome.namethislater.model.Drawable.Direction;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.awesome.namethislater.view.Renderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class MikeController {
	private static final String TAG = "MikeController";

	public enum Keys {
		DOWN, LEFT, UP, RIGHT, JUMP, ATTACK
	}

	private static final float ACCELERATION = 20f;			// The speed of walking
	private static final float JUMP_ACCELERATION = ACCELERATION / 1.5f;	// The acceleration of a jump
	private static final float DEATH_ACCELERATION = ACCELERATION / 4;	// The acceleration of the death rise
	private static final float SHADOW_ACCELERATION = 0.07f;	// The acceleration of the base of the jump
	private static final float DAMP = 0.90f;				// Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

	private float deadDegree;
	private float riseX;
	private float riseY;
	private float deadStartY;
	private float deadStartX;

	private float jumpDegree;
	private float lift;								// The amount to increase or decrease the y-coord for a jump
	private float shadowPercentage;
	private float rotation = 0;						// The amount to rotate the chakram

	private boolean jumpPressed = false;			// Used to prevent auto-jump by holding down jump button
	private boolean attackPressed = false;

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
		keys.put(Keys.ATTACK, false);
	}

	private World world;
	private Level level;
	private Mike mike;
	// Blocks that can be collided with in any frame
	private Array<Block> collidable = new Array<Block>();

	public MikeController(World world) {
		this.world = world;
		this.level = world.getLevel();
		this.mike = world.getMike();
	}

	public void update(float delta) {
		if (!mike.getState().equals(State.DYING) && !mike.getState().equals(State.ATTACKING)
				&& !mike.getState().equals(State.JUMP_ATTACK))
			processInput(delta);	// Allow for movement unless dead

		// Multiply by the delta to convert acceleration to frame units
		mike.getAcceleration().mul(delta);
		// Add the acceleration to the velocity
		mike.getVelocity().add(mike.getAcceleration().x, mike.getAcceleration().y);

		// TODO Fix jump attack
		// If Mike is ready to attack, throw an attack!
		if (mike.isAttacking()) {
			if (mike.isJumping()) {
				float airHeight = mike.getPosition().y - mike.getShadowVelocity().y;
				mike.jumpAttack(airHeight);
			} else
				mike.attack();
		}

		// Check if Mike is currently jumping. If he is, check the time he has been in
		// the air and drop him after the maximum time allowed for his jump.
		if (mike.isJumping()) {
			// The radian is the current angle of the jump, in radian measurements. Use this to determine the
			// velocity that is needed to increase the y-coord for the jump by finding the sin of that radian. The
			// lift variable uses the starting y-coord of the jump, and adds to that the current y velocity of the
			// jump. Mike's position is updated to be at the current lift.
			double radian = jumpDegree * (Math.PI / 180);
			float velocityY = (float) Math.sin(radian);
			lift = mike.getShadowVelocity().y + velocityY;
			// If the y velocity is less than 0, then the jump is over.
			if (velocityY < 0)
				velocityY = 0;
			mike.getPosition().y = lift;
			// Increase the angle of the jump. The jump peaks at 90 degrees, and lands on the ground at 180.
			jumpDegree += 5;
			if (jumpDegree < 90)
				shadowPercentage -= 2.5;
			else
				shadowPercentage += 2.5;

			mike.updateShadow(mike.getPosition().x, mike.getShadowVelocity().y, shadowPercentage);

			if (jumpDegree > 180) {
				mike.setGrounded(true);
				if (mike.getState().equals(State.JUMP_ATTACK))
					mike.setState(State.ATTACKING);
				else
					mike.setState(State.IDLE);
			}
		}

		Iterator<Chakram> it = mike.getChakrams().iterator();
		while (it.hasNext()) {
			Chakram chakram = it.next();
			chakram.getAcceleration().mul(delta);
			chakram.getVelocity().add(chakram.getAcceleration().x, chakram.getAcceleration().y);
			chakram.getPosition().add(chakram.getVelocity());

			chakram.update(new Vector2(chakram.getPosition().x, chakram.getPosition().y), rotation);
			checkChakramCollisions(delta, it, chakram);
		}

		rotation += 5;

		checkCollisions(delta);

		// Dampen Mike's movement so it appears smoother
		if (!mike.isJumping()) {
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
				if (!mike.isJumping()) {
					mike.setState(State.JUMPING);
					mike.setGrounded(false);
					jumpPressed = true;						// The button for jumping is pressed
					jumpDegree = 0;							// Reset the degree for the jump angle
					mike.getShadowVelocity().y = mike.getPosition().y;		// The starting y-coordinate of the jump
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

	private void checkCollisions(float delta) {
		// Multiply by the delta to convert velocity to frame units
		mike.getVelocity().mul(delta);

		// Get the width and height of the level
		int width = world.getLevel().getWidth();
		int height = world.getLevel().getHeight();

		// Obtain Mike's rectangle from the pool of rectangles instead of instantiating every frame. Then set the
		// bounds of the rectangle.
		Rectangle mikeRect = rectPool.obtain();
		float left = mike.getFeetBounds().x;
		float bottom = mike.getFeetBounds().y;
		float right = mike.getFeetBounds().width;
		float top = mike.getFeetBounds().height;

		Rectangle mikeShadow = new Rectangle();
		float l = mike.getShadowBounds().x;
		float b = mike.getShadowBounds().y;
		float r = mike.getShadowBounds().width;
		float t = mike.getShadowBounds().height;

		mikeRect.set(left, bottom, right, top);
		mikeShadow.set(l, b, r, t);

		// Set Mike's collision rect to include his X and Y velocity
		mikeRect.x += mike.getVelocity().x;
		mikeRect.y += mike.getVelocity().y;
		mikeShadow.x += mike.getVelocity().x;
		mikeShadow.y += mike.getVelocity().y;

		// Check for collisions on the horizontal X axis
		int startX, endX;
		int startY = (int) mike.getFeetBounds().y;
		int endY = (int) (mike.getFeetBounds().y + mike.getFeetBounds().height);
		// Check for collisions with blocks on the left and right
		if (mike.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(mike.getFeetBounds().x + mike.getVelocity().x);
		} else {
			startX = endX = (int) Math
					.floor(mike.getFeetBounds().x + mike.getFeetBounds().width + mike.getVelocity().x);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		// Clear the collision rectangles in the world
		world.getCollisionRects().clear();

		// If Mike collides, set his horizontal velocity to 0
		for (Block block : collidable) {
			if (block == null)
				continue;
			if (!mike.isJumping() && !mike.getState().equals(State.DYING)) {
				if (mikeRect.overlaps(block.getBounds())) {
					// Stop all movement and set Mike's state to dying. Then reset the degree used to change the float
					// angle and get the starting x and y coordinates for the death.
					mike.getVelocity().x = 0;
					mike.getVelocity().y = 0;
					world.getCollisionRects().add(block.getBounds());
					mike.setState(State.DYING);
					deadDegree = 0;
					deadStartY = mike.getPosition().y;
					deadStartX = mike.getPosition().x;
					break;
				}
			}
		}

		// Check for collisions on the vertical Y axis
		startX = (int) mike.getFeetBounds().x;
		endX = (int) (mike.getFeetBounds().x + mike.getFeetBounds().width);
		if (mike.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(mike.getFeetBounds().y + mike.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(mike.getFeetBounds().y + (mike.getFeetBounds().height)
					+ mike.getVelocity().y);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		for (Block block : collidable) {
			if (block == null)
				continue;
			if (!mike.isJumping() && !mike.getState().equals(State.DYING)) {
				if (mikeRect.overlaps(block.getBounds())) {
					// Stop all movement and set Mike's state to dying. Then reset the degree used to change the float
					// angle and get the starting x and y coordinates for the death.
					mike.getVelocity().x = 0;
					mike.getVelocity().y = 0;
					world.getCollisionRects().add(block.getBounds());
					mike.setState(State.DYING);
					deadDegree = 0;
					deadStartY = mike.getPosition().y;
					deadStartX = mike.getPosition().x;
					break;
				}
			}
		}

		Enemy enemy = level.getEnemy();
		if (!mike.getState().equals(State.DYING)) {
			if (mike.isJumping()) {
				if ((mikeShadow.y + mikeShadow.height) >= enemy.getDamageBounds().y
						|| mikeShadow.y <= (enemy.getDamageBounds().y + enemy.getDamageBounds().height)) {
					if (mikeShadow.overlaps(enemy.getDamageBounds())) {
						mike.setState(State.DAMAGE);
						jumpPressed = false;
						mike.getVelocity().x = 0;
					}
				}
			} else {
				if (mikeRect.overlaps(enemy.getDamageBounds())) {
					mike.setState(State.DAMAGE);
					mike.getVelocity().x = 0;
					mike.getVelocity().y = 0;
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

		if (mike.getState().equals(State.DYING)) {

			double radian = deadDegree * (Math.PI / 180);
			float velocityX = (float) Math.sin(radian);
			float velocityY = velocityX;
			riseX = deadStartX + velocityX;
			riseY = deadStartY + velocityY;

			if (velocityX < 0)
				velocityX = 0;

			mike.getPosition().x = riseX;
			mike.getAcceleration().y = DEATH_ACCELERATION;

			deadDegree += 3;
			if (deadDegree >= 360) {
				mike.getPosition().x = world.getLevel().getStartingPosition().x;
				mike.getPosition().y = world.getLevel().getStartingPosition().y;
				mike.setState(State.IDLE);
			}
		}

		// Reset Mike's collision rect with his position
		mikeRect.x = mike.getPosition().x;
		mikeRect.y = mike.getPosition().y;
		mikeShadow.x = mike.getPosition().x;
		mikeShadow.y = mike.getShadowVelocity().y;

		// Update the position
		mike.getPosition().add(mike.getVelocity());
		mike.getFeetBounds().x = mike.getPosition().x + (Mike.SIZE / 7);
		mike.getFeetBounds().y = mike.getPosition().y;
		// Un-scale the velocity so that it is no longer in frame time
		mike.getVelocity().mul(1 / delta);
	}

	/**
	 * Check if the chakram collides with an enemy. If it does, remove it from the iterator.
	 * 
	 * @param delta
	 *            The time in seconds since the last update. Used to scale the velocity to frame units.
	 * @param it
	 *            The iterator that is looping through each of the chakrams.
	 * @param chakram
	 *            The current chakram that is being checked.
	 */
	private void checkChakramCollisions(float delta, Iterator<Chakram> it, Chakram chakram) {
		Enemy enemy = level.getEnemy();
		// Multiply by the delta to convert velocity to frame units
		chakram.getVelocity().mul(delta);

		// Create the bounds of the chakram
		Rectangle chakramRect = new Rectangle();
		float left = chakram.getAttackBounds().x;
		float bottom = chakram.getAttackBounds().y;
		float right = chakram.getAttackBounds().width;
		float top = chakram.getAttackBounds().height;

		chakramRect.set(left, bottom, right, top);

		// Set the chakram bounds to include X and Y velocity
		chakramRect.x += chakram.getVelocity().x;
		chakramRect.y += chakram.getVelocity().y;

		// Check for collisions
		if (chakramRect.overlaps(enemy.getDamageBounds())) {
			chakram.getVelocity().x = 0;
			chakram.getVelocity().y = 0;
			it.remove();
		} else {
			chakram.getVelocity().mul(1 / delta);
		}
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

	public boolean onTouch(float x, float y, Renderer renderer) {
		float width = renderer.width;
		float height = renderer.height;

		float radius = (width / 5) / 2;				// The radius of the touch pad
		float cx = (width / 12) + radius;			// Get the center X of the touch pad
		float cy = height - (height / 10) - radius;	// Get the center Y of the touch pad
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

	public void onTouchUp(int x, int y, Renderer renderer) {
		float width = renderer.width;
		float height = renderer.height;

		float radius = (width / 5) / 2;				// The radius of the touch pad
		float cx = (width / 12) + radius;			// Get the center X of the touch pad
		float cy = height - (height / 10) - radius;	// Get the center Y of the touch pad
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
