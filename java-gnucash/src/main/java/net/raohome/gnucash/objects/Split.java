package net.raohome.gnucash.objects;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.math.BigDecimal;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Split extends BaseObject {

	public Split(MemorySegment pointer) {
		super(pointer);
	}

	public static Split newSplit(Book book) {
		MemorySegment splitPointer = xaccMallocSplit(book.pointer);
		return new Split(splitPointer);
	}
	
	public void setAccount(Account account) {
		xaccSplitSetAccount(pointer, account.pointer);
	}
	
	public void setParent(Transaction trans) {
		xaccSplitSetParent(pointer, trans.pointer);
	}
	
	public void setMemo(String memo) {
		xaccSplitSetMemo(pointer, Arena.ofAuto().allocateFrom(memo));
	}
	
	public void setAmount(BigDecimal amount) {
		MemorySegment strAmt = convertNumber(amount.toString());
		xaccSplitSetAmount(pointer, strAmt);
	}
	
	public void setValue(BigDecimal amount) {
		MemorySegment strAmt = convertNumber(amount.toString());
		xaccSplitSetValue(pointer, strAmt);
	}
	
	public void setSharePriceAndAmount(BigDecimal price, BigDecimal amount) {
		MemorySegment pricePtr = convertNumber(price.toString());
		MemorySegment amountPtr = convertNumber(amount.toString());
		
		xaccSplitSetSharePriceAndAmount(pointer, pricePtr, amountPtr);
	}
}
