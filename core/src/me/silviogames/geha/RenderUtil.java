package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

public class RenderUtil
{

	// HELPER CLASS FOR RENDERING STUFF LIKE BOXES

	public static final Color color_trans_gray = Color.LIGHT_GRAY.cpy();

	static
	{
		color_trans_gray.a = 0.5f;
	}

	public static void render_box(int posx, int posy, int width, int height)
	{
		render_box(posx, posy, width, height, Color.WHITE);
	}

	public static void render_box(int posx, int posy, int width, int height, Color color)
	{
		Main.batch.setColor(color);
		Main.batch.draw(Res.pixel, posx, posy, width, height);
		Main.batch.setColor(Color.WHITE);
	}

	public static void render_bar(int posx, int posy, int width, int height, Color cempty, Color cfull, float fill)
	{
		fill = MathUtils.clamp(fill, 0f, 1f);
		render_box(posx, posy, width, height, cempty);
		render_box(posx, posy, MathUtils.clamp(MathUtils.round(width * fill), 0, width), height, cfull);
	}
}
