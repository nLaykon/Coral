package uk.laykon.coral.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {
    public static String formatFloat(float f, int precision) {
        return new BigDecimal(Float.toString(f)).setScale(precision, RoundingMode.HALF_UP).toString();
    }
}
