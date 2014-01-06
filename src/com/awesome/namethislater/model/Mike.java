package com.awesome.namethislater.model;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Mike {

	/** The different states that Mike can be in **/
	public enum State {
		IDLE, RUNNING, JUMPING, FALLING, DYING, ATTACKING
	}

	/** The directions that Mike can be facing **/
	public enum Direction {
		DOWN, LEFT, UP, RIGHT, DOWN_LEFT, UP_LEFT, UP_RIGHT, DOWN_RIGHT
	}

	public static final float SIZE = 1f;	// The size of Mike

	Vector2 position = new Vector2();
	Vector2 acceleration = new Vector2();
	Vector2 velocity = new Vector2();
	Vector2 shadow = new Vector2();

	Rectangle bounds = new Rectangle();
	Rectangle feetBounds = new Rectangle();

	State state = State.IDLE;
	Direction direction = Direction.DOWN;
	boolean facingLeft = true;
	float stateTime = 0;
	boolean longJump = false;

	public Mike(Vector2 position) {
		this.position = position;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		updateFeetBounds(position);
		updateShadow(position);
	}

	public void update(float delta) {
		stateTime += delta;
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding Mike's feet. Use this for collision detection with
	 * floor tiles (pits, water).
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateFeetBounds(Vector2 position) {
		float width = (float) (SIZE * (2.0 / 3));
		feetBounds.x = position.x + (SIZE / 7);
		feetBounds.y = position.y;
		feetBounds.width = width;
		feetBounds.height = SIZE / 3;
	}

	/**
	 * This is used to update the shadow for Mike.
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateShadow(Vector2 position) {
		shadow.x = position.x + (SIZE / 2);
		shadow.y = position.y + (SIZE / 2);
	}

	public void setShadow(Vector2 position) {
		shadow = position;
	}

	public Vector2 getShadow() {
		return shadow;
	}

	/**
	 * @return The current position of Mike.<br>
	 *         You can use this to get the x and y position of the vector of Mike.
	 */
	public Vector2 getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            Sets the current position. This also sets the bounds of Mike, for collision detection.
	 */
	public void setPosition(Vector2 position) {
		this.position = position;
		this.bounds.setX(position.x);
		this.bounds.setY(position.y);
	}

	/**
	 * @return True if it should be a long jump, false if not.
	 */
	public boolean isLongJump() {
		return longJump;
	}

	/**
	 * @param longJump
	 *            True if it should be a long jump, false if not.
	 */
	public void setLongJump(boolean longJump) {
		this.longJump = longJump;
	}

	/**
	 * @return The current acceleration of Mike. This is used to determine how fast he should be moving along the
	 *         horizontal X axis.
	 */
	public Vector2 getAcceleration() {
		return acceleration;
	}

	/**
	 * @param acceleration
	 *            The acceleration along the X axis that Mike should move.
	 */
	public void setAcceleration(Vector2 acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * @return The velocity of Mike. Used to determine direction that that Mike's position will be in.
	 */
	public Vector2 getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity
	 *            The velocity of movement to set for Mike.
	 */
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return The rectangular bounds of Mike, used for collision detection.
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * @param bounds
	 *            The rectangular bounds of Mike, used for collision detection.
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the feetBounds
	 */
	public Rectangle getFeetBounds() {
		return feetBounds;
	}

	/**
	 * @param feetBounds
	 *            the feetBounds to set
	 */
	public void setFeetBounds(Rectangle feetBounds) {
		this.feetBounds = feetBounds;
	}

	/**
	 * @return The current state that Mike is in at the moment.<br>
	 *         The States are:<br>
	 *         - Idle<br>
	 *         -Running<br>
	 *         -Jumping<br>
	 *         -Dying
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param state
	 *            Set current state that Mike should be in at the moment.<br>
	 *            The States are:<br>
	 *            - Idle<br>
	 *            -Running<br>
	 *            -Jumping<br>
	 *            -Dying
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * 
	 * @return The direction that Mike is currently facing.<br>
	 *         The Directions are: <br>
	 *         - Up<br>
	 *         - Left<br>
	 *         - Down<br>
	 *         - Right<br>
	 *         - Down-Left<br>
	 *         - Up-Left<br>
	 *         - Up-Right<br>
	 *         - Down-Right
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * 
	 * @param direction
	 *            Set the direction that Mike should face.<br>
	 *            The Directions are: <br>
	 *            - Up<br>
	 *            - Left<br>
	 *            - Down<br>
	 *            - Right<br>
	 *            - Down-Left<br>
	 *            - Up-Left<br>
	 *            - Up-Right<br>
	 *            - Down-Right
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	/**
	 * @return The state time that Mike is in.
	 */
	public float getStateTime() {
		return stateTime;
	}

	/**
	 * @param stateTime
	 *            The state time that Mike is in.
	 */
	public void setStateTime(float stateTime) {
		this.stateTime = stateTime;
	}
}
