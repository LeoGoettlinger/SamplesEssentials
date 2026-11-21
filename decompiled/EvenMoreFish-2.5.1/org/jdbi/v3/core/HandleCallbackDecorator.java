package org.jdbi.v3.core;

import org.jdbi.v3.meta.Alpha;

@Alpha
public interface HandleCallbackDecorator {
   HandleCallbackDecorator STANDARD_HANDLE_CALLBACK_DECORATOR = new HandleCallbackDecorator() {
      @Override
      public <R, X extends Exception> HandleCallback<R, X> decorate(HandleCallback<R, X> callback) {
         return callback;
      }
   };

   <R, X extends Exception> HandleCallback<R, X> decorate(HandleCallback<R, X> callback);
}
