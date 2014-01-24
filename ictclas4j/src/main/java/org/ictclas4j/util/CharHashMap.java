package org.ictclas4j.util;

/**
 * A hash map that uses primitive chars for the key rather than objects.
 * <P>
 * 
 * 
 * @author Justin Couch
 * @version $Revision: 1.1 $
 * @see java.util.HashMap
 */
public class CharHashMap<E> {
	/**
	 * The hash table data.
	 */
	private transient Entry<E> table[];

	/**
	 * The total number of entries in the hash table.
	 */
	private transient int count;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 * 
	 * @serial
	 */
	private int threshold;

	/**
	 * The load factor for the hashtable.
	 * 
	 * @serial
	 */
	private float loadFactor;

	/**
	 * Innerclass that acts as a datastructure to create a new entry in the
	 * table.
	 */
	private static class Entry<E> {
		char key;
		E value;
		Entry<E> next;

		/**
		 * Create a new entry with the given values.
		 * 
		 * @param hash
		 *            The code used to hash the object with
		 * @param key
		 *            The key used to enter this in the table
		 * @param value
		 *            The value for this key
		 * @param next
		 *            A reference to the next entry in the table
		 */
		protected Entry(char key, E value, Entry<E> next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	/**
	 * Constructs a new, empty hashtable with a default capacity and load
	 * factor, which is <tt>20</tt> and <tt>0.75</tt> respectively.
	 */
	public CharHashMap() {
		this(20, 0.75f);
	}

	/**
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * default load factor, which is <tt>0.75</tt>.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hashtable.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero.
	 */
	public CharHashMap(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	/**
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * the specified load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hashtable.
	 * @param loadFactor
	 *            the load factor of the hashtable.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load
	 *             factor is nonpositive.
	 */
	public CharHashMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal Load: " + loadFactor);

		if (initialCapacity == 0)
			initialCapacity = 1;

		this.loadFactor = loadFactor;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

	/**
	 * Returns the number of keys in this hashtable.
	 * 
	 * @return the number of keys in this hashtable.
	 */
	public int size() {
		return count;
	}

	/**
	 * Tests if this hashtable maps no keys to values.
	 * 
	 * @return <code>true</code> if this hashtable maps no keys to values;
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
		return count == 0;
	}

	/**
	 * Returns the value to which the specified key is mapped in this map.
	 * 
	 * @param key
	 *            a key in the hashtable.
	 * @return the value to which the key is mapped in this hashtable;
	 *         <code>null</code> if the key is not mapped to any value in this
	 *         hashtable.
	 * @see #put(char, Object)
	 */
	public E get(char key) {
		int hash = key;
		int index = (hash & 0x7FFFFFFF) % table.length;
		for (Entry<E> e = table[index]; e != null; e = e.next) {
			if (e.key == key) {
				return e.value;
			}
		}
		return null;
	}

	/**
	 * Increases the capacity of and internally reorganizes this hashtable, in
	 * order to accommodate and access its entries more efficiently. This method
	 * is called automatically when the number of keys in the hashtable exceeds
	 * this hashtable's capacity and load factor.
	 */
	protected void rehash() {
		int oldCapacity = table.length;
		Entry<E> oldMap[] = table;

		int newCapacity = oldCapacity * 2 + 1;
		Entry<E> newMap[] = new Entry[newCapacity];

		threshold = (int) (newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity; i-- > 0;) {
			for (Entry<E> old = oldMap[i]; old != null;) {
				Entry<E> e = old;
				old = old.next;

				int hash = e.key;
				int index = (hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	/**
	 * Maps the specified <code>key</code> to the specified <code>value</code>
	 * in this hashtable. The key cannot be <code>null</code>.
	 * <p>
	 * 
	 * The value can be retrieved by calling the <code>get</code> method with a
	 * key that is equal to the original key.
	 * 
	 * @param key
	 *            the hashtable key.
	 * @param value
	 *            the value.
	 * @return the previous value of the specified key in this hashtable, or
	 *         <code>null</code> if it did not have one.
	 * @throws NullPointerException
	 *             if the key is <code>null</code>.
	 * @see #get(char)
	 */
	public E put(char key, E value) {
		// Makes sure the key is not already in the hashtable.
		int hash = key;
		int index = (hash & 0x7FFFFFFF) % table.length;
		for (Entry<E> e = table[index]; e != null; e = e.next) {
			if (e.key == key) {
				E old = e.value;
				e.value = value;
				return old;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			index = (hash & 0x7FFFFFFF) % table.length;
		}

		// Creates the new entry.
		Entry<E> e = new Entry<E>(key, value, table[index]);
		table[index] = e;
		count++;
		return null;
	}
}