import java.awt.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
// import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.Timer;
import java.util.Arrays;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Color;
import java.lang.Thread;
import java.util.Comparator;
import java.util.Scanner;

// User Interface
public class V_UI extends Frame implements ActionListener{
	private ArrayList<BufferedImage> images;
	private ArrayList<BufferedImage> dbImages;
    private PlaySound playSound;
    private PlaySound playDBSound;
    private int frameRate = 24;
    private JLabel imageLabel;
    private JLabel resultImageLabel;
    private JPanel panel;
    private JLabel errorLabel;
    private JLabel matchLabel;
    private JLabel frameLabel;
    private TextField queryField;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button loadQueryButton;
    private Button loadResultButton;
    private Button searchButton;
    private List resultListDisplay;
    private JList list_list;
    private String fileName;
    private int playStatus = 3;//1 for play, 2 for pause, 3 for stop
    private int resultPlayStatus = 3;
    private Thread playingThread;
    private Thread audioThread;
    private int currentFrameNum = 0;
    private String fileFolder = "Data_rgb";
    private String audioFolder = "Data_wav";
    static final int WIDTH = 640;
    static final int HEIGHT = 360;
    private RunDescriptors searchClass;
    private String[] names = new String[29];
    private int[] sortedIndices = new int[29];
    private double [][]sumDiff = new double[29][480];
    private DrawGraph drawg;

    public V_UI(ArrayList<BufferedImage> imgs){
    	this.images = imgs;
    	names[0] = "Data_rgb/ads/ads_0";
		names[1] = "Data_rgb/ads/ads_1";
		names[2] = "Data_rgb/ads/ads_2";
		names[3] = "Data_rgb/ads/ads_3";
		names[4] = "Data_rgb/cartoon/cartoon_0";
		names[5] = "Data_rgb/cartoon/cartoon_1";
		names[6] = "Data_rgb/cartoon/cartoon_2";
		names[7] = "Data_rgb/cartoon/cartoon_3";
		names[8] = "Data_rgb/cartoon/cartoon_4";
		names[9] = "Data_rgb/concerts/concerts_0";
		names[10] = "Data_rgb/concerts/concerts_1";
		names[11] = "Data_rgb/concerts/concerts_2";
		names[12] = "Data_rgb/concerts/concerts_3";
		names[13] = "Data_rgb/interview/interview_0";
		names[14] = "Data_rgb/interview/interview_1";
		names[15] = "Data_rgb/interview/interview_2";
		names[16] = "Data_rgb/interview/interview_3";
		names[17] = "Data_rgb/interview/interview_4";
		names[18] = "Data_rgb/interview/interview_5";
		names[19] = "Data_rgb/movies/movies_0";
		names[20] = "Data_rgb/movies/movies_1";
		names[21] = "Data_rgb/movies/movies_2";
		names[22] = "Data_rgb/movies/movies_3";
		names[23] = "Data_rgb/movies/movies_4";
		names[24] = "Data_rgb/sport/sport_0";
		names[25] = "Data_rgb/sport/sport_1";
		names[26] = "Data_rgb/sport/sport_2";

    	//Query Panel
	    Panel queryPanel = new Panel();
	    queryField = new TextField(13);
	    queryField.addActionListener(this);
	    JLabel queryLabel = new JLabel("Enter the Query Video name: ");
	    queryPanel.add(queryLabel);
	    queryPanel.add(queryField);
	    errorLabel = new JLabel("");
	    frameLabel = new JLabel("");
	    errorLabel.setForeground(Color.RED);
	    frameLabel.setForeground(Color.BLUE);
	    panel=new JPanel();
	    searchButton = new Button("Find best match!!");
	    searchButton.setFont(new Font("Arial", Font.BOLD, 20));
	    searchButton.addActionListener(this);
	    queryPanel.add(searchButton);
	    Panel searchPanel = new Panel();
	    Panel controlQueryPanel = new Panel();
	    controlQueryPanel.setLayout(new GridLayout(2, 0));
	    controlQueryPanel.add(queryPanel);
	    controlQueryPanel.add(searchPanel);
	    add(controlQueryPanel, BorderLayout.WEST);

	    //Result Panel
	 	Panel resultPanel = new Panel();
		matchLabel = new JLabel("");
  		matchLabel.setText("Matched Videos:    ");
		resultPanel.add(matchLabel, BorderLayout.SOUTH);
		resultListDisplay = new List(7);
		resultListDisplay.setBounds(669, 30, 1035, 1540);
		resultPanel.add(resultListDisplay, BorderLayout.SOUTH);
	 	resultPanel.setPreferredSize(new Dimension(450,400));
	 	loadResultButton = new Button("Load Selected Video");
	 	add(resultPanel, BorderLayout.EAST);

	    //Video List Panel
	    Panel listPanel = new Panel();
	    listPanel.setLayout(new GridLayout(2, 0));
	  	resultListDisplay.addActionListener(this);
 		resultListDisplay.setPreferredSize(new Dimension(540, 480));
 		this.imageLabel = new JLabel(new ImageIcon(images.get(currentFrameNum)));
	    this.resultImageLabel = new JLabel(new ImageIcon(images.get(currentFrameNum)));
	    Panel imagePanel = new Panel();
	    imagePanel.add(this.imageLabel);
	    Panel resultImagePanel = new Panel();
	    resultImagePanel.add(this.resultImageLabel);
	    listPanel.add(imagePanel);
	    listPanel.add(resultImagePanel);

	    //Control Panel
	    Panel controlPanel = new Panel();

	    playButton = new Button("PLAY");
	    playButton.addActionListener(this);
	    controlPanel.add(playButton);

	    pauseButton = new Button("PAUSE");
	    pauseButton.addActionListener(this);
	    controlPanel.add(pauseButton);

	    stopButton = new Button("STOP");
	    stopButton.addActionListener(this);
	    controlPanel.add(stopButton);

	    controlPanel.add(errorLabel);
	    controlPanel.add(frameLabel);
	    listPanel.add(controlPanel);
	    add(listPanel, BorderLayout.SOUTH);
	    setVisible(false);
    }

