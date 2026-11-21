package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.List;
import org.bukkit.entity.Player;

public interface AbstractPacketBundle {
   void addPacket(Object var1, List<Player> var2);

   void send();
}
