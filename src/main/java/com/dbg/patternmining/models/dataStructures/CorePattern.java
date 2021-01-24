package com.dbg.patternmining.models.dataStructures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

public class CorePattern {
	private int nodeNum;
	private int edgeNum;
	public static Set<Integer> edgeLabelSet;
	public static Set<Integer> nodeLabelSet;
	public String corePatternString;
	
	public CorePattern() {
		edgeLabelSet = new HashSet<Integer>();
		nodeLabelSet = new HashSet<Integer>();
		this.nodeNum = 0;
		this.edgeNum = 0;
		this.corePatternString = "";
	}
	
	public String getCorePattern() {
		return corePatternString;
	}
	
	public int getNodeNum() {
		return this.nodeNum;
	}
	
	public int getEdgeNum() {
		return this.edgeNum;
	}
	
	public Set<Integer> getEdgeLabelSet() {
		return edgeLabelSet;
	}
	
	public Set<Integer> getNodeLabelSet() {
		return nodeLabelSet;
	}
	
	public void loadFromFile_Core(String fileName) throws Exception
	{		
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));
		
		// read graph from rows
		// nodes
		String line;
		String tempLine;
		rows.readLine();
		
		while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			if(parts.length == 3) {//nodes with label
				final int label = Integer.parseInt(parts[2]);
				corePatternString += "v " + index + " "+ label +"\n";
				if(!nodeLabelSet.contains(label)){
					nodeLabelSet.add(label);
				}
			}else if(parts.length != 3){//nodes without label
				corePatternString += "v " + index + " "+ "-1" +"\n";
			}
			if (index != nodeNum) {
				throw new ParseException("The node list is not sorted", nodeNum);
			}
			nodeNum++;
		}
		
		tempLine = line;
		
		
		// edges
		//use the first edge line
		if(tempLine.charAt(0)=='e')
			line = tempLine;
		else
			line = rows.readLine();
		
		if(line!=null)
		{
			do
			{
				final String[] parts = line.split("\\s+");
				if(parts.length < 3) {
					continue;
				}
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				if(parts.length == 4) {
					final int edgeLabel = Integer.parseInt(parts[3]);
					if(!edgeLabelSet.contains(edgeLabel)){
						edgeLabelSet.add(edgeLabel);
					}
					corePatternString += "e "+index1+" "+index2+" "+edgeLabel+"\n";
				}else if(parts.length != 4){//nodes without label
					corePatternString += "e "+index1+" "+index2+" "+"-1"+"\n";
				}
				edgeNum ++;
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}
		
	}
}
