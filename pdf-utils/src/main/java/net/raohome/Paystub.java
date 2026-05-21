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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import net.raohome.TextStripperWithPos.Column;
import net.raohome.TextStripperWithPos.TableBuilder;
import net.raohome.TextStripperWithPos.TableBuilder.Justification;
import net.raohome.TextStripperWithPos.TableRow;
import net.raohome.TextStripperWithPos.TextData;

public class Paystub {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage Paystub <PDF File>");
			System.exit(1);
		}

		Collection<PaystubInformation> paystubInformation = getPaystubInformation(args[0]);
		for (PaystubInformation paystub : paystubInformation) {
			System.out.printf("%s%n", paystub);
		}
	}

	public static class PaystubInformation {

		public LocalDate payDate;
		public BigDecimal netPay;
		public BigDecimal gross;

		public List<LineItem> earnings = new ArrayList<Paystub.LineItem>();
		public List<LineItem> taxes = new ArrayList<>();
		public List<LineItem> deductions = new ArrayList<>();
		public int page;

		@Override
		public String toString() {
			StringBuilder bldr = new StringBuilder("Pay date:");

			bldr.append(payDate).append(" Gross:").append(gross).append(" Net Pay:").append(netPay).append("\n");

			writeSection(bldr, "Earnings", earnings);
			writeSection(bldr, "Taxes", taxes);
			writeSection(bldr, "Deductions", deductions);

			return bldr.toString();
		}

		private void writeSection(StringBuilder bldr, String header, List<LineItem> list) {
			bldr.append(header).append("\n");
			for (LineItem item : list) {
				bldr.append("\t").append(item.key).append(":").append(item.amount).append("\n");
			}
		}
	}

	public static Collection<PaystubInformation> getPaystubInformation(String filePath) {
		List<PaystubInformation> dataList = new ArrayList<>();
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filePath));) {

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				PaystubInformation info = getPaystubInformation(document, i);
				info.page = i;
				dataList.add(info);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dataList;
	}

	public static record LineItem(String key, BigDecimal amount) {

	}

	private static PaystubInformation getPaystubInformation(PDDocument document, int i) throws Exception {
		PaystubInformation info = new PaystubInformation();
		TextStripperWithPos stripper = new TextStripperWithPos();
		stripper.setShouldSeparateByBeads(true);
		stripper.setSortByPosition(true);

		stripper.setStartPage(i + 1);
		stripper.setEndPage(i + 1);

		stripper.getText(document);

		List<TextData> line = stripper.findLines("Pay Date").getFirst();

		if (line.isEmpty() == false) {
			info.payDate = PaymentRecord.convertDate(line.getLast().text());
		}

		line = stripper.findLines("Net Pay").getFirst();

		
		if (line.isEmpty() == false) {
			info.netPay = PaymentRecord.convertCurrency(line.getLast().text());
		}

		List<TextData> paySummaryLines = stripper.findLines("Pay Summary").getFirst();
		TextData paySummaryLine = paySummaryLines.getFirst();
		int paySummaryLineY = (int) paySummaryLine.textPositions().getFirst().getY();
		List<List<TextData>> lines = stripper.findLines("Current");

		Optional<List<TextData>> currentLine = lines.stream().filter(l -> {
			int y = (int) l.getFirst().textPositions().getFirst().getY();
			return l.getFirst().text().equals("Current") && paySummaryLineY < y;
		}).findFirst();

		List<TextData> current = currentLine.orElseThrow();

		if (current.size() != 6) {
			throw new RuntimeException("Unexpcted current line size");
		}
		info.gross = PaymentRecord.convertCurrency(current.get(1).text());

		TableBuilder earninesBuilder = stripper.newTableBuilder();

		earninesBuilder.startingWord("Earnings").endingWord("Deductions")
				.withLineFiter(Paystub::sectionHeadingFontSizeChecker)
				.addColumn(Column.ofColumn("Pay Type", Justification.Left))
				.addColumn(Column.ofColumn("Current", Justification.Right));

		List<TableRow> earnings = earninesBuilder.process();

		buildLineItems(info.earnings, earnings);

		TableBuilder deductionsTable = stripper.newTableBuilder();
		deductionsTable.startingWord("Deductions").endingWord("Taxes")
				.withLineFiter(Paystub::sectionHeadingFontSizeChecker)
				.addColumn(Column.ofColumn("Deduction", Justification.Left))
				.addColumn(Column.ofColumn("Employee Current", Justification.Right));

		List<TableRow> deductions = deductionsTable.process();
		buildLineItems(info.deductions, deductions);

		TableBuilder taxesBuilder = stripper.newTableBuilder();
		taxesBuilder.startingWord("Taxes").endingWord("Paid Time Off")
				.withLineFiter(Paystub::sectionHeadingFontSizeChecker)
				.addColumn(Column.ofColumn("Tax", Justification.Left))
				.addColumn(Column.ofColumn("Current", Justification.Right));

		List<TableRow> taxes = taxesBuilder.process();

		buildLineItems(info.taxes, taxes);
		return info;
	}

	private static void buildLineItems(List<LineItem> lineItemList, List<TableRow> rows) {

		rows.forEach(row -> {
			if (row.cells.size() != 2) {
				throw new RuntimeException("Unexpected size:" + row.cells.size());
			}

			BigDecimal amt = PaymentRecord.convertCurrency(row.cells.getLast().data.text());
			if (BigDecimal.ZERO.compareTo(amt) != 0) {
				LineItem lineItem = new LineItem(row.cells.getFirst().data.text(), amt);
				lineItemList.add(lineItem);
			}
		});
	}

	static boolean sectionHeadingFontSizeChecker(TextData td) {
		return 14 == (int) td.textPositions().getFirst().getFontSize();
	}

	public static void findLineItemsInTheSection(List<LineItem> dest, List<List<TextData>> section) {
		section.forEach(elist -> {
			if (elist.size() != 3 && elist.size() != 4) {
				throw new RuntimeException("Unexpected size");
			}
			String item = elist.getFirst().text();
			String amt = elist.get(2).text();
			if (amt.startsWith("$")) {
				BigDecimal amount = PaymentRecord.convertCurrency(amt);
				if (BigDecimal.ZERO.compareTo(amount) != 0) {
					LineItem lineItem = new LineItem(item, amount);
					dest.add(lineItem);
				}
			}
		});
	}

}
