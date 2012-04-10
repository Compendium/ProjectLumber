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

/**
 * A vertex batch, that uses vertex arrays to draw
 * @author compendium
 */
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
	 * @param tex The texture to use for drawing, can be null(TODO)
	 * @param a Upper Left
	 * @param b Lower Left
	 * @param c Upper Right
	 * @param d Lower Right
	 * @param uvmin The upper left texture coordinate in pixels
	 * @param uvmax The lower right texture coordinate in pixels
	 * @param rgb The tinting color
	 */
	public void putQuad (Texture tex, Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec2 uvmin, Vec2 uvmax, Vec3 rgb)
	{
		if(mVertexMap.containsKey(tex.texId) == false)
		{
			TextureInfo ti = new TextureInfo();
			ti.texId = tex.texId;
			//TODO add allocate-size for texture uv
			ti.vertices = ByteBuffer.allocateDirect((3 * FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES) * 6 * 1).order(ByteOrder.nativeOrder()).asFloatBuffer();
			ti.vertexCount = 0;
			ti.maxQuads = 1;
			mVertexMap.put(tex.texId, ti);
		}
		
		if(mVertexMap.get(tex.texId).vertexCount / 6 >= mVertexMap.get(tex.texId).maxQuads)
		{
			mVertexMap.get(tex.texId).maxQuads *= 2;
			FloatBuffer currentBuffer = mVertexMap.get(tex.texId).vertices;
			FloatBuffer newbuffer = ByteBuffer.allocateDirect((3*FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES) * 6 * mVertexMap.get(tex.texId).maxQuads).order(ByteOrder.nativeOrder()).asFloatBuffer();
			currentBuffer.rewind();
			newbuffer.put(currentBuffer);
			currentBuffer.rewind();
			mVertexMap.get(tex.texId).vertices = newbuffer;
		}
		
		Vec2 realuvmin = new Vec2((float)(1./tex.width*uvmin.x), (float)(1./tex.height*uvmin.y));
		Vec2 realuvmax = new Vec2((float)(1./tex.width*uvmax.x), (float)(1./tex.height*uvmax.y));
		
		//addTriangle(mVertexMap.get(texid), a, b, c, rgb);
		//addTriangle(mVertexMap.get(texid), c, b, d, rgb);
		TextureInfo ti = mVertexMap.get(tex.texId);
		
		addVertex(ti, a, rgb, realuvmin);
		addVertex(ti, b, rgb, new Vec2(realuvmin.x, realuvmax.y));
		addVertex(ti, c, rgb, new Vec2(realuvmax.x, realuvmin.y));
		
		addVertex(ti, c, rgb, new Vec2(realuvmax.x, realuvmin.y));
		addVertex(ti, b, rgb, new Vec2(realuvmin.x, realuvmax.y));
		addVertex(ti, d, rgb, realuvmax);
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
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_COLOR_ARRAY);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);

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
