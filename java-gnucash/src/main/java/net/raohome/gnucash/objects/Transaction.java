package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetDescription;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNotes;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetNum;
import static net.raohome.gnucash.gen.GNUCashBinding.xaccTransGetSplitList;
import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.LocalDate;
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
	
	
	public static Transaction newTransaction(Book book) {
		MemorySegment transactionPtr = xaccMallocTransaction(book.pointer);
		return new Transaction(transactionPtr);
	}

	public void setType(byte type) {
		xaccTransSetTxnType(pointer, type);
	}
	
	public void setNumber(String number) {
		xaccTransSetNum(pointer, Arena.ofAuto().allocateFrom(number));
	}
	public void setDescription(String descr) {
		xaccTransSetDescription(pointer, Arena.ofAuto().allocateFrom(descr));
	}
	
	public void setDocsLink(String docLink) {
		xaccTransSetDocLink(pointer, Arena.ofAuto().allocateFrom(docLink));
	}
	
	public void setNotes(String notes) {
		xaccTransSetNotes(pointer, Arena.ofAuto().allocateFrom(notes));
	}
	
	public void setDate(LocalDate date) {
		xaccTransSetDate(pointer, date.getDayOfMonth(), date.getMonthValue(), date.getYear());
	}
	
	public void setCommodity(Commodity commodity) {
		xaccTransSetCurrency(pointer, commodity.pointer);
	}
	
	
	

}
