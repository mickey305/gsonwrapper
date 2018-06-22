package com.gwtcenter.json;

import java.io.*;
import java.util.zip.*;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.inject.*;

/**
 * シリアライザ
 * 
 * <h2>概要</h2>
 * <p>
 * {@link Serializer}はあるオブジェクトの直列化と復帰を行うオブジェクト、{@link Serializer}を作成するには
 * {@link SerializerFactory}に対して、以下のいずれかを指定する。
 * </p>
 * <ul>
 * <li>1.対象とするオブジェクトのクラス
 * <li>2.上記クラスがジェネリクスの場合には、その{@link TypeToken}
 * <li>3.{@link AbstractAdapter}クラス階層以下のタイプアダプタ
 * </ul>
 * <p>
 * 単純なオブジェクトの場合には、1.2.のいずれかで良いが、以下のようなケースでは、3.を選択し、
 * 特に{@link MultiTypeAdapter}を使用しなければならない。
 * </p>
 * <ul>
 * <li>対象とするオブジェクト自身、そのオブジェクト内のフィールドオブジェクト、さらにその中のフィールド
 * 等で「抽象クラス」が使用されている場合。
 * </ul>
 * <p>
 * 例えば、直列化・復帰対象オブジェクトの字面としてはFooだとしても、実際にはそのサブクラスの
 * FooOne, FooTwoのオブジェクトが格納されている場合がある。これを正しく扱うには、{@link MultiTypeAdapter}
 * を使用しなければならない。詳細は{@link MultiTypeAdapter}を参照のこと。
 * </p>
 * <h2>MultiTypeAdapterでのClassNotFound</h2>
 * <p>
 * {@link MultiTypeAdapter}を使用した直列化では、直列化後のJSON文字列に、実際のクラス名称
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
@ImplementedBy(Serializer.Impl.class)
public interface Serializer<T> {
  
  /**
   * 復帰時にクラスが見つからない場合にnullを返す。
   * @param value 
   */
  public Serializer<T> setNullIfClassNotFound(boolean value);

  /**
   * 指定されたオブジェクトをJSON文字列に変換する。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合にはnullを返す。
   * @param object 変換対象オブジェクト
   * @return オブジェクトをJSON化した文字列、またはnull
   */
  public String serialize(T object);
  
  /**
   * 指定されたオブジェクトをJSON文字列に変換し、それをUTF-8文字列としてバイト配列に変換したものを返す。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合にはnullを返す。
   * @param object 変換対象オブジェクト
   * @return オブジェクトをJSON化した文字列、またはnull
   */
  public byte[]serializeToBytes(T object);

  /**
   * {@link #serialize(Object)}と同じだが、結果の文字列をJavaコード文字列に変換する。
   * ユニットテスト用。
   * @param object 変換対象オブジェクト
   * @return Javaコード文字列
   */
  public String serializeToJavaString(T object);
  
  /**
   * 指定されたJSON文字列を元のオブジェクトに変換する。オブジェクトはT型でなければいけない。
   * JSON文字列がnullの場合はnullを返す。
   * @param json JSON文字列
   * @return 復帰されたオブジェクト、あるいはnull
   */
  public T deserialize(String json);

  /**
   * 指定されたバイト配列をUTF-8文字列とし、それを元のオブジェクトに変換する。オブジェクトはT型でなければいけない。
   * 変換対象がnullの場合はnullを返す。
   * @param bytes
   * @return
   */
  public T deserializeFromBytes(byte[]bytes);
  
  /**
   * 指定されたオブジェクトをJSON文字列に変換し、さらにGZIP圧縮した後のバイト配列を取得する。
   * オブジェクトはT型でなければいけない。
   * 変換対象オブジェクトがnullの場合には。nullが返されることに注意。
   * @param object 変換対象オブジェクト
   * @return 直列化されたバイト配列
   */
  public byte[]serializeGzip(T object);

  /**
   * 指定されたGZIPバイト配列から元のオブジェクトを復帰する。オブジェクトはT型でなければいけない。
   * バイト列がnullの場合には、nullが返されることに注意。
   * @param bytes GZIP圧縮されたJSON文字列
   * @return 復帰されたオブジェクト
   */
  public T deserializeGzip(byte[]bytes);
  
  /**
   * {@link Serializer}の実装
   * @author ysugimura
   */
  public static class Impl<T> implements Serializer<T> {
        
    public Impl() {}
  
    /** このシリアライザが直列化及び復帰を行う対象のクラス */
    private TypeToken<T> topType;
    
    /** Gson実行オブジェクト */
    private Gson gson;
    
    /** 復帰時にクラスが見つからない場合はnullを返す */
    private boolean nullIfClassNotFound = true;

    /**
     * セットアップする
     * @param topType
     * @param gson
     */
    void setup(TypeToken<T> topType, Gson gson) {
      this.topType = topType;
      this.gson = gson;
    }
   
    /**
     * 復帰時にクラスが見つからない場合はnullを返す設定
     * @param value 
     */
    public Serializer<T> setNullIfClassNotFound(boolean value) {
      nullIfClassNotFound = value;
      return this;
    }
    
    /** {@inheritDoc} */
    @Override
    public String serialize(T object) {
      if (object == null) return null;
      try {
        return gson.toJson(object, topType.getType());
      } catch (RuntimeException ex) {
        throw new JsonException(ex);
      }
    }
    
    /** {@inheritDoc} */
    @Override
    public byte[]serializeToBytes(T object) {
      if (object == null) return null;
      try {
        return serialize(object).getBytes(ENCODING);
      } catch (Exception ex) {
        throw new JsonException(ex);
      }
    }

    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String json) {
      if (json == null) return null;
      try {
        return (T)gson.fromJson(json, topType.getType());
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
    
    @Override
    public T deserializeFromBytes(byte[]bytes) {
      if (bytes == null) return null;
      try {
        return deserialize(new String(bytes, ENCODING));
      } catch (Exception ex) {
        throw new JsonException(ex);
      }
    }
    
    /** JSONのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
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
}