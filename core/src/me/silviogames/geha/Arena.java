package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

public class Arena
{
   public static int arena_width = 50, arena_height = 36;

   public static int tile_size_px = 16;

   int seed = 0;

   Game game;
   Timer timer_stamina_gain = new Timer(0.3f);
   Timer timer_sort_random_strings = new Timer(1f);
   Timer timer_diffusion = new Timer(0.3f);
   Timer timer_freeze_melt = new Timer(1f);

   Timer timer_grow_rock = new Timer(0.03f);
   boolean grow_tick = false;

   // the time will
   Timer timer_pick_crystals = new Timer(0.05f);
   Osc osc_crystals = new Osc(3f, 15f, 10f);

   // USING ROCKTYPES
   Flatbyte rock_walls;
   Flatbyte damage;
   Flatbyte floor;

   // VALUE OF HOW MUCH THE ROCK IS GROWN, USED FOR OROGENY SKILL AND SPAWNING NEW ROCKS AT EDGE
   Flatbyte rock_growth;

   // this map keeps track where braided segments have been placed already to prevent multiples
   Flatbyte braided_flow;

   Flatbyte gravel;

   Flatbyte ice;

   Flatbyte melt;
   // second melt map since I cannot perform in place pseudo diffusion of the liquid
   Flatbyte melt_after;

   Array<Flatbyte> lists_to_reset = new Array<>();

   // THESE TWO ARE CREATED ONCE ON APPLICATION START WITH ALL UNIQUE POSITIONS IN THE ARENA GRID
   // IN A RANDOM ORDER, WHICH I USE TO CHECK THE FILLSTATUS OF THE GRID
   static IntArray edge_rockwalls_rnd_tilex = new IntArray();
   static IntArray edge_rockwalls_rnd_tiley = new IntArray();
   // THIS LIST WILL LATER BE SHUFFLED TO HAVE A RANDOM LOOK UP OF THE UPPER TWO
   static IntArray edge_rockwalls_rnd_index = new IntArray();
   // THIS INDEX POINTS TO THE TILE ON THE GRID THAT IS CHECKED THIS FRAME
   static int current_check_index = 0;
   // THIS ACCUMULATES THE NUMBER OF WALLS
   static int ongoing_rockwall_count = 0;
   static int last_rockfill_percent = 0;

   // I store here the global values for a thrust fault, only one can happen at each time.
   //this will be the offset where the fault line is ( is a x value if fault line is vertical!)
   static int fault_xy = -1;
   static boolean fault_vertical = false;
   static float fault_progress = 0f;
   // this flag should prevent multiple faults to happen, even if two players press the button in the same frame (after the animation has played of course)
   static boolean fault_ongoing = false;

   Flatbyte drops; // different types of crystals
   Flatbyte drop_noise;

   OpenSimplexNoise noise_board = new OpenSimplexNoise(200, 2);
   OpenSimplexNoise noise_rocktype = new OpenSimplexNoise(300, 4);

   OpenSimplexNoise crystal_noise = new OpenSimplexNoise(300, 1);

   // MINERS
   public static Osc osc_arrow = new Osc(4, 25f, 20f);
   Smartrix sm_particles = new Smartrix(Particles.values().length, -1, -1);

   // CRYSTALS
   Smartrix sm_miners = new Smartrix(MinerData.values().length, -1, -1);
   //Smartrix sm_crystals = new Smartrix(3, -1, -1);

   static int num_total_rocks = 0;

   static int total_melt_sum = 0;

   // [0] TILEY, [1] OBJECT_TYPE, [2] OBJECT_INDEX
   Smartrix sm_sort_objects = new Smartrix(3, -1, -1);
   // OBJECT_TYPE:
   // 1 = miner
   // 2 = particle

