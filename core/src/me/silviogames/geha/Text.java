package me.silviogames.geha;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;

/**
 * @author Silvio
 */
public class Text
{
	private static final String alphabetString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?:;\"#$%&'()*+-.,/<>=@[]\\_{}| ";
	private static final Array<Text> Texts = new Array<Text>();
	public static TextureRegion[] alphabet;
	public static ObjectIntMap<Character> characters = new ObjectIntMap<>(alphabetString.length());
	private static int[] offsets;
	private float x;
	private float y;
	private String message;
	private Color color;
	private float scale;
	private boolean contrast;
	private int contrast_length = 0;

	public Text()
	{
		this.x = 0;
		this.y = 0;
		this.message = "";
		this.color = Color.WHITE.cpy();
		this.scale = 1;
	}

	/**
	 * run this methods once when creating the app to initialize the alphabet.
	 */
	public static void init()
	{
		// <editor-fold defaultstate="collapsed" desc="load alphabet">
		// load alphabet
		alphabet = new TextureRegion[92];
		offsets = new int[92];
		int index = 0;
		for (int y = 0; y < 5; y++)
		{
			for (int x = 0; x < 21; x++)
			{
				if (index < alphabet.length)
				{
					alphabet[index] = new TextureRegion(Res.alphabet, x * 6, y * 8, 5, 7);
					offsets[index] = 5;
				}
				index++;
			}
		}
		offsets[8] = 4;
		offsets[11] = 4;
		offsets[12] = 6;
		offsets[13] = 6;
		offsets[19] = 6;
		offsets[21] = 6;
		offsets[22] = 6;
		offsets[23] = 6;
		offsets[24] = 6;
		offsets[25] = 6;
		offsets[28] = 4;
		offsets[34] = 4;
		offsets[35] = 4;
		offsets[37] = 4;
		offsets[38] = 6;
		offsets[44] = 4;
		offsets[45] = 4;
		offsets[47] = 4;
		offsets[48] = 6;
		offsets[49] = 4;
		offsets[51] = 4;
		offsets[53] = 4;
		offsets[62] = 2;
		offsets[64] = 2;
		offsets[65] = 3;
		offsets[66] = 4;
		offsets[67] = 6;
		offsets[68] = 6;
		offsets[70] = 6;
		offsets[71] = 3;
		offsets[72] = 3;
		offsets[73] = 3;
		offsets[74] = 6;
		offsets[75] = 6;
		offsets[76] = 4;
		offsets[77] = 3;
		offsets[78] = 3;
		offsets[80] = 4;
		offsets[81] = 4;
		offsets[82] = 4;
		offsets[83] = 6;
		offsets[84] = 4;
		offsets[85] = 4;
		offsets[88] = 4;
		offsets[89] = 4;
		offsets[90] = 2;
		offsets[91] = 3;

		int i = 0;
		for (char c : alphabetString.toCharArray())
		{
			characters.put(c, i);
			i++;
		}

	}

//	public static void render()
//	{
//		for (Text t : Texts)
//		{
//			t.renderText(Main.batch);
//		}
//		Texts.clear();
//	}

	/*
	 * converts string into index of array of char-images
	 */
	private static int charToIndex(char buchstabe)
	{
		return characters.get(buchstabe, 91);
	}

	private static int charOff(char s)
	{
		int i = charToIndex(s);
		if (i < offsets.length) return offsets[i];
		return 6;
	}

	public static String color(Color c) //color next word
	{
		return "§" + c.toString();
	}

	public static String color2(Color c) //color full string after that
	{
		return "°" + c.toString();
	}

	public static String int_to_count(int number)
	{
		if (number < 1)
		{
			return "xth";
		} else
		{
			if (number == 1)
			{
				return "1st";
			} else if (number == 2)
			{
				return "2nd";
			} else if (number == 3)
			{
				return "3rd";
			} else
			{
				return number + "th";
			}
		}
	}

