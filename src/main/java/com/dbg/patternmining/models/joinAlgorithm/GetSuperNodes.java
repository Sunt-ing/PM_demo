/**  
 * Project Name:Grami_Join_Compare  
 * File Name:GetSuperNodes.java  
 * Package Name:joinAlgorithm  
 * Date:2020年1月18日  
 * Copyright (c) 2020, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.util.HashMap;

import com.dbg.patternmining.models.dataStructures.Graph;
import com.dbg.patternmining.models.dataStructures.Query;
import com.dbg.patternmining.models.pruning.SPpruner;

/**  
 * ClassName:GetSuperNodes   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     2020年1月18日   
 * @author   user  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class GetSuperNodes {
	private static RETables[] reTables;
	private Query qry;
	private static HashMap<Integer,Integer> nodeIdxLabelMap;

	public GetSuperNodes(Graph singleGraph, Query qry) {
		this.qry=qry;		
		SuperNodesPruner sp = new SuperNodesPruner();
		sp.getSuperPrunedLists(singleGraph, qry);
		nodeIdxLabelMap = sp.getNodeIdxLabelMap();
		reTables = sp.getRETables();		
	}
	
	public RETables[] getReTables() {
		return reTables;
	}
	
	public HashMap<Integer,Integer> getNodeIdxLabelMap() {
		return nodeIdxLabelMap;
	}
}
  
