package oz.wizards.lumber.gfx;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Font {
	private Texture tex;
	private Shader shader;
	private VertexBatch vertexBatch;
	private short [] kerningRight;
	private short [] kerningLeft;
	
	public Font (Texture fontImage, Shader shader) {
		this.tex = fontImage;
		this.shader = shader;
		this.vertexBatch = new VertexBatch(shader);
		kerningRight = new short [256];
		kerningLeft = new short [256];
	}
	
	public Font (Texture fontImage, VertexBatch vertexBatch) {
		this.tex = fontImage;
		this.shader = vertexBatch.getShader();
		this.vertexBatch = vertexBatch;
		kerningRight = new short [256];
		kerningLeft = new short [256];
	}
	
	public void init () {
		loadKerning();
	}
	
	public void draw (Vector2f position, float scale, String str) {
		Vector2f currentPosition = new Vector2f(position);
		float ratio = (float) 800 / (float) 600;
		
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int cy = c / 16;
			int cx = c % 16;
			int charPixelWidth = (8 - kerningLeft[c] - kerningRight[c]); //width of tha character in pixels
			//float charWidth = 1.f/(ratio*2) * (float)charPixelWidth; //'real' width, for the vertexbatch coordinates
			float charWidth = charPixelWidth;
			vertexBatch.putQuad(tex,
					new Vector3f(currentPosition.x, position.y, 0),
					new Vector3f(currentPosition.x, position.y + 8*scale, 0),
					new Vector3f(currentPosition.x + charWidth*scale, position.y, 0),
					new Vector3f(currentPosition.x + charWidth*scale, position.y + 8*scale, 0),
					new Vector2f(cx * 8 + kerningLeft[c], cy * 8),
					new Vector2f(cx * 8 + 8 - kerningRight[c], cy * 8 + 8),
					new Vector3f(1,1,1));
			currentPosition.x = currentPosition.x + charWidth*scale + 1.f * scale;
		}
	}
	
	public float getWidth (String str, float scale) {
		float width = 0.f;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			int charPixelWidth = (8 - kerningLeft[c] - kerningRight[c]);
			float charWidth = charPixelWidth * scale + 1.f * scale;
			width += charWidth;
		}
		return width;
	}
	
	private void loadKerning () {
		for(int row = 0; row <= 15; row++) {
			for(int col = 0; col <= 15; col++) {
				//left to right
				for(int pixelX = 0; pixelX < 8; pixelX++) {
					for(int pixelY = 0; pixelY < 8; pixelY++) {
						if(tex.getPixel((col*8)+pixelX, (row*8)+pixelY) == 0xffffffff) {
							kerningLeft[(row*16)+col] = (short) pixelX;
							pixelX = 8;
							pixelY = 8;
						}
					}
				}
			
				//right to left 
				for(int pixelX = 7; pixelX >= 0; pixelX--) {
					for(int pixelY = 7; pixelY >= 0; pixelY--) {
						if(tex.getPixel((col*8)+pixelX, (row*8)+pixelY) == 0xffffffff) {
							kerningRight[(row*16)+col] = (short) (7 - pixelX);
							pixelX = -1;
							pixelY = -1;
						}
					}
				}
			}
		}
		
		char c = 'r';
		int cy = c / 16;
		int cx = c % 16;
		System.out.printf("character: %c, cx: %d, cy: %d\n", c, cx, cy);
		System.out.printf("left offset: %d, right offset: %d\n", kerningLeft[(cy*16)+cx], kerningRight[(cy*16)+cx]);
	}
}
