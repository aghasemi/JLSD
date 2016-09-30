package io.aghasemi;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.GrayFilter;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;


public class JLSD {
	static
	{
		System.setProperty("jna.library.path", System.getProperty("user.dir"));
	}
	public interface LSD extends Library {
		final LSD INSTANCE = (LSD) Native.loadLibrary("lsd", LSD.class);
		public Pointer lsd_test(Pointer n);
		public Pointer lsd(Pointer n,Pointer img,int X,int Y); //n=int* , img=double*
		public Pointer lsd_scale(Pointer n,Pointer img,int X,int Y,double scale);
		
	}
	public static void main(String[] args) throws IOException {
		
		
		
		double[][] result=extractLines(ImageIO.read(new File("chairs.png")));
		System.out.println(result.length);
		System.out.println(Arrays.deepToString(result));
		
		final double r=16.0/9.0;
		final double d=19.0;
		final double ppi=177;
		
		final double npix=ppi*ppi*d*d*r/(1+r*r);
		final double ph=Math.sqrt(npix/r);
		final double pw=r*ph;
		
		System.out.printf("%f %f-%f :: %4.0fx%4.0f",npix,r*Math.sqrt(d*d/(1+r*r)),Math.sqrt(d*d/(1+r*r)),pw,ph);
		
		
	}
	public static double[][] extractLines(BufferedImage im) throws IOException {
		
		Memory ptr=new Memory(im.getHeight()*im.getWidth()*Double.BYTES);
		for (int x = 0; x < im.getWidth(); x++) {
			for (int y = 0; y < im.getHeight(); y++) {
				int rgb = im.getRGB(x, y);
		        int r = (rgb >> 16) & 0xFF;
		        int g = (rgb >> 8) & 0xFF;
		        int b = (rgb & 0xFF);

		        double val = (r+g+b)/3.0;
				ptr.setDouble(Double.BYTES*(x+y*im.getWidth()), val);
			}
		}
		
		Memory mem=new Memory(Integer.BYTES); //To hold the number of outputs
		Pointer out=LSD.INSTANCE.lsd(mem, ptr, im.getWidth(), im.getHeight());
		int n=mem.getInt(0);
		double[][] result=new double[n][7];
		for (int i = 0; i < n; i++) {
			double[] row=out.getDoubleArray(i*7*Double.BYTES, 7);
			result[i]=row;
			
		}
		
		//System.out.println(Arrays.toString(out.getDoubleArray(0, mem.getInt(0)*7)));
		mem.clear();
		ptr.clear();
		out.clear(n*7*Double.BYTES);
		return result;
	}

}
