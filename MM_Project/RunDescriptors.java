import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.*; 
import java.lang.*;
import java.util.List;

// Run descriptor on query and compare with DB descriptors. 
// Come up with a ranking
public class RunDescriptors{
	String videoFileName;
	String audioFileName;
	String mDesc;
	String cDesc;
	String aDesc;
	int resInd;
	String[] queryColorDescriptor = new String[480];
	int[][] queryMotionDescriptor = new int[480][2];
	int[] queryAudioDescriptor = new int[480]; 
	String[][] colorIndex = new String[29][480];
	int[][] audioIndex = new int[29][480];
	int[][][] motionIndex = new int[29][480][2];
	int minColorDiff = Integer.MAX_VALUE;
	int maxColorDiff = Integer.MIN_VALUE;
	double minMotionDiff = Integer.MAX_VALUE;
	double maxMotionDiff = Integer.MIN_VALUE;
	int minAudioDiff = Integer.MAX_VALUE;
	int maxAudioDiff = Integer.MIN_VALUE;
	double [][]sumDiff = new double[29][480];
	int[] sortedIndices = new int[29];
	double[] completeVideoDiff = new double[29];
	double colorWeight;
	double audioWeight;
	double motionWeight;
	double[][] colorFrameDiff = new double[29][480];
	double[][] motionFrameDiff = new double[29][480];
	double[][] audioFrameDiff = new double[29][480];
	
	public RunDescriptors(String fileName){
		System.out.println("Entered RunDescriptors");
		videoFileName = "Data_rgb" + "/query" +  "/" + fileName.substring(0, fileName.indexOf(".")) + "/" ;
		audioFileName = "Data_wav" + "/query" +  "/" + fileName.substring(0, fileName.indexOf(".")) + ".wav" ;
		
		System.out.println("MotionDescriptor running");
		MotionDescriptor md = new MotionDescriptor();
		StringTokenizer mDesc = new StringTokenizer(md.processVideo(videoFileName));

		System.out.println("ColorDescriptor running");
		ColorDescriptor cd = new ColorDescriptor();
		StringTokenizer cDesc = new StringTokenizer(cd.processVideo(videoFileName));
		
		String fName = "Data_rgb" + "/query" +  "/" + fileName.substring(0, fileName.indexOf("."));
		File file  = new File(fName);
		long len = file.listFiles().length;

		System.out.println("AudioDescriptor running");
		AudioDescriptor ad = new AudioDescriptor();
		StringTokenizer aDesc = new StringTokenizer(ad.processAudio(audioFileName,(int)len));
		System.out.println(aDesc);

// Creating descriptor for query
		for(int i = 0; i < 480; i++){
            queryColorDescriptor[i] = cDesc.nextToken().trim();
            queryAudioDescriptor[i]= Integer.parseInt(aDesc.nextToken());
            if(i==0)
            {
                queryMotionDescriptor[i][0]= 0;
                queryMotionDescriptor[i][1]= 0;
                continue;
            }
            queryMotionDescriptor[i][0] = Integer.parseInt(mDesc.nextToken());
            queryMotionDescriptor[i][1] = Integer.parseInt(mDesc.nextToken());
            
        } 
	}

