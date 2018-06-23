/**
 * JSONユーティリティ
 * 
 * <p>
 * Google Gsonを使用し、任意のオブジェクトのJSONへの直列化、及びその復帰を行う。
 * Javaのシリアライゼーションに比較しての特徴は以下の通り。
 * </p>
 * <ul>
 * <li>パッケージ情報はセーブされないので、直列化後のクラスのパッケージを自由に移動できる。
 * <li>少なくとも復帰の時は、トップレベルのオブジェクトのクラスを指定しなければならない。
 * <li>抽象クラスを直列化する場合（実際にはそのサブクラスを直列化する場合）には、どのサブクラスであるのかを
 * JSONレベルに記述しておく必要がある。
 * <li>抽象クラスで無い場合（あるクラスの複数のサブクラスが直列化される場合）以外の場合には、実際に
 * 直列化されるオブジェクトと同一のクラスが指定されていなければならない。例えば、List<Foo>ではだめで、
 * ArrayList<Foo>などとする。
 * </ul>
 * <p>
 * 下位レベルではGoogle Gsonライブラリを使用するが、本ライブラリを使用する側ではGsonのAPIを気にする
 * 必要は無い。それらは本ライブラリのAPIでラップされている。
 * </p>
 * <h2>基本的な使用方法</h2>
 * <pre>
 * public static class Foo {
 * }
 * public static class FooHandler extends AbstractHandler<Foo> {
 *   public static final FooHandler INSTANCE = new FooHandler();
 *   public FooHandler() {
 *     super(Foo.class);
 *   }
 * }
 * ...
 * public void execute() {
 *   Serializer<Foo>serializer = new Serializer(FooHandler.INSTANCE);
 *   Foo in = new Foo();
 *   String json = serializer.serialize(in);
 *   Foo out = serializer.deserialize(json);
 * }
 * </pre>
 * <p>
 * すべてのハンドラはreadOnlyのため、例のように単一のオブジェクトを使いまわしてよい。
 * （トップレベルのFoo用の）ハンドラのインスタンスを指定してシリアライザを作成する。
 * </p>
 * <p>
 * 当然ながら、Fooクラスの中には様々なオブジェクトが存在するが、以下に述べる事情の無い限り何も指定する
 * 必要はない。
 * </p>
 * <h2>抽象クラスを直列化する場合</h2>
 * <p>
 * この場合には、{@link MultiHandler}を使用する。
 * </p>
 * <pre>
 * public static abstract class Bar {}
 * public static class Bar1 extends Bar {}
 * public static class Bar2 extends Bar {}
 * public static class BarHandler extends MultiTypeHandler<Bar> {
 *   public static final BarHandler INSTANCE = new BarHandler();
 *   public BarHandler() {
 *     super(Bar.class);
 *     add(Bar1.class);
 *     add(Bar2.class);
 *   }
 * }
 * </pre>
 * <p>
 * もしBarが先のFooの中から参照されている場合には、以下のようにする。
 * </p>
 * <pre>
 * public static class Foo {
 *   Bar bar;
 * }
 * public static class FooHandler extends AbstractHandler<Foo> {
 *   public static final FooHandler INSTANCE = new FooHandler();
 *   public Foo() {
 *     super(Foo.class);
 *     add(BarHandler.INSTANCE);
 *   }
 * }
 * </pre>
 * <h2>トップオブジェクトがジェネリックスの場合</h2>
 * <p>
 * トップレベルのオブジェクトがジェネリックスの場合には、特殊な処理が必要である。
 * Javaでは、クラス定義の中にしかジェネリックスの型パラメータ情報が無いので、TypeTokenを用いる
 * 必要がある。例えば、トップレベルがArrayList<Foo>型の場合には、以下のようにする。
 * </p>
 * <pre>
 * public static class FooArrayListHandler extends AbstractHandler<ArrayList<Foo>> {
 *   public static FooArrayListHandler INSTANCE = new FooArrayListHandler();
 *   public FooArrayListHandler() {
 *     super(new TypeToken<ArrayList<Foo>>() {});
 *     add(FooHandler.INSTANCE);
 *   }
 * }
 * </pre>
 */
package com.cm55.gson;

