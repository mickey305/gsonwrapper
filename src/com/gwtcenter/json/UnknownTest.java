package com.gwtcenter.json;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

/**
 * フィールドが多すぎるので、Javaオブジェクトを割り当てるのではなく、マップで取得する例。
 * @author ysugimura
 */
public class UnknownTest {


  /**
   * Serializerを使う場合、ただしこの場合は指定された型しか許されない。例えば、k1やk2の値として
   * リストを入れると例外が発生する。
   */
//  @Test
  public void testWithSerializer() {

    
    Serializer<Foo>serializer = new Serializer<>(Foo.class);    
    Foo object = serializer.deserialize(
        "{'bar':{'mapList':[{'k1':'apple','k2':'orange'},{'k1':'lemmon','k2':'banana'}]}}");

    StringBuilder s = new StringBuilder();
    for (Map<String,String>map: object.bar.mapList) {
      for (Map.Entry<String, String>e: map.entrySet()) {
        s.append(e.getKey() + "=" + e.getValue() + ",");
      }
    }
    assertEquals("k1=apple,k2=orange,k1=lemmon,k2=banana,", s.toString());
  }
  
  public static class Foo {
    Bar bar;
  }
  
  public static class Bar {
    List<Map<String, String>>mapList;    
  }

  public static class FooAdapter extends Adapter<Foo> {
    FooAdapter() {
      super(Foo.class);
      addSubAdapter(new BarAdapter());
    }
  }
  
  public static class BarAdapter extends Adapter<Bar> {
    public BarAdapter() {
      super(Bar.class);
      this.addSubAdapter(
        new Adapter<List<Map<String, String>>>(new TypeToken<List<Map<String, String>>>(){})
      );
    }
  }
  
  /**
   * gsonのみを使う例。
   * この場合はどのような構造でも可
   */
  @Test
  public void onlyGson() {
    String input = "{'bar':{'mapList':[{'k1':'apple','k2':['a']},{'k1':'lemmon','k2':'banana'}]}}";
    
    Gson gson = new Gson();
    JsonObject top = gson.fromJson(input, JsonObject.class);
    JsonObject bar = top.getAsJsonObject("bar");
    JsonArray mapList = bar.getAsJsonArray("mapList");
    
    StringBuilder s = new StringBuilder();
    mapList.forEach(row-> {
      JsonObject rowObject = (JsonObject)row;
      JsonElement k1 = rowObject.get("k1");
      JsonElement k2 = rowObject.get("k2");
      s.append(k1.toString() + "\n");
      s.append(k2.toString() + "\n");
    });

    String result = 
      "\"apple\"\n" +
      "[\"a\"]\n" +
      "\"lemmon\"\n" +
      "\"banana\"\n";
    assertEquals(result, s.toString());
  }
  
}
