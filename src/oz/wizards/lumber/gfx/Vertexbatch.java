package oz.wizards.lumber.gfx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL20.*;

import oz.wizards.lumber.math.Vec2;
import oz.wizards.lumber.math.Vec3;

public class Vertexbatch {
	public Vec3 translation = new Vec3();
	public Vec3 rotation = new Vec3();

	class TextureInfo
	{
		int texId;
		FloatBuffer vertices;
		int vertexCount;
		int maxQuads;
	}
	
	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 8 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_COLOR_OFFSET = 3;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3+3;
	
	private Map <Integer /*texids*/, TextureInfo /*vertices*/> mVertexMap;
	
	public Vertexbatch ()
	{
		mVertexMap = new HashMap<Integer, Vertexbatch.TextureInfo>();
	}
	
	/**
	 * Adds a quad (2 Triangles / 6 Vertices) to the rendering queue for later rendering with end()
	 * @param texid The (gl-) id of the texture to use. TODO has no use currently
	 * @param a Upper Left
	 * @param b Lower Left
	 * @param c Upper Right
	 * @param d Lower Right
	 * @param rgb The tinting color
	 */
	public void putQuad (int texid, Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec2 uvmin, Vec2 uvmax, Vec3 rgb)
	{
		if(mVertexMap.containsKey(texid) == false)
		{
			TextureInfo ti = new TextureInfo();
			ti.texId = texid;
			//TODO add allocate-size for texture uv
			ti.vertices = ByteBuffer.allocateDirect((3 * FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES) * 6 * 1).order(ByteOrder.nativeOrder()).asFloatBuffer();
			ti.vertexCount = 0;
			ti.maxQuads = 1;
			mVertexMap.put(texid, ti);
		}
		
		if(mVertexMap.get(texid).vertexCount / 6 >= mVertexMap.get(texid).maxQuads)
		{
			mVertexMap.get(texid).maxQuads *= 2;
			FloatBuffer currentBuffer = mVertexMap.get(texid).vertices;
			FloatBuffer newbuffer = ByteBuffer.allocateDirect((3*FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES) * 6 * mVertexMap.get(texid).maxQuads).order(ByteOrder.nativeOrder()).asFloatBuffer();
			currentBuffer.rewind();
			newbuffer.put(currentBuffer);
			currentBuffer.rewind();
			mVertexMap.get(texid).vertices = newbuffer;
		}
		
		//addTriangle(mVertexMap.get(texid), a, b, c, rgb);
		//addTriangle(mVertexMap.get(texid), c, b, d, rgb);
		TextureInfo ti = mVertexMap.get(texid);
		
		addVertex(ti, a, rgb, uvmin);
		addVertex(ti, b, rgb, new Vec2(uvmin.x, uvmax.y));
		addVertex(ti, c, rgb, new Vec2(uvmax.x, uvmin.y));
		
		addVertex(ti, c, rgb, new Vec2(uvmax.x, uvmin.y));
		addVertex(ti, b, rgb, new Vec2(uvmin.x, uvmax.y));
		addVertex(ti, d, rgb, uvmax);
	}
	
	public void end ()
	{
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glActiveTexture(GL_TEXTURE0);
		
		for(Entry<Integer, TextureInfo> cursor : mVertexMap.entrySet())
		{
			glBindTexture(GL_TEXTURE_2D, cursor.getKey());
			
			cursor.getValue().vertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
			glVertexPointer(3, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, cursor.getValue().vertices);
			cursor.getValue().vertices.position(TRIANGLE_VERTICES_DATA_COLOR_OFFSET);
			glColorPointer(3, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, cursor.getValue().vertices);
			cursor.getValue().vertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
			glTexCoordPointer(2, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, cursor.getValue().vertices);
			glDrawArrays(GL_TRIANGLES, 0, cursor.getValue().vertexCount+1);
			cursor.getValue().vertexCount = 0;
		}
	}

	public void addVertex(TextureInfo ti, Vec3 p, Vec3 rgb, Vec2 uv)
	{
		ti.vertices.put(ti.vertexCount * 8 + 0, p.x);
		ti.vertices.put(ti.vertexCount * 8 + 1, p.y);
		ti.vertices.put(ti.vertexCount * 8 + 2, p.z);
		ti.vertices.put(ti.vertexCount * 8 + 3, rgb.x);
		ti.vertices.put(ti.vertexCount * 8 + 4, rgb.y);
		ti.vertices.put(ti.vertexCount * 8 + 5, rgb.z);
		ti.vertices.put(ti.vertexCount * 8 + 6, uv.x);
		ti.vertices.put(ti.vertexCount * 8 + 7, uv.y);
		ti.vertexCount++;
	}
}
