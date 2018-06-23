package com.cm55.gson;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.google.gson.*;


/**
 * このシステムでは{@link GsonBuilder}に対してenableComplexMapKeySerialization()
 * の指定をしてある。この指定を行わないと、マップのキーは、常にそのオブジェクトのtoString()で返される
 * 文字列になってしまう。
 * 指定を行うと、キーのオブジェクトも通常と同じように直列化される。
 * @author ysugimura
 */
public class EnableComplexMapKeySerializationTest {
  
  Serializer<TheObject>serializer;
  
  @Before
  public void before() { 
    serializer = new Serializer<TheObject>(TheObjectHandler.INSTANCE);
  }
  
  
  @Test
  public void test() {
    String json;
    
    {
      TheObject object = new TheObject();
      Foo foo = new Foo();
      object.map1.put(foo, "2222");
      foo.a = new int[] { 1, 2, 3 };
      foo.b = new int[] { 5, 6, 7, 8 };
      object.map2.put(Bar.A, "3333");
      
      json = serializer.serialize(object);
    
      //PrintJsonForTest.printJson(json);
      assertEquals(
        "{\"map1\":[[{\"a\":[1,2,3],\"b\":[5,6,7,8]},\"2222\"]],\"map2\":{\"A\":\"3333\"}}", 
        json
     );
    }
    
    {
      TheObject object = serializer.deserialize(json);
      Foo foo = (Foo)object.map1.keySet().toArray()[0];
           
      assertArrayEquals(new int[] { 1, 2, 3 }, foo.a);
      assertArrayEquals(new int[] { 5, 6, 7, 8 }, foo.b);
      
      Bar bar = (Bar)object.map2.keySet().toArray()[0];
      assertEquals(Bar.A, bar);
    }    
  }

  public static class TheObjectHandler  {
    public static final Handler<TheObject> INSTANCE = new HandlerBuilder<>(TheObject.class).build();
    
  }
  
  public static class TheObject {
    HashMap<Foo, String>map1 = new HashMap<Foo, String>();
    HashMap<Bar, String>map2 = new HashMap<Bar, String>();
  }
  
  public static class Foo {
    int[]a;
    int[]b;
  }
  
  public static enum Bar {
    A(1),
    B(2);
    public final int value;
    private Bar(int value) {
      this.value = value;
    }
    @Override
    public String toString() {
      return "" + value;
    }
  }
}
