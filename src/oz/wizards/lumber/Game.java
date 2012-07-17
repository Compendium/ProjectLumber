package oz.wizards.lumber;

import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLSync;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.lumber.gfx.Font;
import oz.wizards.lumber.gfx.ParticleEngine;
import oz.wizards.lumber.gfx.Shader;
import oz.wizards.lumber.gfx.Texture;
import oz.wizards.lumber.gfx.VertexBatch;
import oz.wizards.lumber.gfx.VertexBuffer;
import oz.wizards.lumber.io.KeyboardLayout;
import oz.wizards.lumber.io.Log;
import oz.wizards.lumber.math.Rectangle2f;
import oz.wizards.lumber.math.Vec2;
import oz.wizards.lumber.math.Vec3;

@SuppressWarnings("unused")
public class Game implements Runnable {

	public Vector3f translation = new Vector3f();
	public Vector3f rotation = new Vector3f();
	public Vector3f scale = new Vector3f(1, 1, 1);

	private boolean loop = true;

	Texture tex;
	Texture fontset;
	Texture tree;
	VertexBatch vbBackground;
	VertexBatch vbInterface;
	VertexBatch vbFont;

	int prevx = -1, prevy = -1;
	int diffy = 0, diffx = 0;
	Vector2f m = new Vector2f();
	long deltaTime = 0;
	long lastPrinted = 0;
	long lastTicked = 0;

	Level level = new Level();
	Shader normalShader;
	Shader tintShader;
	Shader billboardShader;
	VertexBuffer normalBuffer;
	VertexBuffer entityBuffer;
	ParticleEngine particleEngine;
	
	boolean houseSelected = false;
	Vector2f housePosition = new Vector2f(-1,-1);
	long houseSelectedTimestamp = 0;
	int woodcount = 0;

	KeyboardLayout kbl;
	
	Font font;

	@Override
	public void run() {
		kbl = new KeyboardLayout("keyboardlayout.txt");
		//Log.redirectOutputToFile = true;
		Log.enableFileOutput("tinyworld");

		init();
		load();
		while (loop) {
			deltaTime = System.nanoTime();
			if (Display.isCloseRequested()
					|| Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
				loop = false;
				break;
			}
			
			tick();
			render();
			Display.update();
			deltaTime = System.nanoTime() - deltaTime;
			if (lastPrinted < System.nanoTime()) {
				lastPrinted = System.nanoTime() + 5L * 1000000000L;
				Log.printf("Delta Time: %f ms, using %d/%d MB of Memory\n", ((double) deltaTime / 1000000.0), Runtime.getRuntime().totalMemory() / 1024 / 1024, Runtime.getRuntime().maxMemory() / 1024 / 1024);
			}
		}
		Display.destroy();
		Log.close();
	}

