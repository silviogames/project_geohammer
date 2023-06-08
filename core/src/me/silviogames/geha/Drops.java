package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntArray;

public enum Drops
{
   CRYSTAL(Color.WHITE, 92, 1),// used for special attacks,
   LIFE(Color.SCARLET, 3, 3),
   POWER(Color.GOLD, 1, 5), // makes user stronger for some time
   SPIN(Color.SKY, 2, 4), // spin attack
   ;

   Drops(Color c, int chance, int tmv)
   {
      this.color = c;
      this.tschermak_merge_value = tmv;

      this.chance = chance;
      // THE PERCENTAGES REFER TO THE CHANCE OF THE TYPE OF CRYSTAL.
      // THIS IS NOT THE TOTAL CHANCE THAT A BROKEN BLOCK WILL SPAWN THIS,
      // THE GENERAL CHANCE OF A BLOCK DROPPING SOMETHING IS DESCRIBED SOMEWHERE ELSE AND SAME FOR ALL TYPES
   }

   private static IntArray list_spawn = new IntArray();

   public final Color color;

   public final int chance;
   public final int tschermak_merge_value;

   public static void init_drops()
   {
      // populate the list_spawn chance
      for (Drops d : values())
      {
         for (int i = 0; i < d.chance; i++)
         {
            list_spawn.add(d.ordinal());
         }
      }
   }

   public static Drops safe_ord(int ord)
   {
      return (ord < 0 || ord >= values().length) ? null : values()[ord];
   }

   public static Drops spawn_random()
   {
      int index_rand = MathUtils.random(0, list_spawn.size - 1);
      return safe_ord(list_spawn.get(index_rand));
   }
}
