package com.awesome.namethislater.view;

import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIHandler {

	private Stage stage;
	private Table table;
	private ScrollPane scrollPane;
	private Skin skin = new Skin();
	private float width, height, healthX;

	private World world;
	private Level level;
	private Mike mike;

	public UIHandler(World world) {
		this.world = world;
		level = world.getLevel();
		mike = world.getMike();
		stage = new Stage();
		width = stage.getWidth();
		height = stage.getHeight();
	}

	public void render(float delta) {
		stage.clear();
		drawIcon();
		drawHealth();
		drawFps();
		stage.draw();
	}

	private void drawIcon() {
		Image mikeFace = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/mikeface.png"))));
		mikeFace.setScale(2, 2);
		mikeFace.setX(10);
		mikeFace.setY((height - mikeFace.getHeight() * 2) - 10);
		healthX = (10 + mikeFace.getWidth() * 2);

		stage.addActor(mikeFace);
	}

	private void drawHealth() {
		Image healthBar = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/healthbar.png"))));
		healthBar.setX(healthX);
		healthBar.setY(height - 20);

		Image health = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/health.png"))));
		health.setX(healthX);
		health.setY(height - 20);

		float h = mike.getHealth() / 100;
		health.setScaleX(h);

		stage.addActor(healthBar);
		stage.addActor(health);
	}

	private void drawFps() {
		Texture fontTexture = new Texture(Gdx.files.internal("skins/default.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		BitmapFont font = new BitmapFont(Gdx.files.internal("skins/default.fnt"), fontRegion, false);
		font.setUseIntegerPositions(false);

		LabelStyle style = new LabelStyle();
		style.font = font;
		Label label = new Label("FPS: " + Gdx.graphics.getFramesPerSecond(), style);
		label.setX(healthX);
		label.setY(height - 50);

		stage.addActor(label);
	}

	public void dispose() {
		stage.dispose();
	}

	private Image buildScrollPane() {
		// Initialize skin
		// Skin skin = new Skin(Gdx.files.internal("skins/uiskin.json"));

		Texture fontTexture = new Texture(Gdx.files.internal("skins/default.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		TextureRegion fontRegion = new TextureRegion(fontTexture);
		BitmapFont font = new BitmapFont(Gdx.files.internal("skins/default.fnt"), fontRegion, false);
		font.setUseIntegerPositions(false);

		ButtonStyle style = new ButtonStyle();
		style.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("images/mikeface.png"))));
		style.unpressedOffsetX = 5f;
		style.pressedOffsetX = style.unpressedOffsetX + 1f;
		style.pressedOffsetY = -1f;

		Table table = new Table();

		Image mikeFace = new Image(new TextureRegion(new Texture(Gdx.files.internal("images/mikeface.png"))));
		mikeFace.setScale(2, 2);
		mikeFace.setY(height - mikeFace.getHeight() * 2);
		table.add(mikeFace);
		table.setSize(mikeFace.getWidth(), mikeFace.getHeight());
		table.setY(height - table.getHeight() * 2);

		return mikeFace;
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

}
