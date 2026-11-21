package org.mineskin.request;

@FunctionalInterface
public interface ResponseInterceptor<R> {
   void intercept(R var1);
}
