package com.awesome.namethislater.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IDrawable {

	public void loadSprite(SpriteBatch spriteBatch, float ppuX, float ppuY);

	public void drawShadow(SpriteBatch spriteBatch, float ppuX, float ppuY);

}
