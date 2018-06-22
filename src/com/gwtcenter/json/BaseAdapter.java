package com.gwtcenter.json;

import com.google.gson.reflect.*;

/**
 * ベースアダプタ
 * @author ysugimura
 *
 * @param <T>
 */
public class BaseAdapter<T> extends AbstractAdapter<T> {
  
  public BaseAdapter(Class<T>clazz) {
    super(clazz);
  }
  
  public BaseAdapter(TypeToken<T>token) {
    super(token);
  }
}
