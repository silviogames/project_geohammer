package me.silviogames.geha;


// A SMART 2D MATRIX OF INTEGERS saved as 1D expandable array
// similar to FlatByte but only the columns are fixed size, the rows can be expanded

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.StringBuilder;

public class Smartrix
{
   public final int width;
   final int def_value;

   // clear value is the value that is assigned to the first integer in a row after clearing,
   // this helps to find empty entries for refill

   final int clear_value;
   public IntArray data;

   // this list holds lines that have been cleared for quick reuse
   private final IntArray list_empty = new IntArray();

   public Smartrix(int width, int default_value, int clear_value)
   {
      this.width = width;
      this.def_value = default_value;
      this.clear_value = clear_value;
      // width * 10 is the capacity of the internal array but when creating a new smartrix it is empty.
      data = new IntArray(width * 10);
   }

   public int get(int line, int offset)
   {
      return data.get((line * width) + offset);
   }

   public int get_set(int line, int offset, int set)
   {
      int ret = data.get((line * width) + offset);
      data.set((line * width) + offset, set);
      return ret;
   }

   public int[] get_line(int line)
   {
      // produces garbage! use with caution
      int[] r = new int[width];
      for (int i = 0; i < width; i++)
      {
         r[i] = get(line, i);
      }
      return r;
   }

   public void get_line(int line, int[] data)
   {
      // this expects an array so it is reused
      for (int i = 0; i < width; i++)
      {
         data[i] = get(line, i);
      }
   }

   public void set_line(int line, float place_holder, int... data_line)
   {
      // replace line with data of array
      if (data_line == null || data_line.length > width)
      {
         System.out.println("[SMARTRIX] cannot set_line( int[] data_line) invalid length of data_line");
      } else
      {
         if (line >= num_lines())
         {
            System.out.println("[SMARTRIX] cannot set_line( int[] data_line) line out of bounds");
         } else
         {
            for (int i = 0; i < data_line.length; i++)
            {
               // will only replace the data that is contained in data_line
               set(line, i, data_line[i]);
            }
         }
      }
   }

   public void add_line(int... data_line)
   {
      if (data_line == null)
      {
         System.out.println("[SMARTRIX] add_line: cannot add line with empty!");
      } else
      {
         // if data_line is too long, the end is ignored
         // if data_line is too short, rest is filled with def_value
         for (int i = 0; i < width; i++)
         {
            if (i < data_line.length)
            {
               data.add(data_line[i]);
            } else
            {
               data.add(def_value);
            }
         }
      }
   }

   public void append(Smartrix other)
   {
      if (other != null && other.width <= this.width)
      {
         for (int i = 0; i < other.num_lines(); i++)
         {
            add_line(other.get_line(i));
         }
      } else
      {
         System.out.println("[SMARTRIX] ERROR cannot append other smx!");
      }
   }

   public void clear_line(int line)
   {
      set(line, 0, clear_value);
      list_empty.add(line);
   }

   public void remove_line(int line)
   {
      // this actually removes values from the backing array, which should not be used in heavy use,
      // long smartrix, since it array copies under the hood
      // in this project it is used for controllers
      data.removeRange(line * width, line * width + width - 1);
   }

   public void clear()
   {
      data.clear();
   }

   public void clear_all_lines()
   {
      // clearing all lines with line reuse
      for (int i = 0; i < num_lines(); i++)
      {
         clear_line(i);
      }
   }

   public int num_lines()
   {
      return data.size / width;
   }

   public void set(int line, int offset, int val)
   {
      data.set((line * width) + offset, val);
   }

   public void multi_set(int line, int... offsets_and_values)
   {
      // can be used to write to multiple values in one line, but maybe it is not as readable anymore!
      // but in theory it could be used to do something like:
      // sm_miner.multiset(miner_id, MinerData.LIFE.ordinal(), 10, MinerData.Blink.ordinal(), 30);
      // the offsets_and_values is constructed by a list pairs of
      // offset and values

      if (offsets_and_values == null)
      {
         System.out.println("Smartrix.multi_set failed, offset_and_values is null");
         return;
      }
      if (offsets_and_values.length % 2 != 0)
      {
         System.out.println("Smartrix.multi_set failed, offset_and_values is wrong size, modulo 2!");
         return;
      }
      for (int i = 0; i < offsets_and_values.length; i += 2)
      {
         set(line, offsets_and_values[i], offsets_and_values[i + 1]);
      }
   }

