package github_gsonwrapper;

import java.util.*;

import org.junit.*;
import static org.junit.Assert.*;

import com.cm55.gson.*;
import com.google.gson.reflect.*;

public class Sample3 {

  @Test
  public void test() {
    Serializer<List<String>> serializer = new Serializer<>(new TypeToken<List<String>>() {});
    String json;
    {
      List<String> list = new ArrayList<String>();
      list.add("A");
      list.add("B");
      json = serializer.serialize(list);
      assertEquals("[\"A\",\"B\"]", json);
    }
    
    {
      List<String>list = serializer.deserialize(json);
      assertArrayEquals(new String[] { "A", "B" }, list.toArray(new String[0]));
    }
  }
}
