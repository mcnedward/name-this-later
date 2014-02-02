package com.awesome.namethislater.controller;

import java.util.Iterator;

import com.awesome.namethislater.model.Chakram;
import com.awesome.namethislater.model.Drawable.Direction;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class MikeController extends Controller {
	private static final String TAG = "MikeController";
	private static final float DEATH_ACCELERATION = 10f; // The acceleration of the death rise
	private static final float DAMP = 0.90f; // Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

	private float lift; // The amount to increase or decrease the y-coord for a jump

	private float deadDegree, deadStartX;
	private float riseX;
	private float rotation = 0; // The amount to rotate the chakram

	public MikeController(World world) {
		super(world);
	}

	@Override
	public void update(float delta) {

		checkForDeath(delta);

		if (!mike.getState().equals(State.DYING) && !mike.getState().equals(State.ATTACKING)
				&& !mike.getState().equals(State.JUMP_ATTACK)) {
			processInput(delta); // Allow for movement unless dead
		}

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
			} else {
				mike.attack();
			}
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
			if (velocityY < 0) {
				velocityY = 0;
			}
			mike.getPosition().y = lift;
			// Increase the angle of the jump. The jump peaks at 90 degrees, and lands on the ground at 180.
			jumpDegree += 5;
			if (jumpDegree < 90) {
				shadowPercentage -= 2.5;
			} else {
				shadowPercentage += 2.5;
			}

			mike.updateShadow(mike.getPosition().x, mike.getShadowVelocity().y, shadowPercentage);

			if (jumpDegree > 180) {
				mike.setGrounded(true);
				if (mike.getState().equals(State.JUMP_ATTACK)) {
					mike.setState(State.ATTACKING);
				} else {
					mike.setState(State.IDLE);
				}
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
		mike.getVelocity().x *= DAMP;
		mike.getVelocity().y *= DAMP;

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

	@Override
	public void checkCollisions(float delta) {
		// Multiply by the delta to convert velocity to frame units
		mike.getVelocity().mul(delta);

		// Get the width and height of the level
		int width = level.getWidth();
		int height = level.getHeight();

		// Obtain Mike's rectangle from the pool of rectangles instead of instantiating every frame. Then set the
		// bounds of the rectangle.
		Rectangle mikeDamage = rectPool.obtain();
		float left = mike.getDamageBounds().x + mike.getVelocity().x;
		float bottom = mike.getDamageBounds().y + mike.getVelocity().y;
		float right = mike.getDamageBounds().width;
		float top = mike.getDamageBounds().height;

		Rectangle mikeFeet = rectPool.obtain();
		float fl = mike.getFeetBounds().x + mike.getVelocity().x;
		float fb = mike.getFeetBounds().y + mike.getVelocity().y;
		float fr = mike.getFeetBounds().width;
		float ft = mike.getFeetBounds().height;

		Rectangle mikeShadow = rectPool.obtain();
		float l = mike.getShadowBounds().x + mike.getVelocity().x;
		float b = mike.getShadowBounds().y + mike.getVelocity().y;
		float r = mike.getShadowBounds().width;
		float t = mike.getShadowBounds().height;

		mikeDamage.set(left, bottom, right, top);
		mikeFeet.set(fl, fb, fr, ft);
		mikeShadow.set(l, b, r, t);

		for (Enemy enemy : enemies) {
			enemy.getVelocity().mul(delta);

			Rectangle enemyRect = rectPool.obtain();
			float el = enemy.getDamageBounds().x + enemy.getVelocity().x;
			float eb = enemy.getDamageBounds().y + enemy.getVelocity().y;
			float er = enemy.getDamageBounds().width;
			float et = enemy.getDamageBounds().height;

			enemyRect.set(el, eb, er, et);

			if (!mike.getState().equals(State.DYING)) {
				if (mike.isJumping()) {
					if ((mikeShadow.y + mikeShadow.height) >= enemyRect.y
							|| mikeShadow.y <= (enemyRect.y + enemyRect.height)) {
						if (mikeShadow.overlaps(enemyRect)) {
							mike.setState(State.DAMAGE);
							mike.setHurt(true);
							if (!mike.isInvicible()) {
								mike.takeDamage(20);
							}
							jumpPressed = false;
							mike.getVelocity().x = 0;
						}
					}
				} else {
					if (mikeDamage.overlaps(enemyRect)) {
						// If Mike hits an enemy, set his state to damaged and set him to hurt.
						// He will not be invincible by default, so have him take damage. After his next update, he will
						// set to be invincible for as long as he is hurt (in the damaged state).
						mike.setState(State.DAMAGE);
						mike.setHurt(true);
						if (!mike.isInvicible()) {
							mike.takeDamage(20);
						}
					}
				}
			}
			enemy.getVelocity().mul(1 / delta);
		}

		if (!mike.isJumping() && !mike.getState().equals(State.DYING)) {
			if (checkForTiles(mike, mikeFeet)) {
				mike.setState(State.DYING);
				deadDegree = 0;
				deadStartX = mike.getPosition().x;
			}
		}

		// checkForDeath(delta);

		// Check for collisions with the left and right sides of the level
		if (mikeFeet.x <= 0 || mikeFeet.x > width - mikeFeet.width - mike.getVelocity().x) {
			mike.getVelocity().x = 0;
		}
		// Check for collisions with the bottom and top sides of the levels
		if (mikeFeet.y <= 0 || mikeFeet.y > height - mikeFeet.height - mike.getVelocity().y) {
			mike.getVelocity().y = 0;
		}

		rectPool.free(mikeFeet);

		// Update the position
		mike.getPosition().add(mike.getVelocity());
		mike.updateDamageBounds(mike.getPosition());
		mike.updateFeetBounds(mike.getPosition());
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

		for (Enemy enemy : enemies) {
			// Check for collisions
			if (chakramRect.overlaps(enemy.getDamageBounds())) {
				chakram.getVelocity().x = 0;
				chakram.getVelocity().y = 0;
				it.remove();
			}
		}
		chakram.getVelocity().mul(1 / delta);
	}

	/**
	 * Check if Mike is dead. If he is, set his motion to the death rise.
	 */
	private boolean checkForDeath(float delta) {
		if (mike.getState().equals(State.DYING)) {

			double radian = deadDegree * (Math.PI / 180);
			float velocityX = (float) Math.sin(radian);
			riseX = deadStartX + velocityX;

			if (velocityX < 0) {
				velocityX = 0;
			}

			mike.getPosition().x = riseX;
			mike.getAcceleration().y = DEATH_ACCELERATION;

			deadDegree += 3;
			if (deadDegree >= 360) {
				deadDegree = 0;
				riseX = 0;
				mike.setState(State.IDLE);
				mike.getPosition().x = level.getStartingPosition().x;
				mike.getPosition().y = level.getStartingPosition().y;
				mike.updateDamageBounds(mike.getPosition());
				mike.updateFeetBounds(mike.getPosition());
				mike.setDirection(Direction.DOWN);
				mike.setHealth(100);
				if (mike.isHurt())
					mike.setHurt(false);
			}
			mike.update(delta);
			return true;
		}
		return false;
	}

}
