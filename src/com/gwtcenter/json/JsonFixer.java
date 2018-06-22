package com.gwtcenter.json;

import java.util.*;

import com.google.inject.*;
import com.gwtcenter.json.Json.*;


/**
 * Json配列を修正するオブジェクト
 * <p>
 * XMLからJSONに変換した場合、配列要素が一つしか無い場合には、JSON配列にならない場合がある。
 * このため、JSON配列でなければならない要素を指定し、もし配列でなければ強制的に配列にしてしまう。
 * </p>
 * <p>
 * 空配列があった場合にそれをオブジェクトに変換する。
 * </p>
 * @author ysugimura
 */
@Singleton
public class JsonFixer {
  /**
   * 修正ノード
   * @author ysugimura
   */
  public abstract static class Node {
    String name;
    private Map<String, Node> subNodeMap;
    
    public Node(String name, Node... nodes) {
      this.name = name;
      addNodes(nodes);
    }
    
    private void addNodes(Node[]nodes) {
      if (nodes.length == 0) return;
      if (subNodeMap == null) 
        subNodeMap = new HashMap<String, Node>();
      for (Node node : nodes) {
        subNodeMap.put(node.name, node);
      }     
    }
    
    public boolean hasSubMap() {
      return subNodeMap != null;
    }

    public Node getSubNode(String name) {
      return subNodeMap.get(name);
    }
    
    @Override
    public String toString() {
      return name;
    }
  }

  /** ルートとして用いる。フィールド名称はない */
  public static class FixRoot extends Node {
    public FixRoot(Node...nodes) {
      super("ROOT", nodes);
    }
  }
  
  /**
   * このノードに関しては何もしない。
   * @author ysugimura
   */
  public static class FixNone extends Node {
    public FixNone(String name, Node...nodes){
      super(name, nodes);
    }
    
  }
  /**
   * 配列を強制する
   * <p>
   * 主にXMLからJSONに変換した場合、XMLには配列を表現する方法が無いため、
   * 要素が一つの場合にはJSONオブジェクトになり、複数の場合にはJSON配列になるという現象が起こったりする。
   * このため、指定されたノードがJSONオブジェクトの場合には強制的にJSON配列に変更する。
   * </p>
   * @author ysugimura
   */
  public static class ForceArray extends Node {    
    public ForceArray(String name, Node...nodes) {
      super(name, nodes);
    }
  }
  
  /**
   * 空配列があったら、その項目自体を削除する。
   * <p>
   * これはMAKESHOPのJSON-APIで起こるのだが、中身がある場合にはJSONオブジェクトとして得られるが、
   * 中身の無い場合には、何故か空のJSON配列になっている。この場合に項目自体を削除する。
   * </p>
   */
  public static class DropEmptyArray extends Node {
    public DropEmptyArray(String name, Node...nodes) {
      super(name, nodes);
    }
  }
  
  /**
   * プリミティブであってはならないところにプリミティブがあった場合、その項目自体を削除する。
   * @author admin
   *
   */
  public static class DropPrimitive extends Node {
    public DropPrimitive(String name, Node...nodes) {
      super(name, nodes);
    }
  }
  
