package me.silviogames.geha;

// holds a game session and this class also receives the controller inputs,
// so it should pass that information on to the player controlling class/code

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.jetbrains.desktop.ConstrainableGraphics2D;

public class Game implements ControllerListener
{
   public static Arena arena;
   public Menu menu = null;
   public ShaderLibrary shaders;

   private static int keyboard_miner_id = 1;
   // OLD CONTROLLER MANAGEMENT:
   //-------------------
   //public static IntIntMap iimap_controller_to_miner = new IntIntMap();

   // unique name of controller points to controller index that I assign,
   // that index is then used in smx_controllers to connect miners to controllers
   //public static ObjectIntMap<String> controller_name = new ObjectIntMap<>();

   // controller index points to mapping
   //public static ObjectMap<Integer, IntIntMap> controller_mapping = new ObjectMap<>();

   // ------------------


   // NEW CONTROLLER MANAGEMENT
   //-----------------------
   // all array use same offset

   public static Smartrix sm_controllers = new Smartrix(Controller_Data.values().length, -1, -1);

   //public static Array<String> controller_unique_ids = new Array<>();
   // a shorter version of the ID to display it in game for debugging
   //public static Array<String> controller_unique_id_short = new Array<>();

   //public static IntArray controller_to_mapping_index = new IntArray();
   //public static IntArray controller_to_miner = new IntArray();
   //// this is used by the CLASS_SELECTION menu
   //public static IntArray controller_to_class = new IntArray();
   //// the default value is 0 and it needs to be managed (reset) by menus
   //public static IntArray controller_to_selection_confirm = new IntArray();

   //public static Array<String> controller_unique_ids = new Array<>();

   //-----------------------

   public static float game_time = 0f;
   public static float time_power_effect = 0f;

   public static float Xaxis, Yaxis;

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
      //shaders.dispose();
   }

   public void init()
   {
      // INFO: when going to the INGAME menu directly after boot, controllers will not work right now, this is only intended to test stuff using keyboard debug input
      change_to_menu(Main.skip_main_menu ? Menu.INGAME : Menu.MAIN_MENU);
      //shaders = new ShaderLibrary();

      Controller_Buttons.controller_state.put(Main.keyboard_controller_state_id, new IntIntMap());

      for (Controller c : Controllers.getControllers())
      {
         connected(c);
      }
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

   public void update(float delta)
   {

      // CONTROLLER POLLING
      if (sm_controllers.num_lines() > 0)
      {
         Array<Controller> list_controller = Controllers.getControllers();
         for (Controller c : list_controller)
         {
            for (Controller_Buttons cb : Controller_Buttons.values())
            {
               if (!cb.is_axis)
               {
                  boolean button_down = c.getButton(cb.default_controller_button_index);
                  //System.out.println("controller " + c.getUniqueId() + "  down " + cb);
                  Controller_Buttons.update_button_state(c.getUniqueId().hashCode(), cb.ordinal(), button_down);
               } else
               {
                  // I had to offset the axis codes by 100 so they do not collide with the button codes
                  float axis_value = c.getAxis(cb.axis_code - 100);
                  Controller_Buttons.update_axis_state(c.getUniqueId().hashCode(), cb.ordinal(), axis_value * (cb.inverted ? -1 : 1));
               }
            }
         }
      }

      // KEYBOARD DEBUG POLLING
      for (Controller_Buttons cb : Controller_Buttons.values())
      {
         if (!cb.is_axis)
         {
            boolean button_down = Gdx.input.isKeyPressed(cb.default_keyboard_stroke);
            Controller_Buttons.update_button_state(Main.keyboard_controller_state_id, cb.ordinal(), button_down);
         }
      }

      // menus handle the updates of arena
      if (menu != null)
      {
         for (int i = 0; i < sm_controllers.num_lines(); i++)
         {
            if (i == 2 && menu == Menu.INGAME)
            {
               Main.batch.setColor(Color.WHITE);
            }
            menu.input_update(i, sm_controllers.get(i, Controller_Data.HASH.ordinal()), this);
         }

         if (Main.allow_debug_keyboard_miner_controll && arena.sm_miners.check_line(0))
         {
            Miner.inner_handle_input(arena, 0, Main.keyboard_controller_state_id);
         }

         menu.update(delta, this);
      }
   }

   public boolean key_press(int keycode)
   {
      return Gdx.input.isKeyJustPressed(keycode);
   }

   @Override
   public void connected(Controller controller)
   {
      //System.out.println("before connecting the controller has player index " + controller.getPlayerIndex() + " and power level " + controller.getPowerLevel() + " support for player index " + controller.supportsPlayerIndex());
      //controller.setPlayerIndex(MathUtils.random(0,3));

      //controller_unique_ids.add(controller.getUniqueId());
      //controller_unique_id_short.add(controller.getUniqueId().substring(0, 8));

      int controller_hash = controller.getUniqueId().hashCode();

      // controller entries know their own hash, so I can iterate over the list to find the entry if I have the outer controller instance with the hash
      sm_controllers.add_line(controller_hash, 0, -1, 0, 0);

      // add entry to controller state using the hash
      Controller_Buttons.controller_state.put(controller_hash, new IntIntMap());

      //controller_name.put(controller.getUniqueId(), local_controller_index);
      System.out.println("Controller [" + sm_controllers.num_lines() + "] " + controller.getName() + " [" + controller.getUniqueId() + "] has been connected!");
      //System.out.println("default controller mapping has been assigned to controller [" + controller_unique_ids.size + "]");
   }

   public static int controller_index(String wanted_controller_unique_id)
   {
      // USE CASES FOR THIS METHOD:
      // (1) needed when event of disconnect controller is triggered,
      // then I need to find the index in the sm_controllers just from the hash that the
      // controller instance gives me trough the event.

      // (2) when buttondown event is triggered by a controller that is connected but not yet established in my datastructures (sm_controllers), this method will then return -1,
      // so I can set up the datastructure since I know the controller is not known yet.

      int name_hash = wanted_controller_unique_id.hashCode();
      for (int i = 0; i < sm_controllers.num_lines(); i++)
      {
         if (sm_controllers.get(i, Controller_Data.HASH.ordinal()) == name_hash)
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
         //controller_unique_ids.removeIndex(controller_remove_index);
         //controller_unique_id_short.removeIndex(controller_remove_index);

         // I used to call clear line here but that did not shrink the backing array, which led to problems after disconnecting a controller, so
         sm_controllers.remove_line(controller_remove_index);
         Controller_Buttons.controller_state.remove(controller.getUniqueId().hashCode());
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
      }

      if (Main.debug_input)
      {
         System.out.println(controller.getUniqueId().hashCode() + " pressed " + buttonCode);
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
         if(arena.sm_miners.num_lines() < 4)
         {
            // adding new miner that can be controlled with the keyboard for testing
            arena.create_miner(Miner.MinerClass.KENKMANN);
         }
      }

      if (key_press(Input.Keys.F6))
      {
         Main.debug_render = !Main.debug_render;
         Main.debug_slow_motion = !Main.debug_slow_motion;
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

   @Override
   public boolean buttonUp(Controller controller, int buttonCode)
   {
      return false;
   }

   @Override
   public boolean axisMoved(Controller controller, int axisCode, float value)
   {
      if (Main.debug_input)
      {
         System.out.println(controller.getUniqueId().hashCode() + " axis " + axisCode + " value: " + Util.FLOAT_TO_INT(value));
      }
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