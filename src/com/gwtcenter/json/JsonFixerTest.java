package com.gwtcenter.json;

import static org.junit.Assert.*;

import org.junit.*;

import com.google.inject.*;
import com.gwtcenter.json.JsonFixer.*;


public class JsonFixerTest {

  JsonFixer fixer;
  
  @Before
  public void before() {
    Injector i = Guice.createInjector();
    fixer = i.getInstance(JsonFixer.class);
  }
  
  @Test
  public void toArray() {
    String input = toDouble(
        "{'orders':{'commodities':{'commodity':{'name':'sample'}}}}");
    String fixed = fixer.fix(
      input,
      new FixRoot(
        new FixNone("orders", 
          new FixNone("commodities",
            new ForceArray("commodity")
          )
        )
    ));
    //ystem.out.println(fixed);
    assertEquals(
      "{'orders':{'commodities':{'commodity':[{'name':'sample'}]}}}",
      toSingle(fixed)
    );
    //ystem.out.println(singleQuote(fixed));
  }
  
  @Test
  public void asIs() {
    String input = toDouble(
        "{'orders':{'commodities':{'commodity':[{'name':'sample'},{'name':'sample'}]}}}");
    String fixed = fixer.fix(
      input,
      new FixRoot(
        new FixNone("orders", 
          new FixNone("commodities",
            new ForceArray("commodity")
          )
        )
    ));
    assertEquals(
        input,
        fixed
    );
    //ystem.out.println(singleQuote(fixed));
  }
  
  @Test
  public void dropEmptyArray() {
    String input = toDouble("{\n" +
      "'created_date': '20160322164031',\n" +
      "'option': [\n" +
      "],\n" +
      "'modified_date': '2016'\n" +
    "}");
    String fixed = fixer.fix(
        input,
        new FixRoot(
          new DropEmptyArray("option")
        )
      );
    assertEquals("{'created_date':'20160322164031','modified_date':'2016'}", toSingle(fixed));

  }
  
  @Test
  public void dropEmptyArray2() {
    String input = toDouble("{'option': []}");
    String fixed = fixer.fix(
        input,
        new FixRoot(
          new DropEmptyArray("option")
        )
      );
    assertEquals("{}", toSingle(fixed));
  }
  
  @Test
  public void changeFieldName() {
    String input = toDouble(
        "{'orders':{'commodities':{'commodity':{'name':'sample'}}}}");
    String fixed = fixer.fix(
      input,
      new FixRoot(
        new FixNone("orders", 
          new ChangeFieldName("commodities", "commodities_item",
            new ForceArray("commodity")
          )
        )
    ));
    assertEquals(
      "{'orders':{'commodities_item':{'commodity':[{'name':'sample'}]}}}",
      toSingle(fixed)
    );
  }
  
  private String toDouble(String s) {
    return s.replace('\'',  '"');
  }
  
  private String toSingle(String s) {
    return s.replace('"',  '\'');
  }
}
