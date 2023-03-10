package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;

public enum MinerData
{
	TILEX,
	TILEY,
	VIEWDIR,
	LIFE,
	MINERALS,
	STAMINA,
	NUM_CRYSTALS,
	ACTIVE,
	CLASS, // ORDINAL of MINER_CLASS
	;

	// // TODO: 12.01.23 introduce more values for the miners that are used in the game 

	final static Color color_box_back = Color.DARK_GRAY.cpy();
	final static Color color_white_transparent = Color.WHITE.cpy();
	public static int global_max_stamina = 100;

	static
	{
		color_box_back.a = 0.6f;
		color_white_transparent.a = 0.3f;
	}

	public static void render_miner_HUD(Smartrix smx_miner, int miner_id)
	{
		int miner_offset = 110 * miner_id;
		// miner_id == line of smx

		int box_width = 100;
		int box_height = 50;
		int inner_margin = 10;
		int running_y_offset = inner_margin + box_height - 10;
		int running_offset_step_big = 10;
		int running_offset_step_small = 6;

		Color color_box = Arena.miner_colors[miner_id].cpy();
		color_box = color_box.mul(Color.GRAY);
		color_box.a = 0.6f;
		RenderUtil.render_box(miner_offset + inner_margin, inner_margin, box_width, box_height, color_box);

		if (Config.CONF.STAMINA_SYSTEM.value == 1)
		{
			int stam = smx_miner.get(miner_id, MinerData.STAMINA.ordinal());
			int stam_max = 100;
			Text.cdraw("stamina", miner_offset + inner_margin + (box_width / 2), running_y_offset, Color.GOLD);
			running_y_offset -= running_offset_step_small;
			RenderUtil.render_bar(miner_offset + inner_margin + 4, running_y_offset, box_width - 8, 3, Color.BROWN, Color.GOLD, stam / ((float) stam_max));
			running_y_offset -= running_offset_step_big;
		}

		Miner.MinerClass mc = Miner.MinerClass.safe_ord(smx_miner.get(miner_id, CLASS.ordinal()));
		Text.cdraw(mc == null ? "NO CLASS" : mc.name(), miner_offset + inner_margin + (box_width / 2), running_y_offset, Color.GOLD);
		running_y_offset -= running_offset_step_big;

		Text.cdraw("health", miner_offset + inner_margin + (box_width / 2), running_y_offset, Color.SALMON);
		running_y_offset -= running_offset_step_small;
		int miner_life = smx_miner.get(miner_id, MinerData.LIFE.ordinal());
		RenderUtil.render_bar(miner_offset + inner_margin + 4, running_y_offset, box_width - 8, 3, Color.FIREBRICK, Color.SALMON, miner_life / ((float) Config.CONF.MINER_MAX_LIFE.value));
		running_y_offset -= running_offset_step_big + 2;

		int num_crystals = smx_miner.get(miner_id, MinerData.NUM_CRYSTALS.ordinal());
		for (int i = 0; i < Config.CONF.CRYSTAL_MAX_POSSESSION.value; i++)
		{
			Main.batch.setColor(i < num_crystals ? Color.WHITE : color_white_transparent);
			Main.batch.draw(Res.CRYSTAL.region, miner_offset + inner_margin + (i * 11) + 2, running_y_offset, 10, 10);
		}
		Main.batch.setColor(Color.WHITE);
		running_y_offset -= running_offset_step_big + 2;

		if (smx_miner.get(miner_id, MinerData.ACTIVE.ordinal()) != 1)
		{
			Text.cdraw("DEAD", miner_offset + inner_margin + (box_width / 2), running_y_offset, Color.MAGENTA);
		}
	}
}