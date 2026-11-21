package eu.kennytv.maintenance.core.dump;

import com.google.gson.JsonObject;
import eu.kennytv.maintenance.core.MaintenancePlugin;
import eu.kennytv.maintenance.core.Settings;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class MaintenanceDump {
   private final ServerDump general;
   private final Map<String, Object> configuration;
   private final JsonObject plugins;

   public MaintenanceDump(MaintenancePlugin plugin, Settings settings) {
      this.general = new ServerDump(plugin.getVersion(), plugin.getServerType().toString(), plugin.getServerVersion(), plugin.getMaintenanceServersDump());
      this.configuration = new LinkedHashMap<>(settings.getConfig().getValues());
      this.addDBSettings("redis", "enabled");
      this.configuration.put("whitelisted-players", settings.getWhitelistedPlayers());
      this.configuration.put("icon-exists", new File(plugin.getDataFolder(), "maintenance-icon.png").exists());
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("plugins", MaintenancePlugin.GSON.toJsonTree(plugin.getPlugins()));
      this.plugins = jsonObject;
   }

   private void addDBSettings(String key, String... allowedSubKeys) {
      Set<String> allowedSubKeysSet = Set.of(allowedSubKeys);
      Object o = this.configuration.get(key);
      if (o instanceof Map) {
         Map<String, Object> map = new LinkedHashMap<>((Map<? extends String, ? extends Object>)o);
         map.keySet().removeIf(subKey -> !allowedSubKeysSet.contains(subKey));
         this.configuration.put(key, map);
      }
   }
}
