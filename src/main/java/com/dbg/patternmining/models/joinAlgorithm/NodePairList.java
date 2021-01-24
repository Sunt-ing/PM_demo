/**  
 * Project Name:PatKG_core_JoinOprV1.0  
 * File Name:NodePairList.java  
 * Package Name:dataStructures  
 * Date:Dec 27, 2019  
 * Copyright (c) 2019, zengjian29@126.com All Rights Reserved.  
 *  
*/  
  
package com.dbg.patternmining.models.joinAlgorithm;

import java.util.ArrayList;

/**  
 * ClassName:NodePairList   
 * Function: TODO ADD FUNCTION.   
 * Reason:   TODO ADD REASON.   
 * Date:     Dec 27, 2019   
 * @author   JIAN  
 * @version    
 * @since    JDK 1.6  
 * @see        
 */
public class NodePairList {
	
	private int[] pairArr;
	private boolean joinflag;//has been joined in this round
	
	public NodePairList() {
		pairArr = new int[2];
		this.joinflag = false;
	}
	
	public void setJoinFlag(boolean flag) {
		this.joinflag = flag;
	}
	public boolean getJoinFlag() {
		return joinflag;
	}
	

	public void add(Integer e, Integer f) {
		pairArr[e]=f;
	}
	public int get(Integer i) {
		return pairArr[i];
	}
	public String toString() {
		return pairArr.toString();
	}

}
  
