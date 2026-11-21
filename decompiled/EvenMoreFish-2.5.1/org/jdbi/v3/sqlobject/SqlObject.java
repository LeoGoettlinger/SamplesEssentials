package org.jdbi.v3.sqlobject;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.HandleConsumer;

public interface SqlObject {
   Handle getHandle();

   <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X;

   default <X extends Exception> void useHandle(HandleConsumer<X> consumer) throws X {
      this.withHandle(consumer.asCallback());
   }
}
