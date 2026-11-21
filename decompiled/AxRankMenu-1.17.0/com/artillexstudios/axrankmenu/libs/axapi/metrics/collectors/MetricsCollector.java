package com.artillexstudios.axrankmenu.libs.axapi.metrics.collectors;

import com.google.gson.JsonArray;

public interface MetricsCollector {
   void collect(JsonArray data);
}
