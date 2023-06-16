package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Config
{
   public static void load_config(boolean verbose)
   {
      if (verbose) System.out.println("LOADING CONFIG");
      // loads and applies config

      // number of successful assignments
      int assigned_values = 0;

      FileHandle file = Gdx.files.internal("config.txt");
      String[] lines = file.readString().split("\n");
      for (String line : lines)
      {
         line = line.trim();
         String[] split_line = line.split(":");
         if (split_line.length != 2)
         {
            if (verbose) System.out.println("config line #" + line + "# invalid, skipped");
            continue;
         }
         int val = -1;
         try
         {
            val = Integer.parseInt(split_line[1]);
         } catch (Exception e)
         {
            if (verbose)
               System.out.println("config line #" + line + "# has no number after colon, skipped");
            continue;
         }
         for (CONF conf : CONF.values())
         {
            if (conf.toString().equals(split_line[0]))
            {
               conf.value = val;
               assigned_values++;
               break;
            }
         }
      }
      if (verbose)
      {
         System.out.println("CONFIG LOADING SUMMARY:");
         System.out.println("number of config values: " + CONF.values().length);
         System.out.println("number of loaded config lines: " + lines.length);
         System.out.println("number of assignments: " + assigned_values);
      }
   }

   public static void print_config()
   {
      // PRINTING CONF TO CONSOLE FOR TESTING
      for (CONF conf : CONF.values())
      {
         System.out.println("CONF:" + conf.toString() + " : " + conf.value);
      }
   }

   public enum CONF
   {
      ARENA_ROCK_RADIUS,
      MINER_SIMPLE_ATTACK_DAMAGE,
      MINER_MAX_LIFE,
      MINER_LIFEGAIN_FROM_CRYSTAL,

      MINER_BLINK_MAX_TIME,

      MINER_BLOOD_PARTICLE_OFFSET_X,
      MINER_BLOOD_PARTICLE_OFFSET_Y,
      MINER_BLOOD_TIME_MS,

      MINER_JUMP_HEIGHT,
      MINER_SPIN_MAX_TIME_MS,
      MINER_SPIN_DAMAGE,

      STAMINA_COST_BASIC_ATTACK,
      STAMINA_COST_SPECIAL_ATTACK,
      STAMINA_REGAIN_PER_INTERVAL,
      RENDER_DIRECTION_ARROWS,
      STAMINA_SYSTEM,

      BLOCK_LIFE,
      CRYSTAL_MAX_POSSESSION,
      CRYSTAL_SKILL_COST,
      CRYSTAL_POWER_MAX_TIME_MS,
      CRYSTAL_OBTAIN_SPEED_MS,

      ROCK_DROP_CHANCE_SANDSTONE,
      ROCK_DROP_CHANCE_GRANITE,
      ROCK_DROP_CHANCE_GABBRO,

      ROCK_SPAWN_CHANCE_SANDSTONE,
      ROCK_SPAWN_CHANCE_GRANITE,
      ROCK_SPAWN_CHANCE_GABBRO,

      IMPACTOR_TARGET_DISTANCE,
      IMPACTOR_INITIAL_OFFSET,
      IMPACTOR_SMOKE_RADIAL_OFFSET,
      IMPACTOR_BLOCK_DAMAGE_RADIUS,
      IMPACTOR_PLAYER_DAMAGE,
      IMPACTOR_LIFETIME,
      IMPACTOR_DISPLAY_TEXT,

      OROGENY_HIT_DAMAGE,
      OROGENY_WIDTH,
      OROGENY_HEIGHT,
      OROGENY_OFFSET_FROM_CASTER,
      OROGENY_CREATION_TIME_MS,

      TRANSFORM_FAULT_EDGE_DAMAGE,
      TRANSFORM_FAULT_OFFSET,
      TRANSFORM_FAULT_DISPLACEMENT,
      TRANSFORM_FAULT_DELAY_MS,
      // THE TIME IT TAKES FOR THE DISPLACEMENT
      TRANSFORM_FAULT_SPEED_MS,

      BRAIDED_CAST_OFFSET,
      BRAIDED_PARTICLE_LIFETIME_MS,
      BRAIDED_PARTICLE_CHILD_DELAY_MS,
      BRAIDED_RANDOM_BRAIDING_BOOL,
      BRAIDED_RANDOM_BRAIDING_CHANCE_PERCENT,
      BRAIDED_DAMAGE,

      ALLUVIAL_FAN_CAST_DAMAGE, // this means a direct hit by the casted particles
      ALLUVIAL_FAN_STEP_DAMAGE,
      ALLUVIAL_FAN_CHILD_DELAY_MS,
      ALLUVIAL_FAN_TILE_RADIUS,

      EROSION_WIDTH,
      EROSION_POWER,
      EROSION_SPEED_MS,
      EROSION_CAST_OFFSET,
      EROSION_BASE_DAMAGE,

      GLACIER_OFFSET,
      GLACIER_WIDTH,
      GLACIER_SPEED,
      GLACIER_MAX_LIFE,
      GLACIER_MINER_DAMAGE,
      GLACIER_BLOCK_DAMAGE,

      MELT_OFFSET,
      MELT_RADIUS,
      MELT_DAMAGE,
      MELT_FREEZE_VALUE,

      TSCHERMAK_OFFSET,
      TSCHERMAK_MERGE_RADIUS,
      TSCHERMAK_TARGET_TIME_MS,
      TSCHERMAK_DAMAGE,
      TSCHERMAK_MERGE_TIME_MS,
      // IF MERGING X AMOUNT OF CRYSTALS; THE ATTACK WILL BE STRONGER
      TSCHERMAK_MERGE_VALUE,

      GROWTH_CHANCE_PERCENT,
      GROWTH_RADIUS,
      GROWTH_OFFSET,

      LANDSLIDE_WIDTH,
      LANDSLIDE_POWER,
      LANDSLIDE_TIME_MS,
      LANDSLIDE_CAST_OFFSET,

      RAYLEIGH_WIDTH,
      RAYLEIGH_POWER,
      RAYLEIGH_DAMAGE,
      RAYLEIGH_TIME_MS,
      RAYLEIGH_CAST_OFFSET,
      RAYLEIGH_DIST_BACKLOOK,

      MAGNETISM_MIN_RADIUS,
      MAGNETISM_MAX_RADIUS,
      MAGNETISM_ATTRACT_RADIUS,
      MAGNETISM_MOVE_TIME_MS,
      MAGNETISM_FLY_SPEED_MS_PER_TILE,
      MAGNETISM_TARGET_RADIUS,
      MAGNETISM_APPROACH_TIME_MS,

      UI_CHAR_SELECT_LEFT_OFFSET,
      UI_CHAR_SELECT_MID_OFFSET,
      UI_CHAR_SELECT_BOX_WIDTH,
      UI_CHAR_SELECT_BOX_HEIGHT,
      UI_CHAR_SELECT_LOWER_OFFSET,
      UI_CHAR_SELECT_TEXT_1_OFFSET,
      UI_CHAR_SELECT_TEXT_2_OFFSET,
      UI_CHAR_SELECT_TEXT_3_OFFSET,

      UI_GAME_TIME_X,
      UI_GAME_TIME_Y,
      UI_GAME_TIME_BOX_WIDTH,
      UI_GAME_TIME_BOX_HEIGHT,
      UI_GAME_TIME_TEXT_OFFSET_X,
      UI_GAME_TIME_TEXT_OFFSET_Y,
      ;

      // IF THE FILE DOES NOT SET THE VALUE A DEFAULT OF 10 MAY BE WEIRD FOR SOME OF THE CONFIGS
      public int value = 10; // loaded from file
   }
}