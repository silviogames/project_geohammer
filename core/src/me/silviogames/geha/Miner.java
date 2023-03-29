package me.silviogames.geha;

public class Miner
{
   public static void handle_input(Arena arena, Smartrix smx_miner, int line, Controller_Buttons ia)
   {
      if (!smx_miner.check_line(line))
      {
         System.out.println("[Miner.handle_input: cannot control this miner with id] " + line);
         return;
      }

      if (smx_miner.get(line, MinerData.ACTIVE.ordinal()) != 1)
      {
         System.out.println("cannot control dead miner " + line);
         return;
      }
      switch (ia)
      {
         case DPAD_UP:
            move(arena, smx_miner, line, 0);
            break;
         case DPAD_DOWN:
            move(arena, smx_miner, line, 2);
            break;
         case DPAD_LEFT:
            move(arena, smx_miner, line, 3);
            break;
         case DPAD_RIGHT:
            move(arena, smx_miner, line, 1);
            break;
         case ACTIONS_B:
            if (consume_stamina(smx_miner, line, MinerAttack.SIMPLE_HIT))
            {
               basic_attack(arena, smx_miner, line);
            }
            break;
         case ACTIONS_A:
            Miner.perform_special_attack(arena, arena.sm_miners, line, 0);
            break;
         case ACTIONS_Y:
            Miner.perform_special_attack(arena, arena.sm_miners, line, 1);
            break;
         case ACTIONS_X:
            Miner.perform_special_attack(arena, arena.sm_miners, line, 2);
            break;

         //case ACTIONS_A:

         //if (consume_stamina(smx_miner, line, MinerAttack.ROUND_SWING))
         //{
         //	special_attack(arena, smx_miner, line);
         //}
         //break;
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

   private static void basic_attack(Arena arena, Smartrix smx_miner, int line)
   {
      int view_dir = smx_miner.get(line, MinerData.VIEWDIR.ordinal());
      int next_tx = smx_miner.get(line, MinerData.TILEX.ordinal()) + Util.fourdirx[view_dir];
      int next_ty = smx_miner.get(line, MinerData.TILEY.ordinal()) + Util.fourdiry[view_dir];
      arena.hammer(next_tx, next_ty, 1);
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
         arena.hammer(mid_tx + Util.eightdirx[i], mid_ty + Util.eightdiry[i], 2);
      }
   }

   public static void perform_special_attack(Arena arena, Smartrix sm_miner, int miner_id, int special_variant)
   {
      // special_variant:
      // 0 -> offensive
      // 1 -> defensive
      // 2 -> special special

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
         if (miner_crystals - ma.crystal_cost < 0)
         {
            System.out.println("miner [" + miner_id + "] does not have enough crystals to perform " + ma);
            return;
         } else
         {
            miner_crystals -= ma.crystal_cost;
            // OVERWRITE VALUE
            sm_miner.set(miner_id, MinerData.NUM_CRYSTALS.ordinal(), miner_crystals);

            System.out.println("miner [" + miner_id + "] now performs " + ma);
         }
         // TODO: 24.02.23 right now miners can perform as many special attacks per time they want (as long as crystals are possessed)
         // 	maybe in the future I want to introduce a cool down, at least for very special skills that involve a longer animation which I do not want to overlap

         // TODO: 24.02.23 for now there is only one crystal, later 3 will be introduced, maybe!

         // TODO: 24.02.23 for now the special attacks are a switch case here, maybe this will move to a separate function in the MinerAttack enum but it is fine I think!
         switch (ma)
         {
            case ALLUVIAL_FAN:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
               int particle_id = arena.spawn_particle(miner_tx + Util.fourdirx[view_dir], miner_ty + Util.fourdiry[view_dir], Particles.TYPE_GRAVEL);
               arena.sm_particles.set(particle_id, Particles.ANGLE.ordinal(), view_dir);
               // putting the origin of the fan in target pos so the particles know when to stop,
               // this info is passed onto children particles
               arena.sm_particles.set(particle_id, Particles.TARGETX.ordinal(), miner_tx);
               arena.sm_particles.set(particle_id, Particles.TARGETY.ordinal(), miner_ty);
            }
            break;
            case BRAIDED_RIVER:
            {
               int miner_tx = arena.sm_miners.get(miner_id, MinerData.TILEX.ordinal());
               int miner_ty = arena.sm_miners.get(miner_id, MinerData.TILEY.ordinal());
               int view_dir = arena.sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
               // TODO: 27.03.23 I'm not checking here if flow map is occupied, lets see what that means
               int particle_id = arena.spawn_particle(miner_tx + Config.CONF.BRAIDED_CAST_OFFSET.value * Util.fourdirx[view_dir], miner_ty + Config.CONF.BRAIDED_CAST_OFFSET.value * Util.fourdiry[view_dir], Particles.TYPE_BRAIDED_RIVER);
               arena.sm_particles.set(particle_id, Particles.ANGLE.ordinal(), view_dir);
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

               int side_offset = 1;
               // 1 = above or right,
               // -1 = left or below
               boolean vert = false;

               switch (view_dir)
               {
                  case 0: // HORIZONTAL ABOVE
                     side_offset = 1;
                     break;
                  case 2: // HORIZONTAL BELOW
                     side_offset = -1;
                     break;
                  case 1: // VERTICAL RIGHT
                     vert = true;
                     side_offset = 1;
                     break;
                  case 3: // VERTICAL LEFT
                     vert = true;
                     side_offset = -1;
                     break;
               }
               // use info from switch statement for placement
               if (vert)
               {
                  for (int iy = 0; iy < Config.CONF.OROGENY_WIDTH.value; iy++)
                  {
                     for (int ix = 0; ix < Config.CONF.OROGENY_HEIGHT.value; ix++)
                     {
                        // the particles itself creates debris particles and then after its lifetime
                        arena.spawn_particle(miner_tx + ix - (Config.CONF.OROGENY_HEIGHT.value / 2) + Config.CONF.OROGENY_OFFSET_FROM_CASTER.value * side_offset, miner_ty + iy - (Config.CONF.OROGENY_WIDTH.value / 2), Particles.TYPE_OROGEN);
                     }
                  }
               } else
               {
                  for (int ix = 0; ix < Config.CONF.OROGENY_WIDTH.value; ix++)
                  {
                     for (int iy = 0; iy < Config.CONF.OROGENY_HEIGHT.value; iy++)
                     {
                        arena.spawn_particle(miner_tx + ix - (Config.CONF.OROGENY_WIDTH.value / 2), miner_ty + Config.CONF.OROGENY_OFFSET_FROM_CASTER.value * side_offset + iy - (Config.CONF.OROGENY_HEIGHT.value / 2), Particles.TYPE_OROGEN);
                     }
                  }
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
                  // TODO: 07.03.23 since the fault is between tiles check the offsets, one must be bigger!
                  xy_offset = miner_ty + (view_dir == 0 ? 3 : -3);
               } else if (view_dir == 1 || view_dir == 3)
               {
                  tf_variant = 2;
                  xy_offset = miner_tx + (view_dir == 1 ? 3 : -3);
               }
               arena.spawn_particle(tf_variant, xy_offset, Particles.TYPE_TRANSFORM_FAULT);
            }
            break;
         }
      }
   }

   private static void move(Arena arena, Smartrix smx_miner, int line, int dir_move)
   {
      // reading and writing directly from and into the matrix that holds the
      // miner data

      // dir_move is same as dir_move ?? all the time?

      int next_tx = smx_miner.get(line, MinerData.TILEX.ordinal()) + Util.fourdirx[dir_move];
      int next_ty = smx_miner.get(line, MinerData.TILEY.ordinal()) + Util.fourdiry[dir_move];

      // set miner dir, even if not moving
      smx_miner.set(line, MinerData.VIEWDIR.ordinal(), dir_move);

      if (arena.CHECK_free_tile(next_tx, next_ty))
      {
         if (next_tx >= 0 && next_tx < Arena.arena_size && next_ty >= 0 && next_ty < Arena.arena_size)
         {
            smx_miner.set(line, MinerData.TILEX.ordinal(), next_tx);
            smx_miner.set(line, MinerData.TILEY.ordinal(), next_ty);

            // TODO: 29.03.23 here I touch a new tile and existing gravel may do damage!
            if(arena.gravel.get(next_tx, next_ty) == 1){
               arena.deal_damage_to_miner(line, Config.CONF.ALLUVIAL_FAN_STEP_DAMAGE.value);
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
      PREUSSER(MinerAttack.SIMPLE_HIT, MinerAttack.ROUND_SWING, MinerAttack.BRAIDED_RIVER, MinerAttack.ALLUVIAL_FAN, null),

      ;

      public final MinerAttack primary, secondary, special_offensive, special_defensive, special_special;

      MinerClass(MinerAttack primary, MinerAttack secondary, MinerAttack offensive, MinerAttack defensive, MinerAttack special)
      {
         this.primary = primary;
         this.secondary = secondary;
         this.special_offensive = offensive;
         this.special_defensive = defensive;
         this.special_special = special;
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