package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Chakram extends Drawable implements IDrawable {

	public static float SIZE = 0.5f; // The size of the chakram
	private static final float ACCELERATION = 2f; // The speed the chakram is thrown

	// Set the bounds of the chakram. Attack bounds use the shadow position to better determine how to collide with an
	// objects.
	public Rectangle bounds = new Rectangle();
	public Rectangle attackBounds = new Rectangle();

	Direction direction; // The direction that the chakram is thrown

	float stateTime = 0;
	float airHeight = 0; // If thrown in jump attack, adjust the height of the shadow
	float rotation = 0; // How much to rotate the chakram

	/**
	 * Create a new instance of a chakram. Set the x and y coordinates, the sprite that threw the chakram, and the
	 * height in the air that the chakram is.
	 * 
	 * @param position
	 *            The position of the chakram.
	 * @param mike
	 *            The sprite that threw the chakram.
	 * @param airHeight
	 *            The height in the air that the chakram is. Used when the sprite is jumping.
	 */
	public Chakram(Vector2 position, Mike mike, float airHeight) {
		super(position, SIZE);
		// Set the position of the chakram and it's shadow
		this.position = position;
		shadowPosition.x = position.x;
		shadowPosition.y = position.y;

		// Set the bounds of the chakram
		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		direction = mike.getDirection(); // The direction the chakram was thrown

		this.airHeight = airHeight; // The height in the air the chakram is

		updateBounds(position);

		// Set the ellipse for the shadow and update its bounds
		shadow = new Ellipse2D.Float();
		update(new Vector2(shadowPosition.x, shadowPosition.y), rotation);

		// Set the position and acceleration of the chakram based on Mike's direction
		switch (direction) {
		case DOWN:
			position.x += SIZE / 4; // Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			break;
		case UP:
			position.x += SIZE / 4; // Set the chakram in the middle of Mike
			position.y += SIZE; // Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			break;
		case LEFT:
			position.x -= (SIZE / 2); // Set the chakram right in front of Mike's hand
			position.y += SIZE; // Set the chakram in the middle of Mike
			acceleration.x = -ACCELERATION;
			break;
		case RIGHT:
			position.x += SIZE + (SIZE / 3); // Set the chakram right in front of Mike's hand
			position.y += SIZE; // Set the chakram in the middle of Mike
			acceleration.x = ACCELERATION;
			break;
		case DOWN_LEFT:
			position.x -= (SIZE / 3); // Set the chakram right in front of Mike's hand
			position.y += SIZE / 2; // Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case DOWN_RIGHT:
			position.x += SIZE + (SIZE / 2); // Set the chakram right in front of Mike's hand
			position.y += SIZE / 2; // Set the chakram in the middle of Mike
			acceleration.y = -ACCELERATION;
			acceleration.x = ACCELERATION;
			break;
		case UP_LEFT:
			position.x -= (SIZE / 2); // Set the chakram right in front of Mike's hand
			position.y += SIZE; // Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			acceleration.x = -ACCELERATION;
			break;
		case UP_RIGHT:
			position.x += SIZE + (SIZE / 3); // Set the chakram right in front of Mike's hand
			position.y += SIZE; // Set the chakram in the middle of Mike
			acceleration.y = ACCELERATION;
			acceleration.x = ACCELERATION;
			break;
		}
	}

	/**
	 * Used to update the position of the chakram and it's shadow.
	 * 
	 * @param position
	 *            The position of the chakram.
	 * @param rotation
	 *            The amount to rotate the chakram.
	 */
	public void update(Vector2 position, float rotation) {
		this.rotation = rotation;

		// Extra distance to move shadow based on the direction of the throw
		float shadowMove = 0.4f;

		// Update the position of the shadow
		switch (direction) {
		case DOWN:
			shadowPosition.x = this.position.x;
			shadowPosition.y = position.y - shadowMove;
			break;
		case UP:
			shadowPosition.x = this.position.x;
			shadowPosition.y = position.y - shadowMove;
			break;
		case LEFT:
			shadowPosition.x = position.x;
			break;
		case RIGHT:
			shadowPosition.x = position.x;
			this.rotation *= -1; // Switch direction on rotation
			break;
		case DOWN_LEFT:
			shadowPosition.x = position.x;
			shadowPosition.y = position.y - shadowMove;
			break;
		case DOWN_RIGHT:
			shadowPosition.x = position.x;
			shadowPosition.y = position.y - shadowMove;
			this.rotation *= -1; // Switch direction on rotation
			break;
		case UP_LEFT:
			shadowPosition.x = position.x;
			shadowPosition.y = position.y - shadowMove;
			break;
		case UP_RIGHT:
			shadowPosition.x = position.x;
			shadowPosition.y = position.y - shadowMove;
			this.rotation *= -1; // Switch direction on rotation
			break;
		}

		shadow.setFrame(shadowPosition.x + (SIZE / 4), shadowPosition.y - airHeight, SIZE / 2, SIZE / 2);

		attackBounds.x = (float) shadow.getX();
		attackBounds.y = (float) shadow.getY();
		attackBounds.width = (float) shadow.getHeight();
		attackBounds.height = (float) shadow.getHeight() / 2;
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
	@Override
	public void loadSprite(SpriteBatch spriteBatch) {
		float x = position.x;
		float y = position.y;

		float width = SIZE;
		float height = SIZE;

		drawShadow(spriteBatch);

		sprite.setOrigin(width / 2, height / 2); // Set the origin in the middle
		sprite.setRotation(rotation); // Rotate the sprite
		sprite.setBounds(x, y, width, height); // Set the bounds
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
	@Override
	public void drawShadow(SpriteBatch spriteBatch) {
		// Get the x and y coordinates to draw. These are the lower left corners of the ellipse.
		float x = (float) shadow.getX();
		float y = (float) shadow.getY();

		// Get the width and height of the shadow, and scale them according to the scale percentage.
		float width = (float) shadow.getWidth();
		float height = (float) shadow.getHeight();

		shadowSprite.setOrigin(width / 2, height / 2); // Set the origin in the middle
		shadowSprite.setBounds(x, y, width, height); // Set the bounds
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
}
