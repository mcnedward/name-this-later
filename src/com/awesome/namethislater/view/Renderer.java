package com.awesome.namethislater.view;

import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class Renderer {

	/** Camera Width and Height **/
	private static final float CAMERA_WIDTH = 10f;
	private static final float CAMERA_HEIGHT = 7f;
	// The duration of each frame
	private static final float RUNNING_FRAME_DURATION = 0.06f;	// 6 FPS

	private World world;
	private OrthographicCamera cam;	// The camera for the screen

	/** For debug rendering **/
	ShapeRenderer debugRenderer = new ShapeRenderer();

	/** Textures **/
	private TextureRegion mikeFrame;
	private TextureRegion mikeIdleDown;
	private TextureRegion mikeIdleLeft;
	private TextureRegion mikeIdleUp;
	private TextureRegion mikeIdleRight;
	private TextureRegion mikeJumpLeft;
	private TextureRegion mikeJumpRight;
	private Texture blockTexture;
	private Texture spriteSheet;
	private TextureRegion[] regions = new TextureRegion[32];
	private Texture touchPad;

	/** Animations **/
	private Animation walkDown, walkLeft, walkUp, walkRight, downLeft, upLeft, upRight, downRight;

	private SpriteBatch spriteBatch;
	private boolean debug = false;
	private int width, height;
	private float ppuX;			// Pixels per unit on the X axis
	private float ppuY;			// Pixels per unit on the Y axis

	public Renderer(World world, boolean debug) {
		this.world = world;
		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
		this.cam.update();
		this.debug = debug;
		spriteBatch = new SpriteBatch();
		loadTextures();
	}

	private void loadTextures() {
		// Create the sprite sheet as a new Texture. The width is the number of columns in the sheet. The height is the
		// number of rows in the sheet.
		spriteSheet = new Texture(Gdx.files.internal("images/mikespritesheet.png"));
		int width = spriteSheet.getWidth() / 4;
		int height = spriteSheet.getHeight() / 8;

		TextureRegion[] walkDownFrames = new TextureRegion[3];
		TextureRegion[] walkLeftFrames = new TextureRegion[3];
		TextureRegion[] walkUpFrames = new TextureRegion[3];
		TextureRegion[] walkRightFrames = new TextureRegion[3];
		TextureRegion[][] animationFrames = new TextureRegion[4][3];

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 3; j++) {
				int x = j * width;
				int y = i * height;
				animationFrames[i][j] = new TextureRegion(spriteSheet, x, y, width, height);
			}
		}

		mikeIdleDown = new TextureRegion(animationFrames[0][0]);
		mikeIdleLeft = new TextureRegion(animationFrames[1][0]);
		mikeIdleUp = new TextureRegion(animationFrames[2][0]);
		mikeIdleRight = new TextureRegion(animationFrames[3][0]);
		walkDown = new Animation(RUNNING_FRAME_DURATION, animationFrames[0]);
		walkLeft = new Animation(RUNNING_FRAME_DURATION, animationFrames[1]);
		walkUp = new Animation(RUNNING_FRAME_DURATION, animationFrames[2]);
		walkRight = new Animation(RUNNING_FRAME_DURATION, animationFrames[3]);

		touchPad = new Texture(Gdx.files.internal("images/touchpad.png"));

		// TextureRegion[] walkLeftFrames = new TextureRegion[5];
		// for (int i = 0; i < 5; i++) {
		// // The x refers to the current frame that we want to get. Use the modulus of 3 because for Mike's sprite
		// // sheet, there are only three sprites for each direction, but there needs to be 5 fps. The y refers to the
		// // direction that the sprite is facing. In the case of walking left, this should be in row 1.
		// int x = width * (i % 3);
		// int y = 0;
		// walkLeftFrames[i] = new TextureRegion(spriteSheet, x + width, y + height, width, height);
		// }
		// mikeIdleLeft = new TextureRegion(walkLeftFrames[0]);
		// walkLeft = new Animation(RUNNING_FRAME_DURATION, walkLeftFrames);
		//
		// mikeJumpLeft = new TextureRegion(spriteSheet, 3 * width, height, width, height);
		//
		// TextureRegion[] walkRightFrames = new TextureRegion[5];
		// for (int i = 0; i < 5; i++) {
		// // The x refers to the current frame that we want to get. Use the modulus of 3 because for Mike's sprite
		// // sheet, there are only three sprites for each direction, but there needs to be 5 fps. The y refers to the
		// // direction that the sprite is facing. In the case of walking right, this should be in row 3. Technically,
		// // I think I could use flip() on walkingLeftFrames and this would be the same as animating for walking
		// // right...
		// int x = width * (i % 3);
		// int y = 2 * height;
		// walkRightFrames[i] = new TextureRegion(spriteSheet, x + width, y + height, width, height);
		// }
		// mikeIdleRight = new TextureRegion(walkRightFrames[0]);
		// walkRight = new Animation(RUNNING_FRAME_DURATION, walkRightFrames);
		//
		// mikeJumpRight = new TextureRegion(spriteSheet, 3 * width, 3 * height, width, height);

		// mikeTexture = regions[0].getTexture();
		// blockTexture = new Texture(Gdx.files.internal("images/block.png"));
	}

	public void render() {
		spriteBatch.begin();
		drawMike();
		spriteBatch.draw(touchPad, 1 * ppuX, 1 * ppuY, 2 * ppuX, 2 * ppuY);
		spriteBatch.end();
		// drawTouchPad();
		// if (debug)
		// drawDebug();
	}

	private void drawMike() {
		Mike mike = world.getMike();
		switch (mike.getDirection()) {
		case DOWN:
			mikeFrame = mikeIdleDown;
			if (mike.getState().equals(State.RUNNING)) {
				mikeFrame = walkDown.getKeyFrame(mike.getStateTime(), true);
			}
			break;
		case LEFT:
			mikeFrame = mikeIdleLeft;
			if (mike.getState().equals(State.RUNNING)) {
				mikeFrame = walkLeft.getKeyFrame(mike.getStateTime(), true);
			}
			break;
		case UP:
			mikeFrame = mikeIdleUp;
			if (mike.getState().equals(State.RUNNING)) {
				mikeFrame = walkUp.getKeyFrame(mike.getStateTime(), true);
			}
			break;
		case RIGHT:
			mikeFrame = mikeIdleRight;
			if (mike.getState().equals(State.RUNNING)) {
				mikeFrame = walkRight.getKeyFrame(mike.getStateTime(), true);
			}
			break;
		default:
			mikeFrame = mikeIdleDown;
			break;
		}
		// mikeFrame = mike.isFacingLeft() ? mikeIdleLeft : mikeIdleRight;
		// if (mike.getState().equals(State.RUNNING)) {
		// mikeFrame = mike.isFacingLeft() ? walkLeft.getKeyFrame(mike.getStateTime(), true) : walkRight.getKeyFrame(
		// mike.getStateTime(), true);
		// } else if (mike.getState().equals(State.JUMPING)) {
		// mikeFrame = mike.isFacingLeft() ? mikeJumpLeft : mikeJumpRight;
		// }
		spriteBatch.draw(mikeFrame, mike.getPosition().x * ppuX, mike.getPosition().y * ppuY, Mike.SIZE * ppuX,
				Mike.SIZE * ppuY);
	}

	private void drawTouchPad() {
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.identity();
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 0, 0, 1f));
		debugRenderer.ellipse(1, 1, (int) 1, (int) 1);// (CAMERA_WIDTH / 10f,
														// CAMERA_HEIGHT /
		// 8f, CAMERA_WIDTH / 6f,
		// CAMERA_HEIGHT / 4f);
		debugRenderer.end();
	}

	public void drawDebug() {
		// Render blocks
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Filled);
		// for (Block block : world.getDrawableBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
		// Rectangle rect = block.getBounds();
		// debugRenderer.setColor(new Color(1, 0, 0, 1));
		// debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		// }
		// Render Mike
		Mike mike = world.getMike();
		Rectangle rect = mike.getBounds();
		debugRenderer.setColor(new Color(0, 1, 0, 1));
		debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		debugRenderer.end();
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		ppuX = (float) width / CAMERA_WIDTH;
		ppuY = (float) height / CAMERA_HEIGHT;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
