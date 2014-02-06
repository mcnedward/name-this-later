package com.awesome.namethislater.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Texture swimSheet; // The sprite sheet for the swim state
	private TextureRegion mikeFrame; // The region of the current frame for Mike
	private TextureRegion dead; // The texture for the death state
	private TextureRegion damage; // The texture for the damage state
	private TextureRegion chakram; // The texture for chakrams
	private TextureRegion shadow; // The texture for the jump shadow
	private TextureRegion enemyTexture; // The texture for the enemy
	private Texture touchPad; // The texture for the touch pad buttons
	private Texture grass; // The texture for grass blocks
	private Texture water; // The texture for water blocks

	/** Animation and Texture Maps **/
	private final Map<Direction, Animation> animationMap = new HashMap<Direction, Animation>();
	private final Map<Direction, TextureRegion> idleMap = new HashMap<Direction, TextureRegion>();
	private final Map<Direction, TextureRegion> jumpMap = new HashMap<Direction, TextureRegion>();
	private final Map<Direction, Animation> attackMap = new HashMap<Direction, Animation>();
	private final Map<Direction, Animation> jumpAttackMap = new HashMap<Direction, Animation>();
	private final Map<Direction, TextureRegion> damageMap = new HashMap<Direction, TextureRegion>();
	private final Map<Direction, TextureRegion> swimMap = new HashMap<Direction, TextureRegion>();

	private final SpriteBatch spriteBatch;
	private boolean debug = false;
	public int width, height;
	private float ppuX; // Pixels per unit on the X axis
	private float ppuY; // Pixels per unit on the Y axis

	float stateTime; // The time since last render
	float currentFrame; // The current frame, based on the state time
	float enemyStateTime;
	private float enemyFrame;

	private final World world;
	private final Level level;
	private final Room room;
	private final Mike mike;
	private final List<Enemy> enemies;

	private TiledMap map;
	private final OrthogonalTiledMapRenderer renderer;

	private BitmapFont font;

	private final int[] layer1 = { 0 };
	private final int[] layer2 = { 1 };

	public Renderer(World world, boolean debug) {
		this.world = world;
		this.level = world.getLevel();
		this.room = world.getRoom();
		mike = world.getMike();
		enemies = level.getEnemies();
		map = level.getMap();
		renderer = new OrthogonalTiledMapRenderer(map, 1f / 32f);

		camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		camera.position.set(CAMERA_WIDTH, CAMERA_HEIGHT, 0);
		camera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);

		this.debug = debug;
		spriteBatch = new SpriteBatch();
		stateTime = 0f;
		enemyStateTime = 0f;

		loadTextures();
	}

	/**
	 * Render all sprites onto the screen, and set the camera to follow the main character.
	 * 
	 * @param delta
	 *            The time in seconds since the last update.
	 */
	public void render(float delta) {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		setCamera();
		renderer.setView(camera);
		renderer.render(layer1);

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		drawEnemies(delta);
		drawMike(delta);
		drawChakrams();
		drawSprites();
		spriteBatch.end();
		renderer.render(layer2);

		if (debug) {
			drawDebug();
		}
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
		if (mike.getState().equals(State.SWIMMING)) {
			mikeFrame = swimMap.get(direction);
		}
		if (mike.isHurt()) {
			mikeFrame = damageMap.get(direction);
			stateTime += delta;
			currentFrame += (int) (stateTime / RUNNING_FRAME_DURATION);
			if (currentFrame <= 120) {
				currentFrame += 1;
			} else {
				mike.setHurt(false);
				mike.setInvincible(false);
				mike.setState(State.IDLE);
				currentFrame = 0;
				stateTime = 0;
			}
		}

		if (mike.getState().equals(State.DYING)) {
			mike.setSpriteRegion(dead);
			mike.loadSprite(spriteBatch);
		} else {
			mike.setSpriteRegion(mikeFrame);
			mike.loadSprite(spriteBatch);
		}
	}

	/**
	 * Draw all the chakrams that Mike has thrown.
	 */
	private void drawChakrams() {
		for (Chakram c : mike.getChakrams()) {
			c.setShadowSpriteRegion(shadow);
			c.setSpriteRegion(chakram);
			c.loadSprite(spriteBatch);
		}
	}

	/**
	 * Draw every enemy in the level.
	 * 
	 * @param delta
	 *            The time in seconds since the last render.
	 */
	private void drawEnemies(float delta) {
		for (Enemy enemy : enemies) {
			// If the enemy is not dead, load it. If the enemy is hurt, then draw its health bar
			if (!enemy.isDead()) {
				enemy.setSpriteRegion(enemyTexture);
				enemy.loadSprite(spriteBatch);
				if (enemy.isHurt()) {
					enemyStateTime += delta;
					enemyFrame += (int) (stateTime / ATTACKING_FRAME_DURATION);
					if (enemyFrame <= 120) {
						enemyFrame += 1;
						enemy.drawHealth(debugRenderer);
					} else {
						enemy.setHurt(false);
						enemyFrame = 0;
						enemyStateTime = 0;
					}
				}
			}
		}
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

		for (Enemy enemy : enemies) {
			if (!enemy.isDead()) {
				sprites.add(enemy);
			}
		}

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

	public void drawDebug() {
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
		// Rectangle r = enemy.getDamageBounds();
		// debugRenderer.setColor(new Color(1, 0, 0, 1));
		// debugRenderer.rect(r.x, r.y, r.width, r.height);
		debugRenderer.end();
	}

	/**
	 * Load all of the textures from the sprite sheet.
	 */
	private void loadTextures() {
		spriteSheet = new Texture(Gdx.files.internal("images/spritesheet.png"));
		int width = 16;
		int height = 32;

		TextureRegion[][] animationFrames = new TextureRegion[8][3];
		TextureRegion[][] jumpFrames = new TextureRegion[8][1];
		TextureRegion[][] attackFrames = new TextureRegion[8][2];
		TextureRegion[][] jumpAttackFrames = new TextureRegion[8][2];
		TextureRegion[][] damageFrames = new TextureRegion[8][1];
		TextureRegion[][] swimFrames = new TextureRegion[8][1];

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 13; j++) {
				int x = j * width;
				int y = i * height;
				if (j == 0 || j == 1 || j == 2) {
					animationFrames[i][j] = new TextureRegion(spriteSheet, x, y, width, height);
				}
				if (j == 3) {
					jumpFrames[i][0] = new TextureRegion(spriteSheet, x, y, width, height);
				}
				if (j == 4 || j == 5) {
					int m = 0;
					if (j == 4) {
						m = 0;
					}
					if (j == 5) {
						m = 1;
					}
					attackFrames[i][m] = new TextureRegion(spriteSheet, x, y, width, height);
				}
				if (j == 6 || j == 7) {
					int m = 0;
					if (j == 6) {
						m = 0;
					}
					if (j == 7) {
						m = 1;
					}
					jumpAttackFrames[i][m] = new TextureRegion(spriteSheet, x, y, width, height);
				}
				if (j == 8) {
					damageFrames[i][0] = new TextureRegion(spriteSheet, x, y, width, height);
				}
				if (j == 9) {
					swimFrames[i][0] = new TextureRegion(spriteSheet, x, y, width, height);
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
		animationMap
				.put(Direction.RIGHT, new Animation(RUNNING_FRAME_DURATION, animationFrames[3]));
		animationMap.put(Direction.DOWN_LEFT, new Animation(RUNNING_FRAME_DURATION,
				animationFrames[4]));
		animationMap.put(Direction.UP_LEFT, new Animation(RUNNING_FRAME_DURATION,
				animationFrames[5]));
		animationMap.put(Direction.UP_RIGHT, new Animation(RUNNING_FRAME_DURATION,
				animationFrames[6]));
		animationMap.put(Direction.DOWN_RIGHT, new Animation(RUNNING_FRAME_DURATION,
				animationFrames[7]));
		// Set the jump for each direction
		jumpMap.put(Direction.DOWN, new TextureRegion(jumpFrames[0][0]));
		jumpMap.put(Direction.LEFT, new TextureRegion(jumpFrames[1][0]));
		jumpMap.put(Direction.UP, new TextureRegion(jumpFrames[2][0]));
		jumpMap.put(Direction.RIGHT, new TextureRegion(jumpFrames[3][0]));
		jumpMap.put(Direction.DOWN_LEFT, new TextureRegion(jumpFrames[4][0]));
		jumpMap.put(Direction.UP_LEFT, new TextureRegion(jumpFrames[5][0]));
		jumpMap.put(Direction.UP_RIGHT, new TextureRegion(jumpFrames[6][0]));
		jumpMap.put(Direction.DOWN_RIGHT, new TextureRegion(jumpFrames[7][0]));
		// Set the attacking animation for each direction
		attackMap.put(Direction.DOWN, new Animation(ATTACKING_FRAME_DURATION, attackFrames[0]));
		attackMap.put(Direction.LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[1]));
		attackMap.put(Direction.UP, new Animation(ATTACKING_FRAME_DURATION, attackFrames[2]));
		attackMap.put(Direction.RIGHT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[3]));
		attackMap
				.put(Direction.DOWN_LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[4]));
		attackMap.put(Direction.UP_LEFT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[5]));
		attackMap.put(Direction.UP_RIGHT, new Animation(ATTACKING_FRAME_DURATION, attackFrames[6]));
		attackMap.put(Direction.DOWN_RIGHT,
				new Animation(ATTACKING_FRAME_DURATION, attackFrames[7]));
		// Set the jump attacking animation for each direction
		jumpAttackMap.put(Direction.DOWN, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[0]));
		jumpAttackMap.put(Direction.LEFT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[1]));
		jumpAttackMap.put(Direction.UP,
				new Animation(ATTACKING_FRAME_DURATION, jumpAttackFrames[2]));
		jumpAttackMap.put(Direction.RIGHT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[3]));
		jumpAttackMap.put(Direction.DOWN_LEFT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[4]));
		jumpAttackMap.put(Direction.UP_LEFT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[5]));
		jumpAttackMap.put(Direction.UP_RIGHT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[6]));
		jumpAttackMap.put(Direction.DOWN_RIGHT, new Animation(ATTACKING_FRAME_DURATION,
				jumpAttackFrames[7]));
		// Set the swim for each direction
		damageMap.put(Direction.DOWN, new TextureRegion(damageFrames[0][0]));
		damageMap.put(Direction.LEFT, new TextureRegion(damageFrames[1][0]));
		damageMap.put(Direction.UP, new TextureRegion(damageFrames[2][0]));
		damageMap.put(Direction.RIGHT, new TextureRegion(damageFrames[3][0]));
		damageMap.put(Direction.DOWN_LEFT, new TextureRegion(damageFrames[4][0]));
		damageMap.put(Direction.UP_LEFT, new TextureRegion(damageFrames[5][0]));
		damageMap.put(Direction.UP_RIGHT, new TextureRegion(damageFrames[6][0]));
		damageMap.put(Direction.DOWN_RIGHT, new TextureRegion(damageFrames[7][0]));
		// Set the swim for each direction
		swimMap.put(Direction.DOWN, new TextureRegion(swimFrames[0][0]));
		swimMap.put(Direction.LEFT, new TextureRegion(swimFrames[1][0]));
		swimMap.put(Direction.UP, new TextureRegion(swimFrames[2][0]));
		swimMap.put(Direction.RIGHT, new TextureRegion(swimFrames[3][0]));
		swimMap.put(Direction.DOWN_LEFT, new TextureRegion(swimFrames[4][0]));
		swimMap.put(Direction.UP_LEFT, new TextureRegion(swimFrames[5][0]));
		swimMap.put(Direction.UP_RIGHT, new TextureRegion(swimFrames[6][0]));
		swimMap.put(Direction.DOWN_RIGHT, new TextureRegion(swimFrames[7][0]));

		dead = new TextureRegion(spriteSheet, 160, 0, width, height);
		enemyTexture = new TextureRegion(spriteSheet, 176, 0, width, height);
		chakram = new TextureRegion(spriteSheet, 192, 0, width, 16);
		shadow = new TextureRegion(spriteSheet, 208, 0, 32, 32);
		mike.setShadowSpriteRegion(shadow);
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