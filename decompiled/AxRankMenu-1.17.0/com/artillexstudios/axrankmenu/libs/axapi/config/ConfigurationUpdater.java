package com.artillexstudios.axrankmenu.libs.axapi.config;

import com.artillexstudios.axrankmenu.libs.axapi.config.annotation.ConfigurationPart;

@FunctionalInterface
public interface ConfigurationUpdater {
   <T extends ConfigurationPart> void update(YamlConfiguration<T> config);
}
