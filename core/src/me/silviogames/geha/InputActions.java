package me.silviogames.geha;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntIntMap;

public enum InputActions
{
	WALK_LEFT(Input.Keys.A, 13), // 13
	WALK_RIGHT(Input.Keys.D, 14), // 14
	WALK_UP(Input.Keys.W, 11), // 11
	WALK_DOWN(Input.Keys.S, 12), // 12

	BASIC_ATTACK(Input.Keys.SPACE, 0), // 0
	SPECIAL_ATTACK(Input.Keys.F, 1), // 1

	;

	final int default_keyboard_stroke, default_controller_button_index;

	InputActions(int dks, int dcbi)
	{
		this.default_keyboard_stroke = dks;
		this.default_controller_button_index = dcbi;
	}

	public static IntIntMap get_default_controller_mapping()
	{
		// a controller mapping is a iimap where button indices map to oridnals of InputActions,
		IntIntMap iidef = new IntIntMap();
		// a button returning -1 is not mapped.

		for (InputActions ia : values())
		{
			iidef.put(ia.default_controller_button_index, ia.ordinal());
		}

		return iidef;
	}

	public static InputActions safe_ordinal(int ord)
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