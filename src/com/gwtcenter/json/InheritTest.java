package com.gwtcenter.json;

import static org.junit.Assert.*;

import org.junit.*;

/**
 * 上位オブジェクトとして直列化した後で、それを下位オブジェクトとして復帰させるテスト。
 * 当然ながらこれは成功する。
 * @author ysugimura
 */
public class InheritTest {


  Serializer<Foo>fooSerializer;
  Serializer<Bar>barSerializer;
  Serializer<Diff>diffSerializer;
  Serializer<Insane>insaneSerializer;
  
  Foo foo;
  
  @Before
  public void before() {

    fooSerializer = new Serializer<>(Foo.class);
    barSerializer = new Serializer<>(Bar.class);
    diffSerializer = new Serializer<>(Diff.class);
    insaneSerializer = new Serializer<>(Insane.class);    
    
    foo = new Foo();
    foo.a = 123;
    foo.b = "abc";
  }
  
  /**
   * 元のクラスの派生クラスで受け取る。全く問題無し
   */
  @Test
  public void inheritTest() {    
    assertEquals("{\"a\":123,\"b\":\"abc\"}", fooSerializer.serialize(foo));
    byte[]bytes = fooSerializer.serializeGzip(foo);
    Bar bar = barSerializer.deserializeGzip(bytes);
    assertEquals(123, bar.a());
    assertEquals("abc", bar.b());
  }

  /**
   * 無関係なクラスで受け取る。問題なし
   */
  @Test
  public void diffTest() {
    byte[]bytes = fooSerializer.serializeGzip(foo);
    Diff diff = diffSerializer.deserializeGzip(bytes);
    assertEquals(123, diff.a);
    assertEquals("abc", diff.b);
  }

  /**
   * フィールドタイプの異なる無関係なクラスで受け取る。例外発生。
   */
  @Test
  public void insaneTest() {    
    byte[]bytes = fooSerializer.serializeGzip(foo);
    try {
      insaneSerializer.deserializeGzip(bytes);
      fail();
    } catch (JsonException ex) {
      
    }    
  }  
  
  public static class Foo  {
    private int a;
    private String b;
    
    public int a() { return a; }
    public String b() { return b; }
  }
  
  public static class Bar extends Foo {    
  }
  
  public static class Diff {
    private int a;
    private String b;
    
  }
  
  public static class Insane {
    public int a;
    public int b;
  }
}
