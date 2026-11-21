package com.viaversion.viaversion.protocols.v1_21_4to1_21_5.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.item.ItemHasher;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.codec.hash.Hasher;
import com.viaversion.viaversion.api.minecraft.data.StructuredData;
import com.viaversion.viaversion.codec.CodecRegistryContext;
import com.viaversion.viaversion.codec.hash.HashFunction;
import com.viaversion.viaversion.codec.hash.HashOps;
import com.viaversion.viaversion.data.item.ItemHasherBase;
import com.viaversion.viaversion.protocols.v1_21_4to1_21_5.Protocol1_21_4To1_21_5;
import java.util.concurrent.ExecutionException;

public class ItemHashStorage1_21_5 implements ItemHasher {
   private final Cache<Long, StructuredData<?>> hashToStructuredData = CacheBuilder.newBuilder().concurrencyLevel(1).maximumSize(512L).build();
   private boolean processingClientboundInventoryPacket;
   private final CodecContext context;

   public ItemHashStorage1_21_5(Protocol1_21_4To1_21_5 protocol, UserConnection connection) {
      CodecContext.RegistryAccess registryAccess = CodecContext.RegistryAccess.of(protocol, connection);
      this.context = new CodecRegistryContext(protocol, registryAccess, true);
   }

   public void trackStructuredData(StructuredData<?> structuredData) {
      if (!structuredData.isEmpty()) {
         HashOps hasher = new HashOps(this.context, HashFunction.crc32c());
         int hash = hasher.context().isSupported(structuredData.key()) ? hash(hasher, structuredData) : ItemHasherBase.UNKNOWN_HASH;
         if (hash != ItemHasherBase.UNKNOWN_HASH) {
            long key = (long)structuredData.id() << 32 | hash;

            try {
               this.hashToStructuredData.get(key, structuredData::copy);
            } catch (ExecutionException var7) {
               throw new RuntimeException(var7);
            }
         }
      }
   }

   public StructuredData<?> dataFromHash(int dataComponentId, int hash) {
      long key = (long)dataComponentId << 32 | hash;
      StructuredData<?> data = (StructuredData<?>)this.hashToStructuredData.getIfPresent(key);
      return data != null ? data.copy() : null;
   }

   private static <T> int hash(Hasher hasher, StructuredData<T> data) {
      hasher.write(data.key().type(), data.value());
      return hasher.hash();
   }

   @Override
   public boolean isProcessingClientboundInventoryPacket() {
      return this.processingClientboundInventoryPacket;
   }

   @Override
   public void setProcessingClientboundInventoryPacket(boolean processingClientboundInventoryPacket) {
      this.processingClientboundInventoryPacket = processingClientboundInventoryPacket;
   }
}
