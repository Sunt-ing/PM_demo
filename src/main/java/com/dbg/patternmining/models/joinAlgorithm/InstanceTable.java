/**  
 * Project Name:PatKG_core_JoinOprV2.0  
 * File Name:InstanceTable.java  
 * Package Name:dataStructures  
 * Date:Dec 28, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.util.HashMap;

/**  
 * ClassName:InstanceTable   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Dec 28, 2019   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class InstanceTable {

	private int[] instances;
	private int qrySize;
	private boolean cycValid;
	private HashMap<Integer,Integer> nodeIdxLabelMap;
	
	public InstanceTable(int size) {
		instances = new int[size];
		this.qrySize = size;
		for (int i = 0; i < instances.length; i++) 
		{
			instances[i]=-1;
		}
		this.cycValid = false;
	}
	
	public InstanceTable(int size, HashMap<Integer,Integer> nodeIdxLabelMap) {
		instances = new int[size];
		this.qrySize = size;
		this.nodeIdxLabelMap = nodeIdxLabelMap;
		for (int i = 0; i < instances.length; i++) 
		{
			instances[i]=-1;
		}
		this.cycValid = false;
	}
	
	public Integer getLabel(Integer i) {
		
		return nodeIdxLabelMap.get(i);
	}
	
	public boolean contains(int nodeIdx) {
		for(int i=0;i<instances.length;i++ ) {
			if(instances[i] != -1) {
				if(instances[i] == nodeIdx) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setCycValid(boolean flag) {
		this.cycValid = flag;
	}
	
	public boolean cycValid() {
		return cycValid;
	}
	
	public InstanceTable copy() {
		InstanceTable inscopy = new InstanceTable(qrySize, nodeIdxLabelMap);
		for(int i=0;i<qrySize;i++) {
			inscopy.getIsoInstances()[i]=instances[i];
		}
		return inscopy;
	}
	
	
	public int insSize() {
		int counter = 0;
		for (int i = 0; i < instances.length; i++) 
		{
			if(instances[i]!=-1) {
				counter ++;
			}
		}
		return counter;
	}
	
	public int qrySize() {
		return qrySize;
	}
	
	public void assign(int pIndex, int node) {//(pattern nodeIdx, node instance)
		instances[pIndex]=node;
	}
	
	public int getIsoInstances(int index) {//return node instance
		return instances[index];
	}
	public int[] getIsoInstances() {
		return instances;
	}
	
	public String toString() {
		String s = "";
		for(int i=0;i<instances.length;i++) {
			s += instances[i] + " ";
		}
		return s;
	}
	
}
  
