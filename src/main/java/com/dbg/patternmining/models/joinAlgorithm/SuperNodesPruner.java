/**  
 * Project Name:Grami_Join_Compare  
 * File Name:SuperNodesPruner.java  
 * Package Name:joinAlgorithm  
 * Date:2020年1月18日  
 * Copyright (c) 2020, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dbg.patternmining.models.CSP.Variable;
import com.dbg.patternmining.models.dataStructures.ConnectedComponent;
import com.dbg.patternmining.models.dataStructures.Graph;
import com.dbg.patternmining.models.dataStructures.Query;
import com.dbg.patternmining.models.dataStructures.myNode;

/**  
 * ClassName:SuperNodesPruner   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     2020年1月18日   
 * @author   user  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class SuperNodesPruner {
	private static HashMap<Integer,Integer> nodeIdxLabelMap;
	private RETables[] reTables;
	public RETables[] getRETables() {
		return reTables;
	}
	
	public SuperNodesPruner() {
		nodeIdxLabelMap = new HashMap<Integer,Integer>();
	}
	
	public HashMap<Integer,Integer> getNodeIdxLabelMap() {
		return nodeIdxLabelMap;
	}
	
	public void getSuperPrunedLists(Graph graph, Query qry)
	{
//		System.out.println("called super nodes pruned lists");
		HashMap<Integer, HashMap<Integer,myNode>> pruned= new HashMap<Integer, HashMap<Integer,myNode>>();// QueryID -> NodeID->NODE

		ArrayList<ConnectedComponent> cls= qry.getConnectedLabels();
				
		//refine according to nodeLabels
		for (int i = 0; i < qry.getListGraph().getNodeCount(); i++) 
		{
			int label= qry.getListGraph().getNodeLabel(i);
			pruned.put(i, (HashMap<Integer,myNode>)graph.getFreqNodesByLabel().get(label).clone());
			nodeIdxLabelMap.put(i,label);
		}
		
		//refine according to degree !!
		HashMap<Integer, HashMap<Integer, Integer>> nodeOutLabelDegrees= new HashMap<Integer, HashMap<Integer,Integer>>();//nodeID-->(Label,Degree)
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
		
		for (int i = 0; i < qry.getListGraph().getNodeCount(); i++) 
		{
			HashMap<Integer, Integer> degreeOutCons= nodeOutLabelDegrees.get(i);
			HashMap<Integer, Integer> degreeInCons= nodeInLabelDegrees.get(i);
			
			HashMap<Integer,myNode> candidates=pruned.get(i);
			boolean isValidNode=true;
			
			for (Iterator<Entry<Integer, myNode>> it = candidates.entrySet().iterator(); it.hasNext();)
			{
				Entry<Integer, myNode> nodeEntry=it.next();
				myNode node=nodeEntry.getValue();
				isValidNode=true;
				if(degreeOutCons!=null)
				for (Iterator<Entry<Integer, Integer>> iterator = degreeOutCons.entrySet().iterator(); iterator.hasNext();) 
				{
					Entry<Integer, Integer> entry =  iterator.next();
					int label=entry.getKey();
					int degree=entry.getValue();
					
					if(node.getOutDegree(label)<degree)
					{isValidNode=false; break;}
				}
				if(isValidNode && degreeInCons!=null)
				{
					for (Iterator<Entry<Integer, Integer>> iterator = degreeInCons.entrySet().iterator(); iterator.hasNext();) 
					{
						Entry<Integer, Integer> entry =  iterator.next();
						int label=entry.getKey();
						int degree=entry.getValue();
						
						if(node.getinDegree(label)<degree)
						{isValidNode=false; break;}
					}
				}
				if(isValidNode==false)
					it.remove();
			}
		}
		
		//add by basic join
		reTables = new RETables[qry.getListGraph().getNodeCount()];
		for(int i=0;i<qry.getListGraph().getNodeCount();i++) {
			int label = qry.getListGraph().getNodeLabel(i);
			reTables[i] = new RETables(i,label,pruned.get(i));
		}
		
	}
}
  
