/********************************************************************\
 * This program is free software; you can redistribute it and/or    *
 * modify it under the terms of the GNU General Public License as   *
 * published by the Free Software Foundation; either version 2 of   *
 * the License, or (at your option) any later version.              *
 *                                                                  *
 * This program is distributed in the hope that it will be useful,  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of   *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the    *
 * GNU General Public License for more details.                     *
 *                                                                  *
 * You should have received a copy of the GNU General Public License*
 * along with this program; if not, contact:                        *
 *                                                                  *
 * Free Software Foundation           Voice:  +1-617-542-5942       *
 * 51 Franklin Street, Fifth Floor    Fax:    +1-617-542-2652       *
 * Boston, MA  02110-1301,  USA       gnu@gnu.org                   *
 *                                                                  *
\********************************************************************/
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
	public void close()  {
		
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
