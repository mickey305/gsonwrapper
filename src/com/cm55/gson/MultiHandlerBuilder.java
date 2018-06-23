package com.cm55.gson;

import com.google.gson.reflect.*;

public class MultiHandlerBuilder<T> extends HandlerBuilder<T> {
  private final TypeTokenNameMap typeTokenMap = new TypeTokenNameMap();

  public MultiHandlerBuilder(Class<T> targetType) {
    super(targetType);
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
   * @param typeClass
   */
  public MultiHandlerBuilder<T> addSubClass(TypeToken<? extends T> typeClass) {
    addSubClass(typeClass.getRawType().getSimpleName(), typeClass);
    return this;
  }

  /**
   * 登録するクラスと、その名称を指定する
   * 
   * @param typeName
   *          登録名称
   * @param typeClass
   *          登録クラス
   */
  public MultiHandlerBuilder<T> addSubClass(String typeName, Class<? extends T> typeClass) {
    addSubClass(typeName, TypeToken.get(typeClass));
    return this;
  }

  /**
   * タイプ名称とそのクラスを指定して登録する。 既に登録されている場合は例外が発生する。
   * 
   * @param typeName
   *          登録名称 JSON中に書き出されるマーカー文字列
   * @param typeToken
   *          登録クラス 上記マーカー文字列の場合に中身とされるクラスのタイプトークン
   */
  public MultiHandlerBuilder<T> addSubClass(String typeName, TypeToken<? extends T> _typeToken) {
    TypeToken<T> topType = typeToken;
    if (!topType.getRawType().isAssignableFrom(_typeToken.getRawType())) {
      throw new IllegalArgumentException(_typeToken + " is not assignable to " + topType);
    }
    typeTokenMap.addType(typeName, _typeToken);
    return this;
  }
  
  public MultiHandlerBuilder<T> addSubHandler(Handler<?> subAdapter) {
    return (MultiHandlerBuilder<T>)super.addSubHandler(subAdapter);
  }

  public MultiHandlerBuilder<T> addSubHandlers(Handler<?>... subAdapters) {
    return (MultiHandlerBuilder<T>)super.addSubHandlers(subAdapters);
  }
  
  public MultiHandler<T> build() {
    return new MultiHandler<T>(typeToken, subHandlers, typeTokenMap);
  }
}