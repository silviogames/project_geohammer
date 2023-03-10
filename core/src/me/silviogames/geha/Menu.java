package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;

public enum Menu
{
	MAIN_MENU,
	CHARACTER_SELECTION,
	INGAME,
	CONTROLLER_MAPPING,

	;

	static float esc_time = 0f;

	private static boolean key_press(int keycode)
	{
		return Gdx.input.isKeyJustPressed(keycode);
	}

	// TODO: 07.03.23 where do I store menu data?
	// 	as soon as I know I need to pass it here
	public void update(float delta, Game game)
	{
		switch (this)
		{
			case MAIN_MENU:
			{
				if (key_press(Input.Keys.SPACE)) game.menu = CHARACTER_SELECTION;

				if (delayed_press(Input.Keys.ESCAPE, delta)) Gdx.app.exit();

			}
			break;

			case INGAME:
			{
				Game.arena.update(delta);

				if (Main.debug_input)
				{
					game.debug_input();
					game.keyboard_input();
				}

				if (delayed_press(Input.Keys.ESCAPE, delta)) game.menu = MAIN_MENU;
			}
			break;

			case CHARACTER_SELECTION:
			{
				// TODO: 08.03.23 from here the controller mapping menu should be accessed

				// TODO: 08.03.23 this menu sets the class per miner
				//  and binds a miner id to a controller id so later the inputs can be rerouted to the respective miner
			}
			break;

			default:

				break;
		}
	}

	public void render(Game game)
	{
		switch (this)
		{
			case MAIN_MENU:
			{
				Text.cdraw("PROJECT GEOHAMMER", Main.window_width / 2, Main.window_height / 2 + 20, Color.WHITE, 3);
				Text.cdraw("press space to start", Main.window_width / 2, Main.window_height / 2 - 20, Color.WHITE, 2);

				if (esc_time > 0f)
				{
					RenderUtil.render_bar(0, Main.window_height - 12, Main.window_width, 12, Color.CLEAR, Color.OLIVE, esc_time / 0.7f);
					Text.draw("CLOSING GAME", 2, Main.window_height - 10, Color.WHITE);
				}
			}
			break;
			case CHARACTER_SELECTION:

				render_character_selection(game);

				break;
			case INGAME:
				// TODO: 07.03.23
				Game.arena.render_floor();
				Game.arena.render_miners();
				Game.arena.render_particles();
				Game.arena.render_HUD();

				break;
			case CONTROLLER_MAPPING:

				break;
		}
	}

	private void render_character_selection(Game game)
	{
		// THIS IS HELPER FUNCTION TO KEEP THE RENDER/UPDATE METHOD SLIM

		Text.cdraw("CHARACTER SELECTION", Main.window_width / 2, Main.window_height - 50, Color.WHITE, 3f);

		int left_offset = Config.CONF.UI_CHAR_SELECT_LEFT_OFFSET.value;
		int mid_offset = Config.CONF.UI_CHAR_SELECT_MID_OFFSET.value;
		int box_width = Config.CONF.UI_CHAR_SELECT_BOX_WIDTH.value;
		int box_height = Config.CONF.UI_CHAR_SELECT_BOX_HEIGHT.value;
		int lower_offset = Config.CONF.UI_CHAR_SELECT_LOWER_OFFSET.value;
		int text_miner_offset = Config.CONF.UI_CHAR_SELECT_TEXT_1_OFFSET.value;

		for (int i = 0; i < 4; i++)
		{
			RenderUtil.render_box(left_offset + (box_width + mid_offset) * i, lower_offset, box_width, box_height, RenderUtil.color_trans_gray);

			Text.cdraw("MINER " + (i + 1), left_offset + (box_width + mid_offset) * i + box_width / 2, lower_offset + text_miner_offset, Color.WHITE, 2f);

			// TODO: 08.03.23 show actual controller data, gray out box if slot unoccupied

			// TODO: 08.03.23 display miner portrait in a box

			// TODO: 08.03.23 display < miner > selection list like in an arcade game

			// TODO: 08.03.23 display list of the 3 special attacks
		}
	}

	private boolean delayed_press(int key, float delta)
	{
		if (Gdx.input.isKeyPressed(key))
		{
			esc_time += delta;
			if (esc_time >= 0.7f)
			{
				esc_time = 0f;
				return true;
			}
		} else
		{
			esc_time = 0f;
		}
		return false;
	}
}