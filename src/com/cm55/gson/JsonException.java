package com.cm55.gson;

/**
 * このラッパライブラリで発生するすべての例外はこのオブジェクトにラップされる。
 * @author ysugimura
 */
public class JsonException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public JsonException(Throwable th) {
    super(th);
  }
  
  public JsonException(String message) {
    super(message);
  }
  
  protected JsonException() {    
  }
}
