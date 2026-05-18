package net.raohome;

import java.io.IOException;
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
		String filename = args[0];
		getMortgateData(filename);
	}

	public static PaymentRecord getMortgateData(String filePath)  {
		try (PDDocument document = Loader.loadPDF(new RandomAccessReadBufferedFile(filePath));) {
			TextStripperWithPos stripper = new TextStripperWithPos();
			stripper.setShouldSeparateByBeads(true);
			stripper.setSortByPosition(true);

			stripper.setStartPage(1);
			stripper.setEndPage(2);

			stripper.getText(document);

			// System.out.println(text);
			//System.out.println("*******************************************");
			List<TextData> textData = stripper.getTextData();
//			textData.forEach(td -> {
//				System.out.println(td);
//			});

			// Look for the line where Past Payments Breakdown words appear
			List<TextData> list = stripper.findLines("Past", "Payments", "Breakdown").getFirst();

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

		//		System.out.println(wordsAfter);
			}

			list = stripper.findLines("Transaction", "Activity").getFirst();
			first = list.getFirst().textPositions().getFirst();
			// Now search for the entries starting at same x position, but bellow the words
			list = stripper.findWordsStartingAtXAfterY((int) first.getX(), (int) first.getEndY());
		//	System.out.println(list);

			if (list.size() >= 2) {
				TextData postingDate = list.get(1);
				pd.setPostingDate(PaymentRecord.convertDate( postingDate.text()));
			}
			return pd;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
