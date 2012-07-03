package oz.wizards.lumber.gfx;

import org.lwjgl.util.vector.Vector2f;

public class Font {
	Texture tex;
	Shader shader;
	VertexBatch vertexBatch;
	
	Font (Texture fontImage, Shader shader) {
		this.tex = fontImage;
		this.shader = shader;
		this.vertexBatch = new VertexBatch(shader);
	}
	
	Font (Texture fontImage, VertexBatch vertexBatch) {
		this.tex = fontImage;
		this.shader = vertexBatch.getShader();
		this.vertexBatch = vertexBatch;
	}
	
	void init () {
		
	}
	
	void draw (Vector2f position, float scale, String str) {
		for(int i = 0; i < 0; i++) {
			char c = str.charAt(i);
		}
	}
}
