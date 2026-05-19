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

import java.lang.foreign.MemorySegment;

import static net.raohome.gnucash.gen.GNUCashBinding.*;

public class Commodity extends BaseObject {

	public Commodity(MemorySegment pointer) {
		super(pointer);
	}

	public String getFullName() {
		MemorySegment fullname = gnc_commodity_get_fullname(this.pointer);
		return fullname.getString(0);
	}
	
	public String getNamespace() {
		MemorySegment namespace = gnc_commodity_get_namespace(this.pointer);
		return namespace.getString(0);
	}

	public String getMnemonic() {
		MemorySegment namespace = gnc_commodity_get_mnemonic(this.pointer);
		return namespace.getString(0);
	}
}
