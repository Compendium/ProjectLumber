package oz.wizards.lumber;

public class Level {
	public static final byte NOTHING = 0;
	public static final byte VILLAGE = 1;
	public static final byte FOREST = 2;
	
	public static final int dim = 64;
	private byte[] level = new byte[dim * dim];
	
	public void set (int x, int y, byte v) {
		if(x < 0 || y < 0 || x > dim || y > dim) {
			System.out.println("Error while set'ting in Level.java (out of bounds)");
		}
		level[x+y*dim] = v;		
	}
	
	public byte get (int x, int y) {
		if(x < 0 || y < 0 || x > dim || y > dim) {
			System.out.println("Error while get'ting in Level.java (out of bounds)");
		}
		return level[x+y*dim];
	}

}
