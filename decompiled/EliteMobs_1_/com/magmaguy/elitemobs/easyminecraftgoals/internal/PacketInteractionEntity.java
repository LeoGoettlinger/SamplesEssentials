package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.function.BiConsumer;
import org.bukkit.entity.Player;

public interface PacketInteractionEntity extends PacketEntityInterface {
   void setSize(float var1, float var2);

   float getWidth();

   float getHeight();

   @Override
   void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> var1);

   @Override
   void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> var1);
}
