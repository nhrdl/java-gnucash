package net.raohome;

import java.math.BigDecimal;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;

import net.raohome.TextStripperWithPos.TextData;

public class Mortgage {

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage Mortgage <statement path>");
			System.exit(1);
		}
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(args[0]));) {
			TextStripperWithPos stripper = new TextStripperWithPos();
			stripper.setStartPage(1);
			stripper.setEndPage(2);

			stripper.getText(document);

			List<TextData> textData = stripper.getTextData();
			textData.forEach(td -> {
				System.out.println(td);
			});

			// Look for the line where Past Payments Breakdown words appear
			List<TextData> list = stripper.findLine("Past", "Payments", "Breakdown");

			TextPosition first = list.getFirst().textPositions().getFirst();

			// Now search for the entries starting at same x position, but bellow the words
			list = stripper.findWordsStartingAtXAfterY((int) first.getX(), (int) first.getEndY());

			PaymentRecord pd = new PaymentRecord();
			for (TextData td : list) {
				List<TextData> wordsAfter = stripper.findWordsAfter(td);

				switch (td.text()) {
				case "Principal":
					pd.setPrincipal(wordsAfter.get(1).text());
					break;
				case "Interest":
					pd.setInterest(wordsAfter.get(1).text());
					break;
				case "Escrow":
					pd.setEscrow(wordsAfter.get(1).text());
					break;
				case "Fees/Late":
					pd.setFees(wordsAfter.get(2).text());
					break;
				}

				System.out.println(wordsAfter);
			}

//			System.out.println(list);
			System.out.println(pd);
		}
	}

}
