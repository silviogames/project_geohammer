package me.silviogames.geha;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;

public enum Controller_Buttons
{
	DPAD_LEFT(Input.Keys.A, 13), // 13
	DPAD_RIGHT(Input.Keys.D, 14), // 14
	DPAD_UP(Input.Keys.W, 11), // 11
	DPAD_DOWN(Input.Keys.S, 12), // 12

	ACTIONS_B(Input.Keys.SPACE, 0), // 0
	ACTIONS_A(Input.Keys.F, 1), // 1
	ACTIONS_Y(Input.Keys.F, 2), // 2
	ACTIONS_X(Input.Keys.F, 3), // 3
	;

	// this enum encodes the known buttons of a controller.
	// previously this used to be in game actions like move and mine but now that the controller input
	// may be redirected to arbitrary menus, the buttons may have a different meaning depending on the context

	final int default_keyboard_stroke, default_controller_button_index;

	// this holds the mappings and controllers keep the key to one of the mappings
	public static ObjectMap<Integer, IntIntMap> controller_mapping = new ObjectMap<>();

	final static IntIntMap controller_mapping_default = new IntIntMap();

	public static void init_default_mapping(){

		// a button returning -1 is not mapped.
		for (Controller_Buttons ia : values())
		{
			controller_mapping_default.put(ia.default_controller_button_index, ia.ordinal());
		}

		controller_mapping.put(0, controller_mapping_default);
	}

	Controller_Buttons(int dks, int dcbi)
	{
		this.default_keyboard_stroke = dks;
		this.default_controller_button_index = dcbi;
	}

	public static Controller_Buttons safe_ordinal(int ord)
	{
		// wrapper to return null if ord is invalid
		if (ord == -1)
		{
			return null;
		} else
		{
			return values()[ord];
		}
	}
}