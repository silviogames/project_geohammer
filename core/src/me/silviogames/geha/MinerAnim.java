package me.silviogames.geha;

public enum MinerAnim
{
   IDLE(Anim.MINER_IDLE, false, false),
   MOVE(Anim.MINER_RUN, true, true),
   SLIDE(Anim.MINER_SLIDE, true, true),
   SIMPLE_ATTACK(Anim.MINER_HAMMER, true, false),
   CAST_ATTACK(Anim.MINER_CAST, true, false),
   SPIN_ON_SPOT(Anim.MINER_SPIN, false, false),
   SPIN_MOVING(Anim.MINER_SPIN, true, true),
   ;

   MinerAnim(Anim anim, boolean si, boolean offmin)
   {
      this.anim = anim;
      this.supress_input = si;
      this.offsetting_miner = offmin;
   }

   public final Anim anim;

   // FOR NOW THIS WILL DO TO PREVENT INPUT WHILE AN ANIMATION IS PLAYING
   public final boolean supress_input;
   public final boolean offsetting_miner;

   public static MinerAnim safe_ord(int ord)
   {
      if (ord < 0 || ord >= values().length)
      {
         return IDLE;
      } else
      {
         return values()[ord];
      }
   }

   public MinerAnim anim_over(Arena arena, Smartrix sm_miners, int miner_id)
   {
      // return which anim follows after this anim, right now it is only used to trigger
      // the next
      float spin_time = Util.INT_TO_FLOAT(sm_miners.get(miner_id, MinerData.TIME_SPIN_EFFECT.ordinal()));
      MinerAnim ret = spin_time > 0 ? SPIN_ON_SPOT : IDLE;
      // for example if walk animation is over the miner must be moved to the other tile
      switch (this)
      {
         case MOVE:
         case SLIDE:
         case SPIN_MOVING:
         {
            // checking the next tile if it also is an ice tile or blocked
            int viewdir = sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
            int next_tx = sm_miners.get(miner_id, MinerData.TILEX.ordinal()) + Util.fourdirx[viewdir];
            int next_ty = sm_miners.get(miner_id, MinerData.TILEY.ordinal()) + Util.fourdiry[viewdir];

            if ((!arena.CHECK_free_tile(next_tx, next_ty) || !arena.CHECK_bounds(next_tx, next_ty)) && this == SLIDE)
            {
               int slide_count = sm_miners.get(miner_id, MinerData.ANIM_STATE_1.ordinal());
               if (slide_count >= 3)
               {
                  arena.deal_damage_to_miner(miner_id, 30 * (slide_count > 6 ? 2 : 1));
               }
            }

            if (arena.CHECK_free_tile(next_tx, next_ty) && arena.CHECK_bounds(next_tx, next_ty) && arena.ice.get(next_tx, next_ty) == 1)
            {
               sm_miners.incr(miner_id, MinerData.ANIM_STATE_1.ordinal(), 1);
               ret = SLIDE;
            }

            // this move is just finished the current move, it does not mean another slide.
            Miner.move_miner(arena, sm_miners, miner_id, viewdir);
         }
         break;
      }
      if (this == SLIDE && ret == IDLE)
      {
         // slide is over, reset the slider counter
         sm_miners.set(miner_id, MinerData.ANIM_STATE_1.ordinal(), 0);
      }

      return ret;
   }

   public void key_frame(Arena arena, Smartrix sm_miners, int miner_id)
   {
      switch (this)
      {
         case SIMPLE_ATTACK:
         {
            Miner.basic_attack(arena, sm_miners, miner_id);
         }
         break;
         case CAST_ATTACK:
         {
            Miner.perform_special_attack(arena, sm_miners, miner_id, sm_miners.get(miner_id, MinerData.CAST.ordinal()));
         }
         break;
      }
   }
}