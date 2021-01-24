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
import java.util.Iterator;

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
public class RETInstances {
	
	private ArrayList<InstanceTable> instanceList;//(int, node)
	private ArrayList<ConnectedComponents> edgeList;

	public RETInstances() {
		instanceList = new ArrayList<InstanceTable>();
	}
	
	public void joinAssign(InstanceTable ins, int addLabel, int addIdx, int addNode) {//(be added pnodeIdx, add graph node)
		if(!ins.contains(addNode) && ins.getLabel(addIdx) == addLabel) {//has not been joined before
//			System.out.println("*** join ***");
			InstanceTable inscopy = ins.copy();
//			System.out.println("before join:"+inscopy.toString());
//			inscopy.roundJoin = false;
			inscopy.assign(addIdx, addNode);
			instanceList.add(inscopy);
//			System.out.println("after join:"+inscopy.toString());
		}
	}
	
	public void addItem(InstanceTable ins) {
		instanceList.add(ins);
	}
	
	public void addEdge(ConnectedComponents edge) {
		edgeList.add(edge);
	}
	
	public ArrayList<InstanceTable> getInsList(){
		return instanceList;
	}
	
	public int edgeSize() {
		return edgeList.size();
	}
	
	public int getSize() {
		return instanceList.size();
	}

	public void removeCycIvalidIns() {
		Iterator it = instanceList.iterator();
		while(it.hasNext()) {
			InstanceTable ins = (InstanceTable) it.next();
			if(!ins.cycValid()) {
				it.remove();
			}
		} 
	}

}
  
