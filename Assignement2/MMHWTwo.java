import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

public class MMHWTwo{

	int width = 640;
	int height = 480;
	long fps = 24;
	long len = 3*height*width;

	JFrame frame = new JFrame();;
	JLabel lbIm1;
	JLabel lbIm2;
	BufferedImage[] img = new BufferedImage[480] ;
	BufferedImage[] rimg = new BufferedImage[480] ;
	BufferedImage[] mimg = new BufferedImage[480] ;
	BufferedImage avg;

	GridBagLayout gLayout = new GridBagLayout();
	JLabel lbText1 = new JLabel("Video height"+height+" width"+ width);
	ArrayList<ImageIcon> images = new ArrayList<ImageIcon>();
	GridBagConstraints c = new GridBagConstraints();

	int black =0xff000000 | (0 << 16) | (0 << 8) | 0;
	int white= 0xff000000 | (255 << 16) | (255 << 8) | 255;
	int grey= 0xff000000 | (128 << 16) | (128 << 8) | 128;

	public void showIms(String[] args){

		frame.getContentPane().setLayout(gLayout);
		lbText1.setHorizontalAlignment(SwingConstants.CENTER);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		frame.getContentPane().add(lbText1, c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;

		int mode = Integer.parseInt(args[2]);
		if(mode == 1){

			mode1(args);
		}
		else if(mode == 0){
			mode0(args);
		}
	}

	public void mode0(String[] args){

		int[][][] sumr = new int[480][height][width];
		int[][][] sumg = new int[480][height][width];
		int[][][] sumb = new int[480][height][width];

		int[] medr = new int[480];
		int[] medg = new int[480];
		int[] medb = new int[480];

		try{
			File back_sub_input = new File(args[0]);
			File[] files1 = back_sub_input.listFiles();
			Arrays.sort(files1);

			File back_input = new File(args[1]);
			File[] files2 = back_input.listFiles();
			Arrays.sort(files2);

			avg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			for(int i=0; i<files1.length; i++){
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				File file1 = new File(args[0]+"/"+files1[i].getName());
				RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
				raf1.seek(0);
				byte[] bytes1 = new byte[(int) len];
				raf1.read(bytes1);

				int ind = 0;
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r1 = bytes1[ind];
						byte g1 = bytes1[ind+height*width];
						byte b1 = bytes1[ind+height*width*2]; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);

						int rf = (pix1 >> 16) & 0xFF;
						int gf = (pix1 >>  8) & 0xFF;
						int bf = (pix1 & 0xFF);

						sumr[i][y][x] = rf;
						sumg[i][y][x] = gf;
						sumb[i][y][x] = bf;

						ind++;
					}
				}
			}

			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					for(int i = 0; i<480; i++){
						medr[i] = sumr[i][y][x];
						medg[i] = sumg[i][y][x];
						medb[i] = sumb[i][y][x];
					}

					Arrays.sort(medr);
					Arrays.sort(medg);
					Arrays.sort(medb);

					int rf = (int)((medr[240] + medr[239])/2);
					int gf = (int)((medg[240] + medg[239])/2);
					int bf = (int)((medb[240] + medb[239])/2);
					int pixf = 0xff000000 | (rf << 16) | (gf << 8) | (bf);

