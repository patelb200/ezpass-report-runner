package io.bharatpatel.ezpassreport.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class TablePrinter<T> {
    private final List<T> data;

    private String tableTitle;
    private final Map<String, Function<T, String>> columnTransformers = new LinkedHashMap<>();

    public TablePrinter(List<T> data) {
        this.data = Objects.requireNonNull(data);
    }

    public TablePrinter<T> title(String tableTitle) {
        this.tableTitle = Objects.requireNonNull(tableTitle);
        return this;
    }

    public TablePrinter<T> column(String columnName, Function<T, String> fieldConverter) {
        Objects.requireNonNull(columnName);
        Objects.requireNonNull(fieldConverter);
        this.columnTransformers.put(columnName, fieldConverter);
        return this;
    }

    public void print() {

        var transformedValues = new String[this.data.size() + 1][this.columnTransformers.size()];
        var maxColumnWidth = new int[this.columnTransformers.size()];

        var rowIdx = 0;
        var colIdx = 0;

        for (var colName : this.columnTransformers.keySet()) {
            transformedValues[rowIdx][colIdx] = colName;
            maxColumnWidth[colIdx] = Math.max(maxColumnWidth[colIdx], colName.length());
            colIdx++;
        }

        rowIdx = 1;
        for (T rowData : this.data) {
            colIdx = 0;
            for (var colTransformer : this.columnTransformers.values()) {
                var val = colTransformer.apply(rowData);
                transformedValues[rowIdx][colIdx] = val == null ? "" : val;
                maxColumnWidth[colIdx] = Math.max(maxColumnWidth[colIdx], transformedValues[rowIdx][colIdx].length());
                colIdx++;
            }
            rowIdx++;
        }

        var tableWidth = 0;
        // give indent to each column except last
        for (int i = 0; i < maxColumnWidth.length - 1; i++) {
            maxColumnWidth[i] += 2;
            tableWidth += maxColumnWidth[i];
        }
        tableWidth += maxColumnWidth[maxColumnWidth.length - 1];

        var printOut = new StringBuilder();

        appendDivider(printOut, tableWidth);
        appendHeader(printOut, this.tableTitle, tableWidth);
        appendDivider(printOut, tableWidth);

        for (rowIdx = 0; rowIdx < transformedValues.length; rowIdx++) {
            for (colIdx = 0; colIdx < transformedValues[rowIdx].length; colIdx++) {
                printOut.append(transformedValues[rowIdx][colIdx]).append(" ".repeat(maxColumnWidth[colIdx] - transformedValues[rowIdx][colIdx].length()));
            }
            printOut.append(System.lineSeparator());
        }

        System.out.println(printOut);
    }

    private void appendDivider(StringBuilder table, int length) {
        table.append("=".repeat(length));
        table.append(System.lineSeparator());
    }

    private void appendHeader(StringBuilder table, String title, int tableWidth) {
        var paddingLength = (tableWidth - title.length()) / 2;
        var paddingString = " ".repeat(paddingLength);
        table.append(paddingString).append(title);
        table.append(System.lineSeparator());
    }
}
