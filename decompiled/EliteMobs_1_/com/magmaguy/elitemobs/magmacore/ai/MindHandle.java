package com.magmaguy.elitemobs.magmacore.ai;

public interface MindHandle extends AutoCloseable {
   void attachBody(MobBody var1);

   void detachBody();

   void suspendBody();

   void setPaused(boolean var1);

   SwapReceipt replace(MindProgram var1, StateTransfer var2);

   MindSnapshot inspect();

   @Override
   void close();
}
