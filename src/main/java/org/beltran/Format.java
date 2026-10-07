package org.beltran;

public final class Format {

    private Format() {
    }

    public static double roundToCentavos(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static String formatPeso(double value) {
        return "PHP " + String.format("%,.2f", value);
    }

    public static String pad(String text, int width) {
        return text + repeat(' ', Math.max(0, width - text.length()));
    }

    public static String padRight(String text, int width) {
        return repeat(' ', Math.max(0, width - text.length())) + text;
    }

    public static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}