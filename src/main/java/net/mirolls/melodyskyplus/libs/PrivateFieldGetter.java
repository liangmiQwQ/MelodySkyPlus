package net.mirolls.melodyskyplus.libs;

import java.lang.reflect.Field;

public class PrivateFieldGetter {
  public static <T> T get(String className, String fieldName, Class<T> type) {
    try {
      Field field = Class.forName(className).getDeclaredField(fieldName);
      field.setAccessible(true);
      return type.cast(field.get(null));
    } catch (Exception e) {
      throw new IllegalStateException(
          "Cannot get " + className + " class " + fieldName + " field. ", e);
    }
  }
}
