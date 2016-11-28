import java.io.*;
import java.lang.Math;

public class A4Q2SmithIan7655381 {
	public static void main(String[] args) {
		int bytes = 8;
		int lines = 16384;
		int linesPerSet = 2;
		Cache c = new Cache(bytes, lines, linesPerSet);
		double hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 32;
		lines = 4096;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 8;
		lines = 32768;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 32;
		lines = 8192;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 8;
		lines = 16384;
		linesPerSet = 4;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 32;
		lines = 4096;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 8;
		lines = 32768;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
		
		bytes = 32;
		lines = 8192;
		c = new Cache(bytes, lines, linesPerSet);
		hits = process(c);
		System.out.println("With " + bytes + " bytes and " + lines + " lines, there was a hitrate of: " + hits);
	}
	
	public static double process(Cache c) {
		double hitRate = 0;
		
		try	{
			BufferedReader in = new BufferedReader(new FileReader("trace.txt"));
			
			int count = 0;
			int hits = 0;
			String line;
			
			while ((line = in.readLine()) != null) {
				String[] split = line.split(" ");
				int instruction = Integer.parseInt(split[0]);
				int address = Integer.parseInt(split[1], 16);
				int hit = c.process(instruction, address);
				//System.out.println("Hit = " + hit);
				hits += hit;
				
				if (count % 100000 == 0) {
					System.out.println("Iteration #" + count);
				}
				
				count++;
			}
			in.close();
			System.out.println(hits);
			hitRate = (double)hits / count;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
		return hitRate;
	}
}

class Cache {
	private int words;
	private int lines;
	private int linesPerSet;
	private Set[] sets;
	
	private int iShift;
	private int tShift;
	
	public Cache(int bytes, int totalLines, int lPS) {
		words = bytes/4;
		linesPerSet = lPS;
		
		int setCount = totalLines / linesPerSet;
		sets = new Set[setCount];
		lines = totalLines;
		
		double index = Math.log10(words) / Math.log10(2.0);
		iShift = (int) index;
		double tag = Math.log10(linesPerSet) / Math.log10(2.0);
		tShift = (int) (tag+index);
		
		for (int i = 0; i < setCount; i++) {
			sets[i] = new Set(linesPerSet, words);
		}
	}
	
	// given an address, determine if that address is present in the cache,
	// if so, return a 1, indicating a hit, otherwise return 0, and insert
	// the address into the cache
	public int process(int instruction, int address) {
		int hit = 0;
		
		int offset = instruction % words;
		// System.out.println(offset);
		
		// get index value
		int index = address >> iShift;
		index = index % linesPerSet;
		// System.out.println(index);
		
		// get tag value
		int tag = address >> tShift;
		// System.out.println(tag);
		
		// first search for the item
		for (int i = 0; i < sets.length; i++) {
			if (sets[i].isPresent(index, offset, tag)) {
				hit = 1;
				moveToTop(index, offset, tag);
				break;
			}
		}
		
		if (hit == 0) {
			insertAtTop(index, offset, tag);
		}

		return hit;
	}
	
	// take this address and move it to the first set, shifting the other items
	// with the same index and offset into lower sets
	// assumes that tag is present somewhere already
	private void moveToTop(int index, int offset, int tag) {
		int old = tag;
		
		for (int i = 0; i < sets.length - 1; i++) {
			int temp = sets[i].replace(index, offset, old);
			
			if (temp == tag) {
				break;
			}
			
			old = temp;
		}
	}
	
	// tag is not present in any sets, so it will be placed at the top
	// and then the other similar items will be pushed down until a -1
	// is found
	private void insertAtTop(int index, int offset, int tag) {
		int old = tag;
		
		for (int i = 0; i < sets.length; i++) {
			int temp = sets[i].replace(index, offset, old);
			
			if (temp == -1) {
				break;
			} else {
				old = temp;
			}
		}
	}
}

class Set
{
	private int lines;
	private int words;
	private int[][] array;
	
	public Set(int l, int w) {
		lines = l;
		words = w;
		array = new int[lines][words];
		
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				array[i][j] = -1;
			}
		}
	}
	
	public boolean isPresent(int index, int offset, int tag) {
		boolean search = false;
		
		if (array[index][offset] == tag) {
			search = true;
		}
		
		return search;
	}
	
	public boolean isAvailable(int index, int offset) {
		boolean available = false;
		
		if (array[index][offset] == -1) {
			available = true;
		}
		
		return available;
	}
	
	public boolean insert(int index, int offset, int tag) {
		boolean success = false;
		
		if (array[index][offset] == -1) {
			array[index][offset] = tag;
			success = true;
		}
		
		return success;
	}
	
	// places a new tag into the array, and returns the old one
	public int replace(int index, int offset, int tag) {
		int temp = array[index][offset];
		
		array[index][offset] = tag;
		
		return temp;
	}
}
