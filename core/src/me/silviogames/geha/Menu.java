package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

import sun.util.resources.ext.CalendarData_hr;

public enum Menu
{
   MAIN_MENU,
   CHARACTER_SELECTION,
   INGAME,
   CONTROLLER_MAPPING,

   AFTER_GAME,

   ;

   // INFO for now the state some of the menus need is kept here as static variables
   //  which should be fine, since most state is needed by the arena which handles it itself
   // 	otherwise I would have to introduce some kind of abstraction to have a dynamic state keeping class
   //  or so

   static float esc_time = 0f;

   private static boolean key_press(int keycode)
   {
      return Gdx.input.isKeyJustPressed(keycode);
   }

   // STATIC STATE SECTION: CONTROLLER REMAPPING
   // arena will always be a square
   // when setting this to a valid index we take in all commands of that controller for remapping
   // ( currently not visual in the game but in the console)
   static boolean wait_for_remap = false;
   static int current_remap_controller_index = -1;
   static int remap_action_index = 0;

   // STATIC STATE SECTION: CHARACTER SELECTION:
   static float selection_ready_delay = 0f;
   static float selection_ready_delay_time = 3f;

   static boolean random_classes = false;

   //--------------------

   // STATIC STATE SECTION: INGAME:
   static Timer timer_game_over = new Timer(0.5f);

   static Timer timer_godmode = new Timer(0.5f);
   static float game_over_delay = 0f;
   static float game_over_delay_time = 5f;

   //--------------------

   static float test_anim_time = 0f;

   public boolean input_update(int sm_controller_index, int controller_hash, Game game)
   {
      boolean ret = false;
      // this is called for all controllers/miners per frame
      // returning true if I want to break the controller loop:
      // for example if I wait for a player to press to go from main menu to character selection,
      // only one press should be processed and then break the loop

      switch (this)
      {
         case MAIN_MENU:
            if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_B) == 2)
            {
               game.change_to_menu(CHARACTER_SELECTION);
               ret = true;
            }
            break;
         case CHARACTER_SELECTION:
         {
            if (Game.sm_controllers.get(sm_controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal()) == 0)
            {
               int class_index_change = 0;
               if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_LEFT) == 2)
               {
                  class_index_change = 1;
               }

