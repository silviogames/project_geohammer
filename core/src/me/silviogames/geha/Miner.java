package me.silviogames.geha;

import com.badlogic.gdx.math.MathUtils;

public class Miner
{
   public static void handle_input(Arena arena, int sm_controller_index)
   {
      // this is called inside the loop of sm_controllers
      int miner_id = Game.sm_controllers.get(sm_controller_index, Controller_Data.ID_MINER.ordinal());
      int controller_hash = Game.sm_controllers.get(sm_controller_index, Controller_Data.HASH.ordinal());

      inner_handle_input(arena, miner_id, controller_hash);
   }

   public static void inner_handle_input(Arena arena, int miner_id, int controller_hash)
   {
      // this method can be called directly passing miner id and the controller_hash,
      // controller_hash can be the debug value which keeps the state populated by keyboard input

      if (!arena.sm_miners.check_line(miner_id))
      {
         System.out.println("[Miner.handle_input: cannot control this miner with id] " + miner_id);
         return;
      }

      if (arena.sm_miners.get(miner_id, MinerData.ACTIVE.ordinal()) != 1)
      {
         //System.out.println("cannot control dead miner " + miner_id);
         return;
      }

      MinerAnim ma = MinerAnim.safe_ord(arena.sm_miners.get(miner_id, MinerData.ANIM.ordinal()));
      if (ma.supress_input) return;

      float spin_time = Util.INT_TO_FLOAT(arena.sm_miners.get(miner_id, MinerData.TIME_SPIN_EFFECT.ordinal()));
      if (spin_time > 0)
      {
         // the miner is in spin mode right now!
         // MOVE ONE TILE
         if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_LEFT) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_X) < -300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 3);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.SPIN_MOVING.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_RIGHT) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_X) > 300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 1);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.SPIN_MOVING.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_UP) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_Y) > 300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 0);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.SPIN_MOVING.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_DOWN) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_Y) < -300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 2);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.SPIN_MOVING.ordinal());
            }
         }
         // spin mode does not allow any other input than movement
         return;
      }

      if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.BUTTON_R) == 1)
      {
         // CHANGE VIEWDIR

      } else
      {
         // MOVE ONE TILE
         if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_LEFT) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_X) < -300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 3);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.MOVE.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_RIGHT) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_X) > 300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 1);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.MOVE.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_UP) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_Y) > 300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 0);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.MOVE.ordinal());
            }
         } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_DOWN) > 0 || Controller_Buttons.read_button(controller_hash, Controller_Buttons.LEFT_AXIS_Y) < -300)
         {
            arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 2);
            if (CHECK_move(arena, arena.sm_miners, miner_id))
            {
               arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
               arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.MOVE.ordinal());
            }
         } else
         {
            if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.RIGHT_AXIS_X) < -500)
            {
               arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 3);
            } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.RIGHT_AXIS_X) > 500)
            {
               arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 1);
            } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.RIGHT_AXIS_Y) < -500)
            {
               arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 2);
            } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.RIGHT_AXIS_Y) > 500)
            {
               arena.sm_miners.set(miner_id, MinerData.VIEWDIR.ordinal(), 0);
            }
            // one controller had problems with the RIGHT AXIS?? which prevented the A and B buttons to be pressed! but this controller was connected to the laptop as Bbitdo, while all others where PS4 dualshocks!

            if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_B) > 0)
            {
               if (consume_stamina(arena.sm_miners, miner_id, MinerAttack.SIMPLE_HIT))
               {
                  // NOT CHECKING FOR A BLOCK AT TARGET
                  arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
                  arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.SIMPLE_ATTACK.ordinal());
                  //basic_attack(arena, sm_miners, miner_id);
               }
            } else
            {
               if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_A) == 2)
               {
                  if (CHECK_skill_crystals(arena, miner_id))
                  {
                     arena.sm_miners.set(miner_id, MinerData.CAST.ordinal(), 0);
                     arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
                     arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.CAST_ATTACK.ordinal());
                  }
               } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_X) == 2)
               {
                  if (CHECK_skill_crystals(arena, miner_id))
                  {
                     arena.sm_miners.set(miner_id, MinerData.CAST.ordinal(), 1);
                     arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
                     arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.CAST_ATTACK.ordinal());
                  }
               } else if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_Y) == 2)
               {
                  if (CHECK_skill_crystals(arena, miner_id))
                  {
                     arena.sm_miners.set(miner_id, MinerData.CAST.ordinal(), 2);
                     arena.sm_miners.set(miner_id, MinerData.ANIM_TIME.ordinal(), 0);
                     arena.sm_miners.set(miner_id, MinerData.ANIM.ordinal(), MinerAnim.CAST_ATTACK.ordinal());
                  }
               }

            }
            // no movement
         }
      }
   }

   public static boolean consume_stamina(Smartrix smx_miner, int miner_id, MinerAttack attack)
   {
      if (Config.CONF.STAMINA_SYSTEM.value == 1)
      {
         int miner_stamina = smx_miner.get(miner_id, MinerData.STAMINA.ordinal());
         if (miner_stamina - attack.stamina_cost < 0)
         {
            System.out.println("not enough mana for action " + attack + " by miner [" + miner_id + "]");
            return false;
         } else
         {
            miner_stamina -= attack.stamina_cost;
            smx_miner.set(miner_id, MinerData.STAMINA.ordinal(), miner_stamina);
            return true;
         }
      } else
      {
         // attack can always be performed with stamina system off
         return true;
      }
   }

   // in all of these line corresponds to miner ID practically

   public static void basic_attack(Arena arena, Smartrix smx_miner, int line)
   {
      // if skills in the game move the miners this will now target a different gridtile, as the
      // animation for this skill may have been started before the miner was moved
      int view_dir = smx_miner.get(line, MinerData.VIEWDIR.ordinal());
      int next_tx = smx_miner.get(line, MinerData.TILEX.ordinal()) + Util.fourdirx[view_dir];
      int next_ty = smx_miner.get(line, MinerData.TILEY.ordinal()) + Util.fourdiry[view_dir];
      arena.hammer(next_tx, next_ty, 2, true);
      arena.hit_all_miners(next_tx, next_ty, Config.CONF.MINER_SIMPLE_ATTACK_DAMAGE.value);
   }

   private static void special_attack(Arena arena, Smartrix smx_miner, int line)
   {
      // this is the special attack for now
      int mid_tx = smx_miner.get(line, MinerData.TILEX.ordinal());
      int mid_ty = smx_miner.get(line, MinerData.TILEY.ordinal());
      // hitting all 8 tiles around the player
      for (int i = 0; i < 8; i++)
      {
         arena.hammer(mid_tx + Util.eightdirx[i], mid_ty + Util.eightdiry[i], 2, true);
      }
   }

   public static void perform_special_attack(Arena arena, Smartrix sm_miner, int miner_id, int special_variant)
   {
      // special_variant:
      // 0 -> offensive
      // 1 -> defensive
      // 2 -> special special

      if (special_variant < 0 || special_variant > 2)
      {
         System.out.println("cannot cast this special_variant: " + special_variant);
         return;
      }

      Miner.MinerClass mc = Miner.MinerClass.safe_ord(arena.sm_miners.get(miner_id, MinerData.CLASS.ordinal()));
      if (mc == null)
      {
         System.out.println("miner [" + miner_id + "] controlled miner has no class!");
      } else
      {
         MinerAttack ma = null;

         switch (special_variant)
         {
            case 0: // OFFENSIVE
               ma = mc.special_offensive;
               break;
            case 1: // DEFENSIVE
               ma = mc.special_defensive;
               break;
            case 2: // SPECIAL SPECIAL
               ma = mc.special_special;
               break;
         }

         if (ma == null)
         {
            System.out.println("cannot perform special attack that is null!");
            return;
         }

         int miner_crystals = sm_miner.get(miner_id, MinerData.NUM_CRYSTALS.ordinal());
         boolean power = arena.sm_miners.get(miner_id, MinerData.TIME_POWER_EFFECT.ordinal()) > 0;

         if (!power && miner_crystals - Config.CONF.CRYSTAL_SKILL_COST.value < 0)
         {
            System.out.println("miner [" + miner_id + "] does not have enough crystals to perform " + ma);
            return;
         }

         // most skills will work regardless of the arena state
         boolean consume_crystals = true;
         // the skill will check if crystal consumption is needed and if so set consume_crystals to false
         switch (ma)
         {
            case GLACIER:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
               Util.GEN_RECT_POSITIONS(miner_tx, miner_ty, Config.CONF.GLACIER_OFFSET.value, view_dir, Config.CONF.GLACIER_WIDTH.value, 1);

               for (int i = 0; i < Util.RECT_xpos.size; i++)
               {
                  int pid = arena.spawn_particle(Util.RECT_xpos.get(i), Util.RECT_ypos.get(i), Particles.TYPE_ICEBLOCK);
                  arena.sm_particles.set(pid, Particles.ANGLE.ordinal(), view_dir);
                  float val = 1 - MathUtils.sin(MathUtils.map(0, Util.RECT_xpos.size, 0, MathUtils.PI, i));
                  arena.sm_particles.set(pid, Particles.TARGETX.ordinal(), Util.FLOAT_TO_INT(-16 * val));
               }
            }
            break;
            case EROSION:
            {
               // TODO: 01.06.23 check if rocks can be eroded, if not do not consume crystals
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               Util.GEN_RECT_POSITIONS(miner_tx, miner_ty, Config.CONF.EROSION_CAST_OFFSET.value, view_dir, Config.CONF.EROSION_WIDTH.value, 1);

               for (int i = 0; i < Util.RECT_xpos.size; i++)
               {
                  int pid = arena.spawn_particle(Util.RECT_xpos.get(i), Util.RECT_ypos.get(i), Particles.TYPE_EROSION);
                  arena.sm_particles.set(pid, Particles.ANGLE.ordinal(), view_dir);
                  //float val = 1 - MathUtils.sin(MathUtils.map(0, Util.RECT_xpos.size, 0, MathUtils.PI, i));
                  //arena.sm_particles.set(pid, Particles.TARGETX.ordinal(), Util.FLOAT_TO_INT(-16 * val));
               }

            }
            break;
            case BRAIDED_RIVER:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               int spawn_x = miner_tx + Config.CONF.BRAIDED_CAST_OFFSET.value * Util.fourdirx[view_dir];
               int spawn_y = miner_ty + Config.CONF.BRAIDED_CAST_OFFSET.value * Util.fourdiry[view_dir];

               if (!arena.CHECK_free_tile(spawn_x, spawn_y))
               {
                  consume_crystals = false;
               } else
               {
                  int particle_id = arena.spawn_particle(spawn_x, spawn_y, Particles.TYPE_BRAIDED_RIVER);
                  arena.sm_particles.set(particle_id, Particles.ANGLE.ordinal(), view_dir);
               }
            }
            break;
            case IMPACT:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
               arena.spawn_particle(miner_tx + Config.CONF.IMPACTOR_TARGET_DISTANCE.value * Util.fourdirx[view_dir], miner_ty + Config.CONF.IMPACTOR_TARGET_DISTANCE.value * Util.fourdiry[view_dir], Particles.TYPE_IMPACTOR);
            }
            break;

            case OROGENY:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               Util.GEN_RECT_POSITIONS(miner_tx, miner_ty, Config.CONF.OROGENY_OFFSET_FROM_CASTER.value, view_dir, Config.CONF.OROGENY_WIDTH.value, Config.CONF.OROGENY_HEIGHT.value);

               for (int i = 0; i < Util.RECT_xpos.size; i++)
               {
                  int tx = Util.RECT_xpos.get(i);
                  int ty = Util.RECT_ypos.get(i);
                  int pid = arena.spawn_particle(tx, ty, Particles.TYPE_OROGEN);
                  arena.create_orogeny_block(tx, ty, Util.RECT_growth.get(i));
               }
            }
            break;

            case TRANSFORM_FAULT:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
               int tf_variant = 0;
               int xy_offset = 0;
               if (view_dir == 0 || view_dir == 2)
               {
                  // HORIZONTAL
                  tf_variant = 1;
                  xy_offset = miner_ty + (view_dir == 0 ? 1 : -1) * Config.CONF.TRANSFORM_FAULT_OFFSET.value;
               } else if (view_dir == 1 || view_dir == 3)
               {
                  tf_variant = 2;
                  xy_offset = miner_tx + (view_dir == 1 ? 1 : -1) * Config.CONF.TRANSFORM_FAULT_OFFSET.value;
               }
               consume_crystals = arena.init_transform_fault(xy_offset, tf_variant == 2);

               arena.spawn_particle(tf_variant, xy_offset, Particles.TYPE_TRANSFORM_FAULT);
            }
            break;

            case MELT:
            {
               // TODO: 01.06.23 make animation of melt similar to erosion,
               //  a check is needed if there will be rocks to melt and if crystals should be consumed

               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               Util.GEN_CIRCLE_POSITIONS(miner_tx + Util.fourdirx[view_dir] * Config.CONF.MELT_OFFSET.value, miner_ty + Util.fourdiry[view_dir] * Config.CONF.MELT_OFFSET.value, Config.CONF.MELT_RADIUS.value);

               int num_melts = 0;

               for (int i = 0; i < Util.CIRCLE_xpos.size; i++)
               {
                  int mx = Util.CIRCLE_xpos.get(i);
                  int my = Util.CIRCLE_ypos.get(i);

                  if (arena.rock_walls.get(mx, my) >= 0)
                  {
                     num_melts++;
                     arena.melt.set(mx, my, (byte) (60 + (arena.rock_walls.get(mx, my) + 1) * 20));
                     arena.rock_walls.set(mx, my, (byte) -1);
                  }
               }
               if (num_melts == 0) consume_crystals = false;
            }
            break;

            case TSCHERMAK:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               int circle_center_x = miner_tx + Util.fourdirx[view_dir] * Config.CONF.TSCHERMAK_OFFSET.value;
               int circle_center_y = miner_ty + Util.fourdiry[view_dir] * Config.CONF.TSCHERMAK_OFFSET.value;

               Util.GEN_CIRCLE_POSITIONS(circle_center_x, circle_center_y, Config.CONF.TSCHERMAK_MERGE_RADIUS.value);

               int num_small_crystals = 0;

               for (int i = 0; i < Util.CIRCLE_xpos.size; i++)
               {
                  int mx = Util.CIRCLE_xpos.get(i);
                  int my = Util.CIRCLE_ypos.get(i);

                  if (arena.drops.get(mx, my) >= 0)
                  {
                     byte drop_type = arena.drops.get(mx, my);
                     Drops d = Drops.safe_ord(drop_type);
                     if (d == null)
                     {
                        // I hope this is never the case!
                        num_small_crystals++;
                     } else
                     {
                        num_small_crystals += d.tschermak_merge_value;
                     }
                     arena.drops.set(mx, my, (byte) -1);
                     int pid = arena.spawn_particle(mx, my, Particles.TYPE_TSCHERMAK_MERGER_CRYSTALS);
                     arena.sm_particles.set(pid, Particles.VARIANT.ordinal(), (int) drop_type);
                     arena.sm_particles.set(pid, Particles.TARGETX.ordinal(), circle_center_x);
                     arena.sm_particles.set(pid, Particles.TARGETY.ordinal(), circle_center_y);
                     // small merger crystal will lerp to the center of the circle to form a large one
                  }
               }

               if (num_small_crystals > 0)
               {
                  // the big merged crystal only is visible after the merge
                  int big_pid = arena.spawn_particle(circle_center_x, circle_center_y, Particles.TYPE_TSCHERMAK_MERGED);
                  // not sure if resetting life here is redundant!
                  arena.sm_particles.set(big_pid, Particles.LIFE.ordinal(), 0);
                  // telling the big merged crystal how many small ones have been found to merge!
                  arena.sm_particles.set(big_pid, Particles.VARIANT.ordinal(), num_small_crystals);
                  arena.sm_particles.set(big_pid, Particles.ANGLE.ordinal(), miner_id);
                  arena.sm_particles.set(big_pid, Particles.DATA1.ordinal(), 0);
               } else
               {
                  consume_crystals = false;
               }
               // if there are no crystals then the skill is useless!
            }
            break;
            case GROWTH:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               int circle_center_x = miner_tx + Util.fourdirx[view_dir] * Config.CONF.GROWTH_OFFSET.value;
               int circle_center_y = miner_ty + Util.fourdiry[view_dir] * Config.CONF.GROWTH_OFFSET.value;

               Util.GEN_CIRCLE_POSITIONS(circle_center_x, circle_center_y, Config.CONF.GROWTH_RADIUS.value);

               int num_growth = 0;

               for (int i = 0; i < Util.CIRCLE_xpos.size; i++)
               {
                  int mx = Util.CIRCLE_xpos.get(i);
                  int my = Util.CIRCLE_ypos.get(i);

                  // TURN RICH MELTS INTO CRYSTALS
                  if (arena.melt.get(mx, my) > Config.CONF.MELT_FREEZE_VALUE.value)
                  {
                     num_growth++;
                     arena.melt.set(mx, my, (byte) MathUtils.random(1, Config.CONF.MELT_FREEZE_VALUE.value));

                     if (MathUtils.randomBoolean(Config.CONF.GROWTH_CHANCE_PERCENT.value / 100f))
                     {
                        if (arena.drops.get(mx, my) == -1)
                        {
                           Drops drop = Drops.spawn_random();
                           arena.drops.set(mx, my, (byte) drop.ordinal());
                        }
                     }
                  }
               }
               if (num_growth == 0) consume_crystals = false;
            }
            break;
            case LANDSLIDE:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               Util.GEN_RECT_POSITIONS(miner_tx, miner_ty, Config.CONF.LANDSLIDE_CAST_OFFSET.value, view_dir, Config.CONF.LANDSLIDE_WIDTH.value, 1);

               for (int i = 0; i < Util.RECT_xpos.size; i++)
               {
                  int pid = arena.spawn_particle(Util.RECT_xpos.get(i), Util.RECT_ypos.get(i), Particles.TYPE_LANDSLIDE);
                  arena.sm_particles.set(pid, Particles.ANGLE.ordinal(), view_dir);
                  //float val = 1 - MathUtils.sin(MathUtils.map(0, Util.RECT_xpos.size, 0, MathUtils.PI, i));
                  //arena.sm_particles.set(pid, Particles.TARGETX.ordinal(), Util.FLOAT_TO_INT(-16 * val));
               }

            }
            break;
            case RAYLEIGH:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

               Util.GEN_RECT_POSITIONS(miner_tx, miner_ty, Config.CONF.RAYLEIGH_CAST_OFFSET.value, view_dir, Config.CONF.RAYLEIGH_WIDTH.value, 1);

               for (int i = 0; i < Util.RECT_xpos.size; i++)
               {
                  int pid = arena.spawn_particle(Util.RECT_xpos.get(i), Util.RECT_ypos.get(i), Particles.TYPE_RAYLEIGH);
                  arena.sm_particles.set(pid, Particles.ANGLE.ordinal(), view_dir);
                  //float val = 1 - MathUtils.sin(MathUtils.map(0, Util.RECT_xpos.size, 0, MathUtils.PI, i));
                  //arena.sm_particles.set(pid, Particles.TARGETX.ordinal(), Util.FLOAT_TO_INT(-16 * val));
               }
            }
            break;
            case MAGNETISM:
            {
               int miner_tx, miner_ty;
               miner_tx = sm_miner.get(miner_id, MinerData.TILEX.ordinal());
               miner_ty = sm_miner.get(miner_id, MinerData.TILEY.ordinal());
               int pid = arena.spawn_particle(miner_tx, miner_ty, Particles.TYPE_MAGNET);
               arena.sm_particles.set(pid, Particles.ANGLE.ordinal(), sm_miner.get(miner_id, MinerData.VIEWDIR.ordinal()));
               // setting target id to -1, just to make sure. I don't know if by default it is -1, a reused line may have something else there
               arena.sm_particles.set(pid, Particles.DATA1.ordinal(), -1);
               arena.sm_particles.set(pid, Particles.VARIANT.ordinal(), miner_id);
            }
            break;
         }

         if (consume_crystals)
         {
            if (!power)
            {
               miner_crystals -= Config.CONF.CRYSTAL_SKILL_COST.value;
               // OVERWRITE VALUE
               sm_miner.set(miner_id, MinerData.NUM_CRYSTALS.ordinal(), miner_crystals);
            }
         }
      }
   }

   public static boolean CHECK_skill_crystals(Arena arena, int miner_id)
   {
      boolean power = arena.sm_miners.get(miner_id, MinerData.TIME_POWER_EFFECT.ordinal()) > 0;
      if (power) return true;
      // check if miner currently has enough crystals to perform a special attack
      int miner_crystals = arena.sm_miners.get(miner_id, MinerData.NUM_CRYSTALS.ordinal());
      return miner_crystals - Config.CONF.CRYSTAL_SKILL_COST.value >= 0;
   }

   public static boolean CHECK_move(Arena arena, Smartrix smx_miner, int line)
   {
      // CAN MINER MOVE TO THIS TILE?
      int dir_move = smx_miner.get(line, MinerData.VIEWDIR.ordinal());
      int next_tx = smx_miner.get(line, MinerData.TILEX.ordinal()) + Util.fourdirx[dir_move];
      int next_ty = smx_miner.get(line, MinerData.TILEY.ordinal()) + Util.fourdiry[dir_move];
      return arena.CHECK_free_tile(next_tx, next_ty) && (arena.CHECK_bounds(next_tx, next_ty));
   }

   public static void move_miner(Arena arena, Smartrix smx_miner, int miner_id, int dir_move)
   {
      // this function is called after a MinerAnim that is supposed to move the miner on the grid.

      // reading and writing directly from and into the matrix that holds the
      // miner data

      // dir_move is same as dir_move ?? all the time?

      int next_tx = smx_miner.get(miner_id, MinerData.TILEX.ordinal()) + Util.fourdirx[dir_move];
      int next_ty = smx_miner.get(miner_id, MinerData.TILEY.ordinal()) + Util.fourdiry[dir_move];

      // set miner dir, even if not moving
      smx_miner.set(miner_id, MinerData.VIEWDIR.ordinal(), dir_move);

      if (arena.CHECK_free_tile(next_tx, next_ty))
      {
         if (arena.CHECK_bounds(next_tx, next_ty))
         {
            smx_miner.set(miner_id, MinerData.TILEX.ordinal(), next_tx);
            smx_miner.set(miner_id, MinerData.TILEY.ordinal(), next_ty);

            // HERE STUFF IS CHECKED THAT ONLY DEALS DAMAGE ONCE WHEN STEPPING ON IT!
            byte gravel_val = arena.gravel.get(next_tx, next_ty);
            if (gravel_val > 0)
            {
               arena.deal_damage_to_miner(miner_id, gravel_val * Config.CONF.EROSION_BASE_DAMAGE.value);
            }
         }
      } else
      {
         // TODO: 26.07.2021 player bump sound?
      }
      // for now move the miner in one frame. later we do a lerp but still keep the grid location
   }

   public enum MinerClass
   {
      KENKMANN(MinerAttack.SIMPLE_HIT, MinerAttack.ROUND_SWING, MinerAttack.IMPACT, MinerAttack.OROGENY, MinerAttack.TRANSFORM_FAULT),
      PREUSSER(MinerAttack.SIMPLE_HIT, MinerAttack.ROUND_SWING, MinerAttack.BRAIDED_RIVER, MinerAttack.EROSION, MinerAttack.GLACIER),
      HERGARTEN(MinerAttack.SIMPLE_HIT, MinerAttack.ROUND_SWING, MinerAttack.LANDSLIDE, MinerAttack.MAGNETISM, MinerAttack.RAYLEIGH),
      DOLEJS(MinerAttack.SIMPLE_HIT, MinerAttack.ROUND_SWING, MinerAttack.TSCHERMAK, MinerAttack.MELT, MinerAttack.GROWTH),
      ;

      public final MinerAttack primary, secondary, special_offensive, special_defensive, special_special;

      MinerClass(MinerAttack primary, MinerAttack secondary, MinerAttack offensive, MinerAttack defensive, MinerAttack special)
      {
         this.primary = primary;
         this.secondary = secondary;
         this.special_offensive = offensive; // (A)
         this.special_defensive = defensive; // (X)
         this.special_special = special;  // (Y)
      }

      public static MinerClass random()
      {
         return values()[MathUtils.random(0, values().length - 1)];
      }

      public static MinerClass safe_ord(int ord)
      {
         if (ord < 0 || ord >= values().length)
         {
            return null;
         } else
         {
            return values()[ord];
         }
      }
   }
}