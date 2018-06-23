package com.cm55.gson;

import java.util.*;

import com.google.gson.reflect.*;

/** ビルダー */
public class HandlerBuilder<T> {
  
  protected final TypeToken<T> typeToken;

  /**
   * サブアダプタリスト
   * <p>
   * 対象とするタイプ（クラス）のフィールド等の型に対するアダプタがもし必要であればここに格納される。
   * 全く必要でない場合は{@link #subHandlers}の値はnullのまま
   * </p>
   */
  protected List<Handler<?>> subHandlers = null;


  /** クラスを指定する */
  public HandlerBuilder(Class<T> targetClass) {
    this(TypeToken.get(targetClass));
  }

  /**
   * タイプトークンを指定する
   * @param targetToken
   */
  public HandlerBuilder(TypeToken<T> typeToken) {
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
  public HandlerBuilder<T> addSubHandler(Handler<?> subAdapter) {
    if (subHandlers == null)
      subHandlers = new ArrayList<Handler<?>>();
    subHandlers.add(subAdapter);
    return this;
  }

  public HandlerBuilder<T> addSubHandlers(Handler<?>... subAdapters) {
    for (Handler<?> adapter : subAdapters) {
      addSubHandler(adapter);
    }
    return this;
  }
  
  public Handler<T> build() {
    return new Handler<T>(typeToken, subHandlers);
  }
}