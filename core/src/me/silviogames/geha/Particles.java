package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntIntMap;

public enum Particles
{
   TYPE,
   POSX,
   POSY,
   TARGETX,
   TARGETY,
   LIFE,
   ANGLE,
   VARIANT,

   DATA1,
   ;

   // TYPES:

   public static final int TYPE_ROCK_DEBRIS = 1;
   public static final int TYPE_IMPACTOR = 2;
   // VARIANT 1 = coming from left
   // VARIANT 2 = coming from right

   public static final int TYPE_IMPACTOR_SMOKE = 3;
   public static final int TYPE_IMPACTOR_SPARK = 4;

   public static final int TYPE_OROGEN = 10;

   public static final int TYPE_TRANSFORM_FAULT = 20;
   // POSX = HORIZONTAL [1], VERTICAL[2]
   // POSY = FAULT_LINE_XY

   public static final int TYPE_BRAIDED_RIVER = 30;
   // the variable called angle is used as view_dir, which is the global flow direction

   public static final int TYPE_GRAVEL = 40;

   public static final int TYPE_ICEBLOCK = 50;
   // target_x and target_y are float encoded offsets to animate the movement of the iceblock
   // angle is view_dir
   // life is damage counter

   // this particle is not visible
   public static final int TYPE_EROSION = 60;
   // angle is view_dir
   // life is damage counter
   // data1 is time as float

   public static final int TYPE_LANDSLIDE = 70;
   // angle is viewdir
   // life is count_spawn
   // data1 is anim time as float

   public static final int TYPE_RAYLEIGH = 71;
   // angle is viewdir
   // data1 is anim time as float

   public static final int TYPE_MOVING_ROCK = 72;
   // from and target pos in tiles
   // data1 = type of rock

   // this particle starts to fly straight until it finds another miner, which it follows and does the effect at their location
   public static final int TYPE_MAGNET = 73;
   // angle is viewdir
   // data1 is target id (-1 in the beginning)
   // variant is id of owner to prevent self harm

   public static final int TYPE_BLOOD = 100;
   public static final int TYPE_OBTAIN_CRYSTAL = 101;

   public static final int TYPE_TSCHERMAK_MERGER_CRYSTALS = 150;
   // VARIANT is ordinal of Drops
   // position is stored as tile with additional float encoded lerp value to interpolate to the target
   // center position of the big merged crystal
   // LIFE is the merging lerp variable

   public static final int TYPE_TSCHERMAK_MERGED = 151;
   // position is stored as pixel for interpolation
   // ANGLE is used for ID of casting miner to not target them!
   // VARIANT IS NUM OF MERGED SMALL CRYSTALS
   // DATA1 is state, 0 = merging, 1 = targeting enemy

   public static final int TYPE_TEST = 2000;

   public static final IntIntMap map_position_as_tiles = new IntIntMap();
   // TYPE_IDS of particles point to
   // -1 or 0 -> position is in pixels (DEFAULT)
   // 1 -> position is in tiles

   public static final IntIntMap map_faults = new IntIntMap();
   // TYPE_IDS of particles point to
   // -1 -> is NOT affected by a fault displacement
   // 1 -> IS affected by a fault displacement

   // put type with a 1 in here to mark them to be sorted in the grid,
   // all other particles will be rendered on top of the map.
   public static final IntIntMap sort_particle = new IntIntMap();

   // every 0.1 tick all braided particles try to damage immersed miners
   static Timer timer_braided_damage = new Timer(0.1f);

   static int[] alluvial_spawn_candidates_x = new int[3];
   static int[] alluvial_spawn_candidates_y = new int[3];

   static Osc osc_todeszone = new Osc(1f, 13f, 13f);

   static
   {
      map_position_as_tiles.put(TYPE_OROGEN, 1);
      map_position_as_tiles.put(TYPE_IMPACTOR, 1);
      map_position_as_tiles.put(TYPE_BRAIDED_RIVER, 1);
      map_position_as_tiles.put(TYPE_ICEBLOCK, 1);
      map_position_as_tiles.put(TYPE_TEST, 1);
      map_position_as_tiles.put(TYPE_TSCHERMAK_MERGER_CRYSTALS, 1);
      map_position_as_tiles.put(TYPE_BLOOD, 1);

      // it looks like it is not but it internally does a pixel displacement during render
      map_position_as_tiles.put(TYPE_ROCK_DEBRIS, 1);

      // blood particle only having a tile based position might be weird but mostly not visible

      map_faults.put(TYPE_OROGEN, 1);
      map_faults.put(TYPE_ICEBLOCK, 1);

      map_position_as_tiles.put(TYPE_BRAIDED_RIVER, 1);

      // NOT SURE IF BLOOD IS SUPPOSED TO BE MOVED BUT THIS IS ONLY VISUAL SO ANYWAYS
      map_faults.put(TYPE_BLOOD, 1);

      sort_particle.put(TYPE_ICEBLOCK, 1);
      sort_particle.put(TYPE_BRAIDED_RIVER, 1);
      sort_particle.put(TYPE_BLOOD, 1);
   }

