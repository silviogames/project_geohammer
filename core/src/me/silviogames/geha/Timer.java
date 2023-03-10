/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.silviogames.geha;

/**
 * Timer class is used for time measurement. it has to be updated every game
 * cycle to work properly
 *
 * @author Silvio
 */
public class Timer
{

	public boolean bool = false;
	public float finalTime, currentTime, factor = 0, adder = 0;

	public Timer(float timeInSeconds)
	{
		this.finalTime = timeInSeconds;
		this.currentTime = 0;
	}

	public Timer(float timeInSeconds, boolean ready)
	{
		this.finalTime = timeInSeconds;
		this.currentTime = ready ? finalTime : 0;
	}

	public Timer(float limit, float factor)
	{
		this.finalTime = limit;
		this.currentTime = 0;
		this.factor = factor;
	}

	public boolean update(float delta)
	{
		boolean returner = false;
		this.currentTime = this.currentTime + delta;
		if (currentTime >= finalTime)
		{
			returner = true;
			if (factor > 0)
			{
				finalTime *= factor;
			}
			if (adder > 0)
			{
				finalTime += adder;
			}
			currentTime = finalTime - currentTime;
		}
		return returner;
	}

	public void update_bool(float delta)
	{
		if (bool)
		{
			// call this to update timer only to reset the flag
			if (update(delta))
			{
				bool = false;
			}
		}
	}

	public float get_current_time()
	{
		return currentTime;
	}

	public float getPercent()
	{
		// progress of timer in interval 0 ... 1f
		return currentTime / finalTime;
	}

	public float getNotPercent()
	{
		return (finalTime - currentTime) / finalTime;
	}

	public void reset()
	{
		this.currentTime = 0;
	}

	public void reset(float new_final_time)
	{
		this.currentTime = 0;
		this.finalTime = new_final_time;
	}

	public void changeFinalTime(float new_final_time)
	{
		this.finalTime = new_final_time;
	}

	public float rest_time()
	{
		return Math.abs(finalTime - currentTime);
	}

	public void ready()
	{
		this.currentTime = finalTime;
	}

	public void setFactor(float factor)
	{
		this.factor = factor;
	}

	public void setAdder(float adder)
	{
		this.adder = adder;
	}
}