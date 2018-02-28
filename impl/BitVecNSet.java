package impl;

import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
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
		checkParameter(other);
		
		// Create the new set called toReturn
		BitVecNSet toReturn = new BitVecNSet(range);

		// First for-loop runs through the bytes in the byte array (internal)
		for (int i = 0; i < internal.length; i++) {
			
			// Second for-loop runs through the bits in each byte of the byte array (internal)
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j; // Create a mask to check each bit of the current byte internal[i]

				// Test case if the number equivalent of the bit is over the range.
				if((j + (8 * i)) >= range())
					break;
				
				// If the bit in the first set (internal) is 1, or the other set contains
				// the number equivalent of the bit, add the number to the new set (toReturn)
				if ((internal[i] & mask) != 0 || other.contains(j + (8 * i))) {
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
		
		// Create the new set called toReturn
		BitVecNSet toReturn = new BitVecNSet(range);

		// First for-loop runs through the bytes in the byte array (internal)
		for (int i = 0; i < internal.length; i++) {
			
			// Second for-loop runs through the bits in each byte of the byte array (internal)
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j; // Create a mask to check each bit of the current byte internal[i]
				
				// Test case if the number equivalent of the bit is over the range.
				if((j + (8 * i)) >= range())
					break;

				// If the bit in the first set (internal) is 1, and the other set contains
				// the number equivalent of the bit, add the number to the new set (toReturn)
				if ((internal[i] & mask) != 0 && other.contains(j + (8 * i))) {
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
		
		// Create the new set called toReturn
		BitVecNSet toReturn = new BitVecNSet(range);

		// First for-loop runs through the bytes in the byte array (internal)
		for (int i = 0; i < internal.length; i++) {
			
			// Second for-loop runs through the bits in each byte of the byte array (internal)
			for (int j = 0; j < 8; j++) {
				int mask = 1 << j; // Create a mask to check each bit of the current byte internal[i]
				
				// Test case if the number equivalent of the bit is over the range.
				if((j + (8 * i)) >= range())
					break;

				// If the bit in the first set (internal) is 1, and the other set doesn't contain
				// the number equivalent of the bit, add the number to the new set (toReturn)
				if ((internal[i] & mask) != 0 && !other.contains(j + (8 * i))) {
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
		
		// If the set is empty, return an empty iterator.
		if(isEmpty())
			return Collections.emptyIterator();
		
		
		
		/* First we must calculate the position of the first set bit */ 
		int j = 0;
		
		// Calculate the position of the first non zero byte in the byte array (internal)
		while (j < internal.length && internal[j] == 0)
			j++;
		
		final int finalJ = j; // starting byte number
		
		// Calculate the position of the first set bit in the byte internal[j]
		int i = 0; 
		while (i < 8 && ((internal[j] & (1 << i)) == 0)) 
			i++;
		
		final int finalI = i; // starting bit number in the byte finalJ
		

		// Now that we know the index of the first bit, we can make the iterator.
		return new Iterator<Integer>() {
			
			int byt = finalJ; // current byte
			int bit = finalI; // current bit
			
			// To see if there is a next, just see if the current byte number is greater
			// greater then the byte array.
			public boolean hasNext() {
				return byt < internal.length;
			}

			
			public Integer next() {
			
				// set the return value to the number value of the last known
				// set bit in the bit vector.
				int toReturn = bit + (8 * byt); 
				
				bit++; // increment the bit number so we can check if the next bit in line is set.
				
				/* make sure there is a next */
				if (!hasNext())
					return null;

				// while the by (byte array index) is less then the length of the byte array(internal)
				while (byt < internal.length) {
					
					// set i at the position of the next bit to check. These bits only range from
					// 0-7 because at bit 8 we begin a new byte. 
					for (int i = bit; i < 8; i++) {
						
						// if the next bit in the internal[by] byte is marked record the position
						// of the bit relative to the byte it is in (variable name is bit).
						// Then return the last known bit which is toReturn.
						if ((internal[byt] & (1 << i)) != 0) {
							bit = i;
							return toReturn;
						}
					}
					byt++; // increment the byte position. (internal[by])
					bit = 0; // reset the bit position to 0 because we entered a new byte.
				}
				return toReturn; // return our last known bit. 
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
	
}
