package com.cm55.gson;

/**
 * 本ライブラリの動作を変更するフラグ類
 * @author ysugimura
 */
public class Settings {

  /** JSONのエンコーディング。出力時にはこのエンコーディングで出力し、入力はこのエンコーディングであるものとする */
  public static String ENCODING = "UTF-8";
  
  /** 
   * マルチハンドラ・タイプフィールドマーカ 
   * {@link MultiHandler}によってJSON化される場合に、タイプとして使用するフィールドの名称。例えば以下になる。
   * <pre>
   * {"T":"BarTwo","D":{"b":2}}
   * </pre>
   */
  public static String MULTIHANDLER_TYPE_MARKER = "T";

  /** 
   * マルチハンドラ・データフィールドマーカ
   * {@link MultiHandler}によってJSON化される場合に、データとして使用するフィールドの名称。例えば以下になる。
   * <pre>
   * {"T":"BarTwo","D":{"b":2}}
   * </pre>
   */
  public static String MULTIHANDLER_DATA_MARKER = "D";
  
    
  /**
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
   */
  public static boolean ENABLE_COMPLEX_MAP_KEY_SERIALIZATION = true;

  /**
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
   */
  public static boolean SERIALIZE_NULLS = true;

  /**
   * <p>
   * JSON自体の仕様ではNaNやInfiniteは存在しないが、これが無いと値が落ちてしまうためサポートする。
   * </p>
   */
  public static boolean SERIALIZE_SPECIAL_FLOATING_POINT_VALUES = true;
  
}
