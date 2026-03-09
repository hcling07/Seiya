package seiya.util;

import java.util.Locale;

public final class NumberFormatter {
    private NumberFormatter() {
    }

    public static String fmt(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.0001) {
            return String.valueOf((int) Math.rint(value));
        }
        return String.format(Locale.US, "%.2f", value);
    }
}
