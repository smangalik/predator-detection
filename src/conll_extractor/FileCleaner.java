package conll_extractor;

import java.io.File;
import javax.swing.JFileChooser;
import java.io.FileNotFoundException;

import java.util.ArrayList;

import java.util.Scanner;

public class FileCleaner {

	public static ArrayList<String> cleanFile() throws FileNotFoundException{

		File dirtyFile = null;
		ArrayList<String> dirtyPosts = new ArrayList<String>();
		ArrayList<String> cleanPosts = new ArrayList<String>();
		

		//Select your dirty file
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("/home/sid/Documents/CSE487/GeneralData"));
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			dirtyFile = fileChooser.getSelectedFile();
		}

		//fill dirty ArrayList with lines
		Scanner scan = new Scanner(dirtyFile);
		while (scan.hasNext()){
			dirtyPosts.add(scan.nextLine());
		}
		scan.close();
		
		//clean up with regex 
		for (int i = 0; i<dirtyPosts.size(); i++){
			String dirtyPost = dirtyPosts.get(i);
			if(dirtyPost.matches("<BODY>.+</BODY>") 
					&& !dirtyPost.matches("<BODY>.*disconnected</BODY>")
					&& !dirtyPost.matches(".*(emoti).*")){
				dirtyPost = dirtyPost.replace("<BODY>", "");
				dirtyPost = dirtyPost.replace("</BODY>", "");
				cleanPosts.add(dirtyPost);
			}
		}
		
		return cleanPosts;
		
	}
}