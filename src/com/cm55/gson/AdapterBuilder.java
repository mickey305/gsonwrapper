package com.cm55.gson;

import java.util.*;

import com.google.gson.reflect.*;

/** ビルダー */
public class AdapterBuilder<T> {
  
  protected final TypeToken<T> typeToken;

  /**
   * サブアダプタリスト
   * <p>
   * 対象とするタイプ（クラス）のフィールド等の型に対するアダプタがもし必要であればここに格納される。
   * 全く必要でない場合は{@link #subAdapters}の値はnullのまま
   * </p>
   */
  protected List<Adapter<?>> subAdapters = null;


  /** クラスを指定する */
  public AdapterBuilder(Class<T> targetClass) {
    this(TypeToken.get(targetClass));
  }

  /**
   * タイプトークンを指定する
   * @param targetToken
   */
  public AdapterBuilder(TypeToken<T> typeToken) {
    this.typeToken = typeToken;
  }

  /**
   * サブアダプタを追加する。
   * <p>
   * このオブジェクトが対象とするT型オブジェクトの中に、もし直列化・復帰のサポートが必要な クラスがある場合は、そのアダプタを登録する。
   * </p>
   * 
   * @param subAdapter サブアダプタ
   */
  public AdapterBuilder<T> addSubAdapter(Adapter<?> subAdapter) {
    if (subAdapters == null)
      subAdapters = new ArrayList<Adapter<?>>();
    subAdapters.add(subAdapter);
    return this;
  }

  public AdapterBuilder<T> addSubAdapters(Adapter<?>... subAdapters) {
    for (Adapter<?> adapter : subAdapters) {
      addSubAdapter(adapter);
    }
    return this;
  }
  
  public Adapter<T> build() {
    return new Adapter<T>(typeToken, subAdapters);
  }
}