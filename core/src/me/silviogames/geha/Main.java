package me.silviogames.geha;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter
{
	public static SpriteBatch batch;
	OrthographicCamera camera;
	Viewport viewport;
	float esc_time = 0f;
	Game game = new Game( );

	@Override
	public void create()
	{
		camera = new OrthographicCamera( );
		viewport = new FitViewport( 16 * 24, 16 * 24, camera );

		batch = new SpriteBatch( );

		Controllers.addListener( game );

		// TODO: 18.07.2021 use Viewport for zoom in
		Res.load( );

		game.init( );
	}

	@Override
	public void resize( int width, int height )
	{
		viewport.update( width, height, true );
	}

	@Override
	public void render()
	{
		ScreenUtils.clear( 0.1f, 0.1f, 0.2f, 1 );

		try
		{
			float d = Gdx.graphics.getDeltaTime( );

			update( Math.min( d, 0.1f ) );
		} catch ( Exception e )
		{
			System.out.println( "CRASH DURING UPDATE" );
			System.out.println( e.getMessage( ) );
			// TODO: 19.07.2021 print error
		}

		try
		{
			camera.update( );
			batch.setProjectionMatrix( camera.combined );

			batch.begin( );

			game.render( );
			// TODO: 26.07.2021 UI rendering ?

			batch.end( );
		} catch ( Exception e )
		{
			System.out.println( "CRASH DURING RENDER" );
			System.out.print( e.getMessage( ) );
		}
	}

	@Override
	public void dispose()
	{
		batch.dispose( );
		Res.dispose( );
	}

	void update( float delta )
	{
		if ( Gdx.input.isKeyPressed( Input.Keys.ESCAPE ) )
		{
			esc_time += delta;
			if ( esc_time >= 0.7f )
			{
				Gdx.app.exit( );
			}
		} else
		{
			esc_time = 0f;
		}

		game.input( );
		game.update( delta );
	}

}
