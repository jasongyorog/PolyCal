package com.gyorog.polycal;

abstract class PolyCalDateFormats {
    static CharSequence[] formats_readable = {
            "Jun 18 | 12:34 pm",
            "6-18|12:34pm",
            "June 18, 2020 @ 12:34 pm",
            "2020-06-18 23:45"
    };
    static CharSequence[] formats_parseable = {
            " MMM d | h:mm a ",
            " M-d|h:mma ",
            " MMMM d, yyyy @ h:mm a ",
            " yyyy-MM-dd kk:mm "
    };
    static CharSequence[] formats_readable_allday = {
            "Jun 18",
            "06-18",
            "June 18, 2020",
            "2020-06-18 23:45"
    };
    static CharSequence[] formats_parseable_allday = {
            " MMM d ",
            " M-d ",
            " MMMM d, yyyy ",
            " yyyy-MM-dd "
    };

    public static CharSequence[] getFormatsReadable() { return formats_readable; }
    public static CharSequence[] getFormatsParseable() { return formats_parseable; }
    public static CharSequence[] getFormatsReadableAllday() { return formats_readable_allday; }
    public static CharSequence[] getFormatsParseableAllday() { return formats_parseable_allday; }

}
