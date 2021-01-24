/**  
 * Project Name:frequentPatternsMiningV1.1  
 * File Name:OneEdgeExtension.java  
 * Package Name:mineSteps  
 * Date:Mar 20, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/

package com.dbg.patternmining.models.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.dbg.patternmining.models.dataStructures.CorePattern;
import com.dbg.patternmining.models.dataStructures.DFSCode;
import com.dbg.patternmining.models.dataStructures.Extension;
import com.dbg.patternmining.models.dataStructures.Frequency;
import com.dbg.patternmining.models.dataStructures.GSpanEdge;
import com.dbg.patternmining.models.dataStructures.GSpanExtension;
import com.dbg.patternmining.models.dataStructures.Graph;
import com.dbg.patternmining.models.dataStructures.HPGraph;
import com.dbg.patternmining.models.dataStructures.HPListGraph;
import com.dbg.patternmining.models.dataStructures.HPMutableGraph;

/**
 * ClassName:OneEdgeExtension Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: Mar 20, 2019
 * 
 * @author JIAN
 * @version
 * @since JDK 1.6
 * @see
 */
public class OneCoreEdgeExtension<NodeType, EdgeType> extends GenerationPartialStep<NodeType, EdgeType> {

	public static int counter = 0;

	private final Map<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>> children;
	private Map<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>> initials;
	private List<String> coreHMT;
	private static HashMap<String,Integer> oneEdgeMNImap;
	private static ArrayList<Integer> sortedNodeLabels;
	private static Graph kGraph;
	private static int[][] maxDegreeMatrix;
	private static CorePattern inPattern;

	/**
	 * creates a new pruning
	 * 
	 * @param next the next step of the generation chain
	 * @param tenv the environment used for releasing unused objects
	 */
	public OneCoreEdgeExtension(final GenerationPartialStep<NodeType, EdgeType> next,CorePattern inPattern, 
			List<String> coreOrginEdges, int[][] maxDegreeMatrix,ArrayList<Integer> sortedNodeLabels) {
		super(next);
		this.children = new TreeMap<GSpanEdge<NodeType, EdgeType>, GSpanExtension<NodeType, EdgeType>>();
		this.coreHMT = coreOrginEdges;
		this.maxDegreeMatrix = maxDegreeMatrix;
		this.sortedNodeLabels = sortedNodeLabels;
		this.inPattern = inPattern;
		this.initials = new HashMap<GSpanEdge<NodeType, EdgeType>, DFSCode<NodeType, EdgeType>>();
		this.initials.putAll(initials);
	}
	
	public static void sendOneEdgeMNIMap(HashMap<String,Integer> map) {
		oneEdgeMNImap = map;
	}
	

