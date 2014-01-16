package com.awesome.namethislater.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.awesome.namethislater.model.Block;
import com.awesome.namethislater.model.Chakram;
import com.awesome.namethislater.model.Enemy;
import com.awesome.namethislater.model.Level;
import com.awesome.namethislater.model.Mike;
import com.awesome.namethislater.model.Mike.Direction;
import com.awesome.namethislater.model.Mike.State;
import com.awesome.namethislater.model.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Renderer {

	/** Camera Width and Height **/
	private static final float CAMERA_WIDTH = 10f;
	private static final float CAMERA_HEIGHT = 7f;
	// The duration of each frame
	private static final float RUNNING_FRAME_DURATION = 0.1f;	// 10 FPS
	private static final float ATTACKING_FRAME_DURATION = 0.2f;

	private OrthographicCamera cam;	// The camera for the screen

	/** For debug rendering **/
	ShapeRenderer debugRenderer = new ShapeRenderer();

	/** Textures **/
	private Texture spriteSheet;		// The sprite sheet for movement
	private Texture attackSheet;		// The sprite sheet for ground attacks
	private Texture jumpAttackSheet;	// The sprite sheet for jump attacks
	private TextureRegion mikeFrame;	// The region of the current frame for Mike
	private Texture dead;				// The texture for the death state
	private Texture damage;				// The texture for the damge state
	private Texture chakram;			// The texture for chakrams
	private Texture shadow;				// The texture for the jump shadow

	private Texture enemyTexture;		// The texture for the enemy

	private Texture touchPad;			// The texture for the touch pad buttons

	private Texture grass;				// The texture for grass blocks
	private Texture water;				// The texture for water blocks

	/** Animation and Texture Maps **/
	private Map<Direction, Animation> animationMap = new HashMap<Direction, Animation>();
	private Map<Direction, TextureRegion> idleMap = new HashMap<Direction, TextureRegion>();
	private Map<Direction, TextureRegion> jumpMap = new HashMap<Direction, TextureRegion>();
	private Map<Direction, Animation> attackMap = new HashMap<Direction, Animation>();
	private Map<Direction, Animation> jumpAttackMap = new HashMap<Direction, Animation>();

	private SpriteBatch spriteBatch;
	private boolean debug = false;
	public int width, height;
	private float ppuX;					// Pixels per unit on the X axis
	private float ppuY;					// Pixels per unit on the Y axis

	float stateTime;					// The time since last render
	float currentFrame;					// The current frame, based on the state time

	private World world;
	private Level level;
	private Mike mike;
	private Enemy enemy;

	public Renderer(World world, boolean debug) {
		loadTextures();

		this.world = world;
		this.level = world.getLevel();
		mike = world.getMike();
		mike.setShadowSpriteRegion(shadow);
		enemy = level.getEnemy();

		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 10);
		this.cam.update();
		this.debug = debug;
		spriteBatch = new SpriteBatch();
		stateTime = 0f;
	}

	private void loadTextures() {
		// Create the sprite sheet as a new Texture. The width is the number of columns in the sheet. The height is the
		// number of rows in the sheet.
		spriteSheet = new Texture(Gdx.files.internal("images/mikespritesheetdetailed.png"));
		int width = spriteSheet.getWidth() / 4;
		int height = spriteSheet.getHeight() / 8;

		// 2-Dimensional array that will hold all the frames for animating the sprite
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
		idleMap.put(Direction.RIGHT, new TextureRegion(animationFrames[3][2]));
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

		enemyTexture = new Texture(Gdx.files.internal("images/enemy.png"));

		touchPad = new Texture(Gdx.files.internal("images/touchpad.png"));
		grass = new Texture(Gdx.files.internal("images/grass.png"));
		water = new Texture(Gdx.files.internal("images/water.png"));
	}

	public void render(float delta) {
		spriteBatch.begin();

		drawBlocks();
		drawEnemy();
		drawMike(delta);
		drawChakrams();
		drawSprites();

		// drawButtons();
		spriteBatch.end();

		drawCollisionBlocks();
		// drawTouchPad();
		if (debug)
			drawDebug();
	}

	public Texture getTouchPad() {
		return touchPad;
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
			// Get the time since last render and the current frame, based on the attack frame rate. Then set Mike's frame to attack. If the current attack frame is the second one in the animation, set his attacking boolean to true so that he will attack on the next update. When the attack animation is finished,
			// set his state back to idle.
			stateTime += delta;
			currentFrame += (int) (stateTime / ATTACKING_FRAME_DURATION);
			mikeFrame = attackMap.get(direction).getKeyFrame(stateTime, false);
			if (currentFrame == 1) {
				mike.setAttacking(true);	// Attack!
				currentFrame += 1;			// Increase the frame count so this will be skipped on the next render
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
			// Get the time since last render. Then set Mike's frame to attack. When the attack animation is finished,
			// check whether he is in the air or not, and set his state accordingly.
			stateTime += delta;
			int currentFrame = (int)(stateTime / ATTACKING_FRAME_DURATION);
			mikeFrame = attackMap.get(direction).getKeyFrame(stateTime, false);
			if (currentFrame == 1) {
				mike.setAttacking(true);	// Attack!
			}
			mikeFrame = jumpAttackMap.get(direction).getKeyFrame(stateTime, false);
			if (jumpAttackMap.get(direction).isAnimationFinished(stateTime)) {
				if (mike.isGrounded()) {
					mike.setState(State.IDLE);
					stateTime = 0;
				} else {
					mike.setState(State.JUMPING);
					stateTime = 0;
				}
			}
		}

		if (mike.getState().equals(State.DYING)) {
			mike.setSpriteRegion(dead);
			mike.loadSprite(spriteBatch, ppuX, ppuY);
		} else if (mike.getState().equals(State.DAMAGE)) {
			mike.setSpriteRegion(damage);
			mike.loadSprite(spriteBatch, ppuX, ppuY);
		} else {	// TODO Check this...
			mike.setSpriteRegion(mikeFrame);
			if (direction == Mike.Direction.DOWN || direction == Mike.Direction.DOWN_LEFT
					|| direction == Mike.Direction.DOWN_RIGHT || direction == Mike.Direction.RIGHT) {
				mike.loadSprite(spriteBatch, ppuX, ppuY);
			} else {
				mike.loadSprite(spriteBatch, ppuX, ppuY);
			}
		}
	}

	private void drawChakrams() {
		for (Chakram c : mike.getChakrams()) {
			c.setShadowSpriteRegion(shadow);
			c.setSpriteRegion(chakram);
			c.loadSprite(spriteBatch, ppuX, ppuY);
		}
	}

	private void drawEnemy() {
		enemy.setSpriteRegion(enemyTexture);
		enemy.loadSprite(spriteBatch, ppuX, ppuY);
	}

	/**
	 * Used to draw all of the sprites.
	 */
	private void drawSprites() {
		// Create a comparator 
		SpriteComparator comparator = new SpriteComparator();
		Array<Sprite> sprites = new Array<Sprite>();
		Array<Sprite> shadows = new Array<Sprite>();

		// Check if the jump is in front of or behind an enemy
		float jumpY = mike.getShadow().y;
		float enemyY = enemy.getPosition().y;
		boolean inFront = jumpY < enemyY;
		
		// If jumping, add the shadow to the array, but not the sprite for Mike. Otherwise add the sprite to the array.
		if (mike.isJumping()) {
			shadows.add(mike.getShadowSprite());
		} else {
			sprites.add(mike.getSprite());
		}
		sprites.add(enemy.getSprite());
		for (Chakram c : mike.getChakrams()) {
			sprites.add(c.getSprite());
			shadows.add(c.getShadowSprite());
		}
		// Sort the sprites
		sprites.sort(comparator);
		shadows.sort(comparator);
		// Render shadows first
		for (Sprite shadow : shadows) {
			shadow.draw(spriteBatch);
		}
		// Check if Mike is jumping and if he is in front of an enemy, then draw accordingly
		if (mike.isJumping() && !inFront) {
			mike.getSprite().draw(spriteBatch);
		}
		for (Sprite sprite : sprites) {
			sprite.draw(spriteBatch);
		}
		if (mike.isJumping() && inFront) {
			mike.getSprite().draw(spriteBatch);
		}
	}

	private void drawShadow() {
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 1, 1, 1));
		debugRenderer.circle(mike.getShadow().x, mike.getShadow().y, mike.getBounds().width / 2);
		debugRenderer.end();
	}

	private void drawButtons() {
		spriteBatch.draw(touchPad, (width / 12), (height / 10), (width / 5), (width / 5));
		spriteBatch.draw(touchPad, (width - (width / 5)), (height / 6), (width / 7), (width / 7));
	}

	private void drawBlocks() {
		for (Block block : world.getOtherBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
			spriteBatch.draw(grass, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX,
					Block.SIZE * ppuY);
		}
		for (Block block : world.getDrawableBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
			spriteBatch.draw(water, block.getPosition().x * ppuX, block.getPosition().y * ppuY, Block.SIZE * ppuX,
					Block.SIZE * ppuY);
		}
	}

	private void drawCollisionBlocks() {
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 1, 1, 1));
		for (Rectangle rect : world.getCollisionRects()) {
			debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		}
		debugRenderer.end();
	}

	private void drawTouchPad() {
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.identity();
		debugRenderer.begin(ShapeType.Filled);
		debugRenderer.setColor(new Color(1, 0, 0, 1f));
		debugRenderer.ellipse(1, 1, (int) 1, (int) 1);
		debugRenderer.end();
	}

	public void drawDebug() {
		// Render blocks
		debugRenderer.setProjectionMatrix(cam.combined);
		debugRenderer.begin(ShapeType.Line);
		// for (Block block : world.getDrawableBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
		// Rectangle rect = block.getBounds();
		// debugRenderer.setColor(new Color(1, 0, 0, 1));
		// debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		// }
		// for (Block block : world.getOtherBlocks((int) CAMERA_WIDTH, (int) CAMERA_HEIGHT)) {
		// Rectangle rect = block.getBounds();
		// debugRenderer.setColor(new Color(1, 0, 0, 1));
		// debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		// }
		// Render Mike
		Rectangle rect = mike.getFeetBounds();
		debugRenderer.setColor(new Color(0, 1, 0, 1));
		debugRenderer.rect(rect.x, rect.y, rect.width, rect.height);
		
		Rectangle fr = mike.getJumpingBounds();
		debugRenderer.setColor(new Color(1, 1, 1, 1));
		debugRenderer.rect(fr.x, fr.y, fr.width, fr.height);
		
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

	public class SpriteComparator implements Comparator<Sprite> {

		@Override
		public int compare(Sprite sprite1, Sprite sprite2) {
			return (sprite2.getY() - sprite1.getY()) > 0 ? 1 : -1;
		}

	}

}
