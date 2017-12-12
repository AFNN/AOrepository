package memory;

import java.sql.Time;
import java.util.ArrayList;

	import util.Utils;

	//
	// CACHE ENTRY
	
	class AssociativeEntry {
	  int value;        // the line's word (1 cache-line = 1 word)
	  int address;          // the line's address
	  boolean isValid;  // the validity bit
	  boolean isdirty=false ; // dirty bit. Il signale que l�entr�e contient une donn�e dont la valeur est plus r�cente en cache qu�en m�moire
	  
	}
	

	public class AssociativeCache implements Memory {

	  //
	  // ATTRIBUTES
	  //
	  
	  private final ArrayList<AssociativeEntry> entries;	
	  private final int accessTime;
	  private final Memory memory;

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
	    

	    this.entries = new ArrayList<>(size);
	    for (int i = 0; i < size; i++) {
	      entries.add(new AssociativeEntry());
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
	   return readFIFO(address);
	  }

	  @Override
	  public void write(int address, int value) {
	    
	  }
	  
	  
	  public int readFIFO(int address) {
		  AssociativeEntry entry = entries.get(address);
		    if (entry.isValid && entry.address == address) {
		    	// hit
		    	operationTime = accessTime;
		    	stats.reads.add(true, operationTime);
		    } 
		    else {
		    	// miss	    	
		    	if(entry.isdirty) {	
		    		memory.write(entry.address,entry.value);
		    		entry.isdirty=false;
		    	}	
		    	entry.value = memory.read(address);
		    	entry.address = address;
		    	entry.isValid = true;
		    	//rajoute l'objet entry a la derniere ligne 
		    	entries.remove(0);
		    	entries.add(entry);
		    	operationTime = memory.getOperationTime() + accessTime;
		    	stats.reads.add(false, operationTime);
		    }
		    return entry.value;
	  }
	  
	  
	  
	  public void writeFIFO(int address, int value) {
		  
		  AssociativeEntry entry= getEntries(address);
		  
		  if(entry==null) {
			  AssociativeEntry entryStc =entries.get(0);
			  memory.write(entryStc.address,entryStc.value);
			  entries.remove(0);
			  
		  }
		  else {			  
			  entry.value=value;
			  entry.isdirty=true;
			  entry.isValid=true;
			  entry.address=address;
		  }
	  }
	  public void readLRU(int a , int b ) {
		  
		  
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
	  
	private AssociativeEntry getEntries(int address ) {
		AssociativeEntry entrie=new AssociativeEntry();
		for(AssociativeEntry en:entries){
			if(en.address==address && en.isValid==true) {
				entrie.address=en.address;
				entrie.isValid=en.isValid;
				entrie.isdirty=en.isdirty;
				entrie.value=en.value;
			}
			else {
				entrie=null;
				
			}
		}
		
		return  entrie;
	}
	  
	 
	}