	// Return ranking (sorted indices)
	public int[] resVideo(){
		try{
			FileInputStream cfstream = new FileInputStream("colorindex.txt");
	        DataInputStream cin = new DataInputStream(cfstream);
	        BufferedReader cbr = new BufferedReader(new InputStreamReader(cin));
	        FileInputStream mfstream = new FileInputStream("motionindex.txt");
	        DataInputStream min = new DataInputStream(mfstream);
	        BufferedReader mbr = new BufferedReader(new InputStreamReader(min));
			FileInputStream afstream = new FileInputStream("audioindex.txt");
			DataInputStream ain = new DataInputStream(afstream);
			BufferedReader abr = new BufferedReader(new InputStreamReader(ain));
			String cline, aline, mline;

			int count = 0;
			while ((cline = cbr.readLine()) != null && (aline = abr.readLine()) != null &&  (mline = mbr.readLine()) != null && count < 29){ 
	            StringTokenizer cst = new StringTokenizer(cline);
	            StringTokenizer ast = new StringTokenizer(aline);
	            StringTokenizer mst = new StringTokenizer(mline);
	            int videoIndex = Integer.parseInt(cst.nextToken());
	            ast.nextToken();
	            mst.nextToken();
	            for(int i = 0; i < 480; i++){
	                colorIndex[videoIndex][i] = cst.nextToken().trim();
	                audioIndex[videoIndex][i] = Integer.parseInt(ast.nextToken());
	                if(i==0){
                        motionIndex[videoIndex][i][0]= 0;
                        motionIndex[videoIndex][i][1]= 0;
                        continue;
                    }
	                motionIndex[videoIndex][i][0]= Integer.parseInt(mst.nextToken());
	                motionIndex[videoIndex][i][1]= Integer.parseInt(mst.nextToken());
	            }    
	            count++;
	        }
	        cin.close();
	        ain.close();
	        min.close();

		}catch(Exception e){
			e.printStackTrace();
		}

// Calculate difference of query with each DB video in color, motion, audio
		for(int i=0; i<29; i++){
            calculateHSVDifference(i);
            calculateAudioDifference(i);
            calculateMotionDifference(i);
        }

        for(int i=0; i<29; i++){
            completeVideoDiff[i]=0;
            for(int j=0;j<480;j++){
                colorFrameDiff[i][j]=(colorFrameDiff[i][j]-minColorDiff)/(maxColorDiff-minColorDiff);
                motionFrameDiff[i][j]=(motionFrameDiff[i][j]-minMotionDiff)/(maxMotionDiff-minMotionDiff);
                audioFrameDiff[i][j]=(audioFrameDiff[i][j]-minAudioDiff)/(maxAudioDiff-minAudioDiff);
                //combined difference
                // sumDiff[i][j] = ((colorWeight*colorFrameDiff[i][j])+(audioWeight*audioFrameDiff[i][j])+(motionWeight*motionFrameDiff[i][j]))/3;
                // completeVideoDiff[i]+=sumDiff[i][j];
            }
            colorWeight = ((480 - Arrays.stream(colorFrameDiff[i]).sum())/480);
            motionWeight = ((480 - Arrays.stream(motionFrameDiff[i]).sum())/480);
            audioWeight = ((480 - Arrays.stream(audioFrameDiff[i]).sum())/480);
            
            for(int j=0;j<480;j++){
            	sumDiff[i][j] = ((colorWeight*(1-colorFrameDiff[i][j]))+(audioWeight*(1-audioFrameDiff[i][j]))+(motionWeight*(1-motionFrameDiff[i][j])))/(colorWeight + motionWeight + audioWeight);
            	completeVideoDiff[i]+=sumDiff[i][j];
            }

        }

// Sorting for ranks
        HashMap<Integer, Double> hm = new HashMap<Integer, Double>();
        for (int i = 0; i < 29; i++){
        	hm.put(i, completeVideoDiff[i]);
        }
        Map<Integer, Double> hm1 = sortByValue(hm);
        int j = 0;
        for (Map.Entry<Integer, Double> en : hm1.entrySet()) { 
        	sortedIndices[j] = en.getKey();
        	j++; 
        } 
        return sortedIndices;
	}

	public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm){
		List<Map.Entry<Integer, Double> > list = new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() { 
            public int compare(Map.Entry<Integer, Double> o1,  
                               Map.Entry<Integer, Double> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        });
        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        }
        return temp; 
	}

	public double[][] getSumDiff(){
        return sumDiff;
    }

	public void calculateHSVDifference(int videoIndex){
		for(int i=0; i< 480; i++){
            String hsv1 = queryColorDescriptor[i];
            String hsv2= colorIndex[videoIndex][i];
            int h1 = Integer.parseInt(hsv1.substring(0, 2));
            int s1 = Integer.parseInt(hsv1.substring(2, 3));
            int v1 = Integer.parseInt(hsv1.substring(3));
            int h2 = Integer.parseInt(hsv2.substring(0, 2));
            int s2 = Integer.parseInt(hsv2.substring(2, 3));
            int v2 = Integer.parseInt(hsv2.substring(3));
            int h = Math.abs(h1 - h2);
            if (h > 8){
                int diff = h - 8;
                h = h - (2 * diff);
            }
            int x = (h * 2) + Math.abs(s1 - s2) + Math.abs(v1 - v2);
            if(x<minColorDiff)
                minColorDiff=x;
            if(x>maxColorDiff)
                maxColorDiff=x;
            colorFrameDiff[videoIndex][i]=x;
        }
	}

	public void calculateAudioDifference(int videoIndex){
        for(int i=0; i< 480; i++){
            int x = Math.abs(audioIndex[videoIndex][i]-queryAudioDescriptor[i]);
            if(x<minAudioDiff)
                minAudioDiff=x;
            if(x>maxAudioDiff)
                maxAudioDiff=x;
            audioFrameDiff[videoIndex][i]=x;
        }
	}

	public void calculateMotionDifference(int videoIndex){
        for(int i=0; i< 480; i++){
        	double x = Math.pow(Math.pow((motionIndex[videoIndex][i][0]-queryMotionDescriptor[i][0]),2) + Math.pow((motionIndex[videoIndex][i][1]-queryMotionDescriptor[i][1]),2),0.5);
             if(x<minMotionDiff)
                minMotionDiff=x;
            if(x>maxMotionDiff)
                maxMotionDiff=x;
            motionFrameDiff[videoIndex][i]=x;
        }
    }	
}