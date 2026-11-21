package com.artillexstudios.axtrade.libs.axapi.reflection;

import com.artillexstudios.axtrade.libs.axapi.libs.caffeine.caffeine.cache.Caffeine;
import com.artillexstudios.axtrade.libs.axapi.libs.caffeine.caffeine.cache.LoadingCache;
import com.artillexstudios.axtrade.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.jspecify.annotations.Nullable;

public enum MethodUtils {
   INSTANCE;

   private final LoadingCache<MethodUtils.MethodCacheKey, Method> METHOD_CACHE = Caffeine.newBuilder().maximumSize(50L).build(this::getMethod);

   private Method getMethod(MethodUtils.MethodCacheKey key) {
      try {
         return key.clazz().getDeclaredMethod(key.method(), key.params());
      } catch (NoSuchMethodException var3) {
         LogUtils.error("Failed to find method {}!", key.method(), var3);
         return null;
      }
   }

   @Nullable
   public Method getMethod(Class<?> clazz, String method) {
      return this.getMethod(clazz, method);
   }

   @Nullable
   public Method getMethod(Class<?> clazz, String method, Class<?>... params) {
      return (Method)this.METHOD_CACHE.get(new MethodUtils.MethodCacheKey(clazz, method, params));
   }

   public <T> T invokeStaticMethod(Method method, Object... params) {
      return this.invokeMethod(method, null, params);
   }

   public <T> T invokeMethod(Method method, T instance, Object... params) {
      try {
         return UncheckedUtils.unsafeCast(method.invoke(instance, params));
      } catch (InvocationTargetException | IllegalAccessException var5) {
         LogUtils.error("Failed to invok method {} on instance {} with arguments {}!", method, instance, params);
         return null;
      }
   }

   private record MethodCacheKey(Class<?> clazz, String method, Class<?>[] params) {
   }
}
