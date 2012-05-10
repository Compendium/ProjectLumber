package oz.wizards.lumber.math;

import org.lwjgl.util.vector.Vector2f;

public class Rectangle2f {
	public Vector2f min, max;
	
	public Rectangle2f (Vector2f a, Vector2f b) {
		this.min = a;
		this.max = b;
	}
	
	public boolean contains (Vector2f p) {
		if(p.x > min.x && p.x < max.x && p.y > min.y && p.y < max.y){
			return true;
		}
		return false;
	}

}
