package me.silviogames.geha;

public enum MinerAnim
{
   IDLE(Anim.MINER_IDLE, false, false),
   MOVE(Anim.MINER_RUN, true, true),
   SLIDE(Anim.MINER_SLIDE, true, true),
   SIMPLE_ATTACK(Anim.MINER_HAMMER, true, false),
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
      MinerAnim ret = IDLE;
      // for example if walk animation is over the miner must be moved to the other tile
      switch (this)
      {
         case MOVE:
         case SLIDE:
         {
            // checking the next tile if it also is an ice tile or blocked
            int viewdir = sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());
            int next_tx = sm_miners.get(miner_id, MinerData.TILEX.ordinal()) + Util.fourdirx[viewdir];
            int next_ty = sm_miners.get(miner_id, MinerData.TILEY.ordinal()) + Util.fourdiry[viewdir];

            if ((!arena.CHECK_free_tile(next_tx, next_ty) || !arena.CHECK_bounds(next_tx, next_ty)) && this == SLIDE)
            {
               arena.deal_damage_to_miner(miner_id, 30);
            }

            if (arena.CHECK_free_tile(next_tx, next_ty) && arena.CHECK_bounds(next_tx, next_ty) && arena.ice.get(next_tx, next_ty) == 1)
            {
               ret = SLIDE;
            }

            // this move is just finished the current move, it does not mean another slide.
            Miner.move(arena, sm_miners, miner_id, viewdir);
         }
         break;
      }
      return ret;
   }

   public void key_frame(Arena arena, Smartrix sm_miners, int miner_id)
   {
      switch (this)
      {
         case SIMPLE_ATTACK:
            Miner.basic_attack(arena, sm_miners, miner_id);
            break;
      }
   }

   public static void handle_input()
   {
      // TODO: 06.04.23 should the inputs be passed here and handled depending on the current anim?
      // but most inputs are possible in IDLE anim,
      // maybe I will later allow something during another anim.

   }
}
