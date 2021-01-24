/**  
 * Project Name:PatKG_core_JoinOprV2.0  
 * File Name:RETables.java  
 * Package Name:dataStructures  
 * Date:Dec 28, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.dbg.patternmining.models.dataStructures.myNode;

/**  
 * ClassName:RETables   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Dec 28, 2019   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class RETables {
	
	private int label;
	private int ID;  //represents patternNodeID
	private HashMap<Integer, myNode> list;//<nodeID,myNode>

	
	public RETables(int ID, int label, HashMap<Integer, myNode> superNodeMap) {//nodeID, node
		this.label = label;
		this.ID = ID;
		this.list = superNodeMap;
	}
	
	public int getLabel() {
		return label;
	}

	public int getID() {
		return ID;
	}
	public HashMap<Integer, myNode> getList() {
		return list;
	}
	
	
}
  
