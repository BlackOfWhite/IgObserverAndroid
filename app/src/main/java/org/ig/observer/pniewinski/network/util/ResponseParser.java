package org.ig.observer.pniewinski.network.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseParser {

  public static int ordinalIndexOf(String str, String substr, int n) {
    int pos = str.indexOf(substr);
    while (--n > 0 && pos != -1) {
      pos = str.indexOf(substr, pos + 1);
    }
    return pos;
  }

  public static Long parseLong(String value, String c, int n) {
    return parseLong(value, c, n, 1);
  }

  /**
   * @param c pattern to find in string to start substr from
   * @param n index of nth occurrence of c char
   */
  public static Long parseLong(String value, String c, int n, int b) {
    if (value != null) {
      return Long.valueOf(parseString(value, c, n, b));
    }
    return null;
  }

  /**
   * @param c pattern to find in string to start substr from
   * @param n index of nth occurrence of c char
   */
  public static Boolean parseBoolean(String value, String c, int n) {
    if (value != null) {
      return Boolean.valueOf(parseString(value, c, n));
    }
    return null;
  }

  public static String getMatch(String text, Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group();
    }
    return null;
  }

  public static String parseString(String value, String c, int n) {
    return parseString(value, c, n, 1);
  }

  public static String parseString(String value, String c, int n, int b) {
    if (value != null) {
      try {
        int start = ordinalIndexOf(value, c, n);
        return value.substring(start + 1, value.length() - b);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }
}