					avg.setRGB(x,y,pixf);
				}
			}

			for(int i=0; i<files1.length; i++){
				mimg[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				rimg[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

				File file1 = new File(args[0]+"/"+files1[i].getName());
				RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
				raf1.seek(0);
				byte[] bytes1 = new byte[(int) len];
				raf1.read(bytes1);

				File file2 = new File(args[1]+"/"+files2[i].getName());
				RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
				raf2.seek(0);
				byte[] bytes2 = new byte[(int) len];
				raf2.read(bytes2);

				int ind = 0;
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r1 = bytes1[ind];
						byte g1 = bytes1[ind+height*width];
						byte b1 = bytes1[ind+height*width*2]; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);

						int red1 = (pix1 >> 16) & 0xFF;
						int green1 = (pix1 >>  8) & 0xFF;
						int blue1 = (pix1 & 0xFF);
						float[] hsv1 = Color.RGBtoHSB(red1,green1,blue1,null);

						int pavg = avg.getRGB(x,y);
						int ravg = (pavg >> 16) & 0xFF;
						int gavg = (pavg >>  8) & 0xFF;
						int bavg = (pavg & 0xFF);
						float[] hsvavg = Color.RGBtoHSB(ravg,gavg,bavg,null);

						float d = Math.abs((hsv1[0]*360) - (hsvavg[0]*360));

						if(d>40){
							mimg[i].setRGB(x,y,white);
						}else{
							if(Math.abs((hsv1[1]*100) - (hsvavg[1]*100)) > 5){
								mimg[i].setRGB(x,y,white);
							}else{
								mimg[i].setRGB(x,y,black);
							}
						}

						ind++;
					}
				}

				for(int y = 2; y < height-2; y++){
					for(int x = 2; x < width-2; x++){
						int pix1 = mimg[i].getRGB(x+1,y);
						int pix2 = mimg[i].getRGB(x,y);
						int pix3 = mimg[i].getRGB(x-1,y);
						int pix4 = mimg[i].getRGB(x,y+1);
						int pix5 = mimg[i].getRGB(x,y-1);

						if(pix1 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									mimg[i].setRGB(x+b,y+a,grey);
								}
							}
						}
						else if(pix3 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									mimg[i].setRGB(x-b,y-a,grey);
								}
							}
						}
						else if(pix4 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									mimg[i].setRGB(x+b,y+a,grey);
								}
							}
						}
						else if(pix5 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									mimg[i].setRGB(x-b,y-a,grey);
								}
							}
						}
					}
				}

				ind = 0;
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r1 = bytes1[ind];
						byte g1 = bytes1[ind+height*width];
						byte b1 = bytes1[ind+height*width*2]; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);

						byte r2 = bytes2[ind];
						byte g2 = bytes2[ind+height*width];
						byte b2 = bytes2[ind+height*width*2]; 
						int pix2 = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);

						int rf = (int) (((r1 & 0xff)*0.3) + ((r2 & 0xff)*0.7));
						int gf = (int) (((g1 & 0xff)*0.3) + ((g2 & 0xff)*0.7));
						int bf = (int) (((b1 & 0xff)*0.3) + ((b2 & 0xff)*0.7));
						int pixf = 0xff000000 | (rf << 16) | (gf << 8) | (bf);

						int mpix = mimg[i].getRGB(x,y);

						if(mpix == black){
							rimg[i].setRGB(x,y,pix2);
						}else if(mpix == white){
							rimg[i].setRGB(x,y,pix1);
						}else{
							rimg[i].setRGB(x,y,pixf);
						}

						ind++;
					}
				}
			}

			for(int i=0; i<files1.length; i++){
				long lStartTime = System.currentTimeMillis();
				ImageIcon image = new ImageIcon(rimg[i]);
				images.add(image);
				displayFrame(image, c);
				long lEndTime = System.currentTimeMillis();
				long output = lEndTime - lStartTime;

				try{
					long delay = (long) ((1000.0/fps) - output);
					Thread.sleep((delay>0)?delay:0);
				}
				catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				frame.getContentPane().removeAll();
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void mode1(String[] args){

		try{
			File fore_input = new File(args[0]);
			File[] files1 = fore_input.listFiles();
			Arrays.sort(files1);

			File back_input = new File(args[1]);
			File[] files2 = back_input.listFiles();
			Arrays.sort(files2);

			for(int i=0; i<files1.length; i++){
				img[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

				File file1 = new File(args[0]+"/"+files1[i].getName());
				RandomAccessFile raf1 = new RandomAccessFile(file1, "r");
				raf1.seek(0);
				byte[] bytes1 = new byte[(int) len];
				raf1.read(bytes1);

				File file2 = new File(args[1]+"/"+files2[i].getName());
				RandomAccessFile raf2 = new RandomAccessFile(file2, "r");
				raf2.seek(0);
				byte[] bytes2 = new byte[(int) len];
				raf2.read(bytes2);

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				int ind = 0;

				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r1 = bytes1[ind];
						byte g1 = bytes1[ind+height*width];
						byte b1 = bytes1[ind+height*width*2]; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);
						img[i].setRGB(x,y,pix1);

						int red = (int) ((pix1 >> 16) & 0xff);
						int green = (int)((pix1 >> 8) & 0xff);
						int blue = (int)(pix1 & 0xff);
						float[] hsv = Color.RGBtoHSB((int)red, (int)green, (int)blue, null);
						if(hsv[0] >= 0.23 && hsv[0] <= 0.47 && hsv[1] >= 0.3 && hsv[2] >= 0.17){
							img[i].setRGB(x,y,black);
						}else{
							img[i].setRGB(x,y,white);
						}

						ind++;
					}
				}

				for(int y = 2; y < height-2; y++){
					for(int x = 2; x < width-2; x++){
						int pix1 = img[i].getRGB(x+1,y);
						int pix2 = img[i].getRGB(x,y);
						int pix3 = img[i].getRGB(x-1,y);
						int pix4 = img[i].getRGB(x,y+1);
						int pix5 = img[i].getRGB(x,y-1);

						if(pix1 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									img[i].setRGB(x+b,y+a,grey);
								}
							}
						}else if(pix3 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									img[i].setRGB(x-b,y-a,grey);
								}
							}
						}else if(pix4 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									img[i].setRGB(x+b,y+a,grey);
								}
							}
						}else if(pix5 == white && pix2 == black){
							for(int a =0; a<3; a++){
								for(int b =0; b<3; b++){
									img[i].setRGB(x-b,y-a,grey);
								}
							}
						}
					}
				}

				ind = 0;
				for(int y = 0; y < height; y++){
					for(int x = 0; x < width; x++){
						byte r1 = bytes1[ind];
						byte g1 = bytes1[ind+height*width];
						byte b1 = bytes1[ind+height*width*2]; 
						int pix1 = 0xff000000 | ((r1 & 0xff) << 16) | ((g1 & 0xff) << 8) | (b1 & 0xff);

						byte r2 = bytes2[ind];
						byte g2 = bytes2[ind+height*width];
						byte b2 = bytes2[ind+height*width*2];
						int pix2 = 0xff000000 | ((r2 & 0xff) << 16) | ((g2 & 0xff) << 8) | (b2 & 0xff);

						int rf = (int) (((r1 & 0xff)*0.3) + ((r2 & 0xff)*0.7));
						int gf = (int) (((g1 & 0xff)*0.3) + ((g2 & 0xff)*0.7));
						int bf = (int) (((b1 & 0xff)*0.3) + ((b2 & 0xff)*0.7));
						int pixf = 0xff000000 | (rf << 16) | (gf << 8) | (bf);

						int cpix = img[i].getRGB(x,y);

						if(cpix == white){
							img[i].setRGB(x,y,pix1);
						}else if(cpix == black){
							img[i].setRGB(x,y,pix2);
						}else{
							img[i].setRGB(x,y,pixf);
						}

						ind++;
					}
				}
			}

			for(int i=0; i<files1.length; i++){
				long lStartTime = System.currentTimeMillis();
				ImageIcon image = new ImageIcon(img[i]);
				images.add(image);
				displayFrame(image, c);
				long lEndTime = System.currentTimeMillis();
				long output = lEndTime - lStartTime;

				try{
					long delay = (long) ((1000.0/fps) - output);
					Thread.sleep((delay>0)?delay:0);
				}
				catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				frame.getContentPane().removeAll();
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private void displayFrame(ImageIcon image, GridBagConstraints c){
		lbIm1 = new JLabel(image);
		frame.getContentPane().add(lbIm1, c);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args){
		MMHWTwo ren = new MMHWTwo();
		ren.showIms(args);
	}
}