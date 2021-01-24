/**
 * Copyright 2014 Mohammed Elseidy, Ehab Abdelhamid

This file is part of Grami.

Grami is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

Grami is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Grami.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dbg.patternmining.models.dataStructures;


import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

//import Temp.SubsetReference;
import com.dbg.patternmining.models.search.OneEdgeExtension;
import com.dbg.patternmining.models.topKresults.MaxHeap;
import com.dbg.patternmining.models.topKresults.MaxHeapNodes;
//import utilities.CombinationGenerator;
import com.dbg.patternmining.models.utilities.Settings;


public class Graph 
{

	public final static int NO_EDGE = 0;
	private HPListGraph<Integer, Double> m_matrix;
	private int nodeCount=0;
	private ArrayList<myNode> nodes;
	private HashMap<Integer, HashMap<Integer,myNode>> freqNodesByLabel;
	private HashMap<Integer, HashMap<Integer,myNode>> nodesByLabel;
	private ArrayList<Integer> sortedFreqLabels; //sorted by frequency !!! Descending......
	
	private HashMap<Integer, Integer> nIndexLabelMap;
	private HashMap<Integer, ArrayList<Integer>> nodeIndexMap;

	private ArrayList<Point> sortedFreqLabelsWithFreq;
	private HashMap<Double, Integer> edgeLabelsWithMNIFreq;
	private HashMap<String,HashMap<Integer,Set<Integer>>> oneEdgePatternsWithMNIFreq;
	private HashMap<String,Integer> oneEdgeWithMNIFreq;

	private HashMap<Double, Set<Integer>> edgeLabelsWithNodesMap;
	private List<Double> topKFreqEdgeLabels;
	private int kthFreq;
	private int updateMetaTableNum;
	private int updateOneEdgeMNINum;
	private HashMap<Double, Integer> edgeLabelsWithFreq;
	private ArrayList<Double> freqEdgeLabels;
	private MaxHeap maxHeap;
	private MaxHeapNodes maxHeapNodes;
	private HashMap<String,ArrayList<int[]>> HMTWithNodes;

	public int getKthFreq() {
		return kthFreq;
	}
	
	public MaxHeap getMaxHeap(){
		return maxHeap;
	}
	
	public MaxHeapNodes getMaxHeapNodes(){
		return maxHeapNodes;
	}
	
	public HashMap<Integer, Integer> getNIndexLabelMap() {
		return this.nIndexLabelMap;
	}
	
	public int[][] getMaxDegreeMatrix(){
		//this matrix stores the max degree of each node label
		int maxDegreeMatrix[][] = new int[nodeIndexMap.size()][2];
		Set<Entry<Integer, ArrayList<Integer>>> entrySet = nodeIndexMap.entrySet();
	    Iterator<Entry<Integer, ArrayList<Integer>>> iter = entrySet.iterator();
	    int index = 0;//node label index, so the data should pre-processing as node label by increasing order one by one
	    while (iter.hasNext()) {
	    	 Entry<Integer, ArrayList<Integer>> entry = iter.next();
	    	 List<Integer> nodeList= entry.getValue();
	    	 int maxInDegree = 0;
	    	 int maxOutDegree = 0;
	    	 for(int i=0;i<nodeList.size();i++) {
	    		 int inDegree = m_matrix.getInDegree(nodeList.get(i));
	    		 int outDegree = m_matrix.getOutDegree(nodeList.get(i));
	    		 if(maxInDegree <inDegree) {
	    			 maxInDegree = inDegree;//in degree stored in first column
	    			 maxDegreeMatrix[index][0] = maxInDegree;
	    		 }
	    		 if(maxOutDegree <outDegree) {
	    			 maxOutDegree = outDegree;//out degree stored in second column
	    			 maxDegreeMatrix[index][1] = maxOutDegree;
	    		 }
	    	 }
	    	 index ++;
	    }
		return maxDegreeMatrix;
	}
	
	private int m_id;
	private HashMap<Integer, Map<String, List<Integer>>> metaTableMap;
	
	public Graph(int ID) 
	{
		updateOneEdgeMNINum = 0;
		updateMetaTableNum = 0;
		sortedFreqLabels= new ArrayList<Integer>();
		sortedFreqLabelsWithFreq = new ArrayList<Point>();
		topKFreqEdgeLabels = new ArrayList<Double>();
		nIndexLabelMap = new HashMap<Integer, Integer>();
		nodeIndexMap = new HashMap<Integer, ArrayList<Integer>>();
		metaTableMap = new HashMap<Integer,Map<String, List<Integer>>>();
		m_matrix= new HPListGraph<Integer, Double>();
		m_id=ID;
		nodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		maxHeap = new MaxHeap<>();
		maxHeapNodes = new MaxHeapNodes();
		HMTWithNodes = new HashMap<String,ArrayList<int[]>>();

		freqNodesByLabel= new HashMap<Integer, HashMap<Integer,myNode>>();
		nodes= new ArrayList<myNode>();
		
		edgeLabelsWithFreq = new HashMap<Double, Integer>();
		edgeLabelsWithMNIFreq = new HashMap<Double, Integer>();
		oneEdgePatternsWithMNIFreq = new HashMap<String,HashMap<Integer,Set<Integer>>>();
		oneEdgeWithMNIFreq = new HashMap<String, Integer>();
		edgeLabelsWithNodesMap = new HashMap<Double, Set<Integer>>();

		freqEdgeLabels = new ArrayList<Double>();
		
		if(StaticData.hashedEdges!=null)
		{
			StaticData.hashedEdges = null;
//			System.out.println(StaticData.hashedEdges.hashCode());//throw exception if more than one graph was created
		}
		StaticData.hashedEdges = new HashMap<String, HashMap<Integer, Integer>[]>();
	}
	
	public ArrayList<Integer> getSortedFreqLabels() {
		return sortedFreqLabels;
	}
	
	public ArrayList<Double> getFreqEdgeLabels() {
		return this.freqEdgeLabels;
	}

	public HashMap<Integer, HashMap<Integer,myNode>> getFreqNodesByLabel()
	{
		return freqNodesByLabel;
	}
	
	public List<Double> getTopKFreqEdgeLabels() {
		return this.topKFreqEdgeLabels;
	}
	
	public HashMap<Double, Integer> getEdgeLabelsWithMNIFreq() {
		return this.edgeLabelsWithMNIFreq;
	}
	
	public <NodeType, EdgeType> void loadFromFile_Ehab(String fileName) throws Exception
	{		
		final BufferedReader rows = new BufferedReader(new FileReader(new File(fileName)));
		
		// read graph from rows
		// nodes
		int counter = 0;
		int numberOfNodes=0;
		String line;
		String tempLine;
		rows.readLine();
		
		while ((line = rows.readLine()) !=null && (line.charAt(0) == 'v')) {
			ArrayList<Integer> nodeIndexList = new ArrayList<Integer>();
			final String[] parts = line.split("\\s+");
			final int index = Integer.parseInt(parts[1]);
			final int label = Integer.parseInt(parts[2]);
			if (index != counter) {
				throw new ParseException("The node list is not sorted", counter);
			}
			nIndexLabelMap.put(index, label);
			//create index for all nodes by node label
			if(!nodeIndexMap.isEmpty() || nodeIndexMap != null) {
				if(nodeIndexMap.containsKey(label)) {
					nodeIndexMap.get(label).add(index);
				}else if(!nodeIndexMap.containsKey(label)) {
					nodeIndexList.add(index);
					nodeIndexMap.put(label, nodeIndexList);
				}
			}else {
				nodeIndexMap.put(label, nodeIndexList);
			}
			
			
			addNode(label);
			myNode n = new myNode(numberOfNodes, label);
			nodes.add(n);
			HashMap<Integer,myNode> tmp = nodesByLabel.get(label);
			if(tmp==null)
			{
				tmp = new HashMap<Integer,myNode>();
				nodesByLabel.put(label, tmp);
			}

			tmp.put(n.getID(), n);
			numberOfNodes++;
			counter++;
		}
		
		nodeCount=numberOfNodes;
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
				final int index1 = Integer.parseInt(parts[1]);
				final int index2 = Integer.parseInt(parts[2]);
				final double label = Double.parseDouble(parts[3]);
				addEdge(index1, index2, label);
				//create index for UB join evaluation
				createHMTnodesTable(index1, index2, label);
			} while((line = rows.readLine()) !=null && (line.charAt(0) == 'e'));
		}
		
		nodeIndexMap = sortByKeyIncreasing(nodeIndexMap);
		edgeLabelsWithFreq = (HashMap<Double, Integer>) sortByValueDescending(edgeLabelsWithFreq);
		//prune infrequent edge labels
		for (Iterator<  Entry< Double,Integer> >  it= this.edgeLabelsWithFreq.entrySet().iterator(); it.hasNext();)
		{
			Entry< Double,Integer > ar =  it.next();
			if(ar.getValue().doubleValue()>=1)
			{
				this.freqEdgeLabels.add(ar.getKey());
			}
		}
		
		
		List<HashMap<Integer, HashMap<Integer,myNode>>> orginList = new ArrayList<HashMap<Integer, HashMap<Integer,myNode>>>();
		//now prune the infrequent nodes
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= nodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			System.out.println("nodeLabel:"+ar.getKey()+"node MNI:"+ar.getValue().size());
			HashMap<Integer, HashMap<Integer,myNode>> midMap = new HashMap<Integer, HashMap<Integer,myNode>>();
			midMap.put(ar.getKey(), ar.getValue());
			orginList.add(midMap);
			if(ar.getValue().size()>=1)//size is not the MNI frequency, so can't replace as kthFreq
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(),ar.getValue().size()));
				freqNodesByLabel.put(ar.getKey(), ar.getValue());//influent the final time!!!!!!!!!!
			}
		}
		
		maxHeapNodes.initOriginList(orginList);
		maxHeapNodes.makeHeap(0, orginList.size());
		
		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) 
		{
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}
		
		rows.close();		
	}
	
	public void createHMTnodesTable(int nodeA, int nodeB, double edgeLabel){
		int labelA = nIndexLabelMap.get(nodeA);
		int labelB = nIndexLabelMap.get(nodeB);
		String pattern = labelA+"_"+(int)edgeLabel+"+"+labelB;//"A_50+B"
		ArrayList<int[]> LL = HMTWithNodes.get(pattern);
		if(LL == null){
			int[] a = new int[3];
			a[0] = nodeA;
			a[1] = nodeB;
			a[2] = (int)edgeLabel;
			LL = new ArrayList<int[]>();
			LL.add(a);
			HMTWithNodes.put(pattern, LL);
		}else{
			int[] a = new int[3];
			a[0] = nodeA;
			a[1] = nodeB;
			a[2] = (int)edgeLabel;
			LL.add(a);
		}
	}
	
	public HashMap<String,ArrayList<int[]>> getHMTnodesTable(){
		return HMTWithNodes;
	}
	
	public void pruneInfrequentNodesAndEdges(int kthFreq) {
		
		freqEdgeLabels = new ArrayList<Double>();
		// prune infrequent edge labels
		for (Iterator<Entry<Double, Integer>> it = this.edgeLabelsWithFreq.entrySet().iterator(); it
				.hasNext();) {
			Entry<Double, Integer> ar = it.next();
			if (ar.getValue().doubleValue() >= kthFreq) {
				this.freqEdgeLabels.add(ar.getKey());
			}
		}
		sortedFreqLabelsWithFreq = new ArrayList<Point>();
//		freqNodesByLabel = new HashMap<Integer, HashMap<Integer,myNode>>();
		// now prune the infrequent nodes
		for (Iterator<Entry<Integer, HashMap<Integer, myNode>>> it = nodesByLabel.entrySet()
				.iterator(); it.hasNext();) {
			Entry<Integer, HashMap<Integer, myNode>> ar = it.next();
			if (ar.getValue().size() >= kthFreq)// size is not the MNI
												// frequency, so can't replace
												// as kthFreq
			{
				sortedFreqLabelsWithFreq.add(new Point(ar.getKey(), ar.getValue().size()));
			}
		}

		Collections.sort(sortedFreqLabelsWithFreq, new freqComparator());
		sortedFreqLabels = new ArrayList<Integer>();
		for (int j = 0; j < sortedFreqLabelsWithFreq.size(); j++) {
			sortedFreqLabels.add(sortedFreqLabelsWithFreq.get(j).x);
		}

	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
		List<Entry<K, V>> list = new LinkedList<Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> o1, Entry<K, V> o2) {
				int compare = (o1.getValue()).compareTo(o2.getValue());
				return -compare;
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	public static HashMap<Integer, ArrayList<Integer>> sortByKeyIncreasing(HashMap<Integer, ArrayList<Integer>> nodeIndexMap) {
		//sort by increasing
		 List<Entry<Integer, ArrayList<Integer>>> list = new ArrayList<Entry<Integer, ArrayList<Integer>>>(nodeIndexMap.entrySet());
	        Collections.sort(list, new Comparator<Entry<Integer, ArrayList<Integer>>>() {
	            @Override
				public int compare(Entry<Integer, ArrayList<Integer>> o1, Entry<Integer, ArrayList<Integer>> o2) {
	                return o1.getKey().compareTo(o2.getKey());
	            }
	        });
			return nodeIndexMap;
	}
	
	/*
	 * @version:V1.1 at 20190321
	 * @param: nodeA,nodeB,edgeLabel
	 * @return: meta table of knowledge graph KG
	 */
	public void createMTtable(int nodeA, int nodeB, Double dedgeLabel) {
		//get the node label of nodeA,nodeB
		int labelA = nIndexLabelMap.get(nodeA);
		int labelB = nIndexLabelMap.get(nodeB);
		int edgeLabel = new Double(dedgeLabel).intValue();
//		System.out.println("pattern:"+labelA+"--"+edgeLabel+"--"+labelB);

		if(metaTableMap != null && !metaTableMap.isEmpty()) {//start at second line
			if(metaTableMap.containsKey(labelA)) {//outcoming:contains node label:111
				Boolean existFlag = false;
				for(Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
					if(entry.getKey().equals(labelA)) {//find node type:"111" and its associate map
						Map<String, List<Integer>> mMap = entry.getValue();
						for(Entry<String, List<Integer>> entry1 : mMap.entrySet()) {
							if(entry1.getKey().equals("Out")) {//Out=2
								//if the relation was existed in the list
								int relation = ifExistType(edgeLabel, entry1.getValue());
								if(relation != -1) {//not exist
									entry1.getValue().add(relation);
									existFlag = true;
									break;
								}else if(relation == -1) {//already exist would add degree count
								}
								existFlag = true;
							}
						}
						
					}
				}
				if (!existFlag) {// do not have this out relation:2, put into Out value
					HashMap<String, List<Integer>> outRelationMap = new HashMap<String, List<Integer>>();
					List<Integer> relationList = new ArrayList<Integer>();

					relationList.add(edgeLabel);
					outRelationMap.put("Out", relationList);
					for (Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
						if (entry.getKey().equals(labelA)) {
							entry.getValue().putAll(outRelationMap);
							break;
						}
					}
				}
				
			}else if(!metaTableMap.containsKey(labelA)) {//add labelA in the map
				HashMap<String, List<Integer>> midMap = new HashMap<String, List<Integer>>();
				List<Integer> relationList = new ArrayList<Integer>();
				HashMap<String, List<Integer>> outRelationMap = new HashMap<String, List<Integer>>();
				relationList.add(edgeLabel);
				outRelationMap.put("Out", relationList);
				midMap.putAll(outRelationMap);
				metaTableMap.put(labelA, midMap);
			}
			
			if(metaTableMap.containsKey(labelB)) {//incoming:contains the other node label:222
				Boolean existFlag = false;
				for(Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
					if(entry.getKey().equals(labelB)) {//find node type:"222" and its associate map
						Map<String, List<Integer>> mMap = entry.getValue();
						for(Entry<String, List<Integer>> entry1 : mMap.entrySet()) {
							if(entry1.getKey().equals("In")) {//In=2
								//if the relation was existed in the list
								int relation = ifExistType(edgeLabel, entry1.getValue());
								if(relation != -1) {//not exist
									entry1.getValue().add(relation);
									existFlag = true;
									break;
								}else if(relation == -1) {//already exist would add degree count
								}
								existFlag = true;
							}
						}
						
					}
				}
				if (!existFlag) {// do not have this in relation:2, put into In value
					HashMap<String, List<Integer>> outRelationMap = new HashMap<String, List<Integer>>();
					List<Integer> relationList = new ArrayList<Integer>();

					relationList.add(edgeLabel);
					outRelationMap.put("In", relationList);
					for (Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
						if (entry.getKey().equals(labelB)) {
							entry.getValue().putAll(outRelationMap);
							break;
						}
					}
				}
				
			}else if(!metaTableMap.containsKey(labelB)) {//add labelB in the map
				HashMap<String, List<Integer>> midMap = new HashMap<String, List<Integer>>();
				List<Integer> relationList = new ArrayList<Integer>();
				HashMap<String, List<Integer>> outRelationMap = new HashMap<String, List<Integer>>();
				relationList.add(edgeLabel);
				outRelationMap.put("In", relationList);
				midMap.putAll(outRelationMap);
				metaTableMap.put(labelB, midMap);
			}
			
			
			
		}else if(metaTableMap == null || metaTableMap.isEmpty()) {
			//The first step
			if(!metaTableMap.containsKey(labelA)) {
				HashMap<String, List<Integer>> midMap = new HashMap<String, List<Integer>>();
				List<Integer> relationList = new ArrayList<Integer>();
				HashMap<String, List<Integer>> outRelationMap = new HashMap<String, List<Integer>>();
				relationList.add(edgeLabel);
				outRelationMap.put("Out", relationList);
				midMap.putAll(outRelationMap);
				metaTableMap.put(labelA, midMap);
			}
			if(!metaTableMap.containsKey(labelB)) {
				HashMap<String, List<Integer>> midMap = new HashMap<String, List<Integer>>();
				List<Integer> relationList = new ArrayList<Integer>();
				HashMap<String, List<Integer>> inRelationMap = new HashMap<String, List<Integer>>();
				relationList.add(edgeLabel);
				inRelationMap.put("In", relationList);
				midMap.putAll(inRelationMap);
				metaTableMap.put(labelB, midMap);
			}
			
		}
		
	}
	
	/*
	 * if the type(r) is in the list, 
	 * No: returns type(r),
	 * Yes: degree count ++ and returns -1;
	 */
	public static int ifExistType(int typer, List<Integer> list) {
		if (list.isEmpty()) {
			return typer;
		} else {
				int count = 0;
				for (int i = 0; i < list.size(); i++) {
					if ((list.get(i) == typer)) {
						break;
					}
					count++;
				}
				if (count == list.size()) {
					return typer;
				}
			}
		return -1;
	}
	
	
	//update oneEdgePatternsWithMNIFreq when k-th frequency changes
	public void updateOneEdgeMNImap(int kthFrequency){
		
		for (Iterator<Entry<String, Integer>> it = this.oneEdgeWithMNIFreq
				.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> ar = it.next();

				int freq = ar.getValue();
				if (freq < kthFrequency) {
					updateOneEdgeMNINum ++;
					it.remove();//right????
				}else if(freq >= kthFrequency){
					
				}
		}
		OneEdgeExtension.sendOneEdgeMNIMap(oneEdgeWithMNIFreq);

	}
	
	
	//update the meta table when k-th frequency changess
	public void updateMetaTable(int kthFrequency) {
		//update node labels of metaTableMap
		for (Iterator<Entry<Integer, HashMap<Integer, myNode>>> it = nodesByLabel.entrySet()
				.iterator(); it.hasNext();) {
			Entry<Integer, HashMap<Integer, myNode>> ar = it.next();
			if (ar.getValue().size() < kthFrequency) {
				int beDeletedNodeLabel = ar.getKey();
				for(Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
					if(entry.getKey().equals(beDeletedNodeLabel)) {
						metaTableMap.remove(beDeletedNodeLabel);
						updateMetaTableNum ++;
						break;
					}
				}
				//how about nodesByLabel ?????????????
				it.remove();
			}else if(ar.getValue().size() >= kthFrequency) {
				continue;
			}
			
		}
		
		// use oneEdgePatternsWithMNIFreq
		for (Iterator<Entry<String, HashMap<Integer, Set<Integer>>>> it = this.oneEdgePatternsWithMNIFreq
				.entrySet().iterator(); it.hasNext();) {
			Entry<String, HashMap<Integer, Set<Integer>>> ar = it.next();

			for (Iterator<Entry<Integer, Set<Integer>>> its = ar.getValue().entrySet().iterator(); its.hasNext();) {
				Entry<Integer, Set<Integer>> ars = its.next();
				int freq = ars.getKey();
				if (freq < kthFrequency) {
					String pattern = ar.getKey();// pattern string format
					int nodeA = Integer.parseInt(pattern.substring(0,pattern.indexOf("_")));
					int tobeDeleteEdgeLabel = Integer.parseInt(pattern.substring(pattern.indexOf("_")+1, pattern.indexOf("+")));
					int nodeB = Integer.parseInt(pattern.substring(pattern.indexOf("+")+1,pattern.length()));
					// delete in degree column
					for (Entry<Integer, Map<String, List<Integer>>> entry : metaTableMap.entrySet()) {
						if (entry.getKey().equals(nodeA)) {// equals the node
															// type
							for (Entry<String, List<Integer>> entry0 : entry.getValue().entrySet()) {
								List<Integer> edgeLabelList = entry0.getValue();
								if (entry0.getKey().equals("Out") && edgeLabelList.contains(tobeDeleteEdgeLabel)) {
									int index = edgeLabelList.indexOf(tobeDeleteEdgeLabel);
									entry0.getValue().remove(index);// remove
																	// the index
																	// of list
									updateMetaTableNum++;
								} else {
									continue;
								}
							}
						}
						if (entry.getKey().equals(nodeB)) {
							for (Entry<String, List<Integer>> entry0 : entry.getValue().entrySet()) {
								List<Integer> edgeLabelList = entry0.getValue();
								if (entry0.getKey().equals("In") && edgeLabelList.contains(tobeDeleteEdgeLabel)) {
									int index = edgeLabelList.indexOf(tobeDeleteEdgeLabel);
									entry0.getValue().remove(index);// remove
																	// the index
																	// of list
									updateMetaTableNum++;
								} else {
									continue;
								}
							}
						}
					}
					its.remove();// ?????????
				} else if (freq >= kthFrequency) {
					continue;
				}

			}

		}
		//new oneEdgeExtension
//		OneEdgeExtension.sendMetaTableMap(metaTableMap);
		
	}
	
	public int getUpdateMetaTableNum() {
		return updateMetaTableNum;
	}
	
	public int getUpdateOneEdgeMNINum() {
		return updateOneEdgeMNINum;
	}
	
	
	public void printFreqNodes()
	{
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			System.out.println("Freq Label: "+ar.getKey()+" with size: "+ar.getValue().size());
		}
	}
	
	//1 hop distance for the shortest paths
	public void setShortestPaths_1hop()
	{
		for (Iterator<  Entry< Integer, HashMap<Integer,myNode> > >  it= freqNodesByLabel.entrySet().iterator(); it.hasNext();)
		{
			Entry< Integer, HashMap<Integer,myNode> > ar =  it.next();
			
			HashMap<Integer,myNode> freqNodes= ar.getValue();
//			int counter=0;
			for (Iterator<myNode> iterator = freqNodes.values().iterator(); iterator.hasNext();) 
			{
				myNode node =  iterator.next();
//				System.out.println(counter++);
				node.setReachableNodes_1hop(this, freqNodesByLabel);
			}
		}
	}
	
	public myNode getNode(int ID)
	{
		return nodes.get(ID);
	}
	
	public HPListGraph<Integer, Double> getListGraph()
	{
		return m_matrix;
	}
	public int getID() {
		return m_id;
	}
	
	public int getDegree(int node) {

		return m_matrix.getDegree(node);
	}
		
	public int getNumberOfNodes()
	{
		return nodeCount;
	}
	public HashMap<Integer, Map<String, List<Integer>>> getMTtable()
	{
		return metaTableMap;
	}
	
	public HashMap<String,Integer> getOneEdgeMNImap(){
		
		return oneEdgeWithMNIFreq;
	}
	 
	public int addNode(int nodeLabel) {
		return m_matrix.addNodeIndex(nodeLabel);
	}
	public int addEdge(int nodeA, int nodeB, double edgeLabel) 
	{
		Integer I = edgeLabelsWithFreq.get(edgeLabel); 
		if(I==null)
			edgeLabelsWithFreq.put(edgeLabel, 1);
		else
			edgeLabelsWithFreq.put(edgeLabel, I.intValue()+1);
		
		//add edge frequency
//		int labelA = nodes.get(nodeA).getLabel();
//		int labelB = nodes.get(nodeB).getLabel();
//		
//		String hn;
//		
//		hn = labelA+"_"+edgeLabel+"_"+labelB;
//		
//		HashMap<Integer,Integer>[] hm = StaticData.hashedEdges.get(hn); 
//		if(hm==null)
//		{
//			hm = new HashMap[2];
//			hm[0] = new HashMap();
//			hm[1] = new HashMap();
//					
//			StaticData.hashedEdges.put(hn, hm);
//		}
//		else
//		{}
//		hm[0].put(nodeA, nodeA);
//		hm[1].put(nodeB, nodeB);
		
		return m_matrix.addEdgeIndex(nodeA, nodeB, edgeLabel, 1);
	}
	
	//use HashSet to replace the list contains
	
	public void addMNIFreqEdge(int nodeA, int nodeB, double edgeLabel) {
		Integer II = edgeLabelsWithMNIFreq.get(edgeLabel);
		if(II == null) {
			Set<Integer> a = new HashSet<Integer>(); 
			a.add(nodeA);
			a.add(nodeB);
			edgeLabelsWithNodesMap.put(edgeLabel, a);
			edgeLabelsWithMNIFreq.put(edgeLabel, 1);

		}else {
			if(edgeLabelsWithNodesMap.get(edgeLabel).contains(nodeA)||edgeLabelsWithNodesMap.get(edgeLabel).contains(nodeB)) {
				return;
			}else {
				edgeLabelsWithMNIFreq.put(edgeLabel, II.intValue()+1);
				Set<Integer> b = edgeLabelsWithNodesMap.get(edgeLabel);
				b.add(nodeA);
				b.add(nodeB);
				edgeLabelsWithNodesMap.put(edgeLabel, b);
			}
		}
	}
	
	//a new MNI evaluation for 1-edge patterns
	public void addMNIOneEdgePattern(int nodeA, int nodeB, double edgeLabel){
		//nIndexLabelMap
		int labelA = nIndexLabelMap.get(nodeA);
		int labelB = nIndexLabelMap.get(nodeB);
		String pattern = labelA+"_"+(int)edgeLabel+"+"+labelB;//"A_50+B"
		HashMap<Integer,Set<Integer>> LL = oneEdgePatternsWithMNIFreq.get(pattern);
		if(LL == null){
			Set<Integer> a = new HashSet<Integer>(); 
			a.add(nodeA);
			a.add(nodeB);
			LL = new HashMap<Integer,Set<Integer>>();
			LL.put(1, a);
			oneEdgePatternsWithMNIFreq.put(pattern, LL);
		}else{//the same pattern
			for(Entry<Integer,Set<Integer>> entry : LL.entrySet()){
				if(entry.getValue().contains(nodeA)||
						entry.getValue().contains(nodeB)){
					return;
				}else{
					int freq = entry.getKey();
					entry.getValue().add(nodeA);
					entry.getValue().add(nodeB);
					oneEdgePatternsWithMNIFreq.get(pattern).remove(freq);
					oneEdgePatternsWithMNIFreq.get(pattern).put(freq+1, entry.getValue());
				}
			}
		}
	}
	
	//remove the node index of MNI map
	public void removeNodeIndexOfMNImap() {
		for (Entry<String, HashMap<Integer, Set<Integer>>> entry : oneEdgePatternsWithMNIFreq.entrySet()) {
			String pattern = entry.getKey();
			for (Entry<Integer, Set<Integer>> entry0 : entry.getValue().entrySet()) {
				int freq = entry0.getKey();
				oneEdgeWithMNIFreq.put(pattern, freq);
			}
		}
	}
	
	public Integer getKthFreqFromOneEdge(){
		List<Integer> freqList = new ArrayList<Integer>();
		for(Entry<String,HashMap<Integer,Set<Integer>>> entry:oneEdgePatternsWithMNIFreq.entrySet()){
			for(Entry<Integer,Set<Integer>> entry0:entry.getValue().entrySet()){
				freqList.add(entry0.getKey());
			}
		}
		Collections.sort(freqList);
	    Collections.reverse(freqList);
	    int k = Settings.k;
	    int kthFreq = 0;
	    if(freqList.size()>=k){//one-edge patterns is larger than k
	    		kthFreq = freqList.get(k-1);
	    }else{//one-edge patterns is smaller than k
		    return freqList.get(freqList.size()-1);
	    }
	    return kthFreq;
	}
}
