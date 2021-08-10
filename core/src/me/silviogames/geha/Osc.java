package me.silviogames.geha;

public class Osc
{
	private final float max;
	private final float up_speed;
	private final float down_speed;
	private boolean grow = true;
	private float value = 0f;
	private float min;

	public Osc( float max, float up_speed, float down_speed )
	{
		this.min = 0f;
		this.max = max;
		if ( min >= max )
		{
			this.min = max - 0.5f;
		}
		this.up_speed = up_speed;
		this.down_speed = down_speed;
	}

	public Osc( float min, float max, float up_speed, float down_speed )
	{
		this.min = min;
		this.max = max;
		this.value = min;
		if ( min >= max )
		{
			this.min = max - 0.5f;
		}
		this.up_speed = up_speed;
		this.down_speed = down_speed;
	}

	public void update( float delta )
	{
		if ( grow )
		{
			value += ( delta * up_speed );
			if ( value >= max )
			{
				value = max;
				grow = false;
			}
		} else
		{
			value -= ( delta * down_speed );
			if ( value <= min )
			{
				value = min;
				grow = true;
			}
		}
	}

	public void reset()
	{
		value = 0f;
	}

	public float value()
	{
		return value;
	}
}