	public static String shorten(String text, float scale, int width)
	{
		if (text != null)
		{
			if (length(text, scale) <= width)
			{
				return text;
			} else
			{
				String temp = text;
				while (true)
				{
					if (temp.length() < 3)
					{
						return temp + "...";
					} else
					{
						temp = temp.substring(0, temp.length() - 1);
						if (length(temp, scale) < width - 3)
						{
							return temp + "...";
						}
					}
				}
			}
		} else
		{
			return "null";
		}

	}

	public static float length(String text, float size)
	{
		float length = 0;
		if (text.length() > 0)
		{
			for (int i = 0; i < text.length(); i++)
			{
				char buchstabe = text.charAt(i);
				if (buchstabe == '§' || buchstabe == '°')
				{
					// color code found
					i += 8;
				} else
				{
					length += charOff(buchstabe) * size;
				}
			}
		}
		return length;
	}

	public static String[] wrap(String text, int width)
	{
		int indexDone = 0;
		Array<String> lineList = new Array<String>();
		String[] wordList = raw_text(text).split(" ");
		String[] rawWords = text.split(" ");
		Color fillcolor = null;
		String temp = "";
		String temp2 = "";
		boolean next_line = false, first_word_of_line = true;
		while (indexDone < wordList.length)
		{
			if (rawWords[indexDone].length() > 0)
			{
				if (rawWords[indexDone].charAt(0) == '°')
				{
					fillcolor = Color.valueOf(rawWords[indexDone].substring(1, 9));
				}
				// if (rawWords[indexDone].charAt(0) == '§') fillcolor = null;

				if (rawWords[indexDone].charAt(0) == '^') next_line = true;
			}
			if (first_word_of_line || (Text.length(temp, 1f) + Text.length(wordList[indexDone], 1f) <= width && !next_line))
			{
				first_word_of_line = false;
				// next word fits on the temp string
				temp = temp + wordList[indexDone] + " ";
				if (temp2.equals("") && fillcolor != null)
				{
					temp2 = "°" + fillcolor;
				}
				temp2 = temp2 + rawWords[indexDone] + " ";
				indexDone++;
				if (indexDone == wordList.length)
				{
					lineList.add(temp2);
				}
			} else
			{
				if (next_line)
				{
					indexDone++;
				}

				next_line = false;
				// temp string is full
				lineList.add(temp2);
				first_word_of_line = true;
				temp = "";
				temp2 = "";
			}
		}
		for (int i = 0; i < lineList.size; i++)
		{
			lineList.set(i, lineList.get(i).trim());
		}
		return lineList.toArray(String.class);
	}

	public static String[] stringBreak(String string, int maxChar)
	{
		Array<String> subLines = new Array<String>();

		int length = string.length();
		int start = 0;
		int end = maxChar;
		if (length > maxChar)
		{
			int noOfLines = (length / maxChar) + 1;

			int[] endOfStr = new int[noOfLines];

			for (int f = 0; f < noOfLines - 1; f++)
			{

				int end1 = maxChar;

				endOfStr[f] = end;

				if (string.charAt(end - 1) != ' ')
				{

					if (string.charAt(end - 2) == ' ')
					{

						subLines.add(string.substring(start, end - 1));
						start = end - 1;
						end = end - 1 + end1;

					} else if (string.charAt(end - 2) != ' '
							&& string.charAt(end) == ' ')
					{

						subLines.add(string.substring(start, end));
						start = end;
						end = end + end1;

					} else if (string.charAt(end - 2) != ' ')
					{

						subLines.add(string.substring(start, end) + "-");
						start = end;
						end = end + end1;

					} else if (string.charAt(end + 2) == ' ')
					{
						//System.out.println( "m here ............" );
						int lastSpaceIndex = string.substring(start, end)
								.lastIndexOf("");
						subLines.add(string.substring(start, lastSpaceIndex));

						start = lastSpaceIndex;
						end = lastSpaceIndex + end1;
					}

				} else
				{

					subLines.add(string.substring(start, end));
					start = end;
					end = end + end1;
				}

			}
			subLines.add(string.substring(endOfStr[noOfLines - 2], length));
		}
		return subLines.toArray(String.class);
	}

