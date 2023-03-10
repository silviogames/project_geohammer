package me.silviogames.geha;

import com.badlogic.gdx.math.MathUtils;

public class Util
{
	// useful methods for all needs

	public static byte[] fourdirx = new byte[]{0, 1, 0, -1};
	public static byte[] fourdiry = new byte[]{1, 0, -1, 0};

	public static byte[] eightdirx = new byte[]{-1, 0, 1, -1, 1, -1, 0, 1};
	public static byte[] eightdiry = new byte[]{1, 1, 1, 0, 0, -1, -1, -1};

	public static int euclid_norm(int x1, int y1, int x2, int y2)
	{
		int dx = (x1 - x2) * (x1 - x2);
		int dy = (y1 - y2) * (y1 - y2);
		return MathUtils.round((float) Math.sqrt((double) dx + dy));
	}

	public static int[] RANDOM_RADIAL_OFFSET(int radius)
	{
		// THIS FILLES THE RANDOM CIRCLE, NOT ONLY ON THE CIRCLE!
		int[] r = new int[2];
		int angle = MathUtils.random(0, 359);
		float rand_radius = MathUtils.random(0f, radius);
		r[0] = (int) (MathUtils.cosDeg(angle) * rand_radius);
		r[1] = (int) (MathUtils.sinDeg(angle) * rand_radius);
		return r;
	}

	public static int FLOAT_TO_INT(float value_in)
	{
		// assuming 3 digits for float are wanted
		// this should be used for times that are saved as ints in arrays of ints
		return (int) (value_in * 1000);
	}

	public static float INT_TO_FLOAT(int value_in)
	{
		// assuming 3 digits for float are wanted
		// this should be used for times that are saved as ints in arrays of ints
		return ((float) value_in) / 1000f;
	}
}
