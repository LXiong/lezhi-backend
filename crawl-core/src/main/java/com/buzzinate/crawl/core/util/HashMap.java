package com.buzzinate.crawl.core.util;

import java.util.Map;

/*
 * An implementation of Phil Bagwell's Hash Array Mapped Trie

 Uses path copying for persistence
 HashCollision leaves vs. extended hashing
 Node polymorphism vs. conditionals
 No sub-tree pools or root-resizing
 Any errors are my own
 */

public class HashMap {
	private int count;
	private INode root;
	private boolean hasNull;
	private Object nullValue;

	final public static HashMap EMPTY = new HashMap(0, null, false, null);
	final private static Object NOT_FOUND = new Object();

	public HashMap() {
		this(0, null, false, null);
	}

	public HashMap(int count, INode root, boolean hasNull, Object nullValue) {
		this.count = count;
		this.root = root;
		this.hasNull = hasNull;
		this.nullValue = nullValue;
	}

	public boolean containsKey(Object key) {
		if (key == null) return hasNull;
		return (root != null) ? root.find(0, key.hashCode(), key, NOT_FOUND) != NOT_FOUND: false;
	}
	
	private static class EntryImpl implements Map.Entry {
		private Object key;
		private Object value;
		
		public EntryImpl(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			Object ov = this.value;
			this.value = value;
			return ov;
		}
	}
	
	public static class Box{
		public Object val;

		public Box(Object val){
			this.val = val;
		}
	}

	public Map.Entry entryAt(Object key) {
		if (key == null)
			return hasNull ? new EntryImpl(null, nullValue) : null;
		return (root != null) ? root.find(0, key.hashCode(), key) : null;
	}

	public HashMap assoc(Object key, Object val) {
		if (key == null) {
			if (hasNull && val == nullValue) return this;
			return new HashMap(hasNull ? count : count + 1, root, true, val);
		}
		Box addedLeaf = new Box(null);
		INode newroot = (root == null ? BitmapIndexedNode.EMPTY : root).assoc(0, key.hashCode(), key, val, addedLeaf);
		if (newroot == root) return this;
		return new HashMap(addedLeaf.val == null ? count: count + 1, newroot, hasNull, nullValue);
	}

	public Object valAt(Object key, Object notFound) {
		if (key == null)
			return hasNull ? nullValue : notFound;
		return root != null ? root.find(0, key.hashCode(), key, notFound): notFound;
	}

	public Object valAt(Object key) {
		return valAt(key, null);
	}

	public HashMap assocEx(Object key, Object val) throws Exception {
		if (containsKey(key)) throw new Exception("Key already present");
		return assoc(key, val);
	}

	public HashMap without(Object key) {
		if (key == null) return hasNull ? new HashMap(count - 1, root, false, null) : this;
		if (root == null) return this;
		INode newroot = root.without(0, key.hashCode(), key);
		if (newroot == root) return this;
		return new HashMap(count - 1, newroot, hasNull, nullValue);
	}

	public int count() {
		return count;
	}

	static int mask(int hash, int shift) {
		// return ((hash << shift) >>> 27);// & 0x01f;
		return (hash >>> shift) & 0x01f;
	}
	
