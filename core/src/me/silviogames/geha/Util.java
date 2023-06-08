package me.silviogames.geha;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.IntArray;

public class Util
{
   // useful methods for all needs

   public static byte[] fourdirx = new byte[]{0, 1, 0, -1};
   public static byte[] fourdiry = new byte[]{1, 0, -1, 0};

   public static boolean[] viewdir_horizontal = new boolean[]{false, true, false, true};

   public static byte[] eightdirx = new byte[]{-1, 0, 1, -1, 1, -1, 0, 1};
   public static byte[] eightdiry = new byte[]{1, 1, 1, 0, 0, -1, -1, -1};

   public static int[] spawn_pos_x = new int[]{0, 1, 0, 1};
   public static int[] spawn_pos_y = new int[]{0, 1, 1, 0};

   public static IntArray RECT_xpos = new IntArray(), RECT_ypos = new IntArray();
   // the RECT_growth array has the calculated growth for a rect per block
   // this is right now only used by the orogeny skill to have a difference in growth state
   // when spawning the orogeny blocks so the growth is further in the middle of the orogene
   public static ByteArray RECT_growth = new ByteArray();

   // I could technically just reuse the other static IntArrays but for clarity I will not!
   public static IntArray CIRCLE_xpos = new IntArray(), CIRCLE_ypos = new IntArray();
   public static IntArray random_circle_indices = new IntArray();

   public static Smartrix TORUS_pos_source = new Smartrix(3, -1, -1);
   public static Smartrix TORUS_pos_target = new Smartrix(3, -1, -1);

   // THIS MAY BE USED BY VARIOUS FUNCTIONS BUT NEED TO CLEAR BEFORE AND AFTER!
   public static IntArray temp_random_miner = new IntArray();

   public static int euclid_norm(int x1, int y1, int x2, int y2)
   {
      int dx = (x1 - x2) * (x1 - x2);
      int dy = (y1 - y2) * (y1 - y2);
      return MathUtils.round((float) Math.sqrt((double) dx + dy));
   }

   public static int[] get_spawn(int miner_id)
   {
      // miner_id is expected to be between in interval [0...3]
      int[] ret = new int[2];
      ret[0] = spawn_pos_x[miner_id] == 0 ? 1 : Arena.arena_width - 2;
      ret[1] = spawn_pos_y[miner_id] == 0 ? 1 : Arena.arena_height - 2;
      return ret;
   }

   public static int[] RANDOM_RADIAL_OFFSET(int radius)
   {
      // THIS FILLES THE RANDOM CIRCLE, NOT ONLY ON THE CIRCLE!
      int[] r = new int[2];
      int angle = MathUtils.random(0, 359);
      float rand_radius = MathUtils.random(0f, radius);
      r[0] = (int) (MathUtils.cosDeg(angle) * rand_radius);
      r[1] = (int) (MathUtils.sinDeg(angle) * rand_radius);
      return r;
   }

   public static int FLOAT_TO_INT(float value_in)
   {
      // assuming 3 digits for float are wanted
      // this should be used for times that are saved as ints in arrays of ints
      return (int) (value_in * 1000);
   }

   public static float INT_TO_FLOAT(int value_in)
   {
      // assuming 3 digits for float are wanted
      // this should be used for times that are saved as ints in arrays of ints
      return ((float) value_in) / 1000f;
   }

   public static int wrapped_increment(int value, int change, int min, int max)
   {
      // max is included!
      int next_value = value + change;
      if (next_value > max) next_value = min;
      if (next_value < min) next_value = max;
      return next_value;
   }

   public static float PERCENTAGE_TO_FRAC(int percent)
   {
      return ((float) percent) / 100f;
   }

   public static int simple_dist(int x1, int y1, int x2, int y2)
   {
      // norm without taking the square root
      int dx = (x1 - x2) * (x1 - x2);
      int dy = (y1 - y2) * (y1 - y2);
      return dx + dy;
   }

   public static void GEN_CIRCLE_POSITIONS(int mid_tilex, int mid_tiley, int radius)
   {
      CIRCLE_xpos.clear();
      CIRCLE_ypos.clear();

      // I don't want even an radius
      if (radius % 2 == 0) radius++;

      int ox, oy;
      for (int ix = -radius; ix <= radius; ix++)
      {
         for (int iy = -radius; iy <= radius; iy++)
         {
            ox = mid_tilex + ix;
            oy = mid_tiley + iy;
            if (simple_dist(mid_tilex, mid_tiley, ox, oy) <= (radius * radius))
            {
               CIRCLE_xpos.add(ox);
               CIRCLE_ypos.add(oy);
            }
         }
      }
   }

   public static void GEN_TORUS_POSITIONS(int mid_tilex, int mid_tiley, int min_radius, int max_radius, boolean target)
   {
      Smartrix local_temp;
      if (target)
      {
         local_temp = TORUS_pos_target;
      } else
      {
         local_temp = TORUS_pos_source;
      }
      local_temp.clear();

      // I don't want even an radius
      //if (min_radius % 2 == 0) min_radius--;
      //if (max_radius % 2 == 0) max_radius++;
      int ox, oy, dst;
      for (int ix = -max_radius; ix <= max_radius; ix++)
      {
         for (int iy = -max_radius; iy <= max_radius; iy++)
         {
            ox = mid_tilex + ix;
            oy = mid_tiley + iy;

            dst = simple_dist(mid_tilex, mid_tiley, ox, oy);
            if (dst <= (max_radius * max_radius) && dst >= (min_radius * min_radius))
            {
               local_temp.add_line(ox, oy, dst);
            }
         }
      }
      local_temp.sort(2, true);
   }

   public static void GEN_RECT_POSITIONS(int tilex, int tiley, int offset, int viewdir, int width, int height)
   {
      RECT_xpos.clear();
      RECT_ypos.clear();
      RECT_growth.clear();

      int side_offset = 0;
      // 1 = above or right,
      // -1 = left or below
      boolean vert = false;


      switch (viewdir)
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
      int max_dist = 0;

      if (vert)
      {
         int mid_tilex = tilex + offset * side_offset;
         for (int iy = 0; iy < width; iy++)
         {
            for (int ix = 0; ix < height; ix++)
            {
               int place_x = tilex + ix - (height / 2) + offset * side_offset;
               int place_y = tiley + iy - (width / 2);
               RECT_xpos.add(place_x);
               RECT_ypos.add(place_y);
               int dist = Math.abs(place_x - mid_tilex);
               if (dist > max_dist) max_dist = dist;
               // the distance must not overflow byte!!!
               RECT_growth.add((byte) dist);
            }
         }
      } else
      {
         int mid_tiley = tiley + offset * side_offset;
         for (int ix = 0; ix < width; ix++)
         {
            for (int iy = 0; iy < height; iy++)
            {
               int place_x = tilex + ix - (width / 2);
               int place_y = tiley + offset * side_offset + iy - (height / 2);
               RECT_xpos.add(place_x);
               RECT_ypos.add(place_y);
               int dist = Math.abs(place_y - mid_tiley);
               if (dist > max_dist) max_dist = dist;
               // the distance must not overflow byte!!!
               RECT_growth.add((byte) dist);
            }
         }
      }

      // now I know all the distances to the middle.
      // and I know the biggest distance to the middle and can use that to norm all other to 4
      byte wanted_max = 12;
      for (int i = 0; i < RECT_growth.size; i++)
      {
         byte normed = (byte) (-MathUtils.round(MathUtils.map(0, max_dist, 0, wanted_max, RECT_growth.get(i))));
         RECT_growth.set(i, normed);

      }
   }
}