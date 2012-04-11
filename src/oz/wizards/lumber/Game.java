package oz.wizards.lumber;

import java.awt.Cursor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

import oz.wizards.lumber.gfx.Shader;
import oz.wizards.lumber.gfx.Texture;
import oz.wizards.lumber.gfx.VertexBuffer;
import oz.wizards.lumber.gfx.Vertexbatch;
import oz.wizards.lumber.math.Vec2;
import oz.wizards.lumber.math.Vec3;

@SuppressWarnings("unused")
public class Game implements Runnable {

	public Vec3 translation = new Vec3();
	public Vec3 rotation = new Vec3();

	private boolean loop = true;

	Texture tex;
	Vertexbatch vb;

	int prevx = -1, prevy = -1;
	int diffy = 0, diffx = 0;
	Vec2 m = new Vec2();
	long deltaTime = 0;
	long lastPrinted = 0;
	
	byte[] level = new byte[256*256];
	Shader vertexBufferShader;
	VertexBuffer vertexBuffer;

	@Override
	public void run() {
		init();
		load();
		while (loop) {
			deltaTime = System.nanoTime();
			if (Display.isCloseRequested()
					|| Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				loop = false;
				break;
			}

			//try {
				//Thread.sleep(10);
			//} catch (InterruptedException e) {
				//// TODO Auto-generated catch block
				//e.printStackTrace();
			//}

			tick();
			render();
			Display.update();
			deltaTime = System.nanoTime() - deltaTime;
			if(lastPrinted < System.nanoTime()) {
				lastPrinted = System.nanoTime() + 1L * 1000000000L;
				System.out.println("dt: " + ((double)deltaTime / 1000000.0) + " ms");
			}
		}
		Display.destroy();
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW_MATRIX);
		glPushMatrix();
		glLoadIdentity();

		glRotatef(rotation.x, 1.f, 0.f, 0.f);
		glRotatef(rotation.y, 0.f, 1.f, 0.f);
		glTranslatef(-translation.x, -translation.y, -translation.z);

