package me.silviogames.geha;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
//import com.badlogic.gdx.backends.lwjgl3.LwjglApplicationConfiguration;

import me.silviogames.geha.Main;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration( );
		config.setForegroundFPS(60);
		config.setWindowPosition(700,0);
		config.setResizable(true);
		config.setWindowedMode(800,800);
		config.setTitle("project geohammer");
		new Lwjgl3Application( new Main( ), config );
	}
}