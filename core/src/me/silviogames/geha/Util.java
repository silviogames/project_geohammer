package me.silviogames.geha;

import com.badlogic.gdx.math.MathUtils;

public class Util
{
	// useful methods for all needs

	public static byte[] fourdirx = new byte[] { 0, 1, 0, -1 };
	public static byte[] fourdiry = new byte[] { 1, 0, -1, 0 };

	public static int euclid_norm( int x1, int y1, int x2, int y2 )
	{
		int dx = ( x1 - x2 ) * ( x1 - x2 );
		int dy = ( y1 - y2 ) * ( y1 - y2 );
		return MathUtils.round( ( float ) Math.sqrt( ( double ) dx + dy ) );
	}
}
