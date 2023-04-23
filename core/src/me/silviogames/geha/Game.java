package me.silviogames.geha;

// holds a game session and this class also receives the controller inputs,
// so it should pass that information on to the player controlling class/code

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

public class Game implements ControllerListener
{
   public static Arena arena;
   public Menu menu = null;
   public ShaderLibrary shaders;

   private static int keyboard_miner_id = 1;
   // OLD CONTROLLER MANAGEMENT:
   //-------------------
   //public static IntIntMap iimap_controller_to_miner = new IntIntMap();

   // TODO: 26.07.2021 let each player toggle direction arrow with controller button
   // unique name of controller points to controller index that I assign,
   // that index is then used in smx_controllers to connect miners to controllers
   //public static ObjectIntMap<String> controller_name = new ObjectIntMap<>();

   // controller index points to mapping
   //public static ObjectMap<Integer, IntIntMap> controller_mapping = new ObjectMap<>();

   // ------------------


   // NEW CONTROLLER MANAGEMENT
   //-----------------------
   // all array use same offset
   // TODO: 16.03.23 this whole thing could be replaced by a Smartrix, which makes the management of the data much shorter. but on the other hand I need to point with indices to the String values.

   public static Smartrix sm_controllers = new Smartrix(Controller_Data.values().length, -1, -1);

   public static Array<String> controller_unique_ids = new Array<>();
   // a shorter version of the ID to display it in game for debugging
   public static Array<String> controller_unique_id_short = new Array<>();

   //public static IntArray controller_to_mapping_index = new IntArray();
   //public static IntArray controller_to_miner = new IntArray();
   //// this is used by the CLASS_SELECTION menu
   //public static IntArray controller_to_class = new IntArray();
   //// the default value is 0 and it needs to be managed (reset) by menus
   //public static IntArray controller_to_selection_confirm = new IntArray();

   //public static Array<String> controller_unique_ids = new Array<>();

   //-----------------------

   public static float game_time = 0f;

   public Game()
   {
      init(new Arena());
   }

   public void init(Arena arena)
   {
      Game.arena = arena;
      arena.init(this);
   }

   public void dispose()
   {
      shaders.dispose();
   }

   public void init()
   {
      // INFO: when going to the INGAME menu directly after boot, controllers will not work right now, this is only intended to test stuff using keyboard debug input
      change_to_menu(Main.skip_main_menu ? Menu.INGAME : Menu.MAIN_MENU);
      //shaders = new ShaderLibrary();
   }

   public void reset()
   {
      // restart the game
      // regen the arena
   }

   public void render()
   {
      if (menu != null) menu.render(this);

      Main.batch.setColor(Color.WHITE);
   }

   public int get_player_with_keyboard()
   {
      return 0;
   }

   public void input()
   {
      // TODO: 25.07.2021 receive keyboard/controller inputs
   }

   public void update(float delta)
   {
      // menus handle the updates of arena
      if (menu != null) menu.update(delta, this);
   }

   public boolean key_press(int keycode)
   {
      return Gdx.input.isKeyJustPressed(keycode);
   }

   @Override
   public void connected(Controller controller)
   {
      System.out.println("before connecting the controller has player index " + controller.getPlayerIndex() + " and power level " + controller.getPowerLevel() + " support for player index " + controller.supportsPlayerIndex());
      controller.setPlayerIndex(3);
      controller_unique_ids.add(controller.getUniqueId());
      controller_unique_id_short.add(controller.getUniqueId().substring(0, 8));

      sm_controllers.add_line(0, -1, 0, 0);

      //controller_name.put(controller.getUniqueId(), local_controller_index);
      System.out.println("Controller [" + controller_unique_ids.size + "] " + controller.getName() + " [" + controller.getUniqueId() + "] has been connected!");
      System.out.println("default controller mapping has been assigned to controller [" + controller_unique_ids.size + "]");
   }

   public static int controller_index(String wanted_controller_unique_id)
   {
      for (int i = 0; i < sm_controllers.num_lines(); i++)
      {
         // TODO: 29.03.23 should sm_controllers and the string arrays not be synced anyways? so I do not need to keep indices, since they change anyways after disconnecting the first controller of 2 or more!
         if (controller_unique_ids.get(i).equals(wanted_controller_unique_id))
         {
            return i;
         }
      }
      return -1;
   }

   @Override
   public void disconnected(Controller controller)
   {
      // there seems to be a weird bug when disconnecting a controller but I'm not sure if the problem is in my data structures.

      System.out.println("Controller " + controller.getUniqueId() + " has been disconnected!");

      int controller_remove_index = controller_index(controller.getUniqueId());
      if (controller_remove_index == -1)
      {
         System.out.println("trying to disconnect unknown controller: " + controller.getUniqueId());
      } else
      {
         // I can just remove all entries in sync and the others move up and it should be fine
         controller_unique_ids.removeIndex(controller_remove_index);
         controller_unique_id_short.removeIndex(controller_remove_index);

         // I used to call clear line here but that did not shrink the backing array, which led to problems after disconnecting a controller, so
         sm_controllers.remove_line(controller_remove_index);
      }
   }

