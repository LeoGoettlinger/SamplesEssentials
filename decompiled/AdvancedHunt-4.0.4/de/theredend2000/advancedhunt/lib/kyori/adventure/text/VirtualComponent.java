package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

public sealed interface VirtualComponent extends TextComponent permits VirtualComponentImpl {
   Class<?> contextType();

   VirtualComponentRenderer<?> renderer();
}
