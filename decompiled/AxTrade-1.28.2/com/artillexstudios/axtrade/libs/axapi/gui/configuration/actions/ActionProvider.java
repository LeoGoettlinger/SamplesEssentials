package com.artillexstudios.axtrade.libs.axapi.gui.configuration.actions;

public interface ActionProvider<T extends Action<?>> {
   T provide(String data);
}
