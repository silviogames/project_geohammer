package me.silviogames.geha;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntIntMap;

public enum Particles
{
	TYPE,
	POSX,
	POSY,
	TARGETX, // MAY NOT BE USED BY ALL PARTICLES
	TARGETY,
	LIFE,
	ANGLE,
	VARIANT,

	;

	// TYPES:
	public static final int TYPE_ROCK_DEBRIS = 1;
	public static final int TYPE_IMPACTOR = 2;
	// VARIANT 1 = coming from left
	// VARIANT 2 = coming from right
	// TODO: 15.02.23 but maybe this can be handled without variants just the inital conditions

	public static final int TYPE_IMPACTOR_SMOKE = 3;
	public static final int TYPE_IMPACTOR_SPARK = 4;

	public static final int TYPE_OROGEN = 10;

	public static final int TYPE_TRANSFORM_FAULT = 20;
	// POSX = HORIZONTAL [1], VERTICAL[2]
	// POSY = FAULT_LINE_XY

	public static final IntIntMap map_position_as_tiles = new IntIntMap();
	// TYPE_IDS of particles point to
	// -1 -> position is in pixels
	// 1 -> position is in tiles

	public static final IntIntMap map_faults = new IntIntMap();
	// TYPE_IDS of particles point to
	// -1 -> is NOT affected by a fault displacement
	// 1 -> IS affected by a fault displacement

	static
	{
		map_position_as_tiles.put(TYPE_OROGEN, 1);
		map_position_as_tiles.put(TYPE_IMPACTOR, 1);

		// it looks like it is not but it internally does a pixel displacement during render
		map_position_as_tiles.put(TYPE_ROCK_DEBRIS, 1);

		map_faults.put(TYPE_OROGEN, 1);
	}


	public static void RENDER_PARTICLES(Smartrix sm_particles)
	{
		int[] data_particle = new int[values().length];

		for (int i = 0; i < sm_particles.num_lines(); i++)
		{
			sm_particles.get_line(i, data_particle);

			if (data_particle[LIFE.ordinal()] < 0) continue;

			switch (data_particle[TYPE.ordinal()])
			{
				case 1: // ROCK DEBRIS
				{
					int angle = data_particle[ANGLE.ordinal()];
					float dx = MathUtils.cosDeg(angle);
					float dy = MathUtils.sinDeg(angle);
					float rx = 16 * data_particle[POSX.ordinal()] + (data_particle[LIFE.ordinal()] / 5f) * dx;
					float ry = 16 * data_particle[POSY.ordinal()] + (data_particle[LIFE.ordinal()] / 5f) * dy;
					Main.batch.setColor(1f, 1f, 1f, (255 - data_particle[LIFE.ordinal()]) / 255f);
					Main.batch.draw(Res.PARTICLES_ROCK.sheet[data_particle[VARIANT.ordinal()]], rx, ry);
				}
				break;
				case 2: // IMPACTOR
				{
					float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					float interp = Interpolation.pow2In.apply(life);
					Main.batch.setColor(1f, 1f, 1f, 0.8f + MathUtils.lerp(0, 0.2f, interp));
					float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
					float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
					Main.batch.draw(Res.IMPACTOR.region, 16 * rx, 16 * ry);
				}
				break;
				case TYPE_IMPACTOR_SMOKE:
				{
					float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					Main.batch.setColor(1f, 1f, 1f, 0.2f + MathUtils.lerp(0.8f, 0f, life));
					Main.batch.draw(Res.PARTICLES_ROCK.sheet[4], data_particle[POSX.ordinal()] - Res.PARTICLES_ROCK.sheet_width / 2f, data_particle[POSY.ordinal()] - Res.PARTICLES_ROCK.sheet_width / 2f);
				}
				break;
				case TYPE_IMPACTOR_SPARK:
				{
					float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					float interp = Interpolation.pow2In.apply(life);
					Main.batch.setColor(1f, 1f, 1f, 0.8f + MathUtils.lerp(0.2f, 0f, interp));
					float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
					float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
					Main.batch.draw(Res.PIXEL.region, rx, ry);
				}
				break;

				case TYPE_OROGEN:
				{

					// TODO: 26.02.23 right now no rendering of the actual particle, it spawns debris though
				}
				break;
			}
		}
	}

