package com.cm55.gson;

import java.util.*;

import com.google.gson.reflect.*;

/**
 * {@link Handler}のビルダ
 * @author ysugimura
 *
 * @param <T> 対象とするタイプ
 */
public class HandlerBuilder<T> {

  /** タイプトークン */
  protected TypeToken<T> typeToken;

  /**
   * サブハンドラリスト
   * <p>
   * 対象とするタイプ（クラス）のフィールド等の型に対するハンドラがもし必要であればここに格納される。
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
    if (typeToken == null) throw new NullPointerException();
    this.typeToken = typeToken;
  }

  /**
   * サブハンドラを追加する
   * @param handlers 複数のサブハンドラ
   * @return 本オブジェクト
   */
  public HandlerBuilder<T> addSubHandler(Handler<?>... handlers) {
    if (subHandlers == null)
      subHandlers = new ArrayList<Handler<?>>();
    for (Handler<?> handler : handlers) {
      subHandlers.add(handler);
    }
    return this;
  }

  /**
   * {@link Handler}をビルドする
   * @return {@link Handler}
   */
  public Handler<T> build() {
    if (typeToken == null) throw new IllegalStateException();
    Handler<T>handler = new Handler<T>(typeToken, subHandlers);
    typeToken = null;
    subHandlers = null;
    return handler;
  }
}