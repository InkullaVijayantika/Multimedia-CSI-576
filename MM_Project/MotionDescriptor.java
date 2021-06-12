import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class MotionDescriptor{
    String[] videos;
    int height = 360;
    int width = 640;
    int numberFrames = 480;
    int[][] img1 = new int[width][height];;
    int[][] img2 = new int[width][height];;

    public MotionDescriptor(){
        
    }

    private boolean isCorrectIndex(int x, int y) {
        if(x < 0 || y < 0){
            return false;
        }
        if((x + 16 > width)|| (y + 16 > height)){
            return false;
        }
        return true;
    }

    public File[] sortByNumber(File[] videoFileName){
      Arrays.sort(videoFileName, new Comparator<File>(){
              @Override
              public int compare(File o1, File o2) {
                  int n1 = extractNumber(o1.getName());
                  int n2 = extractNumber(o2.getName());
                  return n1 - n2;
              }
              private int extractNumber(String name){
                int i = 0;
                try{
                  int j = 0;
                  while (j < name.length() && !Character.isDigit(name.charAt(j))) j++;
                      int s = j;
                      int e = name.lastIndexOf('.');
                      String number = name.substring(s, e);
                      i = Integer.parseInt(number);
                }catch(Exception e) {
                      i = 0;
                }
                return i;
              }
            });
      return videoFileName;
    }

    public String calculateMeanMotionVector(int[][] previousImage, int[][] img){
        try{
            int numberXBlocks= width/16;
            int numberYBlocks = height/16;
            int numBlocks = 0;
            int mvXSum=0;
            int mvYSum = 0;

            for(int x = 0; x < numberYBlocks; ++x){
                for(int y = 0; y < numberXBlocks; ++y){
                    int[] mv = new int[2];
                    mv = bruteForce(previousImage, img, y*16, x*16);
                    mvXSum+=Math.abs(mv[0]);
                    mvYSum+=Math.abs(mv[1]);
                    numBlocks+=1;
                }
            }
            return (""+(mvXSum/numBlocks)+" "+(mvYSum/numBlocks)+" ");
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private int[] bruteForce(int[][] previousImage, int[][] img, int blockStartY, int blockStartX) {
        long minMAD = Long.MAX_VALUE;
        int[] mv =new int[2];
        mv[0]=0;
        mv[1]=0;

        for(int i=(-16); i <= 16; ++i){
            for(int j = (int)(-16); j <= 16; ++j){
                if(!isCorrectIndex(i + blockStartY, j + blockStartX)){
                    continue;
                }
                long sum = calculateMAD(previousImage, img, blockStartY, blockStartX,i , j);
                if(minMAD > sum){
                    minMAD = sum;
                    mv[0] = i;
                    mv[1] = j;
                }
            }
        }
        return mv;   
    }

    private long calculateMAD(int[][] previousImage, int[][] img,int blockStartY, int blockStartX, int i, int j){
        long sum = 0;
        for(int a=0; a < 16; ++a){
            for(int b=0; b < 16; ++b){
                sum +=  Math.abs((img[a + blockStartY][b + blockStartX]&0xff) - (previousImage[a + blockStartY + i][b + blockStartX + j]&0xff));
            }
        }
        return sum;
    }

    public String processVideo(String video){
        try{
            String motionDescriptor="";
            File folder = new File(video);
            File[] listOfFiles = folder.listFiles();
            File[] vName = sortByNumber(listOfFiles);

            for(int i=1;i<480;i++){
                int frameLength = width*height*3;
                File file = new File(video + vName[i-1].getName());
                RandomAccessFile raf = new RandomAccessFile(file,"r");
                raf.seek(0);
                long len=frameLength;
                byte[] bytes = new byte[(int)len];
                raf.read(bytes);
                int ind =0;
                for(int y=0;y<height;y++){
                    for(int x =0; x<width;x++){
                        byte r = bytes[ind];
                        byte g = bytes[ind+height*width];
                        byte b = bytes[ind+height*width*2];
                        byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
                        img1[x][y]=Byte.toUnsignedInt(Y);
                        ind++;
                    }
                }

                File file1 = new File(video + vName[i].getName());
                RandomAccessFile raf1 = new RandomAccessFile(file1,"r");
                raf1.seek(0);
                byte[] bytes1 = new byte[(int)len];
                raf1.read(bytes1);
                ind = 0;
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        byte r = bytes1[ind];
                        byte g = bytes1[ind+height*width];
                        byte b = bytes1[ind+height*width*2];
                        byte Y = (byte)(0.299 * Byte.toUnsignedInt(r) + 0.587 * Byte.toUnsignedInt(g) + 0.114 * Byte.toUnsignedInt(b));
                        img2[x][y]=Byte.toUnsignedInt(Y);
                        ind++;
                    }
                }

                motionDescriptor+=calculateMeanMotionVector(img1, img2);
            }
            return motionDescriptor;
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

         
}