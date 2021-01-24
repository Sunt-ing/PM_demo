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

package com.dbg.patternmining.models.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import com.dbg.patternmining.models.dataStructures.*;
import com.dbg.patternmining.models.topKresults.MaxHeap;
import com.dbg.patternmining.models.topKresults.MaxHeapNodes;
import com.dbg.patternmining.models.topKresults.MinHeap;
import com.dbg.patternmining.models.utilities.MyPair;
import com.dbg.patternmining.models.joinAlgorithm.*;

public class Searcher<NodeType, EdgeType> 
{

	private Graph singleGraph;
	private CorePattern inPattern;
	private ArrayList<Integer> sortedFrequentLabels;
	private ArrayList<Double> freqEdgeLabels;
	private MinHeap<NodeType, EdgeType> minHeap;

	Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
	Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> patternEdges;
	ArrayList<SearchLatticeNode<NodeType, EdgeType>> coreOrginEdgeList;
	HashMap<String,Integer> HashMetaTable;
	
	public ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> result;
	public static Hashtable<Integer, Vector<Integer>> neighborLabels;
	public static Hashtable<Integer, Vector<Integer>> revNeighborLabels;
	public static Set<Integer> coreEdgeSet;//store edge labels of the input core pattern
	public static Set<Integer> coreNodeSet;//store node labels of the input core pattern
	
	public String getQueryPattern() {
		return inPattern.getCorePattern();
	}
	
	public int getQNodeCount() {
		return inPattern.getNodeNum();
	}
	
	public Searcher(String path,String paths, int shortestDistance) throws Exception
	{
		singleGraph = new Graph(1);
		singleGraph.loadFromFile_Ehab(path);
		minHeap = new MinHeap<NodeType, EdgeType>();
		inPattern = new CorePattern();
		inPattern.loadFromFile_Core(paths);
		Set<Integer> edgeSet = inPattern.getEdgeLabelSet();
		Set<Integer> nodeSet = inPattern.getNodeLabelSet();
		this.coreEdgeSet = edgeSet;
		this.coreNodeSet = nodeSet;
		sortedFrequentLabels=singleGraph.getSortedFreqLabels();
		freqEdgeLabels = singleGraph.getFreqEdgeLabels();
		
		JoinOperation join = new JoinOperation(singleGraph);

        singleGraph.setShortestPaths_1hop();
	}
	
	// expand for pattern nodes
	public void initializePlus() {
		patternEdges = new TreeMap<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>(
				new gEdgeComparator<NodeType, EdgeType>());
		MaxHeapNodes HM = singleGraph.getMaxHeapNodes();
		IntFrequency minHeapThreshold = new IntFrequency(0);

		coreOrginEdgeList = new ArrayList<SearchLatticeNode<NodeType, EdgeType>>();
		while (HM.getSize() != 0) {// while Hmax is not empty
			HashMap<Integer, HashMap<Integer, myNode>> patternNodeMap = new HashMap<Integer, HashMap<Integer, myNode>>();
			patternNodeMap.putAll(HM.popHeap(0));// pop the most frequent pattern
			// Connectivity expand
			for (Entry<Integer, HashMap<Integer, myNode>> entry : patternNodeMap.entrySet()) {
				int firstLabel = entry.getKey();

				HashMap<Integer, myNode> tmp = entry.getValue();
				for (Iterator<myNode> iterator = tmp.values().iterator(); iterator.hasNext();) {
					myNode node = iterator.next();// current node

					HashMap<Integer, ArrayList<MyPair<Integer, Double>>> neighbours = node.getReachableWithNodes();
					if (neighbours != null)
						for (Iterator<Integer> iter = neighbours.keySet().iterator(); iter.hasNext();) {
							int secondLabel = iter.next();
							int labelA = sortedFrequentLabels.indexOf(firstLabel);
							int labelB = sortedFrequentLabels.indexOf(secondLabel);

							// iterate over all neighbor nodes to get edge
							// labels as well
							for (Iterator<MyPair<Integer, Double>> iter1 = neighbours.get(secondLabel).iterator(); iter1
									.hasNext();) {
								MyPair<Integer, Double> mp = iter1.next();
								double edgeLabel = mp.getB();
								if (!freqEdgeLabels.contains(edgeLabel))
									continue;

								final GSpanEdge<NodeType, EdgeType> gedge = new GSpanEdge<NodeType, EdgeType>().set(0,
										1, labelA, (int) edgeLabel, labelB, 1, firstLabel, secondLabel);

								if (!patternEdges.containsKey(gedge)) {
									System.out.println(gedge);

									final ArrayList<GSpanEdge<NodeType, EdgeType>> parents = new ArrayList<GSpanEdge<NodeType, EdgeType>>(
											2);
									parents.add(gedge);
									parents.add(gedge);

									HPListGraph<NodeType, EdgeType> lg = new HPListGraph<NodeType, EdgeType>();
									gedge.addTo(lg);
									DFSCode<NodeType, EdgeType> code = new DFSCode<NodeType, EdgeType>(
											sortedFrequentLabels, singleGraph, null).set(lg, gedge, gedge, parents);

									patternEdges.put(gedge, code);

									// add edges that label contained in core-pattern
									if ((coreEdgeSet != null && !coreEdgeSet.isEmpty() && coreEdgeSet.contains((int) edgeLabel)) ||
											(coreNodeSet != null && !coreNodeSet.isEmpty()
											&& (coreNodeSet.contains(firstLabel) || coreNodeSet.contains(secondLabel))) ) {// no less than 1 edge
										coreOrginEdgeList.add(code);
									} 

								}
							}
						}
				}
			}
		}

	}
	
	public ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Frequency>> search()
	{
		Algorithm<NodeType, EdgeType> algo = new Algorithm<NodeType, EdgeType>();
		algo.setOneEdgePatterns(patternEdges);
		RecursiveStrategy<NodeType, EdgeType> rs = new RecursiveStrategy<NodeType, EdgeType>(sortedFrequentLabels);
		result= rs.search(algo,singleGraph,coreOrginEdgeList,inPattern,patternEdges);
		return result;
	}
	
	public Graph getSingleGraph()
	{
		return singleGraph;
	}
	
}
