package oz.wizards.lumber;

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
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

import oz.wizards.lumber.gfx.Image;
import oz.wizards.lumber.gfx.Vertexbatch;
import oz.wizards.lumber.math.Vec2;
import oz.wizards.lumber.math.Vec3;

@SuppressWarnings("unused")
public class Game implements Runnable {

	public Vec3 translation = new Vec3();
	public Vec3 rotation = new Vec3();

	private boolean loop = true;

	Image tex;
	Vertexbatch vb;

	@Override
	public void run() {
		init();
		load();
		while (loop) {
			if (Display.isCloseRequested()) {
				loop = false;
				break;
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			tick();
			render();
			Display.update();
		}
		Display.destroy();
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		vb.putQuad(tex.texId,
				new Vec3(0.f, 0.f, -5.f),
				new Vec3(0.f, 1.f, -5.f),
				new Vec3(1.f, 0.f, -5.f),
				new Vec3(1.f, 1.f, -5.f),
				new Vec2(0.f, 0.f), new Vec2(1.f, 1.f),
				new Vec3(1.f, 1.f, 1.f));
		
//		for(int i = 0; i < 10; i+= 2)
//		{
//			vb.putQuad(tex.texId,
//					new Vec3(i+0.f, 0.f, -5.f),
//					new Vec3(i+0.f, 1.f, -5.f),
//					new Vec3(i+1.f, 0.f, -5.f),
//					new Vec3(i+1.f, 1.f, -5.f),
//					new Vec2(0.f, 0.f), new Vec2(1.f, 1.f),
//					new Vec3(i+1.f, 1.f, 1.f));
//		}
		vb.end();
	}

	private void tick() {
		// TODO Auto-generated method stub

	}

	private void init() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();

			// init OpenGL
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glViewport(0, 0, 800, 600);
			float ratio = (float) 800 / (float) 600;
			// Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 10);
			glFrustum(-ratio, ratio, -1, 1, 1, 100);
			glMatrixMode(GL_MODELVIEW);
			
			glEnable(GL_TEXTURE_2D);
			
			try {
			tex = new Image("res/ball.png");
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			vb = new Vertexbatch();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void load() {
	}
}