	public static void UPDATE_PARTICLES(Arena arena, Smartrix sm_particles, float delta)
	{
		// IT SHOULD BE POSSIBLE TO CREATE NEW PARTICLES DURING THE LOOP SINCE EITHER A FREE SPOT IS FOUND OR THE ARRAY IN ENLARGED WHICH IS FINE SINCE LOOP WILL NOT CONSIDER THE NEW ENTRIES

		int[] data_particle = new int[values().length];
		for (int i = 0; i < sm_particles.num_lines(); i++)
		{
			sm_particles.get_line(i, data_particle);

			boolean dead = false;
			if (data_particle[LIFE.ordinal()] == -1)
			{
				continue;
			}

			switch (data_particle[TYPE.ordinal()])
			{
				case 1: // ROCK DEBRIS
					// TODO: 14.02.23 change life to float saved as int  (ms as ints)
					data_particle[LIFE.ordinal()] += 3;
					if (data_particle[LIFE.ordinal()] > 255)
					{
						dead = true;
					}
					break;

				case 2: // IMPACTOR
				{
					float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					time = MathUtils.clamp(time + delta / 1.7f, 0, 1);
					if (time >= 1)
					{
						dead = true;
						// SPAWN FINISH PARTICLES
						for (int j = 0; j < 32; j++)
						{
							int rx = data_particle[TARGETX.ordinal()];
							int ry = data_particle[TARGETY.ordinal()];
							//int[] offset = Util.RANDOM_RADIAL_OFFSET(Config.CONF.IMPACTOR_SMOKE_RADIAL_OFFSET.value * 2);
							arena.spawn_particle(rx, ry, TYPE_ROCK_DEBRIS);
						}

						arena.impact_at(data_particle[TARGETX.ordinal()], data_particle[TARGETY.ordinal()]);

					} else
					{
						// SPAWN SMOKE PARTICLEs
						data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
						float interp = Interpolation.pow2In.apply(time - 0.03f);

						for (int j = 0; j < 4; j++)
						{
							float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
							float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
							int[] offset = Util.RANDOM_RADIAL_OFFSET(Config.CONF.IMPACTOR_SMOKE_RADIAL_OFFSET.value);
							// CREATING A PARTICLES USING PIXEL POSITIONS!
							int particle_index = arena.spawn_particle((int) (rx * 16) + 16 + offset[0], (int) (ry * 16) + 16 + offset[1], TYPE_IMPACTOR_SMOKE);
							sm_particles.set(particle_index, VARIANT.ordinal(), MathUtils.random(1, 6));

							offset = Util.RANDOM_RADIAL_OFFSET(Config.CONF.IMPACTOR_SMOKE_RADIAL_OFFSET.value);
							// CREATING A PARTICLES USING PIXEL POSITIONS!
							particle_index = arena.spawn_particle((int) (rx * 16) + 16 + offset[0], (int) (ry * 16) + 16 + offset[1], TYPE_IMPACTOR_SPARK);
							sm_particles.set(particle_index, TARGETX.ordinal(), (int) (rx * 16) + 16 + offset[0] + 10);
							sm_particles.set(particle_index, TARGETY.ordinal(), (int) (ry * 16) + 16 + offset[0] + 10);
							sm_particles.set(particle_index, VARIANT.ordinal(), MathUtils.random(1, 6));
						}
					}
				}
				break;
				case TYPE_IMPACTOR_SMOKE:
				case TYPE_IMPACTOR_SPARK:
				{
					float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					int variant = data_particle[VARIANT.ordinal()];
					time = MathUtils.clamp(time + delta * variant, 0, 1);
					if (time >= 1) dead = true;
					data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
				}
				break;
				case TYPE_OROGEN:
				{
					float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					float creation_time = 1 / (Config.CONF.OROGENY_CREATION_TIME_MS.value / 1000f);
					time = MathUtils.clamp(time + delta * creation_time, 0, 1);
					if (time >= 1)
					{
						dead = true;
						int tx = data_particle[POSX.ordinal()];
						int ty = data_particle[POSY.ordinal()];
						arena.create_orogeny_block(tx, ty);
					} else
					{
						// SPAWN SMOKE PARTICLES
						int tx = data_particle[POSX.ordinal()];
						int ty = data_particle[POSY.ordinal()];

						if (MathUtils.randomBoolean(0.2f))
						{
							arena.spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
						}
						data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
					}
				}
				break;

				case TYPE_TRANSFORM_FAULT:
				{
					float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
					float creation_time = 1 / (Config.CONF.TRANSFORM_FAULT_DELAY_MS.value / 1000f);
					time = MathUtils.clamp(time + delta * creation_time, 0, 1);
					if (time >= 1)
					{
						dead = true;

						int hori_vert = data_particle[POSX.ordinal()];
						int xy_offset = data_particle[POSY.ordinal()];
						// performing the currently instant transform fault
						arena.instant_transform_fault(xy_offset, hori_vert);
					} else
					{
						// SPAWN SMOKE PARTICLES
						int hori_vert = data_particle[POSX.ordinal()];
						int xy_offset = data_particle[POSY.ordinal()];

						for (int xy = 0; xy < arena.board.width; xy++)
						{
							if (MathUtils.randomBoolean(0.2f))
							{
								if (hori_vert == 1)
								{ // HORIZONTAL
									// TODO: 07.03.23 since the method expects tile positions this is not placed correclty but for testing it is fine
									arena.spawn_particle(xy, xy_offset, Particles.TYPE_ROCK_DEBRIS);
								} else if (hori_vert == 2)
								{ // VERTICAL
									arena.spawn_particle(xy_offset, xy, Particles.TYPE_ROCK_DEBRIS);
								}
							}
						}
						data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
					}
				}
				break;
			}

			// update particle data
			sm_particles.set_line(i, 0f, data_particle);

			if (dead) sm_particles.clear_line(i);
		}
	}
}