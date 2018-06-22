package com.gwtcenter.json;

import java.util.*;

import com.google.gson.reflect.*;

/**
 * 名称とTypeTokenのマップ
 * <p>
 * 単純に任意のT型のTypeTokenと名称文字列を結びつけておくためのもの。
 * </p>
 * @author ysugimura
 *
 * @param <T>
 */
public class TypeTokenNameMap {
  
  /** 名称/TypeTokenのマップ */
  private Map<String, TypeToken<?>> nameToToken = 
    new HashMap<String, TypeToken<?>>();

  /** TypeToken/名称のマップ */
  private Map<TypeToken<?>, String> tokenToName = 
    new HashMap<TypeToken<?>, String>();
  
  /** タイプを追加する */
  public void addType(String typeName, TypeToken<?>typeToken) {
    
    TypeToken<?>registeredClass = nameToToken.get(typeName);
    String registeredName = tokenToName.get(typeToken);
    if (registeredClass != null) {
      // この名前で既に登録のある場合、クラスが同一でなければエラー。
      if (!registeredClass.equals(typeToken)) {
        throw new IllegalArgumentException(
          "duplicate definition of typeName:" + typeName + "..." + registeredClass + " and " + typeToken);
      }
    }  
    if (registeredName != null) {
      // このクラスで既に登録のある場合、名称が同一でなければエラー。
      if (!registeredName.equals(typeName)) {
        throw new IllegalArgumentException(
          "duplicate definition of typeClass:" + typeToken + "..." + registeredName + " and " + typeName);
      }
    }
    
    // 同一名称、同一クラスで既登録のばあい
    if (registeredClass != null && registeredName != null) {
      return;
    }
    
    // 未登録の場合
    nameToToken.put(typeName, typeToken);
    tokenToName.put(typeToken, typeName);      
  }

  /** 
   * 指定されたTypeTokenの名称を取得する。存在しない場合はnullを返す。
   * @param typeToken {@link TypeToken}
   * @return 名称
   */
  public String getTypeName(TypeToken<?>typeToken) {
    return tokenToName.get(typeToken);
  }

  /** 
   * 指定された名称のTypeTokenを取得する。存在しない場合はnullを返す。
   * @param typeName 名称
   * @return {@link TypeToken}
   */
  public TypeToken<?>getTypeToken(String typeName) {
    return nameToToken.get(typeName);
  }

  /**
   * 登録数を取得する
   * @return 登録数
   */
  public int count() {
    int count = nameToToken.size();
    assert count == tokenToName.size();
    return count;
  }

  /**
   * 登録済のすべての{@link TypeToken}の集合を得る
   * @return
   */
  public Set<TypeToken<?>>allTypeTokens() {
    return tokenToName.keySet();
  }
  
  /**
   * 複製する
   * @return
   */
  public TypeTokenNameMap duplicate() {
    TypeTokenNameMap that = new TypeTokenNameMap();
    that.nameToToken = new HashMap<>(this.nameToToken);
    that.tokenToName = new HashMap<>(this.tokenToName);
    return that;
  }
}
