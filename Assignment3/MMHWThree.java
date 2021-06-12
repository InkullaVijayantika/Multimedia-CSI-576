import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class MMHWThree{

	int width = 640;
	int height = 480;
	JFrame frame1;
	JFrame frame2;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage img1;
	BufferedImage img2;
	BufferedImage imgN = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage imgN1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage imgP = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage imgE = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	int[][] imgnY = new int[height][width];
	int[][] imgn1Y = new int[height][width];
	int blocksx = width/16;
	int blocksy = height/16;
	int[][][] mv = new int[blocksx][blocksy][2];

	public void showIms(String[] args){

		int k = Integer.parseInt(args[2]);


		try{
			int frameLen = width*height*3;
			long len = frameLen;

			File file1 = new File(args[0]);
			RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
			raf1.seek(0);
			byte[] bytes1 = new byte[(int) len];
			raf1.read(bytes1);

			int ind = 0;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					byte r = bytes1[ind];
					byte g = bytes1[ind+height*width];
					byte b = bytes1[ind+height*width*2];
					byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
					byte U = (byte)(0.596 * Byte.toUnsignedInt(r) - 0.274 * Byte.toUnsignedInt(g) -0.322 * Byte.toUnsignedInt(b));
					byte V = (byte)(0.211 * Byte.toUnsignedInt(r) - 0.523 * Byte.toUnsignedInt(g) + 0.312 * Byte.toUnsignedInt(b));
					int pix = 0xff000000 | ((Y & 0xff) << 16) | ((Y & 0xff) << 8) | (Y & 0xff);
					imgnY[y][x]= Byte.toUnsignedInt(Y);
					imgN.setRGB(x,y,pix);
					ind++;

				}
			}

			File file2 = new File(args[1]);
			RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
			raf2.seek(0);
			byte[] bytes2 = new byte[(int) len];
			raf2.read(bytes2);

			ind = 0;
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					byte r = bytes2[ind];
					byte g = bytes2[ind+height*width];
					byte b = bytes2[ind+height*width*2];
					byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
					byte U = (byte)(0.596 * Byte.toUnsignedInt(r) - 0.274 * Byte.toUnsignedInt(g) -0.322 * Byte.toUnsignedInt(b));
					byte V = (byte)(0.211 * Byte.toUnsignedInt(r) - 0.523 * Byte.toUnsignedInt(g) + 0.312 * Byte.toUnsignedInt(b));
					int pix = 0xff000000 | ((Y & 0xff) << 16) | ((Y & 0xff) << 8) | (Y & 0xff);
					imgn1Y[y][x]= Byte.toUnsignedInt(Y);
					imgN1.setRGB(x,y,pix);
					ind++;

				}
			}

			for(int y = 0; y < blocksy; y++){
				// blocksx = 0;
				for(int x = 0; x < blocksx; x++){
					bruteForce(imgnY, imgn1Y, x*16, y*16, k);
					// blocksx++;
				}
				// blocksy++;
			}
			// blocksy = 0;


			for(int y = 0; y < blocksy; y++){
				// blocksx = 0;
				for(int x = 0; x < blocksx; x++){
					displayImg(imgN, x*16, y*16);
					// blocksx++;
				}
				// blocksy++;
			}

			// blocksx = 0;
			// blocksy = 0;


		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}

		frame1 = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame1.getContentPane().setLayout(gLayout);
		lbIm1 = new JLabel(new ImageIcon(imgP));
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		frame1.getContentPane().add(lbIm1, c);
		frame1.setTitle("Reconstructed Frame");
		frame1.pack();
		frame1.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		for(int y = 0; y < height; ++y){
			for(int x = 0; x < width; ++x){
				int ppix= imgP.getRGB(x,y);
				int apix = imgN1.getRGB(x,y);
				int pix = Math.abs(ppix-apix);
				imgE.setRGB(x,y,pix);
			}
		}

		frame2 = new JFrame();
		frame2.getContentPane().setLayout(gLayout);
		lbIm2 = new JLabel(new ImageIcon(imgE));
		frame2.getContentPane().add(lbIm2, c);
		frame2.setTitle("Error Difference Frame");
		frame2.pack();
		frame2.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public void displayImg(BufferedImage imgN, int x, int y){
		for(int i = x; i< x+16; i++){
			for(int j = y; j< y+16; j++){
				int pix = imgN.getRGB(i+mv[x/16][y/16][1], j+mv[x/16][y/16][0]);
				imgP.setRGB(i,j,pix);
			}
		}
	}
	public void bruteForce(int[][] imgnY,int[][] imgn1Y, int x, int y, int k){
		
		long maxval = Long.MAX_VALUE;
		for(int j=(-k); j <= k; ++j){
			for(int i = (-k); i <= k; ++i){
				if(!inImg(j + y, i + x, width, height)){
					continue;
				}
				long sum = MAD(imgnY, imgn1Y, x, y, i+x, j+y);
				if(maxval > sum){
					maxval = sum;
					mv[x/16][y/16][0] = j;
					mv[x/16][y/16][1] = i;
					
				}
			}
		}

	}

	public long MAD(int[][] imgnY, int[][] imgn1Y, int x1, int y1, int x2, int y2){
		long sum = 0;
		for(int j=0; j < 16; j++){
			for(int i=0; i < 16; i++){
				sum += Math.abs((imgn1Y[y1+j][x1+i]&0xff) - (imgnY[y2+j][x2+i]&0xff));
			}

		}
		return sum;
	}

	public boolean inImg(int i, int j, int width, int height){
		if(i < 0 || j < 0){
			return false;
		}
		if(((i + 16) >= height)|| ((j + 16) >= width)){
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		MMHWThree ren = new MMHWThree();
		ren.showIms(args);
	}
}