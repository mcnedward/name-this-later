package com.awesome.namethislater.model;

import java.awt.geom.Ellipse2D;
import java.util.Random;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Drawable {

	public static final float SIZE = 1f; 		// The size of the enemy
	private static float ACCELERATION = 15f;	// The speed of walking
	private static final float DAMP = 0.9f;		// Used to smooth out the walking animation
	private static final float MAX_VEL = 4f;

	float currentFrame;

	final int distanceFromEnemy = 100;
	Random random = new Random();
	// Defines time left for movement. set to random int below 3 will move around at most for 3 seconds
	float enemyTime = random.nextInt(5);

	public Enemy(Vector2 position) {
		super(position, SIZE);
		this.position = position;

		bounds.x = position.x;
		bounds.y = position.y;
		bounds.height = SIZE;
		bounds.width = SIZE;

		updateDamageBounds(position);
		setDirection(random.nextInt(7));

		shadow = new Ellipse2D.Float();
	}

	public void update(float delta) {
		stateTime += delta;
		baseY = position.y;
	}

	public void loadSprite(SpriteBatch spriteBatch) {
		float x = position.x;
		float y = position.y;

		float width = SIZE;
		float height = SIZE * 1.5f;

		sprite.setOrigin(width / 2, height / 2); // Set the origin in the middle
		sprite.setBounds(x, y, width, height); // Set the bounds
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
	 * @return the damageBounds
	 */
	public Rectangle getDamageBounds() {
		return damageBounds;
	}

	/**
	 * @param damageBounds
	 *            the damageBounds to set
	 */
	public void setDamageBounds(Rectangle damageBounds) {
		this.damageBounds = damageBounds;
	}

	/**
	 * @return the enemyTime
	 */
	public float getEnemyTime() {
		return enemyTime;
	}

	/**
	 * @param enemyTime
	 *            the enemyTime to set
	 */
	public void setEnemyTime(float enemyTime) {
		this.enemyTime = enemyTime;
	}
}
