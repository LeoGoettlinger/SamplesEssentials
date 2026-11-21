package me.clip.placeholderapi.libs.kyori.adventure.text;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ComponentApplicable {
   @NotNull
   Component componentApply(@NotNull final Component component);
}
