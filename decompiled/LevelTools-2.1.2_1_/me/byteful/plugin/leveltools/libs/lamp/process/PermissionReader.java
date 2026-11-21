package me.byteful.plugin.leveltools.libs.lamp.process;

import me.byteful.plugin.leveltools.libs.lamp.command.CommandPermission;
import me.byteful.plugin.leveltools.libs.lamp.command.trait.CommandAnnotationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PermissionReader {
   @Nullable
   CommandPermission getPermission(@NotNull CommandAnnotationHolder var1);
}