	/**
	 * @param text that should be cleared of color codes
	 * @return text without color codes in it.
	 */
	public static String raw_text(String text)
	{
		return text.replaceAll("([§°]).{8}", "");
	}

	public static void dispose()
	{

	}

//	@Deprecated
//	public static void add(String text, float x, float y)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, Color.WHITE, false, 1, false);
//		addText(t);
//	}
//
//	@Deprecated
//	private static void addText(Text text)
//	{
//		Texts.add(text);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, Color color)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, color, false, 1, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, boolean centered)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, Color.WHITE, centered, 1, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, float scale)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, Color.WHITE, false, scale, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, Color color, boolean centered)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, color, centered, 1, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, Color color, float scale)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, color, false, scale, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, boolean centered, float scale)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, Color.WHITE, centered, scale, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, Color color, boolean centered, float scale)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, color, centered, scale, false);
//		addText(t);
//	}
//
//	@Deprecated
//	public static void add(String text, float x, float y, Color color, boolean centered, float scale, boolean contrast)
//	{
//		Text t = textPool.obtain();
//		t.initText(x, y, text, color, centered, scale, contrast);
//		addText(t);
//	}

	public static String item_count(int amount)
	{
		if (amount > 999)
		{
			if (amount > 9999)
			{
				int thousands = MathUtils.floor(amount / 10000f);
				return thousands + "KKx";
			} else
			{
				int thousands = MathUtils.floor(amount / 1000f);
				return thousands + "Kx";
			}
		} else
		{
			return amount + "x";
		}
	}

//	@Deprecated
//	public static void draw_wrapped_text(String[] wrapped_text, int start_x, int start_y, boolean centered, int line_margin, Color text_color, float text_scale)
//	{
//		if (wrapped_text != null)
//		{
//			for (int i = 0; i < wrapped_text.length; i++)
//			{
//				add(wrapped_text[i], start_x, start_y - (i * line_margin), text_color, centered, text_scale);
//			}
//		}
//	}

	private static float contrast_length(String text, float size)
	{
		float length = 2 * size;
		if (text.length() > 0)
		{
			for (int i = 0; i < text.length(); i++)
			{
				char buchstabe = text.charAt(i);
				if (buchstabe == '§' || buchstabe == '°')
				{
					// color code found
					i += 8;
				} else
				{
					length += charOff(buchstabe) * size;
				}
			}
		}
		return length;
	}

	/**
	 * convert the chars of the string to the index and draw the char from the
	 * array alphabet
	 */
//	private void renderText(SpriteBatch b)
//	{
//		if (message.length() > 0)
//		{
//			if (contrast)
//			{
//				b.setColor(0, 0, 0, 0.4f * color.a);
//				b.draw(Res.pixel, x - (scale), y - (scale), contrast_length, 8 * scale);
//			}
//
//			char buchstabe;
//			b.setColor(color);
//			float off = 0;
//			for (int i = 0; i < message.length(); i++)
//			{
//				buchstabe = message.charAt(i);
//				if (buchstabe == '§')
//				{
//					Color temp = Color.valueOf(message.substring(i + 1, i + 9));
//					temp.a = color.a;
//					b.setColor(temp);
//					i += 8;
//				} else if (buchstabe == '°')
//				{
//					float temp_a = color.a;
//					this.color = Color.valueOf(message.substring(i + 1, i + 9));
//					this.color.a = temp_a;
//					b.setColor(color);
//					i += 8;
//				} else
//				{
//					if (buchstabe == ' ')
//					{
//						b.setColor(color);
//					}
//					b.draw(alphabet[charToIndex(buchstabe)], x + off, y, 0, 0, 5, 6, scale, scale, 0);
//					off += charOff(buchstabe) * scale;
//				}
//			}
//			b.setColor(Color.WHITE);
//		}
//	}
	public static void render(int posx, int posy, String txt, Color color, boolean contrast, float scale)
	{
		if (txt.length() > 0)
		{
			Color temp = new Color();
			if (contrast)
			{
				Main.batch.setColor(0, 0, 0, 0.4f * color.a);
				Main.batch.draw(Res.pixel, posx - (scale), posy - (scale), contrast_length(txt, scale), 8 * scale);
			}

			char buchstabe;
			Main.batch.setColor(color);
			float off = 0;
			for (int i = 0; i < txt.length(); i++)
			{
				buchstabe = txt.charAt(i);
				if (buchstabe == '§')
				{
					temp = Color.valueOf(txt.substring(i + 1, i + 9));
					temp.a = color.a;
					Main.batch.setColor(temp);
					i += 8;
				} else if (buchstabe == '°')
				{
					// keep alpha of first color
					float temp_a = color.a;
					color = Color.valueOf(txt.substring(i + 1, i + 9));
					color.a = temp_a;
					Main.batch.setColor(color);
					i += 8;
				} else
				{
					if (buchstabe == ' ')
					{
						Main.batch.setColor(color);
					}
					Main.batch.draw(alphabet[charToIndex(buchstabe)], posx + off, posy, 0, 0, 5, 6, scale, scale, 0);
					off += charOff(buchstabe) * scale;
				}
			}
			Main.batch.setColor(Color.WHITE);
		}
	}