	static interface INode {
		INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf);
		INode without(int shift, int hash, Object key);
		Map.Entry find(int shift, int hash, Object key);
		Object find(int shift, int hash, Object key, Object notFound);
	}
	
	final static class ArrayNode implements INode{
		int count;
		final INode[] array;

		ArrayNode(int count, INode[] array){
			this.array = array;
			this.count = count;
		}

		public INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf){
			int idx = mask(hash, shift);
			INode node = array[idx];
			if(node == null)
				return new ArrayNode(count + 1, cloneAndSet(array, idx, BitmapIndexedNode.EMPTY.assoc(shift + 5, hash, key, val, addedLeaf)));			
			INode n = node.assoc(shift + 5, hash, key, val, addedLeaf);
			if(n == node)
				return this;
			return new ArrayNode(count, cloneAndSet(array, idx, n));
		}

		public INode without(int shift, int hash, Object key){
			int idx = mask(hash, shift);
			INode node = array[idx];
			if(node == null)
				return this;
			INode n = node.without(shift + 5, hash, key);
			if(n == node)
				return this;
			if (n == null) {
				if (count <= 8) // shrink
					return pack(idx);
				return new ArrayNode(count - 1, cloneAndSet(array, idx, n));
			} else 
				return new ArrayNode(count, cloneAndSet(array, idx, n));
		}

		public Map.Entry find(int shift, int hash, Object key){
			int idx = mask(hash, shift);
			INode node = array[idx];
			if(node == null)
				return null;
			return node.find(shift + 5, hash, key); 
		}

		public Object find(int shift, int hash, Object key, Object notFound){
			int idx = mask(hash, shift);
			INode node = array[idx];
			if(node == null)
				return notFound;
			return node.find(shift + 5, hash, key, notFound); 
		}

		private INode pack(int idx) {
			Object[] newArray = new Object[2*(count - 1)];
			int j = 1;
			int bitmap = 0;
			for(int i = 0; i < idx; i++)
				if (array[i] != null) {
					newArray[j] = array[i];
					bitmap |= 1 << i;
					j += 2;
				}
			for(int i = idx + 1; i < array.length; i++)
				if (array[i] != null) {
					newArray[j] = array[i];
					bitmap |= 1 << i;
					j += 2;
				}
			return new BitmapIndexedNode(bitmap, newArray);
		}
	}

	final static class BitmapIndexedNode implements INode {
		static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(0, new Object[0]);

		int bitmap;
		Object[] array;

		final int index(int bit) {
			return Integer.bitCount(bitmap & (bit - 1));
		}

		BitmapIndexedNode(int bitmap, Object[] array) {
			this.bitmap = bitmap;
			this.array = array;
		}

		public INode assoc(int shift, int hash, Object key, Object val,
				Box addedLeaf) {
			int bit = bitpos(hash, shift);
			int idx = index(bit);
			if ((bitmap & bit) != 0) {
				Object keyOrNull = array[2 * idx];
				Object valOrNode = array[2 * idx + 1];
				if (keyOrNull == null) {
					INode n = ((INode) valOrNode).assoc(shift + 5, hash, key,
							val, addedLeaf);
					if (n == valOrNode)
						return this;
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2 * idx + 1, n));
				}
				if (key.equals(keyOrNull)) {
					if (val == valOrNode)
						return this;
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2 * idx + 1, val));
				}
				addedLeaf.val = addedLeaf;
				return new BitmapIndexedNode(bitmap, cloneAndSet(
						array,
						2 * idx,
						null,
						2 * idx + 1,
						createNode(shift + 5, keyOrNull, valOrNode, hash, key,val)));
			} else {
				int n = Integer.bitCount(bitmap);
				if (n >= 16) {
					INode[] nodes = new INode[32];
					int jdx = mask(hash, shift);
					nodes[jdx] = EMPTY.assoc(shift + 5, hash, key, val,
							addedLeaf);
					int j = 0;
					for (int i = 0; i < 32; i++)
						if (((bitmap >>> i) & 1) != 0) {
							if (array[j] == null)
								nodes[i] = (INode) array[j + 1];
							else
								nodes[i] = EMPTY.assoc(shift + 5,
										array[j].hashCode(), array[j],
										array[j + 1], addedLeaf);
							j += 2;
						}
					return new ArrayNode(n + 1, nodes);
				} else {
					Object[] newArray = new Object[2 * (n + 1)];
					System.arraycopy(array, 0, newArray, 0, 2 * idx);
					newArray[2 * idx] = key;
					addedLeaf.val = addedLeaf;
					newArray[2 * idx + 1] = val;
					System.arraycopy(array, 2 * idx, newArray, 2 * (idx + 1),
							2 * (n - idx));
					return new BitmapIndexedNode(bitmap | bit, newArray);
				}
			}
		}

		public INode without(int shift, int hash, Object key) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0)
				return this;
			int idx = index(bit);
			Object keyOrNull = array[2 * idx];
			Object valOrNode = array[2 * idx + 1];
			if (keyOrNull == null) {
				INode n = ((INode) valOrNode).without(shift + 5, hash, key);
				if (n == valOrNode)
					return this;
				if (n != null)
					return new BitmapIndexedNode(bitmap, cloneAndSet(array, 2 * idx + 1, n));
				if (bitmap == bit)
					return null;
				return new BitmapIndexedNode(bitmap ^ bit, removePair(array, idx));
			}
			if (key.equals(keyOrNull))
				// TODO: collapse
				return new BitmapIndexedNode(bitmap ^ bit, removePair(array, idx));
			return this;
		}

		public Map.Entry find(int shift, int hash, Object key) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0)
				return null;
			int idx = index(bit);
			Object keyOrNull = array[2 * idx];
			Object valOrNode = array[2 * idx + 1];
			if (keyOrNull == null)
				return ((INode) valOrNode).find(shift + 5, hash, key);
			if (key.equals(keyOrNull))
				return new HashMap.EntryImpl(keyOrNull, valOrNode);
			return null;
		}

		public Object find(int shift, int hash, Object key, Object notFound) {
			int bit = bitpos(hash, shift);
			if ((bitmap & bit) == 0)
				return notFound;
			int idx = index(bit);
			Object keyOrNull = array[2 * idx];
			Object valOrNode = array[2 * idx + 1];
			if (keyOrNull == null)
				return ((INode) valOrNode).find(shift + 5, hash, key, notFound);
			if (key.equals(keyOrNull))
				return valOrNode;
			return notFound;
		}
	}

	final static class HashCollisionNode implements INode {

		final int hash;
		int count;
		Object[] array;

		HashCollisionNode(int hash, int count, Object... array) {
			this.hash = hash;
			this.count = count;
			this.array = array;
		}

		public INode assoc(int shift, int hash, Object key, Object val, Box addedLeaf) {
			if (hash == this.hash) {
				int idx = findIndex(key);
				if (idx != -1) {
					if (array[idx + 1] == val)
						return this;
					return new HashCollisionNode(hash, count, cloneAndSet(array, idx + 1, val));
				}
				Object[] newArray = new Object[array.length + 2];
				System.arraycopy(array, 0, newArray, 0, array.length);
				newArray[array.length] = key;
				newArray[array.length + 1] = val;
				addedLeaf.val = addedLeaf;
				return new HashCollisionNode(hash, count + 1, newArray);
			}
			// nest it in a bitmap node
			return new BitmapIndexedNode(bitpos(this.hash, shift),
					new Object[] { null, this }).assoc(shift, hash, key, val,
					addedLeaf);
		}

		public INode without(int shift, int hash, Object key) {
			int idx = findIndex(key);
			if (idx == -1)
				return this;
			if (count == 1)
				return null;
			return new HashCollisionNode(hash, count - 1, removePair(
					array, idx / 2));
		}

		public Map.Entry find(int shift, int hash, Object key) {
			int idx = findIndex(key);
			if (idx < 0)
				return null;
			if (key.equals(array[idx]))
				return new EntryImpl(array[idx], array[idx + 1]);
			return null;
		}

		public Object find(int shift, int hash, Object key, Object notFound) {
			int idx = findIndex(key);
			if (idx < 0)
				return notFound;
			if (key.equals(array[idx]))
				return array[idx + 1];
			return notFound;
		}


		public int findIndex(Object key) {
			for (int i = 0; i < 2 * count; i += 2) {
				if (key.equals(array[i]))
					return i;
			}
			return -1;
		}
	}

	
	  public static void main(String[] args) {
		  HashMap m = new HashMap();
		  m = m.assoc("abc", 123);
		  m = m.assoc("bc", 23);
		  System.out.println(m.valAt("abc"));
		  System.out.println(m.valAt("bc"));
		  System.out.println(m.valAt("c"));
	  }
	 

	private static INode[] cloneAndSet(INode[] array, int i, INode a) {
		INode[] clone = array.clone();
		clone[i] = a;
		return clone;
	}

	private static Object[] cloneAndSet(Object[] array, int i, Object a) {
		Object[] clone = array.clone();
		clone[i] = a;
		return clone;
	}

	private static Object[] cloneAndSet(Object[] array, int i, Object a, int j,
			Object b) {
		Object[] clone = array.clone();
		clone[i] = a;
		clone[j] = b;
		return clone;
	}

	private static Object[] removePair(Object[] array, int i) {
		Object[] newArray = new Object[array.length - 2];
		System.arraycopy(array, 0, newArray, 0, 2 * i);
		System.arraycopy(array, 2 * (i + 1), newArray, 2 * i, newArray.length
				- 2 * i);
		return newArray;
	}

	private static INode createNode(int shift, Object key1, Object val1,
			int key2hash, Object key2, Object val2) {
		int key1hash = key1.hashCode();
		if (key1hash == key2hash)
			return new HashCollisionNode(key1hash, 2, new Object[] {
					key1, val1, key2, val2 });
		Box _ = new Box(null);
		return BitmapIndexedNode.EMPTY.assoc(shift, key1hash, key1, val1, _).assoc(shift, key2hash, key2, val2, _);
	}

	private static int bitpos(int hash, int shift) {
		return 1 << mask(hash, shift);
	}
}