package com.awesome.namethislater.controller;

import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.Direction;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool;

public class MikeController {

	public enum Keys {
		DOWN, LEFT, UP, RIGHT
	}

	private static final float ACCELERATION = 20f;	// The speed of walking
	private static final float GRAVITY = -20f;		// The gravity of the room
	private static final float DAMP = 0.90f;		// Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

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
	}

	private World world;
	private Mike mike;

	public MikeController(World world) {
		this.world = world;
		this.mike = world.getMike();
	}

	public void update(float delta) {
		processInput();

		// Multiply by the delta to convert acceleration to frame units
		mike.getAcceleration().mul(delta);
		// Add the acceleration to the velocity
		mike.getVelocity().add(mike.getAcceleration().x, mike.getAcceleration().y);

		checkCollisions(delta);

		// Dampen Mike's movement so it appears smoother
		mike.getVelocity().x *= DAMP;
		mike.getVelocity().y *= DAMP;

		// Ensure terminal velocity is not exceeded
		if (mike.getVelocity().x > MAX_VEL) {
			mike.getVelocity().x = MAX_VEL;
		}
		if (mike.getVelocity().x < -MAX_VEL) {
			mike.getVelocity().x = -MAX_VEL;
		}

		mike.update(delta);
	}

	private boolean processInput() {
		if (keys.get(Keys.DOWN)) {
			mike.setDirection(Direction.DOWN);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
			}
			mike.getAcceleration().y = -ACCELERATION;
		} else if (keys.get(Keys.LEFT)) {
			mike.setDirection(Direction.LEFT);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
			}
			mike.getAcceleration().x = -ACCELERATION;
		} else if (keys.get(Keys.UP)) {
			mike.setDirection(Direction.UP);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
			}
			mike.getAcceleration().y = ACCELERATION;
		} else if (keys.get(Keys.RIGHT)) {
			mike.setDirection(Direction.RIGHT);
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.RUNNING);
			}
			mike.getAcceleration().x = ACCELERATION;
		} else {
			if (!mike.getState().equals(State.JUMPING)) {
				mike.setState(State.IDLE);
			}
			mike.getAcceleration().x = 0;
			mike.getAcceleration().y = 0;
		}
		return false;
	}

	private void checkCollisions(float delta) {
		// Multiply by the delta to convert velocity to frame units
		mike.getVelocity().mul(delta);

		// Obtain Mike's rectangle from the pool of rectangles instead of instantiating every frame. Then set the
		// bounds of the rectangle.
		Rectangle mikeRect = rectPool.obtain();
		mikeRect.set(mike.getBounds());

		// Get the width and height of the level
		int width = world.getLevel().getWidth();
		int height = world.getLevel().getHeight();

		// Allow Mike to move away from the X axis
		mikeRect.x += mike.getVelocity().x;
		// Check for collisions with the left and right sides of the level
		if (mikeRect.x <= 0 || mikeRect.x > width - mikeRect.width - mike.getVelocity().x) {
			mike.getVelocity().x = 0;
		}
		// Allow Mike to move away from the Y axis
		mikeRect.y += mike.getVelocity().y;
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

	/** Key Presses and Touch Events **/

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

}