   public void init(Game game)
   {
      // THIS IS CALLED ONCE ON START OF APPLICATION,
      // to regenerate the arena for another round call prepare()

      this.game = game;
      // clean new board
      rock_walls = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) -1);
      rock_growth = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 15);
      damage = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      braided_flow = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      gravel = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      ice = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      melt = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      melt_after = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);
      drops = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) -1);
      drop_noise = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) -1);

      // placing rocks pseudo-randomly on board
      // probability of placement decreases with distance to center of grid

      // different floor tile variations (indices into sheet array)
      floor = new Flatbyte(arena_width, arena_height, (byte) -1, (byte) 0);

      for (int ix = 0; ix < rock_walls.width; ix++)
      {
         for (int iy = 0; iy < rock_walls.height; iy++)
         {
            drop_noise.set(ix, iy, (byte) (crystal_noise.noise(ix, iy) * 100));
            int edge_margin = 5;
            // KEEPING THE EDGE TILES ONLY
            if ((ix < edge_margin || ix > rock_walls.width - 1 - edge_margin) || (iy < edge_margin || iy > rock_walls.height - 1 - edge_margin))
            {
               edge_rockwalls_rnd_tilex.add(ix);
               edge_rockwalls_rnd_tiley.add(iy);
               int s = edge_rockwalls_rnd_index.size;
               edge_rockwalls_rnd_index.add(s);
            }
         }
      }
      edge_rockwalls_rnd_index.shuffle();

      lists_to_reset.add(rock_walls, rock_growth, damage, braided_flow);
      lists_to_reset.add(gravel, ice, melt, melt_after);
      lists_to_reset.add(drops);
   }

   public void update(float delta)
   {
      if (fault_ongoing)
      {
         float fault_speed = 1 / (Config.CONF.TRANSFORM_FAULT_SPEED_MS.value / 1000f);
         fault_progress += delta * fault_speed;
         if (fault_progress >= 1f)
         {
            fault_ongoing = false;
            fault_progress = 0f;
            instant_transform_fault(fault_xy, fault_vertical ? 2 : 1);
         }
      }

      if (Main.debug_slow_motion)
      {
         delta /= Main.slow_motion_factor;
      }
      osc_crystals.update(delta);
      osc_arrow.update(delta);

      // BEGIN OF WALL REPLENISH
      // arena replenish of rock walls if there are less than X percent,
      // place them on the outer border on free tiles
      byte block_wall = rock_walls.get_raw(current_check_index);
      RockTypes rt = RockTypes.safe_ord(block_wall);
      if (rt != null) ongoing_rockwall_count++;
      current_check_index++;
      if (current_check_index >= rock_walls.length)
      {
         current_check_index = 0;
         // GENERATE NEW ROCKS ON EDGE OF ARENA
         last_rockfill_percent = MathUtils.round(100f * (ongoing_rockwall_count / ((float) rock_walls.length)));
         if (last_rockfill_percent < 20)
         {
            for (int i = 0; i < 50 - last_rockfill_percent; i++)
            {
               int random_edge_index = edge_rockwalls_rnd_index.random();
               int posx = edge_rockwalls_rnd_tilex.get(random_edge_index);
               int posy = edge_rockwalls_rnd_tiley.get(random_edge_index);
               // check
               byte check_block_wall = rock_walls.get(posx, posy);
               RockTypes check_rt = RockTypes.safe_ord(check_block_wall);

               // IF a miner is on this location, the miner update will deal damage to the miner and remove the rock

               if (check_rt == null)
               {
                  rock_walls.set(posx, posy, (byte) RockTypes.generate_from_noise(MathUtils.random(0f, 1f)).ordinal());
                  rock_growth.set(posx, posy, (byte) 0);

                  // remove crystals if spawning rock wall on top
                  Drops local_drop = Drops.safe_ord(drops.get(posx, posy));
                  if (local_drop != null)
                  {
                     drops.set(posx, posy, (byte) -1);
                  }
               }
            }
         }
         ongoing_rockwall_count = 0;
      }
      // END OF WALL REPLENISH

      if (timer_diffusion.update(delta))
      {
         byte local_melt;
         int num_neighbors;
         boolean[] free_neighbor = new boolean[4];

         total_melt_sum = 0;
         for (int ix = 0; ix < melt.width; ix++)
         {
            for (int iy = 0; iy < melt.height; iy++)
            {
               num_neighbors = 0;
               // (1) read local value
               local_melt = melt.get(ix, iy);
               total_melt_sum += local_melt;

               if (local_melt > Config.CONF.MELT_FREEZE_VALUE.value)
               {
                  // (2) enumerate number of the 4 adjacent tiles that are passable
                  for (int neigh = 0; neigh < 4; neigh++)
                  {
                     free_neighbor[neigh] = CHECK_free_tile(ix + Util.fourdirx[neigh], iy + Util.fourdiry[neigh]) && CHECK_bounds(ix + Util.fourdirx[neigh], iy + Util.fourdiry[neigh]);
                     num_neighbors += free_neighbor[neigh] ? 1 : 0;
                  }
                  byte val_split = (byte) (local_melt / (num_neighbors + 1));
                  melt_after.incr(ix, iy, val_split);
                  for (int neigh = 0; neigh < 4; neigh++)
                  {
                     if (free_neighbor[neigh])
                     {
                        melt_after.incr(ix + Util.fourdirx[neigh], iy + Util.fourdiry[neigh], val_split);
                     }
                  }
               } else if (local_melt > 0)
               {
                  if (MathUtils.randomBoolean(0.3f))
                  {
                     local_melt--;
                  }
                  melt_after.incr(ix, iy, (byte) local_melt);
               }
               // (3) split the local value by number from (2) + 1 and add that value in all
               //     passable tiles in melt_after
            }
         }

         for (int ix = 0; ix < melt.width; ix++)
         {
            for (int iy = 0; iy < melt.height; iy++)
            {
               // (4) copy melt_after to melt_after
               // clear melt after
               melt.set(ix, iy, melt_after.get(ix, iy));
               melt_after.set(ix, iy, (byte) 0);
            }
         }
      }


      if (timer_grow_rock.update(delta))
      {
         grow_tick = true;
      }

      if (grow_tick)
      {
         for (int ix = 0; ix < rock_growth.width; ix++)
         {
            for (int iy = 0; iy < rock_growth.height; iy++)
            {
               if (rock_growth.get(ix, iy) < 15)
               {
                  rock_growth.incr(ix, iy, (byte) 1);
               }
            }
         }
         grow_tick = false;
      }

      sm_sort_objects.clear();

      Particles.UPDATE_PARTICLES(this, sm_particles, delta);

      CHECK_CRYSTAL_PICKS();

      update_miners(delta);

      // now all to be sorted objects are added to sm_sort_objects

      sm_sort_objects.sort(0, false);
   }

   private void update_miners(float delta)
   {

      // stamina system is not in use! may remove at some point
      if (Config.CONF.STAMINA_SYSTEM.value == 1 && timer_stamina_gain.update(delta))
      {
         // 12.01.23 give all miners some stamina back per tick
         //sm_miners.incr_all_lines(MinerData.STAMINA.ordinal(), 1);
         //

         for (int i = 0; i < sm_miners.num_lines(); i++)
         {
            int stamina = sm_miners.get(i, MinerData.STAMINA.ordinal());
            stamina = MathUtils.clamp(stamina + Config.CONF.STAMINA_REGAIN_PER_INTERVAL.value, 0, MinerData.global_max_stamina);
            sm_miners.set(i, MinerData.STAMINA.ordinal(), stamina);

         }
      }

      float time_max = Util.INT_TO_FLOAT(Config.CONF.MINER_BLINK_MAX_TIME.value);

      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         // adding this miner to the to be sorted list
         sm_sort_objects.add_line(sm_miners.get(i, (MinerData.TILEY.ordinal())), 1, i);

         // LERP DISPLAY LIFE
         // must happen even if miner is dead otherwise the life bar will stop!
         float correct_life = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.LIFE.ordinal()));
         float display_life = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.LIFE_DISPLAY.ordinal()));
         display_life = MathUtils.lerp(display_life, correct_life, 0.05f);
         sm_miners.set(i, MinerData.LIFE_DISPLAY.ordinal(), Util.FLOAT_TO_INT(display_life));

         if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) != 1) return;

         float time_blink = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.BLINK.ordinal()));
         if (time_blink > 0f)
         {
            time_blink += delta;
            if (time_blink >= time_max)
            {
               time_blink = 0f;
            }
            sm_miners.set(i, MinerData.BLINK.ordinal(), Util.FLOAT_TO_INT(time_blink));
         } else
         {
            // DAMAGE CHECK (ONCE PER FRAME)
            int miner_x = sm_miners.get(i, MinerData.TILEX.ordinal());
            int miner_y = sm_miners.get(i, MinerData.TILEY.ordinal());

            // all the damage checks are done but as soon as one triggers, the blink timer being larger than 0f will prevent further damage in this frame

            RockTypes rt = RockTypes.safe_ord(rock_walls.get(miner_x, miner_y));
            if (rt != null)
            {
               // using orogeny damage here
               deal_damage_to_miner(i, Config.CONF.OROGENY_HIT_DAMAGE.value);
               hammer(miner_x, miner_y, 30, false);
            }

            // SOME MAP EFFECT LAYERS DEAL DAMAGE
            // THESE WILL TRIGGER CONTINUOUSLY AS LONG AS STANDING ON LAVA
            if (melt.get(miner_x, miner_y) > 0)
            {
               deal_damage_to_miner(i, Config.CONF.MELT_DAMAGE.value);
               // TODO: 17.05.23 spawn smoke particles
            }

            if (braided_flow.get(miner_x, miner_y) > 0)
            {
               deal_damage_to_miner(i, Config.CONF.BRAIDED_DAMAGE.value);
            }
         }

         // UPDATE SPIN TIME
         float time_spin = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.TIME_SPIN_EFFECT.ordinal()));
         time_spin = MathUtils.clamp(time_spin - delta, 0f, Util.INT_TO_FLOAT(Config.CONF.MINER_SPIN_MAX_TIME_MS.value));
         sm_miners.set(i, MinerData.TIME_SPIN_EFFECT.ordinal(), Util.FLOAT_TO_INT(time_spin));

         // UPDATE POWER TIME
         float time_power = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.TIME_POWER_EFFECT.ordinal()));
         time_power = MathUtils.clamp(time_power - delta, 0f, Util.INT_TO_FLOAT(Config.CONF.CRYSTAL_POWER_MAX_TIME_MS.value));
         sm_miners.set(i, MinerData.TIME_POWER_EFFECT.ordinal(), Util.FLOAT_TO_INT(time_power));

         // ANIMATION UPDATE

         MinerAnim ma = MinerAnim.safe_ord(sm_miners.get(i, MinerData.ANIM.ordinal()));
         float anim_time = Util.INT_TO_FLOAT(sm_miners.get(i, MinerData.ANIM_TIME.ordinal()));
         float[] ret = ma.anim.update(anim_time, delta);

         sm_miners.set(i, MinerData.ANIM_TIME.ordinal(), Util.FLOAT_TO_INT(ret[0]));
         if (ret[1] == 1)
         {
            ma.key_frame(this, sm_miners, i);
         }
         if (ret[2] == 1)
         {
            MinerAnim follow_anim = ma.anim_over(this, sm_miners, i);
            // ANIMATION IS OVER, resetting to idle
            sm_miners.set(i, MinerData.ANIM.ordinal(), follow_anim.ordinal());
         } else
         {
            // DO THE SPIN EFFECT
            if (ma == MinerAnim.SPIN_MOVING || ma == MinerAnim.SPIN_ON_SPOT)
            {
               int last_offset_index = sm_miners.get(i, MinerData.SPIN_OFFSET_INDEX.ordinal());
               float anim_time_frac = ma.anim.get_fractional_time(ret[0]);
               int eight_dir_offset_index = MathUtils.round(MathUtils.map(0f, 1f, 0, 7, anim_time_frac));
               // TODO: 30.05.23 the last position is never reached due to timing issues with the animation!
               //  maybe instead of trying to split the animation time into 8,
               //  just apply the damage to all 8 neighbors at the end of the animation, players
               //  will not notice it anyways but atleast it is more deterministic to really hit all tiles regardless of long frames.
               //  or count offset, do not derive it from anim time
               if (eight_dir_offset_index != last_offset_index)
               {
                  int px = sm_miners.get(i, MinerData.TILEX.ordinal());
                  int py = sm_miners.get(i, MinerData.TILEY.ordinal());
                  hammer(px + Util.eightdirx[eight_dir_offset_index], py + Util.eightdiry[eight_dir_offset_index], 4, false);
                  hit_all_miners(px + Util.eightdirx[eight_dir_offset_index], py + Util.eightdiry[eight_dir_offset_index], Config.CONF.MINER_SPIN_DAMAGE.value);
                  sm_miners.set(i, MinerData.SPIN_OFFSET_INDEX.ordinal(), eight_dir_offset_index);
               }
            }
         }
      }
   }

   public int FIND_tschermak_target(int exclude_miner_id)
   {
      // pick one random other miner that is not exclude_miner_id
      // returning the miner_id
      Util.temp_random_miner.clear();
      int ret = -1;
      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         if (i == exclude_miner_id) continue;
         if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
         {
            Util.temp_random_miner.add(i);
         }
      }
      if (Util.temp_random_miner.size > 0)
      {
         ret = Util.temp_random_miner.random();
         Util.temp_random_miner.clear();
      }

      return ret;
   }

   private void CHECK_CRYSTAL_PICKS()
   {
      // this used to be checked in certain time intervals but that I did when the
      // crystals used to be stored in arrays.
      // now that drops are stored in a flatmap, I can quickly check it every frame

      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == -1)
         {
            // skipping unused miners
            continue;
         }

         int miner_x = sm_miners.get(i, MinerData.TILEX.ordinal());
         int miner_y = sm_miners.get(i, MinerData.TILEY.ordinal());

         Drops local_drop = Drops.safe_ord(drops.get(miner_x, miner_y));

         boolean picked = false;

         if (local_drop != null)
         {
            switch (local_drop)
            {
               case CRYSTAL:
                  if (sm_miners.get(i, MinerData.NUM_CRYSTALS.ordinal()) + sm_miners.get(i, MinerData.FUTURE_CRYSTALS.ordinal()) < Config.CONF.CRYSTAL_MAX_POSSESSION.value)
                  {
                     // remove drop from map
                     //sm_miners.incr(i, MinerData.NUM_CRYSTALS.ordinal(), 1);
                     int pid = spawn_particle(miner_x * 16 + 3, miner_y * 16 + 3, Particles.TYPE_OBTAIN_CRYSTAL);
                     int miner_offset = 110 * i;

                     int inner_margin = 10;

                     int num_crystals = sm_miners.get(i, MinerData.NUM_CRYSTALS.ordinal()) + sm_miners.get(i, MinerData.FUTURE_CRYSTALS.ordinal());

                     sm_miners.incr(i, MinerData.FUTURE_CRYSTALS.ordinal(), 1);

                     int px = miner_offset + inner_margin + (num_crystals * 11) + 6;
                     sm_particles.set(pid, Particles.TARGETX.ordinal(), px);
                     sm_particles.set(pid, Particles.TARGETY.ordinal(), 16);
                     sm_particles.set(pid, Particles.DATA1.ordinal(), i);
                     picked = true;
                  }
                  break;
               case LIFE:
                  int miner_life = sm_miners.get(i, MinerData.LIFE.ordinal());
                  if (miner_life < Config.CONF.MINER_MAX_LIFE.value)
                  {
                     picked = true;
                     miner_life = MathUtils.clamp(miner_life + Config.CONF.MINER_LIFEGAIN_FROM_CRYSTAL.value, 0, Config.CONF.MINER_MAX_LIFE.value);
                     sm_miners.set(i, MinerData.LIFE.ordinal(), miner_life);
                  }
                  break;
               case POWER:
                  picked = true;
                  sm_miners.set(i, MinerData.TIME_POWER_EFFECT.ordinal(), Config.CONF.CRYSTAL_POWER_MAX_TIME_MS.value);
                  break;
               case SPIN:
                  picked = true;
                  sm_miners.set(i, MinerData.TIME_SPIN_EFFECT.ordinal(), Config.CONF.MINER_SPIN_MAX_TIME_MS.value);
                  break;
            }
         }
         if (picked)
         {
            drops.set(miner_x, miner_y, (byte) -1);
         }
      }
   }

   public void render_floor()
   {
      int off = 0;

      // RENDER FLOOR
      // RENDER TILES

      // interval from 0 to 1
      float wall_growth_prog = 0f;
      float x_fault_offset = 0, y_fault_offset = 0;
      float interpolated_fault_prog = Interpolation.exp5.apply(fault_progress);
      int last_sorted_index = 0;

      for (int iy = rock_walls.height - 1; iy >= 0; iy--)
      {
         for (int ix = 0; ix < rock_walls.width; ix++)
         {
            if (fault_vertical)
            {
               y_fault_offset = -Config.CONF.TRANSFORM_FAULT_DISPLACEMENT.value * Arena.tile_size_px * interpolated_fault_prog;
               if (ix > fault_xy)
               {
                  y_fault_offset = -y_fault_offset;
               }
            } else
            {
               x_fault_offset = -Config.CONF.TRANSFORM_FAULT_DISPLACEMENT.value * Arena.tile_size_px * interpolated_fault_prog;
               if (iy < fault_xy)
               {
                  x_fault_offset = -x_fault_offset;
               }
            }

            // FLOOR TEXTURE
            Main.batch.draw(Res.SHEET_FLOOR_TILES.sheet[0], 16 * ix + x_fault_offset, 16 * iy + y_fault_offset);

            // EFFECT LAYERS:

            // ICE:
            if (ice.get(ix, iy) == 1)
            {
               RenderUtil.render_box(16 * ix, 16 * iy, 16, 16, RenderUtil.ICE_COLOR);
            }

            byte local_melt = melt.get(ix, iy);
            if (local_melt > 0)
            {
               if (local_melt <= Config.CONF.MELT_FREEZE_VALUE.value)
               {
                  RenderUtil.interp(Color.BLACK, Color.ORANGE, MathUtils.map(0f, Config.CONF.MELT_FREEZE_VALUE.value, 0.2f, 1f, local_melt));
                  RenderUtil.render_box(16 * ix, 16 * iy, 16, 16, RenderUtil.color_interp);
               } else
               {
                  RenderUtil.render_box(16 * ix, 16 * iy, 16, 16, Color.ORANGE);
               }
            }

            // GRAVEL:
            if (gravel.get(ix, iy) > 0)
            {
               // TODO: 07.06.23 crash
               Main.batch.draw(Res.SHEET_GRAVEL.sheet[gravel.get(ix, iy) - 1], 16 * ix, 16 * iy);
            }

            // DROPS
            Drops drop = Drops.safe_ord(drops.get(ix, iy));
            if (drop != null)
            {
               Main.batch.setColor(drop.color);
               Main.batch.draw(Res.CRYSTAL.region, 16 * ix, 16 * iy + 4 + MathUtils.sin(Game.game_time + drop_noise.get(ix, iy)) * 4);
               Main.batch.setColor(Color.WHITE);
            }

            byte block = rock_walls.get(ix, iy);
            RockTypes rt = RockTypes.safe_ord(block);
            if (rt != null)
            {
               byte rg = rock_growth.get(ix, iy);

               Main.batch.setColor(rt.color);

               if (rg < 15)
               {
                  if (rg < 0) rg = 0;
                  wall_growth_prog = rg / ((float) 14);
                  wall_growth_prog = Interpolation.smoother.apply(wall_growth_prog);
                  rg = (byte) MathUtils.round(MathUtils.map(0f, 1f, 0, 14, wall_growth_prog));

                  // this is a composition of the top texture and the sheet that has all versions of the side texture for the different growth states
                  Main.batch.draw(Res.SHEET_GROWING_ROCKBASE.sheet[rg], 16 * ix, 16 * iy);
                  if (rg > 0)
                  {
                     Main.batch.draw(Res.top_side_rockwall, 16 * ix, 16 * iy + (rg));
                  }
                  Main.batch.setColor(Color.WHITE);
               } else
               {
                  Main.batch.draw(Res.SHEET_BLOCKS.sheet[0], 16 * ix + x_fault_offset, 16 * iy + y_fault_offset);
                  Main.batch.setColor(Color.WHITE);
                  byte ord_dam = damage.get(ix, iy);
                  if (ord_dam > 0)
                  {
                     int mapped_break = MathUtils.round(MathUtils.map(0, Config.CONF.BLOCK_LIFE.value * rt.life_multiplier, 0, 4, ord_dam));
                     //int show_break_frame = MathUtils.clamp(mapped_break, 0, 4);
                     Main.batch.draw(Res.SHEET_BREAK.sheet[mapped_break], 16 * ix + x_fault_offset, 16 * iy + y_fault_offset);
                  }
               }
            }

            if (Main.debug_render)
            {
               //Text.cdraw(rock_growth.get(ix, iy) + "", ix * 16 + 8, iy * 16 + 8, Color.WHITE);
            }
         }

         last_sorted_index = render_sorted_objects(iy, last_sorted_index);
      }
   }

   public int render_sorted_objects(int y_line, int last_sorted_index)
   {
      // returning the last touched index in the sorted matrix so I can use that to continue looking for renders

      // no more objects left to render
      if (last_sorted_index >= sm_sort_objects.num_lines()) return sm_sort_objects.num_lines();

      for (int i = last_sorted_index; i < sm_sort_objects.num_lines(); i++)
      {
         int entry_y = sm_sort_objects.get(i, 0);

         if (entry_y == y_line)
         {
            render_sorted(sm_sort_objects.get(i, 1), sm_sort_objects.get(i, 2));
         } else
         {
            // since I expect the sm_sort_objects to be sorted here, either we hit the same y value or a bigger one or the list is over
            // there are no more objects to render on this line!
            last_sorted_index = i;
            break;
         }
      }
      return last_sorted_index;
   }

   public void render_sorted(int object_type, int object_index)
   {
      switch (object_type)
      {
         case 1: // MINER
         {
            MinerData.render_miner(sm_miners, object_index);
         }
         break;
         case 2: // PARTICLE
         {
            Particles.render_single_particle(sm_particles, object_index);
         }
         break;
      }
   }

   public void render_particles()
   {
      Particles.RENDER_PARTICLES(sm_particles);

      Main.batch.setColor(Color.WHITE);

      // crystals count as particles here in terms of rendering
      //for (int i = 0; i < sm_crystals.num_lines(); i++)
      //{
      //   if (sm_crystals.get(i, Crystal.TYPE.ordinal()) >= 0)
      //   {
      //      int cx = 16 * sm_crystals.get(i, Crystal.POSX.ordinal());
      //      int cy = 16 * sm_crystals.get(i, Crystal.POSY.ordinal());
      //      Main.batch.draw(Res.CRYSTAL.region, cx, cy + osc_crystals.value());
      //   }
      //}
   }

   public int spawn_particle(int tilex, int tiley, int type)
   {
      // tilex, tiley may be pixels as well as tiles and the particle will know
      int free_index = sm_particles.find_free_line_index();

      sm_particles.set_line(free_index, 0.f, type, tilex, tiley, 0, 0, 0, MathUtils.random(0, 359), 0);

      switch (type)
      {
         case Particles.TYPE_IMPACTOR:
            int from = tilex <= Arena.arena_width / 2 ? 1 : -1;
            sm_particles.set(free_index, Particles.POSX.ordinal(), tilex + Config.CONF.IMPACTOR_INITIAL_OFFSET.value * from);
            sm_particles.set(free_index, Particles.POSY.ordinal(), tiley + Config.CONF.IMPACTOR_INITIAL_OFFSET.value);
            sm_particles.set(free_index, Particles.TARGETX.ordinal(), tilex);
            sm_particles.set(free_index, Particles.TARGETY.ordinal(), tiley);
            break;
         case Particles.TYPE_ROCK_DEBRIS:
            sm_particles.set(free_index, Particles.VARIANT.ordinal(), MathUtils.random(0, 4));
            break;
         case Particles.TYPE_BRAIDED_RIVER:
            sm_particles.set(free_index, Particles.ANGLE.ordinal(), 0);

            // put entry into flow map, the check if spawn should happen must happen before this method!
            // this will just overwrite the value there
            // when a braided river particle dies, it must clear the entry in the flow map!
            braided_flow.set(tilex, tiley, (byte) 1);

            break;
         case Particles.TYPE_GRAVEL:
            //gravel.set(tilex, tiley, (byte) 1);
            break;
      }
      // maybe the caller wants to set variables that are only known from caller context
      return free_index;
   }

   public void impact_at(int tx, int ty)
   {
      Util.GEN_CIRCLE_POSITIONS(tx, ty, Config.CONF.IMPACTOR_BLOCK_DAMAGE_RADIUS.value);

      for (int i = 0; i < Util.CIRCLE_xpos.size; i++)
      {
         int mx = Util.CIRCLE_xpos.get(i);
         int my = Util.CIRCLE_ypos.get(i);

         hammer(mx, my, 10, true);
         hit_all_miners(mx, my, Config.CONF.IMPACTOR_PLAYER_DAMAGE.value);
         // this would theoretically also hit the owner of the impact if the miner managed to walk below the impact after triggering it
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
      if (sm_miners.get(miner_id, MinerData.ACTIVE.ordinal()) != 1) return;

      float time_blink = Util.INT_TO_FLOAT(sm_miners.get(miner_id, MinerData.BLINK.ordinal()));
      if (time_blink > 0f) return;

      int current_life = sm_miners.get(miner_id, MinerData.LIFE.ordinal());
      current_life -= damage;
      current_life = MathUtils.clamp(current_life, 0, Config.CONF.MINER_MAX_LIFE.value);
      sm_miners.set(miner_id, MinerData.LIFE.ordinal(), current_life);
      sm_miners.set(miner_id, MinerData.BLINK.ordinal(), Util.FLOAT_TO_INT(0.05f));
      // life display stays old value and will interpolate to current value

      int mx = sm_miners.get(miner_id, MinerData.TILEX.ordinal());
      int my = sm_miners.get(miner_id, MinerData.TILEY.ordinal());
      spawn_particle(mx, my, Particles.TYPE_BLOOD);

      if (current_life <= 0)
      {
         // deactivating or killing the miner
         sm_miners.set(miner_id, MinerData.ACTIVE.ordinal(), 0);

         int num_crystals = sm_miners.get_set(miner_id, MinerData.NUM_CRYSTALS.ordinal(), 0);

         int random_index;
         int cx, cy;
         Util.GEN_CIRCLE_POSITIONS(mx, my, 3);
         for (int i = 0; i < 50; i++)
         {
            if (num_crystals <= 0) break;

            // trying to place the crystals of the defeated miner in a circle
            random_index = MathUtils.random(0, Util.CIRCLE_xpos.size - 1);
            cx = Util.CIRCLE_xpos.get(random_index);
            cy = Util.CIRCLE_ypos.get(random_index);
            if (CHECK_free_tile(cx, cy))
            {
               Drops local_drop = Drops.safe_ord(drops.get(cx, cy));
               if (local_drop == null)
               {
                  drops.set(cx, cy, (byte) Drops.CRYSTAL.ordinal());
                  num_crystals--;
               }
            }
         }
      }
   }

   public void create_orogeny_block(int tx, int ty, byte initial_growth)
   {
      // this will have no effect if out of bounds of arena.
      boolean place_block = true;
      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1)
         {
            if (sm_miners.get(i, MinerData.TILEX.ordinal()) == tx && sm_miners.get(i, MinerData.TILEY.ordinal()) == ty)
            {
               deal_damage_to_miner(i, Config.CONF.OROGENY_HIT_DAMAGE.value);
               place_block = false;
               // traps the miner
            }
         }
      }
      if (place_block)
      {
         rock_walls.set(tx, ty, (byte) RockTypes.GABBRO.ordinal());
         rock_growth.set(tx, ty, (byte) initial_growth);
         damage.set(tx, ty, (byte) 0);
      }
   }

   public boolean init_transform_fault(int xy_faultline, boolean vertical)
   {
      if (!fault_ongoing)
      {
         fault_progress = 0f;
         fault_ongoing = true;
         fault_vertical = vertical;
         fault_xy = xy_faultline;
         return true;
      } else
      {
         System.out.println("ARENA: cannot trigger another fault right now!");
         return false;
      }
   }

   // xy is the offset where the fault line is
   // from view dir of the author the shear sense can be detected
   public void instant_transform_fault(int xy_faultline, int hori_vert)
   {
      // DISPLACE THE TILEMAP
      int offset = Config.CONF.TRANSFORM_FAULT_DISPLACEMENT.value;

      boolean vertical = hori_vert == 2;

      byte[] temp_row_board = new byte[vertical ? rock_walls.height : rock_walls.width];
      byte[] temp_row_damage = new byte[vertical ? rock_walls.height : rock_walls.width];
      // using a temp row eliminates the need for a data preserving look up direction

      int tilex_offset = 0;
      int tiley_offset = 0;

      for (int dim1 = 0; dim1 < (vertical ? rock_walls.width : rock_walls.height); dim1++)
      {
         for (int dim2 = 0; dim2 < (vertical ? rock_walls.height : rock_walls.width); dim2++)
         {
            if (vertical) // VERTICAL FAULT LINE, MOVING IN Y DIRECTION
            {
               // dim1 = x
               // dim2 = y

               //int x_offset = i + (offset * (iy > y_pos ? 1 : -1));
               tiley_offset = offset * (dim1 > xy_faultline ? 1 : -1);

               // need to subtract here since I do not move but look up in other direction to move
               byte new_board_val = rock_walls.get_boundchecked(dim1, dim2 - tiley_offset);
               byte new_damage_val = damage.get_boundchecked(dim1, dim2 - tiley_offset);
               temp_row_board[dim2] = new_board_val == -1 ? (byte) -1 : new_board_val;
               temp_row_damage[dim2] = new_damage_val == -1 ? (byte) -1 : new_damage_val;

            } else
            { // HORIZONTAL FAULT LINE, MOVING IN X DIRECTION
               // dim1 = y
               // dim2 = x

               tilex_offset = offset * (dim1 > xy_faultline ? -1 : 1);

               // need to subtract here since I do not move but look up in other direction to move
               byte new_board_val = rock_walls.get_boundchecked(dim2 - tilex_offset, dim1);
               byte new_damage_val = damage.get_boundchecked(dim2 - tilex_offset, dim1);
               temp_row_board[dim2] = new_board_val == -1 ? (byte) -1 : new_board_val;
               temp_row_damage[dim2] = new_damage_val == -1 ? (byte) -1 : new_damage_val;

            }
         }
         for (int dim2 = 0; dim2 < (vertical ? rock_walls.height : rock_walls.width); dim2++)
         {
            if (vertical)
            {
               rock_walls.set(dim1, dim2, temp_row_board[dim2]);
               damage.set(dim1, dim2, temp_row_damage[dim2]);
            } else
            {
               rock_walls.set(dim2, dim1, temp_row_board[dim2]);
               damage.set(dim2, dim1, temp_row_damage[dim2]);
            }
         }
      }

      // FRICTIONAL MELT AT FAULT LINE
      if (vertical)
      {
         for (int dim1 = 0; dim1 < rock_walls.height; dim1++)
         {
            if (rock_walls.get(xy_faultline, dim1) >= 0)
            {
               melt.set(xy_faultline, dim1, (byte) 50);
               rock_walls.set(xy_faultline, dim1, (byte) -1);
            }
            if (rock_walls.get(xy_faultline + 1, dim1) >= 0)
            {
               melt.set(xy_faultline + 1, dim1, (byte) 50);
               rock_walls.set(xy_faultline + 1, dim1, (byte) -1);
            }
         }
      } else
      {
         // HORIZONTAL
         for (int dim1 = 0; dim1 < rock_walls.width; dim1++)
         {
            if (rock_walls.get(dim1, xy_faultline) >= 0)
            {
               melt.set(dim1, xy_faultline, (byte) 50);
               rock_walls.set(dim1, xy_faultline, (byte) -1);
            }
            if (rock_walls.get(dim1, xy_faultline + 1) >= 0)
            {
               melt.set(dim1, xy_faultline + 1, (byte) 50);
               rock_walls.set(dim1, xy_faultline + 1, (byte) -1);
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
            if (new_ypos < 0 || new_ypos >= rock_walls.height)
            {
               deal_damage_to_miner(miid, 50);
               new_ypos = MathUtils.clamp(new_ypos, 0, rock_walls.height - 1);
            }
            sm_miners.set(miid, MinerData.TILEY.ordinal(), new_ypos);
         } else
         {
            int x_offset = offset * (sm_miners.get(miid, MinerData.TILEY.ordinal()) > xy_faultline ? -1 : 1);
            int new_xpos = sm_miners.get(miid, MinerData.TILEX.ordinal()) + x_offset;
            if (new_xpos < 0 || new_xpos >= rock_walls.width)
            {
               deal_damage_to_miner(miid, 50);
               new_xpos = MathUtils.clamp(new_xpos, 0, rock_walls.width - 1);
            }
            sm_miners.set(miid, MinerData.TILEX.ordinal(), new_xpos);
         }
      }

      // DISPLACE PARTICLES (THAT NEED TO BE DISPLACED)
      for (int i = 0; i < sm_particles.num_lines(); i++)
      {
         // here I assume all particles that should be displaced have their position
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

      // TODO: 29.03.23 effect layers are not yet displaced! (melt is!)
   }

   public void hammer(int tx, int ty, int dealt_damage, boolean destroy_effect_maps)
   {
      // the flag DESTROY_EFFECT_MAPS will distinguish between skills that do damage to the arena and simple hit
      // since simple hits should remove gravel and ice while skills may not in most cases
      // impact for example will destroy effect maps but glacier wont

      // this technically could hit the attacker but he is not on that tile!
      // but now that the attack animation plays, the player cannot move and will not be on the tile the frame

      if (Main.debug_render)
      {
         rock_growth.set(tx, ty, (byte) 0);
      }

      byte block_wall = rock_walls.get(tx, ty);
      RockTypes rt = RockTypes.safe_ord(block_wall);
      if (rt == null)
      {
         // System.out.println("no block at pos " + tx + " | " + ty);
      } else
      {
         byte current_damage = damage.get(tx, ty);

         for (int i = 0; i < MathUtils.random(3, 6); i++)
         {
            // TODO: 06.04.23 replace this with better hit particles
            spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
         }

         current_damage += dealt_damage;

         if (current_damage >= Config.CONF.BLOCK_LIFE.value * rt.life_multiplier)
         {
            // break stone
            rock_walls.set(tx, ty, (byte) -1);
            for (int i = 0; i < MathUtils.random(6, 12); i++)
            {
               spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
            }
            current_damage = 0;

            if (MathUtils.random(100) < rt.crystal_chance())
            {
               if (drops.get(tx, ty) == -1)
               {
                  Drops drop = Drops.spawn_random();
                  drops.set(tx, ty, (byte) drop.ordinal());
               }
               //int index_crystal_free = sm_crystals.find_free_line_index();
               //sm_crystals.set(index_crystal_free, Crystal.TYPE.ordinal(), 0);
               //sm_crystals.set(index_crystal_free, Crystal.POSX.ordinal(), tx);
               //sm_crystals.set(index_crystal_free, Crystal.POSY.ordinal(), ty);
            }
         }
         damage.set(tx, ty, current_damage);
      }

      if (destroy_effect_maps)
      {
         // HAMMERING GRAVEL TO DUST
         if (gravel.get(tx, ty) > 0)
         {
            gravel.incr(tx, ty, (byte) -1);
            for (int i = 0; i < MathUtils.random(4, 8); i++)
            {
               spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
            }
         }
         if (ice.get(tx, ty) == 1)
         {
            ice.set(tx, ty, (byte) 0);
            for (int i = 0; i < MathUtils.random(4, 8); i++)
            {
               // TODO: 23.04.23 ICE PARTICLES!
               spawn_particle(tx, ty, Particles.TYPE_ROCK_DEBRIS);
            }
         }
      }
   }


   boolean CHECK_free_tile(int tx, int ty)
   {
      byte tile_floor = floor.get(tx, ty);
      byte block_wall = rock_walls.get(tx, ty);
      return block_wall < 0;
   }

   public boolean CHECK_bounds(int tx, int ty)
   {
      // returns true if inside of map bounds
      return tx >= 0 && tx < rock_walls.width && ty >= 0 && ty < rock_walls.height;
   }

   public void DEBUG_displace_miner(int miner_id, int tx, int ty)
   {
      if (!CHECK_bounds(tx, ty)) return;
      sm_miners.set(miner_id, MinerData.TILEX.ordinal(), tx);
      sm_miners.set(miner_id, MinerData.TILEY.ordinal(), ty);
      rock_walls.set(tx, ty, (byte) -1);
      damage.set(tx, ty, (byte) 0);
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

   public int create_miner(Miner.MinerClass mc)
   {
      // TODO: 12.02.23 I guess at some point when changing the number of players during rounds, miners are being removed from the list so empty entries will exist that need to be taken care of

      // free miner index
      int fmi = sm_miners.find_free_line_index();

      //  using fmi for now as miner index into spawn position, technically this should be fine
      //  even after a round the slots are reused so it should always be in the interval 0...3
      int[] spawn_pos = Util.get_spawn(fmi);

      sm_miners.set(fmi, MinerData.TILEX.ordinal(), spawn_pos[0]);
      sm_miners.set(fmi, MinerData.TILEY.ordinal(), spawn_pos[1]);

      sm_miners.set(fmi, MinerData.NUM_CRYSTALS.ordinal(), 0);
      sm_miners.set(fmi, MinerData.FUTURE_CRYSTALS.ordinal(), 0);
      sm_miners.set(fmi, MinerData.VIEWDIR.ordinal(), 0);
      sm_miners.set(fmi, MinerData.ACTIVE.ordinal(), 1);

      sm_miners.set(fmi, MinerData.LIFE.ordinal(), Config.CONF.MINER_MAX_LIFE.value);
      sm_miners.set(fmi, MinerData.LIFE_DISPLAY.ordinal(), Config.CONF.MINER_MAX_LIFE.value);
      sm_miners.set(fmi, MinerData.CLASS.ordinal(), mc.ordinal());

      sm_miners.set(fmi, MinerData.ANIM.ordinal(), MinerAnim.IDLE.ordinal());

      // HERE ALL THE INITIAL VALUES HAVE TO BE SET
      return fmi;
   }

   public int number_alive_miners()
   {
      int number = 0;
      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         if (sm_miners.get(i, MinerData.ACTIVE.ordinal()) == 1) number++;
      }
      return number;
   }

   public void prepare()
   {
      // TODO: 20.04.23 game will crash in second round but I need to this if this is still true!
      seed = MathUtils.random(0, Integer.MAX_VALUE - 100);
      OpenSimplexNoise noise_board = new OpenSimplexNoise(seed, 2);
      OpenSimplexNoise noise_rocktype = new OpenSimplexNoise(seed + 1, 4);

      for (Flatbyte fb : lists_to_reset) fb.reset();

      // sets up the arena for playing a round
      // clear all miners (will be added from character selection menu)
      // remove all particles and objects
      // generate the map
      // reset map damage
      num_total_rocks = 0;

      int mid_x = rock_walls.width / 2;
      int mid_y = rock_walls.height / 2;
      for (int ix = 0; ix < rock_walls.width; ix++)
      {
         for (int iy = 0; iy < rock_walls.height; iy++)
         {
            rock_walls.set(ix, iy, (byte) -1);

            int dst_to_mid = Util.simple_dist(ix, iy, mid_x, mid_y);

            float val = (float) ((noise_board.noise(ix, iy) + 1) / 2f);

            if (dst_to_mid < 400)
            {
               if (val <= 0.65f)
               {
                  float spawn_noise = (float) (noise_rocktype.noise(ix, iy) + 1) / 2f;
                  RockTypes rt = RockTypes.generate_from_noise(spawn_noise);
                  rock_walls.set(ix, iy, (byte) rt.ordinal());
                  num_total_rocks++;
               }
            }
         }
      }
      System.out.println("[ARENA] should have spawned " + num_total_rocks + " rocks");

      // TODO: 18.03.23 reusing lines are in wrong order due to pop
      sm_miners.clear_all_lines();
   }

   public void render_HUD()
   {
      if (Main.debug_render)
      {
         int running_debug_offset = -10;
         Text.draw("num particles " + sm_particles.num_lines(), 3, Main.upper_y_bound() + running_debug_offset);
         running_debug_offset -= 10;
         Text.draw("total melt sum " + Arena.total_melt_sum, 3, Main.upper_y_bound() + running_debug_offset);
         running_debug_offset -= 10;
         Text.draw("ongoing wall count " + Arena.ongoing_rockwall_count, 3, Main.upper_y_bound() + running_debug_offset);
         running_debug_offset -= 10;
         Text.draw("count index " + Arena.current_check_index, 3, Main.upper_y_bound() + running_debug_offset);
         running_debug_offset -= 10;
         Text.draw("last rockfill percent " + Arena.last_rockfill_percent, 3, Main.upper_y_bound() + running_debug_offset);
         running_debug_offset -= 10;

         if(sm_miners.num_lines() > 0){
            Text.draw("miner 1 num crystals " + sm_miners.get(0, MinerData.NUM_CRYSTALS.ordinal()), 3, Main.upper_y_bound() + running_debug_offset);
            running_debug_offset -= 10;
            Text.draw("miner 1 num future crystals " + sm_miners.get(0, MinerData.FUTURE_CRYSTALS.ordinal()), 3, Main.upper_y_bound() + running_debug_offset);
            running_debug_offset -= 10;
         }
      }

      int box_x = Main.window_width / 2 - Config.CONF.UI_GAME_TIME_BOX_WIDTH.value / 2;
      int box_y = Main.window_height - Config.CONF.UI_GAME_TIME_BOX_HEIGHT.value;

      RenderUtil.render_box(box_x, box_y, Config.CONF.UI_GAME_TIME_BOX_WIDTH.value, Config.CONF.UI_GAME_TIME_BOX_HEIGHT.value, RenderUtil.color_trans_gray);
      Text.cdraw(RenderUtil.time_to_display(Game.game_time), box_x + Config.CONF.UI_GAME_TIME_TEXT_OFFSET_X.value, box_y + Config.CONF.UI_GAME_TIME_TEXT_OFFSET_Y.value, Color.WHITE, 2f);

      // HUD is abstracted and rendered offset for every miner
      for (int i = 0; i < sm_miners.num_lines(); i++)
      {
         MinerData.render_miner_HUD(sm_miners, i);
      }
   }
}