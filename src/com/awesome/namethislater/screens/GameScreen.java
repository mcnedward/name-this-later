package com.awesome.namethislater.screens;

import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.controller.MikeController;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.World;
import com.awesome.namethislater.view.Basic3DRenderer;
import com.awesome.namethislater.view.Renderer;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;

public class GameScreen implements Screen, InputProcessor {

	private World world;
	private Level level;
	private Renderer renderer;
	private Basic3DRenderer renderer3D;
	private MikeController controller;

	private int width, height;

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL10.GL_LINE_SMOOTH);
		Gdx.gl.glEnable(GL10.GL_POINT_SMOOTH);
		Gdx.gl.glHint(GL10.GL_POLYGON_SMOOTH_HINT, GL10.GL_NICEST);
		Gdx.gl.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST);

		controller.update(delta);
		renderer.render(delta);
		// renderer3D.render();
	}

	@Override
	public void resize(int width, int height) {
		renderer.setSize(width, height);
		// renderer3D.setSize(width, height, 0);
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void show() {
		world = new World();
		level = world.getLevel();
		renderer = new Renderer(world, false);
		// renderer3D = new Basic3DRenderer(world, false);
		controller = new MikeController(world);
		Gdx.input.setInputProcessor(this);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.DOWN:
			controller.downPressed();
			break;
		case Keys.UP:
			controller.upPressed();
			break;
		case Keys.LEFT:
			controller.leftPressed();
			break;
		case Keys.RIGHT:
			controller.rightPressed();
			break;
		case Keys.SPACE:
			controller.jumpPressed();
			break;
		case Keys.S:
			controller.attackPressed();
			break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.DOWN:
			controller.downReleased();
			break;
		case Keys.LEFT:
			controller.leftReleased();
			break;
		case Keys.UP:
			controller.upReleased();
			break;
		case Keys.RIGHT:
			controller.rightReleased();
			break;
		case Keys.SPACE:
			controller.jumpReleased();
			break;
		case Keys.S:
			controller.attackReleased();
			break;
		case Keys.D:
			renderer.setDebug(!renderer.isDebug());
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (!Gdx.app.getType().equals(ApplicationType.Android))
			return false;
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		if (!Gdx.app.getType().equals(ApplicationType.Android))
			return false;
		controller.onTouchUp(Gdx.input.getX(pointer), Gdx.input.getY(pointer), renderer);
		return true;
	}

	@Override
	public boolean touchDragged(int x, int y, int pointer) {
		controller.onTouch(Gdx.input.getX(pointer), Gdx.input.getY(pointer), renderer);
		return false;
	}

	private Map<Integer, TouchInfo> touches = new HashMap<Integer, TouchInfo>();

	class TouchInfo {
		public float x = 0;
		public float y = 0;
		public boolean touched = false;

		public TouchInfo(float x, float y) {
			this.x = x;
			this.y = y;
			touched = true;
		}
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
