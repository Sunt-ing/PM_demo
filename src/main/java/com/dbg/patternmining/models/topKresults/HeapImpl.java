package com.dbg.patternmining.models.topKresults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.dbg.patternmining.models.dataStructures.myNode;

/**
 * Create by zxb on 2017/9/10
 */
public class HeapImpl<T extends Comparable<T>> {
 
    private List<T> heap;
 
    private List<T> orginList;
 
	public void initOriginList(List<T> orginList) {
        this.orginList = orginList;
    }
 
	public List<T> getHeap() {
        return heap;
    }
 
    public HeapImpl() {
		List<HashMap<Integer, HashMap<Integer,myNode>>> heap = new ArrayList<HashMap<Integer, HashMap<Integer,myNode>>>();

        this.heap = new ArrayList<>();
    }
 
    /**
     * 插入对应上浮
     *
     * @param start
     */
    protected void adjustUp(int start) {
        int currentIndex = start;
        int parentIndex = (currentIndex - 1) / 2;
        T tmp = heap.get(currentIndex);
        while (currentIndex > 0) {
            int cmp = heap.get(parentIndex).compareTo(tmp);
            if (cmp >= 0) {
                break;
            } else {
                heap.set(currentIndex, heap.get(parentIndex));
                currentIndex = parentIndex;
                parentIndex = (parentIndex - 1) / 2;
            }
        }
        heap.set(currentIndex, tmp);
    }
 
    public void insert(T data) {
        int size = heap.size();
        heap.add(data);    // 将"数组"插在表尾
        adjustUp(size);        // 向上调整堆
    }
 
    public void remove(int index) {
        int size = heap.size();
        heap.set(index, heap.get(size - 1));
        heap.remove(size - 1);
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
        T tmp = heap.get(currentIndex);
        int size = heap.size();
        while (leftChildIndex < size) {
            T left = null;
            T right = null;
            if (leftChildIndex < size) {
                left = heap.get(leftChildIndex);
            }
            if (rightChildIndex < size) {
                right = heap.get(rightChildIndex);
            }
            int maxIndex = right == null ? leftChildIndex : (left.compareTo(right) >= 0 ? leftChildIndex : rightChildIndex);
            T max = heap.get(maxIndex);
            if(tmp.compareTo(max)>=0){
                break;
            }else{
                heap.set(currentIndex, max);
                heap.set(maxIndex, tmp);
                leftChildIndex = maxIndex * 2 +1;
                rightChildIndex = maxIndex +1;
            }
        }
    }
 
	public void makeHeap(int first, int last) {
        for (int i = first; i < last; i++) {
        System.out.println("insert:"+orginList.get(i));    
        	insert(orginList.get(i));
        }
    }
 
	public void popHeap(int first, int last) {
        remove(first);
    }
 
	public void pushHeap(int first, int last) {
        adjustUp(last - 1);
    }
 
	public void display() {
        System.out.println(heap);
    }
    
 
    public static void main(String[] args) {
     	HeapImpl<Integer> heap = new HeapImpl();
        heap.initOriginList(Arrays.asList(931,5992,15552,1875,9649,1679,13,217,50,1,6132,10,47));
        System.out.println("初始构建堆：");
        heap.makeHeap(0, 13);
        heap.display();
        System.out.println("弹出堆顶：");
        heap.popHeap(0, 5);
        heap.display();
        System.out.println("弹出堆顶：");
        heap.popHeap(0, 4);
        heap.display();
        System.out.println("插入堆尾：");
        heap.getHeap().add(90);
        heap.display();
        System.out.println("重新排列：");
        heap.pushHeap(0, 4);
        heap.display();
    }

}
