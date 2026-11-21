package com.viaversion.viaversion.api.minecraft.codec;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataKey;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.util.Key;

public interface CodecContext {
   CodecContext.RegistryAccess registryAccess();

   boolean mapped();

   boolean isSupported(StructuredDataKey<?> var1);

   UserConnection connection();

   Protocol<?, ?, ?, ?> protocol();

   public interface RegistryAccess {
      UserConnection connection();

      Key item(int var1);

      Key attributeModifier(int var1);

      Key dataComponentType(int var1);

      Key entity(int var1);

      Key blockEntity(int var1);

      Key sound(int var1);

      Key key(MappingData.MappingType var1, int var2);

      int id(MappingData.MappingType var1, String var2);

      static CodecContext.RegistryAccess of(Protocol<?, ?, ?, ?> protocol, UserConnection connection) {
         return new RegistryAccessImpl(protocol, connection);
      }

      Key registryKey(String var1, int var2);

      CodecContext.RegistryAccess withMapped(boolean var1);
   }
}