   @Override
   public boolean buttonDown(Controller controller, int buttonCode)
   {
      int controller_index = controller_index(controller.getUniqueId());

      // if controller_name does not contain the ID then the controller is unknown and must be connected
      if (controller_index == -1)
      {
         System.out.println("unknown controller detected " + controller.getUniqueId());
         connected(controller);
      } else
      {

         Controller_Buttons cb = get_button(controller.getUniqueId(), buttonCode);
         menu.controller_button_down(this, controller_index, cb);
      }
      return false;
   }

   public static Controller_Buttons get_button(String controller_unique_id, int button_code)
   {
      int controller_index = controller_index(controller_unique_id);

      // this mapping returns ordinals of Controller_Buttons
      int controller_to_mapping_index = sm_controllers.get(controller_index, Controller_Data.ID_MAPPING.ordinal());

      IntIntMap controller_mapping = Controller_Buttons.controller_mapping.get(controller_to_mapping_index, Controller_Buttons.controller_mapping_default);
      int cb_ord = controller_mapping.get(button_code, -1);
      // the caller of this function has to deal with a returned null!
      return Controller_Buttons.safe_ordinal(cb_ord);
   }

   public void debug_input()
   {
      if (arena.sm_miners.num_lines() > 0)
      {
         if (key_press(Input.Keys.N) && Main.god_mode)
         {
            arena.sm_miners.set(keyboard_miner_id, MinerData.NUM_CRYSTALS.ordinal(), Config.CONF.CRYSTAL_MAX_POSSESSION.value);
         }
      }

      if (key_press(Input.Keys.F3))
      {
         // adding new miner that can be controlled with the keyboard for testing
         arena.create_miner(Miner.MinerClass.KENKMANN);
      }


      if (key_press(Input.Keys.F6))
      {
         Main.debug_render = !Main.debug_render;
      }

      if (key_press(Input.Keys.F8))
      {
         arena.prepare();
      }

      if (key_press(Input.Keys.F9))
      {
         // move all miners to the middle to test stuff quicker
         for (int i = 0; i < arena.sm_miners.num_lines(); i++)
         {
            arena.DEBUG_displace_miner(i, 11, 11);
         }
      }
   }

   public void change_to_menu(Menu menu)
   {
      this.menu = menu;
      menu.init(this);
   }

   public void keyboard_input()
   {
      // update world
      // update entities
      int A = Input.Keys.A;
      int D = Input.Keys.D;
      int W = Input.Keys.W;
      int S = Input.Keys.S;

      if (Game.arena.sm_miners.num_lines() > 0)
      {
         if (key_press(A))
         {
            Miner.handle_input(arena, arena.sm_miners, keyboard_miner_id, Controller_Buttons.DPAD_LEFT);
         } else if (key_press(D))
         {
            Miner.handle_input(arena, arena.sm_miners, keyboard_miner_id, Controller_Buttons.DPAD_RIGHT);
         } else if (key_press(W))
         {
            Miner.handle_input(arena, arena.sm_miners, keyboard_miner_id, Controller_Buttons.DPAD_UP);
         } else if (key_press(S))
         {
            Miner.handle_input(arena, arena.sm_miners, keyboard_miner_id, Controller_Buttons.DPAD_DOWN);
         }

         if (key_press(Input.Keys.SPACE))
         {
            Miner.handle_input(Game.arena, Game.arena.sm_miners, keyboard_miner_id, Controller_Buttons.ACTIONS_B);
         }

         if (key_press(Input.Keys.X))
         {
            // TODO: 24.02.23 get class of miner and call their secondary attack
            // TODO: 13.03.23 this is not implemented right now.
            //  maybe the secondary simple attacks are performed by holding
            //Miner.handle_input(arena, arena.sm_miners, 0, Controller_Buttons.SPECIAL_ATTACK);
         }

         if (key_press(Input.Keys.C))
         {
            Miner.perform_special_attack(arena, arena.sm_miners, keyboard_miner_id, 0);
         }

         if (key_press(Input.Keys.V))
         {
            Miner.perform_special_attack(arena, arena.sm_miners, keyboard_miner_id, 1);
         }

         if (key_press(Input.Keys.B))
         {
            Miner.perform_special_attack(arena, arena.sm_miners, keyboard_miner_id, 2);
         }
      }
   }

   @Override
   public boolean buttonUp(Controller controller, int buttonCode)
   {
      return false;
   }

   @Override
   public boolean axisMoved(Controller controller, int axisCode, float value)
   {
      return false;
   }

   public static boolean remap(int controller_index, int pressed_button)
   {
      if (true) return true;
      // TODO: 16.03.23 reimplement this later

      IntIntMap mapping = Controller_Buttons.controller_mapping.get(controller_index);

      int old_action_index = -1;
      for (IntIntMap.Entry e : mapping)
      {
         if (e.value == Menu.remap_action_index)
         {
            old_action_index = e.key;
         }
      }
      // unmapping action at old location on mapping
      mapping.remove(old_action_index, -1);

      mapping.put(pressed_button, Menu.remap_action_index);
      System.out.println(Controller_Buttons.values()[Menu.remap_action_index].toString() + " mapped to button " + pressed_button);

      Menu.remap_action_index++;
      if (Menu.remap_action_index >= Controller_Buttons.values().length)
      {
         // return true when all available actions have been mapped
         System.out.println("finished button mapping for controller [" + controller_index + "]");
         return true;
      } else
      {
         System.out.println("press button to map " + Controller_Buttons.values()[Menu.remap_action_index].toString());
      }
      return false;
   }
}