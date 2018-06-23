package github_gsonwrapper;

import static org.junit.Assert.*;

import java.util.*;
import java.util.stream.*;

import org.junit.*;

import com.cm55.gson.*;

public class Sample2 {
  
  @Test
  public void test() {
    
    // Fooに対するシリアライザを作成する
    Serializer<Foo>serializer = new Serializer<>(fooHandler);
    
    // JSON文字列
    String json;

    // オブジェクトを作成し、JSON文字列に変換
    {
      Foo foo = new Foo();
      foo.list.add(new Bar1());
      foo.list.add(new Bar2());
      json = serializer.serialize(foo);
      assertEquals("{\"list\":[{\"T\":\"Bar1\",\"D\":{\"b\":2,\"a\":1}},{\"T\":\"Bar2\",\"D\":{\"c\":3,\"a\":1}}]}", json);
    }
    
    // JSON文字列からオブジェクトを再構築
    {
      Foo foo = serializer.deserialize(json);
      assertEquals("Bar1 1,2,Bar2 1,3", foo.toString());
    }
  }

  static Handler<Bar>barHandler = new MultiHandlerBuilder<>(Bar.class).addSubClasses(Bar1.class, Bar2.class).build();
  static Handler<Foo>fooHandler = new HandlerBuilder<>(Foo.class).addSubHandler(barHandler).build();
  
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
