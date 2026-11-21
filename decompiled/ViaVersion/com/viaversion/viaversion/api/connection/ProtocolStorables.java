package com.viaversion.viaversion.api.connection;

import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.ClientWorld;

public interface ProtocolStorables {
   EntityTracker entityTracker();

   void setEntityTracker(EntityTracker var1);

   ClientWorld clientWorld();

   void setClientWorld(ClientWorld var1);

   ItemHasher itemHasher();

   void setItemHasher(ItemHasher var1);
}
