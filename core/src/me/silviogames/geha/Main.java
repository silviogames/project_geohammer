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
	public static SpriteBatch shader_batch;
	OrthographicCamera camera;
	Viewport viewport;
	public static Smartrix smx_text_data = new Smartrix(100, -1, -1);
	public static boolean debug_render = false;
	public static boolean debug_input = true;
	public static boolean skip_main_menu = false;
	// TODO: 07.03.23 this is hardcoded right now, I'm not sure if I will have different arena sizes
	public static int window_width = 16 * 24;
	public static int window_height = 16 * 24;
	Game game = new Game();

	public static int upper_y_bound()
	{
		return Arena.arena_size * 16;
	}

	private static boolean key_press(int keycode)
	{
		return Gdx.input.isKeyJustPressed(keycode);
	}

	@Override
	public void create()
	{
		camera = new OrthographicCamera();
		viewport = new FitViewport(16 * 24, 16 * 24, camera);

		batch = new SpriteBatch();
		shader_batch = new SpriteBatch();

		Controllers.addListener(game);

		// TODO: 18.07.2021 use Viewport for zoom in
		Res.load();

		Text.init();

		game.init();

		Config.load_config();

		// TEST CODE ARENA


		// TEST CODE ARENA
	}

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height, true);
	}

	@Override
	public void render()
	{
		ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

		//try
		//{
		float d = Gdx.graphics.getDeltaTime();

		update(Math.min(d, 0.1f));
		//} catch (Exception e)
		//{
		//	System.out.println("CRASH DURING UPDATE");
		//	System.out.println(e.getMessage());
		//	// TODO: 19.07.2021 print error
		//}

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		game.render();
		// TODO: 26.07.2021 UI rendering ?

		// TODO: 01.02.23 text render

		// for now no sorting of the text entries happens, since it is not needed in this project
		for (int i = 0; i < smx_text_data.num_lines(); i++)
		{
			Text.render_from_text_smx(smx_text_data, i);

			// free entries after rendering
			smx_text_data.set(i, Text.TEXT_DATA.STATUS.offset, -1);
		}

		batch.end();

		//shader_batch.setProjectionMatrix(camera.combined);
		//shader_batch.setShader(game.shaders.shader_test);
		//shader_batch.begin();
		//shader_batch.draw(Res.IMPACTOR.region, 10, 10, 100, 100);
		//shader_batch.end();

		//} catch (Exception e)
		//{
		//	System.out.println("CRASH DURING RENDER");
		//	System.out.print(e.getMessage());
		//}
	}

	@Override
	public void dispose()
	{
		batch.dispose();
		Res.dispose();

		// it seems that Text class right now does not need a dispose but fine, maybe in future
		Text.dispose();

		game.dispose();
	}

	void update(float delta)
	{
		if (key_press(Input.Keys.F4))
		{
			// RELOAD CONFIG
			Config.load_config();
		}

		if (key_press(Input.Keys.F5))
		{
			Config.print_config();
		}

		game.input();
		game.update(delta);
	}
}