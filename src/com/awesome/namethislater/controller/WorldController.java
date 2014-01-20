package com.awesome.namethislater.controller;

import java.util.Random;

import com.awesome.namethislater.model.Block;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Enemy.Direction;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class WorldController {

	private static float ACCELERATION = 10f;	// The speed of walking
	private static final float DAMP = 0.9f;			// Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

	private World world;
	private Level level;
	private Enemy enemy;
	// Blocks that can be collided with in any frame
	private Array<Block> collidable = new Array<Block>();
	// This is the rectangle pool used in collision detection
	// Good to avoid instantiation each frame
	private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
		@Override
		protected Rectangle newObject() {
			return new Rectangle();
		}
	};

	Random r = new Random();
	// Defines time left for movement. set to random int below 3 will move around at most for 3 seconds
	float enemyTime = r.nextInt(2);

	public WorldController(World world) {
		this.world = world;
		level = world.getLevel();
		enemy = level.getEnemy();
	}

	public void update(float delta) {
		enemyTime -= delta;

		enemy.getAcceleration().mul(delta);
		enemy.getVelocity().add(enemy.getAcceleration().x, enemy.getAcceleration().y);

		Direction direction = enemy.getDirection();
		if (direction.equals(Direction.UP)) {
			enemy.getAcceleration().y = ACCELERATION;
		} else if (direction.equals(Direction.DOWN)) {
			enemy.getAcceleration().y = -ACCELERATION;
		} else if (direction.equals(Direction.LEFT)) {
			enemy.getAcceleration().x = -ACCELERATION;
		} else if (direction.equals(Direction.RIGHT)) {
			enemy.getAcceleration().x = ACCELERATION;
		} else if (direction.equals(Direction.UP_LEFT)) {
			enemy.getAcceleration().x = -ACCELERATION;
			enemy.getAcceleration().y = ACCELERATION;
		} else if (direction.equals(Direction.DOWN_LEFT)) {
			enemy.getAcceleration().y = -ACCELERATION;
			enemy.getAcceleration().y = -ACCELERATION;
		} else if (direction.equals(Direction.UP_RIGHT)) {
			enemy.getAcceleration().x = -ACCELERATION;
			enemy.getAcceleration().y = ACCELERATION;
		} else if (direction.equals(Direction.DOWN_RIGHT)) {
			enemy.getAcceleration().x = ACCELERATION;
			enemy.getAcceleration().y = -ACCELERATION;
		}
		if (enemyTime < 0) {
			enemyTime = r.nextInt(2);
			enemy.setDirection(r.nextInt(7));
		}

		checkCollisions(delta);

		enemy.getVelocity().x *= DAMP;
		enemy.getVelocity().y *= DAMP;

		// Ensure terminal velocity is not exceeded
		if (enemy.getVelocity().x > MAX_VEL) {
			enemy.getVelocity().x = MAX_VEL;
		}
		if (enemy.getVelocity().x < -MAX_VEL) {
			enemy.getVelocity().x = -MAX_VEL;
		}
		if (enemy.getVelocity().y > MAX_VEL) {
			enemy.getVelocity().y = MAX_VEL;
		}
		if (enemy.getVelocity().y < -MAX_VEL) {
			enemy.getVelocity().y = -MAX_VEL;
		}
	}

	private void checkCollisions(float delta) {
		// Multiply by the delta to convert velocity to frame units
		enemy.getVelocity().mul(delta);

		// Get the width and height of the level
		int width = level.getWidth();
		int height = level.getHeight();

		// Obtain enemy's rectangle from the pool of rectangles instead of instantiating every frame. Then set the
		// bounds of the rectangle.
		Rectangle enemyRect = rectPool.obtain();
		float left = enemy.getDamageBounds().x;
		float bottom = enemy.getDamageBounds().y;
		float right = enemy.getDamageBounds().width;
		float top = enemy.getDamageBounds().height;

		enemyRect.set(left, bottom, right, top);

		// Set enemy's collision rect to include his X and Y velocity
		enemyRect.x += enemy.getVelocity().x;
		enemyRect.y += enemy.getVelocity().y;

		// Check for collisions on the horizontal X axis
		int startX, endX;
		int startY = (int) enemy.getDamageBounds().y;
		int endY = (int) (enemy.getDamageBounds().y + enemy.getDamageBounds().height);
		// Check for collisions with blocks on the left and right
		if (enemy.getVelocity().x < 0) {
			startX = endX = (int) Math.floor(enemy.getDamageBounds().x + enemy.getVelocity().x);
		} else {
			startX = endX = (int) Math.floor(enemy.getDamageBounds().x + enemy.getDamageBounds().width
					+ enemy.getVelocity().x);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		// Clear the collision rectangles in the world
		world.getCollisionRects().clear();

		// If enemy collides, set his horizontal velocity to 0
		for (Block block : collidable) {
			if (block == null)
				continue;
			if (enemyRect.overlaps(block.getBounds())) {
				// Stop all movement and set enemy's state to dying. Then reset the degree used to change the float
				// angle and get the starting x and y coordinates for the death.
				enemy.getVelocity().x = 0;
				enemy.getVelocity().y = 0;
				enemyTime = 0;
				world.getCollisionRects().add(block.getBounds());
				break;
			}
		}

		// Check for collisions on the vertical Y axis
		startX = (int) enemy.getDamageBounds().x;
		endX = (int) (enemy.getDamageBounds().x + enemy.getDamageBounds().width);
		if (enemy.getVelocity().y < 0) {
			startY = endY = (int) Math.floor(enemy.getDamageBounds().y + enemy.getVelocity().y);
		} else {
			startY = endY = (int) Math.floor(enemy.getDamageBounds().y + (enemy.getDamageBounds().height)
					+ enemy.getVelocity().y);
		}

		populateCollidableBlocks(startX, startY, endX, endY);

		for (Block block : collidable) {
			if (block == null)
				continue;
			if (enemyRect.overlaps(block.getBounds())) {
				// Stop all movement and set enemy's state to dying. Then reset the degree used to change the float
				// angle and get the starting x and y coordinates for the death.
				enemy.getVelocity().x = 0;
				enemy.getVelocity().y = 0;
				enemyTime = 0;
				world.getCollisionRects().add(block.getBounds());
				break;
			}
		}

		// Check for collisions with the left and right sides of the level
		if (enemyRect.x <= 0) {
			enemy.setDirection(Direction.RIGHT);
		}
		if (enemyRect.x > width - enemyRect.width - enemy.getVelocity().x) {
			enemy.setDirection(Direction.LEFT);
		}
		// Check for collisions with the bottom and top sides of the levels
		if (enemyRect.y <= 0) {
			enemy.setDirection(Direction.DOWN);
		}
		if (enemyRect.y > height - enemyRect.height - enemy.getVelocity().y) {
			enemy.setDirection(Direction.UP);
		}

		// Reset enemy's collision rect with his position
		enemyRect.x = enemy.getPosition().x;
		enemyRect.y = enemy.getPosition().y;

		// Update the position
		enemy.getPosition().add(enemy.getVelocity());
		enemy.getDamageBounds().x = enemy.getPosition().x;
		enemy.getDamageBounds().y = enemy.getPosition().y;
		// Un-scale the velocity so that it is no longer in frame time
		enemy.getVelocity().mul(1 / delta);
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

}
