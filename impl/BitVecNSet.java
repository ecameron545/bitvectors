package impl;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import adt.BadNSetParameterException;
import adt.NSet;

/**
 * BArrayNSet
 * 
 * Implementation of NSet that uses bit vectors to represent the set.
 * 
 * @author Thomas VanDrunen CSCI 345, Wheaton College June 15, 2015
 */
public class BitVecNSet implements NSet {

	/**
	 * The array of bytes, used as a bit vector.
	 */
	private byte[] internal;

	/**
	 * One greater than the largest number than can be stored in this set.
	 */
	private int range;

	/**
	 * Plain constructor
	 * 
	 * @param range
	 *            One greater than the largest number than can be stored in this
	 *            set.
	 */
	public BitVecNSet(int range) {
		this.range = range;
		internal = new byte[range / 8 + 1];
	}

	/**
	 * Check to see if a value could possibly be in this set, and throw an exception
	 * if it is out of range.
	 * 
	 * @param x
	 *            The value in question, interpreted as an index into the array.
	 */
	private void checkIndex(int x) {
		if (x < 0 || x >= range)
			throw new BadNSetParameterException(x + "");
	}

	/**
	 * Make sure the other NSet has the same class and range as this one, throw an
	 * exception otherwise.
	 * 
	 * @param other
	 *            The other NSet, to be checked.
	 */
	private void checkParameter(NSet other) {
		if (!(other instanceof BitVecNSet) || other.range() != range)
			throw new BadNSetParameterException(
					this.getClass() + "," + range + " / " + other.getClass() + "," + other.range());
	}

	/**
	 * Add an item to the set. (No problem if it's already there.)
	 * 
	 * @param item
	 *            The item to add
	 */
	public void add(Integer item) {
		checkIndex(item);
		internal[item / 8] |= 1 << (item % 8);
	}

	/**
	 * Does this set contain the item?
	 * 
	 * @param item
	 *            The item to check
	 * @return True if the item is in the set, false otherwise
	 */
	public boolean contains(Integer item) {
		checkIndex(item);
		
		System.out.println(internal[0]);
		System.out.println(internal[1]);
		System.out.println(internal[2]);

		int index = item / 8; // index in the byte array (which is internal[])

		// to get the mask shift 1 to the left by item % 8.
		// In the byte array, the least significant bit relates to the lowest
		// number in the set. For example in 00000001, the set contains
		// a 0. 10000000 means the set contains a 7.
		int mask = 1 << ((item % 8));

		// check for the bit by &'ing the correct byte and the mask.
		if ((internal[index] & (mask)) != 0)
			return true;
		return false;
	}

	/**
	 * Remove an item from the set, if it's there (ignore otherwise).
	 * 
	 * @param item
	 *            The item to remove
	 */
	public void remove(Integer item) {
		checkIndex(item);

		int index = item / 8; // index in the byte array (which is internal[])
		int mask = 1 << ((item % 8)); // same mask as contains
		int erase = ~(1 << ((item % 8))); // complement of mask to remove the bit

		// if the bit is set, remove it with erase.
		if ((internal[index] & mask) != 0)
			internal[index] = (byte) (internal[index] & erase);

	}

	/**
	 * Is the set empty?
	 * 
	 * @return True if the set is empty, false otherwise.
	 */
	public boolean isEmpty() {

		// Iterate over the byte array and check if the individual bytes are 0. If they
		// all are 0, the bit vector is empty.
		for (int i = 0; i < internal.length; i++) {
			if (internal[i] != 0)
				return false;
		}
		return true;
	}

	/**
	 * The range of this set, that is, one greater than the largest number than can
	 * be stored in this set.
	 * 
	 * @return n such that the elements of this set are drawn from the range [0, n).
	 */
	public int range() {
		return range;
	}

	/**
	 * Compute the complement of of this set.
	 * 
	 * @return A set containing all the elements that aren't in this one and none of
	 *         the elements that are.
	 */
	public NSet complement() {
		BitVecNSet toReturn = new BitVecNSet(range);
		for (int i = 0; i < internal.length - 1; i++)
			// strangely, the negation of a byte is an integer...
			toReturn.internal[i] = (byte) ~(internal[i]);
		byte mask = 0;
		for (int i = 0; i < range % 8; i++) {
			mask <<= 1;
			mask |= 1;
		}
		toReturn.internal[internal.length - 1] = (byte) (mask ^ internal[internal.length - 1]);
		return toReturn;
	}

