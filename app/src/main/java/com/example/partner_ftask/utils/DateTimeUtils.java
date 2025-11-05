package com.example.partner_ftask.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    // Support multiple ISO date formats
    private static final SimpleDateFormat ISO_FORMAT_WITH_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private static final SimpleDateFormat ISO_FORMAT_WITHOUT_TZ = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat ISO_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    static {
        TimeZone vn = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        ISO_FORMAT_WITH_TZ.setTimeZone(vn);
        ISO_FORMAT_WITHOUT_TZ.setTimeZone(vn);
        ISO_FORMAT_WITH_MILLIS.setTimeZone(vn);
    }

    /**
     * Parse ISO date string with multiple format support
     */
    private static Date parseISODate(String isoDateTime) throws ParseException {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            throw new ParseException("Empty date string", 0);
        }

        // Try format with timezone first
        try {
            return ISO_FORMAT_WITH_TZ.parse(isoDateTime);
        } catch (ParseException e1) {
            // Try format without timezone
            try {
                return ISO_FORMAT_WITHOUT_TZ.parse(isoDateTime);
            } catch (ParseException e2) {
                // Try format with milliseconds
                return ISO_FORMAT_WITH_MILLIS.parse(isoDateTime);
            }
        }
    }

    public static String formatDate(String isoDateTime) {
        try {
            Date date = parseISODate(isoDateTime);
            return DISPLAY_DATE_FORMAT.format(date);
        } catch (ParseException e) {
            android.util.Log.e("DateTimeUtils", "Failed to parse date: " + isoDateTime, e);
            return isoDateTime != null ? isoDateTime : "";
        }
    }

    public static String formatTime(String isoDateTime) {
        try {
            Date date = parseISODate(isoDateTime);
            return DISPLAY_TIME_FORMAT.format(date);
        } catch (ParseException e) {
            android.util.Log.e("DateTimeUtils", "Failed to parse time: " + isoDateTime, e);
            return isoDateTime != null ? isoDateTime : "";
        }
    }

    public static String formatDateTime(String isoDateTime) {
        try {
            Date date = parseISODate(isoDateTime);
            return DISPLAY_DATETIME_FORMAT.format(date);
        } catch (ParseException e) {
            android.util.Log.e("DateTimeUtils", "Failed to parse datetime: " + isoDateTime, e);
            return isoDateTime != null ? isoDateTime : "";
        }
    }

    public static String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " phút";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " giờ";
            } else {
                return hours + " giờ " + remainingMinutes + " phút";
            }
        }
    }
}

