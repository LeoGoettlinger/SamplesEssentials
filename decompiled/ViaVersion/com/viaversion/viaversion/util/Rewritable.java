package com.viaversion.viaversion.util;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.rewriter.ItemRewriter;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Rewritable {
   Object rewrite(UserConnection var1, Protocol<?, ?, ?, ?> var2, boolean var3);

   static int rewriteDataComponentType(Protocol<?, ?, ?, ?> protocol, boolean clientbound, int typeId) {
      FullMappings mappings = protocol.getMappingData().getDataComponentSerializerMappings();
      return mappings == null ? typeId : (clientbound ? mappings.getNewId(typeId) : mappings.inverse().getNewId(typeId));
   }

   static int rewriteSound(Protocol<?, ?, ?, ?> protocol, boolean clientbound, int soundId) {
      return protocol.getMappingData().getSoundMappings() == null
         ? soundId
         : (clientbound ? protocol.getMappingData().getNewSoundId(soundId) : protocol.getMappingData().getOldSoundId(soundId));
   }

   static int rewriteItem(Protocol<?, ?, ?, ?> protocol, boolean clientbound, int itemId) {
      return protocol.getMappingData().getItemMappings() == null
         ? itemId
         : (clientbound ? protocol.getMappingData().getNewItemId(itemId) : protocol.getMappingData().getOldItemId(itemId));
   }

   static String rewriteItem(Protocol<?, ?, ?, ?> protocol, boolean clientbound, String itemId) {
      FullMappings mappings = protocol.getMappingData().getFullItemMappings();
      return mappings == null ? itemId : (clientbound ? mappedIdentifier(mappings, itemId) : unmappedIdentifier(mappings, itemId));
   }

   @Nullable
   static Item rewriteItem(UserConnection connection, Protocol<?, ?, ?, ?> protocol, boolean clientbound, Item item) {
      ItemRewriter<?> rewriter = protocol.getItemRewriter();
      return rewriter == null ? item : (clientbound ? rewriter.handleItemToClient(connection, item) : rewriter.handleItemToServer(connection, item));
   }

   static Int2IntFunction itemRewriteFunction(Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return protocol.getMappingData().getItemMappings() == null
         ? Int2IntFunction.identity()
         : (clientbound ? protocol.getMappingData()::getNewItemId : protocol.getMappingData()::getOldItemId);
   }

   static Int2IntFunction blockRewriteFunction(Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return protocol.getMappingData().getBlockMappings() == null
         ? Int2IntFunction.identity()
         : (clientbound ? protocol.getMappingData()::getNewBlockId : protocol.getMappingData()::getOldBlockId);
   }

   static Int2IntFunction soundRewriteFunction(Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return protocol.getMappingData().getSoundMappings() == null
         ? Int2IntFunction.identity()
         : (clientbound ? protocol.getMappingData().getSoundMappings()::getNewId : protocol.getMappingData()::getOldSoundId);
   }

   static Int2IntFunction entityRewriteFunction(Protocol<?, ?, ?, ?> protocol, boolean clientbound) {
      return protocol.getMappingData().getEntityMappings() == null
         ? Int2IntFunction.identity()
         : (clientbound ? protocol.getMappingData().getEntityMappings()::getNewId : protocol.getMappingData().getEntityMappings().inverse()::getNewId);
   }

   @Nullable
   static String mappedIdentifier(FullMappings mappings, String identifier) {
      return mappings.id(identifier) == -1 ? identifier : mappings.mappedIdentifier(identifier);
   }

   @Nullable
   static String unmappedIdentifier(FullMappings mappings, String mappedIdentifier) {
      return mappings.mappedId(mappedIdentifier) == -1 ? mappedIdentifier : mappings.identifier(mappedIdentifier);
   }
}
