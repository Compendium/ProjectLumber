package oz.wizards.lumber.gfx;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {
	public int texId;
	public int width;
	public int height;
	public boolean hasAlpha;

	public Texture(String path) throws IOException {
		InputStream in = new FileInputStream(path);
		ByteBuffer buf;
		try {
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			hasAlpha = decoder.hasAlpha();

			if (decoder.hasAlpha()) {
				buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
						* decoder.getHeight());
				decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			} else {
				buf = ByteBuffer.allocateDirect(3 * decoder.getWidth()
						* decoder.getHeight());
				decoder.decode(buf, decoder.getWidth() * 3, Format.RGB);
			}

			buf.flip();
		} finally {
			in.close();
		}

		texId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, (hasAlpha ? GL11.GL_RGBA
				: GL11.GL_RGB), width, height, 0, (hasAlpha ? GL11.GL_RGBA
				: GL11.GL_RGB), GL11.GL_UNSIGNED_BYTE, buf);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		
		System.out.println("Loaded texture " + texId + " (" + path + ") " + (hasAlpha ? "with alpha." : "without alpha."));
	}
}
