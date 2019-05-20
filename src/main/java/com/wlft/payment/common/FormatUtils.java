package com.wlft.payment.common;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class FormatUtils {

    //  金額正規化
    public static final NumberFormat AMOUNT_FORMATTER = NumberFormat.getCurrencyInstance();
    //  金額正規化
    public static final NumberFormat NUMBER_FORMATTER = DecimalFormat.getNumberInstance();
    //  日期時間正規化
    public static final DateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //  日期時間正規化
    public static final DateFormat DATE_TIME_FORMATTER2 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    //  日期正規化
    public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    //  時間正規化
    public static final DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

}