	/**
	 * includes the found extension to the corresponding fragment(original)
	 * 
	 * @param gEdge
	 * @param emb
	 * @param code
	 * @param edge
	 * @param nodeB
	 */
	protected void add(final GSpanEdge<NodeType, EdgeType> gEdge, final DFSCode<NodeType, EdgeType> code, int type) {
		// search corresponding extension
		GSpanExtension<NodeType, EdgeType> ext = children.get(gEdge);

		if (ext == null) {
			// create new extension
			HPMutableGraph<NodeType, EdgeType> ng = (HPMutableGraph<NodeType, EdgeType>) code.getHPlistGraph().clone();
			// TODO: avoid clone??
			gEdge.addTo(ng); // reformulate the form of the new extended fragment!!
			ext = new GSpanExtension<NodeType, EdgeType>();
			ext.edge = gEdge;
			ext.frag = new DFSCode<NodeType, EdgeType>(code.getSortedFreqLabels(), code.getSingleGraph(), new HashMap<Integer, HashSet<Integer>>())
					.set((HPListGraph<NodeType, EdgeType>) ng, code.getFirst(), code.getLast(), code.getParents());
			ext.frag = (DFSCode<NodeType, EdgeType>) code.extend(ext); // PUT THE STRING HERE

			children.put(gEdge, ext);
		} else {
			gEdge.release();
		}
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.MiningStep#call(de.parsemis.miner.SearchLatticeNode,
	 * java.util.Collection)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node,
			final Collection<Extension<NodeType, EdgeType>> extensions) {
		// just give YOUR extensions to the next step
		extensions.clear();
		extensions.addAll(children.values());
		callNext(node, extensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#call(de.parsemis.miner.
	 * SearchLatticeNode, de.parsemis.graph.Embedding)
	 */
	@Override
	public void call(final SearchLatticeNode<NodeType, EdgeType> node) {
		extend((DFSCode<NodeType, EdgeType>) node);
		callNext(node); // malhash aii lazma
	}
	
	protected final void extend(DFSCode<NodeType, EdgeType> pattern) {

		final HPGraph<NodeType, EdgeType> subGraph = pattern.getHPlistGraph();
		final int lastNode = subGraph.getNodeCount() - 1;// 1
		for (int i = 0; i <= lastNode; i++) {// iterate all nodes in this pattern, start at node 0
			int nodeLabeli = (Integer) subGraph.getNodeLabel(i);// A
			int pInDegree = pattern.getHPlistGraph().getInDegree(i);
			int pOutDegree = pattern.getHPlistGraph().getOutDegree(i);
			
			int maxInDegree = maxDegreeMatrix[nodeLabeli][0];//the max in degree of node label
			int maxOutDegree = maxDegreeMatrix[nodeLabeli][1];//the max out degree of node label
			
			// check if the pattern can extend as a cycle
			for (int j = i + 1; j <= lastNode; j++) {
				int hasEdge = subGraph.getEdge(i, j);// if has an edge between node i and j
				// check if can add an edge
				if (hasEdge == -1) {// don't has an edge between this two nodes
					int nodeLabelj = (Integer) subGraph.getNodeLabel(j);
					
					// incoming
					if (pInDegree < maxInDegree) {// in degree is not tight
						Iterator it = coreHMT.iterator();
						
						while (it.hasNext()) {
							String oneEdgePatternString = (String)it.next();//0_0_1
							//find the in degree node label 
							int nodeLabelA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int inEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							
							if(nodeLabelB == nodeLabeli && nodeLabelA == nodeLabelj){
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>()
										.set(i, j, sortedNodeLabels.indexOf(nodeLabeli), inEdgeLabel,
												sortedNodeLabels.indexOf(nodeLabelj), -1, nodeLabeli,
												nodeLabelj);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
						}
						
					}else if(pInDegree >= maxInDegree){
//						continue;
					}
					
					// outcoming
					if (pOutDegree < maxOutDegree) {// out degree is not tight
						Iterator it = coreHMT.iterator();
						while (it.hasNext()) {
							String oneEdgePatternString = (String)it.next();
							//find the in degree node label 
							int nodeLabelA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int outEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							
							if(nodeLabelA == nodeLabeli && nodeLabelB == nodeLabelj){
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>()
										.set(i, j, sortedNodeLabels.indexOf(nodeLabeli), outEdgeLabel,
												sortedNodeLabels.indexOf(nodeLabelj), 1, nodeLabeli,
												nodeLabelj);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
							
						}
					}else if(pOutDegree >= maxOutDegree){
//						continue;
					}

				}else if (hasEdge != -1) {// already has an edge
					continue;
				}

			}
			
			
			// add an new edge extension
			// if node i is not tight
			if (pInDegree < maxInDegree) {// in degree is not tight

				Iterator it = coreHMT.iterator();
				while (it.hasNext()) {
					String oneEdgePatternString = (String)it.next();
					// find the in degree node label
					int nodeLabelA = Integer.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
					int inEdgeLabel = Integer.parseInt(oneEdgePatternString
							.substring(oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
					int nodeLabelB = Integer.parseInt(oneEdgePatternString.substring(oneEdgePatternString.indexOf("+") + 1,
							oneEdgePatternString.length()));

					if (nodeLabelB == nodeLabeli) {
						// add this edge in
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(i,
								lastNode + 1, sortedNodeLabels.indexOf(nodeLabeli), inEdgeLabel,
								sortedNodeLabels.indexOf(nodeLabelA), -1, nodeLabeli, nodeLabelA);
						if (((pattern.getLast().compareTo(gEdge) < 0))) {
							add(gEdge, pattern, 0);
						} else {
							gEdge.release();
						}

					}

				}
			} else if (pInDegree >= maxInDegree) {
//				continue;
			}
			
			
			if (pOutDegree < maxOutDegree) {// out degree is not tight
				Iterator it = coreHMT.iterator();
				while (it.hasNext()) {
					String oneEdgePatternString = (String)it.next();
					// find the in degree node label
					int nodeLabelA = Integer.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
					int outEdgeLabel = Integer.parseInt(oneEdgePatternString
							.substring(oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
					int nodeLabelB = Integer.parseInt(oneEdgePatternString.substring(oneEdgePatternString.indexOf("+") + 1,
							oneEdgePatternString.length()));

					if (nodeLabelA == nodeLabeli) {
						// add this edge in
						final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(i,
								lastNode + 1, sortedNodeLabels.indexOf(nodeLabeli), outEdgeLabel,
								sortedNodeLabels.indexOf(nodeLabelB), 1, nodeLabeli, nodeLabelB);
						if (((pattern.getLast().compareTo(gEdge) < 0))) {
							add(gEdge, pattern, 0);
						} else {
							gEdge.release();
						}
					}
				}
			} else if (pOutDegree >= maxOutDegree) {
//				continue;
			}
		}
	
	}
	
	protected final void extendCore(DFSCode<NodeType, EdgeType> pattern){
		final HPGraph<NodeType, EdgeType> subGraph = pattern.getHPlistGraph();
		int edgeCount = subGraph.getEdgeCount();
		final int lastNode = subGraph.getNodeCount() - 1;

		
		
		// check if the pattern can extend as a cycle
		for (int i = 0; i <= lastNode; i++) {// iterate all nodes in this pattern, start at node 0
			for (int j = i + 1; j <= lastNode; j++) {
				int hasEdge = subGraph.getEdge(i, j);// if has an edge between node i and j
				// check if can add an edge
				if (hasEdge == -1) {// don't has an edge between this two nodes
					int nodeLabelj = (Integer) subGraph.getNodeLabel(j);
					
					
					
				}else if (hasEdge != -1) {// already has an edge
					continue;
				}
			}
		}
		
		//based on the edge index to extend
		for(int edgeIdx=0; edgeIdx < edgeCount; edgeIdx++) {
			//for query pattern
			EdgeType a = subGraph.getEdgeLabel(edgeIdx);
			String b = a.toString();
			int edgeLabel = Integer.valueOf(b);
			int nodeAIdx = subGraph.getNodeA(edgeIdx);//0
			int nodeBIdx = subGraph.getNodeB(edgeIdx);//1
			int nodeALabel=Integer.valueOf(subGraph.getNodeLabel(nodeAIdx).toString());
			int nodeBLabel=Integer.valueOf(subGraph.getNodeLabel(nodeBIdx).toString());
			
			//for node A
			int pInDegree = pattern.getHPlistGraph().getInDegree(nodeAIdx);
			int pOutDegree = pattern.getHPlistGraph().getOutDegree(nodeAIdx);
			int maxInDegree = maxDegreeMatrix[nodeAIdx][0];//the max in degree of node label
			int maxOutDegree = maxDegreeMatrix[nodeAIdx][1];//the max out degree of node label
			
			if (pInDegree < maxInDegree) {// in degree is not tight
				Iterator it = coreHMT.iterator();
				while(it.hasNext()) {
					String patternEdge = (String) it.next();//from core_HMT: 1_1+2
					int edge = Integer.parseInt(patternEdge.substring(patternEdge.indexOf("_") + 1, patternEdge.indexOf("+")));
					if(edgeLabel == edge) {	
						// add this edge in
						for(int i=0;i<coreHMT.size();i++) {
							String oneEdgePatternString = coreHMT.get(i);
							int nodeLabelAA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int inEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelBB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							if(nodeLabelBB == nodeALabel) {
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(nodeAIdx,
										lastNode + 1, sortedNodeLabels.indexOf(nodeALabel), inEdgeLabel,
										sortedNodeLabels.indexOf(nodeLabelAA), -1, nodeALabel, nodeLabelAA);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
						}
					}
					
				}
				
			}else if(pInDegree >= maxInDegree) {
				
			}
			
			
			if (pOutDegree < maxOutDegree) {// out degree is not tight
				Iterator it = coreHMT.iterator();
				while(it.hasNext()) {
					String patternEdge = (String) it.next();//from core_HMT: 1_1+2
					int edge = Integer.parseInt(patternEdge.substring(patternEdge.indexOf("_") + 1, patternEdge.indexOf("+")));
					if(edgeLabel == edge) {	
						// add this edge in
						for(int i=0;i<coreHMT.size();i++) {
							String oneEdgePatternString = coreHMT.get(i);
							int nodeLabelAA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int outEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelBB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							if(nodeLabelAA == nodeALabel) {
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(nodeAIdx,
										lastNode + 1, sortedNodeLabels.indexOf(nodeALabel), outEdgeLabel,
										sortedNodeLabels.indexOf(nodeLabelBB), 1, nodeALabel, nodeLabelBB);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
						}
					}
					
				}
				
			}else if(pOutDegree >= maxOutDegree) {
				
			}

			
			//for node B
			int pBInDegree = pattern.getHPlistGraph().getInDegree(nodeBIdx);
			int pBOutDegree = pattern.getHPlistGraph().getOutDegree(nodeBIdx);
			int maxBInDegree = maxDegreeMatrix[nodeBIdx][0];//the max in degree of node label
			int maxBOutDegree = maxDegreeMatrix[nodeBIdx][1];//the max out degree of node label
			
			if (pBInDegree < maxBInDegree) {// in degree is not tight
				Iterator it = coreHMT.iterator();
				while(it.hasNext()) {
					String patternEdge = (String) it.next();//from core_HMT: 1_1+2
					int edge = Integer.parseInt(patternEdge.substring(patternEdge.indexOf("_") + 1, patternEdge.indexOf("+")));
					if(edgeLabel == edge) {	
						// add this edge in
						for(int i=0;i<coreHMT.size();i++) {
							String oneEdgePatternString = coreHMT.get(i);
							int nodeLabelAA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int inEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelBB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							if(nodeLabelBB == nodeBLabel) {
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(nodeBIdx,
										lastNode + 1, sortedNodeLabels.indexOf(nodeBLabel), inEdgeLabel,
										sortedNodeLabels.indexOf(nodeLabelAA), -1, nodeBLabel, nodeLabelAA);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
						}
					}
					
				}
				
			}else if(pBInDegree >= maxBInDegree) {
				
			}
			
			
			if (pBOutDegree < maxBOutDegree) {// out degree is not tight
				Iterator it = coreHMT.iterator();
				while(it.hasNext()) {
					String patternEdge = (String) it.next();//from core_HMT: 1_1+2
					int edge = Integer.parseInt(patternEdge.substring(patternEdge.indexOf("_") + 1, patternEdge.indexOf("+")));
					if(edgeLabel == edge) {	
						// add this edge in
						for(int i=0;i<coreHMT.size();i++) {
							String oneEdgePatternString = coreHMT.get(i);
							int nodeLabelAA = Integer
									.parseInt(oneEdgePatternString.substring(0, oneEdgePatternString.indexOf("_")));
							int outEdgeLabel = Integer.parseInt(oneEdgePatternString.substring(
									oneEdgePatternString.indexOf("_") + 1, oneEdgePatternString.indexOf("+")));
							int nodeLabelBB = Integer.parseInt(oneEdgePatternString
									.substring(oneEdgePatternString.indexOf("+") + 1, oneEdgePatternString.length()));
							if(nodeLabelAA == nodeBLabel) {
								// add this edge in
								final GSpanEdge<NodeType, EdgeType> gEdge = new GSpanEdge<NodeType, EdgeType>().set(nodeBIdx,
										lastNode + 1, sortedNodeLabels.indexOf(nodeBLabel), outEdgeLabel,
										sortedNodeLabels.indexOf(nodeLabelBB), 1, nodeBLabel, nodeLabelBB);
								if (((pattern.getLast().compareTo(gEdge) < 0))) {
									add(gEdge, pattern, 0);
								} else {
									gEdge.release();
								}
							}
						}
					}
					
				}
				
			}else if(pBOutDegree >= maxBOutDegree) {
				
			}
			
			
		}
		
		
	}

	/*
	 * if has same value between two lists Yes:return this value No:return -1
	 */
	public static int ifSame(List<Integer> a, List<Integer> b) {
		Iterator<Integer> ait = a.iterator();
		Iterator<Integer> bit = b.iterator();
		while (ait.hasNext()) {
			int inEdgeLabel = ait.next();// 3
			while (bit.hasNext()) {
				int outEdgeLabel = bit.next();
				if (inEdgeLabel == outEdgeLabel) {
					return inEdgeLabel;
				} else {
					
				}
			}

		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.parsemis.miner.GenerationPartialStep#reset()
	 */
	@Override
	public void reset() {
		children.clear();
		resetNext();
	}

}
