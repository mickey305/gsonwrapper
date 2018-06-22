package com.gwtcenter.json;

import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

/**
 * JSON変換用のアダプタ。このクラスを継承して、様々なアダプタを作成すること。
 * 
 * @author ysugimura
 * @param <T> アダプタの対象とするクラスの型
 */
public class  Adapter<T> {

  /** 
   * 対象タイプ
   * <p>
   * このアダプタが対象とするクラスもしくはタイプだが、必ずしもそのオブジェクトに対する特殊な処理を行うとは限らない。
   * 単にクラスもしくはタイプ、つまり「型」を保持するだけの目的の場合もある。
   * 通常は単なるクラスオブジェクトであるが、ジェネリックス型の場合には以下のようなタイプオブジェクトが格納されている場合もある。
   * </p>
   * <pre>
   * // FooのArrayListの場合 
   * Type type = new TypeToken<ArrayList<Foo>>() {}.getType();
   * </pre>
   */
  protected final TypeToken<T> targetType;

  /** 
   * サブアダプタリスト
   * <p>
   * 対象とするタイプ（クラス）のフィールド等の型に対するアダプタがもし必要であればここに格納される。
   * 全く必要でない場合は{@link #subAdapters}の値はnullのまま
   * </p>
   */
  private List<Adapter<?>>subAdapters = null;
  

  /** クラスを指定する */
  protected Adapter(Class<T>targetClass) {
    this(TypeToken.get(targetClass));
  }

  /** 
   * タイプトークンを指定する
   * <pre>
   * new TypeToken<ArrayList<String>>() {}
   * </pre>
   * <p>
   * 等のオブジェクト
   * </p>
   * @param targetToken
   */
  protected Adapter(TypeToken<T>targetToken) {
    targetType = targetToken;
  }
  
  /**
   * このアダプタが対象とするタイプを取得する。
   * @return
   */
  public TypeToken<T> getTargetType() {
    return targetType;
  }
  
  /**
   * サブアダプタを追加する。
   * <p>
   * このオブジェクトが対象とするT型オブジェクトの中に、もし直列化・復帰のサポートが必要な
   * クラスがある場合は、そのアダプタを登録する。
   * </p>
   * @param subAdapter サブアダプタ
   */
  public void addSubAdapter(Adapter<?> subAdapter) {
    if (subAdapters == null) subAdapters = new ArrayList<Adapter<?>>();
    subAdapters.add(subAdapter);
  }
  
  /**
   * このアダプタの特殊処理をGsonBuilderに登録する。
   * @param builder
   */
  protected void registerToBuilder(GsonBuilder builder) {   
    
    // T型に関する特殊処理はサブクラスで定義される。

    // サブアダプタがもしあれば、これらについての特殊処理を行う。
    if (subAdapters == null) return;
    for (Adapter<?> subAdapter: subAdapters) {
      subAdapter.registerToBuilder(builder);
    }
  }
}
