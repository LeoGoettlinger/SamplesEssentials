package com.artillexstudios.axafkzone.libs.axapi.gui.configuration.actions;

public interface ActionProvider<T extends Action<?>> {
   T provide(String data);
}
