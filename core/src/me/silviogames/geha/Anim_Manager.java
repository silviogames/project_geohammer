package me.silviogames.geha;

// A VERY SIMPLE 2D ANIMATION SYSTEM

// ANIM ENUM has all the animation data
// this class manages the state of animation and transitions

public class Anim_Manager
{


	// continuous is being played all the time,
	public Anim continuous_anim = null;
	// unless a single_anim has been set, then the single_anim gets played once
	// then the system falls back to the continuous anim
	public Anim single_anim = null;
	// example: character has idling anim as conti, player pressed button and character
	// does one hammer animation, then falls back to idle anim,
	// if no conti has been set, then

	public float time = 0f;

	Anim_Manager()
	{

	}

	public boolean update( float delta )
	{
		// return true if the keyframe has been triggered.

		if ( single_anim != null )
		{
			float[] ret = single_anim.update( time, delta );
			// ret[0] = next time
			// ret[1] = keyframe
			// ret[2] = animation over
			time = ret[ 0 ];

			if ( ret[ 2 ] == 1f )
			{
				// animation is over, remove single
				single_anim = null;
			}

			return ret[ 1 ] == 1f;
		} else
		{
			if ( continuous_anim != null )
			{
				float[] ret = single_anim.update( time, delta );
				return ret[ 1 ] == 1f;
			}
		}
		return false;
	}


}
