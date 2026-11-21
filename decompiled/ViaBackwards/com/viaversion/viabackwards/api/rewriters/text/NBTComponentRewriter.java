package com.viaversion.viabackwards.api.rewriters.text;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viabackwards.api.data.TranslatableMappings;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NBTComponentRewriter<C extends ClientboundPacketType>
   extends com.viaversion.viaversion.rewriter.text.NBTComponentRewriter<C>
   implements TranslatableRewriter {
   private final Map<String, String> translatables;

   public NBTComponentRewriter(BackwardsProtocol<C, ?, ?, ?> protocol) {
      super(protocol);
      this.translatables = TranslatableMappings.translatablesFor(protocol);
   }

   public NBTComponentRewriter(BackwardsProtocol<C, ?, ?, ?> protocol, String version) {
      super(protocol);
      this.translatables = TranslatableMappings.translatablesFor(version);
   }

   protected void handleTranslate(JsonObject root, String translate) {
      String newTranslate = this.mappedTranslationKey(translate);
      if (newTranslate != null) {
         root.addProperty("translate", newTranslate);
      }
   }

   protected void handleTranslate(UserConnection connection, CompoundTag parentTag, StringTag translateTag) {
      String newTranslate = this.mappedTranslationKey(translateTag.getValue());
      if (newTranslate != null) {
         parentTag.put("translate", new StringTag(newTranslate));
      }
   }

   @Nullable
   @Override
   public String mappedTranslationKey(String translationKey) {
      return this.translatables.get(translationKey);
   }
}
