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
	private static final float ACCELERATION = 0.5f;	// The speed the chakram is thrown

	public Vector2 position = new Vector2();
	Vector2 acceleration = new Vector2();
	Vector2 velocity = new Vector2();
	public Vector2 shadowPosition = new Vector2();
	Ellipse2D shadow;

	Sprite sprite = new Sprite();
	Sprite shadowSprite = new Sprite();

	// Set the bounds of the chakram. Attack bounds use the shadow position to better determine how to collide with an
	// objects.
	public Rectangle bounds = new Rectangle();
	public Rectangle attackBounds = new Rectangle();

	Direction direction;	// The direction that the chakram is thrown

	float stateTime = 0;
	float airHeight = 0;	// If thrown in jump attack, adjust the height of the shadow
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

		// Set the attack bounds of the chakram
		attackBounds.x = x;
		attackBounds.y = y;
		attackBounds.height = SIZE;
		attackBounds.width = SIZE;

		direction = mike.getDirection();	// The direction the chakram was thrown

		this.airHeight = airHeight;			// The height in the air the chakram is

		updateBounds(position);

		// Set the ellipse for the shadow and update its bounds
		shadow = new Ellipse2D.Float();
		update(shadowPosition.x, shadowPosition.y, rotation);

		// Set the position and acceleration of the chakram based on Mike's direction
		switch (direction) {
		case DOWN:
			position.x += SIZE / 4;				// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			break;
		case UP:
			position.x += SIZE / 4;				// Set the chakram in the middle of Mike
			position.y += SIZE / 2;				// Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			break;
		case LEFT:
			position.x -= (SIZE / 2);			// Set the chakram right in front of Mike's hand
			position.y += SIZE / 2;				// Set the chakram in the middle of Mike
			acceleration.x = -ACCELERATION;
			break;
		case RIGHT:
			position.x += SIZE + (SIZE / 3);	// Set the chakram right in front of Mike's hand
			position.y += SIZE / 2;				// Set the chakram in the middle of Mike
			acceleration.x = ACCELERATION;
			break;
		case DOWN_LEFT:
			position.x -= (SIZE / 2);			// Set the chakram right in front of Mike's hand
			position.y += SIZE / 3;				// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case DOWN_RIGHT:
			position.x += SIZE + (SIZE / 3);	// Set the chakram right in front of Mike's hand
			position.y += SIZE / 3;				// Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = ACCELERATION;
			break;
		case UP_LEFT:
			position.x -= (SIZE / 2);			// Set the chakram right in front of Mike's hand
			position.y += SIZE;					//Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case UP_RIGHT:
			position.x += SIZE + (SIZE / 3);	// Set the chakram right in front of Mike's hand
			position.y += SIZE;		 			// Set the chakram in the middle of Mike
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
	public void update(float x, float y, float rotation) {
		this.rotation = rotation;

		// Extra distance to move shadow based on the direction of the throw
		float shadowMove = 0.4f;

		// Update the position of the shadow
		switch (direction) {
		case DOWN:
			shadowPosition.x = position.x;
			shadowPosition.y = y - shadowMove;
			break;
		case UP:
			shadowPosition.x = position.x;
			shadowPosition.y = y - shadowMove;
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
			shadowPosition.y = y - shadowMove;
			break;
		case DOWN_RIGHT:
			shadowPosition.x = x;
			shadowPosition.y = y - shadowMove;
			this.rotation *= -1;					// Switch direction on rotation
			break;
		case UP_LEFT:
			shadowPosition.x = x;
			shadowPosition.y = y - shadowMove;
			break;
		case UP_RIGHT:
			shadowPosition.x = x;
			shadowPosition.y = y - shadowMove;
			this.rotation *= -1;					// Switch direction on rotation
			break;
		}

		shadow.setFrame(shadowPosition.x, shadowPosition.y, SIZE, SIZE);

		attackBounds.x = (float) (shadow.getX() + (shadow.getWidth() / 4));
		attackBounds.y = (float) shadow.getY();
		attackBounds.width = (float) (shadow.getWidth() / 2);
		attackBounds.height = (float) (shadow.getHeight() / 2);
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
	public void loadSprite(SpriteBatch spriteBatch, float ppuX, float ppuY) {
		float x = (float) (position.x * ppuX);
		float y = (float) (position.y * ppuY);

		float width = (float) (SIZE * ppuX);
		float height = (float) (SIZE * ppuX);

		drawShadow(spriteBatch, ppuX, ppuY);

		sprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		sprite.setRotation(rotation);				// Rotate the sprite
		sprite.setBounds(x, y, width, height);		// Set the bounds
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
	public void drawShadow(SpriteBatch spriteBatch, float ppuX, float ppuY) {
		// Get the x and y coordinates to draw. These are the lower left corners of the ellipse.
		float x = (float) (shadow.getX() * ppuX);
		float y = (float) (shadow.getY() * ppuY) - (airHeight * ppuY);

		// Get the width and height of the shadow, and scale them according to the scale percentage.
		float width = (float) (shadow.getWidth() * ppuX);
		float height = (float) (shadow.getHeight() * ppuY);

		shadowSprite.setOrigin(width / 2, height / 2);	// Set the origin in the middle
		shadowSprite.setBounds(x, y, width, height);	// Set the bounds
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding the chakram. Use this for collision detection.
	 * 
	 * @param position
	 *            The current position of the chakram.
	 */
	public void updateBounds(Vector2 position) {
		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;
	}

	/**
	 * @return the attackBounds
	 */
	public Rectangle getAttackBounds() {
		return attackBounds;
	}

	/**
	 * @param attackBounds
	 *            the attackBounds to set
	 */
	public void setAttackBounds(Rectangle attackBounds) {
		this.attackBounds = attackBounds;
	}

	/**
	 * @return the sprite
	 */
	public Sprite getSprite() {
		return sprite;
	}

	/**
	 * Sets the texture for this sprite.
	 * 
	 * @param texture
	 *            The texture for this sprite.
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
	 * @return the shadowSprite
	 */
	public Sprite getShadowSprite() {
		return shadowSprite;
	}

	/**
	 * Sets the texture for this sprite's shadow.
	 * 
	 * @param texture
	 *            The texture for this sprite's shadow.
	 */
	public void setShadowSpriteRegion(Texture texture) {
		shadowSprite.setRegion(texture);
	}

	/**
	 * @param shadowSprite
	 *            the shadowSprite to set
	 */
	public void setShadowSprite(Sprite shadowSprite) {
		this.shadowSprite = shadowSprite;
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
