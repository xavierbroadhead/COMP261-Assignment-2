package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {
	private int endY;
	private int startY;
	float[][] edges;
	public EdgeList(int startY, int endY) {
		this.startY=startY;
		this.endY = endY;
        this.edges=new float[4][endY*4];
	}

	public int getStartY() {
		return this.startY;
	}

	public int getEndY() {
		return this.endY;
	}

	public float getLeftX(int y) {
		return this.edges[0][y];
	}

	public float getRightX(int y) {
		return this.edges[1][y];
	}

	public float getLeftZ(int y) {
		return this.edges[2][y];
	}

	public float getRightZ(int y) {
		return this.edges[3][y];
	}
}

// code for comp261 assignments
