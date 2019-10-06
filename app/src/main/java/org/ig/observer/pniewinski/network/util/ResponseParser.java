package org.ig.observer.pniewinski.network.util;

import static org.ig.observer.pniewinski.network.Processor.NOT_FOUND;

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

  /**
   * @param c pattern to find in string to start substr from
   * @param n index of nth occurrence of c char
   */
  public static long parseLong(String value, String c, int n) {
    return Long.valueOf(parseString(value, c, n));
  }

  public static String getMatch(String text, Pattern pattern) {
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
      return matcher.group();
    }
    return String.valueOf(NOT_FOUND);
  }

  public static String parseString(String value, String c, int n) {
    try {
      int start = ordinalIndexOf(value, c, n);
      return value.substring(start + 1, value.length() - 1);
    } catch (Exception e) {
      return String.valueOf(NOT_FOUND);
    }
  }

}
