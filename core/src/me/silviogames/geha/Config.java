package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Config
{
	public static void load_config()
	{
		System.out.println("LOADING CONFIG");
		// loads and applies config

		// number of successful assignments
		int assigned_values = 0;

		FileHandle file = Gdx.files.internal("config.txt");
		String[] lines = file.readString().split("\n");
		for (String line : lines)
		{
			line = line.trim();
			String[] split_line = line.split(":");
			if (split_line.length != 2)
			{
				System.out.println("config line #" + line + "# invalid, skipped");
				continue;
			}
			int val = -1;
			try
			{
				val = Integer.parseInt(split_line[1]);
			} catch (Exception e)
			{
				System.out.println("config line #" + line + "# has no number after colon, skipped");
				continue;
			}
			for (CONF conf : CONF.values())
			{
				if (conf.toString().equals(split_line[0]))
				{
					conf.value = val;
					assigned_values++;
					break;
				}
			}
		}
		System.out.println("CONFIG LOADING SUMMARY:");
		System.out.println("number of config values: " + CONF.values().length);
		System.out.println("number of loaded config lines: " + lines.length);
		System.out.println("number of assignments: " + assigned_values);
	}

	public static void print_config()
	{
		// PRINTING CONF TO CONSOLE FOR TESTING
		for (CONF conf : CONF.values())
		{
			System.out.println("CONF:" + conf.toString() + " : " + conf.value);
		}
	}

	public enum CONF
	{
		MINER_MAX_LIFE,

		STAMINA_COST_BASIC_ATTACK,
		STAMINA_COST_SPECIAL_ATTACK,
		STAMINA_REGAIN_PER_INTERVAL,
		RENDER_DIRECTION_ARROWS,
		STAMINA_SYSTEM,

		BLOCK_LIFE,
		CRYSTAL_CHANCE,
		CRYSTAL_MAX_POSSESSION,

		IMPACTOR_TARGET_DISTANCE,
		IMPACTOR_INITIAL_OFFSET,
		IMPACTOR_SMOKE_RADIAL_OFFSET,
		IMPACTOR_BLOCK_DAMAGE_RADIUS,

		OROGENY_DAMAGE,
		OROGENY_WIDTH,
		OROGENY_HEIGHT,
		OROGENY_OFFSET_FROM_CASTER,
		OROGENY_CREATION_TIME_MS,

		TRANSFORM_FAULT_EDGE_DAMAGE,
		TRANSFORM_FAULT_OFFSET,
		TRANSFORM_FAULT_DELAY_MS,

		UI_CHAR_SELECT_LEFT_OFFSET,
		UI_CHAR_SELECT_MID_OFFSET,
		UI_CHAR_SELECT_BOX_WIDTH,
		UI_CHAR_SELECT_BOX_HEIGHT,
		UI_CHAR_SELECT_LOWER_OFFSET,
		UI_CHAR_SELECT_TEXT_1_OFFSET,

		;

		// IF THE FILE DOES NOT SET THE VALUE A DEFAULT OF 10 MAY BE WEIRD FOR SOME OF THE CONFIGS
		public int value = 10; // loaded from file
	}
}