    public void showUI() {
	    pack();
	    setVisible(true);
	}

    private void playVideo(){
    	playingThread = new Thread(){
    		public void run(){
    			System.out.println("Start playing video ");
    			for (int i = currentFrameNum; i < 480; i++){
    				imageLabel.setIcon(new ImageIcon(images.get(i)));
    				resultImageLabel.setIcon(new ImageIcon(dbImages.get(i)));
    				try{
    					sleep(1000/frameRate);
    					String msg="Frame number:"+(i+1);
    					frameLabel.setText(msg);
    				}catch (InterruptedException e){
    					if(playStatus == 3){
    						currentFrameNum = 0;
    					}else{
    						currentFrameNum = i;
    					}
    					imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
    					resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentFrameNum)));
	                  	currentThread().interrupt();
	                  	break;
    				}
    			}
    			if(playStatus < 2){
    				playStatus = 3;
		            currentFrameNum = 0;
		            playSound.stop();
		            playDBSound.stop();
    			}
    			System.out.println("End playing video ");

	            playButton.setEnabled(true);
	            pauseButton.setEnabled(true);
	            stopButton.setEnabled(true);
    		}
    	};

    	audioThread = new Thread() {
            public void run() {
                try {
        	        playSound.play();
        	        playDBSound.play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        errorLabel.setText(e.getMessage());
        	        return;
        	    }
        	    
	        }
	    };

	    audioThread.start();
	    playingThread.start();
	    
    }

    private void pauseVideo() throws InterruptedException{
    	if(playingThread != null){
    		playingThread.interrupt();
			audioThread.interrupt();
			playSound.pause();
			playDBSound.pause();
			playingThread = null;
			audioThread = null;
    	}
    }

    private void stopVideo(){
    	if(playingThread != null){
    		playingThread.interrupt();
			audioThread.interrupt();
			playSound.stop();
			playDBSound.stop();
			playingThread = null;
			audioThread = null;
    	}else{
    		currentFrameNum = 0;
			displayScreenShot();
			displayDBScreenShot();
    	}
    }

// The frame that appears on the display panel for query
    private void displayScreenShot(){
    	Thread initThread = new Thread(){
    		public void run(){
    			imageLabel.setIcon(new ImageIcon(images.get(currentFrameNum)));
    		}
    	};
    	initThread.start();
    }

// The frame that appears on the display panel for DB
    private void displayDBScreenShot(){
    	Thread initThread = new Thread(){
    		public void run(){
    			resultImageLabel.setIcon(new ImageIcon(dbImages.get(currentFrameNum)));
    		}
    	};
    	initThread.start();
    }

// To load the DB video selected from list
    private void loadDBVideo(String dbVideoName){
    	System.out.println("Begin loading db video.");
    	try{
    		if(dbVideoName == null || dbVideoName.isEmpty()){
    			return;
    		}
    		dbImages = new ArrayList<BufferedImage>();
    		File file1 = new File(dbVideoName);
    		File[] videoFileName = file1.listFiles();
    		File[] vName = sortByNumber(videoFileName);

    		for(int i=0; i<480; i++){
    			String audioFileName = audioFolder + dbVideoName.substring(dbVideoName.indexOf("/"), dbVideoName.length()) + ".wav";
    			File file = new File(dbVideoName + "/" + vName[i].getName());
    			long len = file.length();
    			RandomAccessFile raf = new RandomAccessFile(file, "r");
    			raf.seek(0);
    			byte[] bytes = new byte[(int) len];
    			raf.read(bytes);
    			BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    			int ind = 0;
    			for (int y = 0; y < HEIGHT; y++){
    				for (int x = 0; x < WIDTH; x++){
	    				byte r = bytes[ind];
	   					byte g = bytes[ind+HEIGHT*WIDTH];
	   					byte b = bytes[ind+HEIGHT*WIDTH*2]; 
	   					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    				img.setRGB(x,y,pix);
	    				ind++;
    				}
    			}
    			dbImages.add(img);
    			playDBSound = new PlaySound(audioFileName);
    		}
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}
    	catch(IOException e) {
    		e.printStackTrace();
	      	errorLabel.setText(e.getMessage());
    	}

    	this.playStatus = 3;
	    currentFrameNum = 0;
	    displayDBScreenShot();
	    System.out.println("End loading db video contents.");
    }

