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
package net.raohome;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.Year;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class PaymentRecord {
	private static DateTimeFormatter MDY_DATE_FORMATTER;
	BigDecimal principal;
	BigDecimal interest;
	BigDecimal escrow;
	BigDecimal fees;
	LocalDate postingDate;

	public LocalDate getPostingDate() {
		return postingDate;
	}

	public void setPostingDate(LocalDate postingDate) {
		this.postingDate = postingDate;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public void setPrincipal(String principal) {
		this.principal = convertCurrency(principal);
	}

	public BigDecimal getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = convertCurrency(interest);
	}

	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}

	public BigDecimal getEscrow() {
		return escrow;
	}

	public void setEscrow(String escrow) {
		this.escrow = convertCurrency(escrow);
	}

	public void setEscrow(BigDecimal escrow) {
		this.escrow = escrow;
	}

	public BigDecimal getFees() {
		return fees;
	}

	public void setFees(String fees) {
		this.fees = convertCurrency(fees);
	}

	public void setFees(BigDecimal fees) {
		this.fees = fees;
	}

	@Override
	public String toString() {
		return String.format("Principal :%s, Interest :%s, Escrow :%s, Fees:%s", principal, interest, escrow, fees);
	}

	public static LocalDate convertDate(String dateString) {
		TemporalAccessor date = MDY_DATE_FORMATTER.parse(dateString);

		return LocalDate.of(getYear(date), date.get(ChronoField.MONTH_OF_YEAR), date.get(ChronoField.DAY_OF_MONTH));
	}

	/**
	 * Calculates the appropriate year for a parsed date when year information is
	 * missing or ambiguous. Handles 2-digit years by converting them to 20xx format
	 * and performs leap year validation for February 29th dates.
	 * 
	 * @param date TemporalAccessor containing the parsed date information (may be
	 *             missing year)
	 * @return The calculated 4-digit year, adjusted for leap year compatibility if
	 *         needed
	 */
	private static int getYear(TemporalAccessor date) {
		int year = date.isSupported(ChronoField.YEAR) ? date.get(ChronoField.YEAR) : Year.now().getValue();

		// Convert 2-digit years to 20xx if less than 2000
		if (year < 2000) {
			year += 2000;
		}
		if (date.get(ChronoField.MONTH_OF_YEAR) == 2 && date.get(ChronoField.DAY_OF_MONTH) == 29) {
			while (!IsoChronology.INSTANCE.isLeapYear(year)) {
				year--;
			}
		}
		return year;
	}

	public static BigDecimal convertCurrency(String val) {

		DecimalFormat cf = new DecimalFormat("$##,##0.00;($##,##0.00)");
		cf.setParseBigDecimal(true);
		try {
			return (BigDecimal) cf.parse(val);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	static {
		{
			DateTimeFormatterBuilder bldr = new DateTimeFormatterBuilder();
			bldr.parseCaseInsensitive().appendOptional(DateTimeFormatter.ofPattern("LLLL d,[ ]y"))
					.appendOptional(DateTimeFormatter.ofPattern("LLLL d y"))
					.appendOptional(DateTimeFormatter.ofPattern("L d,[ ]y")) //
					.appendOptional(DateTimeFormatter.ofPattern("L d, y")) //
					.appendOptional(DateTimeFormatter.ofPattern("LLL d, y")) //
					.appendOptional(DateTimeFormatter.ofPattern("LLL d,y")) //
					.appendOptional(DateTimeFormatter.ofPattern("LLL d")) //
					.appendOptional(DateTimeFormatter.ofPattern(getFormatterString("L~d~y"))) //
					.appendOptional(DateTimeFormatter.ofPattern(getFormatterString("L~d")))
					.appendOptional(DateTimeFormatter.ofPattern("LLL['t']. d, y"))
					.appendOptional(DateTimeFormatter.ofPattern("LLL['t'].d,y"))
					.appendOptional(DateTimeFormatter.ofPattern("LLL['t']d,y"))

			;
			MDY_DATE_FORMATTER = bldr.toFormatter();
		}
	}

	private static String getFormatterString(String format) {
		return format.replace("~", "[ ][,][/][-]");
	}
}
