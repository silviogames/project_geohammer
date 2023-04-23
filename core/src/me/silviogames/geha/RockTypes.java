package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;

public enum RockTypes
{
   SANDSTONE( Color.WHITE.cpy(), 1),
   GRANITE( Color.LIGHT_GRAY.cpy(), 2),
   GABBRO( Color.GRAY.cpy(), 3),
   ;

   public final int life_multiplier;
   public Color color;

   RockTypes( Color c, int lm)
   {
      this.color = c;
      this.life_multiplier = lm;
   }

   public int crystal_chance_from_id(int rock_id)
   {
      // the rock_id are the ordinals in this enum.
      // -1 is a free tile

      if (rock_id < 0 || rock_id >= values().length)
      {
         return 0;
      } else
      {
         RockTypes rt = RockTypes.values()[rock_id];
         return rt.crystal_chance();
      }
   }

   public static RockTypes safe_ord(int ord)
   {
      if (ord < 0 || ord >= values().length)
      {
         return null;
      } else
      {
         return values()[ord];
      }
   }

   public int crystal_chance()
   {
      int r = 50;
      // reading from config
      switch (this)
      {
         case SANDSTONE:
            r = Config.CONF.ROCK_DROP_CHANCE_SANDSTONE.value;
            break;
         case GRANITE:
            r = Config.CONF.ROCK_DROP_CHANCE_GRANITE.value;
            break;
         case GABBRO:
            r = Config.CONF.ROCK_DROP_CHANCE_GABBRO.value;
            break;
      }
      return r;
   }

   public static RockTypes generate_from_noise(float noise)
   {
      if (noise <= Util.PERCENTAGE_TO_FRAC(Config.CONF.ROCK_SPAWN_CHANCE_SANDSTONE.value))
      {
         return SANDSTONE;
      } else if (noise <= Util.PERCENTAGE_TO_FRAC(Config.CONF.ROCK_SPAWN_CHANCE_GRANITE.value))
      {
         return GRANITE;
      } else
      {
         return GABBRO;
      }
   }
}