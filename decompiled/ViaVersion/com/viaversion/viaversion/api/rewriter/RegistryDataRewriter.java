package com.viaversion.viaversion.api.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;

public interface RegistryDataRewriter {
   void handle(PacketWrapper var1);

   boolean shouldRemoveRegistry(String var1);

   boolean hasRegistriesToRemove();

   void updateDialog(UserConnection var1, CompoundTag var2);

   boolean updateBlockStateProvider(CompoundTag var1);

   void sendMissingRegistries(UserConnection var1);
}