	private void render() {
		Vector2f offset = new Vector2f(0, 0);
		Vector2f uvmin = new Vector2f(0, 0);
		Vector2f uvmax = new Vector2f(0, 0);
		Vector2f bguvmin = new Vector2f(0, 32);
		Vector2f bguvmax = new Vector2f(16, 48);
		Vector3f color = new Vector3f(1, 1, 1);
		float ratio = (float) 800 / (float) 600;
		Vector2f c = new Vector2f((1.f / ((1.f / ratio)
				* (Display.getWidth() / 2) * scale.x))
				* (m.x - Display.getWidth() / 2) + translation.x,
				(1.f / ((Display.getHeight() / 2) * scale.y))
						* (m.y - Display.getHeight() / 2) + translation.y);
		Rectangle2f r = new Rectangle2f(new Vector2f(), new Vector2f());
		float zl = 0.f;
		for (int x = 0; x < Level.dim; x++) {
			for (int y = 0; y < Level.dim; y++) {
				// grass-background
				vbBackground.putQuad(tex, new Vector3f(0 + x, 0 + y, zl), new Vector3f(
						0 + x, 1 + y, zl), new Vector3f(1 + x, 0 + y, zl),
						new Vector3f(1 + x, 1 + y, zl), bguvmin, bguvmax,
						new Vector3f(1, 1, 1));
			}
		}
		//System.out.printf("Background iterations: %d * %d = %d\n", Level.dim, Level.dim, Level.dim * Level.dim);
		
		//interface
		{
			//cursor
			if(houseSelected) {
				long diff = (System.nanoTime()/1000 - houseSelectedTimestamp/1000);
				int x = (int) housePosition.x;
				int y = (int) housePosition.y;
				float z = 3.f;
				long speed = 50000;
				if(diff < speed*1) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(0, 96), new Vector2f(16,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*2) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(16, 96), new Vector2f(32,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*3) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(32, 96), new Vector2f(48,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*4) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(48, 96), new Vector2f(64,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*5) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(64, 96), new Vector2f(80,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*6) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(80, 96), new Vector2f(96,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*7) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(96, 96), new Vector2f(112,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*8) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(112, 96), new Vector2f(128,112), new Vector3f(1,1,1));
				}
				else if(diff < speed*9) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(0, 112), new Vector2f(16,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*10) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(16, 112), new Vector2f(32,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*11) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(32, 112), new Vector2f(48,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*12) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(48, 112), new Vector2f(64,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*13) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(64, 112), new Vector2f(80,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*14) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(80, 112), new Vector2f(96,128), new Vector3f(1,1,1));
				}
				else if(diff < speed*15) {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(96, 112), new Vector2f(112,128), new Vector3f(1,1,1));
				}
				else {
					vbInterface.putQuad(tex, new Vector3f(0+x, 0+y, z), new Vector3f(0+x, 1+y, z), 
							new Vector3f(1+x,0+y,z), new Vector3f(1+x,1+y,z), 
							new Vector2f(112, 112), new Vector2f(128,128), new Vector3f(1,1,1));
					houseSelectedTimestamp = System.nanoTime();
				}
			}
			
		}
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		//glEnable(GL_DEPTH_TEST);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glRotatef(rotation.x, 1.f, 0.f, 0.f);
		glRotatef(rotation.y, 0.f, 1.f, 0.f);
		glScalef(scale.x, scale.y, scale.z);
		glTranslatef(-translation.x, -translation.y, -translation.z);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(-ratio, ratio, -1, 1, -10, 10);
		
		tintShader.enable();
		vbBackground.render();
		tintShader.disable();
		
		normalShader.enable();
		level.render();
		normalShader.disable();
		
		tintShader.enable();
		particleEngine.render();
		vbInterface.render();
		tintShader.disable();
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 0, 600, -10, 10);
		//glDisable(GL_DEPTH_TEST);
		
		tintShader.enable();
		float size = 8*2.5f;
		float x,y,z;
		x = 800 - font.getWidth("Wood: " + woodcount, 2.f) - size;
		y = 600 - size;
		z = 1;
		vbFont.putQuad(tex,
				new Vector3f(x,y,z),
				new Vector3f(x,y+size,z), 
				new Vector3f(x+size,y,z),
				new Vector3f(x+size,y+size,z), new Vector2f(0,48), new Vector2f(7,55), new Vector3f(1,1,1));
		font.draw(new Vector2f(800 - font.getWidth("Wood: " + woodcount, 2.f),600-2*8-3), 2.f, "Wood: " + woodcount);
		vbFont.render();
		tintShader.disable();
	}

	boolean isUpdating = false;

	private void tick() {
		if (lastTicked < System.nanoTime() && !isUpdating) {
			lastTicked = System.nanoTime() + 1000000000L;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					isUpdating = true;
					level.tick();
					isUpdating = false;
					System.out.println("level thread t=" + Thread.currentThread().getId() + " finished");
				}
			});
			t.start();
			System.out.println("level thread t=" + t.getId() + " started");
		}

		diffx = Mouse.getX() - prevx;
		diffy = Mouse.getY() - prevy;
		prevx = Mouse.getX();
		prevy = Mouse.getY();
		m.x = Mouse.getX();
		m.y = Mouse.getY();

		/*
		 * if (Mouse.isButtonDown(1)) { rotation.x += -diffy * 1.f; rotation.y
		 * += diffx * 1.f;
		 * 
		 * if (rotation.x > 90.0f) rotation.x = 90.0f; if (rotation.x < -90.0f)
		 * rotation.x = -90.0f; }
		 */


		float vel = 0.1f;
		if (m.y < Display.getHeight() / 10) {
			translation.y += -vel * (1.f/scale.y);
		} else if (m.y > (Display.getHeight() / 10) * 9) {
			translation.y += vel * (1.f/scale.y);
		}
		if (m.x < Display.getWidth() / 10) {
			translation.x -= vel * (1.f/scale.x);
		} else if (m.x > (Display.getWidth() / 10) * 9) {
			translation.x += vel * (1.f/scale.x);
		}
		
		int md = Mouse.getDWheel();
		if (md > 0) {
			scale.x *= 1.1f;
			scale.y *= 1.1f;
		} else if (md < 0) {
			scale.x *= 0.9f;
			scale.y *= 0.9f;
		}
			
		while (Mouse.next()) {
			
			if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() == true) {
				float ratio = (float) 800 / (float) 600;
				Vector2f c = new Vector2f((1.f / ((1.f / ratio)
						* (Display.getWidth() / 2) * scale.x))
						* (m.x - Display.getWidth() / 2) + translation.x,
						(1.f / ((Display.getHeight() / 2) * scale.y))
								* (m.y - Display.getHeight() / 2)
								+ translation.y);
				int x = (int) Math.floor(c.x);
				int y = (int) Math.floor(c.y);
				//level.set((int) Math.floor(c.x), (int) Math.floor(c.y),
						//Level.NOTHING);
				System.out.println("x: " + Math.floor(c.x) + ", y: "
						+ Math.floor(c.y));
				
				if(level.get(x,y) == Level.VILLAGE) {
					houseSelected = true;
					housePosition.x = x;
					housePosition.y = y;
					houseSelectedTimestamp = System.nanoTime();
				} else if (level.get(x,y) == Level.FOREST && houseSelected) {
					particleEngine.add(new Vector3f(x+.5f,y+.5f,2), new Vector3f(housePosition.x+.5f,housePosition.y+.5f,2), 1.0f, 1, 1, 0.05f);
					level.set(x, y, Level.FOREST_DESTROYED);
					woodcount++;
				} else if (level.get(x,y) == Level.FOREST_DESTROYED && houseSelected) {
					particleEngine.add(new Vector3f(x+.5f,y+.5f,2), new Vector3f(housePosition.x+.5f,housePosition.y+.5f,2), 1.0f, 1, 1, 0.05f);
					level.set(x, y, Level.NOTHING);
					woodcount++;
				} else if (level.get(x,y) == Level.NOTHING) {
					houseSelected = false;
				}
			} else if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() == true) {
				float ratio = (float) 800 / (float) 600;
				Vector2f c = new Vector2f((1.f / ((1.f / ratio)
						* (Display.getWidth() / 2) * scale.x))
						* (m.x - Display.getWidth() / 2) + translation.x,
						(1.f / ((Display.getHeight() / 2) * scale.y))
								* (m.y - Display.getHeight() / 2)
								+ translation.y);
				int x = (int) Math.floor(c.x);
				int y = (int) Math.floor(c.y);
				System.out.print("x: " + x + ", y: "
						+ y);
				System.out.print(" id: "
						+ level.get(x, y));
				System.out.print(", tex: V[(" + 
						(level.buffer.get((x+y*63) * 6 * 5) + 3) +
						" | " + (level.buffer.get((x+y*63) * 6 * 5) + 4) + ")] \n");
				
			}

		}

		float velTranslation = 1.f;
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			translation.z -= velTranslation;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
			translation.z += velTranslation;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			translation.x -= velTranslation;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
			translation.x += velTranslation;
		}
		
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState() == true) {
				if (Keyboard.getEventKey() == kbl.get("reset")) {
					rotation = translation = new Vector3f(0, 0, 0);
				}
			}
			if (Keyboard.getEventKey() == Keyboard.KEY_P) {
				Vector2f mouse = new Vector2f(Mouse.getX(), Mouse.getY());
				FloatBuffer mvMat = ByteBuffer.allocateDirect(4 * (4 * 4))
						.order(ByteOrder.nativeOrder()).asFloatBuffer(), projMat = ByteBuffer
						.allocateDirect(4 * (4 * 4))
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				glGetFloat(GL_MODELVIEW_MATRIX, mvMat);
				glGetFloat(GL_PROJECTION_MATRIX, projMat);
				IntBuffer view = ByteBuffer.allocateDirect(4 * (4 * 4))
						.order(ByteOrder.nativeOrder()).asIntBuffer();
				glGetInteger(GL_VIEWPORT, view);

				FloatBuffer obj_pos = ByteBuffer.allocateDirect(4 * (4 * 4))
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				// float _mouseY = view.get(3) - mouse.y;
				float _mouseY = mouse.y;
				FloatBuffer fbuf = ByteBuffer.allocateDirect(4 * 2)
						.order(ByteOrder.nativeOrder()).asFloatBuffer();
				glReadPixels((int) mouse.x, (int) _mouseY, 1, 1,
						GL_DEPTH_COMPONENT, GL_FLOAT, fbuf);
				GLU.gluUnProject(mouse.x, _mouseY, fbuf.get(0), mvMat, projMat,
						view, obj_pos);
				System.out.print(obj_pos.get(0) + ", ");
				System.out.print(obj_pos.get(1) + ", ");
				System.out.print(obj_pos.get(2) + "\n");
				System.out.println("Scale(" + scale.toString() + ")");
			}
		}
		particleEngine.tick();
		// System.out.println("" + rotation.y);
	}

	private void init() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
			Display.setTitle("Tiny World");
			// Display.setLocation(-1680, 0);

			// init OpenGL
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glViewport(0, 0, 800, 600);
			float ratio = (float) 800 / (float) 600;
			// Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 10);
			// glFrustum(-ratio, ratio, -1, 1, 1, 100);
			//glOrtho(-ratio, ratio, -1, 1, -1, 1);
			glOrtho(0, 800, 0, 600, -1, 1);

			glMatrixMode(GL_MODELVIEW);

			glEnable(GL_TEXTURE_2D);
			glDisable(GL_SMOOTH);
			//glEnable(GL_CULL_FACE);
			glDisable(GL_CULL_FACE);
			glFrontFace(GL_CW);

			//glEnable(GL_ALPHA_TEST);
			//glAlphaFunc(GL_EQUAL, 1.0f);
			// glDepthMask(false);

			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glEnable(GL_BLEND);

			//glEnable(GL_DEPTH_TEST);

			float k = 1.f / 255.f;
			glClearColor(k * 0x80, k * 0xa6, k * 0xa9, 1.0f);

			try {
				tex = new Texture("res/tiles.png");
				fontset = new Texture("res/font.png");
				tree = new Texture("res/tree.png");
				System.out.println(tex.getPixel(0,0));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void load() {
		normalShader = new Shader("res/shaders/normal");
		tintShader = new Shader("res/shaders/tint");

		//normalBuffer = new VertexBuffer(tintShader, tex);
		//entityBuffer = new VertexBuffer(tintShader, tex);
		
		vbBackground = new VertexBatch(tintShader);
		
		vbInterface = new VertexBatch(tintShader);
		vbFont = new VertexBatch(tintShader);
		particleEngine = new ParticleEngine(tex, vbInterface);

		level.init(normalShader, tex);
		
		font = new Font(fontset, vbFont);
		font.init();
	}
}