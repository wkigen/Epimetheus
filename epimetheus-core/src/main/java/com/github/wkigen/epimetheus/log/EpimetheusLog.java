package com.github.wkigen.epimetheus.log;

/**
 * Created by Dell on 2018/3/26.
 */

public class EpimetheusLog {

    private final String TAG = "EpimetheusLog";

    public static void v(final String tag, final String format, final Object... params) {
        String log = (params == null || params.length == 0) ? format : String.format(format, params);
        android.util.Log.v(tag, log);
    }

    public static void i(final String tag, final String format, final Object... params) {
        String log = (params == null || params.length == 0) ? format : String.format(format, params);
        android.util.Log.i(tag, log);

    }

    public static void d(final String tag, final String format, final Object... params) {
        String log = (params == null || params.length == 0) ? format : String.format(format, params);
        android.util.Log.d(tag, log);
    }

    public static void w(final String tag, final String format, final Object... params) {
        String log = (params == null || params.length == 0) ? format : String.format(format, params);
        android.util.Log.w(tag, log);
    }

    public static void e(final String tag, final String format, final Object... params) {
        String log = (params == null || params.length == 0) ? format : String.format(format, params);
        android.util.Log.e(tag, log);
    }

}
