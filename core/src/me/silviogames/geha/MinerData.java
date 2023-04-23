package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

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

   BLINK // used to display hit frames
   ,
   ANIM,
   ANIM_TIME, // float saved as int
   // these states may be different depending on the current anim, maybe later I will find out
   // that I actually do not need so many data
   ANIM_STATE_1,
   ANIM_STATE_2,
   ANIM_STATE_3,
   ANIM_STATE_4,
   ANIM_STATE_5,
   ;

   final static Color color_box_back = Color.DARK_GRAY.cpy();
   final static Color color_white_transparent = Color.WHITE.cpy();
   public static int global_max_stamina = 100;

   static
   {
      color_box_back.a = 0.6f;
      color_white_transparent.a = 0.3f;
   }

   public static void render_miner(Smartrix sm_miners, int miner_id)
   {
      int minerposx = sm_miners.get(miner_id, MinerData.TILEX.ordinal());
      int minerposy = sm_miners.get(miner_id, MinerData.TILEY.ordinal());
      int miner_viewdir = sm_miners.get(miner_id, MinerData.VIEWDIR.ordinal());

      if (sm_miners.get(miner_id, MinerData.ACTIVE.ordinal()) == 1)
      {
         float time_blink = Util.INT_TO_FLOAT(sm_miners.get(miner_id, MinerData.BLINK.ordinal()));
         boolean blink = time_blink > 0f && MathUtils.floor(time_blink / 0.07f) % 2 == 0;

         Main.batch.setColor(blink ? RenderUtil.miner_colors_trans[miner_id] : RenderUtil.miner_colors[miner_id]);

         MinerAnim ma = MinerAnim.safe_ord(sm_miners.get(miner_id, MinerData.ANIM.ordinal()));
         float anim_time = Util.INT_TO_FLOAT(sm_miners.get(miner_id, MinerData.ANIM_TIME.ordinal()));

         float move_offset_x = 0f;
         float move_offset_y = 0f;
         float frac_time = ma.anim.get_fractional_time(anim_time);
         float jump_arc_y = 0;

         if (ma.offsetting_miner)
         {
            jump_arc_y = RenderUtil.arc(frac_time);
            switch (miner_viewdir)
            {
               case 0:
                  move_offset_y = frac_time * 16;
                  break;
               case 1:
                  move_offset_x = frac_time * 16;
                  break;
               case 2:
                  move_offset_y = frac_time * -16;
                  break;
               case 3:
                  move_offset_x = frac_time * -16;
                  break;
            }
         }

         Main.batch.draw(Res.get_frame(anim_time, ma.anim, miner_viewdir == 3), 16 * minerposx - 16 + 8 + move_offset_x, 16 * minerposy + 8 + move_offset_y + jump_arc_y * Config.CONF.MINER_JUMP_HEIGHT.value);

         // direction arrow (are only needed on alive miners)
         int ud, lr;
         lr = Util.fourdirx[miner_viewdir];
         ud = Util.fourdiry[miner_viewdir];

         if (Config.CONF.RENDER_DIRECTION_ARROWS.value == 1 && sm_miners.get(miner_id, MinerData.ACTIVE.ordinal()) == 1)
         {
            Main.batch.setColor(RenderUtil.miner_colors_trans[miner_id]);
            Main.batch.draw(Res.DIRECTIONS.sheet[miner_viewdir], 16 * minerposx + (lr * (24 + Arena.osc_arrow.value())) + move_offset_x, 16 * minerposy + (ud * (24 + Arena.osc_arrow.value())) + 8 + move_offset_y);
         }
      } else
      {
         // DEAD
         Main.batch.setColor(RenderUtil.miner_colors[miner_id]);
         Main.batch.draw(Res.GUY.sheet[0], 16 * minerposx - 16 + 8, 16 * minerposy + 8, Res.GUY.sheet_width / 2f, Res.GUY.sheet_height / 2f, Res.GUY.sheet_width, Res.GUY.sheet_height, 0.75f, 0.75f, 90);
      }


      Main.batch.setColor(Color.WHITE);
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

      Color color_box = RenderUtil.miner_colors[miner_id].cpy();
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