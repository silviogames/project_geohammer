package me.silviogames.geha;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectMap;

public enum Controller_Buttons
{
   DPAD_LEFT(Input.Keys.A, 13), // 13
   DPAD_RIGHT(Input.Keys.D, 14), // 14
   DPAD_UP(Input.Keys.W, 11), // 11
   DPAD_DOWN(Input.Keys.S, 12), // 12

   ACTIONS_B(Input.Keys.SPACE, 0), // 0
   ACTIONS_A(Input.Keys.V, 1), // 1
   ACTIONS_Y(Input.Keys.F, 2), // 2
   ACTIONS_X(Input.Keys.C, 3), // 3

   BUTTON_L(Input.Keys.R, 9),
   BUTTON_R(Input.Keys.Q, 10),

   // LEFT ANALOG STICK!
   LEFT_AXIS_X(100 + 0, false),
   LEFT_AXIS_Y(100 + 1, true),
   RIGHT_AXIS_X(100 + 2, false),
   RIGHT_AXIS_Y(100 + 3, true),
   ;

   // this enum encodes the known buttons of a controller.
   // previously this used to be in game actions like move and mine but now that the controller input
   // may be redirected to arbitrary menus, the buttons may have a different meaning depending on the context

   final boolean inverted, is_axis;

   final int default_keyboard_stroke, default_controller_button_index, axis_code;

   // this holds the mappings and controllers keep the key to one of the mappings
   public static ObjectMap<Integer, IntIntMap> controller_mapping = new ObjectMap<>();

   final static IntIntMap controller_mapping_default = new IntIntMap();

   // controller hash maps to button/axis state
   public static ObjectMap<Integer, IntIntMap> controller_state = new ObjectMap<>();

   public static void init_default_mapping()
   {
      // a button returning -1 is not mapped.
      for (Controller_Buttons ia : values())
      {
         if (!ia.is_axis)
         {
            controller_mapping_default.put(ia.default_controller_button_index, ia.ordinal());
         }
      }
      controller_mapping.put(0, controller_mapping_default);
   }

   Controller_Buttons(int dks, int dcbi)
   {
      this.default_keyboard_stroke = dks;
      this.default_controller_button_index = dcbi;
      this.inverted = false;
      this.is_axis = false;
      this.axis_code = -1;
   }

   Controller_Buttons(int axis_code, boolean inverted)
   {
      this.inverted = inverted;
      this.is_axis = true;
      this.axis_code = axis_code;
      this.default_keyboard_stroke = -1;
      this.default_controller_button_index = -1;
   }

   public static void update_button_state(int controller_hash, int button, boolean pressed)
   {
      IntIntMap state = controller_state.get(controller_hash);
      int old_value = state.get(button, 0);

      // state:
      // 0 -> not pressed
      // 1 -> pressed
      // 2 -> up (not pressed after being pressed before)

      int new_value = 0;
      switch (old_value)
      {
         case 0:
         case 2:
            // BUTTON WAS NOT PRESSED BEFORE BUT IS NOW
            new_value = pressed ? 1 : 0;
            break;
         case 1:
            // BUTTON WAS PRESSED BEFORE
            new_value = pressed ? 1 : 2;
            break;
      }
      state.put(button, new_value);
   }

   public static void update_axis_state(int controller_hash, int button, float value)
   {
      IntIntMap state = controller_state.get(controller_hash);
      state.put(button, Util.FLOAT_TO_INT(value));
   }

   public static int read_button(int controller_hash, Controller_Buttons cb)
   {
      // this is called by my game code that wants to read the button state
      // may be used for axis as well!
      IntIntMap state = controller_state.get(controller_hash);
      return state.get(cb.ordinal(), 0);
   }

   public static Controller_Buttons safe_ordinal(int ord)
   {
      // wrapper to return null if ord is invalid
      if (ord == -1)
      {
         return null;
      } else
      {
         return values()[ord];
      }
   }
}