	/**
	 * Compute the union of this and the given set.
	 * 
	 * @param other
	 *            Another set of the same class and range.
	 * @return A set containing all the elements that are in either this or the
	 *         other set.
	 */
	public NSet union(NSet other) {

		BitVecNSet toReturn = new BitVecNSet(range);

		for (int i = 0; i < internal.length; i++) {
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j;
				if ((internal[i] & mask) == 1 && other.contains(j + (8 * i))) {
					toReturn.add(j + (8 * i));
				}
			}
		}
		return toReturn;
	}

	/**
	 * Compute the intersection of this and the given set.
	 * 
	 * @param other
	 *            Another set of the same class and range.
	 * @return A set containing all the elements that are in both this and the other
	 *         set.
	 */
	public NSet intersection(NSet other) {
		checkParameter(other);
		BitVecNSet toReturn = new BitVecNSet(range);

		for (int i = 0; i < internal.length; i++) {
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j;
				if ((internal[i] & mask) == 1 || other.contains(j + (8 * i))) {
					toReturn.add(j + (8 * i));
				}
			}
		}
		return toReturn;
	}

	/**
	 * Compute the difference between this and the given set.
	 * 
	 * @param other
	 *            Another set of the same class and range.
	 * @return A set containing all the elements that are in this set but not in the
	 *         other set.
	 */
	public NSet difference(NSet other) {
		checkParameter(other);
		BitVecNSet toReturn = new BitVecNSet(range);

		for (int i = 0; i < internal.length; i++) {
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j;
				if ((internal[i] & mask) == 1 && !other.contains(j + (8 * i))) {
					toReturn.add(j + (8 * i));
				}
			}
		}
		return toReturn;
	}

	/**
	 * The number of items in the set
	 * 
	 * @return The number of items.
	 */
	public int size() {
		int size = 0;

		/* Iterate over the byte array and store each byte in the variable current */
		for (int i = 0; i < internal.length; i++) {
			int current = internal[i];

			/*
			 * Now iterate through each bit in current and check if it is set. If the bit is
			 * set, increase size
			 */
			for (int j = 0; j < 8; j++) {
				if ((current & 1) != 0)
					size++;
				current = current >> 1; // shift current right one to move to the next bit
			}
		}
		return size;
	}

	/**
	 * Iterate through this set.
	 */
	public Iterator<Integer> iterator() {

		// calculate the index of the first true position, // if any; that is, the
		// first value the iterator should return
		int j = 0;
		while (j < internal.length && internal[j] == 0)
			j++;
		final int finalJ = j;
		int startByte = internal[j];

		
		int i = 0; 
		while (i < 8 && ((startByte & (1 << i)) == 0)) 
			i++;
		
			
		final int finalI = i;
		final int startBit = startByte & (1 << i);

		
		
		
		
		return new Iterator<Integer>() {
			
			
			
			int by = finalJ; // current byte
			int bi = startBit; // current bit
			int fi = finalI + 1;
			
			
			
			
			public boolean hasNext() {
				return by < internal.length;
			}

			
			
			
			public Integer next() {
			
				int ret = bi; // save old 

				if (!hasNext())
					return null;

				while (by < internal.length) {
					for (int i = fi; i < 8; i++) {
						if ((internal[by] & (1 << i)) != 0) {
							bi = internal[by] & (1 << i);
							fi = i+1;
							return ret;
						}
					}
					by++;
				}
				return ret;
			}
			
			
			
			
			
		};
	}

	public String toString() {
		String toReturn = "[";
		for (int i = 0; i < internal.length - 1; i++)
			for (int j = 0; j < 8; j++)
				if ((internal[i] & (1 << j)) == 0)
					toReturn += " ";
				else
					toReturn += ".";
		for (int j = 0; j < range % 8; j++)
			if ((internal[internal.length - 1] & (1 << j)) == 0)
				toReturn += " ";
			else
				toReturn += ".";
		for (int j = range % 8; j < 8; j++)
			toReturn += "x";
		toReturn += "]";
		return toReturn;
	}
	
	public static void main(String[] args) {
		BitVecNSet bvs = new BitVecNSet(24);
		bvs.add(0);
		bvs.add(13);
		bvs.add(22);
		System.out.println(bvs.toString());
		bvs.contains(3);
		
	}

}