	public static void cdraw(String content, int posx, int posy, Color color, float scale)
	{
		posx = (int) (posx - ((Text.length(content, scale)) / 2f));
		draw(content, posx, posy, color, scale);
	}

	public static void cdraw(String content, int posx, int posy, Color color)
	{
		posx = (int) (posx - ((Text.length(content, 1f)) / 2f));
		draw(content, posx, posy, color, 1f);
	}

	public static void cdraw(String content, int posx, int posy)
	{
		posx = (int) (posx - ((Text.length(content, 1f)) / 2f));
		draw(content, posx, posy, Color.WHITE, 1f);
	}

	public static void draw(String content, int posx, int posy)
	{
		draw(content, posx, posy, Color.WHITE, 1f);
	}

	public static void draw(String content, int posx, int posy, Color color)
	{
		draw(content, posx, posy, color, 1f);
	}

	public static void draw(String content, int posx, int posy, Color color, float scale)
	{
		if (content.equals("")) return;

		int found_index = Main.smx_text_data.find_free_line_index();

		// fill/replace data for text parameters
		Main.smx_text_data.set(found_index, TEXT_DATA.STATUS.offset, 1);
		Main.smx_text_data.set(found_index, TEXT_DATA.X_POS.offset, posx);
		Main.smx_text_data.set(found_index, TEXT_DATA.Y_POS.offset, posy);
		Main.smx_text_data.set(found_index, TEXT_DATA.COLOR_R.offset, MathUtils.floor(color.r * 255f));
		Main.smx_text_data.set(found_index, TEXT_DATA.COLOR_G.offset, MathUtils.floor(color.g * 255f));
		Main.smx_text_data.set(found_index, TEXT_DATA.COLOR_B.offset, MathUtils.floor(color.b * 255f));
		Main.smx_text_data.set(found_index, TEXT_DATA.SCALE.offset, MathUtils.round(scale * 100f));

		// fill/replace content
		for (int i = 0; i < content.length(); i++)
		{
			char buchstabe = content.charAt(i);
			int smx_offset = i + TEXT_DATA.CONTENT.offset;
			if (smx_offset < Main.smx_text_data.width)
			{
				Main.smx_text_data.set(found_index, smx_offset, (int) buchstabe);
			} else
			{
				System.out.println("text to long for renderer!");
			}
			if (i == content.length() - 1)
			{
				// this is the last entry
				if (smx_offset + 1 < Main.smx_text_data.width)
				{
					// check if the next entry is still in line
					// and put a -1 to stop rendering here,
					// there are characters after but they can stay there, either will be overridden
					// or not rendered
					Main.smx_text_data.set(found_index, smx_offset + 1, -1);
				}
			}
		}
	}

