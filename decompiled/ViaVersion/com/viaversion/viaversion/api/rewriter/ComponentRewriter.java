package com.viaversion.viaversion.api.rewriter;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.libs.gson.JsonElement;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface ComponentRewriter {
   void processTag(UserConnection var1, @Nullable Tag var2);

   void processText(UserConnection var1, @Nullable JsonElement var2);

   JsonElement processText(UserConnection var1, @Nullable String var2);

   void handleShowItem(UserConnection var1, CompoundTag var2);

   void passthroughAndProcess(PacketWrapper var1);
}
