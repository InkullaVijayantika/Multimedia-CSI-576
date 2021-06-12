import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.lang.Math;
import javax.swing.*;
import java.util.HashMap;

public class ColorDescriptor{
    int height = 360;
    int width = 640;
    int numberFrames = 480;

    public ColorDescriptor(){
        
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

    public String getHSV(int r, int g, int b){
        float[] hsv = new float[3];
        Color.RGBtoHSB(r,g,b,hsv);
        int h,s,v;
        h = (int)(Math.abs(hsv[0]*15));
        s = (int)(Math.abs(hsv[1]*3));
        v = (int)(Math.abs(hsv[2]*3));
        if (h < 10){
            return "0" + h + "" + s + "" + v;
        }
        return "" + h + "" + s + "" + v;
    }

    public String processVideo(String video){

        try{
            String colorDescriptor="";
            File folder = new File(video);
            File[] listOfFiles = folder.listFiles();
            File[] vName = sortByNumber(listOfFiles);

            for(int i=0;i<480;i++){
                HashMap<String, Integer> hist = new HashMap<String, Integer>();
                BufferedImage img = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);
                int frameLength = width*height*3;
                File file = new File(video + vName[i].getName());
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(0);
                long len = frameLength;
                byte[] bytes = new byte[(int) len];
                raf.read(bytes);
                int ind = 0;
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int r = (int)(bytes[ind]);
                        int g = (int)(bytes[ind+height*width]);
                        int b = (int)(bytes[ind+height*width*2]);

                        String hsv = getHSV(r,g,b);
                        if (hist.get(hsv) == null)
                            hist.put(hsv, 1);
                        else
                            hist.put(hsv, hist.get(hsv) + 1);
                        ind++;
                    }
                }
                String maxHSV = "";
                for (String s : hist.keySet())
                {
                    if (maxHSV.equals(""))
                            maxHSV = s;
                    else
                    {
                        if (hist.get(s) > hist.get(maxHSV))
                            maxHSV = s;
                    }
                }
                colorDescriptor+=("" + maxHSV + " ");
                raf.close();
            }
            return colorDescriptor;
        }
        catch(Exception e){
            e.printStackTrace();
            return "";
        }
    }

    
}