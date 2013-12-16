package com.awesome.namethislater;

import com.awesome.namethislater.screens.GameScreen;
import com.badlogic.gdx.Game;

public class NameThisLater extends Game {

	@Override
	public void create() {
		setScreen(new GameScreen());
	}
}
