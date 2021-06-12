import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;

public class MMHWOne{
	JFrame frame;
	JLabel lbIm1;
	BufferedImage img;
	BufferedImage imgOne;
	private InputStream is;

	// Processing method
	public void processImg(String[] args) throws IOException{
		int newWidth =0 ;
		int newHeight = 0;
		int orgWidth=512;
		int orgHeight=512;

		//Taking input parameters
		File file = new File(args[0]);
		is = new FileInputStream(file);

		int n=3;
		long len=0;

		float s=Float.parseFloat(args [1]);
		int q = Integer.parseInt(args[2]);
		int m = Integer.parseInt(args[3]);

		len = file.length();
		byte[] bytes = new byte[(int)len];
		System.out.println("File read success");

		if(s == 1.0){
			newWidth = orgWidth;
			newHeight = orgHeight;
		}else{
			newWidth = (int)(orgWidth * s);
			newHeight = (int)(orgHeight * s);
		}

		
		//Needed declarations
		byte[][][] rgbin=new byte[orgHeight][orgWidth][3];
		byte[][][] rgbfil=new byte[orgHeight][orgWidth][3];
		byte[][][] rgbout=new byte[newHeight][newWidth][3];
		byte[] outputImg = new byte[(int)(newHeight*newWidth*3)];

		img = new BufferedImage(orgWidth, orgHeight, BufferedImage.TYPE_INT_RGB);
		imgOne = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

		int offset = 0;
		int numRead = 0;

		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
		{
			offset += numRead;
		}

		int index =0;
		rgbin=convertTo3D(bytes,orgWidth,orgHeight,index);

		//Filtering
		rgbfil=avgFilter(rgbin,n,orgWidth,orgHeight);
		//Interpolation by nearest neighbor
		int widthRatio = (int)((orgWidth<<16)/newWidth)+1;
		int heightRatio=(int)((orgHeight<<16)/newHeight)+1;

		int c = 0;
		for(int i=0;i<newHeight;i++){
			for(int j=0; j<newWidth;j++){
				rgbout[i][j][0]=rgbfil[((i * heightRatio) >> 16)][((j * widthRatio) >> 16)][0];
				rgbout[i][j][1]=rgbfil[((i * heightRatio) >> 16)][((j * widthRatio) >> 16)][1];
				rgbout[i][j][2]=rgbfil[((i * heightRatio) >> 16)][((j * widthRatio) >> 16)][2];

				outputImg[c] = rgbout[i][j][0];
				outputImg[c+(newHeight*newWidth)] = rgbout[i][j][1];
				outputImg[c+(2*newWidth*newHeight)] = rgbout[i][j][2];
				
				c++;
			}
		}

		//Converting byte (-128 to 127) to int (0-255) 
		int imageCopy[] = new int[3*newHeight*newWidth];
		int ind = 0;

		for(int i = 0; i < newHeight; i++){
			for(int j = 0; j < newWidth; j++){
				byte a = 0;
				byte r = outputImg[ind];
				if(r<0) {
                    imageCopy[(i*newWidth + j)*3] = r+256;
                } else {
                    imageCopy[(i*newWidth + j)*3] = r;
                }
                byte g = outputImg[ind+(newHeight*newWidth)];
                if(g<0) {
                    imageCopy[(i*newWidth + j)*3 + 1] = g+256;
                } else {
                    imageCopy[(i*newWidth + j)*3 + 1] = g;
                }
                byte b = outputImg[ind+(newHeight*newWidth*2)];
                if(b<0) {
                    imageCopy[(i*newWidth + j)*3 + 2] = b+256;
                } else {
                    imageCopy[(i*newWidth + j)*3 + 2] = b;
                }
                ind++;


			}
		}

		//Quantization
		c = 0;
		for(int i = 0; i < newHeight; i++){
			for(int j = 0; j < newWidth; j++){
				int k = i*newWidth + j;
				byte r,g,b;
				imageCopy[3*k] = quantization(imageCopy[3*k],q,m);
				imageCopy[3*k + 1] = quantization(imageCopy[3*k +1],q,m);
            	imageCopy[3*k + 2] = quantization(imageCopy[3*k + 2],q,m);
            	

            	if(imageCopy[3*k] > 127){
            		r = (byte)(imageCopy[3*k] - 256);
            	}else {
                	r = (byte)imageCopy[3*k];
            	}

            	if(imageCopy[3*k + 1] > 127) {
                	g = (byte)(imageCopy[3*k + 1] - 256);
            	} else {
                	g = (byte)imageCopy[3*k + 1];
            	}
            	if(imageCopy[3*k + 2] > 127) {
                	b = (byte)(imageCopy[3*k + 2] - 256);
            	} else {
                	b = (byte)imageCopy[3*k + 2];
            	}

            	outputImg[c] = r;
				outputImg[c+(newHeight*newWidth)] = g;
				outputImg[c+(2*newWidth*newHeight)] = b;
				c++;


			}
		}

		// int l =(newHeight*newWidth*3);
		// for(int x = 0; x<l; x++){
		// 	System.out.println(outputImg[x]+","+x);
		// }

		//Displaying image
		frame = new JFrame();
		GridBagLayout gLayout = new GridBagLayout();
		frame.getContentPane().setLayout(gLayout);
		lbIm1 = new JLabel(new ImageIcon(imgOne));
		GridBagConstraints con = new GridBagConstraints();
		con.fill = GridBagConstraints.HORIZONTAL;
		con.anchor = GridBagConstraints.CENTER;
		con.weightx = 0.5;
		con.gridx = 0;
		con.gridy = 0;
		frame.getContentPane().add(lbIm1, con);
		con.fill = GridBagConstraints.HORIZONTAL;
		con.gridx = 0;
		con.gridy = 1;

		while(true){
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int idx = 0;
		for(int y = 0; y < newHeight; y++)
		{
			for(int x = 0; x < newWidth; x++)
			{
				byte a = 0;
				byte r = outputImg[idx];
				byte g = outputImg[idx+newHeight*newWidth];
				byte b = outputImg[idx+newHeight*newWidth*2]; 

				int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
					//int pix = ((a << 24) + (r << 16) + (g << 8) + b);
				imgOne.setRGB(x,y,pix);
				idx++;
			}
		}

		frame.pack();
		frame.setVisible(true);
	}

		// System.out.println("Displaying Image");
	}

