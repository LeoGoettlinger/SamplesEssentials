package org.mineskin.request;

@FunctionalInterface
public interface RequestInterceptor<R> {
   void intercept(R var1);
}
