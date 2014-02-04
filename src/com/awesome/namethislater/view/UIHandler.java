package com.awesome.namethislater.view;

import com.awesome.namethislater.controller.Controller;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class UIHandler {

	/** Viewport Width and Height **/
	private static final int VIEWPORT_WIDTH = 400;
	private static final int VIEWPORT_HEIGHT = 240;

	/** Textures **/
	private Texture spriteSheet;
	private TextureRegion idle;
	private TextureRegion jump;
	private TextureRegion attack;
	private TextureRegion damage;
	private TextureRegion swim;
	private TextureRegion death;
	private TextureRegion bar;
	private TextureRegion health;
	private TextureRegion stamina;
	private TextureRegion touch;
	private TextureRegion knob;

	private final Stage stage;
	private TouchpadStyle style;
	private Skin skin;
	private float width, height, healthX;
	private final World world;
	private final Level level;
	private final Mike mike;

	private final Controller controller;

	public UIHandler(World world, Controller controller) {
		this.world = world;
		this.controller = controller;
		level = world.getLevel();
		mike = world.getMike();
		stage = new Stage(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, true);
		width = stage.getWidth();
		height = stage.getHeight();
		loadImages();
		Gdx.input.setInputProcessor(stage);
	}

	public void render(float delta) {
		// stage.clear();
		stage.act(delta);
		stage.draw();
		drawIcon();
		drawHealth();
		drawStamina();
		drawFps();
		drawTouchpad();
	}

	private void drawIcon() {
		Image mikeFace;
		if (mike.isHurt()) {
			mikeFace = new Image(damage);
		} else if (mike.isAttackingState()) {
			mikeFace = new Image(attack);
		} else if (mike.isJumping()) {
			mikeFace = new Image(jump);
		} else if (mike.getState().equals(State.DYING)) {
			mikeFace = new Image(death);
		} else {
			mikeFace = new Image(idle);
		}
		mikeFace.setScale(2, 2);
		mikeFace.setX(10);
		mikeFace.setY(height - (mikeFace.getHeight() * 2) - 10);
		healthX = (10 + mikeFace.getWidth() * 2);

		stage.addActor(mikeFace);
	}

	private void drawHealth() {
		Image healthBar = new Image(bar);
		healthBar.setX(healthX);
		healthBar.setY(height - 20);

		Image h = new Image(health);
		h.setX(healthX);
		h.setY(height - 20);

		float mikeHealth = mike.getHealth() / 100;
		h.setScaleX(mikeHealth);

		stage.addActor(healthBar);
		stage.addActor(h);
	}

	private void drawStamina() {
		Image staminaBar = new Image(bar);
		staminaBar.setX(healthX);
		staminaBar.setY(height - 30);

		Image s = new Image(stamina);
		s.setX(healthX);
		s.setY(height - 30);

		stage.addActor(staminaBar);
		stage.addActor(s);
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

	Drawable backgroundDrawable;
	Drawable knobDrawable;
	Touchpad touchpad;

	private void drawTouchpad() {
		skin = new Skin();
		skin.add("touchBackground", touch);
		skin.add("knob", knob);
		backgroundDrawable = skin.getDrawable("touchBackground");
		knobDrawable = skin.getDrawable("knob");
		style = new TouchpadStyle();
		style.background = backgroundDrawable;
		style.knob = knobDrawable;
		touchpad = new Touchpad(10, style);
		touchpad.setBounds(10, 10, 100, 100);

		touchpad.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				float touch = x;
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				// if (!Gdx.app.getType().equals(ApplicationType.Android))
				// return false;
				// controller.onTouchUp(x, y, touchpad);
				controller.touchUp();
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				// controller.onTouchh(x, y, touchpad);
				controller.touchDown();
			}
		});

		stage.addActor(touchpad);
	}

	private void loadImages() {
		spriteSheet = new Texture(Gdx.files.internal("images/ui.png"));
		idle = new TextureRegion(spriteSheet, 0, 48, 16, 16);
		jump = new TextureRegion(spriteSheet, 16, 48, 16, 16);
		attack = new TextureRegion(spriteSheet, 32, 48, 16, 16);
		damage = new TextureRegion(spriteSheet, 48, 48, 16, 16);
		swim = new TextureRegion(spriteSheet, 64, 48, 16, 16);
		death = new TextureRegion(spriteSheet, 80, 48, 16, 16);
		bar = new TextureRegion(spriteSheet, 0, 0, 64, 16);
		health = new TextureRegion(spriteSheet, 0, 16, 64, 16);
		stamina = new TextureRegion(spriteSheet, 0, 32, 64, 16);
		touch = new TextureRegion(spriteSheet, 64, 0, 32, 32);
		knob = new TextureRegion(spriteSheet, 96, 0, 32, 32);
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
		style.up = new TextureRegionDrawable(new TextureRegion(new Texture(
				Gdx.files.internal("images/mikeface.png"))));
		style.unpressedOffsetX = 5f;
		style.pressedOffsetX = style.unpressedOffsetX + 1f;
		style.pressedOffsetY = -1f;

		Table table = new Table();

		Image mikeFace = new Image(new TextureRegion(new Texture(
				Gdx.files.internal("images/mikeface.png"))));
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