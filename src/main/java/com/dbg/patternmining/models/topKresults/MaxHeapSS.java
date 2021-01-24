package com.dbg.patternmining.models.topKresults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.dbg.patternmining.models.search.SearchLatticeNode;

public class MaxHeapSS<NodeType, EdgeType> {

	private ArrayList<HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer>> data;

	public MaxHeapSS(int capacity) {
		data = new ArrayList<>(capacity);
	}

	public MaxHeapSS() {
		data = new ArrayList<>();
	}

	// 返回堆中元素的个数
	public int getSize() {
		return data.size();
	}

	// 判断堆是否为空
	public boolean isEmpty() {
		return data.isEmpty();
	}

	// 返回完全二叉树中索引为index的节点的父节点的索引
	private int parent(int index) {
		if (index == 0)
			throw new IllegalArgumentException("index-0 doesn't have parent");

		return (index - 1) / 2;
	}

	// 返回完全二叉树中索引为index的节点的左孩子的索引
	private int leftChild(int index) {
		return index * 2 + 1;
	}

	// 返回完全二叉树中索引为index的节点的右孩子的索引
	private int rightChild(int index) {
		return index * 2 + 2;
	}

	// 交换索引为i、j的值
	private void swap(int i, int j) {
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> t = data.get(i);
		data.set(i, data.get(j));
		data.set(j, t);
	}

	// 向堆中添加元素
	public void add(HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> e) {
		data.add(e);
		siftUp(data.size() - 1);
	}

	private void siftUp(int k) {
		// k不能是根节点，并且k的值要比父节点的值大
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> parentKMap = data.get(parent(k));
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> kMap = data.get(k);
		for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entry1 : parentKMap.entrySet()) {
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entry2 : kMap.entrySet()) {

				while (k > 0 && entry1.getValue().compareTo(entry2.getValue()) < 0) {
					swap(k, parent(k));
					k = parent(k);
				}
			}
		}

	}

	// 看堆中最大的元素
	public HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> findMax() {
		if (data.size() == 0)
			throw new IllegalArgumentException("Can not findMax when heap is empty");
		return data.get(0);
	}

	// 取出堆中最大的元素
	public HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> extractMax() {
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> ret = findMax();

		swap(0, data.size() - 1);
		data.remove(data.size() - 1);
		siftDown(0);

		return ret;
	}

	private void siftDown(int k) {
		// leftChild存在
		while (leftChild(k) < data.size()) {
			int j = leftChild(k);
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> jMap = data.get(j);
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> kMap = data.get(j+1);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entry1 : jMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entry2 : kMap.entrySet()) {
					// rightChild存在,且值大于leftChild的值
					if (j + 1 < data.size() && entry1.getValue().compareTo(entry2.getValue()) < 0)
						j = rightChild(k);
					
				}
			}
			// data[j]是leftChild和rightChild中最大的
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> kkMap = data.get(k);
			for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entryj : jMap.entrySet()) {
				for (Entry<SearchLatticeNode<NodeType, EdgeType>, Integer> entryk : kkMap.entrySet()) {
					
					if (entryk.getValue().compareTo(entryj.getValue()) >= 0)
						break;
				}
			}

			swap(k, j);
			k = j;
		}

	}

	// 取出堆中最大的元素,替换为元素e
	public HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> replace(
			HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> e) {
		HashMap<SearchLatticeNode<NodeType, EdgeType>, Integer> ret = findMax();

		data.set(0, e);
		siftDown(0);

		return ret;
	}

}
