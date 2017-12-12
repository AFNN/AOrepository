package memory;

import java.sql.Time;
import java.util.ArrayList;

import util.Utils;

//
// CACHE ENTRY

class AssociativeEntry {
	int value; // the line's word (1 cache-line = 1 word)
	int address; // the line's address
	boolean isValid; // the validity bit
	boolean isdirty = false; // dirty bit. Il signale que l’entrée contient une donnée dont la valeur est
								// plus récente en cache qu’en mémoire

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
		
		return readLRU(address);
	}

	@Override
	public void write(int address, int value) {
		writeLRU(address, value);
	}

	public int readFIFO(int address) {
		
		AssociativeEntry entry = new AssociativeEntry();
		entry = getEntries(address);

		if (entry != null) {
			// hit
			operationTime = accessTime;
			stats.reads.add(true, operationTime);
		} else {
			// miss

			AssociativeEntry entryStc = entries.get(0);
			if (entryStc.isdirty == true) {
				memory.write(entryStc.address, entryStc.value);
			}

			entryStc.value = memory.read(address);
			entryStc.address = address;
			entryStc.isValid = true;
			// supprimer l'entree la plus agee
			entries.remove(0);
			// rajoute l'objet entry a la derniere ligne
			entries.add(entryStc);
			operationTime = memory.getOperationTime() + accessTime;
			stats.reads.add(false, operationTime);
			entry = entryStc;
		}

		return entry.value;
	}

	public void writeFIFO(int address, int value) {

		AssociativeEntry entry = getEntries(address);

		if (entry == null) {
			AssociativeEntry entryStc = entries.get(0);
			memory.write(entryStc.address, entryStc.value);
			entries.remove(0);

		} else {
			entry.value = value;
			entry.isdirty = true;
			entry.isValid = true;
			entry.address = address;
		}
	}

	public int readLRU(int address) {
		AssociativeEntry entry = getEntries(address);
		if (entry != null) {
			// hit
			// supprimer la la donnee situé a un index dans la liste entries
			entries.remove(entry);
			// rajoute la valeur a la derniere ligne
			entries.add(entry);
			operationTime = accessTime;
			stats.reads.add(true, operationTime);
		} else {
			// miss
			AssociativeEntry entryStc = entries.get(0);
			memory.write(entryStc.address, entryStc.value);
			// supprimer l'entree la plus agee
			entries.remove(0);
			entryStc.value = memory.read(address);
			entryStc.address = address;
			entryStc.isValid = true;
			// rajoute l'objet entry a la derniere ligne
			entries.add(entryStc);
			operationTime = memory.getOperationTime() + accessTime;
			stats.reads.add(false, operationTime);
			entry=entryStc;
		}
		return entry.value;
	}

	public void writeLRU(int address, int value) {
		AssociativeEntry entry = getEntries(address);

		if (entry == null) {
			AssociativeEntry entryStc = entries.get(0);
			memory.write(entryStc.address, entryStc.value);
			entries.remove(0);

		} else {
			// supprimer la la donnee situé a un index dans la liste entries
			entries.remove(entry);
			// modifie les valeurs de la donnée
			entry.value = value;
			entry.isdirty = true;
			entry.isValid = true;
			entry.address = address;
			// rajoute la valeur a la derniere ligne
			entries.add(entry);
		}
	}

	public void fflush() {
		for (AssociativeEntry en : entries) {
			if (en.isdirty == true) {
				memory.write(en.address, en.value);
			}

		}
	}

	@Override
	public int getOperationTime() {
		return operationTime;
	}

	@Override
	public Stats getStats() {
		
		return stats;
	}

	//
	// UTILITIES
	//

	private AssociativeEntry getEntries(int address) {

		for (AssociativeEntry en : entries) {
			if (en.address == address && en.isValid == true) {
				return en;
			}

		}
		return null;
	}

}
