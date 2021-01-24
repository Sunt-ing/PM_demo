package com.dbg.patternmining.models.topKresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.dbg.patternmining.models.dataStructures.myNode;

public class MaxHeapNodes {
	private List<HashMap<Integer, HashMap<Integer,myNode>>> heap;//Graph.nodesByLabel
	 
    private List<HashMap<Integer, HashMap<Integer,myNode>>> orginList;
 
    public void initOriginList(List<HashMap<Integer, HashMap<Integer,myNode>>> orginList) {
        this.orginList = orginList;
    }
 
	public List<HashMap<Integer, HashMap<Integer,myNode>>> getHeap() {
        return heap;
    }
	
	public Integer getSize(){
		return heap.size();
	}
	
	public MaxHeapNodes(){
//		initOriginList(List<T> orginList);
		List<HashMap<Integer, HashMap<Integer,myNode>>> heap = new ArrayList<HashMap<Integer, HashMap<Integer,myNode>>>();
		this.heap = heap;
	}
 
    /**
     * 插入对应上浮
     *
     * @param start
     */
    protected void adjustUp(int start) {
        int currentIndex = start;
        int parentIndex = (currentIndex - 1) / 2;
        
        HashMap<Integer, HashMap<Integer,myNode>> currentIndexMap = heap.get(currentIndex);
		OUT:
		while (currentIndex > 0) {
			HashMap<Integer, HashMap<Integer,myNode>> parentIndexMap = heap.get(parentIndex);
		for (Entry<Integer, HashMap<Integer,myNode>> currentIndexentry : currentIndexMap.entrySet()) {
			for (Entry<Integer, HashMap<Integer,myNode>> parentIndexentry : parentIndexMap.entrySet()) {
//		            int cmp = parentIndexentry.getValue().size().compareTo(currentIndexentry.getValue().size());
		            if (parentIndexentry.getValue().size() >= (currentIndexentry.getValue().size())) {
		                break OUT;
		            } else {
		                heap.set(currentIndex, heap.get(parentIndex));
		                currentIndex = parentIndex;
		                parentIndex = (parentIndex - 1) / 2;
		            }
		        }
			}
		}
        heap.set(currentIndex, currentIndexMap);
    }
 
    public void insert(HashMap<Integer, HashMap<Integer,myNode>> data) {
        int size = heap.size();
        heap.add(data);    // 将"数组"插在表尾
        adjustUp(size);        // 向上调整堆
    }
 
    public void remove(int index) {
        int size = heap.size();
        heap.set(index, heap.get(size - 1));
        heap.remove(size - 1);
        if(heap.size() == 0){
        	return;
        }
        adjustDown(index);
    }
 
    /**
     * 删除对应下沉
     *
     * @param index
     */
	private void adjustDown(int index) {
		int currentIndex = index;
		int leftChildIndex = index * 2 + 1;
		int rightChildIndex = index * 2 + 2;
		int size = heap.size();
		OUT:
		while (leftChildIndex < size) {
			HashMap<Integer, HashMap<Integer,myNode>> currentIndexMap = heap.get(currentIndex);

			HashMap<Integer, HashMap<Integer,myNode>> left = null;
			HashMap<Integer, HashMap<Integer,myNode>> right = null;
			if (leftChildIndex < size) {
				left = heap.get(leftChildIndex);
			}
			if (rightChildIndex < size) {
				right = heap.get(rightChildIndex);
			}

			for (Entry<Integer, HashMap<Integer,myNode>> entryl : left.entrySet()) {
				for (Entry<Integer, HashMap<Integer,myNode>> entryt : currentIndexMap.entrySet()) {
					int maxIndex = 0;
					if(right != null){
						for (Entry<Integer, HashMap<Integer,myNode>> entryr : right.entrySet()) {
//							maxIndex = (entryl.getValue().compareTo(entryr.getValue()) >= 0 ? leftChildIndex: rightChildIndex);
							maxIndex = (entryl.getValue().size() >= (entryr.getValue().size()) ? leftChildIndex: rightChildIndex);

						}
						
					}else{
						maxIndex = leftChildIndex;
					}
//						int maxIndex = right == null ? leftChildIndex
//								: (entryl.getValue().compareTo(entryr.getValue()) >= 0 ? leftChildIndex
//										: rightChildIndex);
					HashMap<Integer, HashMap<Integer,myNode>> max = heap.get(maxIndex);
						for (Entry<Integer, HashMap<Integer,myNode>> entryMax : max.entrySet()) {

							if (entryt.getValue().size()  >= (entryMax.getValue().size())) {
								break OUT;
							} else {
								heap.set(currentIndex, max);
								heap.set(maxIndex, currentIndexMap);
								currentIndex = maxIndex;//add
								leftChildIndex = maxIndex * 2 + 1;
								rightChildIndex = leftChildIndex + 1;//leftChildIndex+1
							}
						}
					}

				}

		}
	}
 
	public void makeHeap(int first, int last) {
        for (int i = first; i < last; i++) {
            insert(orginList.get(i));
        }
    }
	public HashMap<Integer, HashMap<Integer,myNode>> popHeap(int first) {
		HashMap<Integer, HashMap<Integer,myNode>> reMap = heap.get(0);//！！！！！！！！
		remove(first);
        return reMap;
    }
 
	public void pushHeap(int first, int last) {
        adjustUp(last - 1);
    }
 
	public void display() {
        System.out.println(heap);
    }

}
