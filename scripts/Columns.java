package scripts;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static scripts.Utils.println;

class Columns {

    public static void print(List<String> headerNames, Stream<List<Object>> dataStream) {
        var headers = headerNames.stream().map(HeaderDescription::new).collect(Collectors.toList());
        final int columnCount = headers.size();
        var data = dataStream
                .map(row -> row.stream().map(v -> v == null ? "-" : v.toString()).toList())
                .toList();
        if (!data.stream().allMatch(row -> row.size() == columnCount)) {
            throw new IllegalArgumentException("data dimensions");
        }
        for (int i = 0; i < columnCount; ++i) {
            for (List<String> datum : data) {
                headers.get(i).updateMaxLength(datum.get(i).length());
            }
        }
        printRow(headers, headers.stream().map(h -> h.text).collect(Collectors.toList()));
        data.forEach(row -> printRow(headers, row));
    }

    private static void printRow(List<HeaderDescription> headers, List<String> row) {
        assert headers.size() == row.size();
        var line = IntStream.range(0, headers.size()).mapToObj(i -> headers.get(i).render(row.get(i)))
                .collect(Collectors.joining("  "));
        println(line);
    }

    private static class HeaderDescription {
        public final String text;
        public final boolean right;
        private int maxLength;

        public HeaderDescription(String text) {
            this.right = text.endsWith("*");
            this.text = this.right ? text.substring(0, text.length() - 1) : text;
            this.maxLength = this.text.length();
        }

        public void updateMaxLength(int value) {
            if (value > maxLength) {
                maxLength = value;
            }
        }

        public String render(String value) {
            var sb = new StringBuilder(maxLength);
            var padding = maxLength - value.length();
            if (right) {
                pad(sb, padding);
                sb.append(value);
            } else {
                sb.append(value);
                pad(sb, padding);
            }
            return sb.toString();
        }

        private static void pad(StringBuilder sb, int num) {
            sb.append(" ".repeat(num));
        }
    }
}
