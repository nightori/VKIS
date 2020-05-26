package vkis;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageUtils {
	private final int width, height;
	private final byte[] pixels;
	private final int pixlen;

	// get pixel array from image
	ImageUtils(BufferedImage image)
	{
		this.pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		this.width = image.getWidth();
		this.height = image.getHeight();
		// [B,G,R] or [A,B,G,R]
		this.pixlen = (image.getAlphaRaster() == null) ? 3 : 4;
	}

	// get specific color value at given position
	// there are built-in methods but this is much faster
	int getRGB(int x, int y, int color)
	{
		int pos = (y * pixlen * width) + (x * pixlen);
		int offset = (pixlen == 3) ? color : color+1;
		return pixels[pos+offset] & 0xff;
	}

	// roughly scale image to given size
	public static BufferedImage getMiniature(BufferedImage img, int width, int height) {
		Image tmp = img.getScaledInstance(width, height, Image.SCALE_FAST);
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g2d = resized.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return resized;
	}

	// get sum of 3 color differences
	public static long getDiff(ImageUtils sIU, ImageUtils cIU) {
		long diff = 0;
		for (int i = 0; i<sIU.width; i++) {
			for (int j = 0; j<sIU.height; j++) {
				int r = sIU.getRGB(i,j,2) - cIU.getRGB(i,j,2);
				int g = sIU.getRGB(i,j,1) - cIU.getRGB(i,j,1);
				int b = sIU.getRGB(i,j,0) - cIU.getRGB(i,j,0);
				diff += Math.abs(r) + Math.abs(g) + Math.abs(b);
			}
		}
		return diff;
	}
}
