package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntIntMap;

public class Arena
{
	final static Color[] miner_colors = new Color[4];
	public static int arena_size = 24;

	static
	{
		miner_colors[0] = Color.SALMON;
		miner_colors[1] = Color.SKY;
		miner_colors[2] = Color.OLIVE;
		miner_colors[3] = Color.GOLD;
	}

	Game game;
	Timer timer_stamina_gain = new Timer(0.3f);
	Timer timer_sort_random_strings = new Timer(1f);
	Timer timer_pick_crystals = new Timer(0.1f);
	Osc osc_crystals = new Osc(3f, 15f, 10f);
	Flatbyte board;
	Flatbyte damage;
	Flatbyte floor;
	OpenSimplexNoise noise_board = new OpenSimplexNoise(10, 2);

	// MINERS
	Osc osc_arrow = new Osc(4, 25f, 20f);
	Smartrix sm_particles = new Smartrix(Particles.values().length, -1, -1);

	// CRYSTALS
	Smartrix sm_miners = new Smartrix(MinerData.values().length, -1, -1);
	Smartrix sm_crystals = new Smartrix(3, -1, -1);

	public void init(Game game)
	{
		this.game = game;
		// clean new board
		board = new Flatbyte(arena_size, arena_size, (byte) -1, (byte) 0);
		damage = new Flatbyte(arena_size, arena_size, (byte) -1, (byte) 0);

		// placing rocks pseudorandomly on board
		// probability of placement decreases with distance to center of grid

		int mid = board.width / 2;
		for (int ix = 0; ix < board.width; ix++)
		{
			for (int iy = 0; iy < board.height; iy++)
			{
				int dst_to_mid = Util.euclid_norm(ix, iy, mid, mid);

				float val = (float) ((noise_board.noise(ix, iy) + 1) / 2f);
				if (dst_to_mid < 15)
				{
					if (val <= 0.4f)
					{
						board.set(ix, iy, (byte) 1);
					}
				}
			}
		}
		// different floor tile variations (indices into sheet array)
		floor = new Flatbyte(arena_size, arena_size, (byte) -1, (byte) 0);

	}

	public void update(float delta)
	{
		osc_crystals.update(delta);
		osc_arrow.update(delta);

		if (Config.CONF.STAMINA_SYSTEM.value == 1 && timer_stamina_gain.update(delta))
		{
			// 12.01.23 give all miners some stamina back per tick
			//sm_miners.incr_all_lines(MinerData.STAMINA.ordinal(), 1);

			for (int i = 0; i < sm_miners.num_lines(); i++)
			{
				int stamina = sm_miners.get(i, MinerData.STAMINA.ordinal());
				stamina = MathUtils.clamp(stamina + Config.CONF.STAMINA_REGAIN_PER_INTERVAL.value, 0, MinerData.global_max_stamina);
				sm_miners.set(i, MinerData.STAMINA.ordinal(), stamina);
			}
		}

		CHECK_CRYSTAL_PICKS(delta);

		Particles.UPDATE_PARTICLES(this, sm_particles, delta);
	}

