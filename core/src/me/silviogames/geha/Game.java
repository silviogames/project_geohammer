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
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

public class Game implements ControllerListener
{
	public static int next_controller_index = 0;
	public static Arena arena;
	public Menu menu = null;
	public ShaderLibrary shaders;
	boolean skip_to_test_arena = true;

	// arena will always be a square
	// when setting this to a valid index we take in all commands of that controller for remapping
	// ( currently not visual in the game but in the console)
	boolean wait_for_remap = false;
	int current_remap_controller_index = -1;
	int remap_action_index = 0;
	IntIntMap iimap_controller_to_miner = new IntIntMap();

	// TODO: 26.07.2021 let each player toggle direction arrow with controller button
	// unique name of controller points to controller index that I assign,
	// that index is then used in smx_controllers to connect miners to controllers
	ObjectIntMap<String> controller_name = new ObjectIntMap<>();
	// controller index points to mapping
	ObjectMap<Integer, IntIntMap> controller_mapping = new ObjectMap<>();

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
		menu = Main.skip_main_menu ? Menu.INGAME : Menu.MAIN_MENU;

		shaders = new ShaderLibrary();
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
		int local_controller_index = next_controller_index;
		next_controller_index++;
		controller_name.put(controller.getUniqueId(), local_controller_index);
		IntIntMap iimap_def = InputActions.get_default_controller_mapping();
		controller_mapping.put(local_controller_index, iimap_def);
		System.out.println("Controller [" + local_controller_index + "] " + controller.getName() + " [" + controller.getUniqueId() + "] has been connected!");
		System.out.println("default controller mapping has been assigned to controller [" + local_controller_index + "]");
		iimap_controller_to_miner.put(local_controller_index, -1);
	}

	@Override
	public void disconnected(Controller controller)
	{
		System.out.println("Controller " + controller.getUniqueId() + " has been disconnected!");

		int controller_index = controller_name.get(controller.getUniqueId(), -1);

		iimap_controller_to_miner.remove(controller_index, 0);
		controller_name.remove(controller.getUniqueId(), 0);
		controller_mapping.remove(controller_index);

		// previously controlled miner is now without control but the next controller may be
		// assigned to them
	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode)
	{
		int controller_index = controller_name.get(controller.getUniqueId(), -1);

		if (controller_index != -1)
		{
			if (wait_for_remap)
			{
				System.out.println("starting remapping for controller [" + controller_index + "]");
				current_remap_controller_index = controller_index;
				remap_action_index = 0;
				System.out.println("press button to map " + InputActions.values()[0].toString());
				wait_for_remap = false;
				// return here to skip the rest of the method
				return false;
			}

			if (current_remap_controller_index == controller_index)
			{
				// HANDLE REMAPPING
				if (remap(controller_index, buttonCode))
				{
					current_remap_controller_index = -1;
					remap_action_index = 0;
				}
			} else
			{
				IntIntMap current_mapping = controller_mapping.get(controller_index);
				InputActions ia = InputActions.safe_ordinal(current_mapping.get(buttonCode, -1));
				System.out.println("controller [" + controller_index + "] pressed action " + (ia == null ? "{UNKNOWN}" : ia.toString()));
				int miner_index = iimap_controller_to_miner.get(controller_index, -1);
				if (miner_index != -1)
				{
					if (ia != null)
					{
						Miner.handle_input(arena, arena.sm_miners, miner_index, ia);
					} else
					{
						System.out.println("invalid action!");
					}
				} else
				{
					System.out.println("controller [" + controller_index + "] not assigned to miner!");
				}
			}
		} else
		{
			System.out.println("button press by unknown controller " + controller.getUniqueId());
			connected(controller);
		}

		return false;
	}

	public void debug_input()
	{
		if (arena.sm_miners.num_lines() > 0)
		{
			if (key_press(Input.Keys.N))
			{
				arena.sm_miners.set(0, MinerData.NUM_CRYSTALS.ordinal(), Config.CONF.CRYSTAL_MAX_POSSESSION.value);
			}
		}

		// TODO: 17.11.22 may be redundant since button press of unknown controller triggers connection
		if (key_press(Input.Keys.F1))
		{
			Array<Controller> list_controller = Controllers.getControllers();
			System.out.println(list_controller.size + " controllers connected");
			for (Controller ct : list_controller)
			{
				System.out.println(ct.getName() + " " + ct.getUniqueId());
			}
		}

		if (key_press(Input.Keys.F2))
		{
			arena.UTIL_fill_empty_controller(iimap_controller_to_miner);
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
			wait_for_remap = true;
			remap_action_index = 0;
			if (current_remap_controller_index != -1)
			{
				System.out.println("ongoing remap for controller [" + current_remap_controller_index + "] has been stopped prematurely");
			}
			current_remap_controller_index = -1;
			System.out.println("listening for controller remap, press any button on any controller");
			// TODO: 17.11.22 check if this is bad when a remap is going on right now
		}
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
				Miner.handle_input(arena, arena.sm_miners, 0, InputActions.WALK_LEFT);
			} else if (key_press(D))
			{
				Miner.handle_input(arena, arena.sm_miners, 0, InputActions.WALK_RIGHT);
			} else if (key_press(W))
			{
				Miner.handle_input(arena, arena.sm_miners, 0, InputActions.WALK_UP);
			} else if (key_press(S))
			{
				Miner.handle_input(arena, arena.sm_miners, 0, InputActions.WALK_DOWN);
			}

			if (key_press(Input.Keys.SPACE))
			{
				Miner.handle_input(Game.arena, Game.arena.sm_miners, 0, InputActions.BASIC_ATTACK);
			}

			if (key_press(Input.Keys.X))
			{
				// TODO: 24.02.23 get class of miner and call their secondary attack
				Miner.handle_input(arena, arena.sm_miners, 0, InputActions.SPECIAL_ATTACK);
			}


			if (key_press(Input.Keys.C))
			{
				Miner.perform_special_attack(arena, arena.sm_miners, 0, 0);
			}

			if (key_press(Input.Keys.V))
			{
				Miner.perform_special_attack(arena, arena.sm_miners, 0, 1);
			}

			if (key_press(Input.Keys.B))
			{
				Miner.perform_special_attack(arena, arena.sm_miners, 0, 2);
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

	private boolean remap(int controller_index, int pressed_button)
	{
		IntIntMap mapping = controller_mapping.get(controller_index);

		int old_action_index = -1;
		for (IntIntMap.Entry e : mapping)
		{
			if (e.value == remap_action_index)
			{
				old_action_index = e.key;
			}
		}
		// unmapping action at old location on mapping
		mapping.remove(old_action_index, -1);

		mapping.put(pressed_button, remap_action_index);
		System.out.println(InputActions.values()[remap_action_index].toString() + " mapped to button " + pressed_button);

		remap_action_index++;
		if (remap_action_index >= InputActions.values().length)
		{
			// return true when all available actions have been mapped
			System.out.println("finished button mapping for controller [" + controller_index + "]");
			return true;
		} else
		{
			System.out.println("press button to map " + InputActions.values()[remap_action_index].toString());
		}
		return false;
	}
}