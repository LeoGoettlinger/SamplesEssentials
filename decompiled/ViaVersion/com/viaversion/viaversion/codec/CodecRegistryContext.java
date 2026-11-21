package com.viaversion.viaversion.codec;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.codec.CodecContext;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.types.version.VersionedTypesHolder;
import com.viaversion.viaversion.libs.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Set;

public record CodecRegistryContext(Protocol<?, ?, ?, ?> protocol, CodecContext.RegistryAccess registryAccess, boolean mapped) implements CodecContext {
   private static final Set<StructuredDataKey<?>> NOT_IMPLEMENTED = new ReferenceOpenHashSet<>(List.of());

   public CodecRegistryContext(final Protocol<?, ?, ?, ?> protocol, final CodecContext.RegistryAccess registryAccess, final boolean mapped) {
      this.protocol = protocol;
      this.registryAccess = registryAccess.withMapped(mapped);
      this.mapped = mapped;
   }

   @Override
   public boolean isSupported(StructuredDataKey<?> key) {
      if (this.protocol == null) {
         return !NOT_IMPLEMENTED.contains(key);
      } else {
         VersionedTypesHolder types = this.mapped ? this.protocol.mappedTypes() : this.protocol.types();
         return (NOT_IMPLEMENTED.isEmpty() || !NOT_IMPLEMENTED.contains(key)) && types.structuredDataKeys().supportsOps(key);
      }
   }

   @Override
   public UserConnection connection() {
      return this.registryAccess.connection();
   }
}
