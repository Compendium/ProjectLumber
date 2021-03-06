package oz.wizards.lumber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.lumber.gfx.Shader;
import oz.wizards.lumber.gfx.Texture;
import oz.wizards.lumber.io.Log;
import oz.wizards.lumber.math.SimplexNoise;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

public class Level {
	public static final byte NOTHING = 0;
	public static final byte VILLAGE = 1;
	public static final byte VILLAGE_DESTROYED = 2;
	public static final byte FOREST = 3;
	public static final byte FOREST_DESTROYED = 4;

	public static int dim = 80;
	private byte[] level = new byte[dim * dim];
	private byte[] data = new byte[dim * dim];

	public void tick() {
		for(int x = 0; x < dim; x++) {
			for(int y = 0; y < dim; y++) {
				if(level[x+y*dim] == Level.FOREST_DESTROYED)
					data[x+y*dim]++;
				if(level[x+y*dim] == Level.FOREST_DESTROYED && data[x+y*dim] == 5) {
					set(x, y, Level.FOREST);
					data[x+y*dim] = 0;
				}
			}
		}
	}

	public void set(int x, int y, byte v) {
		if (x < 0 || y < 0 || x >= dim || y >= dim) {
			 System.out
			 .println("Error while set'ting in Level.java (out of bounds)");
			return;
		}
		level[x + y * dim] = v;
		Vector2f uvmin = new Vector2f(), uvmax = new Vector2f();
		if (v == Level.FOREST) {
			uvmin.x = 0 + 16;
			uvmin.y = 0 + 16*0;
			uvmax.x = 16 + 16;
			uvmax.y = 16 + 16*0;
		} else if (v == Level.VILLAGE) {
			uvmin.x = 0 + 16;
			uvmin.y = 0 + 16*1;
			uvmax.x = 16 + 16;
			uvmax.y = 16 + 16*1;
		} else if (v == Level.VILLAGE_DESTROYED) {
			uvmin.x = 0 + 16;
			uvmin.y = 0 + 16*2;
			uvmax.x = 16 + 16;
			uvmax.y = 16 + 16*2;
			// color = new Vector3f(1, 0, 0);
		} else if (v == Level.FOREST_DESTROYED) {
			uvmin.x = 0 + 16;
			uvmin.y = 0 + 16*3;
			uvmax.x = 16 + 16;
			uvmax.y = 16 + 16*3;
		} else {
			setQuad((x + y * dim) * 6 * 5, new Vector3f(0, 0, 0), new Vector3f(0,
					0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector2f(0,0),
					new Vector2f(0,0));
			return;
		}

		setQuad((x + y * dim) * 6 * 5, new Vector3f(0 + x, 0 + y, 1),
				new Vector3f(0 + x, 1 + y, 1), new Vector3f(1 + x, 0 + y, 1),
				new Vector3f(1 + x, 1 + y, 1), uvmin, uvmax);
	}

	public byte get(int x, int y) {
		if (x < 0 || y < 0 || x > dim || y > dim) {
			// System.out
			// .println("Error while get'ting in Level.java (out of bounds)");
			return -1;
		}
		return level[x + y * dim];
	}

	public void setData(int x, int y, byte v) {
		if (x < 0 || y < 0 || x > dim || y > dim) {
			// System.out
			// .println("Error while set'ting in Level.java (out of bounds)");
			return;
		}
		data[x + y * dim] = v;
	}

	public byte getData(int x, int y) {
		if (x < 0 || y < 0 || x > dim || y > dim) {
			// System.out
			// .println("Error while get'ting in Level.java (out of bounds)");
			return -1;
		}
		return data[x + y * dim];
	}

	private Shader shader;
	private int mPositionAttrib = 0;
	private int mTexcoordAttrib = 0;
	private int mTexUniform = 0;
	private int mMaxVertexCount = (dim * dim) * 6;
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = (3 + 2)
			* FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
	private Texture texture;
	public FloatBuffer buffer;

	public void setQuad(int startIndex, Vector3f a, Vector3f b, Vector3f c,
			Vector3f d, Vector2f uvmin, Vector2f uvmax) {
		//Vector2f realuvmin = new Vector2f((float) (1. / texture.width * uvmin.x),(float) (1. / texture.height * uvmin.y));
		//Vector2f realuvmax = new Vector2f((float) (1. / texture.width * uvmax.x), (float) (1. / texture.height * uvmax.y));
		Vector2f realuvmin = new Vector2f((float)(1./texture.width*uvmin.x) + (float)(0.1 / texture.width), (float)(1./texture.height*uvmin.y) + (float)(0.1 / texture.height));
		Vector2f realuvmax = new Vector2f((float)(1./texture.width*uvmax.x) - (float)(0.1 / texture.width), (float)(1./texture.height*uvmax.y) - (float)(0.1 / texture.height));

		addVertex(startIndex + 0 * 5, a, new Vector2f(realuvmin.x, realuvmax.y)); // lower
																					// left
		addVertex(startIndex + 1 * 5, b, new Vector2f(realuvmin.x, realuvmin.y)); // upper
																					// left
		addVertex(startIndex + 2 * 5, c, new Vector2f(realuvmax.x, realuvmax.y));// lower
																					// right

		addVertex(startIndex + 3 * 5, c, new Vector2f(realuvmax.x, realuvmax.y));// lower
																					// right
		addVertex(startIndex + 4 * 5, b, new Vector2f(realuvmin.x, realuvmin.y)); // upper
																					// left
		addVertex(startIndex + 5 * 5, d, new Vector2f(realuvmax.x, realuvmin.y));// upper
																					// right
	}

	public void addVertex(int index, Vector3f v, Vector2f uv) {
		buffer.put(index + 0, v.x);
		buffer.put(index + 1, v.y);
		buffer.put(index + 2, v.z);
		buffer.put(index + 3, uv.x);
		buffer.put(index + 4, uv.y);
	}
	
	private float interpolate(double a, double b, double x) {
		float ft = (float) (x * Math.PI);
		float f = (float) ((1-Math.cos(ft)) * .5f);
		return (float) (a*(1-f)+b*f);
	}
	
	private float interpolatedNoise(float x, float y) {
		int integer_X    = (int)(x);
		float fractional_X = x - integer_X;

		int integer_Y    = (int)(y);
		float fractional_Y = y - integer_Y;

		float v1 = SimplexNoise.noise(integer_X,     integer_Y);
		float v2 = SimplexNoise.noise(integer_X + 1, integer_Y);
		float v3 = SimplexNoise.noise(integer_X,     integer_Y + 1);
		float v4 = SimplexNoise.noise(integer_X + 1, integer_Y + 1);

		float i1 = interpolate(v1 , v2 , fractional_X);
		float i2 = interpolate(v3 , v4 , fractional_X);

		return interpolate(i1 , i2 , fractional_Y);
	}
	
	private float noise(float x, float y) {
		float total = 0;
		
		float frequency = .015f * 4;
		float amplitude = 2.0f;
		total = (float) (total + interpolatedNoise(x * frequency, y * frequency) * amplitude);
		
		frequency = .2f;
		amplitude = .6f;
		total = (float) (total + interpolatedNoise(x * frequency, y * frequency) * amplitude);
		
		frequency = 1.f;
		amplitude = .8f;
		total = (float) (total + interpolatedNoise(x * frequency, y * frequency) * amplitude);
		return total;
	}

	public void init(Shader s, Texture tex) {
		shader = s;
		texture = tex;

		buffer = ByteBuffer.allocateDirect(mMaxVertexCount * (3 * 4 + 2 * 4))
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mPositionAttrib = s.getAttributeLocation("position");
		mTexcoordAttrib = s.getAttributeLocation("texcoord");
		mTexUniform = s.getUniformLocation("texture");
		glUniform1i(mTexUniform, 0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.texId);

		for (int x = 0; x < dim; x++)
			for (int y = 0; y < dim; y++) {
				//float noise = (float) SimplexNoise.noise(x, y);
				float noise = noise(x,y);
				if (noise > 0.25) {
					set(x, y, Level.FOREST);
				} else if (noise < 0.05) {
					set(x,y, Level.VILLAGE);
				} else {
					set(x, y, Level.NOTHING);
				}
			}

	}

	public void render() {
		glEnableVertexAttribArray(mPositionAttrib);
		glEnableVertexAttribArray(mTexcoordAttrib);
		glUniform1i(mTexUniform, 0);
		glActiveTexture(GL_TEXTURE0);

		glBindTexture(GL_TEXTURE_2D, texture.texId);

		buffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
		glVertexAttribPointer(mPositionAttrib, 3, false, (3 * 4) + (2 * 4),
				buffer);

		buffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
		glVertexAttribPointer(mTexcoordAttrib, 2, false, (3 * 4) + (2 * 4),
				buffer);

		glDrawArrays(GL_TRIANGLES, 0, dim * dim * 6);

		glDisableVertexAttribArray(mPositionAttrib);
		glDisableVertexAttribArray(mTexcoordAttrib);

	}

}
