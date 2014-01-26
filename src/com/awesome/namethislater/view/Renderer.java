package com.awesome.namethislater.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.model.Block;
import com.awesome.namethislater.model.Chakram;
import com.awesome.namethislater.model.Drawable;
import com.awesome.namethislater.model.Drawable.Direction;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.Room;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Renderer {

	/** Camera Width and Height **/
	private static final float CAMERA_WIDTH = 10f;
	private static final float CAMERA_HEIGHT = 7f;
	// The duration of each frame
	private static final float RUNNING_FRAME_DURATION = 0.1f; // 10 FPS
	private static final float ATTACKING_FRAME_DURATION = 0.2f;

	private OrthographicCamera camera; // The camera for the screen

	/** For debug rendering **/
	ShapeRenderer debugRenderer = new ShapeRenderer();

	/** Textures **/
	private Texture spriteSheet; // The sprite sheet for movement
	private Texture attackSheet; // The sprite sheet for ground attacks
	private Texture jumpAttackSheet; // The sprite sheet for jump attacks
	private TextureRegion mikeFrame; // The region of the current frame for Mike
	private Texture dead; // The texture for the death state
	private Texture damage; // The texture for the damge state
	private Texture chakram; // The texture for chakrams
	private Texture shadow; // The texture for the jump shadow
	private Texture enemyTexture; // The texture for the enemy
	private Texture touchPad; // The texture for the touch pad buttons
	private Texture grass; // The texture for grass blocks
	private Texture water; // The texture for water blocks

	/** Animation and Texture Maps **/
	private final Map<Direction, Animation> animationMap = new HashMap<Direction, Animation>();
	private final Map<Direction, TextureRegion> idleMap = new HashMap<Direction, TextureRegion>();
	private final Map<Direction, TextureRegion> jumpMap = new HashMap<Direction, TextureRegion>();
	private final Map<Direction, Animation> attackMap = new HashMap<Direction, Animation>();
	private final Map<Direction, Animation> jumpAttackMap = new HashMap<Direction, Animation>();

	private final SpriteBatch spriteBatch;
	private boolean debug = false;
	public int width, height;
	private float ppuX; // Pixels per unit on the X axis
	private float ppuY; // Pixels per unit on the Y axis

	float stateTime; // The time since last render
	float currentFrame; // The current frame, based on the state time

	private final World world;
	private final Level level;
	private final Room room;
	private final Mike mike;
	private final Enemy enemy;

	private TiledMap map;
	private final OrthogonalTiledMapRenderer renderer;

	private BitmapFont font;

	public Renderer(World world, boolean debug) {
		this.world = world;
		this.level = world.getLevel();
		this.room = world.getRoom();
		mike = world.getMike();
		enemy = level.getEnemy();
		map = level.getMap();
		renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);

		camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		camera.position.set(CAMERA_WIDTH, CAMERA_HEIGHT, 0);
		camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);

		this.debug = debug;
		spriteBatch = new SpriteBatch();
		stateTime = 0f;

		loadTextures();
	}

	private void loadTextures() {
		// Create the sprite sheet as a new Texture. The width is the number of
		// columns in the sheet. The height is the number of rows in the sheet.
		spriteSheet = new Texture(Gdx.files.internal("images/mikespritesheetdetailed.png"));
		int width = spriteSheet.getWidth() / 4;
		int height = spriteSheet.getHeight() / 8;

		// 2-Dimensional array that will hold all the frames for animating the
		// sprite
		TextureRegion[][] animationFrames = new TextureRegion[8][3];
		TextureRegion[][] jumpFrames = new TextureRegion[8][1];

		// Loop through the sprite sheet and cut out each frame
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				int x = j * width;
				int y = i * height;
				if (j == 3) {
					jumpFrames[i][0] = new TextureRegion(spriteSheet, x, y, width, height);
				} else {
					animationFrames[i][j] = new TextureRegion(spriteSheet, x, y, width, height);
				}
			}
		}

		// Set the frames for the Idle state
		idleMap.put(Direction.DOWN, new TextureRegion(animationFrames[0][0]));
		idleMap.put(Direction.LEFT, new TextureRegion(animationFrames[1][0]));
		idleMap.put(Direction.UP, new TextureRegion(animationFrames[2][0]));
		idleMap.put(Direction.RIGHT, new TextureRegion(animationFrames[3][0]));
		idleMap.put(Direction.DOWN_LEFT, new TextureRegion(animationFrames[4][0]));
		idleMap.put(Direction.UP_LEFT, new TextureRegion(animationFrames[5][0]));
		idleMap.put(Direction.UP_RIGHT, new TextureRegion(animationFrames[6][0]));
		idleMap.put(Direction.DOWN_RIGHT, new TextureRegion(animationFrames[7][0]));
		// Set the running animation for each direction
		animationMap.put(Direction.DOWN, new Animation(RUNNING_FRAME_DURATION, animationFrames[0]));
		animationMap.put(Direction.LEFT, new Animation(RUNNING_FRAME_DURATION, animationFrames[1]));
		animationMap.put(Direction.UP, new Animation(RUNNING_FRAME_DURATION, animationFrames[2]));
		animationMap.put(Direction.RIGHT, new Animation(RUNNING_FRAME_DURATION, animationFrames[3]));
		animationMap.put(Direction.DOWN_LEFT, new Animation(RUNNING_FRAME_DURATION, animationFrames[4]));
		animationMap.put(Direction.UP_LEFT, new Animation(RUNNING_FRAME_DURATION, animationFrames[5]));
		animationMap.put(Direction.UP_RIGHT, new Animation(RUNNING_FRAME_DURATION, animationFrames[6]));
		animationMap.put(Direction.DOWN_RIGHT, new Animation(RUNNING_FRAME_DURATION, animationFrames[7]));
		// Set the jump for each direction
		jumpMap.put(Direction.DOWN, new TextureRegion(jumpFrames[0][0]));
		jumpMap.put(Direction.LEFT, new TextureRegion(jumpFrames[1][0]));
		jumpMap.put(Direction.UP, new TextureRegion(jumpFrames[2][0]));
		jumpMap.put(Direction.RIGHT, new TextureRegion(jumpFrames[3][0]));
		jumpMap.put(Direction.DOWN_LEFT, new TextureRegion(jumpFrames[4][0]));
		jumpMap.put(Direction.UP_LEFT, new TextureRegion(jumpFrames[5][0]));
		jumpMap.put(Direction.UP_RIGHT, new TextureRegion(jumpFrames[6][0]));
		jumpMap.put(Direction.DOWN_RIGHT, new TextureRegion(jumpFrames[7][0]));

		// Cut out the sprites from the attack sheet
		attackSheet = new Texture(Gdx.files.internal("images/attacking.png"));
		int w = attackSheet.getWidth() / 2;
		int h = attackSheet.getHeight() / 8;

		// Cut out the sprites from the attack sheet
		jumpAttackSheet = new Texture(Gdx.files.internal("images/jumpattack.png"));
		int w2 = jumpAttackSheet.getWidth() / 2;
		int h2 = jumpAttackSheet.getHeight() / 8;

		TextureRegion[][] attackFrames = new TextureRegion[8][2];

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 2; j++) {
				int x = j * w;
				int y = i * h;
				attackFrames[i][j] = new TextureRegion(attackSheet, x, y, w, h);
			}
		}

		TextureRegion[][] jumpAttackFrames = new TextureRegion[8][2];

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 2; j++) {
				int x = j * w2;
				int y = i * h2;
				jumpAttackFrames[i][j] = new TextureRegion(jumpAttackSheet, x, y, w, h);
			}
		}

		// Set the attacking animation for each direction
		attackMap.put(Direction.DOWN, new Animation(ATTACKING_FRAME_DURATION, attackFrames[0]));
		attackMap.put(Direction.LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[1]));
		attackMap.put(Direction.UP, new Animation(ATTACKING_FRAME_DURATION, attackFrames[2]));
		attackMap.put(Direction.RIGHT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[3]));
		attackMap.put(Direction.DOWN_LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[4]));
		attackMap.put(Direction.UP_LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[5]));
		attackMap.put(Direction.UP_RIGHT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[6]));
		attackMap.put(Direction.DOWN_RIGHT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[7]));
		// Set the jump attacking animation for each direction
		jumpAttackMap.put(Direction.DOWN, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[0]));
		jumpAttackMap.put(Direction.LEFT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[1]));
		jumpAttackMap.put(Direction.UP, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[2]));
		jumpAttackMap.put(Direction.RIGHT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[3]));
		jumpAttackMap.put(Direction.DOWN_LEFT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[4]));
		jumpAttackMap.put(Direction.UP_LEFT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[5]));
		jumpAttackMap.put(Direction.UP_RIGHT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[6]));
		jumpAttackMap.put(Direction.DOWN_RIGHT, new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[7]));

		dead = new Texture(Gdx.files.internal("images/dead.png"));
		damage = new Texture(Gdx.files.internal("images/damage.png"));

		shadow = new Texture(Gdx.files.internal("images/shadow.png"));
		chakram = new Texture(Gdx.files.internal("images/chakra.png"));
		mike.setShadowSpriteRegion(shadow);

		enemyTexture = new Texture(Gdx.files.internal("images/enemy.png"));

		touchPad = new Texture(Gdx.files.internal("images/touchpad.png"));
		grass = new Texture(Gdx.files.internal("images/grass.png"));
		water = new Texture(Gdx.files.internal("images/water.png"));
	}

	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		setCamera();
		renderer.setView(camera);
		renderer.render(new int[] { 0 });

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		// drawBlocks();
		drawEnemy();
		drawMike(delta);
		drawChakrams();
		drawSprites();
		// drawButtons();
		spriteBatch.end();
		renderer.render(new int[] { 1 });

		drawCollisionBlocks();
		// drawTouchPad();
		if (debug) {
			drawDebug();
		}
	}

	/**
	 * Set the position of the camera to surround the boundaries of the map.<br>
	 * Source: http://xiopod.net/libgdx-lock-camera-to-bounds-of-tiledmap-with-centered-player
	 */
	private void setCamera() {
		// Get the properties of the map to find the width and height.
		MapProperties prop = renderer.getMap().getProperties();
		int mapWidth = prop.get("width", Integer.class);
		int mapHeight = prop.get("height", Integer.class);

		// Set the minimum and maximum areas of the map.
		float minCameraX = camera.zoom * (CAMERA_WIDTH / 2);
		float maxCameraX = mapWidth - minCameraX;
		float minCameraY = camera.zoom * (CAMERA_HEIGHT / 2);
		float maxCameraY = mapHeight - minCameraY;

		// Set the position of the map based on the minimum value of the bounds, based of the current position of the
		// player.
		camera.position.set(Math.min(maxCameraX, Math.max(mike.getPosition().x, minCameraX)),
				Math.min(maxCameraY, Math.max(mike.getPosition().y, minCameraY)), 0);
		camera.update();
	}

	/**
	 * Draw Mike's sprite.
	 * 
	 * @param delta
	 *            The time in seconds since the last render.
	 */
	private void drawMike(float delta) {
		Direction direction = mike.getDirection();

		if (mike.getState().equals(State.IDLE)) {
			mikeFrame = idleMap.get(direction);
		}
		if (mike.getState().equals(State.RUNNING)) {
			mikeFrame = animationMap.get(direction).getKeyFrame(mike.getStateTime(), true);
		}
		if (mike.getState().equals(State.JUMPING)) {
			mikeFrame = jumpMap.get(direction);
		}
		if (mike.getState().equals(State.ATTACKING)) {
			// Get the time since last render and the current frame, based on
			// the attack frame rate. Then set Mike's
			// frame to attack. If the current attack frame is the second one in
			// the animation, set his attacking
			// boolean to true so that he will attack on the next update. When
			// the attack animation is finished,
			// set his state back to idle.
			stateTime += delta;
			currentFrame += (int) (stateTime / ATTACKING_FRAME_DURATION);
			mikeFrame = attackMap.get(direction).getKeyFrame(stateTime, false);
			if (currentFrame == 1) {
				mike.setAttacking(true); // Attack!
				currentFrame += 1; // Increase the frame count so this will be
									// skipped on the next render
			} else {
				mike.setAttacking(false);
			}
			if (attackMap.get(direction).isAnimationFinished(stateTime)) {
				mike.setState(State.IDLE);
				stateTime = 0;
				currentFrame = 0;
			}
		}
		if (mike.getState().equals(State.JUMP_ATTACK)) {
			// Get the time since last render. Then set Mike's frame to attack.
			// When the attack animation is finished,
			// check whether he is in the air or not, and set his state
			// accordingly.
			stateTime += delta;
			currentFrame += (int) (stateTime / ATTACKING_FRAME_DURATION);
			mikeFrame = jumpAttackMap.get(direction).getKeyFrame(stateTime, false);
			if (currentFrame == 1) {
				mike.setAttacking(true); // Attack!
				currentFrame += 1; // Increase the frame count so this will be
									// skipped on the next render
			} else {
				mike.setAttacking(false);
			}
			if (jumpAttackMap.get(direction).isAnimationFinished(stateTime)) {
				currentFrame = 0;
				stateTime = 0;
				if (mike.isGrounded()) {
					mike.setState(State.IDLE);
				} else {
					mike.setState(State.JUMPING);
				}
			}
		}

		if (mike.getState().equals(State.DYING)) {
			mike.setSpriteRegion(dead);
			mike.loadSprite(spriteBatch);
		} else if (mike.getState().equals(State.DAMAGE)) {
			mike.setSpriteRegion(damage);
			mike.loadSprite(spriteBatch);
		} else { // TODO Check this...
			mike.setSpriteRegion(mikeFrame);
			if (direction == Mike.Direction.DOWN || direction == Mike.Direction.DOWN_LEFT
					|| direction == Mike.Direction.DOWN_RIGHT || direction == Mike.Direction.RIGHT) {
				mike.loadSprite(spriteBatch);
			} else {
				mike.loadSprite(spriteBatch);
			}
		}
	}

	private void drawChakrams() {
		for (Chakram c : mike.getChakrams()) {
			c.setShadowSpriteRegion(shadow);
			c.setSpriteRegion(chakram);
			c.loadSprite(spriteBatch);
		}
	}

	private void drawEnemy() {
		enemy.setSpriteRegion(enemyTexture);
		enemy.loadSprite(spriteBatch);
	}

	/**
	 * Used to draw all of the sprites.
	 */
	private void drawSprites() {
		// Create a comparator
		DrawableComparator comparator = new DrawableComparator();
		Array<Drawable> sprites = new Array<Drawable>();
		Array<Sprite> shadows = new Array<Sprite>();

		// If jumping, add the shadow to the array.
		if (mike.isJumping()) {
			shadows.add(mike.getShadowSprite());
			mike.setBaseY(mike.getShadowPosition().y);
			sprites.add(mike);
		} else {
			sprites.add(mike);
		}

		sprites.add(enemy);
		for (Chakram c : mike.getChakrams()) {
			shadows.add(c.getShadowSprite());
			c.setBaseY(c.getShadowPosition().y);
			sprites.add(c);
		}
		// Sort the sprites
		sprites.sort(comparator);
		// Render shadows first
		for (Sprite shadow : shadows) {
			shadow.draw(spriteBatch);
		}

		for (Drawable sprite : sprites) {
			sprite.getSprite().draw(spriteBatch);
		}
	}

	private void drawShadow() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 1, 1, 1));
		debugRenderer.circle(mike.getShadowVelocity().x, mike.getShadowVelocity().y, mike.getBounds().width / 2);
		debugRenderer.end();
	}

	private void drawButtons() {
		spriteBatch.draw(touchPad, (width / 12), (height / 10), (width / 5), (width / 5));
		spriteBatch.draw(touchPad, (width - (width / 5)), (height / 6), (width / 7), (width / 7));
	}

	private void drawBlocks() {
		for (Block block : world.getWaterBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
			spriteBatch.draw(grass, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX,
					Block.SIZE * ppuY);
		}
		for (Block block : world.getGrassBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
			spriteBatch.draw(water, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX,
					Block.SIZE * ppuY);
		}
	}

	private void drawCollisionBlocks() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 1, 1, 1));
		for (Rectangle rect : world.getCollisionRects()) {
			debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		}
		debugRenderer.end();
	}

	private void drawTouchPad() {
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.identity();
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 0, 0, 1f));
		debugRenderer.ellipse(1, 1, 1, 1);
		debugRenderer.end();
	}

	public void drawDebug() {
		// Render blocks
		debugRenderer.setProjectionMatrix(camera.combined);
		debugRenderer.begin(ShapeType.Line);
		// Render Mike
		Rectangle db = mike.getDamageBounds();
		debugRenderer.setColor(new Color(1, 0, 0, 1));
		debugRenderer.rect(db.x, db.y, db.width, db.height);

		Rectangle rect = mike.getFeetBounds();
		debugRenderer.setColor(new Color(0, 1, 0, 1));
		debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);

		Rectangle jb = mike.getJumpingBounds();
		debugRenderer.setColor(new Color(0, 0, 1, 1));
		debugRenderer.rect(jb.x, jb.y, jb.width, jb.height);

		Rectangle sb = mike.getShadowBounds();
		debugRenderer.setColor(new Color(0, 0, 0, 1));
		debugRenderer.rect(sb.x, sb.y, sb.width, sb.height);

		// Render chakram
		for (Chakram chakram : mike.getChakrams()) {
			Rectangle r = chakram.getAttackBounds();
			debugRenderer.setColor(new Color(0, 0, 1, 1));
			debugRenderer.rect(r.x, r.y, r.width, r.height);
		}
		// Render enemy
		Rectangle r = enemy.getDamageBounds();
		debugRenderer.setColor(new Color(1, 0, 0, 1));
		debugRenderer.rect(r.x, r.y, r.width, r.height);
		debugRenderer.end();
	}

	public void dispose() {
		map.dispose();
		renderer.dispose();
	}

	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		ppuX = width / CAMERA_WIDTH;
		ppuY = height / CAMERA_HEIGHT;
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public void setCamera(OrthographicCamera camera) {
		this.camera = camera;
	}

	public Texture getTouchPad() {
		return touchPad;
	}

	public TiledMap getMap() {
		return map;
	}

	public void setMap(TiledMap map) {
		this.map = map;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public class DrawableComparator implements Comparator<Drawable> {

		@Override
		public int compare(Drawable d1, Drawable d2) {
			return (d2.getBaseY() - d1.getBaseY()) > 0 ? 1 : -1;
		}

	}

}