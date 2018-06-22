package com.gwtcenter.json;

import com.google.gson.*;
import com.google.gson.reflect.*;
import com.google.inject.*;
import com.gwtcenter.json.Serializer.*;

/**
 * {@link Serializer}のファクトリ。
 * {@link Serializer}は直接作成せずに、このオブジェクトに作成させること。
 * @author ysugimura
 */
@Singleton
public class SerializerFactory {

  /** {@link Serializer}のプロバイダ */
  @Inject private Provider<Impl<?>>provider;

  /**
   * {@link AbstractAdapter}を指定して{@link Serializer}オブジェクトを作成する。
   * @param def 直列化定義オブジェクト
   * @return 直列化実行オブジェクト
   */
  @SuppressWarnings("unchecked")
  public <T>Serializer<T> create(AbstractAdapter<T> adapter) {
        
    // このアダプタおよび、複数のサブアダプタをGsonBuilderに登録する
    GsonBuilder builder = createGsonBuilder();
    adapter.registerToBuilder(builder);    

    // gsonを作成する
    Gson gson = builder.create();

    // シリアライザを作成してセットアップ
    Impl<T> serializer = (Impl<T>)provider.get();
    serializer.setup(adapter.getTargetType(), gson);
    return serializer;
  }    
  
  /**
   * 単純なクラスについて{@link BaseAdapter}を省略してシリアライザを作成する。
   * @param clazz 対象とするクラス
   * @return シリアライザ
   */
  public <T>Serializer<T> create(Class<T>clazz) {
    BaseAdapter<T>adapter = new BaseAdapter<T>(clazz);
    return create(adapter);
  }

  /**
   * {@link TypeToken}について{@link BaseAdapter}を省略してシリアライザを作成する。
   * @param token {@link TypeToken}
   * @return シリアライザ
   */
  public <T>Serializer<T> create(TypeToken<T>token) {
    BaseAdapter<T>adapter = new BaseAdapter<T>(token);
    return create(adapter);
  }
  
  /**
   * {@link GsonBuilder}オブジェクトを作成し、このオブジェクト中に格納された情報をセットアップする。
   * 
   * <h2>enableComplexMapKeySerialization()</h2>
   * <p>
   * これを行わないと、マップのキーは必ずそのオブジェクトのtoString()の結果の文字列になってしまう。
   * これはなぜかというと、JSONのマップのキーは単一の文字列でなければならないからのようだ。
   * </p>
   * <p>
   * しかしこれでは、複雑なオブジェクトをキーとして使っている場合には、適切なtoString()を定義しないと
   * いけないし（復帰方法は調べていない）、逆にenumにtoString()が定義されていると、適切なJSON化が
   * できなくなる。
   * ここでは、JSON文字列としての不適切さよりも、Javaオブジェクトの直列化・復帰を主眼にしているので、
   * このオプションを指定する。
   * </p>
   * <h2>serializeNulls()</h2>
   * <p>
   * これを行わないと、値がnullのフィールドはフィールド自体が省略されてしまう。
   * 以下のケースのaフィールドはこれでも問題が無いが、bのハッシュマップの値がnullの場合には、キーも
   * 格納されなくなってしまう。
   * </p>
   * <pre>
   * public static class Sample {
   *   String a;
   *   HashMap<Integer, String>b = new HashMap<Integer, String>();
   * }
   * </pre>
   * <p>
   * 例えば、以下のようなケースの場合、ハッシュマップの値がnullなのでキー自体も現れなくなってしまう。
   * </p>
   * <pre>
   * Sample sample = new Sample();
   * sample.b.put(123, null);
   * </pre>
   * <h2>serializeSpecialFloatingPointValues()</h2>
   * <p>
   * JSON自体の仕様ではNaNやInfiniteは存在しないが、これが無いと値が落ちてしまうためサポートする。
   * </p>
   */
  protected <T>GsonBuilder createGsonBuilder() {
    
    GsonBuilder builder = new GsonBuilder();
    
    // ※上記説明を参照のこと
    builder.enableComplexMapKeySerialization();

    // ※上記説明を参照のこと。
    builder.serializeNulls();
    
    // ※上記説明を参照のこと。
    builder.serializeSpecialFloatingPointValues();

    return builder;
  }
}
