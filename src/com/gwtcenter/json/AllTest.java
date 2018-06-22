package com.gwtcenter.json;

import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class) 
@SuiteClasses( { 
  EnableComplexMapKeySerializationTest.class,
  InheritTest.class,
  SerializeNullsTest.class,
  SerializerTest.class,
  SpecialFloatingPointValuesTest.class
})
public class AllTest {
  public static void main(String[] args) {
    JUnitCore.main(AllTest.class.getName());
  }
}