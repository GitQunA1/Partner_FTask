package com.example.partner_ftask.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    static {
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static String formatDate(String isoDateTime) {
        try {
            Date date = ISO_FORMAT.parse(isoDateTime);
            return DISPLAY_DATE_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDateTime;
        }
    }

    public static String formatTime(String isoDateTime) {
        try {
            Date date = ISO_FORMAT.parse(isoDateTime);
            return DISPLAY_TIME_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDateTime;
        }
    }

    public static String formatDateTime(String isoDateTime) {
        try {
            Date date = ISO_FORMAT.parse(isoDateTime);
            return DISPLAY_DATETIME_FORMAT.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return isoDateTime;
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

