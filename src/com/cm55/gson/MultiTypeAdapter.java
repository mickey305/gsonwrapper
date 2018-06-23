package com.cm55.gson;

import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.*;

/**
 * JSON化するオブジェクト中のフィールドに格納されるタイプが単一でないときがある。
 * 例えば、「Sample sample;」というフィールドには、次のいずれかのオブジェクトが格納される場合がある。
 * <pre>
 * class abstract class Sample { ... }
 * class SampleA extends Sample { ... }
 * class SampleB extends Sample { ... }
 * </pre>
 * <p>
 * として、
 * </p>
 * <pre>
 * class Foo {
 *   Sample sample;
 *   Foo foo;
 * }
 * </pre>
 * <p>
 * このような場合には、フィールド値をそのまま格納してはいけない。直列化復帰の際に、SampleA、SampleBのいずれのオブジェクトであるか
 * がわからなくなるからである。直列化のときにも直列化復帰の時にも、格納されるオブジェクトについて特別な処理を行わなければならない。
 * </p>
 * <p>
 * {@link MultiTypeAdapter}は、あるクラスの代わりとなる可能性のあるすべてのクラスを定義しておき、直列化の際にも直列化復帰の際にも
 * どのクラスであるかを識別するために用いられる。具体的には、以下のように行う。
 * </p>
 * <pre>
 * class MyAdapter extends MultiTypeAdapter<Sample> {
 *   public MyAdapter() {
 *     super(Sample.class);
 *     add("a", SampleA.class);
 *     add("b", SampleB.class);
 *   }
 * }
 * </pre>
 * <h2>注意事項</h2>
 * <p>
 * ジェネリックスはサポートすることはできない。その理由としては
 * </p>
 * <ul>
 * <li>JSON側からオブジェクトがもたらされ、その直列化指示がされるが、当然ながらそれはオブジェクトのみであり、
 * 型パラメータについては何もわからない。
 * </ul>
 * @author ysugimura
 *
 * @param <T>
 */
public class MultiTypeAdapter<T> extends Adapter<T> {

  /** TypeTokenのマップ */
  private final TypeTokenNameMap typeTokenMap;
  
  /** 
   * 処理対象タイプを指定する
   * @param targetType
   */
   MultiTypeAdapter(TypeToken<T> targetType,  List<Adapter<?>>subAdapters, TypeTokenNameMap typeTokenMap) {
    super(targetType, subAdapters);
    this.typeTokenMap = typeTokenMap;
  }
  
  /**
   * 登録済のサブクラスの数
   */
  public int subClassCount() {
    return typeTokenMap.count();
  }
    
  /**
   * Gsonビルダに登録する
   */
  protected void registerToBuilder(GsonBuilder builder) {   
    
    if (typeTokenMap.count() == 0) {
      // 間違えてこのクラスを使用した場合の例外通知
      throw new JsonException("typeTokenMap empty");
    }
    
    // 環境＝TypeAdapterFactoryをGsonに登録する
    builder.registerTypeAdapterFactory(new MultiTypeAdapterFactory<T>(
        typeToken,
        this.typeTokenMap.duplicate()
    ));
    
    super.registerToBuilder(builder);
  }
}
