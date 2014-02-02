package com.awesome.namethislater.model;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Mike extends Drawable implements IDrawable {

	public static final float SIZE = 1f; // The size of Mike

	/** The different states that Mike can be in **/
	public enum State {
		IDLE, RUNNING, JUMPING, FALLING, DYING, DAMAGE, ATTACKING, JUMP_ATTACK, SWIMMING
	}

	Rectangle feetBounds = new Rectangle(); // The bounds of Mike's feet
	Rectangle jumpingBounds = new Rectangle();

	State state = State.IDLE; // The state that Mike is in
	Direction direction = Direction.DOWN; 	// The direction Mike is facing

	List<Chakram> chakrams;

	boolean grounded; 	// Whether Mike is on the ground or not
	boolean swimming;
	boolean attacking; 	// Whether Mike is attacking or not
	boolean hurt;
	boolean invincible;
	float damageAmount;
	float shadowPercentage; // The amount to scale Mike's jumping shadow

	private float health;

	public Mike(Vector2 position) {
		super(position, SIZE);

		chakrams = new ArrayList<Chakram>();

		grounded = true;
		updateDamageBounds(position);
		updateFeetBounds(position);
		shadowPercentage = 100.0f;

		health = 100;
		hurt = false;
	}

	/**
	 * Update Mike's state time according to the delta.
	 * 
	 * @param delta
	 *            The time in seconds since the last render.
	 */
	public void update(float delta) {
		stateTime += delta;
		baseY = position.y;

		if (health <= 0) {
			state = State.DYING;
		} else {
			// Set Mike to be invincible after he takes one hit.
			invincible = hurt;
		}
	}

	@Override
	public void loadSprite(SpriteBatch spriteBatch) {
		float x = position.x;
		float y = position.y;

		float width = SIZE;
		float height = SIZE * 1.5f;

		if (isJumping()) {
			drawShadow(spriteBatch);
		}

		sprite.setOrigin(width / 2, height / 2); // Set the origin in the middle
		sprite.setBounds(x, y, width, height); // Set the bounds
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
	@Override
	public void drawShadow(SpriteBatch spriteBatch) {
		// Get the origin x and y. These are used to scale the shadow around the center of the ellipse.
		float originX = (float) shadow.getCenterX();
		float originY = (float) shadow.getCenterY();

		// Get the x and y coordinates to draw. These are the lower left corners of the ellipse.
		float x = (float) shadow.getX();
		float y = (float) shadow.getY();

		// Get the width and height of the shadow, and scale them according to the scale percentage.
		float width = (float) shadow.getWidth() * shadowPercentage;
		float height = (float) shadow.getHeight() * shadowPercentage;

		// Determine the amount of pixels to move the shadow's x and y coordinates. These are used to keep the scaling
		// of the shadow around the center of the ellipse.
		float moveX = (originX - x) - ((originX - x) * shadowPercentage);
		float moveY = (originY - y) - ((originY - y) * shadowPercentage);

		shadowSprite.setOrigin(width / 2, height / 2); // Set the origin in the middle
		shadowSprite.setBounds(x + moveX, y + moveY, width, height - moveY); // Set the bounds
	}

	/**
	 * Throw a chakram. This creates a new Chakram and adds it to Mike's list of thrown chakrams.
	 */
	public void attack() {
		Chakram chakram = new Chakram(new Vector2(position.x, position.y), this, 0);
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
		Chakram chakram = new Chakram(new Vector2(position.x, position.y), this, airHeight);
		chakrams.add(chakram);
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding the area where Mike will recieve damage. Use this for
	 * collision detection with
	 * damage and death tiles, and for enemy attacks.
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateDamageBounds(Vector2 position) {
		damageBounds.x = position.x + (SIZE / 8);
		damageBounds.y = position.y;
		damageBounds.width = SIZE * 0.7f;
		damageBounds.height = SIZE / 3;
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding Mike's feet. Use this for collision detection with
	 * floor tiles (pits, water).
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateFeetBounds(Vector2 position) {
		feetBounds.x = position.x + (SIZE / 3);
		feetBounds.y = position.y + (SIZE / 12);
		feetBounds.width = SIZE * 0.3f;
		feetBounds.height = SIZE * 0.2f;
	}

	/**
	 * This is used to update the shadow for Mike.
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateShadow(float x, float y, float percentage) {
		shadowPercentage = (percentage / 100);
		shadow.setFrame(x, y, SIZE, SIZE);
		shadowBounds.x = x + (SIZE / 3);
		shadowBounds.y = y;
		shadowBounds.width = SIZE * 0.3f;
		shadowBounds.height = SIZE * 0.2f;
		updateJumpingBounds(y);
	}

	/**
	 * This is used to update the Rectangle boundaries surrounding Mike's shadow when jumping. Use this for collision
	 * detection with enemies.
	 * 
	 * @param position
	 *            The current position of Mike.
	 */
	public void updateJumpingBounds(float y) {
		float air = feetBounds.y - y;
		jumpingBounds.x = damageBounds.x;
		jumpingBounds.y = y;
		jumpingBounds.width = damageBounds.width;
		jumpingBounds.height = damageBounds.height + air;
	}

	public void takeDamage(float damage) {
		health -= damage;
	}

	/**
	 * Determines whether Mike is jumping, either a normal jump or a jump attack.
	 * 
	 * @return True if Mike is jumping, false otherwise.
	 */
	public boolean isJumping() {
		if (state == State.JUMPING || state == State.JUMP_ATTACK || !grounded)
			return true;
		else
			return false;
	}

	public boolean isAttackingState() {
		if (state == State.ATTACKING || state == State.JUMP_ATTACK || attacking)
			return true;
		else
			return false;
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
	 * @return the swimming
	 */
	public boolean isSwimming() {
		return swimming;
	}

	/**
	 * @param swimming
	 *            the swimming to set
	 */
	public void setSwimming(boolean swimming) {
		this.swimming = swimming;
	}

	/**
	 * @return the attacking
	 */
	public boolean isAttacking() {
		return attacking;
	}

	/**
	 * @param attacking
	 *            the attacking to set
	 */
	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	/**
	 * @return the hurt
	 */
	public boolean isHurt() {
		return hurt;
	}

	/**
	 * @param hurt
	 *            the hurt to set
	 */
	public void setHurt(boolean hurt) {
		this.hurt = hurt;
	}

	/**
	 * @return the invincible
	 */
	public boolean isInvicible() {
		return invincible;
	}

	/**
	 * @param invincible
	 *            the invincible to set
	 */
	public void setInvincible(boolean invincible) {
		this.invincible = invincible;
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
	 * @return the health
	 */
	public float getHealth() {
		return health;
	}

	/**
	 * @param health
	 *            the health to set
	 */
	public void setHealth(float health) {
		this.health = health;
	}

	// TODO Add all states
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
	 * @return the jumpingBounds
	 */
	public Rectangle getJumpingBounds() {
		return jumpingBounds;
	}

	/**
	 * @param jumpingBounds
	 *            the jumpingBounds to set
	 */
	public void setJumpingBounds(Rectangle jumpingBounds) {
		this.jumpingBounds = jumpingBounds;
	}

}