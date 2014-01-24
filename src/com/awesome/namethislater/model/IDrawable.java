package com.awesome.namethislater.model;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IDrawable {

	public void loadSprite(SpriteBatch spriteBatch);

	public void drawShadow(SpriteBatch spriteBatch);

}