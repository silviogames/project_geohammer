package me.silviogames.geha;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Arrays;

public class Flatbyte implements Json.Serializable
{
   static IntArray rx = new IntArray(), ry = new IntArray();
   public byte[] values;
   public int width, length, height;
   private byte default_return, default_value;

   public Flatbyte(int width, int height, byte default_return, byte default_value)
   {
      values = new byte[width * height];
      this.width = width;
      this.height = height;
      this.length = width * height;
      this.default_return = default_return;
      this.default_value = default_value;
      if (default_value != 0) Arrays.fill(values, default_value);
   }

   public Flatbyte()
   {
      //json constructor
   }

   public Flatbyte(Flatbyte other)
   {
      // copy
      this.values = other.values.clone();
      this.width = other.width;
      this.height = other.height;
      this.length = width * height;
      this.default_return = other.default_return;
      this.default_value = other.default_value;
   }

   public byte get(int tilex, int tiley)
   {
      // this version will overflow to other row/column if tilex or tiley is out of bounds!
      int index = (tiley * width) + tilex;
      if (index < length && index >= 0)
      {
         return values[index];
      } else
      {
         return default_return;
      }
   }

   public byte get_set(int tilex, int tiley, byte set_val)
   {
      byte ret = get(tilex, tiley);
      set(tilex, tiley, set_val);
      return ret;
   }

   public byte get_raw(int index)
   {
      if (index < length && index >= 0)
      {
         return values[index];
      } else
      {
         return default_return;
      }
   }

   public byte get_boundchecked(int tilex, int tiley)
   {
      if (tilex < 0 || tilex >= width || tiley < 0 || tiley >= height)
      {
         return default_return;
      } else
      {
         int index = (tiley * width) + tilex;
         if (index < length && index >= 0)
         {
            return values[index];
         } else
         {
            return default_return;
         }
      }
   }

   public void set(int tilex, int tiley, byte value)
   {
      if (tilex < 0 || tiley < 0 || tilex >= width || tiley >= height)
      {
         return;
      }
      int index = (tiley * width) + tilex;
      if (index < length && index >= 0)
      {
         values[index] = value;
      }
   }

   public void incr(int tilex, int tiley, byte change)
   {
      int index = (tiley * width) + tilex;
      if (index < length && index >= 0)
      {
         values[index] += change;
      }
   }

   public int[] find_value(byte find)
   {
      // return pairs of grid positions of value 'find'
      IntArray found_x_pos = new IntArray();
      IntArray found_y_pos = new IntArray();

      for (int i = 0; i < width; i++)
      {
         for (int j = 0; j < height; j++)
         {
            if (get(i, j) == find)
            {
               found_x_pos.add(i);
               found_y_pos.add(j);
            }
         }
      }

      int[] ret = new int[found_x_pos.size * 2];
      for (int i = 0; i < found_x_pos.size; i++)
      {
         ret[i * 2] = found_x_pos.get(i);
         ret[i * 2 + 1] = found_y_pos.get(i);
      }
      return ret;
   }

   public void change_random_field(byte before, byte after)
   {
      // list all positions with value 'before' and change one of them to value 'after'
      rx.clear();
      ry.clear();
      // will have no effect if no before value has been found in the grid
      for (int ix = 0; ix < width; ix++)
      {
         for (int iy = 0; iy < height; iy++)
         {
            if (get(ix, iy) == before)
            {
               rx.add(ix);
               ry.add(iy);
            }
         }
      }
      if (rx.size > 0)
      {
         int random_index = MathUtils.random(0, rx.size - 1);
         set(rx.get(random_index), ry.get(random_index), after);
      }
   }

   public void change_random_neighbor(byte value_before, byte value_neighbor, byte value_after, float chance_random)
   {
      // change random adjacent field of certain value
      // used in puzzle state manipulation

      IntArray wx = new IntArray();
      IntArray wy = new IntArray();

      IntArray fx = new IntArray();
      IntArray fy = new IntArray();

      //int[] pos_goal = find_value( ( byte ) 3 );

      for (int ix = 0; ix < width; ix++)
      {
         for (int iy = 0; iy < height; iy++)
         {
            byte val = get(ix, iy);
            if (val == value_before)
            {
               //if ( Main.manhatten( ix, iy, pos_goal[ 0 ], pos_goal[ 1 ] ) < 4 )
               //{
               //	continue;
               //}

               int neigh = 0;
               for (int i = 0; i < 4; i++)
               {
                  int ox = ix + Util.fourdirx[i];
                  int oy = iy + Util.fourdiry[i];
                  if (get(ox, oy) == value_neighbor)
                  {
                     neigh += 1;
                  }
                  if (get(ox, oy) == (byte) 2)
                  {
                     neigh = -1;
                     break;
                  }
                  if (get(ox, oy) == (byte) 3)
                  {
                     neigh = -1;
                     break;
                  }
               }
               if (neigh == 0)
               {
                  // free tile
                  fx.add(ix);
                  fy.add(iy);
               } else
               {
                  // wall tile
                  for (int i = 0; i < neigh * 2; i++)
                  {
                     wx.add(ix);
                     wy.add(iy);
                  }
               }
            }
         }
      }
      if (MathUtils.randomBoolean(chance_random))
      {
         // take free tile
         if (fx.size > 0)
         {
            int rinx = MathUtils.random(0, fx.size - 1);
            set(fx.get(rinx), fy.get(rinx), value_after);
         }
      } else
      {
         // take wall tile
         if (wx.size > 0)
         {
            int rinx = MathUtils.random(0, wx.size - 1);
            set(wx.get(rinx), wy.get(rinx), value_after);
         }
      }
   }

   public void fill(int index, byte value)
   {
      //for direct fill from a load
      if (index > 0 && index < length)
      {
         values[index] = value;
      }
   }

   public void reset()
   {
      // set all values to the default value
      Arrays.fill(values, default_value);
   }

   public byte dump(int index)
   {
      //direct access to save
      if (index > 0 && index < length)
      {
         return values[index];
      } else
      {
         return default_return;
      }
   }

   public void fill(byte[] loaded_values)
   {
      for (int i = 0; i < loaded_values.length; i++)
      {
         fill(i, loaded_values[i]);
      }
   }

   @Override
   public void write(Json json)
   {
      json.writeValue("l", length);
      json.writeValue("w", width);
      json.writeValue("h", height);
      json.writeValue("defr", default_return);
      json.writeValue("defv", default_value);
//        json.writeValue( "v", values );
      json.writeArrayStart("v");
      for (int i = 0; i < values.length; i++)
      {
         json.writeValue(values[i]);
      }
      json.writeArrayEnd();

   }

   @Override
   public void read(Json json, JsonValue jsonData)
   {
      width = json.readValue("w", int.class, 1, jsonData);
      height = json.readValue("h", int.class, 1, jsonData);
      length = json.readValue("l", int.class, 1, jsonData);
      values = json.readValue("v", byte[].class, new byte[length], jsonData);
      default_return = json.readValue("defr", byte.class, (byte) -1, jsonData);
      default_value = json.readValue("defv", byte.class, (byte) -1, jsonData);
   }

   public void debug_print()
   {
      System.out.println("debug print of FlatByte");
      for (int iy = 0; iy < height; iy++)
      {
         System.out.print("Y [" + iy + "] ");
         for (int ix = 0; ix < width; ix++)
         {
            System.out.print(get(ix, iy) + " | ");
         }
         System.out.println(" ");
      }
   }
}
