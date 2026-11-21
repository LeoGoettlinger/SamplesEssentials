package com.artillexstudios.axafkzone.libs.lamp.process;

import com.artillexstudios.axafkzone.libs.lamp.command.CommandPermission;
import com.artillexstudios.axafkzone.libs.lamp.command.trait.CommandAnnotationHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PermissionReader {
   @Nullable
   CommandPermission getPermission(@NotNull CommandAnnotationHolder var1);
}
