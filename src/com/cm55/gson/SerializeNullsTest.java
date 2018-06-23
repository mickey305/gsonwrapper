package com.cm55.gson;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

/**
 * nullがシリアライズされることのテスト
 * 特にHashMapの値がシリアライズされないと困る。
 * @author ysugimura
 */
public class SerializeNullsTest {

  Serializer<Sample>serializer;
  
  @Before
  public void before() {
    serializer = new Serializer<>(new HandlerBuilder<>(Sample.class).build());
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