		/*vb.putQuad(tex, new Vec3(0.f, 0.f, -5.f), new Vec3(0.f, 1.f, -5.f),
				new Vec3(1.f, 0.f, -5.f), new Vec3(1.f, 1.f, -5.f), new Vec2(
						0.f, 0.f), new Vec2(7.f, 7.f), new Vec3(1.f, 1.f, 1.f));
		vb.putQuad(tex, new Vec3(0.f, 0.f, 5.f), new Vec3(0.f, 1.f, 5.f),
				new Vec3(1.f, 0.f, 5.f), new Vec3(1.f, 1.f, 5.f), new Vec2(0.f,
						0.f), new Vec2(7.f, 7.f), new Vec3(1.f, 1.f, 1.f));
		for(int x = 0; x < 256; x++)
		{
			for(int z = 0; z < 256; z++)
			{
				if(level[x+z*256] == 1) {
					vb.putQuad(tex, new Vec3(x, 0.f, z+1),
						new Vec3(x, 0.f, z),
						new Vec3(x+1, 0.f, z+1),
						new Vec3(x+1, 0.f, z),
						new Vec2(0.f, 0.f),
						new Vec2(7.f, 7.f),
						new Vec3(1.f, 1.f, 1.f));
				}
				else if(level[x+z*256] == 2)
				{
					vb.putQuad(tex, new Vec3(x, 0.f, z+1),
						new Vec3(x, 0.f, z),
						new Vec3(x+1, 0.f, z+1),
						new Vec3(x+1, 0.f, z),
						new Vec2(0.f, 0.f),
						new Vec2(7.f, 7.f),
						new Vec3(1.f, 1.f, 1.f));
				}
			}
		}*/
		//vb.end();
		vertexBuffer.render();
		glPopMatrix();
	}

	private void tick() {
		diffx = Mouse.getX() - prevx;
		diffy = Mouse.getY() - prevy;
		prevx = Mouse.getX();
		prevy = Mouse.getY();
		m.x = Mouse.getX();
		m.y = Mouse.getY();

		if (Mouse.isButtonDown(2)) {
			rotation.x += -diffy * 1.f;
			rotation.y += diffx * 1.f;
			
			if(rotation.x > 90.0f) rotation.x = 90.0f;
			if(rotation.x < -90.0f) rotation.x = -90.0f;
		} else {
			float vel = 0.1f;
			if (m.y < Display.getHeight() / 10) {
				 translation.x -= (float)Math.sin(rotation.y * (Math.PI /
				 180.0)) * 0.05f;
				 translation.z += (float)Math.cos(rotation.y * (Math.PI /
				 180.0)) * 0.05f;
				 translation.y += (float)Math.sin(rotation.x * (Math.PI /
				 180.0)) * 0.05f;
				// translation.z += 1;
			} else if (m.y > (Display.getHeight() / 10) * 9) {
				 translation.x += (float)Math.sin(rotation.y * (Math.PI /
				 180.0)) * 0.05f;
				 translation.z -= (float)Math.cos(rotation.y * (Math.PI /
				 180.0)) * 0.05f;
				 translation.y -= (float)Math.sin(rotation.x * (Math.PI /
				 180.0)) * 0.05f;
				// translation.z += -1;
			}
		}
		//System.out.println("" + rotation.y);
	}

	private void init() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
			Display.setLocation(-1680, 0);

			// init OpenGL
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glViewport(0, 0, 800, 600);
			float ratio = (float) 800 / (float) 600;
			// Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 10);
			glFrustum(-ratio, ratio, -1, 1, 1, 100);
			glMatrixMode(GL_MODELVIEW);

			glEnable(GL_TEXTURE_2D);
			glDisable(GL_SMOOTH);
			glDisable(GL_CULL_FACE);
			//glFrontFace(GL_CW);

			try {
				tex = new Texture("res/tiles.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
			//vb = new Vertexbatch();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void load() {
		//vertexBufferShader = new Shader("res/shaders/vertexbuffer");
		vertexBuffer = new VertexBuffer();
		for(int x = 0; x < 256; x++)
			for(int y = 0; y < 256; y++)
			{
				if(Math.random() > 0.6)
					level[x + y*256] = 2;
				else
					level[x+y*256] = 1;
				//vertexBuffer.add(new Vector3f(x, 0.f, y+1));//, new Vector2f(0.f, 0.f), level[x+y*256]);
				//vertexBuffer.add(new Vector3f(x, 0.f, y));//, new Vector2f(0.f, 7.f), level[x+y*256]);
				//vertexBuffer.add(new Vector3f(x+1, 0.f, y+1));//, new Vector2f(7.f, 7.f), level[x+y*256]);
				//vertexBuffer.add(new Vector3f(x+1, 0.f, y));//, new Vector2f(7.f, 0.f), level[x+y*256]);
			}
		
		/*vb.putQuad(tex, new Vec3(0.f, 0.f, -5.f), new Vec3(0.f, 1.f, -5.f),
				new Vec3(1.f, 0.f, -5.f), new Vec3(1.f, 1.f, -5.f), new Vec2(
						0.f, 0.f), new Vec2(7.f, 7.f), new Vec3(1.f, 1.f, 1.f));
		vb.putQuad(tex, new Vec3(0.f, 0.f, 5.f), new Vec3(0.f, 1.f, 5.f),
				new Vec3(1.f, 0.f, 5.f), new Vec3(1.f, 1.f, 5.f), new Vec2(0.f,
						0.f), new Vec2(7.f, 7.f), new Vec3(1.f, 1.f, 1.f));*/
		
		//vertexBuffer.add(new Vector3f(0.f, 0.f, -5.f));//, new Vector2f(0.0f, 0.0f), (byte) 1);
		//vertexBuffer.add(new Vector3f(0.f, 1.f, -5.f));//, new Vector2f(0.f, 7.0f), (byte) 1);
		//vertexBuffer.add(new Vector3f(1.f, 0.f, -5.f));//, new Vector2f(7.f, 0.f), (byte) 1);
		//vertexBuffer.add(new Vector3f(1.f, 1.f, -5.f));//, new Vector2f(7.f, 7.f), (byte) 1);
				
		vertexBuffer.add(new Vector3f(0.f, 0.f, 5.f));//, new Vector2f(0.0f, 0.0f), (byte) 1);
		vertexBuffer.add(new Vector3f(0.f, 1.f, 5.f));//, new Vector2f(0.f, 7.0f), (byte) 1);
		vertexBuffer.add(new Vector3f(1.f, 0.f, 5.f));//, new Vector2f(7.f, 0.f), (byte) 1);
		vertexBuffer.add(new Vector3f(1.f, 1.f, 5.f));//, new Vector2f(7.f, 7.f), (byte) 1);
		
		try {
			vertexBuffer.upload();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}