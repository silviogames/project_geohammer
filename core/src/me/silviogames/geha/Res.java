package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;

// this enum contains all texture regions that are loaded from atlas
public enum Res
{

	SHEET_FLOOR_TILES(3, 16, 16),
	SHEET_BLOCKS(1, 16, 30),
	SHEET_BREAK(5, 16, 30),
	GUY(9, 32, 34),
	DIRECTIONS(4, 16, 16),
	PARTICLES_ROCK(5, 16, 16),
	ERROR_FRAME(1, 0, 0),
	CRYSTAL(1, 16, 16),
	IMPACTOR(1, 32, 32),

	PIXEL(),
	;

	static final Array<Anim> load_queue = new Array<Anim>();
	public static Array<TextureRegion> animation_frames = new Array<TextureRegion>(); // contains all frames of all animations
	public static IntIntMap anim_to_frame = new IntIntMap(); // points from anim id to start of frame in animation_frames
	static TextureAtlas atlas;
	public static TextureRegion alphabet, pixel;
	int sheet_num = 0, sheet_width = -1, sheet_height = -1;
	TextureRegion region;
	TextureRegion[] sheet;

	Res()
	{
		// empty constructor

	}

	Res(int num, int sw, int sh)
	{
		this.sheet_num = num;
		this.sheet_width = sw;
		this.sheet_height = sh;
	}

	public static void load()
	{
		atlas = new TextureAtlas("geohammer.atlas");
		alphabet = atlas.findRegion("5x6font");
		pixel = new TextureRegion(alphabet, 2, 0, 1, 1);

		for (Res r : values())
		{
			r.region = atlas.findRegion(r.name().toLowerCase());

			if (r.sheet_num > 0)
			{
				r.sheet = cut_sprites(r.region, r.sheet_num, r.sheet_width, r.sheet_height);
			}
		}
	}

	private static void fixBleeding(TextureRegion... array)
	{
		for (TextureRegion tr : array)
		{
			fixBleeding(tr);
		}
	}

	private static void fixBleeding(TextureRegion region)
	{
		float fix = 0.01f;

		float x = region.getRegionX();
		float y = region.getRegionY();
		float width = region.getRegionWidth();
		float height = region.getRegionHeight();
		float invTexWidth = 1f / region.getTexture().getWidth();
		float invTexHeight = 1f / region.getTexture().getHeight();
		region.setRegion((x + fix) * invTexWidth, (y + fix) * invTexHeight, (x + width - fix) * invTexWidth, (y + height - fix) * invTexHeight); // Trims
		// region
	}

	private static TextureRegion[] cut_sprites(TextureRegion sprite_sheet, int num_sprites, int width, int height)
	{
		// this is only working properly with a 1 pixel spacing between frames
		if (sprite_sheet == null)
		{
			System.out.println("[RES] cannot cut sprite. missing sprite_sheet!");
			return new TextureRegion[num_sprites];
		}
		TextureRegion[] r = new TextureRegion[num_sprites];
		for (int i = 0; i < num_sprites; i++)
		{
			r[i] = new TextureRegion(sprite_sheet, i * (width + 1), 0, width, height);
		}
		fixBleeding(r);
		return r;
	}

	// TODO: 04.08.2021 this needs to be fixed!
	private static void load_anims(TextureRegion source, int width, int height, int yoff)
	{
		if (source == null)
		{
			Gdx.app.log("[RES] ", "could not find source region");
			return;
		}
		// load frames of animation strip into global frame list
		int xpos = 0;
		int frames = 0;

		for (int i = 0; i < load_queue.size; i++)
		{
			anim_to_frame.put(load_queue.get(i).ordinal(), animation_frames.size);
			xpos = frames * (width + 1); // move start position on animation strip
			for (int j = 0; j < load_queue.get(i).num_frames; j++)
			{
				animation_frames.add(new TextureRegion(source, xpos + (j * (width + 1)), yoff, width, height));
				frames++;
			}
			if (load_queue.get(i).flip)
			{
				for (int j = 0; j < load_queue.get(i).num_frames; j++)
				{
					animation_frames.add(new TextureRegion(source, xpos + (j * (width + 1)) + width, yoff, -width, height));
				}
			}
		}
	}

	public static TextureRegion get_frame(float frame_time, Anim anim, boolean flipped)
	{
		if (anim == null)
		{
			return ERROR_FRAME.region;
		}
		int frame = anim_to_frame.get(anim.ordinal(), 0);
		if (flipped && anim.flip)
		{
			frame += anim.num_frames;
		}
		frame += anim.get_frame(frame_time);
		if (frame >= 0 && frame < animation_frames.size)
		{
			return animation_frames.get(frame);
		} else
		{
			return ERROR_FRAME.region;
		}
	}

	public static void dispose()
	{
		atlas.dispose();
	}
}


