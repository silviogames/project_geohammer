package me.silviogames.geha;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class ShaderLibrary
{
	private final Array<ShaderProgram> list_shaders = new Array<>();
	ShaderProgram shader_test;

	public ShaderLibrary()
	{
		shader_test = new ShaderProgram(Gdx.files.internal("shader_vertex_test.glsl"), Gdx.files.internal("shader_fragment_test.glsl"));

		System.out.println("shader compiled " + shader_test.isCompiled());

		list_shaders.add(shader_test);
	}

	public void dispose()
	{
		for (ShaderProgram sp : list_shaders)
		{
			sp.dispose();
		}
	}
}
