package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;
import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

	public static final float SIZE = 1f;			// The size of the enemy

	/** The directions that the enemy can be facing **/
	public enum Direction {
		DOWN, LEFT, UP, RIGHT, DOWN_LEFT, UP_LEFT, UP_RIGHT, DOWN_RIGHT
	}

	Vector2 position = new Vector2();				// The current position of the enemy.
	Vector2 acceleration = new Vector2();			// The speed that the enemy should move
	Vector2 velocity = new Vector2();				// The acceleration and direction of the enemy's movement
	Vector2 shadowVector = new Vector2();			// The position of the enemy's jumping shadow
	Ellipse2D shadow;								// An ellipse used to determine the bounds of the shadow

	Sprite sprite = new Sprite();

	Rectangle bounds = new Rectangle();				// The bounds of the enemy's sprite rectangle
	Rectangle damageBounds = new Rectangle();		// The bounds of the enemy's feet

	Direction direction = Direction.DOWN;			// The direction the enemy is facing

	float stateTime;
	float currentFrame;

	final int distanceFromSamus = 100;
	Random r = new Random();
	int x = 500;
	final int speed = 1;

	public Enemy(Vector2 position) {
		this.position = position;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		updateDamageBounds(position);
		setDirection(r.nextInt(7));

		shadow = new Ellipse2D.Float();
	}

	public void loadSprite(SpriteBatch spriteBatch, float ppuX, float ppuY) {
		float x = (float) (position.x * ppuX);
		float y = (float) (position.y * ppuY);

		float width = (float) (SIZE * ppuX);
		float height = (float) (SIZE * ppuX) * 1.5f;

		sprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		sprite.setBounds(x, y, width, height);		// Set the bounds
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding for the damage area. Use this for collision
	 * detection.
	 * 
	 * @param position
	 *            The current position of the enemy.
	 */
	public void updateDamageBounds(Vector2 position) {
		damageBounds.x = position.x;
		damageBounds.y = position.y;
		damageBounds.width = SIZE;
		damageBounds.height = SIZE / 3;
	}

	/**
	 * 
	 * @return The direction that the enemy is currently facing.<br>
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
	 * Use this method when setting a random direction for enemy movement.
	 * 
	 * @param direction
	 *            Set the direction that the enemy should face.<br>
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
	public void setDirection(int direction) {
		switch (direction) {
		case 0:
			this.direction = Direction.DOWN;
			break;
		case 1:
			this.direction = Direction.LEFT;
			break;
		case 2:
			this.direction = Direction.UP;
			break;
		case 3:
			this.direction = Direction.RIGHT;
			break;
		case 4:
			this.direction = Direction.DOWN_LEFT;
			break;
		case 5:
			this.direction = Direction.DOWN_RIGHT;
			break;
		case 6:
			this.direction = Direction.UP_LEFT;
			break;
		case 7:
			this.direction = Direction.UP_RIGHT;
			break;
		}
	}

	/**
	 * 
	 * @param direction
	 *            Set the direction that the enemy should face.<br>
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
	 * @return the sprite
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Set the sprite texture for the enemy.
	 * 
	 * @param texture
	 *            The texture for this enemy.
	 */
	public void setSpriteRegion(Texture texture) {
		sprite.setRegion(texture);
	}

	/**
	 * @param sprite
	 *            the sprite to set
	 */
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	/**
	 * @return the position
	 */
	public Vector2 getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Vector2 position) {
		this.position = position;
	}

	/**
	 * @return the acceleration
	 */
	public Vector2 getAcceleration() {
		return acceleration;
	}

	/**
	 * @param acceleration
	 *            the acceleration to set
	 */
	public void setAcceleration(Vector2 acceleration) {
		this.acceleration = acceleration;
	}

	/**
	 * @return the velocity
	 */
	public Vector2 getVelocity() {
		return velocity;
	}

	/**
	 * @param velocity
	 *            the velocity to set
	 */
	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	/**
	 * @return the shadowVector
	 */
	public Vector2 getShadowVector() {
		return shadowVector;
	}

	/**
	 * @param shadowVector
	 *            the shadowVector to set
	 */
	public void setShadowVector(Vector2 shadowVector) {
		this.shadowVector = shadowVector;
	}

	/**
	 * @return the shadow
	 */
	public Ellipse2D getShadow() {
		return shadow;
	}

	/**
	 * @param shadow
	 *            the shadow to set
	 */
	public void setShadow(Ellipse2D shadow) {
		this.shadow = shadow;
	}

	/**
	 * @return the bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * @param bounds
	 *            the bounds to set
	 */
	public void setBounds(Rectangle bounds) {
		this.bounds = bounds;
	}

	/**
	 * @return the feetBounds
	 */
	public Rectangle getDamageBounds() {
		return damageBounds;
	}

	/**
	 * @param feetBounds
	 *            the feetBounds to set
	 */
	public void setDamageBounds(Rectangle damageBounds) {
		this.damageBounds = damageBounds;
	}

	/**
	 * @return the stateTime
	 */
	public float getStateTime() {
		return stateTime;
	}

	/**
	 * @param stateTime
	 *            the stateTime to set
	 */
	public void setStateTime(float stateTime) {
		this.stateTime = stateTime;
	}

}
