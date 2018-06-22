package com.gwtcenter.json;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import com.google.inject.*;

/**
 * nullがシリアライズされることのテスト
 * 特にHashMapの値値がシリアライズされないと困る。
 * @author ysugimura
 */
public class SerializeNullsTest {

  Serializer<Sample>serializer;
  
  @Before
  public void before() {
    Injector i = Guice.createInjector();
    serializer = i.getInstance(SerializerFactory.class).create(new BaseAdapter<Sample>(Sample.class));
  }
  
  @Test
  public void test() {
    Sample sample = new Sample();
    sample.b.put(123,  null);
    sample.b.put(null, "abc");
    String json = serializer.serialize(sample);
    //PrintJsonForTest.printJson(json);
    assertEquals("{\"a\":null,\"b\":{\"null\":\"abc\",\"123\":null}}", json);
    
  }

  public static class Sample {
    String a;
    HashMap<Integer, String>b = new HashMap<Integer, String>();
  }
}
