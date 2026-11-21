package com.viaversion.viaversion.api.data.item;

public interface ItemHasher {
   boolean isProcessingClientboundInventoryPacket();

   void setProcessingClientboundInventoryPacket(boolean var1);
}
