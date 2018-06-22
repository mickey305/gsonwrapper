package com.gwtcenter.json;

import static org.junit.Assert.*;

import org.junit.*;

import com.google.inject.*;

public class SpecialFloatingPointValuesTest {

  Serializer<Sample> serializer;

  @Before
  public void before() {
    Injector i = Guice.createInjector();
    serializer = i.getInstance(SerializerFactory.class).create(
        new BaseAdapter<Sample>(Sample.class));
  }

  @Test
  public void test() {
    {
      Sample in = new Sample(Float.NaN, Double.NaN);
      String json = serializer.serialize(in);
      // PrintJsonForTest.printJson(json);
      assertEquals("{\"f\":NaN,\"d\":NaN}", json);
      Sample out = serializer.deserialize(json);
      assertTrue(
          Float.isNaN(out.f) && Double.isNaN(out.d)
      );
      //assertTrue(in.equals(serializer.deserialize(json))); NaNは比較できないらしい
    }

    {
      Sample in = new Sample(Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
      String json = serializer.serialize(in);
      // PrintJsonForTest.printJson(json);
      assertEquals("{\"f\":Infinity,\"d\":Infinity}", json);
      assertTrue(in.equals(serializer.deserialize(json)));
    }

    {
      Sample in = new Sample(Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
      String json = serializer.serialize(in);
      // PrintJsonForTest.printJson(json);
      assertEquals("{\"f\":-Infinity,\"d\":-Infinity}", json);
      assertTrue(in.equals(serializer.deserialize(json)));
    }
  }

  public static class Sample {
    float f;
    double d;

    public Sample(float f, double d) {
      this.f = f;
      this.d = d;
    }
    
    @Override
    public boolean equals(Object o) {
      Sample that = (Sample)o;
      return this.f == that.f && this.d == that.d;
    }
  }
}
