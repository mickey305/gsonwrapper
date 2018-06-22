package com.gwtcenter.json;

import java.util.*;

import com.google.gson.*;

public class Json {

  
  public static <T extends JElement>T get(String jsonString) {      
    Gson gson = new Gson();
    return wrap(gson.fromJson(jsonString, JsonObject.class));      
  }
  
  
  public static enum JType {
    OBJECT,
    ARRAY,
    PRIMITIVE,
    NULL;
  }
  
  @SuppressWarnings("unchecked")
  public static  <T extends JElement>T wrap(JsonElement element) {
    if (element instanceof JsonObject) {
      return (T)new JObjectImpl((JsonObject)element);
    }
    if (element instanceof JsonArray) {
      return (T)new JArrayImpl((JsonArray)element);
    }
    if (element instanceof JsonPrimitive) {
      return (T)new JPrimitiveImpl((JsonPrimitive)element);
    }
    if (element instanceof JsonNull) {
      return (T)new JNullImpl((JsonNull)element);
    }
    throw new RuntimeException();
  }
  
  public interface JElement {    
    public JType getType();
    public JPrimitive asPrimitive();
    public JArray asArray();
    public JObject asObject();
    
    /**
     * この要素をJPrimitiveに変換し、更にその文字列を取得する。
     * ただし、JNullの場合や、文字列が空文字列の場合にはnullを返す。
     * それ以外の場合は例外が発生する
     * @return
     */
    public String asString();
    
    /**
     * この要素をJPrimitiveに変換し、更にその文字列をintに変換して取得する
     * ただし、JNullの場合や、文字列が空文字列の場合にはnullを返す。
     * それ以外の場合は例外が発生する
     * @return
     */
    public Integer asInteger();
    
    public String getJson();
  }
  
  public interface JObject extends JElement {
    public JType getType();
    public <T extends JElement>T get(String fieldName);
    public Set<Map.Entry<String, JElement>>entrySet();
  }
  
  public interface JArray extends JElement,Iterable<JElement> {
    public int size();
  }

  public interface JPrimitive extends JElement {
    public boolean isEmpty();
    public String asString();
    public Integer asInteger();
  }

  public interface JNull extends JElement {
    
  }
  
  public static class JElementImpl<T extends JsonElement> implements JElement {
    private JType type;
    protected T element;
    private JElementImpl(JType type, T element) {
      this.type = type;
      this.element = element;
    }
    public JType getType() {
      return type;
    }
    public JPrimitive asPrimitive() { return (JPrimitive)this; }
    public JArray asArray() { return (JArray)this; }
    public JObject asObject() { return (JObject)this; }
    public String asString() {
      if (getType() == JType.NULL) return null; 
      String s = asPrimitive().asString(); 
      if (s.length() == 0) return null;
      return s;
    }
    public Integer asInteger() {
      String s = asString();
      if (s == null) return null;
      return Integer.parseInt(s);
    }
    
    public String getJson() {
      return element.toString();
    }
  }
  
  public static class JObjectImpl extends JElementImpl<JsonObject> implements JObject {
    private Map<String, JElement>map;
    private JObjectImpl(JsonObject object) {
      super(JType.OBJECT, object);
      map = new HashMap<String, JElement>();
      for (Map.Entry<String, JsonElement>e: element.entrySet()) {
        map.put(e.getKey(), wrap(e.getValue()));
      }      
    }
    
    @Override
    public <T extends JElement> T get(String fieldName) {
      return (T)map.get(fieldName);
    }
    
    public Set<Map.Entry<String, JElement>>entrySet() {
      return map.entrySet();
    }
  }
  
  public static class JArrayImpl extends JElementImpl<JsonArray> implements JArray {
    private List<JElement>list;
    private JArrayImpl(JsonArray array) {
      super(JType.ARRAY, array);
      list = new ArrayList<JElement>();
      for (JsonElement e: element) {
        list.add(wrap(e));
      }      
    }

    @Override
    public int size() {
      return list.size();
    }
    
    @Override
    public Iterator<JElement> iterator() {
      return list.iterator();
    }

  }
  
  public static class JPrimitiveImpl extends JElementImpl<JsonPrimitive> implements JPrimitive  {
    private JPrimitiveImpl(JsonPrimitive primitive) {
      super(JType.PRIMITIVE, primitive);
    }
    
    /**
     * JSON用のダブルクォートされた文字列ではなく、中身の文字列を取得する
     */
    @Override
    public String asString() {
      return element.getAsString();
    }

    @Override
    public boolean isEmpty() {
      return asString().length() == 0;
    }

    @Override
    public Integer asInteger() {
      return Integer.parseInt(asString());
    }
  }
  
  public static class JNullImpl extends JElementImpl<JsonNull> implements JNull {
    private JNullImpl(JsonNull n) {
      super(JType.NULL, n);
    }
  }
}
