package com.example.tejas.finalv2.constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by tejas on 2015/08/20.
 */
public final class Constants {

    private Constants() {
    }

    public static final String IP_ADDRESS = "212.71.235.106";
    public static final String ENCODING_FORMAT = "UTF-8";

    public static final DateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss");
    public static final String DIALOG_ERROR = "dialog_error";

    public static final int CENTER_BUTTON_ID = 0x2;
    public static final int PORT_NUMBER = 7380;
    public static final int REQUEST_RESOLVE_ERROR = 1001;

    public static final int ALICE_ID = 1000;
    public static final int BOB_ID = 1001;
}