   public void sort(int sorting_index, boolean ascending)
   {
      quickSort(0, num_lines() - 1, sorting_index, ascending);
   }

   public void quickSort(int low, int high, int sorting_index, boolean ascending)
   {
      if (low < high)
      {
         int pivotIndex = partition(low, high, sorting_index, ascending);
         quickSort(low, pivotIndex - 1, sorting_index, ascending);
         quickSort(pivotIndex + 1, high, sorting_index, ascending);
      }
   }

   public int partition(int low, int high, int sorting_index, boolean ascending)
   {
      int pivot = get(high, sorting_index);
      int i = low - 1;
      for (int j = low; j < high; j++)
      {
         if ((get(j, sorting_index) < pivot && ascending) || (get(j, sorting_index) > pivot && !ascending))
         {
            i++;
            swap_lines(i, j);
         }
      }
      swap_lines(i + 1, high);
      return i + 1;
   }

   public void swap_lines(int first, int second)
   {
      if (first >= num_lines() || second >= num_lines() || first < 0 || second < 0)
      {
         return;
      } else
      {
         int[] temp_line = get_line(second);
         set_line(second, 0f, get_line(first));
         set_line(first, 0f, temp_line);
      }
   }

   public void print_to_console(String intro)
   {
      // may spam console if large!
      System.out.println("______ " + intro + " _____");
      // DEBUG PRINT
      if (num_lines() == 0)
      {
         System.out.println("[SMARTRIX] is empty");
      } else
      {
         for (int i = 0; i < num_lines(); i++)
         {
            System.out.println("smartrix line [" + i + "]");
            for (int j = 0; j < width; j++)
            {
               System.out.print(get(i, j) + ",");
            }
            System.out.print("\n");
            System.out.println("++++++++++++++++++++");
         }
      }
   }

   public void incr_all_lines(int offset, int change)
   {
      for (int i = 0; i < num_lines(); i++)
      {
         incr(i, offset, change);
      }
   }

   public void incr(int line, int offset, int change)
   {
      data.incr((line * width) + offset, change);
   }

   public void print_to_csv(String file_location_and_name, boolean append)
   {
      FileHandle file = Gdx.files.local(file_location_and_name + ".csv");
      StringBuilder sb = new StringBuilder();
      int lines = data.size / width;
      for (int i = 0; i < lines; i++)
      {
         for (int j = 0; j < width; j++)
         {
            sb.append(get(i, j));
            sb.append(",");
         }
         sb.append("\n");
      }
      file.writeString(sb.toString(), append);
   }

   public int find_free_line_index()
   {
      if (list_empty.size > 0)
      {
         return list_empty.pop();
      }

      // this manual searching is actually only needed when a cleanup happens without calling clean_line
      // and I think I checked all occurrences where lines are cleared

      int found_index = -1;
      // seeks through smartix for first line that begins with the free_value
      for (int i = 0; i < num_lines(); i++)
      {
         if (get(i, 0) == clear_value)
         {
            found_index = i;
            break;
         }
      }
      if (found_index == -1)
      {
         // all lines seem full, add new and return that index
         found_index = num_lines();
         add_line();
      }

      return found_index;
   }

   public void read_from_csv(String file_location_and_name)
   {
      FileHandle file = Gdx.files.local(file_location_and_name + ".csv");
      if (file.exists())
      {
         String[] line_data;
         int[] temp_int_data = new int[width];
         String[] lines = file.readString().split("\n");
         for (int i = 0; i < lines.length; i++)
         {
            line_data = lines[i].split(",");
            for (int j = 0; j < width; j++)
            {
               if (j >= line_data.length)
               {
                  // if load is too short (should not happen!)
                  temp_int_data[j] = def_value;
               } else
               {
                  temp_int_data[j] = Integer.parseInt(line_data[j]);
               }
            }
            add_line(temp_int_data);
         }
      } else
      {
         System.out.println("[SMARTRIX] cannot read, file " + file_location_and_name + " does not exist!");
      }
   }

   public boolean check_line(int line)
   {
      // return true if this line exists
      if (line < num_lines())
      {
         return get(line, 0) != clear_value;
      }
      return false;
   }
}