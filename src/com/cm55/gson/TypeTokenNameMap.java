package com.cm55.gson;

import java.util.*;
import java.util.stream.*;

import com.google.gson.reflect.*;

/**
 * 名称とTypeTokenのマップ
 * <p>
 * 単純に任意のT型のTypeTokenと名称文字列を結びつけておくためのもの。
 * これは{@link MultiHandler}内部で使用される。
 * </p>
 * @author ysugimura
 */
class TypeTokenNameMap {
  
  /** 名称/TypeTokenのマップ */
  private Map<String, TypeToken<?>> nameToToken = new HashMap<>();

  /** TypeToken/名称のマップ */
  private Map<TypeToken<?>, String> tokenToName = new HashMap<>();
  
  /** タイプを追加する */
  void addType(String typeName, TypeToken<?>typeToken) {
    
    TypeToken<?>registeredToken = nameToToken.get(typeName);
    String registeredName = tokenToName.get(typeToken);
    if (registeredToken != null) {
      // この名前で既に登録のある場合、クラスが同一でなければエラー。
      if (!registeredToken.equals(typeToken)) {
        throw new IllegalArgumentException(
          "duplicate definition of typeName:" + typeName + "..." + registeredToken + " and " + typeToken);
      }
    }  
    if (registeredName != null) {
      // このクラスで既に登録のある場合、名称が同一でなければエラー。
      if (!registeredName.equals(typeName)) {
        throw new IllegalArgumentException(
          "duplicate definition of typeClass:" + typeToken + "..." + registeredName + " and " + typeName);
      }
    }
    
    // 同一名称、同一クラスで既登録の場合、何もしない
    if (registeredToken != null && registeredName != null) {
      return;
    }
    
    // 未登録の場合、登録する
    nameToToken.put(typeName, typeToken);
    tokenToName.put(typeToken, typeName);      
  }

  /** 
   * 指定されたTypeTokenに与えられた名称を取得する。存在しない場合はnullを返す。
   * @param typeToken {@link TypeToken}
   * @return 名称
   */
  String getTypeName(TypeToken<?>typeToken) {
    return tokenToName.get(typeToken);
  }

  /** 
   * 指定された名称のTypeTokenを取得する。存在しない場合はnullを返す。
   * @param typeName 名称
   * @return {@link TypeToken}
   */
  TypeToken<?>getTypeToken(String typeName) {
    return nameToToken.get(typeName);
  }

  /**
   * 登録数を取得する
   * @return 登録数
   */
  int count() {
    int count = nameToToken.size();
    assert count == tokenToName.size();
    return count;
  }

  /**
   * 登録済のすべての{@link TypeToken}の集合を得る
   * @return
   */
  Stream<TypeToken<?>>allTypeTokens() {
    return tokenToName.keySet().stream();
  }
  
  /**
   * 複製する
   * @return
   */
  TypeTokenNameMap duplicate() {
    TypeTokenNameMap that = new TypeTokenNameMap();
    that.nameToToken = new HashMap<>(this.nameToToken);
    that.tokenToName = new HashMap<>(this.tokenToName);
    return that;
  }
}
