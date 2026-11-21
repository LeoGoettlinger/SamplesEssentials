package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import org.bukkit.Location;

public interface PacketTextEntity extends PacketModelEntity {
   void setText(String var1);

   void setTextVisible(boolean var1);

   void initializeText(Location var1);
}
