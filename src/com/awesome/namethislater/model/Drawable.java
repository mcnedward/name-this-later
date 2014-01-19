package com.awesome.namethislater.model;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Drawable {

	private float y;
	private Sprite sprite;

	public Drawable(float y, Sprite sprite) {
		this.y = y;
		this.sprite = sprite;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(float y) {
		this.y = y;
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

}
