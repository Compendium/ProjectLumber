package oz.wizards.lumber.gfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class VertexBuffer {
	class vertex_t {
		Vector3f pos;
		Vector2f tex;
		int type;
	}
	private int mVboid = -1;
	private int mPositionAttrib, mTexcoordAttrib, mTypeAttrib;
	private int mTexUniform;
	private Shader mShader;
	
	private int mVertexCount = 0;
	vertex_t [] vertices = new vertex_t [4];
	ByteBuffer buffer;
	
	public VertexBuffer (Shader s, int tex) {
		buffer = ByteBuffer.allocateDirect((3*4+2*4) * 4);
		mVboid = createVBOID();
		
		mShader = s;
		mPositionAttrib = s.getAttributeLocation("position");
		mTexcoordAttrib = s.getAttributeLocation("texcoord");
		mTexUniform = s.getUniformLocation("texture");
		glUniform1i(mTexUniform, 0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, tex);
		//mTypeAttrib = s.getAttributeLocation("aType");
	}
	
	public void add(Vector3f pos, Vector2f tex, byte type)
	{
		vertex_t toadd = new vertex_t();
		toadd.pos = pos;
		toadd.tex = tex;
		toadd.type = type;
		add(toadd);
	}
	
	public void add(vertex_t v) {
		if(mVertexCount+1 == vertices.length) {
			vertex_t [] newbuffer = new vertex_t[vertices.length * 2];
			for(int i = 0; i < vertices.length; i++)
				newbuffer[i] = vertices[i];
			vertices = newbuffer;
			
			ByteBuffer newbb = ByteBuffer.allocateDirect((vertices.length * (3*4+2*4) * 2));
			newbb.put(buffer);
			buffer = newbb;
		}
		
		vertices[mVertexCount] = v;
		mVertexCount++;
		
		buffer.putFloat(v.pos.x);
		buffer.putFloat(v.pos.y);
		buffer.putFloat(v.pos.z);
		buffer.putFloat(v.tex.x);
		buffer.putFloat(v.tex.y);
		buffer.putInt(v.type);
	}
	
	public void upload() throws IOException{
		bufferData(mVboid, buffer, ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
	}
	
	public void render(){
		mShader.enable();
		glEnable(GL_VERTEX_ARRAY);
		glEnableVertexAttribArray(mPositionAttrib);
		glEnableVertexAttribArray(mTexcoordAttrib);
		//glEnableVertexAttribArray(mTypeAttrib);
		glBindBuffer(GL_ARRAY_BUFFER, mVboid);
		glVertexAttribPointer(mPositionAttrib, 3, GL_FLOAT, false, 3*4+2*4, 0);
		glVertexAttribPointer(mTexcoordAttrib, 2, GL_FLOAT, false, 3*4+2*4, 3*4);
		//glVertexAttribPointer(mTypeAttrib, 1, GL_INT, false, 3*4+2*4+4, 3*4+2*4);
		glDrawArrays(GL_QUADS, 0, mVertexCount);
		//mShader.disable();
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDisable(GL_VERTEX_ARRAY);
		glDisableVertexAttribArray(mPositionAttrib);
		glDisableVertexAttribArray(mTexcoordAttrib);
	}
	
	private int createVBOID ()
	{
		if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
			IntBuffer buffer = BufferUtils.createIntBuffer(1);
			ARBVertexBufferObject.glGenBuffersARB(buffer);
			return buffer.get(0);
		}
		return -1;
	}
	
	private void bufferData (int id, ByteBuffer buffer, int usg)
	{
		if(GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
			//ARBVertexBufferObject.glBindBufferARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
			//ARBVertexBufferObject.glBufferDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer, usg);
			glBindBuffer(GL_ARRAY_BUFFER, id);
			glBufferData(GL_ARRAY_BUFFER, buffer, usg);
		}
	}
}
