package com.artillexstudios.axtrade.libs.lamp.process;

import com.artillexstudios.axtrade.libs.lamp.command.CommandPermission;
import com.artillexstudios.axtrade.libs.lamp.command.trait.CommandAnnotationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PermissionReader {
   @Nullable
   CommandPermission getPermission(@NotNull CommandAnnotationHolder var1);
}
