package me.byteful.plugin.leveltools.libs.lamp.bukkit;

import java.util.List;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntitySelector<E extends Entity> extends List<E> {
   boolean containsExactly(@NotNull Entity... entities);
}