               if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.DPAD_RIGHT) == 2)
               {
                  class_index_change = -1;
               }

               if (class_index_change != 0)
               {
                  Game.sm_controllers.set(sm_controller_index, Controller_Data.ID_SELECTION_CLASS.ordinal(), Util.wrapped_increment(Game.sm_controllers.get(sm_controller_index, Controller_Data.ID_SELECTION_CLASS.ordinal()), class_index_change, 0, Miner.MinerClass.values().length - 1));
               }
            }

            if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_B) == 2)
            {
               Game.sm_controllers.set(sm_controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal(), 0);
            }
            if (Controller_Buttons.read_button(controller_hash, Controller_Buttons.ACTIONS_A) == 2)
            {
               Game.sm_controllers.set(sm_controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal(), 1);
            }
         }
         break;
         case INGAME:
         {
            Miner.handle_input(Game.arena, sm_controller_index);
         }
         break;
      }

      return ret;
   }

   public void update(float delta, Game game)
   {
      switch (this)
      {
         case MAIN_MENU:
         {
            if (key_press(Input.Keys.SPACE))
            {
               game.change_to_menu(CHARACTER_SELECTION);
            }

            if (delayed_press(Input.Keys.ESCAPE, delta)) Gdx.app.exit();

            float[] ret = Anim.PARTICLE_BLOOD.update(test_anim_time, delta);
            test_anim_time = ret[0];
         }
         break;

         case INGAME:
         {
            Game.game_time += delta;
            Game.time_power_effect += delta * 10;
            Game.arena.update(delta);

            if (Main.debug_input)
            {
               // KEYBOARD DEBUG INPUT THAT DOES STUFF TO THE ARENA
               // BUT NOT DIRECTLY CONTROLS MINERS
               game.debug_input();
            }

            if (delayed_press(Input.Keys.ESCAPE, delta))
            {
               game.change_to_menu(MAIN_MENU);
            }

            if (Main.god_mode && timer_godmode.update(delta))
            {
               for (int i = 0; i < Game.arena.sm_miners.num_lines(); i++)
               {
                  Game.arena.sm_miners.set(i, MinerData.NUM_CRYSTALS.ordinal(), Config.CONF.CRYSTAL_MAX_POSSESSION.value);
               }
            }

            if (!Main.skip_main_menu)
            {
               if (game_over_delay == 0f)
               {
                  if (timer_game_over.update(delta))
                  {
                     if (Game.arena.number_alive_miners() <= 1 && !Main.debug_render)
                     {
                        game_over_delay = 0.001f;
                     }
                  }
               } else
               {
                  game_over_delay += delta;
                  if (game_over_delay >= game_over_delay_time)
                  {
                     game.change_to_menu(MAIN_MENU);
                  }
               }
            }
         }
         break;

         case CHARACTER_SELECTION:
         {
            if (delayed_press(Input.Keys.ESCAPE, delta))
            {
               game.change_to_menu(MAIN_MENU);
            }

            if (key_press(Input.Keys.R))
            {
               random_classes = !random_classes;
            }

            if (key_press(Input.Keys.F1))
            {
               Array<Controller> list_controller = Controllers.getControllers();
               System.out.println(list_controller.size + " controllers connected");
               for (Controller ct : list_controller)
               {
                  System.out.println(ct.getName() + " " + ct.getUniqueId());
               }
            }

            if (all_players_ready())
            {
               selection_ready_delay += delta;
               if (selection_ready_delay >= selection_ready_delay_time)
               {
                  for (int i = 0; i < Game.sm_controllers.num_lines(); i++)
                  {
                     Miner.MinerClass mc = null;
                     if (random_classes)
                     {
                        mc = Miner.MinerClass.random();
                     } else
                     {
                        mc = Miner.MinerClass.safe_ord(Game.sm_controllers.get(i, Controller_Data.ID_SELECTION_CLASS.ordinal()));
                     }
                     // mc should never be null here

                     int miner_id = Game.arena.create_miner(mc);
                     Game.sm_controllers.set(i, Controller_Data.ID_MINER.ordinal(), miner_id);
                  }
                  if (Main.spawn_keyboard_miner && Game.arena.sm_miners.num_lines() < 4)
                  {
                     Game.arena.create_miner(Miner.MinerClass.KENKMANN);
                  }
                  game.change_to_menu(INGAME);
               }
            } else
            {
               selection_ready_delay = 0f;
            }
         }
         break;
         case AFTER_GAME:
         {
            game.change_to_menu(CHARACTER_SELECTION);
         }
         break;

         default:

            break;
      }
   }

   private boolean all_players_ready()
   {
      // this should only be called in CHARACTER SELECTION Menu

      if (Game.sm_controllers.num_lines() == 0) return false;

      int counter = 0;
      for (int i = 0; i < Game.sm_controllers.num_lines(); i++)
      {
         counter += Game.sm_controllers.get(i, Controller_Data.ID_SELECTION_CONFIRM.ordinal());
      }

      // TODO: 16.03.23 right now this would mean every connected controller must play but maybe in the
      //  future it may be different so only a few of the connected play.
      return counter == Game.sm_controllers.num_lines();
   }

   public void controller_button_down(Game game, int controller_index, Controller_Buttons pressed_button)
   {
      if (controller_index == -1)
      {
         return;
      }

      switch (this)
      {
         case MAIN_MENU:
         {
            if (pressed_button == Controller_Buttons.ACTIONS_B)
            {
               game.change_to_menu(CHARACTER_SELECTION);
            }
         }
         break;
         case CHARACTER_SELECTION:
         {
            if (Game.sm_controllers.get(controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal()) == 0)
            {
               int class_index_change = 0;
               if (pressed_button == Controller_Buttons.DPAD_RIGHT) class_index_change = 1;
               if (pressed_button == Controller_Buttons.DPAD_LEFT) class_index_change = -1;

               if (class_index_change != 0)
               {
                  Game.sm_controllers.set(controller_index, Controller_Data.ID_SELECTION_CLASS.ordinal(), Util.wrapped_increment(Game.sm_controllers.get(controller_index, Controller_Data.ID_SELECTION_CLASS.ordinal()), class_index_change, 0, Miner.MinerClass.values().length - 1));
               }
            }

            if (pressed_button == Controller_Buttons.ACTIONS_B)
            {
               Game.sm_controllers.set(controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal(), 0);
            }
            if (pressed_button == Controller_Buttons.ACTIONS_A)
            {
               Game.sm_controllers.set(controller_index, Controller_Data.ID_SELECTION_CONFIRM.ordinal(), 1);
            }
         }
         break;

         case INGAME:

            int miner_index = Game.sm_controllers.get(controller_index, Controller_Data.ID_MINER.ordinal());

            if (miner_index != -1)
            {
               if (Main.debug_input)
               {
                  System.out.println("controller [" + controller_index + "] pressed action " + (pressed_button == null ? "{UNKNOWN}" : pressed_button.toString()));
               }

               if (pressed_button != null)
               {
                  //Miner.handle_input(Game.arena, Game.arena.sm_miners, miner_index, );
               } else
               {
                  System.out.println("invalid action!");
               }
            } else
            {
               System.out.println("controller [" + controller_index + "] not assigned to miner!");
            }
            break;

         case CONTROLLER_MAPPING:
         {
            if (wait_for_remap)
            {
               System.out.println("starting remapping for controller [" + controller_index + "]");
               current_remap_controller_index = controller_index;
               remap_action_index = 0;
               System.out.println("press button to map " + Controller_Buttons.values()[0].toString());
               wait_for_remap = false;
               // return here to skip the rest of the method
               return;
            }

            if (current_remap_controller_index == controller_index)
            {
               // HANDLE REMAPPING
               // TODO: 16.03.23 0 is wrong! remapping needs to be rewritten anyways
               if (Game.remap(controller_index, 0))
               {
                  current_remap_controller_index = -1;
                  remap_action_index = 0;
               }
            } else
            {
               // TODO: 16.03.23 this is not needed here
               //IntIntMap current_mapping = Controller_Buttons.controller_mapping.get//(controller_index);
               //Controller_Buttons ia = Controller_Buttons.safe_ordinal(current_mapping.get//(button_code, -1));
               //if(Main.debug_input)
               //{
               //System.out.println("controller [" + controller_index + "] pressed action " + (ia == null ? "{UNKNOWN}" : ia.toString()));
               //}
            }
         }
         break;
      }
   }

   // called as soon as a menu is entered
   public void init(Game game)
   {
      esc_time = 0f;

      switch (this)
      {
         case AFTER_GAME:
         {
            // i used to clear miners here but it can be done somewhere else
         }
         break;
         case MAIN_MENU:

            break;

         case CHARACTER_SELECTION:

            random_classes = false;
            // when this menu is entered after a played game the miners must be cleared.
            Game.arena.prepare();

            // reset the selection for a new round
            for (int i = 0; i < Game.sm_controllers.num_lines(); i++)
            {
               Game.sm_controllers.set(i, Controller_Data.ID_SELECTION_CONFIRM.ordinal(), 0);
            }

            break;

         case INGAME:
         {
            Game.game_time = 0f;
            game_over_delay = 0f;
         }
         break;

         case CONTROLLER_MAPPING:
         {
            wait_for_remap = true;
            remap_action_index = 0;
            if (current_remap_controller_index != -1)
            {
               System.out.println("ongoing remap for controller [" + current_remap_controller_index + "] has been stopped prematurely");
            }
            current_remap_controller_index = -1;
            System.out.println("listening for controller remap, press any button on any controller");
         }
         break;
      }
   }

   public void render(Game game)
   {
      switch (this)
      {
         case MAIN_MENU:
         {
            Text.cdraw("PROJECT GEOHAMMER", Main.window_width / 2, Main.window_height / 2 + 20, Color.WHITE, 3);
            Text.cdraw("press space to start", Main.window_width / 2, Main.window_height / 2 - 20, Color.WHITE, 2);

            Text.draw("version " + 2324, Main.window_width - 60, 2, Color.WHITE, 1);

            if (esc_time > 0f)
            {
               RenderUtil.render_bar(0, Main.window_height - 12, Main.window_width, 12, Color.CLEAR, Color.OLIVE, esc_time / 0.7f);
               Text.draw("CLOSING GAME", 2, Main.window_height - 10, Color.WHITE);
            }
         }
         break;
         case CHARACTER_SELECTION:
         {
            render_character_selection(game);

            if (Main.debug_render)
            {
               Text.draw("num_limes sm_controller " + Game.sm_controllers.num_lines(), 10, Main.window_height - 10, Color.WHITE, 1);
               Text.draw("num data sm_controller " + Game.sm_controllers.data.size, 10, Main.window_height - 20, Color.WHITE, 1);
            }
         }
         break;
         case INGAME:
         {
            Game.arena.render_floor();

            //Game.arena.render_miners();

            Game.arena.render_particles();

            Game.arena.render_HUD();
         }
         break;
         case CONTROLLER_MAPPING:
         {

         }
         break;
      }
   }

   private void render_character_selection(Game game)
   {
      // THIS IS HELPER FUNCTION TO KEEP THE RENDER/UPDATE METHOD SLIM

      Text.cdraw("CHARACTER SELECTION", Main.window_width / 2, Main.window_height - 50, Color.WHITE, 3f);

      int left_offset = Config.CONF.UI_CHAR_SELECT_LEFT_OFFSET.value;
      int mid_offset = Config.CONF.UI_CHAR_SELECT_MID_OFFSET.value;
      int box_width = Config.CONF.UI_CHAR_SELECT_BOX_WIDTH.value;
      int box_height = Config.CONF.UI_CHAR_SELECT_BOX_HEIGHT.value;
      int lower_offset = Config.CONF.UI_CHAR_SELECT_LOWER_OFFSET.value;
      int text_miner_offset = Config.CONF.UI_CHAR_SELECT_TEXT_1_OFFSET.value;
      int text_class_offset = Config.CONF.UI_CHAR_SELECT_TEXT_2_OFFSET.value;
      int text_ready_offset = Config.CONF.UI_CHAR_SELECT_TEXT_3_OFFSET.value;

      for (int i = 0; i < 4; i++)
      {
         int text_pos_mid = left_offset + (box_width + mid_offset) * i + box_width / 2;
         Text.cdraw("MINER " + (i + 1), text_pos_mid, lower_offset + text_miner_offset, Color.WHITE, 2f);

         if (Game.sm_controllers.num_lines() > i)
         {
            RenderUtil.render_box(left_offset + (box_width + mid_offset) * i, lower_offset, box_width, box_height, RenderUtil.miner_colors_trans[i]);

            //Text.cdraw(Game.controller_unique_id_short.get(i), text_pos_mid, lower_offset + 2, Color.WHITE, 1f);
            Text.cdraw(Game.sm_controllers.get(i, Controller_Data.HASH.ordinal()) + " ", text_pos_mid, lower_offset + 12, Color.WHITE, 1f);

            int class_index = Game.sm_controllers.get(i, Controller_Data.ID_SELECTION_CLASS.ordinal());
            Miner.MinerClass mc = Miner.MinerClass.safe_ord(class_index);

            if (random_classes)
            {
               Text.cdraw("< RANDOM >", text_pos_mid, lower_offset + text_class_offset, Color.GOLD, 1f);
            } else
            {
               Text.cdraw("< " + mc + " >", text_pos_mid, lower_offset + text_class_offset, Color.WHITE, 1f);
            }

            Text.cdraw("ABILITIES:", text_pos_mid, lower_offset + text_class_offset - 20, Color.LIGHT_GRAY, 1f);

            Text.cdraw(mc.special_offensive == null ? "none" : mc.special_offensive.toString(), text_pos_mid, lower_offset + text_class_offset - 40, Color.WHITE, 1f);
            Text.cdraw(mc.special_defensive == null ? "none" : mc.special_defensive.toString(), text_pos_mid, lower_offset + text_class_offset - 60, Color.WHITE, 1f);
            Text.cdraw(mc.special_special == null ? "none" : mc.special_special.toString(), text_pos_mid, lower_offset + text_class_offset - 80, Color.WHITE, 1f);

            // TODO: 29.03.23 check if the controller is still connected?

            if (Game.sm_controllers.get(i, Controller_Data.ID_SELECTION_CONFIRM.ordinal()) == 1)
            {
               Text.cdraw("READY", text_pos_mid, lower_offset + text_ready_offset, Color.GOLD, 1f);
            } else
            {
               Text.cdraw("click A", text_pos_mid, lower_offset + text_ready_offset, Color.WHITE, 1f);
               Text.cdraw("when ready", text_pos_mid, lower_offset + text_ready_offset - 8, Color.WHITE, 1f);
            }

         } else
         {
            RenderUtil.render_box(left_offset + (box_width + mid_offset) * i, lower_offset, box_width, box_height, RenderUtil.color_trans_gray);
         }

         // TODO: 08.03.23 display miner portrait in a box

         // TODO: 08.03.23 display list of the 3 special attacks
      }

      if (selection_ready_delay > 0f)
      {
         RenderUtil.render_bar(0, Main.window_height - 12, Main.window_width, 12, Color.CLEAR, Color.OLIVE, selection_ready_delay / selection_ready_delay_time);
         Text.draw("GAME STARTING SOON", 2, Main.window_height - 10, Color.WHITE);
      }
   }

   private boolean delayed_press(int key, float delta)
   {
      if (Gdx.input.isKeyPressed(key))
      {
         esc_time += delta;
         if (esc_time >= 0.7f)
         {
            esc_time = 0f;
            return true;
         }
      } else
      {
         esc_time = 0f;
      }
      return false;
   }
}