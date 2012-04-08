package oz.wizards.lumber;

public class Main {

	private static Thread gameThread;
	private static Game g;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		g = new Game();
		gameThread = new Thread(g);
		gameThread.start();
	}

}
