package renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		return (poly.getVertices()[1].minus(poly.getVertices()[0]))
				.crossProduct(poly.getVertices()[2].minus(poly.getVertices()[1])).z > 0;
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            The Vector3D pointing to the directional light read in from
	 *            the file.
	 * @param lightColor
	 *            The color of that directional light.
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Vector3D lightDirection, Color lightColor, Color ambientLight) {
		Vector3D[] vertices = poly.getVertices();
		
		//find normal
		Vector3D n = vertices[1].minus(vertices[0]).crossProduct(vertices[2].minus(vertices[1]));
		
		float angle = n.cosTheta(lightDirection);
		
		int red = (int) Math.abs(((ambientLight.getRed()/255.0f * poly.getReflectance().getRed()) + (lightColor.getRed()/255.0f * poly.getReflectance().getRed())) * angle);
		int green = (int) Math.abs(((ambientLight.getGreen()/255.0f * poly.getReflectance().getGreen()) + (lightColor.getGreen()/255.0f * poly.getReflectance().getGreen())) * angle);
		int blue = (int) Math.abs(((ambientLight.getBlue()/255.0f * poly.getReflectance().getBlue()) + (lightColor.getBlue()/255.0f * poly.getReflectance().getBlue())) * angle);
		if (red > 255) {
			red = red - 255;
		}
		if (green > 255) {
			green = green - 255;
		}
		if (blue > 255) {
			blue = blue - 255;
		}
		return new Color(red, green, blue);
	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 * 
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		Transform rotateMatrix = Transform.newXRotation(xRot).compose(Transform.newYRotation(yRot));
		return toScene(scene, rotateMatrix);
	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene, float x, float y, float z) {
		Transform translateMatrix = Transform.newTranslation(x, y, z);
		return toScene(scene, translateMatrix);
	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene, float x, float y, float z) {
		Transform scaleMatrix = Transform.newScale(x, y, z);
		return toScene(scene, scaleMatrix);
	}

	/** Converts the passed scene and its transformative matrix to the corresponding
	 *  scene with correct alterations made to it. 
	 * 
	 * 
	 * @param scene
	 * 				The scene we want to transform
	 * @param matrix
	 * 				The matrix by which we need to multiply our scene's vertices by
	 * @return
	 * 		 		An altered scene
	 */
	public static Scene toScene(Scene scene, Transform matrix) {
		
		Vector3D lightPos = scene.getLight();
		
		List<Polygon> polyList = new ArrayList<>();
		
		for (int i = 0; i < scene.getPolygons().size(); i++) {
			Vector3D[] buffer = new Vector3D[3];
			for (int j = 0; j < 3; j++) {
				buffer[j] = matrix.multiply(scene.getPolygons().get(i).getVertices()[j]);
			}
			Polygon bufferPoly = new Polygon(buffer[0], buffer[1], buffer[2], scene.getPolygons().get(i).getReflectance());
			polyList.add(bufferPoly);
		}
		return new Scene(polyList, lightPos);
	}
	/** Returns the bounding box of the passed scene
	 * 
	 * 
	 * @param s the Scene we want to change
	 * @return an array of floats in the order { maxX, minX, maxY, minY }
	 */
	public static float[] getBoundingBox(Scene s) {
		//initialise local buffer at their apex so we can find the actual max/min
		float maxX = Float.MIN_VALUE;
		float minX = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE;
		for (Polygon p : s.getPolygons()) {
			if (!isHidden(p)) {
				for (Vector3D v : p.getVertices()) {
					if (v.x > maxX) maxX = v.x;
					if (v.x < minX) minX = v.x;
					if (v.y > maxY) maxY = v.y;
					if (v.y < minX) minY = v.y;
				}
			}
		}
		return new float[] {maxX, minX, maxY, minY};
	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
	     Vector3D[] edges = poly.getVertices();
	     int minY= Integer.MAX_VALUE;
	     int maxY= Integer.MIN_VALUE;
	     System.out.print("\ninit min/max y: " + minY + "/" + maxY);
	     for(Vector3D v: edges) {
			 if (v.y < minY) minY = Math.round(v.y);
			 if (v.y > maxY) maxY = Math.round(v.y);
		 }
	     minY = Math.abs(minY);
	     maxY = Math.abs(maxY);
	     System.out.print("\npost min/max y: " + minY + "/" + maxY);
		 EdgeList edgeList = new EdgeList(minY, maxY);
	     Vector3D v1;
	     Vector3D v2;
	     for(int index = 0 ; index<3; index++){
	    	 	v1=edges[index];
	     	v2=edges[(index+1)%3];
	     	float slopeX= (v2.x-v1.x)/(Math.round(v2.y)-Math.round(v1.y));
			float slopeZ= (v2.z-v1.z)/(Math.round(v2.y)-Math.round(v1.y));
			float positionX = Math.abs(v1.x);
			float positionZ = Math.abs(v1.z);
	     	if(v1.y<v2.y) {
	     		int y= Math.round(Math.abs(v1.y));
	     		while (y<= Math.round(Math.abs(v2.y))) {

					System.out.print("\ny: " + y);
					System.out.print("\nxPos: " + positionX);
					System.out.print("\nzPos" + positionZ);
					System.out.print("\nleftX edgelist size: " + edgeList.edges[0].length);
					System.out.print("\nleftZ edgelist size: " + edgeList.edges[1].length);
					edgeList.edges[0][Math.abs(y - minY)]=positionX;
					edgeList.edges[2][Math.abs(y - minY)]=positionZ;
					positionX = positionX + slopeX;
					positionZ = positionZ + slopeZ;
					y++;
				}
			}
			else{
				int y=Math.round(Math.abs(v1.y));
				while(y>=Math.round(Math.abs(v2.y))){
					System.out.println("\ny: " + y + "\nminY: " + minY + "\nmaxY: " + maxY + "\nedgelist size: " + edgeList.edges.length);
					edgeList.edges[1][Math.abs(y - minY)]=positionX;
					edgeList.edges[3][Math.abs(y - minY)]=positionZ;
					positionX-=slopeX;
					positionZ-=slopeZ;
					y--;
				}
			}
		 }
	     return edgeList;
	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {
		
		int yMin = polyEdgeList.getStartY();
		int yMax = polyEdgeList.getEndY();
		
		for (int y = 0; y < yMax; y++) {
			if (y + yMin < 0 || y + yMin >= zbuffer[0].length) {
				y++;
				continue;
			}
			int rightX = (int) polyEdgeList.getRightX(y);
			int leftX = (int) polyEdgeList.getLeftX(y);
			float rightZ = (int) polyEdgeList.getRightZ(y);
			float leftZ = (int) polyEdgeList.getLeftZ(y);
			float slope = (rightZ - leftZ) / (rightX - leftX);
			
			while (leftX < rightX) {
 	            if (leftX < 0 || leftX >= zbuffer.length) {
 	                leftZ += slope;
 	                leftX++;
 	                continue;
 	            }           
				if (leftZ < zdepth[leftX][y + yMin]) {
					zdepth[leftX][y + yMin] = leftZ;
					zbuffer[leftX][y + yMin] = polyColor;
				}
				leftZ+= slope;
				leftX++;
			}
		}
	}
}

// code for comp261 assignments
