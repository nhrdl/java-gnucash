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

		getPaystubInformation(args[0]);
	}

	public static class PaystubInformation {

		public LocalDate payDate;
		public BigDecimal gross;

		public List<LineItem> earnings = new ArrayList<Paystub.LineItem>();
		public List<LineItem> taxes = new ArrayList<>();
		public List<LineItem> deductions = new ArrayList<>();
	}

	private static Collection<PaystubInformation> getPaystubInformation(String filePath) {
		List<PaystubInformation> dataList = new ArrayList<>();
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filePath));) {

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				PaystubInformation info = getPaystubInformation(document, i);
				dataList.add(info);

				System.err.println("End page *********************************************************");
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
		
		earnings.forEach(tr -> {
			tr.cells.forEach(td -> {
				System.out.printf("%s:%s%n", td.column.getName(), td.data.text());
			});

			System.out.println("***************************************************************");
		});

		System.err.println("****************************************%n");
		TableBuilder deductionsTable = stripper.newTableBuilder();
		deductionsTable.startingWord("Deductions").endingWord("Taxes")
				.withLineFiter(Paystub::sectionHeadingFontSizeChecker)
				.addColumn(Column.ofColumn("Deduction", Justification.Left))
				.addColumn(Column.ofColumn("Employee Current", Justification.Right));

		List<TableRow> deductions = deductionsTable.process();
		deductions.forEach(tr -> {
			tr.cells.forEach(td -> {
				System.out.printf("%s:%s%n", td.column.getName(), td.data.text());
			});

			System.out.println("***************************************************************");
		});

		System.err.printf("****************************************%n");

		TableBuilder taxesBuilder = stripper.newTableBuilder();
		taxesBuilder.startingWord("Taxes").endingWord("Paid Time Off")
				.withLineFiter(Paystub::sectionHeadingFontSizeChecker)
				.addColumn(Column.ofColumn("Tax", Justification.Left))
				.addColumn(Column.ofColumn("Current", Justification.Right));

		List<TableRow> taxes = taxesBuilder.process();

		taxes.forEach(tr -> {
			tr.cells.forEach(td -> {
				System.out.printf("%s:%s%n", td.column.getName(), td.data.text());
			});

			System.out.println("***************************************************************");
		});

		System.err.printf("****************************************%n");

		return info;
	}

	private static void buildLineItems(List<LineItem> lineItemList, List<TableRow> rows) {
		
		rows.forEach(row-> {
			if (row.cells.size() != 2) {
				throw new RuntimeException("Unexpected size:" + row.cells.size());
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
