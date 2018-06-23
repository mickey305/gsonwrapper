package com.cm55.gson;

import com.google.gson.reflect.*;

/**
 * {@link MultiHandler}のビルダ
 * @author ysugimura
 *
 * @param <T> 対象とするタイプ
 */
public class MultiHandlerBuilder<T> extends HandlerBuilder<T> {
  
  private TypeTokenNameMap typeTokenMap = new TypeTokenNameMap();

  public MultiHandlerBuilder(Class<T> targetType) {
    super(targetType);
  }

  public MultiHandlerBuilder(TypeToken<T> typeToken) {
    super(typeToken);
  }

  /**
   * 登録するタイプを複数指定する。 各クラスの登録名称は{@link Class#getSimpleName()}で取得される文字列になる。
   * 
   * @param classes
   *          登録対象クラス（複数）
   */
  @SafeVarargs
  public final MultiHandlerBuilder<T> addSubClasses(Class<? extends T>... classes) {
    for (Class<? extends T> clazz : classes) {
      addSubClass(clazz);
    }
    return this;
  }

  /**
   * 登録するタイプを指定する。名称は{@link Class#getSimpleName()}となる。
   * 
   * @param clazz
   *          対象クラス
   */
  public MultiHandlerBuilder<T> addSubClass(Class<? extends T> clazz) {
    addSubClass(clazz.getSimpleName(), clazz);
    return this;
  }

  /**
   * 登録するタイプトークンを指定する。名称はRawタイプの{@link Class#getSimpleName()}となる。
   * 
   * @param subTypeClass
   */
  public MultiHandlerBuilder<T> addSubClass(TypeToken<? extends T> subTypeClass) {
    addSubClass(subTypeClass.getRawType().getSimpleName(), subTypeClass);
    return this;
  }

  /**
   * 登録するクラスと、その名称を指定する
   * @param typeName 登録名称
   * @param subTypeClass 登録クラス
   */
  public MultiHandlerBuilder<T> addSubClass(String typeName, Class<? extends T> subTypeClass) {
    addSubClass(typeName, TypeToken.get(subTypeClass));
    return this;
  }

  /**
   * タイプ名称とそのクラスを指定して登録する。 既に登録されている場合は例外が発生する。
   * 
   * @param typeName  登録名称 JSON中に書き出されるマーカー文字列
   * @param typeToken 登録クラス 上記マーカー文字列の場合に中身とされるクラスのタイプトークン
   */
  public MultiHandlerBuilder<T> addSubClass(String typeName, TypeToken<? extends T> subTypeToken) {
    if (!typeToken.getRawType().isAssignableFrom(subTypeToken.getRawType())) {
      throw new IllegalArgumentException(subTypeToken + " is not assignable to " + typeToken);
    }
    typeTokenMap.addType(typeName, subTypeToken);
    return this;
  }

  /**
   * サブハンドラを登録する
   */
  public MultiHandlerBuilder<T> addSubHandler(Handler<?>... subHandlers) {
    return (MultiHandlerBuilder<T>)super.addSubHandler(subHandlers);
  }

  /**
   * ビルドする
   */
  public MultiHandler<T> build() {
    if (typeToken == null) throw new IllegalStateException();
    MultiHandler<T>handler = new MultiHandler<T>(typeToken, subHandlers, typeTokenMap);
    typeToken = null;
    subHandlers = null;
    typeTokenMap = null;
    return handler;
  }
}
