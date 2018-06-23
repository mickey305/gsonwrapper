package com.cm55.gson;

import java.io.*;
import java.util.zip.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

/**
 * シリアライザ
 * 
 * <h2>概要</h2>
 * <p>
 * {@link Serializer}はあるオブジェクトの直列化と復帰を行うオブジェクト、コンストラクタ引数として以下のいずれかを指定する。
 * </p>
 * <ul>
 * <li>1.対象とするオブジェクトのクラス
 * <li>2.上記クラスがジェネリッククラスの場合には、その{@link TypeToken}
 * <li>3.{@link Handler}クラス階層以下のタイプアダプタ
 * </ul>
 * <p>
 * 単純なオブジェクトの場合には、1.2.のいずれかで良いが、以下のようなケースでは、3.を選択し、
 * 特に{@link MultiHandler}を使用しなければならない。
 * </p>
 * <ul>
 * <li>対象とするオブジェクト自身、そのオブジェクト内のフィールドオブジェクト、さらにその中のフィールド
 * 等で「抽象クラス」が使用されている場合。
 * </ul>
 * <p>
 * 例えば、直列化・復帰対象オブジェクトの字面としてはFooだとしても、実際にはそのサブクラスの
 * FooOne, FooTwoのオブジェクトが格納されている場合がある。これを正しく扱うには、{@link MultiHandler}
 * を使用しなければならない。詳細は{@link MultiHandler}を参照のこと。
 * </p>
 * <h2>MultiTypeAdapterでのClassNotFound</h2>
 * <p>
 * {@link MultiHandler}を使用した直列化では、直列化後のJSON文字列に、実際のクラス名称
 * （もしくはユーザが決めた名称）が記述されている。何らかの理由で直列化後にこの名称を変更した場合、
 * 復帰時にJsonClassNotFoundExceptionを発生させている。
 * </p>
 * <p>
 * これでは面倒なので、nullIfClassNotFoundフラグがtrueの場合には単純にnullを返すようにしている。
 *　これはデフォルトでtrueである。
 * </p>
 * 
 * @author ysugimura
 *
 * @param <T>
 */
public class Serializer<T> {

  /** このシリアライザが直列化及び復帰を行う対象のクラス */
  private final TypeToken<T> typeToken;
  
  /** Gson実行オブジェクト */
  private final Gson gson;
  
  /** 復帰時にクラスが見つからない場合はnullを返す */
  private boolean nullIfClassNotFound = true;
  
  /**
   * 単純なクラスについて{@link BaseAdapter}を省略してシリアライザを作成する。
   * @param clazz 対象とするクラス
   * @return シリアライザ
   */
  public Serializer(Class<T>clazz) {
    this(new HandlerBuilder<T>(clazz).build());
  }

  /**
   * {@link TypeToken}について{@link BaseAdapter}を省略してシリアライザを作成する。
   * @param token {@link TypeToken}
   * @return シリアライザ
   */
  public Serializer(TypeToken<T>token) {
    this(new HandlerBuilder<T>(token).build());
  }
  
  /**
   * {@link Handler}を指定して{@link Serializer}オブジェクトを作成する。
   * @param handler タイプハンドラ
   */
  public Serializer(Handler<T> handler) {

    // ハンドラからTypeTokenを取得する
    typeToken = handler.getTypeToken();
    
    // GsonBuilderを作成する
    GsonBuilder builder = new GsonBuilder();    
    if (Settings.ENABLE_COMPLEX_MAP_KEY_SERIALIZATION) {
      builder.enableComplexMapKeySerialization();
    }    
    if (Settings.SERIALIZE_NULLS) {
      builder.serializeNulls();
    }    
    if (Settings.SERIALIZE_SPECIAL_FLOATING_POINT_VALUES) {
      builder.serializeSpecialFloatingPointValues();
    }
    
    // このハンドラ及び複数のサブハンドラの処理をGsonBuilderに登録する。
    handler.registerToBuilder(builder);    

    // gsonを作成する
    gson = builder.create();
  }

  
  /**
   * 復帰時にクラスが見つからない場合にnullを返す。
   * @param value 
   */
  public Serializer<T> setNullIfClassNotFound(boolean value) {
    nullIfClassNotFound = value;
    return this;
  }

