package com.viaversion.viaversion.connection;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.StorableObject;
import com.viaversion.viaversion.api.connection.UserConnection;

public abstract class StorableObjectTask<T extends StorableObject> implements Runnable {
   private final Class<T> storableObject;

   protected StorableObjectTask(Class<T> storableObject) {
      this.storableObject = storableObject;
   }

   public abstract void run(UserConnection var1, T var2);

   @Override
   public void run() {
      for (UserConnection connection : Via.getManager().getConnectionManager().getConnections()) {
         if (connection.isActive() && connection.has(this.storableObject)) {
            connection.getChannel().eventLoop().execute(() -> {
               T object = connection.get(this.storableObject);
               if (object != null) {
                  this.run(connection, object);
               }
            });
         }
      }
   }
}
