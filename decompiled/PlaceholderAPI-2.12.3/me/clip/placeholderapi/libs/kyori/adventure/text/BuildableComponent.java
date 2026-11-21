package me.clip.placeholderapi.libs.kyori.adventure.text;

import me.clip.placeholderapi.libs.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.NotNull;

public interface BuildableComponent<C extends BuildableComponent<C, B>, B extends ComponentBuilder<C, B>> extends Buildable<C, B>, Component {
   @NotNull
   B toBuilder();
}
