package oz.wizards.lumber;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.lumber.gfx.Shader;
import oz.wizards.lumber.gfx.Texture;

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

	public static int dim = 64;
	private byte[] level = new byte[dim * dim];
	private byte[] data = new byte[dim * dim];

	public void tick() {
		//dim = 63;
		/*for (int x = 0; x < level.length; x++) {
			for (int y = 0; y < level.length; y++) {
				if (getData(x, y) != 127) {
					setData(x, y, (byte) (getData(x, y) + 1));
				}
				// if(get(x,y) == VILLAGE_DESTROYED && getData(x,y) == 2) {
				// set(x,y, NOTHING);
				// setData(x,y, (byte) -127);
				// }

			}
		}
		for (int x = 1; x < level.length - 1; x++) {
			for (int y = 1; y < level.length - 1; y++) {
				if (get(x, y) == Level.VILLAGE && getData(x, y) == 4
						&& (get(x - 1, y) == Level.VILLAGE)) {
					// || get(x, y - 1) == Level.VILLAGE
					// || get(x - 1, y - 1) == Level.VILLAGE
					// || get(x + 1, y) == Level.VILLAGE
					// || get(x, y + 1) == Level.VILLAGE
					// || get(x + 1, y + 1) == Level.VILLAGE
					// || get(x + 1, y - 1) == Level.VILLAGE
					// || get(x - 1, y + 1) == Level.VILLAGE)) {
					set(x, y, Level.VILLAGE_DESTROYED);
					setData(x, y, (byte) 0);
				}
			}
		}*/
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
			putQuad((x + y * dim) * 6 * 5, new Vector3f(0, 0, 0), new Vector3f(0,
					0, 0), new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector2f(0,0),
					new Vector2f(0,0));
			return;
		}

		putQuad((x + y * dim) * 6 * 5, new Vector3f(0 + x, 0 + y, 0),
				new Vector3f(0 + x, 1 + y, 0), new Vector3f(1 + x, 0 + y, 0),
				new Vector3f(1 + x, 1 + y, 0), uvmin, uvmax);
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

	public void putQuad(int startIndex, Vector3f a, Vector3f b, Vector3f c,
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
				if (Math.random() > 0.285) {
					set(x, y, Level.FOREST);
				} else if (Math.random() < 0.4) {
					set(x, y, Level.VILLAGE);
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
