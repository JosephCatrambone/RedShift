package com.josephcatrambone.metalskyarena.scenes;

import com.josephcatrambone.metalskyarena.MainGame;

/**
 * Created by Jo on 1/17/2016.
 */
public class WinScene extends KeyWaitScene {

	public static final String WIN_BG = "youwin.png";

	public WinScene() {
		super(WIN_BG, MainGame.GameState.TITLE);
		this.clearBlack = true;
	}
}
