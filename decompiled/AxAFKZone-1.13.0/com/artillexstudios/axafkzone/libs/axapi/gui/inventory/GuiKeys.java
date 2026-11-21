package com.artillexstudios.axafkzone.libs.axapi.gui.inventory;

import com.artillexstudios.axafkzone.libs.axapi.context.ContextKey;
import org.bukkit.entity.Player;

public class GuiKeys {
   public static final ContextKey<Player> PLAYER = ContextKey.of("player", Player.class);
   public static final ContextKey<Gui> GUI = ContextKey.of("gui", Gui.class);
}
