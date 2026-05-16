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

	}

	private static Collection<PaystubInformation> getPaystubInformation(String filePath) {
		List<PaystubInformation> dataList = new ArrayList<>();
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filePath));) {

			for (int i = 0; i < document.getNumberOfPages(); i++) {
				PaystubInformation info = getPaystubInformation(document, i);
				dataList.add(info);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return dataList;
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
		if (line.isEmpty() == false ) {
			info.payDate = PaymentRecord.convertDate(line.getLast().text());
		}
		
		List<TextData> paySummaryLines = stripper.findLines("Pay Summary").getFirst();
		TextData paySummaryLine = paySummaryLines.getFirst();
		int paySummaryLineY = (int) paySummaryLine.textPositions().getFirst().getY();
		List<List<TextData>> lines = stripper.findLines("Current");
		
		Optional<List<TextData>> currentLine = lines.stream().filter(l-> {
			int y = (int)l.getFirst().textPositions().getFirst().getY();
			return l.getFirst().text().equals("Current") &&
					paySummaryLineY < y
					;
		}).findFirst();
		
		List<TextData> current = currentLine.orElseThrow();
		
		if (current.size() != 6) {
			throw new RuntimeException("Unexpcted current line size");
		}
		info.gross = PaymentRecord.convertCurrency(current.get(1).text());
		return info;
	}

}
