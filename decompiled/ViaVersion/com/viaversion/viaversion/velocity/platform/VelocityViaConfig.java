package com.viaversion.viaversion.velocity.platform;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class VelocityViaConfig extends AbstractViaConfig {
   private int velocityPingInterval;
   private boolean velocityPingSave;
   private Map<String, Integer> velocityServerProtocols;

   public VelocityViaConfig(File folder, Logger logger) {
      super(new File(folder, "config.yml"), logger);
   }

   @Override
   protected void loadFields() {
      super.loadFields();
      this.velocityPingInterval = this.getInt("velocity-ping-interval", 60);
      this.velocityPingSave = this.getBoolean("velocity-ping-save", true);
      this.velocityServerProtocols = this.get("velocity-servers", new HashMap<>());
   }

   @Override
   protected void handleConfig(Map<String, Object> config) {
      Map<String, Object> servers;
      if (config.get("velocity-servers") instanceof Map velocityServers) {
         servers = velocityServers;
      } else {
         servers = new HashMap<>();
      }

      for (Entry<String, Object> entry : new HashSet<>(servers.entrySet())) {
         if (!(entry.getValue() instanceof Integer)) {
            if (entry.getValue() instanceof String protocol) {
               ProtocolVersion found = ProtocolVersion.getClosest(protocol);
               if (found != null) {
                  servers.put(entry.getKey(), found.getVersion());
               } else {
                  servers.remove(entry.getKey());
               }
            } else {
               servers.remove(entry.getKey());
            }
         }
      }

      if (!servers.containsKey("default")) {
         try {
            servers.put("default", VelocityViaInjector.getLowestSupportedProtocolVersion());
         } catch (Exception var7) {
            var7.printStackTrace();
         }
      }

      config.put("velocity-servers", servers);
   }

   @Override
   public List<String> getUnsupportedOptions() {
      return BUKKIT_ONLY_OPTIONS;
   }

   @Override
   public Set<String> getSectionsWithModifiableKeys() {
      return Set.of("velocity-servers");
   }

   public int getVelocityPingInterval() {
      return this.velocityPingInterval;
   }

   public boolean isVelocityPingSave() {
      return this.velocityPingSave;
   }

   public Map<String, Integer> getVelocityServerProtocols() {
      return this.velocityServerProtocols;
   }
}