  /**
   * フィールド名称を変更する。
   * <p>
   * jsonフィールドとして"default"などという名前が使われているとJavaオブジェクトへのマッピングができないため、
   * 強制的にフィールド名称を変更する。
   * </p>
   */
  public static class ChangeFieldName extends Node {
    private String to;
    public ChangeFieldName(String from, String to, Node...nodes) {
      super(from, nodes);
      this.to = to;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  //
  ////////////////////////////////////////////////////////////////////////////
  
  @Inject private Json.Factory jsonFactory;

  public String fix(String json, Node node) {
    return fix((JElement) jsonFactory.get(json), node);
  }

  /**
   * 
   * @param element
   * @param node
   * @return
   */
  public String fix(JElement element, FixRoot root) {
    return fix(element, root);
  }
  
  private String fix(JElement parent, Node node) {
    // ystem.out.println("parent:" + element.getJson());
    if (!node.hasSubMap()) {
      return parent.getJson();
    }

    // この要素が配列のとき
    if (parent instanceof JArray) {
      return fixArray((JArray)parent, node);
    }

    // マップのとき
    return fixObject((JObject)parent, node);
  }

  private String fixObject(JObject object, Node node) {
    Stocker s = new Stocker();
    for (Map.Entry<String, JElement> e : object.entrySet()) {
      String childName = e.getKey();
      JElement childElement = e.getValue();
      Node subNode = node.getSubNode(childName);
      fixChild(s, childName, childElement, subNode);
    }
    return s.asObjectString();    
  }
  
  private void fixChild(Stocker s, String childName, JElement childElement, Node subNode) {        
    if (subNode == null) {
      s.addItemJson(childName, childElement.getJson());
      return;
    }    
    fixMap.get(subNode.getClass()).fix(s, childName, childElement, subNode);
  }
  
  private Map<Class<? extends Node>, Executer>fixMap = new HashMap<Class<? extends Node>, Executer>();
  {
    fixMap.put(FixNone.class,  new FixNoneExecuter());
    fixMap.put(DropEmptyArray.class, new DropEmptyArrayExecuter());
    fixMap.put(DropPrimitive.class, new DropPrimitiveExecuter());
    fixMap.put(ForceArray.class, new ForceArrayExecuter());
    fixMap.put(ChangeFieldName.class,  new ChangeFieldNameExecuter());
  }
  
  public class Executer {
    public void fix(Stocker s, String childName, JElement childElement, Node subNode) {
      s.addItemJson(childName, JsonFixer.this.fix(childElement, subNode));     
    }
  }
  
  class FixNoneExecuter extends Executer {
  }
  
  class DropEmptyArrayExecuter extends Executer {
    public void fix(Stocker s, String childName, JElement childElement, Node subNode) {
      // このノードを無視する
      if (childElement instanceof JArray && ((JArray)childElement).size() == 0)
        return;
      super.fix(s, childName, childElement, subNode);
    }
  }
  
  class DropPrimitiveExecuter extends Executer {
    public void fix(Stocker s, String childName, JElement childElement, Node subNode) {
      // このノードを無視する
      if (childElement instanceof JPrimitive)
        return;
      super.fix(s, childName, childElement, subNode);
    }
  }

  class ForceArrayExecuter extends Executer {
    public void fix(Stocker s, String childName, JElement childElement, Node subNode) {
      if (!(childElement instanceof JArray))
        s.addItemJson(childName, "[" + JsonFixer.this.fix(childElement, subNode) + "]");
      else
        super.fix(s, childName, childElement, subNode);  
    }
  }
  
  class ChangeFieldNameExecuter extends Executer {
    public void fix(Stocker s, String childName, JElement childElement, Node subNode) {
      ChangeFieldName node = (ChangeFieldName)subNode;
      assert childName.equals(node.name);
      s.addItemJson(node.to, JsonFixer.this.fix(childElement, subNode));     
    }
  }

  /**
   * 配列を修正する
   * @param array
   * @param node
   * @return
   */
  private String fixArray(JArray array, Node node) {
    Stocker s = new Stocker();
    array.forEach(element-> {
      s.addJson(fix(element, node));
    });      
    return s.asArrayString();
  }

  /**
   * JSON文字列のストッカ
   * @author ysugimura
   *
   */
  public static class Stocker {
    StringBuilder s = new StringBuilder();

    public void addItemJson(String itemName, String json) {
      if (s.length() > 0) s.append(",");
      s.append("\"" + itemName + "\":" + json);
    }
    
    public void addJson(String json) {
      if (s.length() > 0) s.append(",");
      s.append(json);
    }
    
    public String asObjectString() {
      return "{" + s.toString() + "}";
    }
    
    public String asArrayString() {
      return "[" + s.toString() + "]";
    }
  }
}