// Sorting frames of a video according to frame number
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

// To load the query video 
    private void loadVideo(String userInput){
    	System.out.println("Load query video.");
    	try{
    		if(userInput == null || userInput.isEmpty()){
    			return;
    		}
    		images = new ArrayList<BufferedImage>();
    		File file1 = new File(fileFolder + "/query/" + userInput.substring(0, userInput.indexOf(".")));
    		File[] videoFileName = file1.listFiles();
    		File[] vName = sortByNumber(videoFileName);

    		for(int i=0; i<480; i++){
    			String audioFileName = audioFolder + "/query/" + userInput.substring(0, userInput.indexOf(".")) + ".wav";
    			File file = new File(fileFolder + "/query/" + userInput.substring(0, userInput.indexOf(".")) + "/" + vName[i].getName());
    			long len = file.length();
    			RandomAccessFile raf = new RandomAccessFile(file, "r");
    			raf.seek(0);
    			byte[] bytes = new byte[(int) len];
    			raf.read(bytes);
    			BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    			int ind = 0;
    			for (int y = 0; y < HEIGHT; y++){
    				for (int x = 0; x < WIDTH; x++){
	    				byte r = bytes[ind];
	   					byte g = bytes[ind+HEIGHT*WIDTH];
	   					byte b = bytes[ind+HEIGHT*WIDTH*2]; 
	   					int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
	    				img.setRGB(x,y,pix);
	    				ind++;
    				}
    			}
	    		images.add(img);
	    		playSound = new PlaySound(audioFileName);
    		}
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}
    	catch(IOException e) {
    		e.printStackTrace();
	      	errorLabel.setText(e.getMessage());
    	}

    	this.playStatus = 3;
	    currentFrameNum = 0;
	    displayScreenShot();
	    System.out.println("Close loading query video.");

    }

    @Override
    public void actionPerformed(ActionEvent e){
    	// Query field
    	if(e.getSource() == this.queryField){
    		String userInput = queryField.getText();
    		if(userInput != null && !userInput.isEmpty()){
    			this.playingThread = null;
    			this.audioThread = null;
    			this.loadVideo(userInput.trim());
    			playButton.setEnabled(true);
    			pauseButton.setEnabled(true);
                stopButton.setEnabled(true);
    		}
    	}
    	// play button
    	else if(e.getSource() == this.playButton){
    		System.out.println("play_query clicked");
    		if(this.playStatus > 1){
    			this.playStatus = 1;
    			this.playVideo();
    			playButton.setEnabled(false);
    			pauseButton.setEnabled(true);
	         	stopButton.setEnabled(true);
    		}
    	}
    	// pause button
    	else if(e.getSource() == this.pauseButton){
    		System.out.println("pause_query clicked");
    		if(this.playStatus == 1){
    			this.playStatus = 2;
    			try{
    				pauseButton.setEnabled(false);
					playButton.setEnabled(true);
				    stopButton.setEnabled(true);
				    this.pauseVideo();
    			}catch (InterruptedException e1){
    				e1.printStackTrace();
    			}
    		}
    	}
    	// stop button
    	else if(e.getSource() == this.stopButton){
    		System.out.println("stop_query clicked");
    		if(this.playStatus < 3){
    			this.playStatus = 3;
    			stopButton.setEnabled(false);
				pauseButton.setEnabled(true);
				playButton.setEnabled(true);
				this.stopVideo();
    		}
    	}
    	// search button
    	else if(e.getSource() == this.searchButton){
    		System.out.println("search_query clicked");
    		String userInput = queryField.getText();
    		if(userInput.trim().isEmpty()) {
				return;
			}
			resultListDisplay.removeAll();
			searchClass = new RunDescriptors(userInput);
			sortedIndices = searchClass.resVideo();
			int k = 0;
			for(int i = 0; i<29; i++){
				resultListDisplay.add(names[sortedIndices[28 - i]]);	
			}
			sumDiff = searchClass.getSumDiff();
			
    	}
    	else if(e.getSource() == this.resultListDisplay){
    		int userSelect = resultListDisplay.getSelectedIndex() ;
    		if(userSelect >= 0){
				this.loadDBVideo(names[sortedIndices[28 - userSelect]]);
				frameLabel.removeAll();
				frameLabel.revalidate();
				frameLabel.repaint();
    		}
    		ArrayList<Double> dList = new ArrayList<Double>();
			for (int i = 0; i<sumDiff[sortedIndices[userSelect]].length;i++){
    			dList.add(sumDiff[sortedIndices[28 - userSelect]][i]);
			}

			// For visual descriptor
			drawg = new DrawGraph(dList);
			drawg.createAndShowGui(sumDiff[sortedIndices[28 - userSelect]]);
    	}
    }

}

