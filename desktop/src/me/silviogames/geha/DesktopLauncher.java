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
		config.setWindowPosition(600,20);
		config.setResizable(false);
		config.setWindowedMode((int) (Main.window_width * 1.5f) ,(int) (Main.window_height * 1.5f));
		config.setTitle("project geohammer");
		new Lwjgl3Application( new Main( ), config );
	}
}