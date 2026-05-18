package net.raohome.gnucash.objects;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

import java.lang.foreign.MemorySegment;


public class Book extends BaseObject {

	private Book(MemorySegment pointer) {
		super(pointer);
	}

	public static Book newBook() {
		MemorySegment segment = qof_book_new();
		return new Book(segment);
	}
	
	public Account getRootAccount() {
		MemorySegment accountPtr = gnc_book_get_root_account(pointer);
		return new Account(accountPtr);
	}
	
	public CommodityTable getCommidityTable() {
		MemorySegment segment = gnc_commodity_table_get_table(pointer);
		return new CommodityTable(segment);
	}
	
	public PriceDB getPriceDB() {
		MemorySegment segment = gnc_pricedb_get_db(pointer);
		return new PriceDB(segment);
	}
}
