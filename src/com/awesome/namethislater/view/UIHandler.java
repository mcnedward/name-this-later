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
import com.badlogic.gdx.scenes.scene2d.ui.Button;
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
	private Texture fontTexture;

	/** Images **/
	private Image mikeFace = null;
	private Image idleIcon = null;
	private Image jumpIcon = null;
	private Image attackIcon = null;
	private Image damageIcon = null;
	private Image deathIcon = null;
	private Image healthBar = null;
	private Image currentHealth = null;
	private Image staminaBar = null;
	private Image currentStamina = null;

	/** Drawables **/
	private Drawable backgroundDrawable;
	private Drawable knobDrawable;
	private Drawable jumpButtonUp;
	private Drawable jumpButtonDown;
	private Drawable attackButtonUp;
	private Drawable attackButtonDown;

	/** Text Stuff **/
	private Label fpsLabel = null;
	private LabelStyle labelStyle = null;
	private TextureRegion fontRegion = null;
	private BitmapFont font = null;

	/** Touchpad **/
	private Skin touchSkin;
	private TouchpadStyle touchpadStyle;
	private Touchpad touchpad;

	/** Jump Button **/
	private Skin jumpSkin;
	private ButtonStyle jumpStyle;
	private Button jumpButton;

	/** Jump Button **/
	private Skin attackSkin;
	private ButtonStyle attackStyle;
	private Button attackButton;

	private final Stage stage;
	private final World world;
	private final Level level;
	private final Mike mike;
	private final Controller controller;

	private float width, height, healthX;
	private final boolean android;

	public UIHandler(World world, Controller controller, boolean android) {
		this.world = world;
		this.controller = controller;
		this.android = android;

		level = world.getLevel();
		mike = world.getMike();
		stage = new Stage(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, true);
		width = stage.getWidth();
		height = stage.getHeight();

		loadImages();
		loadText();
		drawTouchpad();
		drawJumpButton();
		drawAttackButton();

		if (android) {
			Gdx.input.setInputProcessor(stage);
		}
	}

	public void render(float delta) {
		drawIcon();
		drawHealth();
		drawStamina();
		drawFps();
		stage.act(delta);
		stage.draw();
	}

	private void drawIcon() {
		// Remove the actor to clear his state
		stage.getRoot().removeActor(mikeFace);

		if (mike.isHurt()) {
			mikeFace = damageIcon;
		} else if (mike.isAttackingState()) {
			mikeFace = attackIcon;
		} else if (mike.isJumping()) {
			mikeFace = jumpIcon;
		} else if (mike.getState().equals(State.DYING)) {
			mikeFace = deathIcon;
		} else {
			mikeFace = idleIcon;
		}
		mikeFace.setScale(2, 2);
		mikeFace.setX(10);
		mikeFace.setY(height - (mikeFace.getHeight() * 2) - 10);
		healthX = (10 + mikeFace.getWidth() * 2);

		stage.addActor(mikeFace);
	}

	private void drawHealth() {
		healthBar.setX(healthX);
		healthBar.setY(height - 20);

		currentHealth.setX(healthX);
		currentHealth.setY(height - 20);

		float mikeHealth = mike.getHealth() / 100;
		currentHealth.setScaleX(mikeHealth);

		stage.addActor(healthBar);
		stage.addActor(currentHealth);
	}

	private void drawStamina() {
		staminaBar.setX(healthX);
		staminaBar.setY(height - 30);

		currentStamina.setX(healthX);
		currentStamina.setY(height - 30);

		stage.addActor(staminaBar);
		stage.addActor(currentStamina);
	}

	private void drawFps() {
		fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
	}

	private void drawTouchpad() {
		touchSkin = new Skin();
		touchSkin.add("touchBackground", touch);
		touchSkin.add("knob", knob);
		backgroundDrawable = touchSkin.getDrawable("touchBackground");
		knobDrawable = touchSkin.getDrawable("knob");
		touchpadStyle = new TouchpadStyle();
		touchpadStyle.knob = knobDrawable;
		touchpadStyle.background = backgroundDrawable;
		touchpad = new Touchpad(10, touchpadStyle);
		touchpad.setBounds(10, 10, 100, 100);

		touchpad.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				// if (!Gdx.app.getType().equals(ApplicationType.Android))
				// return false;
				// controller.onTouchUp(x, y, touchpad);
				controller.onTouchpadUp();
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				controller.onTouchpadDragged(x, y, touchpad);
			}
		});

		stage.addActor(touchpad);
	}

	private void drawJumpButton() {
		jumpSkin = new Skin();
		jumpSkin.add("buttonUp", knob);
		jumpSkin.add("buttonDown", touch);
		jumpButtonUp = jumpSkin.getDrawable("buttonUp");
		jumpButtonDown = jumpSkin.getDrawable("buttonDown");

		jumpStyle = new ButtonStyle();
		jumpStyle.up = jumpButtonUp;
		jumpStyle.down = jumpButtonDown;
		jumpButton = new Button(jumpStyle);
		jumpButton.setBounds(width - 80 - jumpButton.getWidth(), 15, 50, 50);

		jumpButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return controller.onJumpButtonDown();
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				controller.onJumpButtonUp();
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {

			}
		});

		stage.addActor(jumpButton);
	}

	private void drawAttackButton() {
		attackSkin = new Skin();
		attackSkin.add("buttonUp", knob);
		attackSkin.add("buttonDown", touch);
		attackButtonUp = attackSkin.getDrawable("buttonUp");
		attackButtonDown = attackSkin.getDrawable("buttonDown");

		attackStyle = new ButtonStyle();
		attackStyle.up = attackButtonUp;
		attackStyle.down = attackButtonDown;
		attackButton = new Button(attackStyle);
		attackButton.setBounds(width - 60, 25, 50, 50);

		attackButton.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return controller.onAttackButtonDown();
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				controller.onAttackButtonUp();
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {

			}
		});

		stage.addActor(attackButton);
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

		idleIcon = new Image(idle);
		jumpIcon = new Image(jump);
		attackIcon = new Image(attack);
		damageIcon = new Image(damage);
		deathIcon = new Image(death);

		healthBar = new Image(bar);
		currentHealth = new Image(health);
		staminaBar = new Image(bar);
		currentStamina = new Image(stamina);
	}

	private void loadText() {
		fontTexture = new Texture(Gdx.files.internal("skins/default.png"));
		fontTexture.setFilter(TextureFilter.Linear, TextureFilter.MipMapLinearLinear);
		fontRegion = new TextureRegion(fontTexture);
		font = new BitmapFont(Gdx.files.internal("skins/default.fnt"), fontRegion, false);
		font.setUseIntegerPositions(false);

		labelStyle = new LabelStyle();
		labelStyle.font = font;
		fpsLabel = new Label("FPS: " + Gdx.graphics.getFramesPerSecond(), labelStyle);
		fpsLabel.setX(width - 60);
		fpsLabel.setY(height - 30);

		stage.addActor(fpsLabel);
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