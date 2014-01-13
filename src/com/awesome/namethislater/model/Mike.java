package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Mike {

	/** The different states that Mike can be in **/
	public enum State {
		IDLE, RUNNING, JUMPING, FALLING, DYING, DAMAGE, ATTACKING, JUMP_ATTACK
	}

	/** The directions that Mike can be facing **/
	public enum Direction {
		DOWN, LEFT, UP, RIGHT, DOWN_LEFT, UP_LEFT, UP_RIGHT, DOWN_RIGHT
	}

	public static final float SIZE = 1f;	// The size of Mike

	Vector2 position = new Vector2();		// The current position of Mike.
	Vector2 acceleration = new Vector2();	// The speed that Mike should move
	Vector2 velocity = new Vector2();		// The acceleration and direction of Mike's movement
	Vector2 shadowVector = new Vector2();	// The position of Mike's jumping shadow
	Ellipse2D shadow;						// An ellipse used to determine the bounds of the shadow

	Rectangle bounds = new Rectangle();		// The bounds of Mike's sprite rectangle
	Rectangle feetBounds = new Rectangle();	// The bounds of Mike's feet

	State state = State.IDLE;				// The state that Mike is in
	Direction direction = Direction.DOWN;	// The direction Mike is facing

	List<Chakram> chakrams;

	boolean facingLeft = true;				// TODO Is this needed?
	float stateTime = 0;					// The state time that Mike is currently in, determined by last render time
	boolean grounded;						// Whether Mike is on the ground or not
	float shadowPercentage;					// The amount to scale Mike's jumping shadow

	/**
	 * Create a new instance of the Mike sprite model.
	 * 
	 * @param position
	 *            The position to place Mike.
	 */
	public Mike(Vector2 position) {
		this.position = position;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		chakrams = new ArrayList<Chakram>();

		grounded = true;
		updateFeetBounds(position);
		shadowPercentage = 100.0f;

		shadow = new Ellipse2D.Float();
		updateShadow(position.x, position.y, bounds.width, bounds.height, shadowPercentage);
	}

	/**
	 * Update Mike's state time according to the delta.
	 * 
	 * @param delta
	 *            The time in seconds since the last render.
	 */
	public void update(float delta) {
		stateTime += delta;
	}

	public void render(SpriteBatch spriteBatch, Sprite sprite, Texture shadowSprite, float ppuX, float ppuY) {
		float x = (float) (position.x * ppuX);
		float y = (float) (position.y * ppuY);

		float width = (float) (SIZE * ppuX);
		float height = (float) (SIZE * ppuX) * 1.5f;

		if (isJumping())
			drawShadow(spriteBatch, shadowSprite, ppuX, ppuY);

		sprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		sprite.setBounds(x, y, width, height);		// Set the bounds
		sprite.draw(spriteBatch);					// Draw!!!
	}

	/**
	 * Throw a chakram. This creates a new Chakram and adds it to Mike's list of thrown chakrams.
	 */
	public void attack() {
		Chakram chakram = new Chakram(position.x, position.y, this, 0);
		chakrams.add(chakram);
	}

	/**
	 * Throw a chakram. This creates a new Chakram and adds it to Mike's list of thrown chakrams. The air height is used
	 * to determine shadow position of the chakram.
	 * 
	 * @param airHeight
	 *            The height that Mike is in the air.
	 */
	public void jumpAttack(float airHeight) {
		Chakram chakram = new Chakram(position.x, position.y, this, airHeight);
		chakrams.add(chakram);
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
	public void updateShadow(float x, float y, float w, float h, float percentage) {
		shadowPercentage = (percentage / 100);
		getShadow().x = x;
		getShadow().y = y;

		shadow.setFrame(x, y, w, h);
	}

	/**
	 * Used to draw the shadow for Mike's jump. All scaling for the shadow is done here.
	 * 
	 * @param spriteBatch
	 *            The sprite batch used to draw the sprite.
	 * @param texture
	 *            The texture for the shadow to draw.
	 * @param ppuX
	 *            The pixel point units for the x coordinates.
	 * @param ppuY
	 *            The pixel point units for the y coordinates.
	 */
	public void drawShadow(SpriteBatch spriteBatch, Texture texture, float ppuX, float ppuY) {
		// Get the origin x and y. These are used to scale the shadow around the center of the ellipse.
		float originX = (float) (shadow.getCenterX() * ppuX);
		float originY = (float) (shadow.getCenterY() * ppuY);

		// Get the x and y coordinates to draw. These are the lower left corners of the ellipse.
		float x = (float) (shadow.getX() * ppuX);
		float y = (float) (shadow.getY() * ppuY);

		// Get the width and height of the shadow, and scale them according to the scale percentage.
		float width = (float) (shadow.getWidth() * shadowPercentage * ppuX);
		float height = (float) (shadow.getHeight() * shadowPercentage * ppuY);

		// Determine the amount of pixels to move the shadow's x and y coordinates. These are used to keep the scaling
		// of the shadow around the center of the ellipse.
		float moveX = (originX - x) - ((originX - x) * shadowPercentage);
		float moveY = (originY - y) - ((originY - y) * shadowPercentage);

		// Draw the shadow.
		spriteBatch.draw(texture, x + moveX, y + moveY, width, height - moveY);
	}

	/**
	 * Determines whether Mike is jumping, either a normal jump or a jump attack.
	 * 
	 * @return True if Mike is jumping, false otherwise.
	 */
	public boolean isJumping() {
		if (state == State.JUMPING || state == State.JUMP_ATTACK) {
			return true;
		} else
			return false;
	}

	/**
	 * @return the chakras
	 */
	public List<Chakram> getChakrams() {
		return chakrams;
	}

	/**
	 * @param chakras
	 *            the chakras to set
	 */
	public void setChakrams(List<Chakram> chakrams) {
		this.chakrams = chakrams;
	}

	/**
	 * Sets the position of Mike's jumping shadow.
	 * 
	 * @param position
	 *            The position of Mike's jumping shadow.
	 */
	public void setShadow(Vector2 position) {
		shadowVector = position;
	}

	/**
	 * Returns the position of Mike's jumping shadow.
	 * 
	 * @return The position of Mike's jumping shadow.
	 */
	public Vector2 getShadow() {
		return shadowVector;
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
	 * @return the grounded
	 */
	public boolean isGrounded() {
		return grounded;
	}

	/**
	 * @param grounded
	 *            the grounded to set
	 */
	public void setGrounded(boolean grounded) {
		this.grounded = grounded;
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
