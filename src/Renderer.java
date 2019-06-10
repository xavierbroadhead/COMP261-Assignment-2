package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import renderer.Scene.Polygon;

public class Renderer extends GUI {
	
	Scene scene;
	float rotateX = 0f;
	float rotateY = 0f;
	float scale = 1.0f;
	float rotateAngle = 0.1f;
	float translationFactor = 7.5f;
	Vector3D viewPos = new Vector3D(0f, 0f, 0f);
	
	@Override
	protected void onLoad(File file) {
		
		try(BufferedReader in = new BufferedReader(new FileReader(file))) {
			int numOfPolygons = Integer.parseInt(in.readLine());
			ArrayList<Polygon> polygons = new ArrayList<>();
			
			for (int i = 0; i < numOfPolygons; i++) {
				String line = in.readLine();
				String[] buffer = line.split(",");
				int[] RGB = new int[3];
				float[] vertices = new float[9];
				for (int j = 0; j < 3; j++) {
					RGB[j] = Integer.parseInt(buffer[j]);
				}
				for (int k = 3; k < 12; k++) {
					vertices[k - 3] = Float.parseFloat(buffer[k]);
				}
				polygons.add(new Polygon(vertices, RGB));
			}
			String[] bufferLightPos = in.readLine().split(",");
			this.scene = new Scene(polygons, new Vector3D(Float.parseFloat(bufferLightPos[0]),
														 Float.parseFloat(bufferLightPos[1]),
														 Float.parseFloat(bufferLightPos[2])));
			in.close();
			
		}
		
		catch(IOException e) {
			System.out.println(e);
			return;
		}
	}

	@Override
	protected void onKeyPress(KeyEvent ev) {
		this.rotateX = 0;
		this.rotateY = 0;
		this.viewPos = new Vector3D(0f,0f,0f);
		this.scale = 1.0f;
		char c = ev.getKeyChar();
		if (ev.getKeyCode() == KeyEvent.VK_UP) {
			this.rotateX += this.rotateAngle;
		}
		else if (ev.getKeyCode() == KeyEvent.VK_RIGHT) {
			this.rotateY += this.rotateAngle;
		}
		else if (ev.getKeyCode() == KeyEvent.VK_DOWN) {
			this.rotateX -= this.rotateAngle;
		}
		else if (ev.getKeyCode() == KeyEvent.VK_LEFT) {
			this.rotateY -= this.rotateAngle;
		}
		else if (ev.getKeyCode() == KeyEvent.VK_EQUALS) {
			this.scale+= 0.1f;
		}
		else if (ev.getKeyCode() == KeyEvent.VK_MINUS) {
			this.scale -= 0.1f;
		}
		else if (c == 'W' || c == 'w') {
			this.viewPos = this.viewPos.plus(new Vector3D(0f, -this.translationFactor, 0f));
		}
		else if (c == 'A' || c == 'a') {
			this.viewPos = this.viewPos.plus(new Vector3D(-this.translationFactor, 0f, 0f));
		}
		else if (c == 'S' || c == 's') {
			this.viewPos = this.viewPos.plus(new Vector3D(0f, this.translationFactor, 0f));
		}
		else if (c == 'D' || c == 'd') {
			this.viewPos = this.viewPos.plus(new Vector3D(this.translationFactor, 0f, 0f));
		}
	}

	@Override
	protected BufferedImage render() {
		Color[][] renderedImage = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float [][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
		for (int i = 0; i < CANVAS_WIDTH; i++) {
			for (int j = 0; j < CANVAS_HEIGHT; j++) {
				zdepth[i][j] = Float.POSITIVE_INFINITY;
				renderedImage[i][j] = new Color(130, 130, 130);
			}
		}
		
		if (this.scene == null) {
			return convertBitmapToImage(renderedImage);
		}
		this.scene = Pipeline.rotateScene(this.scene, this.rotateX, this.rotateY);
		
		this.scene = Pipeline.scaleScene(this.scene, this.scale, this.scale, this.scale);
		
		this.scene = Pipeline.translateScene(this.scene, this.viewPos.x, this.viewPos.y, this.viewPos.z);
		
		
		Color light = new Color(0,255,128);
		for (Scene.Polygon p : this.scene.getPolygons()) {
			if (Pipeline.isHidden(p)) {
				continue;
			}
			int[] ambientBuffer = getAmbientLight();
			EdgeList bufferEdgeList = Pipeline.computeEdgeList(p);
			Color shading = Pipeline.getShading(p, this.scene.getLight(), light, new Color(ambientBuffer[0], ambientBuffer[1], ambientBuffer[2]));
			Pipeline.computeZBuffer(renderedImage, zdepth, bufferEdgeList, shading);
		}
		return convertBitmapToImage(renderedImage);
	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
