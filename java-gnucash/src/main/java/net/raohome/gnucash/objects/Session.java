package net.raohome.gnucash.objects;


import java.io.Closeable;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Session extends BaseObject implements Closeable{

	private Book book;

	public Book getBook() {
		return book;
	}

	public enum SessionMode {
		SESSION_NORMAL_OPEN(SESSION_NORMAL_OPEN()),
		SESSION_NEW_STORE(SESSION_NEW_STORE()),
		SESSION_NEW_OVERWRITE(SESSION_NEW_OVERWRITE()),
		SESSION_READ_ONLY(SESSION_READ_ONLY())
		;
		public final int mode;

		SessionMode(int mode) {
			this.mode = mode;
		}
	}
	private Session(MemorySegment pointer, Book book) {
		super(pointer);
		this.book = book;
	}

	public static Session newSession() {
		Book book = Book.newBook();
		MemorySegment segment = qof_session_new(book.pointer);
		return new Session(segment, book);
	}

	@Override
	public void close() throws IOException {
		
		qof_session_end(pointer);
		qof_session_destroy(pointer);
	}

	public void save() {
		qof_session_save(pointer, MemorySegment.NULL);
	}
	
	public void beginSession(String uri, SessionMode sessionMode) {
		
		qof_session_begin(pointer, Arena.ofAuto().allocateFrom(uri), sessionMode.mode); 
		MemorySegment qof_session_get_error_message = qof_session_get_error_message(pointer);
		System.out.println(qof_session_get_error_message.getString(0));
	}

	public void load() {
		qof_session_load(pointer, MemorySegment.NULL);
		
	}
}
