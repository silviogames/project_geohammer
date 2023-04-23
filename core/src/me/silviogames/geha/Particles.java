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

   public static final int TYPE_BLOOD = 100;

   public static final int TYPE_TEST = 2000;

   public static final IntIntMap map_position_as_tiles = new IntIntMap();
   // TYPE_IDS of particles point to
   // -1 or 0 -> position is in pixels (DEFAULT)
   // 1 -> position is in tiles

   public static final IntIntMap map_faults = new IntIntMap();
   // TYPE_IDS of particles point to
   // -1 -> is NOT affected by a fault displacement
   // 1 -> IS affected by a fault displacement

   // every 0.1 tick all braided particles try to damage immersed miners
   static Timer timer_braided_damage = new Timer(0.1f);

   static int[] alluvial_spawn_candidates_x = new int[3];
   static int[] alluvial_spawn_candidates_y = new int[3];

   static
   {
      map_position_as_tiles.put(TYPE_OROGEN, 1);
      map_position_as_tiles.put(TYPE_IMPACTOR, 1);
      map_position_as_tiles.put(TYPE_BRAIDED_RIVER, 1);
      map_position_as_tiles.put(TYPE_ICEBLOCK, 1);
      map_position_as_tiles.put(TYPE_TEST, 1);

      // it looks like it is not but it internally does a pixel displacement during render
      map_position_as_tiles.put(TYPE_ROCK_DEBRIS, 1);

      // TODO: 22.03.23 blood right now may be a tile based particle but later when the miners move
      //  sub tiles then it might be pixel

      map_faults.put(TYPE_OROGEN, 1);
      map_faults.put(TYPE_ICEBLOCK, 1);

      map_position_as_tiles.put(TYPE_BRAIDED_RIVER, 1);

      // NOT SURE IF BLOOD IS SUPPOSED TO BE MOVED BUT THIS IS ONLY VISUAL SO ANYWAYS
      map_faults.put(TYPE_BLOOD, 1);
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
            case 2: // IMPACTOR
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
            case TYPE_BLOOD:
            {
               int px = data_particle[POSX.ordinal()];
               int py = data_particle[POSY.ordinal()];
               int ox = Config.CONF.MINER_BLOOD_PARTICLE_OFFSET_X.value;
               int oy = Config.CONF.MINER_BLOOD_PARTICLE_OFFSET_Y.value;
               float time = Util.INT_TO_FLOAT(data_particle[LIFE.ordinal()]);
               //float time_max = Util.INT_TO_FLOAT(Config.CONF.MINER_BLOOD_TIME_MS.value);
               // TODO: 22.03.23 CONTINUE HERE, there still seems to be a rendering bug with frames.
               //  after the blood animation plays rock debris is visible,
               //  also the loaded time in MS is not working, it does not play all frames!

               Main.batch.draw(Res.get_frame(time, Anim.PARTICLE_BLOOD), px + ox, py + oy);
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
   }

   public static void UPDATE_PARTICLES(Arena arena, Smartrix sm_particles, float delta)
   {
      // IT SHOULD BE POSSIBLE TO CREATE NEW PARTICLES DURING THE LOOP SINCE EITHER A FREE SPOT IS FOUND OR THE ARRAY IN ENLARGED WHICH IS FINE SINCE LOOP WILL NOT CONSIDER THE NEW ENTRIES

      boolean braided_damage_frame = timer_braided_damage.update(delta);

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
            case TYPE_TEST:
               data_particle[LIFE.ordinal()]++;
               if (data_particle[LIFE.ordinal()] > 60)
               {
                  dead = true;
               }
               break;
            case 1: // ROCK DEBRIS
            {
               // TODO: 14.02.23 change life to float saved as int  (ms as ints)
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
               // TODO: 27.03.23 changing the time here will change the look of the flow, if older ones
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
                  // TODO: 29.03.23 maybe this should be moved to another location since there might be two particles on one tile but the flow map is unique.
                  arena.hit_all_miners(px, py, 50);
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

               // TODO: 21.04.23 maybe the timing of destroying blocks must be tweaked so
               //  the ice block does not first overlap with the rock and then destroy it after
               //  fully reaching the block, so maybe during the transition before reaching it

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
         }

         // update particle data
         sm_particles.set_line(i, 0f, data_particle);

         if (dead) sm_particles.clear_line(i);
      }
   }
}