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

import net.raohome.gnucash.gen.GNUCashBinding;

public class GNCGUID {

	private MemorySegment pointer;

	public GNCGUID(MemorySegment pointer) {
		this.pointer = pointer;
		
	}
	
	public String guidToString() {
		return GNUCashBinding.guid_to_string(pointer).getString(0);
	}
}
