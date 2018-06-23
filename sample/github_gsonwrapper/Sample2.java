package github_gsonwrapper;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

import com.cm55.gson.*;

public class Sample2 {


  @Test
  public void test() {
    
    Adapter<Foo>fooAdapter = new AdapterBuilder<>(Foo.class).addSubAdapters(
      new MultiTypeAdapterBuilder<>(Bar.class).addSubClasses(Bar1.class, Bar2.class).build()
    ).build();

    
    // Fooに対するシリアライザを作成する
    Serializer<Foo>serializer = new Serializer<>(fooAdapter);
    
    // JSON文字列
    String json;

    // オブジェクトを作成し、JSON文字列に変換
    {
      Foo foo = new Foo();
      foo.list.add(new Bar1());
      foo.list.add(new Bar2());
      json = serializer.serialize(foo);
      System.out.println("" + json);     
    }
    
    // JSON文字列からオブジェクトを再構築
    {
      Foo foo = serializer.deserialize(json);
      assertEquals("testString1,null,1,2,3,4", foo.toString());
    }
  }

  public static class Foo {
    List<Bar>list = new ArrayList<>();
    @Override
    public String toString() {
      return list.stream().map(b->b.toString()).collect(Collectors.joining(","));
    }
  }
  
  public static abstract class Bar {
    int a = 1;
  }
  public static class Bar1 extends Bar {    
    int b = 2;
    @Override
    public String toString() {
      return "Bar1 " + a + "," + b;
    }
  }
  public static class Bar2 extends Bar {    
    int c = 3;
    @Override
    public String toString() {
      return "Bar2 " + a + "," + c;
    }
  }
}