	public static void render_from_text_smx(Smartrix textdata, int line)
	{
		// check if entry is valid, but maybe that was checked before but is cheap.
		// iterate over the payload until the first -1 value is found
		// now either the stored ints in the payload directly are the offsets into the alphabet textureregion array.
		// but since I want to do inline color change I need to know the character
		// TODO: 26.05.23 there seems to be a bug here that did not occur very often in this project,
		//  but it did in project_delivery, every frame one of the texts on the screen flickers and that flicker effect moves through the texts, can be observed sometimes when debug_rendering a value on every tile of the arena grid

		if (textdata.get(line, TEXT_DATA.STATUS.offset) >= 0)
		{
			Color temp = new Color();
			//if (contrast)
			//{
			//	Main.batch.setColor(0, 0, 0, 0.4f * color.a);
			//	Main.batch.draw(Res.pixel, posx - (scale), posy - (scale), contrast_length(txt, scale), 8 * scale);
			//}
			float scale = textdata.get(line, TEXT_DATA.SCALE.offset) / 100f;

			char buchstabe;
			Main.batch.setColor(textdata.get(line, TEXT_DATA.COLOR_R.offset) / 255f, textdata.get(line, TEXT_DATA.COLOR_G.offset) / 255f, textdata.get(line, TEXT_DATA.COLOR_B.offset) / 255f, 1f);
			float off = 0;
			for (int i = 0; i < 90; i++)
			{
				int value = textdata.get(line, TEXT_DATA.CONTENT.offset + i);
				if (value == -1) break;
				buchstabe = (char) value;

				// inline color does not work in this version of text but I do not need it in this game
				if (buchstabe == '§')
				{
					//temp = Color.valueOf(txt.substring(i + 1, i + 9));
					//temp.a = color.a;
					//Main.batch.setColor(temp);
					//i += 8;
				} else if (buchstabe == '°')
				{
					// keep alpha of first color
					//float temp_a = color.a;
					//color = Color.valueOf(txt.substring(i + 1, i + 9));
					//color.a = temp_a;
					//Main.batch.setColor(color);
					//i += 8;
				} else
				{
					if (buchstabe == ' ')
					{
						//Main.batch.setColor(color);
					}
					Main.batch.draw(alphabet[charToIndex(buchstabe)], textdata.get(line, TEXT_DATA.X_POS.offset) + off, textdata.get(line, TEXT_DATA.Y_POS.offset), 0, 0, 5, 6, scale, scale, 0);
					off += charOff(buchstabe) * scale;
				}
			}
			Main.batch.setColor(Color.WHITE);
		}
	}

	public void initText(float x, float y, String message, Color color, boolean centered, float scale, boolean contrast)
	{
		this.x = x;
		this.y = y;
		this.message = message;
		if (color == null)
		{
			this.color = Color.WHITE.cpy();
		} else
		{
			this.color = color.cpy();
		}

		if (scale <= 0)
		{
			this.scale = 1;
		} else
		{
			this.scale = scale;
		}

		if (centered)
		{
			this.x = (int) (this.x - ((length(message, scale)) / 2));
		}

		this.contrast = contrast;
		if (contrast)
		{
			contrast_length = (int) contrast_length(message, scale);
		}
	}

	public enum TEXT_DATA
	{
		STATUS(0), // indicates status of entry, -1 means empty or not used anymore
		X_POS(1),
		Y_POS(2),
		COLOR_R(3), // 255
		COLOR_G(4), // 255
		COLOR_B(5), // 255
		SCALE(6), // in percent, 100 -> 1
		ORDER(7), // used for sorting pre rendering rendering
		CONTENT(8), // CHARACTERS OF THE STRING

		;

		// pre render sorting:
		// put all render candidates into a sublist
		// also the order value must be in a sublist

		public final int offset;

		TEXT_DATA(int offset)
		{
			this.offset = offset;
		}
	}

//	@Override
//	public void reset()
//	{
//		this.x = -10;
//		this.y = -10;
//		this.message = "";
//		this.scale = 1;
//		this.color = Color.WHITE.cpy();
//
//	}
}