   public static void render_single_particle(Smartrix sm_particles, int pid)
   {
      // this single render function is needed to be called from the outside for a single sorted particles,
      // all other non sorted particles are called from the loop in the RENDER_PARTICLES function the same way

      int[] data_particle = new int[values().length];
      sm_particles.get_line(pid, data_particle);

      switch (data_particle[TYPE.ordinal()])
      {
         case TYPE_MAGNET:
         {
            int mhw = Res.MAGNET.region.getRegionWidth() / 2;

            if (data_particle[DATA1.ordinal()] == -1)
            {
               int px = data_particle[POSX.ordinal()];
               int py = data_particle[POSY.ordinal()];
               float offset = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               int viewdir = data_particle[ANGLE.ordinal()];
               Main.batch.draw(Res.MAGNET.region, px * 16 + Util.fourdirx[viewdir] * offset * 16 - mhw, py * 16 + Util.fourdiry[viewdir] * offset * 16 - mhw);
            } else
            {
               float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float interp = Interpolation.linear.apply(life);
               //float sin_val = MathUtils.map(0f, 1f, 0f, MathUtils.PI, interp);
               float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
               float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
               Main.batch.draw(Res.MAGNET.region, rx * 16 - mhw, ry * 16 - mhw);
            }
         }
         break;
         case TYPE_MOVING_ROCK:
         {
            float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
            float interp = Interpolation.smoother.apply(life);
            float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
            float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
            RockTypes rt = RockTypes.safe_ord(data_particle[DATA1.ordinal()]);
            Main.batch.setColor(rt.color);
            //Main.batch.draw(Res.SHEET_BLOCKS.sheet[0], 16 * rx, 16 * ry);

            float wall_growth_prog = 4 / ((float) 14);
            wall_growth_prog = Interpolation.smoother.apply(wall_growth_prog);
            byte rg = (byte) MathUtils.round(MathUtils.map(0f, 1f, 0, 14, wall_growth_prog));

            // this is a composition of the top texture and the sheet that has all versions of the side texture for the different growth states
            Main.batch.draw(Res.SHEET_GROWING_ROCKBASE.sheet[rg], 16 * rx, 16 * ry);
            if (rg > 0)
            {
               Main.batch.draw(Res.top_side_rockwall, 16 * rx, 16 * ry + (rg));
            }
            Main.batch.setColor(Color.WHITE);
         }
         break;
         case TYPE_OBTAIN_CRYSTAL:
         {
            float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
            float interp = Interpolation.circle.apply(life);
            float sin_val = MathUtils.map(0f, 1f, 0f, MathUtils.PI, interp);
            float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
            float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp) + (MathUtils.sin(sin_val) * 64);
            Main.batch.draw(Res.CRYSTAL.region, rx, ry, 10, 10);
         }
         break;
         case TYPE_LANDSLIDE:
         case TYPE_EROSION:
         case TYPE_RAYLEIGH:
         {
            int posx = 16 * data_particle[POSX.ordinal()];
            int posy = 16 * data_particle[POSY.ordinal()];
            RenderUtil.render_box(posx, posy, 16, 16, RenderUtil.IMPACT_WARNING_COLOR);
         }
         break;
         case TYPE_TSCHERMAK_MERGED:
         {
            if (data_particle[DATA1.ordinal()] == 0)
            {
               // MERGING PHASE; CRYSTAL GROWS
               int posx = 16 * data_particle[POSX.ordinal()];
               int posy = 16 * data_particle[POSY.ordinal()];
               Main.batch.setColor(Color.WHITE);

               float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               int num_merged = data_particle[VARIANT.ordinal()];
               float max_size = 1 + (num_merged / ((float) Config.CONF.TSCHERMAK_MERGE_VALUE.value));
               float size = MathUtils.lerp(1f, max_size, life);
               Main.batch.draw(Res.CRYSTAL.region, posx - (8 * (size - 1)), posy, 16 * size, 16 * size);
            } else if (data_particle[DATA1.ordinal()] == 1)
            {
               // TARGETING PHASE
               int posx = 16 * data_particle[POSX.ordinal()];
               int posy = 16 * data_particle[POSY.ordinal()];
               int tox = 16 * data_particle[TARGETX.ordinal()];
               int toy = 16 * data_particle[TARGETY.ordinal()];
               float px, py;

               float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float sin_val = MathUtils.map(0f, 1f, 0f, MathUtils.PI, life);
               life = Interpolation.circleIn.apply(life);
               px = MathUtils.lerp(posx, tox, life);
               py = MathUtils.lerp(posy, toy, life) + (MathUtils.sin(sin_val) * 32);


               Main.batch.setColor(Color.WHITE);
               int num_merged = data_particle[VARIANT.ordinal()];
               float max_size = 1 + (num_merged / ((float) Config.CONF.TSCHERMAK_MERGE_VALUE.value));
               Main.batch.draw(Res.CRYSTAL.region, px - (8 * (max_size - 1f)), py, 16 * max_size, 16 * max_size);
            }
         }
         break;
         case TYPE_TSCHERMAK_MERGER_CRYSTALS:
         {
            Drops drop = Drops.safe_ord(data_particle[VARIANT.ordinal()]);
            if (drop != null)
            {
               Main.batch.setColor(drop.color);

               int posx = 16 * data_particle[POSX.ordinal()];
               int posy = 16 * data_particle[POSY.ordinal()];
               int tox = 16 * data_particle[TARGETX.ordinal()];
               int toy = 16 * data_particle[TARGETY.ordinal()];
               float px, py;

               float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               life = Interpolation.exp10Out.apply(life);
               px = MathUtils.lerp(posx, tox, life);
               py = MathUtils.lerp(posy, toy, life);
               // TODO: 20.05.23 not sure if the offsets are correct here! but since crystals are 16x16 pixels it should be fine

               Main.batch.setColor(drop.color.r, drop.color.g, drop.color.b, 1 - life);
               Main.batch.draw(Res.CRYSTAL.region, px, py);
            }
         }
         break;
         case TYPE_TEST:
         {
            int px = 16 * data_particle[POSX.ordinal()];
            int py = 16 * data_particle[POSY.ordinal()];
            RenderUtil.render_box(px, py, 16, 16, Color.PINK);
         }
         break;
         case 1: // ROCK DEBRIS
         {
            int angle = data_particle[ANGLE.ordinal()];
            float dx = MathUtils.cosDeg(angle);
            float dy = MathUtils.sinDeg(angle);
            float rx = 16 * data_particle[POSX.ordinal()] + (data_particle[LIFE.ordinal()] / 5f) * dx;
            float ry = 16 * data_particle[POSY.ordinal()] + (data_particle[LIFE.ordinal()] / 5f) * dy;
            Main.batch.setColor(1f, 1f, 1f, (255 - data_particle[LIFE.ordinal()]) / 255f);
            Main.batch.draw(Res.PARTICLES_ROCK.sheet[data_particle[VARIANT.ordinal()]], rx, ry);
            Main.batch.setColor(Color.WHITE);
         }
         break;
         case TYPE_IMPACTOR:
         {
            float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
            float interp = Interpolation.pow2In.apply(life);
            Main.batch.setColor(1f, 1f, 1f, 0.8f + MathUtils.lerp(0, 0.2f, interp));
            float rx = MathUtils.lerp(data_particle[POSX.ordinal()], data_particle[TARGETX.ordinal()], interp);
            float ry = MathUtils.lerp(data_particle[POSY.ordinal()], data_particle[TARGETY.ordinal()], interp);
            Main.batch.draw(Res.IMPACTOR.region, 16 * rx, 16 * ry);

            if (Config.CONF.IMPACTOR_DISPLAY_TEXT.value == 1)
            {
               int text_x = data_particle[TARGETX.ordinal()] * 16 + 8;
               int text_y = data_particle[TARGETY.ordinal()] * 16 + 8;

               Text.cdraw("TODESZONE!", text_x, text_y, Color.SCARLET, 1.4f);
            }
            if (life < 0.2f)
            {
               int tx = data_particle[TARGETX.ordinal()];
               int ty = data_particle[TARGETY.ordinal()];

               Util.GEN_CIRCLE_POSITIONS(tx, ty, Config.CONF.IMPACTOR_BLOCK_DAMAGE_RADIUS.value);

               for (int j = 0; j < Util.CIRCLE_xpos.size; j++)
               {
                  int mx = Util.CIRCLE_xpos.get(j);
                  int my = Util.CIRCLE_ypos.get(j);

                  RenderUtil.render_box(mx * 16, my * 16, 16, 16, RenderUtil.IMPACT_WARNING_COLOR);
               }
            }
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
            // animation of the orogen growth is done via the rock_growth effect map in arena
         }
         break;
         case TYPE_BLOOD:
         {
            int px = data_particle[POSX.ordinal()];
            int py = data_particle[POSY.ordinal()];
            int ox = Config.CONF.MINER_BLOOD_PARTICLE_OFFSET_X.value;
            int oy = Config.CONF.MINER_BLOOD_PARTICLE_OFFSET_Y.value;
            float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
            //float time_max = Util.INT_TO_FLOAT(Config.CONF.MINER_BLOOD_TIME_MS.value);

            Main.batch.draw(Res.get_frame(time, Anim.PARTICLE_BLOOD), px * 16 + ox, py * 16 + oy);
         }
         break;
         case TYPE_BRAIDED_RIVER:
         {
            int px = data_particle[POSX.ordinal()] * 16;
            int py = data_particle[POSY.ordinal()] * 16;
            Main.batch.setColor(Color.SKY);
            Main.batch.draw(Res.pixel, px, py, 16, 16);
            Main.batch.setColor(Color.WHITE);
         }
         break;
         case TYPE_GRAVEL:
         {
            // MOVE THE RENDERING OF GRAVEL TO ARENA!
            //  since gravel can be hammered I need to remove the entries but I do not want to
            //  search all particles for the right gravel entry so the gravel particle is only logical, the visual effect happens in the arena render method!

            //int px = data_particle[POSX.ordinal()] * 16;
            //int py = data_particle[POSY.ordinal()] * 16;
            //Main.batch.draw(Res.TILE_GRAVEL.region, px, py);
         }
         break;
         case TYPE_ICEBLOCK:
         {
            int px = data_particle[POSX.ordinal()] * 16;
            int py = data_particle[POSY.ordinal()] * 16;
            float offset = Util.INT_TO_FLOAT(data_particle[TARGETX.ordinal()]);
            int viewdir = data_particle[ANGLE.ordinal()];
            Main.batch.setColor(Color.WHITE);
            Main.batch.draw(Res.pixel, px + Util.fourdirx[viewdir] * offset, py + Util.fourdiry[viewdir] * offset, 16, 24);
            Main.batch.setColor(Color.WHITE);
         }
         break;
      }
   }

   public static void RENDER_PARTICLES(Smartrix sm_particles)
   {
      for (int i = 0; i < sm_particles.num_lines(); i++)
      {
         if (sm_particles.get(i, LIFE.ordinal()) < 0 || sort_particle.get(sm_particles.get(i, TYPE.ordinal()), -1) == 1)
         {
            // skipping particles that should be rendered sorted in the grid;
            continue;
         }

         render_single_particle(sm_particles, i);
      }
   }

   public static void UPDATE_PARTICLES(Arena arena, Smartrix sm_particles, float delta)
   {
      // IT SHOULD BE POSSIBLE TO CREATE NEW PARTICLES DURING THE LOOP SINCE EITHER A FREE SPOT IS FOUND OR THE ARRAY IN ENLARGED WHICH IS FINE SINCE LOOP WILL NOT CONSIDER THE NEW ENTRIES

      boolean braided_damage_frame = timer_braided_damage.update(delta);

      osc_todeszone.update(delta);
      RenderUtil.IMPACT_WARNING_COLOR.a = osc_todeszone.value() * 0.25f;

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
            case TYPE_OBTAIN_CRYSTAL:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               time = MathUtils.clamp(time + delta / Util.INT_TO_FLOAT(Config.CONF.CRYSTAL_OBTAIN_SPEED_MS.value), 0, 1);
               if (time >= 1)
               {
                  dead = true;
                  arena.sm_miners.incr(data_particle[DATA1.ordinal()], MinerData.NUM_CRYSTALS.ordinal(), 1);
                  arena.sm_miners.incr(data_particle[DATA1.ordinal()], MinerData.FUTURE_CRYSTALS.ordinal(), -1);
               }
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
            }
            break;
            case TYPE_TSCHERMAK_MERGED:
            {
               if (data_particle[DATA1.ordinal()] == 0)
               {
                  // MERGING PHASE
                  float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
                  float life_time = Util.INT_TO_FLOAT(Config.CONF.TSCHERMAK_MERGE_TIME_MS.value);
                  life += delta / life_time;
                  if (life >= 1f)
                  {
                     life = 0;
                     // SWITCH TO TARGET PHASE
                     data_particle[DATA1.ordinal()] = 1;

                     // I now once select a target and set the target_x target_y variables
                     // the particle will then aim for that tile and if there is somebody there they will receive damage!

                     int random_target_miner_id = arena.FIND_tschermak_target(data_particle[ANGLE.ordinal()]);

                     if (random_target_miner_id == -1)
                     {
                        // no target found (must be single miner game)
                        // shooting to upper right corner to make the skill visible for testing
                        data_particle[TARGETX.ordinal()] = Arena.arena_width - 1;
                        data_particle[TARGETY.ordinal()] = Arena.arena_height - 1;
                     } else
                     {
                        int mx = arena.sm_miners.get(random_target_miner_id, MinerData.TILEX.ordinal());
                        int my = arena.sm_miners.get(random_target_miner_id, MinerData.TILEY.ordinal());
                        data_particle[TARGETX.ordinal()] = mx;
                        data_particle[TARGETY.ordinal()] = my;
                     }
                  }
                  data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(life);
               } else if (data_particle[DATA1.ordinal()] == 1)
               {
                  float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
                  float life_time = Util.INT_TO_FLOAT(Config.CONF.TSCHERMAK_TARGET_TIME_MS.value);
                  life += delta / life_time;

                  if (life >= 1f)
                  {
                     life = 1f;

                     int num_merged = data_particle[VARIANT.ordinal()];
                     int radius = 1 + MathUtils.floor(num_merged / ((float) Config.CONF.TSCHERMAK_MERGE_VALUE.value));

                     Util.GEN_CIRCLE_POSITIONS(data_particle[TARGETX.ordinal()], data_particle[TARGETY.ordinal()], radius);

                     for (int j = 0; j < Util.CIRCLE_xpos.size; j++)
                     {
                        int cx = Util.CIRCLE_xpos.get(j);
                        int cy = Util.CIRCLE_ypos.get(j);

                        arena.hit_all_miners(cx, cy, Config.CONF.TSCHERMAK_DAMAGE.value * radius);
                     }

                     // the following line is probably not needed since dead == true
                     data_particle[DATA1.ordinal()] = 2;
                     dead = true;
                  }
                  data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(life);
               }
            }
            break;
            case TYPE_TSCHERMAK_MERGER_CRYSTALS:
            {
               float life = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float life_time = Util.INT_TO_FLOAT(Config.CONF.TSCHERMAK_MERGE_TIME_MS.value);
               life += delta / life_time;
               if (life >= 1f)
               {
                  dead = true;
               }
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(life);
            }
            break;
            case TYPE_TEST:
               data_particle[LIFE.ordinal()]++;
               if (data_particle[LIFE.ordinal()] > 60)
               {
                  dead = true;
               }
               break;
            case 1: // ROCK DEBRIS
            {
               data_particle[LIFE.ordinal()] += 3;
               if (data_particle[LIFE.ordinal()] > 255)
               {
                  dead = true;
               }
            }
            break;

            case 2: // IMPACTOR
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float life_time = Util.INT_TO_FLOAT(Config.CONF.IMPACTOR_LIFETIME.value);
               time = MathUtils.clamp(time + delta / life_time, 0, 1);
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
                  // the block spawning happens directly when the skill is performed, this
                  // particle just spawns the debris particles
                  //int tx = data_particle[POSX.ordinal()];
                  //int ty = data_particle[POSY.ordinal()];
                  //arena.create_orogeny_block(tx, ty);
               } else
               {
                  // SPAWN SMOKE PARTICLES
                  int tx = data_particle[POSX.ordinal()];
                  int ty = data_particle[POSY.ordinal()];

                  if (MathUtils.randomBoolean(0.05f))
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

                  // the instant function is now called by static code
                  //arena.instant_transform_fault(xy_offset, hori_vert);
               } else
               {
                  // SPAWN SMOKE PARTICLES
                  int hori_vert = data_particle[POSX.ordinal()];
                  int xy_offset = data_particle[POSY.ordinal()];

                  for (int xy = 0; xy < arena.rock_walls.width; xy++)
                  {
                     if (MathUtils.randomBoolean(0.2f))
                     {
                        if (hori_vert == 1)
                        { // HORIZONTAL
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
            case TYPE_BLOOD:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float[] ret = Anim.PARTICLE_BLOOD.update(time, delta);
               if (ret[2] == 1) dead = true;
               time = ret[0];
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
            }
            break;
            case TYPE_BRAIDED_RIVER:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               // changing the time here will change the look of the flow, if older ones
               //  stay longer or vanish quicker
               time += delta;

               if (time >= Util.INT_TO_FLOAT(Config.CONF.BRAIDED_PARTICLE_LIFETIME_MS.value))
               {
                  dead = true;
                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];
                  // clearing the flow value
                  arena.braided_flow.set(px, py, (byte) 0);
               } else if (braided_damage_frame)
               {
                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];
                  //arena.hit_all_miners(px, py, 50);

                  arena.melt.set(px, py, (byte) 0);
               }
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
               if (!dead && data_particle[VARIANT.ordinal()] == 0 && time >= Util.INT_TO_FLOAT(Config.CONF.BRAIDED_PARTICLE_CHILD_DELAY_MS.value))
               {
                  // this braided river has not yet spawned a child
                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];
                  // using angle as view dir!
                  int flow_dir = data_particle[ANGLE.ordinal()];

                  int nx = px + Util.fourdirx[flow_dir];
                  int ny = py + Util.fourdiry[flow_dir];

                  // first lets check if child would be in the arena and if so spawn
                  if (arena.CHECK_bounds(nx, ny) && arena.braided_flow.get(nx, ny) == 0)
                  {
                     // now lets see if the next position is blocked by a rock and if so braid the river
                     // subtracting the chance to random braid from the 100 chance to flow if path is free!
                     float braiding_chance = 100 - (Config.CONF.BRAIDED_RANDOM_BRAIDING_BOOL.value == 1 ? Config.CONF.BRAIDED_RANDOM_BRAIDING_CHANCE_PERCENT.value : 0);
                     // TODO: 27.03.23 I could make the look of the free path braiding more predictable if I tied the chance to the modulo of the distance to origin but only in flow direction, then it will braid at fixed intervals

                     // TODO: 31.05.23 I should not try to braid if flowing in a 1 tile big channel, because the braid will fail and the river will stop since the middle tile stops at a braid

                     if (arena.CHECK_free_tile(nx, ny) && MathUtils.randomBoolean(braiding_chance / 100f))
                     {
                        // tile is free, just braid
                        int particle_id = arena.spawn_particle(nx, ny, TYPE_BRAIDED_RIVER);
                        arena.sm_particles.set(particle_id, Particles.ANGLE.ordinal(), flow_dir);
                     } else
                     {
                        int ox = 0;
                        int oy = 0;

                        if (nx == px)
                        {
                           // vertical movement
                           ox = 1;
                        } else if (ny == py)
                        {
                           // horizontal movement
                           oy = 1;
                        }

                        boolean created_child = false;

                        // checking if those neighbor locations are occupied already!
                        if (arena.CHECK_free_tile(px + ox, py + oy) && arena.braided_flow.get(px + ox, py + oy) == 0)
                        {
                           int particle_id_left = arena.spawn_particle(px + ox, py + oy, TYPE_BRAIDED_RIVER);
                           arena.sm_particles.set(particle_id_left, Particles.ANGLE.ordinal(), flow_dir);
                           created_child = true;
                        }

                        if (arena.CHECK_free_tile(px - ox, py - oy) && arena.braided_flow.get(px - ox, py - oy) == 0)
                        {
                           int particle_id_right = arena.spawn_particle(px - ox, py - oy, TYPE_BRAIDED_RIVER);
                           arena.sm_particles.set(particle_id_right, Particles.ANGLE.ordinal(), flow_dir);
                           created_child = true;
                        }

                        if (created_child)
                        {
                           // hit rock at location
                           arena.hammer(nx, ny, 2, false);
                        }
                     }
                  }
                  // set variant of this to 1 will prevent another spawning of a child
                  data_particle[VARIANT.ordinal()] = 1;
               }
            }
            break;
            case TYPE_GRAVEL:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               time += delta;
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);

               if (time >= Util.INT_TO_FLOAT(Config.CONF.ALLUVIAL_FAN_CHILD_DELAY_MS.value * 2))
               {
                  // the particle can be killed, the gravel is saved in the effect map.
                  // it does damage and is rendered from there
                  dead = true;
               }

               if (data_particle[DATA1.ordinal()] == 0)
               {
                  arena.hit_all_miners(data_particle[POSX.ordinal()], data_particle[POSY.ordinal()], Config.CONF.ALLUVIAL_FAN_CAST_DAMAGE.value);
                  data_particle[DATA1.ordinal()] = 1;
               }

               if (data_particle[VARIANT.ordinal()] == 0 && time >= Util.INT_TO_FLOAT(Config.CONF.ALLUVIAL_FAN_CHILD_DELAY_MS.value))
               {
                  // spawn children
                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];
                  // using angle as view dir!
                  int flow_dir = data_particle[ANGLE.ordinal()];

                  int origin_x = data_particle[TARGETX.ordinal()];
                  int origin_y = data_particle[TARGETY.ordinal()];

                  // populate the spawn candidates depending on direction
                  switch (flow_dir)
                  {
                     case 0: // UP
                        alluvial_spawn_candidates_x[0] = px - 1;
                        alluvial_spawn_candidates_y[0] = py + 1;
                        alluvial_spawn_candidates_x[1] = px;
                        alluvial_spawn_candidates_y[1] = py + 1;
                        alluvial_spawn_candidates_x[2] = px + 1;
                        alluvial_spawn_candidates_y[2] = py + 1;
                        break;
                     case 1: // RIGHT
                        alluvial_spawn_candidates_x[0] = px + 1;
                        alluvial_spawn_candidates_y[0] = py - 1;
                        alluvial_spawn_candidates_x[1] = px + 1;
                        alluvial_spawn_candidates_y[1] = py;
                        alluvial_spawn_candidates_x[2] = px + 1;
                        alluvial_spawn_candidates_y[2] = py + 1;
                        break;
                     case 2: // DOWN
                        alluvial_spawn_candidates_x[0] = px - 1;
                        alluvial_spawn_candidates_y[0] = py - 1;
                        alluvial_spawn_candidates_x[1] = px;
                        alluvial_spawn_candidates_y[1] = py - 1;
                        alluvial_spawn_candidates_x[2] = px + 1;
                        alluvial_spawn_candidates_y[2] = py - 1;
                        break;
                     case 3: // LEFT
                        alluvial_spawn_candidates_x[0] = px - 1;
                        alluvial_spawn_candidates_y[0] = py - 1;
                        alluvial_spawn_candidates_x[1] = px - 1;
                        alluvial_spawn_candidates_y[1] = py;
                        alluvial_spawn_candidates_x[2] = px - 1;
                        alluvial_spawn_candidates_y[2] = py + 1;
                        break;
                  }

                  // try to spawn at candidate location, if gravel effect layer is free

                  for (int j = 0; j < 3; j++)
                  {
                     if (arena.gravel.get(alluvial_spawn_candidates_x[j], alluvial_spawn_candidates_y[j]) == 0)
                     {
                        int dst_to_mid = Util.euclid_norm(alluvial_spawn_candidates_x[j], alluvial_spawn_candidates_y[j], origin_x, origin_y);
                        if (dst_to_mid > Config.CONF.ALLUVIAL_FAN_TILE_RADIUS.value || !arena.CHECK_bounds(alluvial_spawn_candidates_x[j], alluvial_spawn_candidates_y[j]) || !arena.CHECK_free_tile(alluvial_spawn_candidates_x[j], alluvial_spawn_candidates_y[j]))
                           continue;
                        int particle_id = arena.spawn_particle(alluvial_spawn_candidates_x[j], alluvial_spawn_candidates_y[j], TYPE_GRAVEL);
                        // child must have same flow dir!
                        arena.sm_particles.set(particle_id, ANGLE.ordinal(), flow_dir);

                        arena.sm_particles.set(particle_id, TARGETX.ordinal(), origin_x);
                        arena.sm_particles.set(particle_id, TARGETY.ordinal(), origin_y);
                     }
                  }
                  data_particle[VARIANT.ordinal()] = 1;
               }
            }
            break;
            case TYPE_ICEBLOCK:
            {
               // increment the pixel offset
               // until a new tile is reached,
               // check the new block and destroy it if needed,
               // hit miners
               // spawn icy floor

               float offset = Util.INT_TO_FLOAT(data_particle[TARGETX.ordinal()]);
               float speed = Util.INT_TO_FLOAT(Config.CONF.GLACIER_SPEED.value);

               offset += delta * 16 * (1 / speed);
               if (data_particle[DATA1.ordinal()] < 1 && offset > Arena.tile_size_px / 4f)
               {
                  int viewdir = data_particle[ANGLE.ordinal()];
                  int nx = data_particle[POSX.ordinal()] + Util.fourdirx[viewdir];
                  int ny = data_particle[POSY.ordinal()] + Util.fourdiry[viewdir];
                  arena.hit_all_miners(nx, ny, Config.CONF.GLACIER_MINER_DAMAGE.value);
                  arena.hammer(nx, ny, Config.CONF.GLACIER_BLOCK_DAMAGE.value, false);
                  arena.ice.set(data_particle[POSX.ordinal()], data_particle[POSY.ordinal()], (byte) 1);
                  data_particle[DATA1.ordinal()] = 1;
               }
               if (offset >= Arena.tile_size_px)
               {
                  offset = 0;
                  // reset the ability to hit miners and blocks
                  data_particle[DATA1.ordinal()] = 0;

                  // change the tile position
                  int viewdir = data_particle[ANGLE.ordinal()];
                  data_particle[POSX.ordinal()] += Util.fourdirx[viewdir];
                  data_particle[POSY.ordinal()] += Util.fourdiry[viewdir];

                  data_particle[LIFE.ordinal()]++;
                  if (data_particle[LIFE.ordinal()] >= 10)
                  {
                     dead = true;
                     // TODO: 21.04.23 spawn ice particles when despawning
                  }
               }

               data_particle[TARGETX.ordinal()] = Util.FLOAT_TO_INT(offset);
            }
            break;
            case TYPE_LANDSLIDE:
            {
               float prog = Util.INT_TO_FLOAT(data_particle[DATA1.ordinal()]);
               float speed = Util.INT_TO_FLOAT(Config.CONF.LANDSLIDE_TIME_MS.value);

               prog += delta * (1 / speed);

               if (prog >= 1f)
               {
                  data_particle[LIFE.ordinal()]++;
                  int viewdir = data_particle[ANGLE.ordinal()];
                  data_particle[POSX.ordinal()] += Util.fourdirx[viewdir];
                  data_particle[POSY.ordinal()] += Util.fourdiry[viewdir];

                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];

                  RockTypes rt = RockTypes.safe_ord(arena.rock_walls.get(px, py));
                  if (rt == null)
                  {
                     data_particle[LIFE.ordinal()] += 1;
                     arena.rock_walls.set(px, py, (byte) RockTypes.SANDSTONE.ordinal());
                     arena.rock_growth.set(px, py, (byte) 0);
                  }
                  prog = 0f;

                  if (data_particle[LIFE.ordinal()] >= Config.CONF.LANDSLIDE_POWER.value)
                  {
                     dead = true;
                  }
               }
               data_particle[DATA1.ordinal()] = Util.FLOAT_TO_INT(prog);
            }
            break;
            case TYPE_RAYLEIGH:
            {
               float prog = Util.INT_TO_FLOAT(data_particle[DATA1.ordinal()]);
               float speed = Util.INT_TO_FLOAT(Config.CONF.RAYLEIGH_TIME_MS.value);

               prog += delta * (1 / speed);

               if (prog >= 1f)
               {
                  data_particle[LIFE.ordinal()]++;
                  int viewdir = data_particle[ANGLE.ordinal()];
                  data_particle[POSX.ordinal()] += Util.fourdirx[viewdir];
                  data_particle[POSY.ordinal()] += Util.fourdiry[viewdir];

                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];

                  // TODO: 04.06.23 do damage somehow, maybe if miners are next to rock and wave particle
                  RockTypes rt = RockTypes.safe_ord(arena.rock_walls.get(px, py));
                  if (rt != null)
                  {
                     arena.rock_growth.set(px, py, (byte) 3);
                  }
                  prog = 0f;

                  if (data_particle[LIFE.ordinal()] > Config.CONF.RAYLEIGH_DIST_BACKLOOK.value)
                  {
                     int px_back = data_particle[POSX.ordinal()] + Config.CONF.RAYLEIGH_DIST_BACKLOOK.value * -Util.fourdirx[viewdir];
                     int py_back = data_particle[POSY.ordinal()] + Config.CONF.RAYLEIGH_DIST_BACKLOOK.value * -Util.fourdiry[viewdir];

                     RockTypes rt_back = RockTypes.safe_ord(arena.rock_walls.get(px_back, py_back));
                     if (rt_back != null)
                     {
                        arena.rock_growth.set(px_back, py_back, (byte) 3);
                     }
                  }

                  if (data_particle[LIFE.ordinal()] > Config.CONF.RAYLEIGH_DIST_BACKLOOK.value * 2)
                  {
                     int px_back = data_particle[POSX.ordinal()] + Config.CONF.RAYLEIGH_DIST_BACKLOOK.value * 2 * -Util.fourdirx[viewdir];
                     int py_back = data_particle[POSY.ordinal()] + Config.CONF.RAYLEIGH_DIST_BACKLOOK.value * 2 * -Util.fourdiry[viewdir];

                     RockTypes rt_back = RockTypes.safe_ord(arena.rock_walls.get(px_back, py_back));
                     if (rt_back != null)
                     {
                        arena.rock_growth.set(px_back, py_back, (byte) 3);
                     }
                  }

                  if (data_particle[LIFE.ordinal()] >= Config.CONF.RAYLEIGH_POWER.value)
                  {
                     dead = true;
                  }
               }
               data_particle[DATA1.ordinal()] = Util.FLOAT_TO_INT(prog);
            }

            break;
            case TYPE_EROSION:
            {
               float prog = Util.INT_TO_FLOAT(data_particle[DATA1.ordinal()]);
               float speed = Util.INT_TO_FLOAT(Config.CONF.EROSION_SPEED_MS.value);

               prog += delta * (1 / speed);

               if (prog >= 1f)
               {
                  data_particle[LIFE.ordinal()]++;
                  int viewdir = data_particle[ANGLE.ordinal()];
                  data_particle[POSX.ordinal()] += Util.fourdirx[viewdir];
                  data_particle[POSY.ordinal()] += Util.fourdiry[viewdir];

                  int px = data_particle[POSX.ordinal()];
                  int py = data_particle[POSY.ordinal()];
                  arena.hit_all_miners(px, py, Config.CONF.EROSION_BASE_DAMAGE.value);

                  RockTypes rt = RockTypes.safe_ord(arena.rock_walls.get(px, py));
                  if (rt != null)
                  {
                     arena.hammer(px, py, 20, false);
                     data_particle[LIFE.ordinal()] += rt.ordinal() + 1;
                     //arena.rock_walls.set(px, py, (byte) -1);
                     byte gravel_val = (byte) MathUtils.clamp(arena.gravel.get(px, py) + rt.ordinal() + 1, 0, 4);
                     arena.gravel.set(px, py, gravel_val);
                  }
                  prog = 0f;

                  if (data_particle[LIFE.ordinal()] >= Config.CONF.EROSION_POWER.value)
                  {
                     dead = true;
                  }
               }

               data_particle[DATA1.ordinal()] = Util.FLOAT_TO_INT(prog);
            }
            break;
            case TYPE_MOVING_ROCK:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float life_time = Util.INT_TO_FLOAT(Config.CONF.MAGNETISM_MOVE_TIME_MS.value);
               time = MathUtils.clamp(time + delta / life_time, 0, 1);
               if (time >= 1)
               {
                  dead = true;
                  arena.rock_walls.set(data_particle[TARGETX.ordinal()], data_particle[TARGETY.ordinal()], (byte) data_particle[DATA1.ordinal()]);
                  arena.rock_growth.set(data_particle[TARGETX.ordinal()], data_particle[TARGETY.ordinal()], (byte) 4);
               } else
               {
                  data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
               }
            }
            break;
            case TYPE_MAGNET:
            {
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               float life_time = Util.INT_TO_FLOAT(data_particle[DATA1.ordinal()] == -1 ? Config.CONF.MAGNETISM_FLY_SPEED_MS_PER_TILE.value : Config.CONF.MAGNETISM_APPROACH_TIME_MS.value);
               time = MathUtils.clamp(time + delta / life_time, 0, 1);
               if (time >= 1)
               {
                  time = 0;
                  if (data_particle[DATA1.ordinal()] != -1)
                  {
                     arena.perform_magnetism(data_particle[TARGETX.ordinal()], data_particle[TARGETY.ordinal()]);
                     dead = true;
                  } else
                  {
                     int viewdir = data_particle[ANGLE.ordinal()];
                     data_particle[POSX.ordinal()] += Util.fourdirx[viewdir];
                     data_particle[POSY.ordinal()] += Util.fourdiry[viewdir];

                     // check if out of arena then die,
                     if (data_particle[Particles.POSX.ordinal()] < -5 || data_particle[Particles.POSX.ordinal()] > Arena.arena_width + 5 || data_particle[Particles.POSY.ordinal()] > Arena.arena_height + 5 || data_particle[Particles.POSY.ordinal()] < -5)
                     {
                        dead = true;
                     } else
                     {
                        // check if enemy miner is near then go into targeting mode
                        for (int j = 0; j < arena.sm_miners.num_lines(); j++)
                        {
                           if (j != data_particle[VARIANT.ordinal()])
                           {
                              // this is an enemy
                              if (arena.sm_miners.get(j, MinerData.ACTIVE.ordinal()) == 1)
                              {
                                 int miner_tx, miner_ty;
                                 miner_tx = arena.sm_miners.get(j, MinerData.TILEX.ordinal());
                                 miner_ty = arena.sm_miners.get(j, MinerData.TILEY.ordinal());

                                 // enemy is alive
                                 if (Util.simple_dist(data_particle[POSX.ordinal()], data_particle[POSY.ordinal()], miner_tx, miner_ty) < Config.CONF.MAGNETISM_TARGET_RADIUS.value * Config.CONF.MAGNETISM_TARGET_RADIUS.value)
                                 {
                                    data_particle[Particles.DATA1.ordinal()] = j;
                                    data_particle[TARGETX.ordinal()] = miner_tx;
                                    data_particle[TARGETY.ordinal()] = miner_ty;
                                    break;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
               data_particle[LIFE.ordinal()] = Util.FLOAT_TO_INT(time);
            }
            break;
         }

         // update particle data
         sm_particles.set_line(i, 0f, data_particle);

         if (dead)
         {
            sm_particles.clear_line(i);
         } else
         {
            if (sort_particle.get(data_particle[TYPE.ordinal()], -1) == 1)
            {
               int py = data_particle[POSY.ordinal()];
               arena.sm_sort_objects.add_line(py, 2, i);
            }
         }
      }
   }
}