package net.raohome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class TextStripperWithPos extends PDFTextStripper {

	private List<TextData> textData;

	public static record TextData(String text, List<TextPosition> textPositions) {

		@Override
		public final String toString() {
			TextPosition first = textPositions.getFirst();
			TextPosition last = textPositions.getLast();

			return String.format("(%d,%d)=>(%d,%d) %s", (int) first.getX(), (int) first.getY(), (int) last.getEndX(),
					(int) last.getEndY(), text);
		}
	}

	Map<Integer, List<TextData>> lines;

	List<TextData> sortedList = new ArrayList<>();

	public List<TextData> getSortedTextDataList() {
		if (sortedList.isEmpty()) {
			sortedList.addAll(textData);

			sortedList.sort(compareTextData());
		}

		return sortedList;
	}

	public Comparator<? super TextData> compareTextData() {
		return (t1, t2) -> {
			int compare = Float.compare(t1.textPositions.getFirst().getY(), t2.textPositions.getFirst().getY());
			if (compare != 0) {
				return compare;
			}
			compare = Float.compare(t1.textPositions.getFirst().getX(), t2.textPositions.getFirst().getX());
			return compare;
		};
	}

	public List<TextData> getTextData() {
		return textData;
	}

	public TextStripperWithPos() {
		this.textData = new ArrayList<TextData>();
		lines = new HashMap<>();
	}

	protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
		TextData currentTextData = new TextData(text, textPositions);
		textData.add(currentTextData);
		List<TextData> line = lines.computeIfAbsent((int) textPositions.getFirst().getY(), (_) -> {
			return new ArrayList<>();
		});
		line.add(currentTextData);

		super.writeString(text, textPositions);
	}

	public List<TextData> findWordsStartingAtXAfterY(int xPos, int yAfter) {
		return textData.stream().filter(td -> {
			return xPos == (int) td.textPositions.getFirst().getX();
		}).filter(td -> {
			return td.textPositions.getLast().getEndY() < yAfter;
		}).toList();
	}

	public List<List<TextData>> findLines(String... lineWords) {

		List<List<TextData>> linesContainingWords = lines.values().stream().filter(tdList -> {
			Set<String> curlineWords = tdList.stream().map(t -> t.text).collect(Collectors.toSet());
			for (String word : lineWords) {

				if (curlineWords.contains(word) == false) {
					return false;
				}
			}
			return true;
		}).filter(tdList -> {
			return tdList.size() >= lineWords.length;
		}).collect(Collectors.toList());

		linesContainingWords.forEach(t -> {
			t.sort(compareTextData());
		});

		return linesContainingWords;

	}

	public List<TextData> findWordsAfter(TextData td) {
		int beginX = (int) td.textPositions.getFirst().getX();
		int beginY = (int) td.textPositions.getFirst().getY();
		List<TextData> list = textData.stream().filter(data -> {
			return beginX < data.textPositions.getFirst().getX();
		}).filter(data -> {
			return beginY == (int) data.textPositions.getFirst().getY();
		}).toList();

		return list;
	}

	public List<List<TextPosition>> getSeparatedTextPositionsIfSpacePresent(List<TextPosition> textPositions) {

		if (textPositions == null || textPositions.isEmpty()) {
			return List.of();
		}

		var resultList = new ArrayList<List<TextPosition>>();
		int start = 0;

		for (int i = 0; i < textPositions.size() - 1; i++) {
			var current = textPositions.get(i);
			var next = textPositions.get(i + 1);

			float diff = next.getX() - current.getEndX();

			// Detect a "space" or large horizontal gap
			if (diff > getSpacingTolerance()) {
				var part = List.copyOf(textPositions.subList(start, i + 1));
				resultList.add(part);
				start = i + 1;
			}
		}

		if (start < textPositions.size()) {
			var lastPart = List.copyOf(textPositions.subList(start, textPositions.size()));
			resultList.add(lastPart);
		}

		return List.copyOf(resultList);
	}
}
