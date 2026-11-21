package com.artillexstudios.axafkzone.libs.axapi.config;

import com.artillexstudios.axafkzone.libs.axapi.config.annotation.ConfigurationPart;

@FunctionalInterface
public interface ConfigurationUpdater {
   <T extends ConfigurationPart> void update(YamlConfiguration<T> config);
}
