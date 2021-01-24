package com.dbg.patternmining.models.mainMine;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dbg.patternmining.models.dataStructures.Frequency;
import com.dbg.patternmining.models.dataStructures.HPListGraph;
import com.dbg.patternmining.models.joinAlgorithm.InstanceTable;
import com.dbg.patternmining.models.joinAlgorithm.JoinOperation;
import com.dbg.patternmining.models.search.SearchLatticeNode;
import com.dbg.patternmining.models.search.Searcher;
import com.dbg.patternmining.models.utilities.CommandLineParser;
import com.dbg.patternmining.models.utilities.Settings;
import com.dbg.patternmining.models.utilities.StopWatch;


/*
 * 1). our solution: with HMT, no UB(join), only minheap
 * 2). core-based solution, core: 
 *     edge constraint: (1 node & 0 edge, 2 nodes & 1 edge, 3 nodes & 2 edges, 3 nodes & 3 edges)
 */

public class PatKGmine {
	
	public int test(String[] args){
		
		//parse the command line arguments
//		CommandLineParser.parse(args);
		
		Searcher<String, String> sr=null;
		StopWatch watch = new StopWatch();	
		watch.start();
		try
		{
			if(Settings.fileName==null)
			{
				System.out.println("You have to specify a filename");
				System.exit(1);
			}
			else
			{
				sr = new Searcher<String, String>(Settings.datasetsFolder+Settings.fileName,
						Settings.datasetsFolder+Settings.coreFileName, 1);
			}
			String queryPattern = sr.getQueryPattern();
			int queryNodeCount = sr.getQNodeCount();
			sr.initializePlus();
			ArrayList<HashMap<SearchLatticeNode<String, String>, Frequency>> result = sr.search();
			
			watch.stop();
			System.out.println("all_elapsedTime:"+watch.getElapsedTime()/1000.0);
			//write the top-k results in txt file
			writeTxtFiles(result, watch, queryPattern, queryNodeCount);
			return result.size();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
		
	}

	public static void writeTxtFiles(ArrayList<HashMap<SearchLatticeNode<String, String>, Frequency>> result,
			StopWatch watch, String queryPattern, int queryNodeCount) {
		FileWriter fw;
		String fName = Settings.fileName.substring(0,Settings.fileName.length()-3)+"_Q"+queryNodeCount+"_Top_"+Settings.k+".txt";
		try {
			fw = new FileWriter(fName);
			System.out.println("write file to " + fName);
			Date day = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			JoinOperation j = new JoinOperation();
			int insSize = j.getInsSize();
			fw.write("Date:" + df.format(day) + "\n");
			fw.write("all_elapsedTime:"+watch.getElapsedTime()/1000.0 + "\n");
			fw.write("insSize:"+insSize+ "\n");
			fw.write("--------------------- Query Pattern --------------------------"+ "\n");
			fw.write(queryPattern);
			fw.write("--------------------- results --------------------------"+ "\n");
			fw.write("Top-"+Settings.k+" List:\n"+result+ "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
