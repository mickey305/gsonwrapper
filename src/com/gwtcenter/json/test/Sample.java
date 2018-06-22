package com.gwtcenter.json.test;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.stream.*;

public class Sample {

  public static void main(String[]args) {
    GsonBuilder builder = new GsonBuilder();
    builder.enableComplexMapKeySerialization();
    builder.serializeNulls();
    builder.serializeSpecialFloatingPointValues();
    
    builder.registerTypeAdapterFactory(new MyTypeAdapterFactory());
    
    Gson gson = builder.create();
    
    String json = gson.toJson(new FooOne(), Foo.class);
    System.out.println(json);
    
    Foo out = gson.fromJson(json, Foo.class);
    System.out.println(out.getClass());
    System.out.println(out.bar.getClass());
  }
  
  public static class MyTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T>typeToken) {
      
      System.out.println("checking for " + typeToken);

      if (typeToken.getType().equals(Foo.class)) {
        
        HashMap<Class, TypeAdapter>subClassAdapters = 
          new HashMap<Class, TypeAdapter>();
      
        subClassAdapters.put(FooOne.class, 
          gson.getDelegateAdapter(this, new TypeToken<FooOne>() {}));
        subClassAdapters.put(FooTwo.class,
          gson.getDelegateAdapter(this,  new TypeToken<FooTwo>() {}));
      
      
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
      
        Context ctx = new Context(subClassAdapters, elementAdapter);
     

        System.out.println("returning for " + typeToken);
        return (TypeAdapter<T>)new FooTypeAdapter(ctx);
      }

      
      return null;
    }    
  }
  
  public static class MyTypeAdapterFactory2 implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T>typeToken) {
      
      System.out.println("checking for " + typeToken);


      if (typeToken.getType().equals(Bar.class)) {
        
        HashMap<Class, TypeAdapter>subClassAdapters = 
          new HashMap<Class, TypeAdapter>();
      
        subClassAdapters.put(BarOne.class, 
          gson.getDelegateAdapter(this, new TypeToken<BarOne>() {}));
        subClassAdapters.put(BarTwo.class,
          gson.getDelegateAdapter(this,  new TypeToken<BarTwo>() {}));
      
      
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
      
        Context ctx = new Context(subClassAdapters, elementAdapter);
     

        System.out.println("returning for " + typeToken);
        return (TypeAdapter<T>)new BarTypeAdapter(ctx);
      }
      
      return null;
    }    
  }
  
  public static class Context {
    HashMap<Class, TypeAdapter>subClassAdapters;
    TypeAdapter elementAdapter;
    public Context(HashMap<Class, TypeAdapter>subClassAdapters, TypeAdapter elementAdapter) {
      this.subClassAdapters = subClassAdapters;
      this.elementAdapter = elementAdapter;
    }
  }
  
  public static class FooTypeAdapter extends TypeAdapter<Foo> {

    Context ctx;
    
    public FooTypeAdapter(Context ctx) {
      this.ctx = ctx;
    }
    
    @Override
    public void write(JsonWriter writer, Foo value)
        throws IOException {
      System.out.println("write " + value.getClass());
 
      writer.beginObject();
      writer.name("T").value(value.getClass().getName());
      writer.name("D");
      
      JsonElement tree = ctx.subClassAdapters.get(value.getClass()).toJsonTree(value);
      ctx.elementAdapter.write(writer, tree);
      // TODO Auto-generated method stub

      writer.endObject();
    }
    
    @Override
    public Foo read(JsonReader reader) throws IOException {
      
      System.out.println("start");
      
      reader.beginObject();
      reader.nextName().equals("T");
      System.out.println("start");
      
      
      String className = reader.nextString();
      System.out.println("start");

      
      reader.nextName().equals("D");
      
      JsonElement tree = (JsonElement)ctx.elementAdapter.read(reader);
      Foo foo;
      try {
        foo = (Foo)ctx.subClassAdapters.get(Class.forName(className)).fromJsonTree(tree);
      } catch (ClassNotFoundException ex) {        
        throw new RuntimeException(ex);
      }
      
      reader.endObject();
      
      return foo;
    }    
  }
  
  public static class BarTypeAdapter extends TypeAdapter<Bar> {

    Context ctx;
    
    public BarTypeAdapter(Context ctx) {
      this.ctx = ctx;
    }
    
    @Override
    public void write(JsonWriter writer, Bar value)
        throws IOException {
      System.out.println("write " + ctx + "," + value.getClass());
 
      writer.beginObject();
      writer.name("T").value(value.getClass().getName());
      writer.name("D");
      
      JsonElement tree = ctx.subClassAdapters.get(value.getClass()).toJsonTree(value);
      ctx.elementAdapter.write(writer, tree);
      // TODO Auto-generated method stub

      writer.endObject();
    }
    
    @Override
    public Bar read(JsonReader reader) throws IOException {
      System.out.println("read");
      reader.beginObject();
      reader.nextName().equals("T");
      String className = reader.nextString();
      
      reader.nextName().equals("D");
      
      JsonElement tree = (JsonElement)ctx.elementAdapter.read(reader);
      Bar bar;
      try {
        bar = (Bar)ctx.subClassAdapters.get(Class.forName(className)).fromJsonTree(tree);
      } catch (ClassNotFoundException ex) {        
        throw new RuntimeException(ex);
      }
      
      reader.endObject();
      
      return bar;
    }    
  }
  
  public static class Foo {
    int foo = 0;
    Bar bar = new BarOne();
  }
  
  public static class FooOne extends Foo {
    int one = 1;
  }
  
  public static class FooTwo extends Foo {
    int two = 2;    
  }
  
  public static class Bar {
    int bar = 0;    
  }
  
  public static class BarOne extends Bar {
    int one = 1;
  }
  
  public static class BarTwo extends Bar {
    int two = 2;
  }
}
