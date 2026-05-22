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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import net.raohome.TextStripperWithPos.TextData;

public class RSU {
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage Paystub <PDF File>");
			System.exit(1);
		}

		List<RSURecord> rsuRecords = process(args[0]);
		rsuRecords.forEach(System.out::println);
	}

	private static List<RSURecord> process(String filePath) {
		List<RSURecord> rsuRecords = new ArrayList<>();
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filePath));) {

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				processPage(rsuRecords, document, i);
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return rsuRecords;
	}

	private static void processPage(List<RSURecord> rsuRecords, PDDocument document, int i) throws Exception {
		TextStripperWithPos stripper = new TextStripperWithPos();
		stripper.setShouldSeparateByBeads(true);
		stripper.setSortByPosition(true);

		stripper.setStartPage(i + 1);
		stripper.setEndPage(i + 1);

		stripper.getText(document);

		List<TextData> textDataList = stripper.getSortedTextDataList();
		// textDataList.forEach(System.out::println);

		List<TextData> releaseEntries = textDataList.stream().filter(t -> {
			return t.text().contains("Share Units - Release");
		}).toList();
		System.out.printf("Number of release entries found %d%n", releaseEntries.size());
		for (TextData releaseEntry : releaseEntries) {

			RSURecord rsu = new RSURecord();
			rsuRecords.add(rsu);
			
			rsu.header = releaseEntry.text();

			Optional<TextData> endMarker = textDataList.stream().filter(t -> {

				return t.text().contains("Total Value:") && releaseEntry.getY() < t.getY();
			}).findFirst();

			if (endMarker.isEmpty()) {
				System.err.println("Can not find end marker on the same page " + releaseEntry);
				continue;
			}

			// Find the text between Share Units - Release and Total Value
			int headerY = releaseEntry.getY(), footerY = endMarker.get().getY();

			List<TextData> dataList = textDataList.stream().filter(t -> {
				return t.getY() > headerY && t.getY() < footerY;
			}).toList();

			// dataList.forEach(System.out::println);

			Optional<TextData> entry = findColumn(dataList, "Settlement Date:​");

			if (entry.isEmpty()) {
				System.err.println("No settlement date for " + releaseEntry.text());
			} else {
				List<TextData> rowEntries = findRowEntries(dataList, entry.get());
				if (rowEntries.isEmpty() == false) {
					rsu.date = PaymentRecord.convertDate(rowEntries.getFirst().text());
				}
			}

			entry = findColumn(dataList, "Quantity Released:​");

			if (entry.isEmpty()) {
				System.err.println("No Quantity Released for " + releaseEntry.text());
			} else {
				List<TextData> rowEntries = findRowEntries(dataList, entry.get());
				if (rowEntries.isEmpty() == false) {
					rsu.awarded = PaymentRecord.convertCurrency(rowEntries.getFirst().text());
				}
			}

			entry = findColumn(dataList, "Number of Restricted Awards Withheld:​​");
			if (entry.isEmpty()) {
				System.err.println("No Number of Restricted Awards Withheld:​ for " + releaseEntry.text());
			} else {
				List<TextData> rowEntries = findRowEntries(dataList, entry.get());
				if (rowEntries.isEmpty() == false) {
					rsu.withheld = PaymentRecord.convertCurrency(rowEntries.getFirst().text());
				}
			}
		}

	}

	static List<TextData> findRowEntries(List<TextData> dataList, TextData column) {

		return dataList.stream().filter(t -> {
			return t.getY() == column.getY() && t.getX() >= column.getEndX();
		}).toList();
	}

	public static Optional<TextData> findColumn(List<TextData> dataList, String string) {
		Optional<TextData> entry = dataList.stream().filter(t -> {
			return string.contains(t.text());
		}).findFirst();
		return entry;
	}

	public static class RSURecord {

		public BigDecimal withheld;
		public BigDecimal awarded;
		public LocalDate date;
		public String header;

		@Override
		public String toString() {
		return String.format("%s, %s, Awarded %s, withheld %s", header, date, awarded, withheld);
		}
	}
}
