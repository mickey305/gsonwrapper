package com.cm55.gson;

import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

/**
 * @author ysugimura
 * @param <T> アダプタの対象とするクラスの型
 */
public class Handler<T> {

  /**
   * 対象タイプ
   * <p>
   * このアダプタが対象とするクラスもしくはタイプだが、必ずしもそのオブジェクトに対する特殊な処理を行うとは限らない。
   * 単にクラスもしくはタイプ、つまり「型」を保持するだけの目的の場合もある。
   * 通常は単なるクラスオブジェクトであるが、ジェネリックス型の場合には以下のようなタイプオブジェクトが格納されている場合もある。
   * </p>
   * 
   * <pre>
   * // FooのArrayListの場合
   * Type type = new TypeToken<ArrayList<Foo>>() {
   * }.getType();
   * </pre>
   */
  protected final TypeToken<T> typeToken;

  /**
   * サブアダプタリスト
   * <p>
   * 対象とするタイプ（クラス）のフィールド等の型に対するアダプタがもし必要であればここに格納される。
   * 全く必要でない場合は{@link #subHandlers}の値はnullのまま
   * </p>
   */
  private final List<Handler<?>> subHandlers;

  protected Handler(TypeToken<T> typeToken, List<Handler<?>> subAdapters) {
    this.typeToken = typeToken;
    this.subHandlers = subAdapters;
  }

  /**
   * このアダプタが対象とするタイプを取得する。
   * @return タイプトークン
   */
  public TypeToken<T> getTypeToken() {
    return typeToken;
  }
  
  /**
   * このアダプタの特殊処理をGsonBuilderに登録する。
   * @param builder {@link GsonBuilder}
   */
  protected void registerToBuilder(GsonBuilder builder) {

    // T型に関する特殊処理はサブクラスで定義される。

    // サブアダプタがもしあれば、これらについての特殊処理を行う。
    if (subHandlers == null)
      return;
    for (Handler<?> subHandler : subHandlers) {
      subHandler.registerToBuilder(builder);
    }
  }
}
