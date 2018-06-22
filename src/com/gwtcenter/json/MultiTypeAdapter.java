package com.gwtcenter.json;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.stream.*;

/**
 * JSON化するオブジェクト中のフィールドに格納されるタイプが単一でないときがある。
 * 例えば、「Sample sample;」というフィールドには、次のいずれかのオブジェクトが格納される場合がある。
 * <pre>
 * class abstract class Sample { ... }
 * class SampleA extends Sample { ... }
 * class SampleB extends Sample { ... }
 * </pre>
 * <p>
 * として、
 * </p>
 * <pre>
 * class Foo {
 *   Sample sample;
 *   Foo foo;
 * }
 * </pre>
 * <p>
 * このような場合には、フィールド値をそのまま格納してはいけない。直列化復帰の際に、SampleA、SampleBのいずれのオブジェクトであるか
 * がわからなくなるからである。直列化のときにも直列化復帰の時にも、格納されるオブジェクトについて特別な処理を行わなければならない。
 * </p>
 * <p>
 * {@link MultiTypeAdapter}は、あるクラスの代わりとなる可能性のあるすべてのクラスを定義しておき、直列化の際にも直列化復帰の際にも
 * どのクラスであるかを識別するために用いられる。具体的には、以下のように行う。
 * </p>
 * <pre>
 * class MyAdapter extends MultiTypeAdapter<Sample> {
 *   public MyAdapter() {
 *     super(Sample.class);
 *     add("a", SampleA.class);
 *     add("b", SampleB.class);
 *   }
 * }
 * </pre>
 * <h2>注意事項</h2>
 * <p>
 * ジェネリックスはサポートすることはできない。その理由としては
 * </p>
 * <ul>
 * <li>JSON側からオブジェクトがもたらされ、その直列化指示がされるが、当然ながらそれはオブジェクトのみであり、
 * 型パラメータについては何もわからない。
 * </ul>
 * @author ysugimura
 *
 * @param <T>
 */
public class MultiTypeAdapter<T> extends Adapter<T> {

  private static final boolean DEBUG = false;
  
  /** タイプフィールドマーカ */
  private static final String TYPE_FIELD = "T";

  /** データフィールドマーカ */
  private static final String DATA_FIELD = "D";
  
  /** TypeTokenのマップ */
  private TypeTokenNameMap typeTokenMap = new TypeTokenNameMap ();
  
  /** 
   * 処理対象タイプを指定する
   * @param targetType
   */
  public MultiTypeAdapter(Class<T> targetType) {
    super(targetType);
  }
  
  /**
   * 登録済のサブクラスの数
   */
  public int subClassCount() {
    return typeTokenMap.count();
  }
  
  /**
   * 登録するタイプを複数指定する。
   * 各クラスの登録名称は{@link Class#getSimpleName()}で取得される文字列になる。
   * @param classes 登録対象クラス（複数）
   */
  @SuppressWarnings("unchecked")
  public void addSubClasses(Class<? extends T>...classes){
    for (Class<? extends T> clazz : classes) {
      addSubClass(clazz);
    }    
  }

  /**
   * 登録するタイプを指定する。名称は{@link Class#getSimpleName()}となる。
   * @param clazz 対象クラス
   */
  public void addSubClass(Class<? extends T> clazz) {
    addSubClass(clazz.getSimpleName(), clazz);
  }
  
  /**
   * 登録するタイプトークンを指定する。名称はRawタイプの{@link Class#getSimpleName()}となる。
   * @param typeClass
   */
  public void addSubClass(TypeToken<? extends T>typeClass) {
    addSubClass(typeClass.getRawType().getSimpleName(), typeClass);
  }

  /** 
   * 登録するクラスと、その名称を指定する
   * @param typeName 登録名称
   * @param typeClass 登録クラス
   */
  public void addSubClass(String typeName, Class<? extends T> typeClass) {
    addSubClass(typeName, TypeToken.get(typeClass));
  }
  
  /**
   * タイプ名称とそのクラスを指定して登録する。
   * 既に登録されている場合は例外が発生する。
   * @param typeName　登録名称 JSON中に書き出されるマーカー文字列
   * @param typeToken　登録クラス 上記マーカー文字列の場合に中身とされるクラスのタイプトークン
   */
  public void addSubClass(String typeName, TypeToken<? extends T> typeToken) {    
    TypeToken<T> topType = getTargetType();
    if (!topType.getRawType().isAssignableFrom(typeToken.getRawType())) {
      throw new IllegalArgumentException(
          typeToken + " is not assignable to " + topType);
    }    
    typeTokenMap.addType(typeName, typeToken);
  }

  /**
   * Gsonビルダに登録する
   */
  protected void registerToBuilder(GsonBuilder builder) {   
    
    if (typeTokenMap.count() == 0) {
      // 間違えてこのクラスを使用した場合の例外通知
      throw new JsonException("typeTokenMap empty");
    }
    
    // 環境＝TypeAdapterFactoryをGsonに登録する
    builder.registerTypeAdapterFactory(new Environment<T>(
        this.getTargetType(),
        this.typeTokenMap.duplicate()
    ));
    
    super.registerToBuilder(builder);
  }

  /**
   * 特定のGsonオブジェクト用の実行環境
   * @author ysugimura
   *
   * @param <T>
   */
  static class Environment<T> implements TypeAdapterFactory {
    
