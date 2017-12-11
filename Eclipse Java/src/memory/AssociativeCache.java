package memory;

import java.util.ArrayList;

	import util.Utils;

	//
	// CACHE ENTRY
	
	class EntryA {
	  int value;        // the line's word (1 cache-line = 1 word)
	  int tag;          // the line's tag
	  boolean isValid;  // the validity bit
	  boolean isdirty=false ; // dirty bit. Il signale que l’entrée contient une donnée dont la valeur est plus récente en cache qu’en mémoire
	}
	

	public class AssociativeCache implements Memory {

	  //
	  // ATTRIBUTES
	  //
	  
	  private final ArrayList<Entry> entries;	
	  private final int accessTime;
	  private final Memory memory;

	  private final int indexWidth;
	  private final int indexMask;

	  private int operationTime;
	  private final Stats stats;

	  //
	  // CONSTRUCTORS
	  //
	  public AssociativeCache(int size, int accessTime, Memory memory) {
	    if (size <= 0) {
	      throw new IllegalArgumentException("size");
	    }
	    if (accessTime <= 0) {
	      throw new IllegalArgumentException("accessTime");
	    }
	    if (memory == null) {
	      throw new NullPointerException("memory");
	    }
	    indexWidth = Utils.log(size);
	    if (indexWidth == -1) {
	      throw new IllegalArgumentException("size");
	    }
	    this.indexMask = Utils.mask(indexWidth);

	    this.entries = new ArrayList<>(size);
	    for (int i = 0; i < size; i++) {
	      entries.add(new Entry());
	    }
	    this.accessTime = accessTime;
	    this.memory = memory;

	    this.stats = new Stats();
	  }

	  //
	  // Memory INTERFACE
	  //
	  @Override
	  public int read(int address) {
	    Entry entry = entries.get(toIndex(address));
	    if (entry.isValid && entry.tag == toTag(address)) {
	      // hit
	      operationTime = accessTime;
	      stats.reads.add(true, operationTime);
	    } else {
	      // miss
	    if(entry.isdirty) {	
	    	memory.write(toAddress(entry.tag,toIndex(address)),entry.value);
	    	entry.isdirty=false;
	    }	
	      entry.value = memory.read(address);
	      entry.tag = toTag(address);
	      entry.isValid = true;  
	      operationTime = memory.getOperationTime() + accessTime;
	      stats.reads.add(false, operationTime);
	    }
	    return entry.value;
	  }

	  @Override
	  public void write(int address, int value) {
	    
	  }

	@Override
	public int getOperationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Stats getStats() {
		// TODO Auto-generated method stub
		return null;
	}
	
	  //
	  // UTILITIES
	  //
	  private int toIndex(int address) {
	    return address & indexMask;
	  }

	  private int toTag(int address) {
	    return address >> indexWidth;
	  }

	  private int toAddress(int tag, int index) {
	    return (tag << indexWidth) + index;
	  }

	}
