package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy {

	public static final float SIZE = 1f;	// The size of the enemy

	Vector2 position = new Vector2();		// The current position of the enemy.
	Vector2 acceleration = new Vector2();	// The speed that the enemy should move
	Vector2 velocity = new Vector2();		// The acceleration and direction of the enemy's movement
	Vector2 shadowVector = new Vector2();	// The position of the enemy's jumping shadow
	Ellipse2D shadow;						// An ellipse used to determine the bounds of the shadow

	Rectangle bounds = new Rectangle();		// The bounds of the enemy's sprite rectangle
	Rectangle damageBounds = new Rectangle();	// The bounds of the enemy's feet

	float stateTime;

	public Enemy(Vector2 position) {
		this.position = position;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		updateDamageBounds(position);

		shadow = new Ellipse2D.Float();
	}

	public void update(float delta) {
		stateTime += delta;
	}

	public void render(SpriteBatch spriteBatch, Sprite sprite, float ppuX, float ppuY) {
		float x = (float) (position.x * ppuX);
		float y = (float) (position.y * ppuY);

		float width = (float) (SIZE * ppuX);
		float height = (float) (SIZE * ppuX) * 1.5f;

		sprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		sprite.setBounds(x, y, width, height);		// Set the bounds
		sprite.draw(spriteBatch);					// Draw!!!
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
		damageBounds.height = SIZE / 2;
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
