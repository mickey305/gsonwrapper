package com.cm55.gson;

public class PrintJsonForTest {
  public static void printJson(String json) {
    StringBuilder s = new StringBuilder();
    for (char c: json.toCharArray()) {
      if (c == '"') s.append('\\');
      s.append(c);
    }
    System.out.println('"' + s.toString() + '"');
  }
}
