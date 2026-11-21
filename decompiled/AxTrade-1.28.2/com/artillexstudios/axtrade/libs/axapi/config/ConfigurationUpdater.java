package com.artillexstudios.axtrade.libs.axapi.config;

import com.artillexstudios.axtrade.libs.axapi.config.annotation.ConfigurationPart;

@FunctionalInterface
public interface ConfigurationUpdater {
   <T extends ConfigurationPart> void update(YamlConfiguration<T> config);
}
