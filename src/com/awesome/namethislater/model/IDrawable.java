package com.awesome.namethislater.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class IDrawable {

	/** The directions that Mike can be facing **/
	public enum Direction {
		DOWN, LEFT, UP, RIGHT, DOWN_LEFT, UP_LEFT, UP_RIGHT, DOWN_RIGHT
	}

	Vector2 position = new Vector2();			// The current position
	Vector2 acceleration = new Vector2();		// The speed of movement
	Vector2 velocity = new Vector2();			// The acceleration and direction of movement

	Sprite sprite = new Sprite();

	Rectangle bounds = new Rectangle();			// The bounds of this drawable

	Direction direction = Direction.DOWN;		// The direction

	float size;
	float stateTime;

	public IDrawable(Vector2 position, float size) {
		this.position = position;
		this.size = size;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = size;
		bounds.width = size;
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
		this.bounds.setX(position.x);
		this.bounds.setY(position.y);
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
	 * @return the sprite
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * @param sprite
	 *            the sprite to set
	 */
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	/**
	 * Sets the sprite to the specified texture region.
	 * 
	 * @param textureRegion
	 *            The TextureRegion to use for this sprite.
	 */
	public void setSpriteRegion(TextureRegion textureRegion) {
		sprite.setRegion(textureRegion);
	}

	/**
	 * Sets the sprite to the specified texture.
	 * 
	 * @param texture
	 *            The Texture to use for this sprite.
	 */
	public void setSpriteRegion(Texture texture) {
		sprite.setRegion(texture);
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
	 * @return the size
	 */
	public float getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(float size) {
		this.size = size;
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
