package com.lishid.openinv.util.setting;

import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface PlayerToggle {
   @NotNull
   String getName();

   boolean is(@NotNull UUID var1);

   boolean is(@NotNull Player var1);

   boolean set(@NotNull UUID var1, boolean var2);
}