	//Log quantization method
	public static float logfn(float x){
		float res = (float)(1-((Math.log((1 + (255*(1-Math.abs(x))))))/(Math.log(256))));
		if(x<0){
			res = res*(-1);
			return res;
		}
		return res;
	}

	public static int quantization(int input, int q,int center){
		if(center == -1){
			int qcap = (int)Math.pow(2.0,(double)q);
			if(qcap< 256 && qcap>0){
				int span = 256/qcap;
				int level = (int)Math.floor(input/span);
				return span*level+(span/2);
			}
		}
		else{
			int qcap = (int)Math.pow(2.0,(double)q);
			if(center>=(int)(256/2)){
				center = 255 - center;
				float normlow = (float)(0 - center)/(255 - center);
				float high = 1;
				float val = (float)(1 - Math.abs(normlow))*(float)Math.log(256);
				float low = (float)(Math.exp(val)-256)/255;
				float span = (high-low)/qcap;
				// System.out.println(low+","+high+","+span);
				int[] intLow = new int[(int)(qcap)+1];
				int[] newLow = new int[(int)(qcap)+1];
				float i = low;
				int ctr = 0;
				while(ctr<(qcap+1)){
					intLow[ctr] = (int)Math.round((logfn(i)*(255 - center) + center));
					// System.out.println(intLow[ctr]);
					i+=span;
					ctr+=1;	
				}
			
				ctr= 0;
				while(ctr<(qcap+1)){
					intLow[ctr] = 255 - intLow[ctr] ;
					// System.out.println(intLow[ctr]);
					ctr+=1;
				}
				int j = qcap;
				ctr = 0;
				while(ctr<(qcap+1)){
					newLow[ctr] = intLow[j];
					// System.out.println(ctr+","+newLow[ctr]);
					ctr++;
					j--;
				}
				ctr = 0;
				while(ctr<(qcap+1)){
					if(input<=newLow[ctr]){
						if(ctr == 0){
							// System.out.println(ctr+","+(int)((newLow[ctr] + newLow[ctr + 1])/2));
							return (int)((newLow[ctr] + newLow[ctr + 1])/2);
						}else{
							// System.out.println(ctr+","+(int)((newLow[ctr] + newLow[ctr - 1])/2));
							return (int)((newLow[ctr] + newLow[ctr - 1])/2);
						}
						
					}
					ctr+=1;
				}
				

			}

			else{
				float normlow = (float)(0 - center)/(255 - center); 
				float val = (float)(1 - Math.abs(normlow))*(float)Math.log(256);
				float low = (float)(Math.exp(val)-256)/255;
				float high = 1;
				float span = (high-low)/qcap;
				int[] intLow = new int[(int)(qcap)+1];
				float i = low;
				int ctr = 0;
				while(ctr<(qcap+1)){
					intLow[ctr] = (int)Math.round((logfn(i)*(255 - center) + center));
					// System.out.println(ctr+","+intLow[ctr]);
					i+=span;
					ctr+=1;
				}
				ctr = 0;
				while(ctr<(qcap+1)){
					if(input<=intLow[ctr]){
						if(ctr == 0){
							// System.out.println(ctr+","+(int)((intLow[ctr] + intLow[ctr + 1])/2));
							return (int)((intLow[ctr] + intLow[ctr + 1])/2);
						}else{
							// System.out.println(ctr+","+(int)((intLow[ctr] + intLow[ctr - 1])/2));
							return (int)((intLow[ctr] + intLow[ctr - 1])/2);
						}
						
					}
					ctr+=1;
				}
			}

		}
		return input;
	}

