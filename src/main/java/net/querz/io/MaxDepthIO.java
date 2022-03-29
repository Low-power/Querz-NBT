package net.querz.io;

public abstract class MaxDepthIO {

	public int decrementMaxDepth(int maxDepth) {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("negative maximum depth is not allowed");
		} else if (maxDepth == 0) {
			throw new MaxDepthReachedException("reached maximum depth of NBT structure");
		}
		return --maxDepth;
	}
}
