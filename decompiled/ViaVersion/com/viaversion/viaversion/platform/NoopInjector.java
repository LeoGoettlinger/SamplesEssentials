package com.viaversion.viaversion.platform;

import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectLinkedOpenHashSet;
import com.viaversion.viaversion.libs.gson.JsonObject;
import java.util.SortedSet;

public class NoopInjector implements ViaInjector {
   @Override
   public void inject() {
   }

   @Override
   public void uninject() {
   }

   @Override
   public ProtocolVersion getServerProtocolVersion() throws Exception {
      return this.getServerProtocolVersions().first();
   }

   @Override
   public SortedSet<ProtocolVersion> getServerProtocolVersions() {
      SortedSet<ProtocolVersion> versions = new ObjectLinkedOpenHashSet<>();
      versions.addAll(ProtocolVersion.getProtocols());
      return versions;
   }

   @Override
   public JsonObject getDump() {
      return new JsonObject();
   }
}
