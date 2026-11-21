package com.earth2me.essentials.libs.configurate.reactive;

public interface TransactionalSubscriber<V> extends Subscriber<V> {
   @Override
   default void submit(V item) {
      try {
         this.beginTransaction(item);
         this.commit();
      } catch (TransactionFailedException var3) {
         this.rollback();
      } catch (Exception var4) {
         this.rollback();
         throw var4;
      }
   }

   void beginTransaction(V newValue) throws TransactionFailedException;

   void commit();

   void rollback();
}
