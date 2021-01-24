/**  
 * Project Name:Grami_Join_Compare  
 * File Name:JoinOperation.java  
 * Package Name:joinAlgorithm  
 * Date:2020年1月17日  
 * Copyright (c) 2020, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dbg.patternmining.models.CSP.ConstraintGraph;
import com.dbg.patternmining.models.dataStructures.Frequency;
import com.dbg.patternmining.models.dataStructures.Graph;
import com.dbg.patternmining.models.dataStructures.HPListGraph;
import com.dbg.patternmining.models.dataStructures.IntIterator;
import com.dbg.patternmining.models.dataStructures.Query;
import com.dbg.patternmining.models.dataStructures.myNode;
import com.dbg.patternmining.models.search.SearchLatticeNode;
import com.dbg.patternmining.models.utilities.MyPair;
import com.dbg.patternmining.models.utilities.Settings;
import com.dbg.patternmining.models.utilities.StopWatch;

/**  
 * ClassName:JoinOperation   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     2020年1月17日   
 * @author   user  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class JoinOperation {
	private HPListGraph<Integer, Double> qryGraph;
	private int frq;
	private RETables[] RET;
	private ArrayList<ConnectedComponents> Edges;
	private static Graph KG;
	private int qrySize;
	private static HashMap<Integer,Integer> nodeIdxLabelMap;
	private static int insTableSize;

	public JoinOperation(Graph singleGraph) {
		this.KG = singleGraph;
		this.insTableSize = 0;
	}
	
	public JoinOperation(){
		
	}
	
	public int getInsSize(){
		return insTableSize;
	}

	public <NodeType, EdgeType> JoinOperation(SearchLatticeNode<NodeType, EdgeType> pattern) {
		Query qry = new Query((HPListGraph<Integer, Double>) pattern.getHPlistGraph());
		GetSuperNodes gs = new GetSuperNodes(KG, qry);
		this.RET = gs.getReTables();
		this.qryGraph = qry.getListGraph();
		this.Edges = qry.getConnectedEdges();
		this.qrySize = qryGraph.getNodeCount();
		this.nodeIdxLabelMap = gs.getNodeIdxLabelMap();
	}
	
	public <NodeType, EdgeType> int getMNIfrqByBasicJoin(SearchLatticeNode<NodeType, EdgeType> code) {
		HashSet<Integer> nodeIdxSet = new HashSet<Integer>();
		RETInstances reInstances = new RETInstances();

		while (Edges.size() != 0) {
			for (int k = 0; k < Edges.size(); k++) {
				ConnectedComponents edge = Edges.get(k);
				int labelA = edge.getLabelA();// node label
				int labelB = edge.getLabelB();
				double edgeLabel = edge.getEdgeLabel();
				int nodeA = edge.getIndexA();// node index in pattern
				int nodeB = edge.getIndexB();
				RETables nodesA = RET[nodeA];
				RETables nodesB = RET[nodeB];
				String pattern = labelA + "_" + edgeLabel + "+" + labelB;
//				System.out.println("current pattern:" + pattern);
				// direction: A->B
				ArrayList<NodePairList> tobeJoinTable = getNodepairs(nodesA, edgeLabel, nodesB);
				if (nodeIdxSet != null && (nodeIdxSet.contains(nodeA) || nodeIdxSet.contains(nodeB))) {
					if (nodeIdxSet.contains(nodeA) && nodeIdxSet.contains(nodeB)) {
						// cyclic join
						reInstances = joinForCyclic(qryGraph, nodeA, nodeB, reInstances, tobeJoinTable);
					} else {
						if (nodeIdxSet.contains(nodeA) && !nodeIdxSet.contains(nodeB)) {// join by nodeA
							nodeIdxSet.add(nodeB);
							int pairColloumnA = 0;
							int pairColloumnB = 1;
							reInstances = joinTables(nodeA, nodeB, labelB, tobeJoinTable, reInstances, pairColloumnA, pairColloumnB);
						} else if (nodeIdxSet.contains(nodeB) && !nodeIdxSet.contains(nodeA)) {// join by nodeB
							nodeIdxSet.add(nodeA);
							int pairColloumnA = 1;
							int pairColloumnB = 0;
							reInstances = joinTables(nodeB, nodeA, labelA, tobeJoinTable, reInstances, pairColloumnA, pairColloumnB);
						}
					}
					Edges.remove(k);
				} else if (nodeIdxSet == null || nodeIdxSet.size() == 0) {// first edge
					for (int i = 0; i < tobeJoinTable.size(); i++) {
						InstanceTable instance = new InstanceTable(qrySize,nodeIdxLabelMap);
						instance.assign(nodeA, tobeJoinTable.get(i).get(0));// (pattern nodeIdx, node)
						instance.assign(nodeB, tobeJoinTable.get(i).get(1));// (pattern nodeIdx, node)
						reInstances.addItem(instance);
						nodeIdxSet.add(nodeA);
						nodeIdxSet.add(nodeB);
					}
					Edges.remove(k);
				}
				if (reInstances == null) {
					return 0;
				} else if (reInstances.getSize() == 0) {
					return 0;
				}
			}
		}
		ArrayList<InstanceTable> result = reInstances.getInsList();
		
		Iterator it = result.iterator();
		while(it.hasNext()){
			InstanceTable insTable = (InstanceTable) it.next();
			int[] instances = insTable.getIsoInstances();
			code.setInstances(instances);
		}
		
		frq = getMNIfrq(result);
//		System.out.println("joinFreq:"+frq);
		return frq;
	}
	
	public ArrayList<InstanceTable> getInstancesByBasicJoin() {
		HashSet<Integer> nodeIdxSet = new HashSet<Integer>();
		RETInstances reInstances = new RETInstances();

		while (Edges.size() != 0) {
			for (int k = 0; k < Edges.size(); k++) {
				ConnectedComponents edge = Edges.get(k);
				int labelA = edge.getLabelA();// node label
				int labelB = edge.getLabelB();
				double edgeLabel = edge.getEdgeLabel();
				int nodeA = edge.getIndexA();// node index in pattern
				int nodeB = edge.getIndexB();
				RETables nodesA = RET[nodeA];
				RETables nodesB = RET[nodeB];
				String pattern = labelA + "_" + edgeLabel + "+" + labelB;
//				System.out.println("current pattern:" + pattern);
				// direction: A->B
				ArrayList<NodePairList> tobeJoinTable = getNodepairs(nodesA, edgeLabel, nodesB);
				if (nodeIdxSet != null && (nodeIdxSet.contains(nodeA) || nodeIdxSet.contains(nodeB))) {
					if (nodeIdxSet.contains(nodeA) && nodeIdxSet.contains(nodeB)) {
						// cyclic join
						reInstances = joinForCyclic(qryGraph, nodeA, nodeB, reInstances, tobeJoinTable);
					} else {
						if (nodeIdxSet.contains(nodeA) && !nodeIdxSet.contains(nodeB)) {// join by nodeA
							nodeIdxSet.add(nodeB);
							int pairColloumnA = 0;
							int pairColloumnB = 1;
							reInstances = joinTables(nodeA, nodeB, labelB, tobeJoinTable, reInstances, pairColloumnA, pairColloumnB);
						} else if (nodeIdxSet.contains(nodeB) && !nodeIdxSet.contains(nodeA)) {// join by nodeB
							nodeIdxSet.add(nodeA);
							int pairColloumnA = 1;
							int pairColloumnB = 0;
							reInstances = joinTables(nodeB, nodeA, labelA, tobeJoinTable, reInstances, pairColloumnA, pairColloumnB);
						}
					}
					Edges.remove(k);
				} else if (nodeIdxSet == null || nodeIdxSet.size() == 0) {// first edge
					for (int i = 0; i < tobeJoinTable.size(); i++) {
						InstanceTable instance = new InstanceTable(qrySize,nodeIdxLabelMap);
						instance.assign(nodeA, tobeJoinTable.get(i).get(0));// (pattern nodeIdx, node)
						instance.assign(nodeB, tobeJoinTable.get(i).get(1));// (pattern nodeIdx, node)
						reInstances.addItem(instance);
						nodeIdxSet.add(nodeA);
						nodeIdxSet.add(nodeB);
					}
					Edges.remove(k);
				}
				if (reInstances == null) {
					return null;
				} else if (reInstances.getSize() == 0) {
					return null;
				}
			}
		}
		ArrayList<InstanceTable> result = reInstances.getInsList();
		return result;
	}
	
	public ArrayList<NodePairList> getNodepairs(RETables nodesA, double edgeLabel, RETables nodesB) {
		ArrayList<NodePairList> reNodePairsList = new ArrayList<NodePairList>();
		HashMap<Integer, myNode> nodeAmap = nodesA.getList();
		HashMap<Integer, myNode> nodeBmap = nodesB.getList();
		int labelB = nodesB.getLabel();
		for (Entry<Integer, myNode> entryA : nodeAmap.entrySet()) {
			int nodeAIdx = entryA.getKey();// 1
			myNode nodeA = entryA.getValue();// 1:1
			// represented by Label~<nodeID,edge_label>, represents the out going nodes
			HashMap<Integer, ArrayList<MyPair<Integer, Double>>> nodeAout = nodeA.getReachableWithNodes();
			if (nodeAout == null)
				continue;
			ArrayList<MyPair<Integer, Double>> nodeAoutList = nodeAout.get(labelB);// 2,6
			if (nodeAoutList == null)
				continue;
			Iterator it = nodeAoutList.iterator();
			while (it.hasNext()) {
				MyPair<Integer, Double> pair = (MyPair<Integer, Double>) it.next();
				int nodeBIdx = pair.getA();
				double curLabel = pair.getB();
				if (nodeBmap.containsKey(nodeBIdx) && curLabel == edgeLabel) {// can connect to nodeA
					// A->B
					NodePairList nPair = new NodePairList();
					nPair.add(0, nodeAIdx);
					nPair.add(1, nodeBIdx);
					reNodePairsList.add(nPair);
				}
			}
		}
		return reNodePairsList;
	}
	
	public <EdgeType> RETInstances joinTables(int joinNodeIdx, int addIdx, int addLabel, ArrayList<NodePairList> tobeJoinTable,
			RETInstances reInstances, int pairColloumnA, int pairColloumnB) {
		RETInstances reRETable = new RETInstances();
		ArrayList<InstanceTable> retList = reInstances.getInsList();
		for (int k = 0; k < retList.size(); k++) {
			InstanceTable ins = retList.get(k);
//			System.out.println("------------");
//			System.out.println("ins:" + ins);
			for (int i = 0; i < tobeJoinTable.size(); i++) {
//				System.out.println("current pair:" + currentNodePairsList.get(i));
//				System.out.println("_"+ins.getIsoInstances(joinNodeIdx) +"_"+currentNodePairsList.get(i).get(pairColloumnA)+"_");
				if (ins.getIsoInstances(joinNodeIdx) == tobeJoinTable.get(i).get(pairColloumnA)) {
					//set joined pair join
					reRETable.joinAssign(ins, addLabel, addIdx, tobeJoinTable.get(i).get(pairColloumnB));
					//this pair has been joined
					tobeJoinTable.get(i).setJoinFlag(true);
					//get the instances size
					int tmp = reRETable.getSize();
					if(insTableSize < tmp){
						insTableSize = tmp;
//						System.out.println("ins size:"+insTableSize);
						
					}
				}
			}
		}
		//remove the unjoined pairs
//		nodesNotJoined(tobeJoinTable);
		return reRETable;
	}
	
	public RETInstances joinForCyclic(HPListGraph<Integer, Double> qryGraph, int nodeA, int nodeB,
			RETInstances reInstances, ArrayList<NodePairList> tobeJoinTable) {
		ArrayList<InstanceTable> instanceList = reInstances.getInsList();
		Iterator it = instanceList.iterator();
		while (it.hasNext()) {
			InstanceTable ins = (InstanceTable) it.next();
			for (int i = 0; i < tobeJoinTable.size(); i++) {
				if ((ins.getIsoInstances(nodeA) == tobeJoinTable.get(i).get(0))
						&& (ins.getIsoInstances(nodeB) == tobeJoinTable.get(i).get(1))) {
					// found a match
					ins.setCycValid(true);
					break;
				}
			}
		}
		// remove the instances that are not satisfy the cyclic
		reInstances.removeCycIvalidIns();
		return reInstances;
	}

	public int getMNIfrq(ArrayList<InstanceTable> instanceList) {
		HashMap<Integer, HashSet<Integer>> MNIinstanceMap = new HashMap<Integer, HashSet<Integer>>();
		Iterator it = instanceList.iterator();
		while(it.hasNext()) {
			InstanceTable midList = (InstanceTable) it.next();
			for(int i=0;i<midList.insSize();i++) {
				HashSet<Integer> LL = MNIinstanceMap.get(i);
				if(LL == null) {
					LL = new HashSet<Integer>();
					LL.add(midList.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}else if(LL != null) {
					LL.add(midList.getIsoInstances(i));
					MNIinstanceMap.put(i, LL);
				}
			}
		}
		int min = MNIinstanceMap.get(0).size();
		for(Entry<Integer, HashSet<Integer>> entry:MNIinstanceMap.entrySet()) {
			int tmp=entry.getValue().size();
			if(min > tmp) {
				min = tmp;
			}
		}
		return min;
	}

}
  
