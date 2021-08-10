package me.silviogames.geha;

// holds a game session

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class Game implements ControllerListener
{

	final static int PLAYER_POSX = 0, PLAYER_POSY = 1;
	// miner data
	//[0] tilex
	//[1] tiley
	//[2] view dir
	final static int PLAYER_VIEWDIR = 2;

	// particle data:
	static final int PARTICLE_X = 0;
	static final int PARTICLE_Y = 1;
	static final int PARTICLE_LIFE = 2;
	static final int PARTICLE_ANGLE = 3;
	static final int PARTICLE_TYPE = 4;

	Osc osc_arrow = new Osc( 4, 25f, 20f );

	// arena will always be a square
	int arena_size = 24;


	// TODO: 18.07.2021 use Smartrix for Entities later when having more than 1

	// TODO: 26.07.2021 let each player toggle direction arrow with controller button
	Flatbyte board;
	Flatbyte damage;
	Flatbyte floor;
	OpenSimplexNoise noise_board = new OpenSimplexNoise( 10, 2 );
	int[] player_data = new int[ 3 ];

	Smartrix sm_particles = new Smartrix( 5, -1 );

	public Game()
	{

	}

	public void init()
	{
		// clean new board
		board = new Flatbyte( arena_size, arena_size, ( byte ) -1, ( byte ) 0 );
		damage = new Flatbyte( arena_size, arena_size, ( byte ) -1, ( byte ) 0 );

		// place rocks on board

		int mid = board.width / 2;
		for ( int ix = 0; ix < board.width; ix++ )
		{
			for ( int iy = 0; iy < board.height; iy++ )
			{
				int dst_to_mid = Util.euclid_norm( ix, iy, mid, mid );

				float val = ( float ) ( ( noise_board.noise( ix, iy ) + 1 ) / 2f );
				if ( dst_to_mid < 10 )
				{
					if ( val < 0.5f )
					{
						board.set( ix, iy, ( byte ) 1 );
					}
				}
			}
		}

		// different floor tile variations (indices into sheet array)
		floor = new Flatbyte( arena_size, arena_size, ( byte ) -1, ( byte ) 0 );
	}

	public void reset()
	{
		// restart the game
		// regen the arena
	}

	public void render()
	{
		int off = 0;

		// RENDER FLOOR
		for ( int ix = 0; ix < board.width; ix++ )
		{
			for ( int iy = 0; iy < board.height; iy++ )
			{
				Main.batch.draw( Res.SHEET_FLOOR_TILES.sheet[ 0 ], 16 * ix, 16 * iy );
			}
		}

		// TODO: 19.07.2021 some time in future I need to Y sort entities and Walls
		// RENDER BLOCKS
		for ( int ix = 0; ix < board.width; ix++ )
		{
			for ( int iy = board.height - 1; iy >= 0; iy-- )
			{
				byte block = board.get( ix, iy );
				if ( block == ( byte ) 1 )
				{
					Main.batch.draw( Res.SHEET_BLOCKS.sheet[ 0 ], 16 * ix, 16 * iy );
					byte ord_dam = damage.get( ix, iy );
					if ( ord_dam > 0 )
					{
						// FIXME: 26.07.2021 will crash if hitting to often!
						Main.batch.draw( Res.SHEET_BREAK.sheet[ ord_dam ], 16 * ix, 16 * iy );
					}
				}
			}
		}

		// only rendering player for now
		// TODO: 26.07.2021 center miner sprite
		Main.batch.draw( Res.GUY.sheet[ 0 ], 16 * player_data[ PLAYER_POSX ] - 16 + 8, 16 * player_data[ PLAYER_POSY ] + 8 );

		// direction arrow
		int ud, lr;
		lr = Util.fourdirx[ player_data[ PLAYER_VIEWDIR ] ];
		ud = Util.fourdiry[ player_data[ PLAYER_VIEWDIR ] ];

		Main.batch.draw( Res.DIRECTIONS.sheet[ player_data[ PLAYER_VIEWDIR ] ], 16 * player_data[ PLAYER_POSX ] + ( lr * ( 24 + osc_arrow.value( ) ) ), 16 * player_data[ PLAYER_POSY ] + ( ud * ( 24 + osc_arrow.value( ) ) ) + 8 );

		// TODO: 18.07.2021 render entities on board

		// TODO: 31.07.2021 render the particles
		for ( int i = 0; i < sm_particles.num_lines( ); i++ )
		{
			int[] data_particle = sm_particles.get_line( i );

			if ( data_particle[ PARTICLE_TYPE ] >= 0 )
			{
				// check if this particle is alive

				int angle = data_particle[ PARTICLE_ANGLE ];
				float dx = MathUtils.cosDeg( angle );
				float dy = MathUtils.sinDeg( angle );
				float rx = 16 * data_particle[ PARTICLE_X ] + ( data_particle[ PARTICLE_LIFE ] / 5f ) * dx;
				float ry = 16 * data_particle[ PARTICLE_Y ] + ( data_particle[ PARTICLE_LIFE ] / 5f ) * dy;
				Main.batch.setColor( 1f, 1f, 1f, ( 255 - data_particle[ PARTICLE_LIFE ] ) / 255f );
				Main.batch.draw( Res.PARTICLES_ROCK.sheet[ data_particle[ PARTICLE_TYPE ] ], rx, ry );
			}
		}
		Main.batch.setColor( Color.WHITE );
	}

	public void input()
	{
		// TODO: 25.07.2021 receive keyboard/controller inputs

	}

	public void update( float delta )
	{
		osc_arrow.update( delta );

		// update world
		// update entities
		int A = Input.Keys.A;
		int D = Input.Keys.D;
		int W = Input.Keys.W;
		int S = Input.Keys.S;

		if ( key_press( A ) )
		{
			move_miner( player_data, 3 );
		} else if ( key_press( D ) )
		{
			move_miner( player_data, 1 );
		} else if ( key_press( W ) )
		{
			move_miner( player_data, 0 );
		} else if ( key_press( S ) )
		{
			move_miner( player_data, 2 );
		}

		if ( key_press( Input.Keys.SPACE ) )
		{
			hammer( player_data[ 0 ] + Util.fourdirx[ player_data[ 2 ] ], player_data[ 1 ] + Util.fourdiry[ player_data[ 2 ] ] );
		}
		if ( key_press( Input.Keys.ALT_RIGHT ) )
		{
			System.out.println( "number of particles " + sm_particles.num_lines( ) );
		}


		// TODO: 31.07.2021 update the particles
		for ( int i = 0; i < sm_particles.num_lines( ); i++ )
		{
			int[] data_particle = sm_particles.get_line( i );

			if ( data_particle[ PARTICLE_TYPE ] >= 0 )
			{
				// check if this particle is alive

//			int angle = data_particle[PARTICLE_ANGLE];
//			float dx = MathUtils.cosDeg( angle );
//			float dy = MathUtils.sinDeg( angle );
				data_particle[ PARTICLE_LIFE ] += 3;
				if ( data_particle[ PARTICLE_LIFE ] > 255 )
				{
					// kill the particle
					data_particle[ PARTICLE_TYPE ] = -1;
				}
			}

			// update particle data
			sm_particles.set_line( i, data_particle );
		}
	}

	// not having classes/instances for the player characters.
	// doing it data + function style
	public void move_miner( int[] miner_data, int dir )
	{
		// TODO: 26.07.2021 add lerping between tiles when moving stuff

		int next_tx = miner_data[ PLAYER_POSX ] + Util.fourdirx[ dir ];
		int next_ty = miner_data[ PLAYER_POSY ] + Util.fourdiry[ dir ];

		// set miner dir, even if not moving
		player_data[ PLAYER_VIEWDIR ] = dir;

		if ( free_tile( next_tx, next_ty ) )
		{
			if ( next_tx >= 0 && next_tx < arena_size && next_ty >= 0 && next_ty < arena_size )
			{
				miner_data[ PLAYER_POSX ] = next_tx;
				miner_data[ PLAYER_POSY ] = next_ty;
			}
		} else
		{
			// TODO: 26.07.2021 player bump sound?
		}

		// for now instant movement between tiles


		// TODO: 26.07.2021 check bounds of map

	}

	private void spawn_particle( int tilex, int tiley )
	{
		for ( int i = 0; i < sm_particles.num_lines( ); i++ )
		{
			// find next free particle
			if ( sm_particles.get( i, PARTICLE_TYPE ) == -1 )
			{
				System.out.println( "[PARTICLES] could use free particle data slot" );
				// replace it with new data
				sm_particles.set_line( i, new int[] { tilex, tiley, 0, MathUtils.random( 0, 359 ), MathUtils.random( 0, 4 ) } );
				return;
			}
		}
		// rock particles only for now
		System.out.println( "[PARTICLES] had to create new particle data slot" );
		sm_particles.add_line( tilex, tiley, 0, MathUtils.random( 0, 359 ), MathUtils.random( 0, 4 ) );
	}

	void hammer( int tx, int ty )
	{
		byte block_wall = board.get( tx, ty );
		if ( block_wall == 0 )
		{
			System.out.println( "no block at pos " + tx + " | " + ty );
		} else
		{
			byte current_damage = damage.get( tx, ty );

			current_damage++;
			if ( current_damage >= 5 )
			{
				// break stone
				board.set( tx, ty, ( byte ) 0 );
				for ( int i = 0; i < MathUtils.random( 6, 12 ); i++ )
				{
					spawn_particle( tx, ty );
				}
				current_damage = 0;
			}
			damage.set( tx, ty, current_damage );
		}
	}

	boolean free_tile( int tx, int ty )
	{
		byte tile_floor = floor.get( tx, ty );
		byte block_wall = board.get( tx, ty );
		return block_wall <= 0;
	}

	public boolean key_press( int keycode )
	{
		return Gdx.input.isKeyJustPressed( keycode );
	}

	@Override
	public void connected( Controller controller )
	{
		System.out.println( "Controller " + controller.getName( ) + " [" + controller.getUniqueId( ) + "] has been connected!" );
	}

	@Override
	public void disconnected( Controller controller )
	{
		System.out.println( "Controller " + controller.getUniqueId( ) + " has been disconnected!" );
	}

	@Override
	public boolean buttonDown( Controller controller, int buttonCode )
	{
		//System.out.println( "down " + buttonCode );
		return false;
	}

	@Override
	public boolean buttonUp( Controller controller, int buttonCode )
	{
		//System.out.println( "up " + buttonCode );
		return false;
	}

	@Override
	public boolean axisMoved( Controller controller, int axisCode, float value )
	{
		return false;
	}
}
