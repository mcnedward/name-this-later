package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;

import com.awesome.namethislater.model.Mike.Direction;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Chakram {

	public static float SIZE = 0.5f;				// The size of the chakram
	private static final float ACCELERATION = 4f;	// The speed the chakram is thrown

	public Vector2 position = new Vector2();
	Vector2 acceleration = new Vector2();
	Vector2 velocity = new Vector2();
	public Vector2 shadowPosition = new Vector2();
	Ellipse2D shadow;

	public Rectangle bounds = new Rectangle();

	Direction direction;	// The direction that the chakram is thrown

	float stateTime = 0;

	float airHeight = 0;	// If thrown in jump attack, adjust the height of the shadow
	float shadowMove = 0;	// Extra distance to move shadow based on the direction of the throw
	float rotation = 0;		// How much to rotate the chakram

	/**
	 * Create a new instance of a chakram. Set the x and y coordinates, the sprite that threw the chakram, and the
	 * height in the air that the chakram is.
	 * 
	 * @param x
	 *            The x coordinate of the chakram's starting position.
	 * @param y
	 *            The y coordinate of the chakram's starting position.
	 * @param mike
	 *            The sprite that threw the chakram.
	 * @param airHeight
	 *            The height in the air that the chakram is. Used when the sprite is jumping.
	 */
	public Chakram(float x, float y, Mike mike, float airHeight) {
		// Set the position of the chakram and it's shadow
		position.x = x;
		position.y = y;
		shadowPosition.x = x;
		shadowPosition.y = y;

		// Set the bounds of the chakram
		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		direction = mike.getDirection();	// The direction the chakram was thrown

		this.airHeight = airHeight;			// The height in the air the chakram is

		// Set the ellipse for the shadow and update its bounds
		shadow = new Ellipse2D.Float();
		update(shadowPosition.x, shadowPosition.y, SIZE, SIZE, rotation);

		// Set the position and acceleration of the chakram based on Mike's direction
		switch (direction) {
		case DOWN:
			position.x += mike.bounds.width / 4;	// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			break;
		case UP:
			position.x += mike.bounds.width / 4;	// Set the chakram in the middle of Mike
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			break;
		case LEFT:
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			acceleration.x = -ACCELERATION;
			break;
		case RIGHT:
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			acceleration.x = ACCELERATION;
			break;
		case DOWN_LEFT:
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case DOWN_RIGHT:
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			position.x += mike.bounds.width / 4;	// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = ACCELERATION;
			break;
		case UP_LEFT:
			position.x += mike.bounds.width / 4;	// Set the chakram in the middle of Mike
			position.y += mike.bounds.height / 2;	// Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case UP_RIGHT:
			position.x += mike.bounds.width / 4;	// Set the chakram in the middle of Mike
			position.y += mike.bounds.height / 2; 	// Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			acceleration.x = ACCELERATION;
			break;
		}
	}

	/**
	 * Used to update the position of the chakram and it's shadow.
	 * 
	 * @param x
	 *            The x position of the chakram.
	 * @param y
	 *            The y position of the chakram.
	 * @param w
	 *            The width of the shadow.
	 * @param h
	 *            The width of the shadow.
	 * @param rotation
	 *            The amount to rotate the chakram.
	 */
	public void update(float x, float y, float w, float h, float rotation) {
		this.rotation = rotation;

		// Update the position of the shadow
		switch (direction) {
		case DOWN:
			shadowPosition.x = position.x;
			shadowPosition.y = y;
			shadowMove = 20;
			break;
		case UP:
			shadowPosition.x = position.x;
			shadowPosition.y = y;
			shadowMove = 20;
			break;
		case LEFT:
			shadowPosition.x = x;
			break;
		case RIGHT:
			shadowPosition.x = x;
			this.rotation *= -1;					// Switch direction on rotation
			break;
		case DOWN_LEFT:
			shadowPosition.x = x;
			shadowPosition.y = y;
			shadowMove = 20;
			break;
		case DOWN_RIGHT:
			shadowPosition.x = x;
			shadowPosition.y = y;
			shadowMove = 20;
			this.rotation *= -1;					// Switch direction on rotation
			break;
		case UP_LEFT:
			shadowPosition.x = x;
			shadowPosition.y = y;
			shadowMove = 20;
			break;
		case UP_RIGHT:
			shadowPosition.x = x;
			shadowPosition.y = y;
			shadowMove = 20;
			this.rotation *= -1;					// Switch direction on rotation
			break;
		}
		shadow.setFrame(shadowPosition.x, shadowPosition.y, w, h);
	}

	/**
	 * Draw the chakram.
	 * 
	 * @param spriteBatch
	 *            The sprite batch used to draw the sprite.
	 * @param sprite
	 *            The sprite that represents the chakram.
	 * @param shadowSprite
	 *            The texture that represents the chakram's shadow.
	 * @param ppuX
	 *            The pixel point units for scaling the x coordinates.
	 * @param ppuY
	 *            The pixel point units for scaling the y coordinates.
	 */
	public void draw(SpriteBatch spriteBatch, Sprite sprite, Texture shadowSprite, float ppuX, float ppuY) {
		float x = (float) (position.x * ppuX);
		float y = (float) (position.y * ppuY);

		float width = (float) (SIZE * ppuX);
		float height = (float) (SIZE * ppuX);

		drawShadow(spriteBatch, shadowSprite, ppuX, ppuY);

		sprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		sprite.setRotation(rotation);				// Rotate the sprite
		sprite.setBounds(x, y, width, height);		// Set the bounds
		sprite.draw(spriteBatch);					// Draw!!!
	}

	/**
	 * Used to draw the shadow for each chakram. All scaling for the shadow is done here.
	 * 
	 * @param spriteBatch
	 *            The sprite batch used to draw the sprite.
	 * @param texture
	 *            The texture for the shadow to draw.
	 * @param ppuX
	 *            The pixel point units for scaling the x coordinates.
	 * @param ppuY
	 *            The pixel point units for scaling the y coordinates.
	 */
	public void drawShadow(SpriteBatch spriteBatch, Texture texture, float ppuX, float ppuY) {
		// Get the x and y coordinates to draw. These are the lower left corners of the ellipse.
		float x = (float) (shadow.getX() * ppuX);
		float y = (float) (shadow.getY() * ppuY) - shadowMove - (airHeight * ppuY);

		// Get the width and height of the shadow, and scale them according to the scale percentage.
		float width = (float) (shadow.getWidth() * ppuX);
		float height = (float) (shadow.getHeight() * ppuY);

		// Draw the shadow.
		spriteBatch.draw(texture, x, y, width, height);
	}

	/**
	 * @return the shadowVector
	 */
	public Vector2 getShadowVector() {
		return shadowPosition;
	}

	/**
	 * @param shadowVector
	 *            the shadowVector to set
	 */
	public void setShadowVector(Vector2 shadowVector) {
		this.shadowPosition = shadowVector;
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
