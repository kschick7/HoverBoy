package com.kyleschick.hoverboy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class HoverBoy extends ApplicationAdapter {
	private static final float GRAVITY = -25;
	private static final float JUMP_SPEED = 500;
	private static final float OBSTACLE_INTERVAL = 1.5f;
	private static final float HORIZONTAL_SPEED = 175;
	private static final int BIRD_START_Y = 300;
	private static final int BIRD_START_X = 70;
	private static final int OBSTACLE_MARGIN = 15;
	private static final int PIPE_WIDTH = 497;
	private static final int PIPE_HEIGHT = 94;
	private static final int BIRD_WIDTH = 38;
	private static final int BIRD_HEIGHT = 28;

	public enum State { MENU, IN_GAME, GAME_OVER, PAUSED }

	SpriteBatch batch;
	InputManager inputManager;
	Texture backgroundTexture;
	Texture birdTextures[];
	Texture pipeTextures[];
	Rectangle bird;
	State state;
	ArrayList<Rectangle> obstaclesTop;
	ArrayList<Rectangle> obstaclesBottom;
	OrthographicCamera camera;
	float birdSpeed;
	float obstacleTime;

	@Override
	public void create () {
		bird = new Rectangle(BIRD_START_X, BIRD_START_Y, BIRD_WIDTH, BIRD_HEIGHT);
		obstacleTime = 0;
		batch = new SpriteBatch();
		obstaclesTop = new ArrayList();
		obstaclesBottom = new ArrayList();
		inputManager = new InputManager();
		backgroundTexture = new Texture("background.png");
		birdTextures = new Texture[2];
		birdTextures[0] = new Texture("bird0.png");
		birdTextures[1] = new Texture("bird1.png");
		pipeTextures = new Texture[2];
		pipeTextures[0] = new Texture("topPipe.png");
		pipeTextures[1] = new Texture("bottomPipe.png");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 397, 707);
		state = State.MENU;
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		if (state == State.IN_GAME) {
			batch.draw(backgroundTexture, 0, 0);
			if (inputManager.isKeyPressed(Input.Keys.SPACE) || inputManager.isTapped())
				birdSpeed = JUMP_SPEED;
			renderObstacles(true);
			renderBird();

			obstacleTime += Gdx.graphics.getDeltaTime();
			if (obstacleTime >= OBSTACLE_INTERVAL) {
				obstacleTime = 0;
				spawnObstacles();
			}

			if (inputManager.isKeyPressed(Input.Keys.ENTER))
				state = State.PAUSED;
		} else if (state == State.MENU) {
			if (inputManager.isKeyPressed(Input.Keys.ENTER) || inputManager.isTapped()) {
				state = State.IN_GAME;
				obstacleTime = System.currentTimeMillis();
			}
		} else if (state == State.PAUSED) {
			if (inputManager.isKeyPressed(Input.Keys.ENTER) || inputManager.isTapped())
				state = State.IN_GAME;
		} else if (state == State.GAME_OVER) {
			batch.draw(backgroundTexture, 0, 0);
			renderObstacles(false);
			renderBird();
			if (inputManager.isKeyPressed(Input.Keys.ENTER) || inputManager.isTapped()) {
				state = State.IN_GAME;
				obstaclesTop.clear();
				obstaclesBottom.clear();
				bird.setY(BIRD_START_Y);
				birdSpeed = 0;
			}
		}

		batch.end();
	}

	public void move(Rectangle obj, float xVelocity, float yVelocity) {
		obj.x += xVelocity * Gdx.graphics.getDeltaTime();
		obj.y += yVelocity * Gdx.graphics.getDeltaTime();
	}

	public void spawnObstacles() {
		int openingHeight = MathUtils.random(100, 450) ;
		obstaclesTop.add(new Rectangle(600, 640 - openingHeight, PIPE_HEIGHT - OBSTACLE_MARGIN, PIPE_WIDTH - OBSTACLE_MARGIN));
		obstaclesBottom.add(new Rectangle(600, -openingHeight, PIPE_HEIGHT - OBSTACLE_MARGIN, PIPE_WIDTH - OBSTACLE_MARGIN));
	}

	public void renderObstacles(boolean moving) {
		for (int i = 0; i < obstaclesTop.size(); ++i) {
			if (obstaclesTop.get(i).x < -100) {
				obstaclesTop.remove(i);
				obstaclesBottom.remove(i--);
			} else {
				if (moving) {
					move(obstaclesTop.get(i), -HORIZONTAL_SPEED, 0);
					move(obstaclesBottom.get(i), -HORIZONTAL_SPEED, 0);
				}
				obstaclesTop.get(i);
				batch.draw(pipeTextures[0], obstaclesTop.get(i).x - OBSTACLE_MARGIN / 2, obstaclesTop.get(i).y - OBSTACLE_MARGIN / 2);
				batch.draw(pipeTextures[1], obstaclesBottom.get(i).x - OBSTACLE_MARGIN / 2, obstaclesBottom.get(i).y - OBSTACLE_MARGIN / 2);
				if ((bird.overlaps(obstaclesTop.get(i)) || bird.overlaps(obstaclesBottom.get(i)))
						&& state == State.IN_GAME)
					state = State.GAME_OVER;
			}
		}
	}

	public void renderBird() {
		birdSpeed += GRAVITY;
		move(bird, 0, birdSpeed);
		if (birdSpeed < 0)
			batch.draw(birdTextures[0], bird.getX(), bird.getY());
		else
			batch.draw(birdTextures[1], bird.getX(), bird.getY());
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		birdTextures[0].dispose();
		birdTextures[1].dispose();
		backgroundTexture.dispose();
		pipeTextures[0].dispose();
		pipeTextures[1].dispose();
		batch.dispose();
	}
}
