package me.clip.placeholderapi.libs.kyori.adventure.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface VirtualComponent extends TextComponent {
   @NotNull
   Class<?> contextType();

   @NotNull
   VirtualComponentRenderer<?> renderer();
}
