package com.dbg.patternmining.models.dataStructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dbg.patternmining.models.CSP.Variable;

public class UBHyperGraph {
	private Variable[] variables;
	public Variable[] getVariables() {
		return variables;
	}
	/*
	 * upper bound evaluation function
	 */
	public <EdgeType> HashMap<Integer, HashMap<Integer, myNode>> getSupperNodeList(Graph graph, Query qry, int kthFreq)
	{
		HashMap<Integer, HashMap<Integer,myNode>> supperNodeMap= new HashMap<Integer, HashMap<Integer,myNode>>();// QueryID -> NodeID->NODE
		ArrayList<ConnectedComponent> cls= qry.getConnectedLabels();
			
		HPListGraph<Integer, Double> qryGraph = qry.getListGraph();
		//refine according to nodeLabels
		for (int i = 0; i < qryGraph.getNodeCount(); i++) 
		{
			int label= qryGraph.getNodeLabel(i);//nodeLabel
			supperNodeMap.put(i, (HashMap<Integer,myNode>)graph.getFreqNodesByLabel().get(label).clone());
		}
		
		int min;
		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		//refine according to degree !!
		HashMap<Integer, HashMap<Integer, Integer>> nodeOutLabelDegrees= new HashMap<Integer, HashMap<Integer,Integer>>();//nodeID-->(reach node Label,Degree)
		HashMap<Integer, HashMap<Integer, Integer>> nodeInLabelDegrees= new HashMap<Integer, HashMap<Integer,Integer>>();
		
		for (int i = 0; i < cls.size(); i++) 
		{
			ConnectedComponent c = cls.get(i);
			int nodeA=c.getIndexA();
			int nodeB=c.getIndexB();
			HashMap<Integer, Integer> nodeAmap=nodeOutLabelDegrees.get(nodeA);
			HashMap<Integer, Integer> nodeBmap=nodeInLabelDegrees.get(nodeB);
			if(nodeAmap==null)
			{
				nodeAmap= new HashMap<Integer, Integer>();
				nodeOutLabelDegrees.put(nodeA, nodeAmap);
			}
			if(nodeBmap==null)
			{
				nodeBmap= new HashMap<Integer, Integer>();
				nodeInLabelDegrees.put(nodeB, nodeBmap);
			}
			
			Integer degreeA=nodeAmap.get(c.getLabelB());
			if(degreeA==null)
				degreeA=0;
			Integer degreeB=nodeBmap.get(c.getLabelA());
			if(degreeB==null)
				degreeB=0;
			nodeAmap.put(c.getLabelB(), degreeA+1);
			nodeBmap.put(c.getLabelA(), degreeB+1);
		}
		
		for (int i = 0; i < qryGraph.getNodeCount(); i++) {
			HashMap<Integer, Integer> degreeOutCons = nodeOutLabelDegrees.get(i);
			HashMap<Integer, Integer> degreeInCons = nodeInLabelDegrees.get(i);

			HashMap<Integer, myNode> candidates = supperNodeMap.get(i);
			boolean isValidNode = true;

			for (Iterator<Entry<Integer, myNode>> it = candidates.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, myNode> nodeEntry = it.next();
				myNode node = nodeEntry.getValue();
				isValidNode = true;
				if (degreeOutCons != null)
					for (Iterator<Entry<Integer, Integer>> iterator = degreeOutCons.entrySet().iterator(); iterator
							.hasNext();) {
						Entry<Integer, Integer> entry = iterator.next();
						int label = entry.getKey();
						int degree = entry.getValue();

						if (node.getOutDegree(label) < degree) {
							isValidNode = false;
							break;
						}
					}
				if (isValidNode && degreeInCons != null) {
					for (Iterator<Entry<Integer, Integer>> iterator = degreeInCons.entrySet().iterator(); iterator
							.hasNext();) {
						Entry<Integer, Integer> entry = iterator.next();
						int label = entry.getKey();
						int degree = entry.getValue();

						if (node.getinDegree(label) < degree) {
							isValidNode = false;
							break;
						}
					}
				}
				if (isValidNode == false)
					it.remove();
			}
		}
		
		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		// edge label verify
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> nodeAEdgeLabels = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();// nodeID-->(edge
		HashMap<Integer, HashMap<Integer, ArrayList<Double>>> nodeBEdgeLabels = new HashMap<Integer, HashMap<Integer, ArrayList<Double>>>();// nodeID-->(edge
																																			// labels)
		for (int i = 0; i < cls.size(); i++) {
			ConnectedComponent c = cls.get(i);
			int nodeA = c.getIndexA();
			int nodeB = c.getIndexB();
			EdgeType e = (EdgeType) qryGraph.getEdgeLabel(nodeA, nodeB);
			Double ss = Double.valueOf(e.toString());
			HashMap<Integer, ArrayList<Double>> nodeAEdgemap = nodeAEdgeLabels.get(nodeA);
			HashMap<Integer, ArrayList<Double>> nodeBEdgemap = nodeBEdgeLabels.get(nodeB);
			if (nodeAEdgemap == null) {
				nodeAEdgemap = new HashMap<Integer, ArrayList<Double>>();
				nodeAEdgeLabels.put(nodeA, nodeAEdgemap);
			}
			if (nodeBEdgemap == null) {
				nodeBEdgemap = new HashMap<Integer, ArrayList<Double>>();
				nodeBEdgeLabels.put(nodeB, nodeBEdgemap);
			}
			ArrayList<Double> nodeAEdgelist = nodeAEdgemap.get(c.getLabelB());
			if (nodeAEdgelist == null) {
				nodeAEdgelist = new ArrayList<Double>();
			}
			nodeAEdgelist.add(ss);
			nodeAEdgemap.put(c.getLabelB(), nodeAEdgelist);

			ArrayList<Double> nodeBEdgelist = nodeBEdgemap.get(c.getLabelA());
			if (nodeBEdgelist == null) {
				nodeBEdgelist = new ArrayList<Double>();
			}
			nodeBEdgelist.add(ss);
			nodeBEdgemap.put(c.getLabelA(), nodeBEdgelist);
		}

		for (int i = 0; i < qryGraph.getNodeCount(); i++) {
			HashMap<Integer, ArrayList<Double>> nodeAEdgeCons = nodeAEdgeLabels.get(i);
			HashMap<Integer, ArrayList<Double>> nodeBEdgeCons = nodeBEdgeLabels.get(i);
			boolean isValidNode = true;
			HashMap<Integer, myNode> superNode = supperNodeMap.get(i);
			for (Iterator<Entry<Integer, myNode>> it = superNode.entrySet().iterator(); it.hasNext();) {
				Entry<Integer, myNode> nodeEntry = it.next();
				myNode node = nodeEntry.getValue();
				isValidNode = true;
				if (nodeAEdgeCons != null)
					for (Iterator<Entry<Integer, ArrayList<Double>>> iterator = nodeAEdgeCons.entrySet()
							.iterator(); iterator.hasNext();) {
						Entry<Integer, ArrayList<Double>> entry = iterator.next();
						int label = entry.getKey();
						Collection<Double> edgeLabel = entry.getValue();
						if (!node.getAEdgeLabelsList(label).containsAll(edgeLabel)) {
							isValidNode = false;
							break;
						}
					}

				if (isValidNode && nodeBEdgeCons != null)
					for (Iterator<Entry<Integer, ArrayList<Double>>> iterator = nodeBEdgeCons.entrySet()
							.iterator(); iterator.hasNext();) {
						Entry<Integer, ArrayList<Double>> entry = iterator.next();
						int label = entry.getKey();
						Collection<Double> edgeLabel = entry.getValue();
						if (!node.getBEdgeLabelsList(label).containsAll(edgeLabel)) {
							isValidNode = false;
							break;
						}
					}

				if (isValidNode == false)
					it.remove();
			}
		}
		
		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		// Database join operator
		HashMap<String, ArrayList<int[]>> HMTWithNodes = graph.getHMTnodesTable();
		// contains more than 2 components
		for (int i = 1; i < cls.size(); i++) {
			ConnectedComponent c = cls.get(i);
			int labelA = c.getLabelA();// node label
			int labelB = c.getLabelB();
			int edgeLabel = (int) c.getEdgeLabel();
			String pattern = labelA + "_" + edgeLabel + "+" + labelB;// "1_6+2"
			// direction: A->B
			ArrayList<int[]> curNodesList = HMTWithNodes.get(pattern);// [1,2] [3,2]
			int nodeA = c.getIndexA();// node index in pattern
			int nodeB = c.getIndexB();
			int edgeIdx = qry.getListGraph().getEdge(nodeA, nodeB);

			// get the minimum of nodeA and nodeB, and the direction
			int direction = 0;
			int minIdx = nodeA;
			int label = labelA;
			if (nodeA > nodeB) {
				direction = 1;
				minIdx = nodeB;
				label = labelB;
			}
			// the minimum one should be joined
			HashMap<Integer, myNode> joinSet = supperNodeMap.get(minIdx);// <nodeIdx, myNode>
			// enumerate all the edges
			IntIterator edgeIndecies = qryGraph.getEdgeIndices(minIdx);
			while (edgeIndecies.hasNext()) {
				int edges = edgeIndecies.next();
				if (edgeIdx != edges) {
					int reachableNodeIdx = qryGraph.getOtherNode(edges, minIdx);
					int reachNodeLabel = qryGraph.getNodeLabel(reachableNodeIdx);
					int direMin = qryGraph.getDirection(edges, minIdx);// (0,1) -> -1
					String previousPString = "";
					/////////////////////////////////////////////////////////////////////////////////////
					if (direMin == -1) {// previous edge direction flag
						EdgeType e = (EdgeType) qryGraph.getEdgeLabel(reachableNodeIdx, minIdx);
						int reachEdgeLabel = Integer.valueOf(e.toString());
						previousPString = reachNodeLabel + "_" + String.valueOf(reachEdgeLabel) + "+" + label;
//								if (previousPString.equals(pattern)) {// same pattern edge can not join
//									continue;
//								}
						ArrayList<int[]> preNodesList = HMTWithNodes.get(previousPString);
						//////////////////////////////////////////////////////////////////
						if (direction == 0) {// joined one is nodeA, first column of list
							HashSet<Integer> midSet = new HashSet<Integer>();
							for (int m = 0; m < curNodesList.size(); m++) {
								if (!midSet.contains(curNodesList.get(m)[0])) {
									midSet.add(curNodesList.get(m)[0]);
								}
							}
							HashSet<Integer> preMidSet = new HashSet<Integer>();
//									if (label != reachNodeLabel) {// pattern edge has distinct node labels
							for (int m = 0; m < preNodesList.size(); m++) {
								if (!preMidSet.contains(preNodesList.get(m)[1])) {
									preMidSet.add(preNodesList.get(m)[1]);
								}
							}
//									}
//									if (label == reachNodeLabel) {// same node labels
//										continue;
//									}
							midSet.retainAll(preMidSet);// join the results
							for (Iterator<Entry<Integer, myNode>> it = joinSet.entrySet().iterator(); it.hasNext();) {
								Entry<Integer, myNode> nodeEntry = it.next();
								if (!midSet.contains(nodeEntry.getKey())) {
									it.remove();// remove unjoined nodes from super node
								}
							}
							//////////////////////////////////////////////////////////////////
						} else if (direction == 1) {// joined one is nodeB, second column of list
							// joined one is nodeA, first column of list
							HashSet<Integer> midSet = new HashSet<Integer>();
							for (int m = 0; m < curNodesList.size(); m++) {
								if (!midSet.contains(curNodesList.get(m)[1])) {
									midSet.add(curNodesList.get(m)[1]);
								}
							}
							HashSet<Integer> preMidSet = new HashSet<Integer>();
//									if (label != reachNodeLabel) {
							for (int m = 0; m < preNodesList.size(); m++) {
								if (!preMidSet.contains(preNodesList.get(m)[1])) {
									preMidSet.add(preNodesList.get(m)[1]);
								}
							}
//									}
//									if (label == reachNodeLabel) {
//										continue;
//									}
							midSet.retainAll(preMidSet);
							for (Iterator<Entry<Integer, myNode>> it = joinSet.entrySet().iterator(); it.hasNext();) {
								Entry<Integer, myNode> nodeEntry = it.next();
								if (!midSet.contains(nodeEntry.getKey())) {
									it.remove();
								}
							}
						}
						//////////////////////////////////////////////////////////////////////////
					} else if (direMin == 1) {// previous edge direction flag
						EdgeType e = (EdgeType) qryGraph.getEdgeLabel(minIdx, reachableNodeIdx);
						int reachEdgeLabel = Integer.valueOf(e.toString());
						previousPString = label + "_" + String.valueOf(reachEdgeLabel) + "+" + reachNodeLabel;
//								if (previousPString.equals(pattern)) {// same pattern edge can not join
//									continue;
//								}
						ArrayList<int[]> preNodesList = HMTWithNodes.get(previousPString);
						//////////////////////////////////////////////////////////////////
						if (direction == 0) {// joined one is nodeA, first column of list
							HashSet<Integer> midSet = new HashSet<Integer>();
							for (int m = 0; m < curNodesList.size(); m++) {
								if (!midSet.contains(curNodesList.get(m)[0])) {
									midSet.add(curNodesList.get(m)[0]);
								}
							}
							HashSet<Integer> preMidSet = new HashSet<Integer>();
//									if (label != reachNodeLabel) {// pattern edge has distinct node labels
							for (int m = 0; m < preNodesList.size(); m++) {
								if (!preMidSet.contains(preNodesList.get(m)[0])) {
									preMidSet.add(preNodesList.get(m)[0]);
								}
							}
//									}
//									if (label == reachNodeLabel) {// same node labels
//										continue;
//									}
							midSet.retainAll(preMidSet);// join the results
							for (Iterator<Entry<Integer, myNode>> it = joinSet.entrySet().iterator(); it.hasNext();) {
								Entry<Integer, myNode> nodeEntry = it.next();
								if (!midSet.contains(nodeEntry.getKey())) {
									it.remove();
								}
							}
							//////////////////////////////////////////////////////////////////
						} else if (direction == 1) {// joined one is nodeB, second column of list
							HashSet<Integer> midSet = new HashSet<Integer>();
							for (int m = 0; m < curNodesList.size(); m++) {
								if (!midSet.contains(curNodesList.get(m)[1])) {
									midSet.add(curNodesList.get(m)[1]);
								}
							}
							HashSet<Integer> preMidSet = new HashSet<Integer>();
//									if (label != reachNodeLabel) {
							for (int m = 0; m < preNodesList.size(); m++) {
								if (!preMidSet.contains(preNodesList.get(m)[1])) {
									preMidSet.add(preNodesList.get(m)[1]);
								}
							}
//									}
//									if (label == reachNodeLabel) {
//										continue;
//									}
							midSet.retainAll(preMidSet);
							for (Iterator<Entry<Integer, myNode>> it = joinSet.entrySet().iterator(); it.hasNext();) {
								Entry<Integer, myNode> nodeEntry = it.next();
								if (!midSet.contains(nodeEntry.getKey())) {
									it.remove();
								}
							}
						}
					}

				}
			}

		}
		
		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		// create the variables
		variables = new Variable[qryGraph.getNodeCount()];
		for (int i = 0; i < qryGraph.getNodeCount(); i++) {
			int label = qryGraph.getNodeLabel(i);
			variables[i] = new Variable(i, label, supperNodeMap.get(i), null, null);
		}

		for (int i = 0; i < cls.size(); i++) {
			ConnectedComponent c = cls.get(i);
			int nodeA = c.getIndexA();
			int nodeB = c.getIndexB();

			variables[nodeA].addConstraintWith(nodeB, c.getEdgeLabel());
			variables[nodeB].addConstrainedBy(nodeA, c.getEdgeLabel());
		}
		return supperNodeMap;
	}
	public <EdgeType> HashMap<Integer, HashMap<Integer, myNode>> getOneEdgeSupperNodeList(Graph graph, Query qry, int kthFreq)
	{
		HashMap<Integer, HashMap<Integer,myNode>> supperNodeMap= new HashMap<Integer, HashMap<Integer,myNode>>();// QueryID -> NodeID->NODE
		ArrayList<ConnectedComponent> cls= qry.getConnectedLabels();
			
		HPListGraph<Integer, Double> qryGraph = qry.getListGraph();
		//refine according to nodeLabels
		for (int i = 0; i < qryGraph.getNodeCount(); i++) 
		{
			int label= qryGraph.getNodeLabel(i);//nodeLabel
			supperNodeMap.put(i, (HashMap<Integer,myNode>)graph.getFreqNodesByLabel().get(label).clone());
		}
		
		int min;
		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		//Database join operator(one edge)
		HashMap<String, ArrayList<int[]>> HMTWithNodes = graph.getHMTnodesTable();
		// pattern edges
		ConnectedComponent c = cls.get(0);
		int labelA = c.getLabelA();// node label
		int labelB = c.getLabelB();
//		if (labelA != labelB) {
		int nodeA = c.getIndexA();// node index in pattern
		int nodeB = c.getIndexB();// node index in pattern
		int edgeLabel = (int) c.getEdgeLabel();
		String pattern = labelA + "_" + edgeLabel + "+" + labelB;// "1_6+2"
		// direction: A->B
		ArrayList<int[]> curNodesList = HMTWithNodes.get(pattern);
		// node A
		HashMap<Integer, myNode> joinSetA = supperNodeMap.get(nodeA);// <nodeIdx, myNode>
		HashSet<Integer> midSetA = new HashSet<Integer>();
		for (int m = 0; m < curNodesList.size(); m++) {
			if (!midSetA.contains(curNodesList.get(m)[0])) {
				midSetA.add(curNodesList.get(m)[0]);
			}
		}
		for (Iterator<Entry<Integer, myNode>> it = joinSetA.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, myNode> nodeEntry = it.next();
			if (!midSetA.contains(nodeEntry.getKey())) {
				it.remove();
			}
		}
		// node B
		HashMap<Integer, myNode> joinSetB = supperNodeMap.get(nodeB);// <nodeIdx, myNode>
		HashSet<Integer> midSetB = new HashSet<Integer>();
		for (int m = 0; m < curNodesList.size(); m++) {
			if (!midSetB.contains(curNodesList.get(m)[1])) {
				midSetB.add(curNodesList.get(m)[1]);
			}
		}
		for (Iterator<Entry<Integer, myNode>> it = joinSetB.entrySet().iterator(); it.hasNext();) {
			Entry<Integer, myNode> nodeEntry = it.next();
			if (!midSetB.contains(nodeEntry.getKey())) {
				it.remove();
			}
//			}
		}

		min = supperNodeMap.get(0).size();
		for(Entry<Integer, HashMap<Integer, myNode>> entry:supperNodeMap.entrySet()){
			if(min > entry.getValue().size()){
				min=entry.getValue().size();
			}
		}
		if(min <= kthFreq) {
			return null;
		}
		
		return supperNodeMap;
	}
}