	private void CHECK_CRYSTAL_PICKS(float delta)
	{
		if (timer_pick_crystals.update(delta))
		{
			for (int i = 0; i < sm_miners.num_lines(); i++)
			{
				if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == -1)
				{
					// skipping unused miners
					continue;
				}
				// check all alive crystals for all miners
				for (int j = 0; j < sm_crystals.num_lines(); j++)
				{
					if (sm_crystals.get(j, Crystal.TYPE.ordinal()) == -1)
					{
						// skipping invalid crystal entries
						continue;
					} else
					{
						if (sm_crystals.get(j, Crystal.POSX.ordinal()) == sm_miners.get(i, MinerData.TILEX.ordinal()) && sm_crystals.get(j, Crystal.POSY.ordinal()) == sm_miners.get(i, MinerData.TILEY.ordinal()))
						{
							if (sm_miners.get(i, MinerData.NUM_CRYSTALS.ordinal()) < Config.CONF.CRYSTAL_MAX_POSSESSION.value)
							{
								sm_miners.incr(i, MinerData.NUM_CRYSTALS.ordinal(), 1);
								sm_crystals.clear_line(j);

								// this expects that there is only one crystal per tile at any time,
								// but even if there were more then the other ones would be picked in the next intervals
								break;
							}
						}
					}
				}
			}
		}
	}

	public void render_floor()
	{
		int off = 0;

		// RENDER FLOOR
		for (int ix = 0; ix < board.width; ix++)
		{
			for (int iy = 0; iy < board.height; iy++)
			{
				Main.batch.draw(Res.SHEET_FLOOR_TILES.sheet[0], 16 * ix, 16 * iy);
			}
		}

		// TODO: 19.07.2021 some time in future I need to Y sort entities and Walls
		// RENDER BLOCKS
		for (int ix = 0; ix < board.width; ix++)
		{
			for (int iy = board.height - 1; iy >= 0; iy--)
			{
				byte block = board.get(ix, iy);
				if (block == (byte) 1)
				{
					Main.batch.draw(Res.SHEET_BLOCKS.sheet[0], 16 * ix, 16 * iy);
					byte ord_dam = damage.get(ix, iy);
					if (ord_dam > 0)
					{
						int mapped_break = MathUtils.round(MathUtils.map(0, Config.CONF.BLOCK_LIFE.value, 0, 4, ord_dam));
						//int show_break_frame = MathUtils.clamp(mapped_break, 0, 4);
						Main.batch.draw(Res.SHEET_BREAK.sheet[mapped_break], 16 * ix, 16 * iy);
					}
				}
			}
		}
	}

	public void render_miners()
	{
		for (int i = 0; i < sm_miners.num_lines(); i++)
		{
			int minerposx = sm_miners.get(i, MinerData.TILEX.ordinal());
			int minerposy = sm_miners.get(i, MinerData.TILEY.ordinal());
			int miner_viewdir = sm_miners.get(i, MinerData.VIEWDIR.ordinal());

			if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
			{
				Main.batch.setColor(miner_colors[i]);
				Main.batch.draw(Res.GUY.sheet[0], 16 * minerposx - 16 + 8, 16 * minerposy + 8);
			} else
			{
				// DEAD
				Main.batch.setColor(miner_colors[i]);
				Main.batch.draw(Res.GUY.sheet[0], 16 * minerposx - 16 + 8, 16 * minerposy + 8, Res.GUY.sheet_width / 2f, Res.GUY.sheet_height / 2f, Res.GUY.sheet_width, Res.GUY.sheet_height, 0.75f, 0.75f, 90);
			}

			// direction arrow
			int ud, lr;
			lr = Util.fourdirx[miner_viewdir];
			ud = Util.fourdiry[miner_viewdir];

			// TODO: 16.11.22 toggle this by setting
			if (Config.CONF.RENDER_DIRECTION_ARROWS.value == 1 && sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
			{
				Main.batch.draw(Res.DIRECTIONS.sheet[miner_viewdir], 16 * minerposx + (lr * (24 + osc_arrow.value())), 16 * minerposy + (ud * (24 + osc_arrow.value())) + 8);
			}

			Main.batch.setColor(Color.WHITE);
		}
	}

	public void render_particles()
	{
		// TODO: 31.07.2021 render the particles
		Particles.RENDER_PARTICLES(sm_particles);

		Main.batch.setColor(Color.WHITE);
		// crystals count as particles here in terms of rendering
		for (int i = 0; i < sm_crystals.num_lines(); i++)
		{
			if (sm_crystals.get(i, Crystal.TYPE.ordinal()) >= 0)
			{
				int cx = 16 * sm_crystals.get(i, Crystal.POSX.ordinal());
				int cy = 16 * sm_crystals.get(i, Crystal.POSY.ordinal());
				Main.batch.draw(Res.CRYSTAL.region, cx, cy + osc_crystals.value());
			}
		}
	}

	public int spawn_particle(int tilex, int tiley, int type)
	{
		int free_index = sm_particles.find_free_line_index();

		sm_particles.set_line(free_index, 0.f, type, tilex, tiley, 0, 0, 0, MathUtils.random(0, 359), 0);

		switch (type)
		{
			case Particles.TYPE_IMPACTOR:
				int from = tilex <= Arena.arena_size / 2 ? 1 : -1;
				sm_particles.set(free_index, Particles.POSX.ordinal(), tilex + Config.CONF.IMPACTOR_INITIAL_OFFSET.value * from);
				sm_particles.set(free_index, Particles.POSY.ordinal(), tiley + Config.CONF.IMPACTOR_INITIAL_OFFSET.value);
				sm_particles.set(free_index, Particles.TARGETX.ordinal(), tilex);
				sm_particles.set(free_index, Particles.TARGETY.ordinal(), tiley);
				break;
			case Particles.TYPE_ROCK_DEBRIS:
				sm_particles.set(free_index, Particles.VARIANT.ordinal(), MathUtils.random(0, 4));
				break;
		}
		// maybe the caller wants to set variables that are only known from caller context
		return free_index;
	}

	public void impact_at(int tx, int ty)
	{
		for (int offx = -10; offx <= 10; offx++)
		{
			for (int offy = -10; offy <= 10; offy++)
			{
				int locx = tx + offx;
				int locy = ty + offy;
				if (Math.sqrt(offx * offx + offy * offy) <= Config.CONF.IMPACTOR_BLOCK_DAMAGE_RADIUS.value)
				{
					hammer(locx, locy, 10);
					hit_all_miners(locx, locy, 50);
					// this would theoretically also hit the owner of the impact if the miner managed to walk below the impact after triggering it
				}
			}
		}
	}

	public void hit_all_miners(int tx, int ty, int damage)
	{
		// this can be called from skills that hit multiple tiles like the impactor,
		// here all miners will be collision checked if they are in the area

		// iterate over miners, deal damage:
		for (int i = 0; i < sm_miners.num_lines(); i++)
		{
			// check if dead already
			if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
			{
				// checking location
				if (sm_miners.get(i, MinerData.TILEX.ordinal()) == tx && sm_miners.get(i, MinerData.TILEY.ordinal()) == ty)
				{
					// damage function must be separate since the life book keeping for the running game must be managed at one location
					deal_damage_to_miner(i, damage);
				}
			}
		}
	}

	public void deal_damage_to_miner(int miner_id, int damage)
	{
		int current_life = sm_miners.get(miner_id, MinerData.LIFE.ordinal());
		current_life -= damage;
		current_life = MathUtils.clamp(current_life, 0, Config.CONF.MINER_MAX_LIFE.value);
		sm_miners.set(miner_id, MinerData.LIFE.ordinal(), current_life);
		if (current_life <= 0)
		{
			// deactivating or killing the miner
			sm_miners.set(miner_id, MinerData.ACTIVE.ordinal(), 0);
		}
	}

	public void create_orogeny_block(int tx, int ty)
	{
		// this will have no effect if out of bounds of arena.
		boolean place_block = true;
		for (int i = 0; i < sm_miners.num_lines(); i++)
		{
			if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
			{
				if (sm_miners.get(i, MinerData.TILEX.ordinal()) == tx && sm_miners.get(i, MinerData.TILEY.ordinal()) == ty)
				{
					deal_damage_to_miner(i, 50);
					place_block = false;
					// traps the miner
					// TODO: 24.02.23 if miner moves just in this frame (right now with instant movement) they may be trapped in the neighbor block
					// 	but should be able to move into the vacant position,
					// 	later a lerping movement system would check during movement if the target position is still walkable and cancel the movement
					// 	but another solution could be that miners always check if they are trapped and destroy the block with damage penalty
				}
			}
		}

		if (place_block)
		{
			board.set(tx, ty, (byte) 1);
			damage.set(tx, ty, (byte) 0);
		}
	}

	// xy is the offset where the fault line is
	// from view dir of the author the shear sense can be detected
	public void instant_transform_fault(int xy_faultline, int hori_vert)
	{
		// DISPLACE THE TILEMAP
		int offset = Config.CONF.TRANSFORM_FAULT_OFFSET.value;

		boolean vertical = hori_vert == 2;

		// this expects the board to be a square (equal side lengths)
		byte[] temp_row_board = new byte[board.width];
		byte[] temp_row_damage = new byte[board.width];
		// using a temp row makes eliminates the need for a data preserving look up direction

		int tilex_offset = 0;
		int tiley_offset = 0;

		for (int dim1 = 0; dim1 < board.height; dim1++)
		{
			for (int dim2 = 0; dim2 < board.width; dim2++)
			{
				if (vertical) // VERTICAL FAULT LINE, MOVING IN Y DIRECTION
				{
					// dim1 = x
					// dim2 = y

					//int x_offset = i + (offset * (iy > y_pos ? 1 : -1));
					tiley_offset = offset * (dim1 > xy_faultline ? 1 : -1);

					// need to subtract here since I do not move but look up in other direction to move
					byte new_board_val = board.get_boundchecked(dim1, dim2 - tiley_offset);
					byte new_damage_val = damage.get_boundchecked(dim1, dim2 - tiley_offset);
					temp_row_board[dim2] = new_board_val == -1 ? (byte) 0 : new_board_val;
					temp_row_damage[dim2] = new_damage_val == -1 ? (byte) 0 : new_damage_val;
				} else
				{ // HORIZONTAL FAULT LINE, MOVING IN X DIRECTION
					// dim1 = y
					// dim2 = x

					tilex_offset = offset * (dim1 > xy_faultline ? -1 : 1);

					// need to subtract here since I do not move but look up in other direction to move
					byte new_board_val = board.get_boundchecked(dim2 - tilex_offset, dim1);
					byte new_damage_val = damage.get_boundchecked(dim2 - tilex_offset, dim1);
					temp_row_board[dim2] = new_board_val == -1 ? (byte) 0 : new_board_val;
					temp_row_damage[dim2] = new_damage_val == -1 ? (byte) 0 : new_damage_val;
				}
			}
			for (int dim2 = 0; dim2 < board.width; dim2++)
			{
				if (vertical)
				{
					board.set(dim1, dim2, temp_row_board[dim2]);
					damage.set(dim1, dim2, temp_row_damage[dim2]);
				} else
				{
					board.set(dim2, dim1, temp_row_board[dim2]);
					damage.set(dim2, dim1, temp_row_damage[dim2]);
				}
			}
		}

		// DISPLACE MINERS
		for (int miid = 0; miid < sm_miners.num_lines(); miid++)
		{
			if (vertical)
			{
				int y_offset = offset * (sm_miners.get(miid, MinerData.TILEX.ordinal()) > xy_faultline ? 1 : -1);
				int new_ypos = sm_miners.get(miid, MinerData.TILEY.ordinal()) + y_offset;
				if (new_ypos < 0 || new_ypos >= board.height)
				{
					deal_damage_to_miner(miid, 50);
					new_ypos = MathUtils.clamp(new_ypos, 0, board.height - 1);
				}
				sm_miners.set(miid, MinerData.TILEY.ordinal(), new_ypos);
			} else
			{
				int x_offset = offset * (sm_miners.get(miid, MinerData.TILEY.ordinal()) > xy_faultline ? -1 : 1);
				int new_xpos = sm_miners.get(miid, MinerData.TILEX.ordinal()) + x_offset;
				if (new_xpos < 0 || new_xpos >= board.width)
				{
					deal_damage_to_miner(miid, 50);
					new_xpos = MathUtils.clamp(new_xpos, 0, board.width - 1);
				}
				sm_miners.set(miid, MinerData.TILEX.ordinal(), new_xpos);
			}
			// TODO: 01.03.23 then new position may be in a block, break it
		}

		// DISPLACE PARTICLES (THAT NEED TO BE DISPLACED)
		for (int i = 0; i < sm_particles.num_lines(); i++)
		{
			// TODO: 01.03.23 here I assume all particles that should be displaced have their position
			//	as grid tiles not pixels
			int particle_type = sm_particles.get(i, Particles.TYPE.ordinal());
			if (particle_type != -1)
			{
				if (Particles.map_faults.get(particle_type, -1) != -1)
				{
					if (vertical)
					{
						int y_offset = offset * (sm_particles.get(i, Particles.POSX.ordinal()) > xy_faultline ? 1 : -1);
						int new_ypos = sm_particles.get(i, Particles.POSY.ordinal()) + y_offset;
						sm_particles.set(i, Particles.POSY.ordinal(), new_ypos);
					} else
					{
						int x_offset = offset * (sm_particles.get(i, Particles.POSY.ordinal()) > xy_faultline ? -1 : 1);
						int new_xpos = sm_particles.get(i, Particles.POSX.ordinal()) + x_offset;
						sm_particles.set(i, Particles.POSX.ordinal(), new_xpos);
					}
				}
			}
		}
	}

	public void hammer(int tx, int ty, int dealt_damage)
	{
		// TODO: 04.11.22 move this method somewhere else.
		//  this is called by the miner class static method when swining the hammer

		// TODO: 08.11.22 later hitting rocks is done differently, different moves/skills will inflict damage on rocks

		byte block_wall = board.get(tx, ty);
		if (block_wall == 0)
		{
			//System.out.println("no block at pos " + tx + " | " + ty);
		} else
		{
			byte current_damage = damage.get(tx, ty);

			current_damage += dealt_damage;
			if (current_damage >= Config.CONF.BLOCK_LIFE.value)
			{
				// break stone
				board.set(tx, ty, (byte) 0);
				for (int i = 0; i < MathUtils.random(6, 12); i++)
				{
					spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
				}
				current_damage = 0;

				if (MathUtils.random(100) < Config.CONF.CRYSTAL_CHANCE.value)
				{
					int index_crystal_free = sm_crystals.find_free_line_index();
					sm_crystals.set(index_crystal_free, Crystal.TYPE.ordinal(), 0);
					sm_crystals.set(index_crystal_free, Crystal.POSX.ordinal(), tx);
					sm_crystals.set(index_crystal_free, Crystal.POSY.ordinal(), ty);
				}
			}
			damage.set(tx, ty, current_damage);
		}
	}

	boolean free_tile(int tx, int ty)
	{
		// TODO: 16.11.22 rename, give CHECK prefix
		byte tile_floor = floor.get(tx, ty);
		byte block_wall = board.get(tx, ty);
		return block_wall <= 0;
	}

	public void UTIL_fill_empty_controller(IntIntMap iimap_controller_to_miner)
	{
		// find controller that is not yet bound to a miner and spawn miner for that controller

		int empty_controller_index = -1;
		for (IntIntMap.Entry iie : iimap_controller_to_miner)
		{
			if (iie.value < 0)
			{
				empty_controller_index = iie.key;
				break;
			}
		}

		if (empty_controller_index != -1)
		{
			int local_new_miner_index = sm_miners.num_lines();
			create_miner(Miner.MinerClass.KENKMANN);
			iimap_controller_to_miner.put(empty_controller_index, local_new_miner_index);
			System.out.println("miner " + local_new_miner_index + " is now controlled by controller " + empty_controller_index);
		} else
		{
			System.out.println("no empty controller found");
		}
	}

	public boolean UTIL_remove_uncontrolled_miner()
	{
		// TODO: 16.11.22 query the list of miners and remove the ones that do not have
		//  a controller assigned.
		//  return true if at least one has been removed

		return false;
	}

	public void create_miner(Miner.MinerClass mc)
	{
		// TODO: 12.02.23 I guess at some point when changing the number of players during rounds, miners are being removed from the list so empty entries will exist that need to be taken care of

		// free miner index
		int fmi = sm_miners.find_free_line_index();
		sm_miners.set(fmi, MinerData.TILEX.ordinal(), 0);
		sm_miners.set(fmi, MinerData.TILEY.ordinal(), 0);
		sm_miners.set(fmi, MinerData.NUM_CRYSTALS.ordinal(), 0);
		sm_miners.set(fmi, MinerData.VIEWDIR.ordinal(), 0);
		sm_miners.set(fmi, MinerData.ACTIVE.ordinal(), 1);

		// TODO: 22.02.23 the max life may change per miner class in future!
		sm_miners.set(fmi, MinerData.LIFE.ordinal(), Config.CONF.MINER_MAX_LIFE.value);
		sm_miners.set(fmi, MinerData.CLASS.ordinal(), mc.ordinal());

		// HERE ALL THE INITIAL VALUES HAVE TO BE SET
	}

	public void render_HUD()
	{
		if (Main.debug_render)
		{
			int running_debug_offset = -10;
			Text.draw("num particles " + sm_particles.num_lines(), 3, Main.upper_y_bound() + running_debug_offset);
			running_debug_offset -= 10;
		}


		// HUD is abstracted and rendered offset for every miner
		for (int i = 0; i < sm_miners.num_lines(); i++)
		{
			MinerData.render_miner_HUD(sm_miners, i);
		}
	}
}