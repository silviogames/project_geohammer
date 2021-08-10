package me.silviogames.geha.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import me.silviogames.geha.Main;

public class DesktopLauncher
{
	public static void main( String[] arg )
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration( );
		config.forceExit = false;
		config.resizable = true;
		config.x = 700;
		config.y = 0;
		config.width = 800;
		config.height = 800;
		config.title = "project_geohammer";
		new LwjglApplication( new Main( ), config );
	}
}
