package com.cm55.gson;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.gson.stream.*;

/**
 * 特定のGsonオブジェクト用の実行環境
 * @author ysugimura
 *
 * @param <T>
 */
class MultiTypeAdapterFactory<T> implements TypeAdapterFactory {


  TypeToken<T> topType;
  TypeTokenNameMap typeTokenMap;
  Map<TypeToken<? extends T>, TypeAdapter<T>> subTypeAdapters;
  TypeAdapter<JsonElement> elementAdapter;

  public MultiTypeAdapterFactory(TypeToken<T> topType, TypeTokenNameMap typeTokenMap) {
    this.topType = topType;
    this.typeTokenMap = typeTokenMap;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> TypeAdapter<C> create(Gson gson, TypeToken<C> typeToken) {
    if (!typeToken.equals(topType))
      return null;

    // サブクラス用のTypeAdapterを取得する
    subTypeAdapters = getSubTypeAdapters(gson, this);

    // JsonElement用のTypeAdapterを取得する
    elementAdapter = gson.getAdapter(JsonElement.class);

    return (TypeAdapter<C>) new GsonTypeAdapter<T>(this);
  }

  /**
   * typeTokenMapに登録されているすべての{@link TypeToken}について、その直列化・復帰を行う
   * {@link TypeAdapter}を取得し、マップにして返す。
   * 
   * @param gson
   * @param adapterFactory
   * @return
   */
  @SuppressWarnings("unchecked")
  Map<TypeToken<? extends T>, TypeAdapter<T>> getSubTypeAdapters(Gson gson, TypeAdapterFactory adapterFactory) {

    Map<TypeToken<? extends T>, TypeAdapter<T>> subTypeAdapters = new HashMap<>();

    typeTokenMap.allTypeTokens().forEach(typeToken -> {
      TypeToken<? extends T> tt = (TypeToken<? extends T>) typeToken;
      TypeAdapter<T> adapter = (TypeAdapter<T>) gson.getDelegateAdapter(adapterFactory, tt);
      subTypeAdapters.put(tt, adapter);
    });
    return subTypeAdapters;
  }

  /**
   * Gson用のタイプアダプタ
   * 
   * @author ysugimura
   *
   * @param <T>
   */
  public static class GsonTypeAdapter<T> extends TypeAdapter<T> {

    /** タイプアダプタの実行環境 */
    private MultiTypeAdapterFactory<T> env;

    /** 実行環境を指定する */
    public GsonTypeAdapter(MultiTypeAdapterFactory<T> env) {
      this.env = env;
    }

    /**
     * オブジェクトを直列化して書き込む
     */
    @SuppressWarnings("unchecked")
    @Override
    public void write(JsonWriter writer, T value) throws IOException {

      // オブジェクトしか与えられないため、正確なTypeTokenを得ることは不可能。
      // このため、MultiTypeAdapterではジェネリクスは使用できない。
      TypeToken<? extends T> typeToken = (TypeToken<? extends T>) TypeToken.get(value.getClass());

      // タイプ名称を取得
      String typeName = env.typeTokenMap.getTypeName(typeToken);

      // タイプアダプタを取得し、値をJsonElementに変換
      TypeAdapter<T> typeAdapter = env.subTypeAdapters.get(typeToken);
      JsonElement tree = typeAdapter.toJsonTree(value);


      // オブジェクト書き込み開始
      writer.beginObject();

      // 型フィールドを書き込み
      writer.name(Settings.MULTIHANDLER_TYPE_MARKER).value(typeName);

      // データフィールドを書き込み
      writer.name(Settings.MULTIHANDLER_DATA_MARKER);
      env.elementAdapter.write(writer, tree);

      // オブジェクト書き込み終了
      writer.endObject();

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
      if (!typeField.equals(Settings.MULTIHANDLER_TYPE_MARKER)) {
        // assertにしてしまうと復旧ができないので例外にする
        throw new JsonException("Invalid TYPE FIELD Marker in MultiTypeAdapter:" + typeField);
      }
      String typeName = reader.nextString();
      String dataField = reader.nextName();
      if (!dataField.equals(Settings.MULTIHANDLER_DATA_MARKER)) {
        // assertにしてしまうと復旧ができないので例外にする
        throw new JsonException("Invalid DATA FIELD Marker in MultiTypeAdapter:" + dataField);
      }
      JsonElement tree = (JsonElement) env.elementAdapter.read(reader);

      // オブジェクトの読み出し終了
      reader.endObject();

      // タイプ名称からTypeTokenを取得する
      TypeToken<? extends T> typeToken = (TypeToken<? extends T>) env.typeTokenMap.getTypeToken(typeName);

      // タイプ名称が未登録の場合
      if (typeToken == null)
        throw new JsonClassNotFoundException();

      // TypeTokenからアダプタを取得
      TypeAdapter<T> typeAdapter = env.subTypeAdapters.get(typeToken);

      // アダプタにJsonElementツリーを解析させていオブジェクトを取得
      T result = (T) typeAdapter.fromJsonTree(tree);

      return result;
    }
  }

}
