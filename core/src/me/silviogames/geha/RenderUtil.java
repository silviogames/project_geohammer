package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class RenderUtil
{

   // HELPER CLASS FOR RENDERING STUFF LIKE BOXES

   public static final Color color_trans_gray = Color.LIGHT_GRAY.cpy();

   public final static Color[] miner_colors = new Color[4];

   public final static Color[] miner_colors_trans = new Color[4];

   static
   {
      color_trans_gray.a = 0.5f;

      miner_colors[0] = Color.SALMON.cpy();
      miner_colors[1] = Color.SKY.cpy();
      miner_colors[2] = Color.OLIVE.cpy();
      miner_colors[3] = Color.GOLD.cpy();

      miner_colors_trans[0] = Color.SALMON.cpy();
      miner_colors_trans[0].a = 0.5f;
      miner_colors_trans[1] = Color.SKY.cpy();
      miner_colors_trans[1].a = 0.5f;
      miner_colors_trans[2] = Color.OLIVE.cpy();
      miner_colors_trans[2].a = 0.5f;
      miner_colors_trans[3] = Color.GOLD.cpy();
      miner_colors_trans[3].a = 0.5f;
   }

   public static void render_box(int posx, int posy, int width, int height)
   {
      render_box(posx, posy, width, height, Color.WHITE);
   }

   public static void render_box(int posx, int posy, int width, int height, Color color)
   {
      Main.batch.setColor(color);
      Main.batch.draw(Res.pixel, posx, posy, width, height);
      Main.batch.setColor(Color.WHITE);
   }

   public static void render_bar(int posx, int posy, int width, int height, Color cempty, Color cfull, float fill)
   {
      fill = MathUtils.clamp(fill, 0f, 1f);
      render_box(posx, posy, width, height, cempty);
      render_box(posx, posy, MathUtils.clamp(MathUtils.round(width * fill), 0, width), height, cfull);
   }

   public static String time_to_display(float game_time)
   {
      int seconds = MathUtils.round(game_time % 60f);
      int minutes = MathUtils.floor(game_time / 60f);

      return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
   }

   public static int anim_time_to_frame(float time, float time_max, int num_frames)
   {
      // little helper function to transform from a float that represents time of the animation to frame
     float frame_float = MathUtils.map(0f, time_max, 0, num_frames, time);
     return MathUtils.clamp(MathUtils.floor(frame_float), 0, num_frames-1);
   }
}