    TypeToken<T>topType;
    TypeTokenNameMap typeTokenMap;
    Map<TypeToken<? extends T>, TypeAdapter<T>> subTypeAdapters;
    TypeAdapter<JsonElement> elementAdapter;
    
    public Environment(TypeToken<T>topType, TypeTokenNameMap typeTokenMap) {
      this.topType = topType;
      this.typeTokenMap = typeTokenMap;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C> TypeAdapter<C> create(Gson gson, TypeToken<C>typeToken) {
      if (!typeToken.equals(topType)) return null;

      // サブクラス用のTypeAdapterを取得する
      subTypeAdapters = getSubTypeAdapters(gson, this);      
      
      // JsonElement用のTypeAdapterを取得する
      elementAdapter = gson.getAdapter(JsonElement.class);      

      if (DEBUG) {
        System.out.println("returning for " + typeToken);
      }
      
      return (TypeAdapter<C>)new GsonTypeAdapter<T>(this);
    }
    
    /**
     * typeTokenMapに登録されているすべての{@link TypeToken}について、その直列化・復帰を行う
     * {@link TypeAdapter}を取得し、マップにして返す。
     * @param gson
     * @param adapterFactory
     * @return
     */
    @SuppressWarnings("unchecked")
    Map<TypeToken<? extends T>, TypeAdapter<T>> 
      getSubTypeAdapters(Gson gson, TypeAdapterFactory adapterFactory) {
      
      Map<TypeToken<? extends T>, TypeAdapter<T>>subTypeAdapters =
          new HashMap<>();

      typeTokenMap.allTypeTokens().forEach(typeToken-> {
        TypeToken<? extends T>tt = (TypeToken<? extends T>)typeToken;
        TypeAdapter<T>adapter = (TypeAdapter<T>)gson.getDelegateAdapter(adapterFactory, tt);
        subTypeAdapters.put(
          tt,
          adapter
        );
      });
      return subTypeAdapters;
    }
  }
  
  /**
   * Gson用のタイプアダプタ
   * @author ysugimura
   *
   * @param <T>
   */
  public static class GsonTypeAdapter<T> extends TypeAdapter<T> {

    /** タイプアダプタの実行環境 */
    private Environment<T> env;

    /** 実行環境を指定する */
    public GsonTypeAdapter(Environment<T> env) {
      this.env = env;
    }
    
    /**
     * オブジェクトを直列化して書き込む
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(JsonWriter writer, T value)
        throws IOException {

      // オブジェクトしか与えられないため、正確なTypeTokenを得ることは不可能。
      // このため、MultiTypeAdapterではジェネリクスは使用できない。
      TypeToken<? extends T>typeToken = 
          (TypeToken<? extends T>)TypeToken.get(value.getClass());
      
      // タイプ名称を取得
      String typeName = env.typeTokenMap.getTypeName(typeToken);

      // タイプアダプタを取得し、値をJsonElementに変換
      TypeAdapter<T> typeAdapter = env.subTypeAdapters.get(typeToken);      
      JsonElement tree = typeAdapter.toJsonTree(value); 
      
      if (DEBUG) {
        System.out.println("write " + value + "," + typeToken + "," + typeName);
      }

      // オブジェクト書き込み開始
      writer.beginObject();
      
      // 型フィールドを書き込み
      writer.name(TYPE_FIELD).value(typeName);
      
      // データフィールドを書き込み
      writer.name(DATA_FIELD);     
      env.elementAdapter.write(writer, tree);

      // オブジェクト書き込み終了
      writer.endObject();
      
      if (DEBUG) {
        System.out.println("end of write");
      }
    }
    
    /**
     * オブジェクトを復帰する。
     */
    @SuppressWarnings("unchecked")
    @Override
    public T read(JsonReader reader) throws IOException {
      
      // オブジェクトの読み出し開始
      reader.beginObject();
      
      // 型フィールド、データフィールドを読み出し
      String typeField = reader.nextName();
      if (!typeField.equals(TYPE_FIELD)) {
        // assertにしてしまうと復旧ができないので例外にする
        throw new JsonException("Invalid TYPE FIELD Marker in MultiTypeAdapter:" + typeField);
      }
      String typeName = reader.nextString();      
      String dataField = reader.nextName();
      if (!dataField.equals(DATA_FIELD)) {
        // assertにしてしまうと復旧ができないので例外にする
        throw new JsonException("Invalid DATA FIELD Marker in MultiTypeAdapter:" + dataField);
      }
      JsonElement tree = (JsonElement)env.elementAdapter.read(reader);            
      
      // オブジェクトの読み出し終了
      reader.endObject();

      // タイプ名称からTypeTokenを取得する
      TypeToken<? extends T>typeToken = 
          (TypeToken<? extends T>)env.typeTokenMap.getTypeToken(typeName);

      // タイプ名称が未登録の場合
      if (typeToken == null) throw new JsonClassNotFoundException();
      
      // TypeTokenからアダプタを取得
      TypeAdapter<T> typeAdapter = env.subTypeAdapters.get(typeToken);
      
      // アダプタにJsonElementツリーを解析させていオブジェクトを取得
      T result = (T)typeAdapter.fromJsonTree(tree);
      
      return result;
    }    
  }
}
