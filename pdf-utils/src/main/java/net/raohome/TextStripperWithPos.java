package net.raohome;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import net.raohome.TextStripperWithPos.TableBuilder.Justification;

public class TextStripperWithPos extends PDFTextStripper {

	private List<TextData> textData;

	public static record TextData(String text, List<TextPosition> textPositions) {

		@Override
		public final String toString() {
			TextPosition first = textPositions.getFirst();
			TextPosition last = textPositions.getLast();

			return String.format("(%d,%d)=>(%d,%d) (%s/%f) %s", (int) first.getX(), (int) first.getY(),
					(int) last.getEndX(), (int) last.getEndY(), first.getFont().getName(), first.getFontSize(), text);
		}

		public int getX() {
			return (int) textPositions.getFirst().getX();
		}

		int getY() {
			return (int) textPositions.getFirst().getY();
		}

		public int getEndX() {
			return (int) textPositions.getLast().getEndX();
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

		line.sort((a, b) -> {

			int result = Integer.compare(a.getY(), b.getY());
			if (result != 0)
				return result;

			return Integer.compare(a.getX(), b.getX());
		});
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

	public List<List<TextData>> findLinesBetween(String firstWord, String secondWord, Predicate<TextData> acceptor) {
		List<List<TextData>> firstWordList = findLinesStartingWith(firstWord, acceptor);

		List<List<TextData>> secondWordList = findLinesStartingWith(secondWord, acceptor);

		if (firstWordList.size() != 1 || secondWordList.size() != 1) {
			firstWordList.forEach(System.out::println);

			System.out.println("******************");
			secondWordList.forEach(System.out::println);
			throw new RuntimeException("Does not match ");
		}
		int lowerY = secondWordList.getFirst().getFirst().getY();
		int upperY = firstWordList.getFirst().getFirst().getY();

		List<List<TextData>> list = lines.values().stream().filter(t -> {
			int y = t.getFirst().getY();
			return y > upperY && y < lowerY;
		}).collect(Collectors.toList());

		list.sort((l1, l2) -> {
			int first = l1.getFirst().getY();
			int second = l2.getFirst().getY();
			return Integer.compare(first, second);
		});
		return list;
	}

	public List<List<TextData>> findLinesStartingWith(String text, Predicate<TextData> acceptor) {
		return lines.values().stream().filter(t -> {
			return text.equals(t.getFirst().text());
		}).filter(l -> {
			return acceptor.test(l.getFirst());
		}).toList();
	}

	public static class Column {

		private String name;
		public String getName() {
			return name;
		}

		private Justification justification;
		private TextData headerData;

		int pos;

		void computePos() {
			if (this.justification == Justification.Left) {
				pos = headerData.getX();
			} else {
				pos = headerData.getEndX();
			}
		}

		public Column(String name, Justification justification) {
			this.name = name;
			this.justification = justification;
		}

		public static Column ofColumn(String name, Justification justification) {
			return new Column(name, justification);
		}
	}

	public class TableCell {
		public Column column;
		public TextData data;
	}

	public class TableRow {
		public List<TableCell> cells = new ArrayList<TextStripperWithPos.TableCell>();
	}

	public class TableBuilder {
		public static enum Justification {
			Left, Right
		};

		private List<Column> columns = new ArrayList<TextStripperWithPos.Column>();

		private String startingWord;
		private String endingWord;

		private Predicate<TextData> textFilter;

		public TableBuilder startingWord(String startingWord) {
			this.startingWord = startingWord;
			return this;
		}

		public TableBuilder endingWord(String endingWord) {
			this.endingWord = endingWord;
			return this;
		}

		public TableBuilder addColumn(Column clmn) {
			columns.add(clmn);
			return this;
		}

		public TableBuilder withLineFiter(Predicate<TextData> textFilter) {
			this.textFilter = textFilter;
			return this;

		}

		public List<TableRow> process() {
			List<List<TextData>> lines = findLinesBetween(startingWord, endingWord, textFilter);
			// Let's assume first row is the header row and find coordinates
			List<TextData> headerLine = lines.getFirst();
			System.out.println(headerLine);
			for (Column clmn : columns) {
				for (TextData td : headerLine) {
					if (td.text.equals(clmn.name)) {
						clmn.headerData = td;
					}
				}
			}

			for (Column clmn : columns) {
				if (clmn.headerData == null) {
					throw new RuntimeException("Could not find column data: " + clmn.name);
				}
				clmn.computePos();
			}

			List<TableRow> rows = new ArrayList<TextStripperWithPos.TableRow>();
			for (int i = 1; i < lines.size(); i++) {
				List<TextData> line = lines.get(i);
				TableRow row = new TableRow();
				rows.add(row);
				for (Column clmn : columns) {
					for (TextData td : line) {
						int pos;
						if (clmn.justification == Justification.Left) {
							pos = td.getX();
						} else {
							pos = td.getEndX();
						}
						if (pos == clmn.pos) {
							TableCell cell = new TableCell();
							cell.column = clmn;
							cell.data = td;
							row.cells.add(cell);
							break;
						}
					}
				}
			}

			return rows;
		}
	}

	public List<List<TextPosition>> buildTableWithColumns(String startingWord, String endWord, String... columns) {

		return List.of();
	}

	public TableBuilder newTableBuilder() {
		return new TableBuilder();
	}
}
