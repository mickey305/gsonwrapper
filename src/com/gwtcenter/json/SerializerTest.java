package com.gwtcenter.json;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.google.gson.reflect.*;

/**
 * 直列化及び復帰のテスト
 * @author ysugimura
 */
public class SerializerTest {


  
  /**
   * 抽象クラスFooを直列化する。実際にはFooの下位クラスFooOneが直列化される。
   */
  @Test
  public void トップレベルの抽象クラス() {
    Serializer<Foo>serializer = new Serializer<>(FooAdapter.INSTANCE);
    Foo in = new FooOne();
    String json = serializer.serialize(in);
    //PrintJsonForTest.printJson(json);
    assertEquals("{\"T\":\"FooOne\",\"D\":{\"one\":1}}", json);
    
    Foo out = serializer.deserialize(json);
    assertTrue(out instanceof FooOne);
  }
  
  @Test
  public void トップレベルの抽象クラスJava文字列化() {
    Serializer<Foo>serializer = new Serializer<Foo>(FooAdapter.INSTANCE);
    Foo in = new FooOne();
    String json = serializer.serializeToJavaString(in);
    assertEquals("\"{\\\"T\\\":\\\"FooOne\\\",\\\"D\\\":{\\\"one\\\":1}}\"", json);
  }
  
  @Test
  public void トップレベルの抽象クラス_gzip() {
    Serializer<Foo>serializer = new Serializer<Foo>(FooAdapter.INSTANCE);
    Foo in = new FooOne();
    Foo out = serializer.deserializeGzip(serializer.serializeGzip(in));
    assertTrue(out instanceof FooOne);
  }
  
  
  /**
   * トップレベルオブジェクトとしてVariousオブジェクト。
   * このオブジェクトの中には様々なフィールドがあり、その一つにFooがある。
   */
  @Test
  public void 様々なオブジェクトを含むオブジェクト() {
    Serializer<Various> serializer = new Serializer<>(VariousAdapter.INSTANCE);
    
    Various in = new Various(123, "abc", new FooOne());
    String json = serializer.serialize(in);
    //PrintJsonForTest.printJson(json);
    assertEquals("{\"i\":123,\"s\":\"abc\",\"foo\":{\"T\":\"FooOne\",\"D\":{\"one\":1}}}", json);
    
    Various out = serializer.deserialize(json);
    assertEquals(123, out.i);
    assertEquals("abc", out.s);
    assertTrue(out.foo instanceof FooOne);
  }
  
  @Test
  public void 様々なオブジェクトを含むオブジェクト_gzip() {
    Serializer<Various> serializer = new Serializer<>(VariousAdapter.INSTANCE);    
    Various in = new Various(123, "abc", new FooOne());
    Various out = serializer.deserializeGzip(serializer.serializeGzip(in));
    assertEquals(123, out.i);
    assertEquals("abc", out.s);
    assertTrue(out.foo instanceof FooOne);
  }

  /**
   * トップレベルのArrayList。要素はVarious
   */
  @Test
  public void トップレベルのジェネリックスリスト() {
    
    Serializer<ArrayList<Various>> serializer = new Serializer<>(
        VariousArrayListAdapter.INSTANCE);

    ArrayList<Various>in = new ArrayList<Various>();
    Various in0 = new Various(123, "abc", new FooOne());
    in.add(in0);
    
    String json = serializer.serialize(in);
    
    //PrintJsonForTest.printJson(json);
    assertEquals("[{\"i\":123,\"s\":\"abc\",\"foo\":{\"T\":\"FooOne\",\"D\":{\"one\":1}}}]", json);
    
    ArrayList<Various>out = serializer.deserialize(json);
    Various out0 = out.get(0);
    assertEquals(123, out0.i);
    assertEquals("abc", out0.s);
    assertTrue(out0.foo instanceof FooOne);
  }
  
  @Test
  public void トップレベルのジェネリックスリスト_gzip() {
    
    Serializer<ArrayList<Various>> serializer = new Serializer<>(
        VariousArrayListAdapter.INSTANCE);

    ArrayList<Various>in = new ArrayList<Various>();
    Various in0 = new Various(123, "abc", new FooOne());
    in.add(in0);
    
    ArrayList<Various>out = serializer.deserializeGzip(serializer.serializeGzip(in));
    Various out0 = out.get(0);
    assertEquals(123, out0.i);
    assertEquals("abc", out0.s);
    assertTrue(out0.foo instanceof FooOne);
  }
  
  /**
   * トップレベルのハッシュマップ。キー要素はVarious
   */
  @Test
  public void トップレベルのジェネリックスマップ() {
    HashMap<Various, String>in = new HashMap<>();
    in.put(new Various(1, "a", new FooOne()), "one");
    in.put(new Various(2, "b", new FooTwo()), "two");
     
    Serializer<HashMap<Various, String>>serializer = 
        new Serializer<>(VariousHashMapAdapter.INSTANCE);
    String json = serializer.serialize(in);
    
    //PrintJsonForTest.printJson(json);    
    assertEquals("[[{\"i\":1,\"s\":\"a\",\"foo\":{\"T\":\"FooOne\",\"D\":{\"one\":1}}},\"one\"],[{\"i\":2,\"s\":\"b\",\"foo\":{\"T\":\"FooTwo\",\"D\":{\"two\":2}}},\"two\"]]",
        json);
    
    HashMap<Various, String>out = serializer.deserialize(json);
    assertEquals("one", out.get(new Various(1, "a", new FooOne())));
    assertEquals("two", out.get(new Various(2, "b", new FooTwo())));
  }
  