	//Filtering method
	public byte[][][]avgFilter(byte[][][]img,int n,int Size_w,int Size_h){
		byte[] Imgr=new byte[n*n];
		byte[] Imgg=new byte[n*n];
		byte[] Imgb=new byte[n*n];

		byte[] Imgr1=new byte[(n-1)*(n-1)];
		byte[] Imgg1=new byte[(n-1)*(n-1)];
		byte[] Imgb1=new byte[(n-1)*(n-1)];

		byte[] Imgr2=new byte[(n-1)*(n-1)];
		byte[] Imgg2=new byte[(n-1)*(n-1)];
		byte[] Imgb2=new byte[(n-1)*(n-1)];

		byte[] Imgr3=new byte[(n-1)*(n-1)];
		byte[] Imgg3=new byte[(n-1)*(n-1)];
		byte[] Imgb3=new byte[(n-1)*(n-1)];

		byte[] Imgr4=new byte[(n-1)*(n-1)];
		byte[] Imgg4=new byte[(n-1)*(n-1)];
		byte[] Imgb4=new byte[(n-1)*(n-1)];

		byte[] Imgr5=new byte[n*(n-1)];
		byte[] Imgg5=new byte[n*(n-1)];
		byte[] Imgb5=new byte[n*(n-1)];

		byte[] Imgr6=new byte[n*(n-1)];
		byte[] Imgg6=new byte[n*(n-1)];
		byte[] Imgb6=new byte[n*(n-1)];

		for(int i=0;i<Size_h-(n-1);i++){
			for(int j=0;j<Size_w-(n-1);j++){
				for(int k=0;k<n;k++){
					for(int l=0;l<n;l++){
						Imgr[k*n+l]=img[i+k][j+l][0];
						Imgg[k*n+l]=img[i+k][j+l][1];
						Imgb[k*n+l]=img[i+k][j+l][2];
					}
				}
				img[i+(n>>1)][j+(n>>1)][0]=(byte) meanTake(Imgr,n*n);
				img[i+(n>>1)][j+(n>>1)][1]=(byte) meanTake(Imgg,n*n);
				img[i+(n>>1)][j+(n>>1)][2]=(byte) meanTake(Imgb,n*n);
			}
		}

		for(int k=0;k<n-1;k++){
			for(int l=0;l<n-1;l++){
				Imgr1[k*(n-1)+l] = img[k][l][0];
				Imgg1[k*(n-1)+l] = img[k][l][1];
				Imgb1[k*(n-1)+l] = img[k][l][2];

				Imgr2[k*(n-1)+l] = img[k][l+Size_w-(n-1)][0];
				Imgg2[k*(n-1)+l] = img[k][l+Size_w-(n-1)][1];
				Imgb2[k*(n-1)+l] = img[k][l+Size_w-(n-1)][2];

				Imgr3[k*(n-1)+l] = img[k+Size_h-(n-1)][l][0];
				Imgg3[k*(n-1)+l] = img[k+Size_h-(n-1)][l][1];
				Imgb3[k*(n-1)+l] = img[k+Size_h-(n-1)][l][2];

				Imgr4[k*(n-1)+l] = img[k+Size_h-(n-1)][l+Size_w-(n-1)][0];
				Imgg4[k*(n-1)+l] = img[k+Size_h-(n-1)][l+Size_w-(n-1)][1];
				Imgb4[k*(n-1)+l] = img[k+Size_h-(n-1)][l+Size_w-(n-1)][2];
			}
		}

		img[0][0][0] = (byte) meanTake(Imgr1,(n-1)*(n-1));
		img[0][0][1] = (byte) meanTake(Imgg1,(n-1)*(n-1));
		img[0][0][2] = (byte) meanTake(Imgb1,(n-1)*(n-1));

		img[0][(Size_w-1)][0] = (byte) meanTake(Imgr2,(n-1)*(n-1));
		img[0][(Size_w-1)][1] = (byte) meanTake(Imgg2,(n-1)*(n-1));
		img[0][(Size_w-1)][2] = (byte) meanTake(Imgb2,(n-1)*(n-1));

		img[Size_h-1][0][0] = (byte) meanTake(Imgr3,(n-1)*(n-1));
		img[Size_h-1][0][1] = (byte) meanTake(Imgg3,(n-1)*(n-1));
		img[Size_h-1][0][2] = (byte) meanTake(Imgb3,(n-1)*(n-1));

		img[Size_h-1][Size_w-1][0] = (byte) meanTake(Imgr4,(n-1)*(n-1));
		img[Size_h-1][Size_w-1][1] = (byte) meanTake(Imgg4,(n-1)*(n-1));
		img[Size_h-1][Size_w-1][2] = (byte) meanTake(Imgb4,(n-1)*(n-1));

		for(int i=0;i<(n>>1);i++){
			for(int j=0;j<Size_w-(n-1);j++){
				for(int k=0;k<n-1;k++){
					for(int l=0;l<n;l++){
						Imgr5[k*n+l]=img[i+k][j+l][0];
						Imgg5[k*n+l]=img[i+k][j+l][1];
						Imgb5[k*n+l]=img[i+k][j+l][2];

						Imgr6[k*n+l] = img[i+Size_h-(n-1)+k][j+l][0];
						Imgg6[k*n+l] = img[i+Size_h-(n-1)+k][j+l][1];
						Imgb6[k*n+l] = img[i+Size_h-(n-1)+k][j+l][2];
					}	
				}
				img[i][j+(n>>1)][0] = (byte) meanTake(Imgr5,n*(n-1));
				img[i][j+(n>>1)][1] = (byte) meanTake(Imgg5,n*(n-1));
				img[i][j+(n>>1)][2] = (byte) meanTake(Imgb5,n*(n-1));

				img[i+Size_h-n+(n>>1)][j+(n>>1)][0] = (byte) meanTake(Imgr6, n*(n-1));
				img[i+Size_h-n+(n>>1)][j+(n>>1)][1] = (byte) meanTake(Imgg6, n*(n-1));
				img[i+Size_h-n+(n>>1)][j+(n>>1)][2] = (byte) meanTake(Imgb6, n*(n-1));
			}
		}

		for(int i=0;i<Size_h-(n-1);i++){
			for(int j=0;j<(n>>1);j++){
				for(int k=0;k<n;k++){
					for(int l=0;l<n-1;l++){
						Imgr5[k*(n-1)+l]=img[i+k][j+l][0];
						Imgg5[k*(n-1)+l]=img[i+k][j+l][1];
						Imgb5[k*(n-1)+l]=img[i+k][j+l][2];

						Imgr6[k*(n-1)+l] = img[i+k][j+Size_w-(n-1)+l][0];
						Imgg6[k*(n-1)+l] = img[i+k][j+Size_w-(n-1)+l][1];
						Imgb6[k*(n-1)+l] = img[i+k][j+Size_w-(n-1)+l][2];
						}							
				}
				img[i+(n>>1)][j][0] = (byte)meanTake(Imgr5, n*(n-1));
				img[i+(n>>1)][j][1] = (byte)meanTake(Imgg5, n*(n-1));
				img[i+(n>>1)][j][2] = (byte)meanTake(Imgb5, n*(n-1));

				img[i+(n>>1)][j+Size_w-1][0] = (byte) meanTake(Imgr6, n*(n-1));
				img[i+(n>>1)][j+Size_w-1][1] = (byte) meanTake(Imgg6, n*(n-1));
				img[i+(n>>1)][j+Size_w-1][2] = (byte) meanTake(Imgb6, n*(n-1));
			}
		}

		return img;
	}

	//Taking average for filtering
	public int meanTake(byte[] arr,int n){
		int mean=0;
		for(int i=0;i<n;i++){
			mean=Byte.toUnsignedInt(arr[i])+ mean;
		}
		mean=Integer.divideUnsigned(mean, n);
		return mean;
	}

	//Conerting 1D  byte array to 3D
	public byte[][][] convertTo3D(byte[] bytes, int width, int height, int ind){
		byte[][][] rgb=new byte[height][width][3];
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				rgb[y][x][0] = bytes[ind];
				rgb[y][x][1] = bytes[ind+height*width];
				rgb[y][x][2] = bytes[ind+height*width*2]; 
				ind++;
			}
		}
		return rgb;
	}

	public static void main(String[] args) throws IOException{
		MMHWOne image = new MMHWOne();
		image.processImg(args);
	}

}