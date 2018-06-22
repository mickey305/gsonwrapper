package com.cm55.gson;

import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.reflect.*;

public class MultiTypeAdapterTest {

  @Before
  public void before() {

  }
  
  @Test
  public void invalidTest() {
    try {
      Serializer<Bar>serializer = new Serializer<>(InvalidAdapter.INSTANCE);
      fail();
    } catch (Exception ex) {
      
    }
  }
  
  @Test
  public void TypeTokenのtest() {
    TypeToken<Foo>fooToken = new TypeToken<Foo>() {};
    assertEquals(fooToken.getRawType(), Foo.class);    
  }
  
  @Test
  public void test0() {
    Serializer<Bar>serializer = new Serializer<>(BarAdapter.INSTANCE);
    Bar in = new BarTwo();
    String json = serializer.serialize(in);
    //PrintJsonForTest.printJson(json);
    assertEquals("{\"T\":\"BarTwo\",\"D\":{\"b\":2}}", json);
    BarTwo out = (BarTwo)serializer.deserialize(json);

    assertEquals(2, out.b);
  }
  
  @Test
  public void test1() {
    Serializer<Foo>serializer = new Serializer<>(FooAdapter.INSTANCE);
    Foo in = new FooTwo();
    String json = serializer.serialize(in);
    //PrintJsonForTest.printJson(json);    
    assertEquals("{\"T\":\"FooTwo\",\"D\":{\"two\":2}}", json);
    Foo out = serializer.deserialize(json);
    assertEquals(2, ((FooTwo)out).two);
    
  }
  
  //@Test
  public void test1_1() {
    String json = "{\"T\":\"FooFour\",\"D\":{\"two\":2}}";
    Serializer<Foo>serializer = new Serializer<Foo>(FooAdapter.INSTANCE);
    
    assertNull(serializer.deserialize(json));
    
    serializer.setNullIfClassNotFound(false);
    try {
      serializer.deserialize(json);
      fail();
    } catch (JsonClassNotFoundException ex) {      
    }
  }
  
  @Test 
  public void 抽象クラスが入れ子になっているケース() {
    Serializer<Foo>serializer = new Serializer<Foo>(FooAdapter.INSTANCE);
    Foo in = new FooThree(new BarTwo());
    String json = serializer.serialize(in);
    //PrintJsonForTest.printJson(json);
    
    FooThree out = (FooThree)serializer.deserialize(json);
    BarTwo out2 = (BarTwo)out.value;
    assertEquals(2, out2.b);
    
  }
  
  @Test
  public void test3() {
    MultiTypeAdapter<Bar> adapter = new MultiTypeAdapter<Bar>(Bar.class);
    adapter.addSubClass("one",  BarOne.class);
    
    // 同じクラスを別名称で登録しようとしている
    try {
      adapter.addSubClass("two",  BarOne.class);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
    
    // 同じ名称で、別のクラスを登録しようとしている。
    try {
      adapter.addSubClass("one", BarTwo.class);
      fail();
    } catch (IllegalArgumentException ex) {      
    }
    
    // 同じ名称、同じクラスで登録
    adapter.addSubClass("one", BarOne.class);
    
    assertEquals(1, adapter.subClassCount());
  }
  
  public static class FooAdapter extends MultiTypeAdapter<Foo> {
    public static final FooAdapter INSTANCE = new FooAdapter();
    public FooAdapter() {
      super(Foo.class);
      addSubClass(new TypeToken<FooOne>() {});
      addSubClass(new TypeToken<FooTwo>() {});
      addSubClass(new TypeToken<FooThree>() {});
      
      addSubAdapter(BarAdapter.INSTANCE);
      addSubAdapter(BarAdapter.INSTANCE); // duplicated registration is OK
    }
  }
  
  public static class Foo {    
  }
  
  public static class FooOne extends Foo {
    int one = 1;
  }
  
  public static class FooTwo extends Foo {
    int two = 2;
  }
  
  public static class FooThree extends Foo {
    Bar value;
    public FooThree(Bar value) {
      this.value = value;
    }
  }
  
  public static class InvalidAdapter extends MultiTypeAdapter<Bar> {
    public static final InvalidAdapter INSTANCE = new InvalidAdapter();
    public InvalidAdapter() {
      super(Bar.class);
    }
  }
  
  public static class BarAdapter extends MultiTypeAdapter<Bar> {
    public static final BarAdapter INSTANCE = new BarAdapter();
    public BarAdapter() {
      super(Bar.class);
      addSubClass(BarOne.class);
      addSubClass(BarTwo.class);
    }
  }
  
  public interface Bar {
    
  }
  
  public static class BarOne implements Bar {
    public int a = 1;
  }
  
  public static class BarTwo implements Bar {
    public int b = 2;
  }
  
}
