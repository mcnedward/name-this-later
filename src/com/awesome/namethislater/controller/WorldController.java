package com.awesome.namethislater.controller;

import java.util.Random;

import com.awesome.namethislater.model.Drawable.Direction;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.math.Rectangle;

public class WorldController extends Controller {

	private static float ACCELERATION = 10f; // The speed of walking
	private static final float DAMP = 0.9f; // Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

	Random random = new Random();

	public WorldController(World world) {
		super(world);
	}

	@Override
	public void update(float delta) {
		checkCollisions(delta);
	}

	@Override
	public void checkCollisions(float delta) {
		for (Enemy enemy : enemies) {
			if (enemy.isDead()) {
				level.removeDeadEnemy(enemy);
			} else {
				processInput(delta);
				enemy.setEnemyTime(enemy.getEnemyTime() - delta);
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
				if (enemy.getEnemyTime() < 0) { // Reset the enemy time and direction
					enemy.setEnemyTime(random.nextInt(5));
					enemy.setDirection(random.nextInt(7));
				}
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

				if (checkForTiles(enemy, enemyRect)) {
					enemy.setEnemyTime(0);
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
					enemy.setDirection(Direction.UP);
				}
				if (enemyRect.y > height - enemyRect.height - enemy.getVelocity().y) {
					enemy.setDirection(Direction.DOWN);
				}

				// Reset enemy's collision rect with his position
				enemyRect.x = enemy.getPosition().x;
				enemyRect.y = enemy.getPosition().y;

				rectPool.free(enemyRect);

				// Update the position
				enemy.getPosition().add(enemy.getVelocity());
				enemy.updateDamageBounds(enemy.getPosition());
				// Un-scale the velocity so that it is no longer in frame time
				enemy.getVelocity().mul(1 / delta);

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
				enemy.update(delta);
			}
		}
	}
}