  /**
   * 指定されたオブジェクトをJSON文字列に変換する。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合にはnullを返す。
   * @param object 変換対象オブジェクト
   * @return オブジェクトをJSON化した文字列、またはnull
   */
  public String serialize(T object) {
    if (object == null) return null;
    try {
      return gson.toJson(object, typeToken.getType());
    } catch (RuntimeException ex) {
      throw new JsonException(ex);
    }
  }
  
  /**
   * 指定されたオブジェクトをJSON文字列に変換し、それをUTF-8文字列としてバイト配列に変換したものを返す。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合にはnullを返す。
   * @param object 変換対象オブジェクト
   * @return オブジェクトをJSON化した文字列、またはnull
   */
  public byte[]serializeToBytes(T object) {
    if (object == null) return null;
    try {
      return serialize(object).getBytes(Settings.ENCODING);
    } catch (Exception ex) {
      throw new JsonException(ex);
    }
  }

  /**
   * {@link #serialize(Object)}と同じだが、結果の文字列をJavaコード文字列に変換する。
   * ユニットテスト用。
   * @param object 変換対象オブジェクト
   * @return Javaコード文字列
   */
  public String serializeToJavaString(T object) {
    StringBuilder result = new StringBuilder();
    String serialized = serialize(object);
    result.append('"');
    for (char c: serialized.toCharArray()) {
      if (c == '"') result.append("\\");
      result.append(c);
    }
    result.append('"');
    return result.toString();
  }
  
  /**
   * 指定されたJSON文字列を元のオブジェクトに変換する。オブジェクトはT型でなければいけない。
   * JSON文字列がnullの場合はnullを返す。
   * @param json JSON文字列
   * @return 復帰されたオブジェクト、あるいはnull
   */
  @SuppressWarnings("unchecked")
  public T deserialize(String json) {
    if (json == null) return null;
    try {
      return (T)gson.fromJson(json, typeToken.getType());
    } catch (JsonClassNotFoundException ex) {
      // 復帰時にクラスが見つからない場合
      if (nullIfClassNotFound) return null;          
      throw ex;
    } catch (JsonException ex) {
      // 上記以外のJSON例外
      throw ex;
    } catch (RuntimeException ex) {
      // 上記以外のランタイム例外
      throw new JsonException(ex);
    }
  }

  /**
   * 指定されたバイト配列をUTF-8文字列とし、それを元のオブジェクトに変換する。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合はnullを返す。
   * @param bytes
   * @return
   */
  public T deserializeFromBytes(byte[]bytes) {
    if (bytes == null) return null;
    try {
      return deserialize(new String(bytes, Settings.ENCODING));
    } catch (Exception ex) {
      throw new JsonException(ex);
    }
  }
  
  /**
   * 指定されたオブジェクトをJSON文字列に変換し、さらにGZIP圧縮した後のバイト配列を取得する。
   * オブジェクトはT型でなければいけない。
   * 変換対象オブジェクトがnullの場合には。nullが返されることに注意。
   * @param object 変換対象オブジェクト
   * @return 直列化されたバイト配列
   */
  public byte[] serializeGzip(T object) {
    if (object == null) return null;
    byte[]bytes = serializeToBytes(object);
    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      GZIPOutputStream gout = new GZIPOutputStream(bout);
      gout.write(bytes);
      gout.close();
      return bout.toByteArray();
    } catch (Exception ex) {
      throw new JsonException(ex);
    }
  }

  /**
   * 指定されたGZIPバイト配列から元のオブジェクトを復帰する。オブジェクトはT型でなければいけない。
   * バイト列がnullの場合には、nullが返されることに注意。
   * @param bytes GZIP圧縮されたJSON文字列
   * @return 復帰されたオブジェクト
   */
  public T deserializeGzip(byte[]bytes) {
    if (bytes == null)
      return null;
    try {
      ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
      GZIPInputStream gin = new GZIPInputStream(bin);
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      byte[]buffer = new byte[1024];
      while (true) {
        int size = gin.read(buffer);
        if (size <= 0) break;
        bout.write(buffer, 0, size);
      }
      bout.close();
      return deserializeFromBytes(bout.toByteArray());
    } catch (Exception ex) {
      throw new JsonException(ex);
    }
  }

}
