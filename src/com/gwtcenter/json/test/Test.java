package com.gwtcenter.json.test;

import java.util.*;

import com.google.gson.reflect.*;

public class Test {

  public static void main(String[]args) {
    /*
    TypeToken<Foo> token = TypeToken.get(Foo.class);
    System.out.println("" + token + "," + token.equals(new TypeToken<Foo>() {}));
    */
    TypeToken<ArrayList<Foo>>token = new TypeToken<ArrayList<Foo>>() {};
    System.out.println(token.getRawType());
    System.out.println(token.getType());
  }
  
  public static class Foo {
    
  }
}
