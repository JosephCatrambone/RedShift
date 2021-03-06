package com.josephcatrambone.redshift.actors;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.josephcatrambone.redshift.MainGame;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Jo on 12/23/2015.
 */
public class Player extends Pawn {

	public static final String PLAYER_USER_DATA = "player";
	public float walkSpeed = 6.5f;

	public static final String PLAYER_SPRITESHEET = "player.png";

	public static final String PLAYER_COOLDOWN = "cooldown.wav";
	private Sound cooldown = null;

	public Player(int x, int y) {
		create(x, y, 8, 8, 1.0f, PLAYER_SPRITESHEET);

		// TODO: Hard-coding animations blows.
		// Idle
		animations[State.IDLE.ordinal()][Direction.RIGHT.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 0*16, 0, 16, 16)
		});
		animations[State.IDLE.ordinal()][Direction.UP.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 2*16, 0, 16, 16)
		});
		animations[State.IDLE.ordinal()][Direction.LEFT.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 4*16, 0, 16, 16)
		});
		animations[State.IDLE.ordinal()][Direction.DOWN.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 6*16, 0, 16, 16)
		});

		// Moving
		animations[State.MOVING.ordinal()][Direction.RIGHT.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 0*16, 0, 16, 16),
				new TextureRegion(this.spriteSheet, 1*16, 0, 16, 16)
		});
		animations[State.MOVING.ordinal()][Direction.UP.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 2*16, 0, 16, 16),
				new TextureRegion(this.spriteSheet, 3*16, 0, 16, 16)
		});
		animations[State.MOVING.ordinal()][Direction.LEFT.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 4*16, 0, 16, 16),
				new TextureRegion(this.spriteSheet, 5*16, 0, 16, 16)
		});
		animations[State.MOVING.ordinal()][Direction.DOWN.ordinal()] = new Animation(0.1f, new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 6*16, 0, 16, 16),
				new TextureRegion(this.spriteSheet, 7*16, 0, 16, 16)
		});

		// Dead
		TextureRegion[] deadFrames = new TextureRegion[] {
				new TextureRegion(this.spriteSheet, 8*16, 0, 16, 16),
				new TextureRegion(this.spriteSheet, 9*16, 0, 16, 16)
		};
		animations[State.DEAD.ordinal()][Direction.RIGHT.ordinal()] = new Animation(0.5f, deadFrames);
		animations[State.DEAD.ordinal()][Direction.UP.ordinal()] = new Animation(0.5f, deadFrames);
		animations[State.DEAD.ordinal()][Direction.LEFT.ordinal()] = new Animation(0.5f, deadFrames);
		animations[State.DEAD.ordinal()][Direction.DOWN.ordinal()] = new Animation(0.5f, deadFrames);

		cooldown = MainGame.assetManager.get(PLAYER_COOLDOWN);

		// Always use first fixture for labelling contact data.
		HashMap<String, String> fixtureData = new HashMap<String, String>();
		fixtureData.put("type", PLAYER_USER_DATA);
		this.getBody().getFixtureList().get(0).setUserData(fixtureData);
		//this.getBody().setUserData(PLAYER_USER_DATA);
	}

	@Override
	public void act(float deltaTime) {
		super.act(deltaTime);

		if(this.state == State.MOVING) {
			float dx = 0;
			float dy = 0;
			if (this.direction == Direction.RIGHT) { dx = this.walkSpeed; dy = 0; }
			if (this.direction == Direction.UP) { dx = 0; dy = this.walkSpeed; }
			if (this.direction == Direction.LEFT) { dx = -this.walkSpeed; dy = 0;	}
			if (this.direction == Direction.DOWN) { dx = 0; dy = -this.walkSpeed; }
			this.getBody().setLinearVelocity(dx, dy);
		} else {
			this.getBody().setLinearVelocity(0, 0);
		}
	}

	public void kill() {
		this.state = State.DEAD;
	}

	@Override
	public void draw(Batch spriteBatch, float alpha) {
		super.draw(spriteBatch, alpha);
	}

	public InputListener getInputListener() {
		// Might be lazy programming to have an input handler here.  Find something better.
		final Player ref = this;

		// TODO: Use a key map.
		return new InputListener() {
			private Stack<Pawn.Direction> directionStack = new Stack<Pawn.Direction>(); // TODO: Figure out the cocksucking language level dogshit that's preventing diamong ops.

			public boolean keyDown(InputEvent event, int keycode) {
				if(keycode == Input.Keys.D) { directionStack.push(Direction.RIGHT); }
				if(keycode == Input.Keys.W) { directionStack.push(Direction.UP); }
				if(keycode == Input.Keys.A) { directionStack.push(Direction.LEFT); }
				if(keycode == Input.Keys.S) { directionStack.push(Direction.DOWN); }

				if(ref.state != State.DEAD) {
					if (!directionStack.empty()) {
						ref.direction = directionStack.peek();
						ref.state = State.MOVING;
					} else {
						ref.state = State.IDLE;
					}
				}
				return true;
			}

			public boolean keyUp(InputEvent event, int keycode) {
				if(keycode == Input.Keys.D) { directionStack.remove(Direction.RIGHT); }
				if(keycode == Input.Keys.W) { directionStack.remove(Direction.UP); }
				if(keycode == Input.Keys.A) { directionStack.remove(Direction.LEFT); }
				if(keycode == Input.Keys.S) { directionStack.remove(Direction.DOWN); }
				// Keep looking the way we were if there are no keys.
				if(ref.state != State.DEAD) {
					if (!directionStack.empty()) {
						ref.direction = directionStack.peek();
						ref.state = State.MOVING;
					} else {
						ref.state = State.IDLE;
					}
				}
				return true;
			}
		};
	}
}
