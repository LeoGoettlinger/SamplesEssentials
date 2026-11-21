package com.artillexstudios.axrankmenu.libs.axapi.metrics.collectors.implementation;

import com.artillexstudios.axrankmenu.libs.axapi.metrics.collectors.MetricsCollector;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;

public final class OnlinePlayersMetricsCollector implements MetricsCollector {
   @Override
   public void collect(JsonArray data) {
      JsonObject object = new JsonObject();
      object.addProperty("@type", "online-players");
      object.addProperty("online-players", Bukkit.getOnlinePlayers().size());
      data.add(object);
   }
}
