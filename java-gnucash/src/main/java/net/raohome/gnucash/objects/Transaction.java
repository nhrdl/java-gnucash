package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetDescription;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNotes;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNum;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetSplitList;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetTxnType;

import java.lang.foreign.MemorySegment;
public class Transaction extends BaseObject {

	public Transaction(MemorySegment pointer) {
		super(pointer);
	}

	public String getDescription() {
		return xaccTransGetDescription(pointer).getString(0);
	}

	public byte getType() {
		return xaccTransGetTxnType(pointer);
	}

	public String getNum() {
		return xaccTransGetNum(pointer).getString(0);
	}

	public String getNotes() {
		MemorySegment notes = xaccTransGetNotes(pointer);
		if (notes.equals(MemorySegment.NULL) == false)
			return notes.getString(0);

		return null;
	}
	
	public GList<Split> getSplitList() {
		MemorySegment list = xaccTransGetSplitList(pointer);
		
		return new GList<Split>(list, Split::new);
	}
	
	
}
