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

	public Vector3f translation = new Vector3f();
	public Vec3 rotation = new Vec3();

	private boolean loop = true;

	Texture tex;
	Vertexbatch vb;

	int prevx = -1, prevy = -1;
	int diffy = 0, diffx = 0;
	Vec2 m = new Vec2();
	long deltaTime = 0;
	long lastPrinted = 0;
	
	private static final int dim = 64;
	byte[] level = new byte[dim*dim];
	Shader normalShader;
	Shader billboardShader;
	VertexBuffer normalBuffer;
	VertexBuffer entityBuffer;

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
				lastPrinted = System.nanoTime() + 5L * 1000000000L;
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
		
		normalShader.enable();
		normalBuffer.render(GL_QUADS, translation);
		entityBuffer.render(GL_QUADS, translation);
		normalShader.disable();
		
		
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
		}
		if(Mouse.isButtonDown(0)){
			float vel = 0.1f;
			if (m.y < Display.getHeight() / 10) {
				 translation.x -= (float)Math.sin(rotation.y * (Math.PI /
				 180.0)) * vel;
				 translation.z += (float)Math.cos(rotation.y * (Math.PI /
				 180.0)) * vel;
				 translation.y += (float)Math.sin(rotation.x * (Math.PI /
				 180.0)) * vel;
				 //translation.z += 1;
			} else if (m.y > (Display.getHeight() / 10) * 9) {
				 translation.x += (float)Math.sin(rotation.y * (Math.PI /
				 180.0)) * vel;
				 translation.z -= (float)Math.cos(rotation.y * (Math.PI /
				 180.0)) * vel;
				 translation.y -= (float)Math.sin(rotation.x * (Math.PI /
				 180.0)) * vel;
				 //translation.z += -1;
			}
			if(m.x < Display.getWidth() / 10) {
				translation.x -= .1;
			} else if (m.x > (Display.getWidth() / 10) * 9) {
				translation.x += .1;
			}
		}
		
		float vel = 1.f;
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			translation.z -= vel;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			translation.z += vel;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			translation.x -= vel;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			translation.x += vel;
		}
		//System.out.println("" + rotation.y);
	}

	private void init() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
			//Display.setLocation(-1680, 0);

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
			//glEnable(GL_CULL_FACE);
			glDisable(GL_CULL_FACE);
			glFrontFace(GL_CW);
			
			//glEnable(GL_ALPHA_TEST);
			//glAlphaFunc(GL_EQUAL, 1.0f);
			//glDepthMask(false);
			
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glEnable(GL_BLEND);
			
			glEnable(GL_DEPTH_TEST);
			
			float k = 1.f / 255.f;
			glClearColor(k * 0x80, k * 0xa6, k * 0xa9, 1.0f);

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
		normalShader = new Shader("res/shaders/normal");
		
		normalBuffer = new VertexBuffer(normalShader, tex);
		entityBuffer = new VertexBuffer(normalShader, tex);
		
		for(int x = 0; x < dim; x++)
			for(int y = 0; y < dim; y++)
			{
				if(Math.random() > 0.9) {
					level[x + y*dim] = 2;
					
					normalBuffer.add(new Vector3f(0.f+x, 0.f, 0+y), new Vector2f(0.f, 16.f));
					normalBuffer.add(new Vector3f(1.f+x, 0.f, 0+y), new Vector2f(15.f, 16.f));
					normalBuffer.add(new Vector3f(1.f+x, 0.f, 1+y), new Vector2f(15.f, 31.f));
					normalBuffer.add(new Vector3f(0.f+x, 0.f, 1+y), new Vector2f(0.f, 31.f));
					
					entityBuffer.add(new Vector3f(0.f+x, 1.f, 0+y), new Vector2f(0.f, 0.f));
					entityBuffer.add(new Vector3f(1.f+x, 1.f, 1+y), new Vector2f(16.f, 0.f));
					entityBuffer.add(new Vector3f(1.f+x, 0.f, 1+y), new Vector2f(16.f, 16.f));
					entityBuffer.add(new Vector3f(0.f+x, 0.f, 0+y), new Vector2f(0.f, 16.f));
					
					entityBuffer.add(new Vector3f(0.f+x, 1.f, 1+y), new Vector2f(0.f, 0.f));
					entityBuffer.add(new Vector3f(1.f+x, 1.f, 0+y), new Vector2f(16.f, 0.f));
					entityBuffer.add(new Vector3f(1.f+x, 0.f, 0+y), new Vector2f(16.f, 16.f));
					entityBuffer.add(new Vector3f(0.f+x, 0.f, 1+y), new Vector2f(0.f, 16.f));
					
				}
				else {
					level[x+y*dim] = 1;
					
					normalBuffer.add(new Vector3f(0.f+x, 0.f, 0+y), new Vector2f(0.f, 32.f));
					normalBuffer.add(new Vector3f(1.f+x, 0.f, 0+y), new Vector2f(15.f, 32.f));
					normalBuffer.add(new Vector3f(1.f+x, 0.f, 1+y), new Vector2f(15.f, 47.f));
					normalBuffer.add(new Vector3f(0.f+x, 0.f, 1+y), new Vector2f(0.f, 47.f));
				}
			}
		
		try {
			normalBuffer.upload();
			entityBuffer.upload();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}