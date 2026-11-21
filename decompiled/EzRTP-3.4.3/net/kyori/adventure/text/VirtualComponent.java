package net.kyori.adventure.text;

public sealed interface VirtualComponent extends TextComponent permits VirtualComponentImpl {
   Class<?> contextType();

   VirtualComponentRenderer<?> renderer();
}
