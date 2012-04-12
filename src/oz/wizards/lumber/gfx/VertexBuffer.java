package oz.wizards.lumber.gfx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
	private int mVboid = -1;
	private int mPositionAttrib;
	private int mVertexCount = 0;
	private int mMaxVertexCount = 10;
	//FloatBuffer buffer;
	ByteBuffer buffer;
	
	public VertexBuffer () {
		//buffer = ByteBuffer.allocateDirect((3*4) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(mMaxVertexCount * 3 * 4).order(ByteOrder.nativeOrder());
	}
	
	public void add(Vector3f v) {
		if(mVertexCount == mMaxVertexCount) {
			mMaxVertexCount *= 2;
			//FloatBuffer newbb = ByteBuffer.allocateDirect((mMaxVertexCount * (3*4))).order(ByteOrder.nativeOrder()).asFloatBuffer();
			//newbb.put(buffer);
			//buffer = newbb;
			ByteBuffer newbb = ByteBuffer.allocateDirect(mMaxVertexCount * 3 * 4).order(ByteOrder.nativeOrder());
			newbb.rewind();
			newbb.put(buffer);
			buffer = newbb;
			System.out.println("Buffer-resize");
		}
		mVertexCount++;
		
		buffer.putFloat(v.x);
		buffer.putFloat(v.y);
		buffer.putFloat(v.z);
	}
	
	public void upload() throws IOException{
		mVboid = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, mVboid);
		buffer.flip();
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, 0, 0);
	}
	
	public void render(){
		//System.out.println(mVertexCount + "");
		glBindBuffer(GL_ARRAY_BUFFER, mVboid);
		glDrawArrays(GL_QUADS, 0, mMaxVertexCount);
	}
}
