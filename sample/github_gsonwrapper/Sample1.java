package github_gsonwrapper;

import java.util.*;
import java.util.stream.*;

import org.junit.*;
import static org.junit.Assert.*;

import com.cm55.gson.*;

public class Sample1 {

  @Test
  public void test() {
    
    // Fooに対するシリアライザを作成する
    Serializer<Foo>serializer = new Serializer<>(Foo.class);
    
    // JSON文字列
    String json;

    // オブジェクトを作成し、JSON文字列に変換
    {
      Foo foo = new Foo("testString1", "testString2");
      foo.list.add(new Bar(1, 2));
      foo.list.add(new Bar(3, 4));
      json = serializer.serialize(foo);
      
      assertEquals("{\"list\":[{\"a\":1,\"b\":2},{\"a\":3,\"b\":4}],\"str1\":\"testString1\"}", json);
    }
    
    // JSON文字列からオブジェクトを再構築
    {
      Foo foo = serializer.deserialize(json);
      assertEquals("testString1,null,1,2,3,4", foo.toString());
    }
  }

  public static class Foo {
    List<Bar>list = new ArrayList<>();
    String str1;
    transient String str2;
    Foo(String str1, String str2) {
      this.str1 = str1;
      this.str2 = str2;
    }
    @Override
    public String toString() {
      return str1 + "," + str2 + "," + list.stream().map(b->b.toString()).collect(Collectors.joining(","));
    }
  }
  
  public static class Bar {
    int a, b;
    Bar(int a, int b) {
      this.a = a;
      this.b = b;
    }
    @Override
    public String toString() {
      return a + "," + b;
    }
  }
}