  @Test
  public void トップレベルのジェネリックスマップ_gzip() {
    HashMap<Various, String>in = new HashMap<>();
    in.put(new Various(1, "a", new FooOne()), "one");
    in.put(new Various(2, "b", new FooTwo()), "two");
     
    Serializer<HashMap<Various, String>>serializer = 
        new Serializer<>(VariousHashMapAdapter.INSTANCE);
    HashMap<Various, String>out = serializer.deserializeGzip(serializer.serializeGzip(in));
    assertEquals("one", out.get(new Various(1, "a", new FooOne())));
    assertEquals("two", out.get(new Various(2, "b", new FooTwo())));
  }
  
  @Test
  public void nullの入出力() {
    Serializer<Foo> serializer = new Serializer<>(FooAdapter.INSTANCE);   
    assertNull(serializer.serialize(null));
    assertNull(serializer.deserialize(null));
    assertNull(serializer.serializeGzip(null));
    assertNull(serializer.deserializeGzip(null));
  }
  
  @Test
  public void バイナリテスト1() {
    Serializer<Binary>serializer = new Serializer<>(Binary.class);
    Binary b = new Binary();
    b.b = new byte[] { 1, 2, 127, -125 };
    String json = serializer.serialize(b);    
    assertEquals("{\"b\":[1,2,127,-125]}", json);
  }
  
  @Test
  public void バイナリテスト2() {
    Serializer<byte[]>serializer = new Serializer<>(byte[].class);
    byte[]b = new byte[] { 1, 2, 127, -125 };
    String json = serializer.serialize(b);    
    assertEquals("[1,2,127,-125]", json);
  }
  
  // HashMap of Various ///////////////////////////////////////////////////////

  /**
   * {@link Various}をキーとしたマップ
   * @author ysugimura
   *
   */
  public static class VariousHashMapAdapter extends AbstractAdapter<HashMap<Various, String>> {
    public static VariousHashMapAdapter INSTANCE = new VariousHashMapAdapter();
    
    public VariousHashMapAdapter() {
      super(new TypeToken<HashMap<Various, String>>() {});
      addSubAdapter(FooAdapter.INSTANCE);
    }
  }
  
  // ArrayList of Various /////////////////////////////////////////////////////

  /**
   * {@link Various}のリスト
   * @author ysugimura
   *
   */
  public static class VariousArrayListAdapter extends AbstractAdapter<ArrayList<Various>> {
    public static VariousArrayListAdapter INSTANCE = new VariousArrayListAdapter();
    
    public VariousArrayListAdapter() {
      super(new TypeToken<ArrayList<Various>>() {});
      addSubAdapter(FooAdapter.INSTANCE);
    }
  }

  // Various //////////////////////////////////////////////////////////////////
  
  /**
   * 内部に様々なオブジェクトを持つオブジェクト
   * @author ysugimura
   */
  public static class Various {
    int i;
    String s;
    Foo foo;

    public Various(int i, String s, Foo foo) {
      this.i = i;
      this.s = s;
      this.foo = foo;
    }
    
    @Override
    public int hashCode() {
      return i + s.hashCode() + foo.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
      
      Various that = (Various)o;
      return 
        this.i == that.i &&
        this.s.equals(that.s) &&
        this.foo.equals(that.foo);  
    }
  }

  public static class VariousAdapter extends AbstractAdapter<Various> {    
    public static VariousAdapter INSTANCE = new VariousAdapter();    
    public VariousAdapter() {
      super(Various.class);
      this.addSubAdapter(FooAdapter.INSTANCE);
    }
  }

  // Foo //////////////////////////////////////////////////////////////////////
  
  /**
   * 抽象クラス
   * @author ysugimura
   */
  public abstract static class Foo {    
  }
  
  public static class FooOne extends Foo {    
    int one = 1;

    @Override
    public int hashCode() {
      return one;
    }
    
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof FooOne)) return false;
      FooOne that = (FooOne)o;
      return this.one == that.one;
    }
  }
  
  public static class FooTwo extends Foo {    
    int two = 2;
    
    @Override
    public int hashCode() {
      return two;
    }
    
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof FooTwo)) return false;
      FooTwo that = (FooTwo)o;
      return this.two == that.two;
    }
  }
  
  public static class FooAdapter extends MultiTypeAdapter<Foo> {    
    public static FooAdapter INSTANCE = new FooAdapter();    
    public FooAdapter() {
      super(Foo.class);
      this.addSubClass(FooOne.class);
      this.addSubClass(FooTwo.class);
    }
  }

  public static class Binary {
    public byte[]b;
  }
}
