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
				
				count++;
			}
			in.close();
			hitRate = (double)hits / count;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
		return hitRate;
	}
}

class Cache {
	private int wordsPerLine;
	private int setCount;
	private int lines;
	private int linesPerSet;
	private Set[] sets;
	
	private int iShift;
	private int tShift;
	
	public Cache(int bytes, int totalLines, int lPS) {
		wordsPerLine = bytes/4;
		linesPerSet = lPS;
		
		setCount = totalLines / linesPerSet;
		sets = new Set[setCount];
		lines = totalLines;
		
		// word addresses, so we need bits for displacement within a line, a set index, and for a tag
		// to get the index, we need to right-shift until the displacement bits are gone, and the mod by the number of sets
		double index = Math.log10(wordsPerLine) / Math.log10(2.0);
		iShift = (int) index;
		
		// to get the tag, we need to right-shift until both the index and displacement bits are gone
		double tag = Math.log10(setCount) / Math.log10(2.0);
		tShift = (int) (tag+index);
		
		for (int i = 0; i < setCount; i++) {
			sets[i] = new Set(linesPerSet, wordsPerLine);
		}
	}
	
	// given an address, determine if that address is present in the cache,
	// if so, return a 1, indicating a hit, otherwise return 0, and insert
	// the address into the cache
	public int process(int instruction, int address) {
		int hit = 0;
		// System.out.println(address);
		
		int offset = address % wordsPerLine;
		// System.out.println("offset = address % wordsPerLine = " + address + " % " + wordsPerLine + " = " + offset);
		
		// get index value
		int index = address >> iShift;
		index = index % setCount;
		// System.out.println("index = address >> iShift % setCount = " + address + " >> " + iShift + " % " + setCount + " = " + index);
		
		// get tag value
		int tag = address >> tShift;
		// System.out.println("tag = address >> tShift = " + address + " >> " + tShift + " = " + tag);
		
		if (sets[index].isPresent(tag)) {
			// System.out.println(tag + " was present in set[" + index + "]");
			hit = 1;
		}
		sets[index].insertAtTop(tag);
		// System.out.println();
		
		return hit;
	}
}

class Set
{
	private int lines;
	private int words;
	private Line[] array;
	
	public Set(int l, int w) {
		lines = l;
		words = w;
		array = new Line[lines];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = new Line();
		}
	}
	
	// return true if the tag can be found in one of the set lines
	public boolean isPresent(int tag) {
		boolean search = false;
		
		for (int i = 0; i < lines && !search; i++) {
			if (array[i].getTag() == tag) {
				search = true;
			}
		}
		
		return search;
	}
	
	// tag is not present in any lines, so it will be placed at the top
	// and then the other similar items will be pushed down until a -1
	// is found
	public void insertAtTop(int tag) {
		int old = tag;
		
		/*
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i].getTag() + " ");
		}
		System.out.println();
		*/
		
		for (int i = 0; i < array.length; i++) {
			int temp = array[i].getTag();
			array[i].setTag(old);
			
			if (temp == -1) {
				break;
			} else {
				old = temp;
			}
		}
		
		/*
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i].getTag() + " ");
		}
		System.out.println();
		*/
	}
}

class Line {
	private int tag;
	
	public Line() {
		tag = -1;
	}
	
	public boolean isAvailable() {
		boolean available = tag == Integer.MIN_VALUE;
		
		return available;
	}
	
	public int getTag() { return tag;}
	public void setTag(int t) {
		tag = t;
	}
}
