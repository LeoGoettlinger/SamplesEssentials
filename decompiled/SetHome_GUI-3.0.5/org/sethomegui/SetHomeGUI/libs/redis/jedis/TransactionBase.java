package org.sethomegui.SetHomeGUI.libs.redis.jedis;

@Deprecated
public abstract class TransactionBase extends AbstractTransaction {
   @Deprecated
   protected TransactionBase() {
   }

   protected TransactionBase(CommandObjects commandObjects) {
      super(commandObjects);
   }
}
