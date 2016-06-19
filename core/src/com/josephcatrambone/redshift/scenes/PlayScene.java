package com.josephcatrambone.metalskyarena.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.josephcatrambone.metalskyarena.Level;
import com.josephcatrambone.metalskyarena.MainGame;
import com.josephcatrambone.metalskyarena.actors.Pawn;
import com.josephcatrambone.metalskyarena.actors.Player;
import com.josephcatrambone.metalskyarena.handlers.RegionContactListener;

/**
 * Created by Jo on 12/20/2015.
 */
public class PlayScene extends Scene {
	public final int PIXEL_DISPLAY_WIDTH = 160; // Ten pixels on a side?
	Stage stage;
	Camera camera;
	Level level;
	Player player;
	float sceneChangeDelay = 2.5f;

	RegionContactListener regionContactListener;

	Box2DDebugRenderer debugRenderer;

	@Override
	public void create() {
		MainGame.world = new World(new Vector2(0, 0), true);

		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())); // Fit viewport = black bars.
		debugRenderer = new Box2DDebugRenderer();

		regionContactListener = new RegionContactListener();
		MainGame.world.setContactListener(regionContactListener);

		// Setup camera.  Enforce y-up.
		float invAspectRatio = stage.getHeight()/stage.getWidth();
		camera = stage.getCamera();
		((OrthographicCamera)camera).setToOrtho(false, PIXEL_DISPLAY_WIDTH, PIXEL_DISPLAY_WIDTH*invAspectRatio);
		camera.update(true);

		level = new Level("test.tmx");

		player = new Player(level.getPlayerStartX(), level.getPlayerStartY());
		stage.addActor(player);

		// Global input listener if needed.
		stage.addListener(player.getInputListener());

		// TODO: When resuming, restore input processors.
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void dispose() {
		level.dispose();
		stage.dispose();
		MainGame.world.dispose();
	}

	@Override
	public void render(float deltaTime) {
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		level.drawBG(camera);
		stage.draw();
		//debugRenderer.render(MainGame.world, camera.combined);
		level.drawOverlay(camera);
	}

	@Override
	public void update(float deltaTime) {
		MainGame.world.step(deltaTime, 8, 3);
		stage.act(deltaTime);

		// Reached the goal?
		if(regionContactListener.reachedGoal) {
			MainGame.switchState((MainGame.GameState.WIN));
		}

		// Update player's heat.
		if(regionContactListener.playerCooling) {
			player.cool(deltaTime);
		} else {
			player.heat(deltaTime);
		}

		// Touch teleporter?
		if(regionContactListener.playerTeleport) {
			regionContactListener.playerTeleport = false;
			if(regionContactListener.teleportMap != null) {
				level.load(regionContactListener.teleportMap);
			}
			player.teleportTo(regionContactListener.teleportX, regionContactListener.teleportY);
		}

		// How long has the player been dead?
		if(player.state == Pawn.State.DEAD) {
			sceneChangeDelay -= deltaTime;
			if(sceneChangeDelay < 0) {
				MainGame.switchState(MainGame.GameState.GAME_OVER);
			}
		}

		// Camera follows player?
		camera.position.set(player.getX(), player.getY(), camera.position.z);
		camera.update();
	}

}
