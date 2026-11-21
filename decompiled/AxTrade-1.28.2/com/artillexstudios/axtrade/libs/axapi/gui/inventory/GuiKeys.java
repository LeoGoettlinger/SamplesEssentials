package com.artillexstudios.axtrade.libs.axapi.gui.inventory;

import com.artillexstudios.axtrade.libs.axapi.context.ContextKey;
import org.bukkit.entity.Player;

public class GuiKeys {
   public static final ContextKey<Player> PLAYER = ContextKey.of("player", Player.class);
   public static final ContextKey<Gui> GUI = ContextKey.of("gui", Gui.class);
}
