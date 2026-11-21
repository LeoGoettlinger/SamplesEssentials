package com.github.retrooper.packetevents.protocol.item.type;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.StaticComponentMap;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntity;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public interface ItemType extends MappedEntity {
   int getMaxAmount();

   int getMaxDurability();

   default boolean isMusicDisc() {
      return this.hasAttribute(ItemTypes.ItemAttribute.MUSIC_DISC);
   }

   ItemType getCraftRemainder();

   @Nullable
   StateType getPlacedType();

   Set<ItemTypes.ItemAttribute> getAttributes();

   default boolean hasAttribute(ItemTypes.ItemAttribute attribute) {
      return this.getAttributes().contains(attribute);
   }

   @Deprecated
   default StaticComponentMap getComponents() {
      return this.getComponents(PacketEvents.getAPI().getServerManager().getVersion().toClientVersion());
   }

   default StaticComponentMap getComponents(ClientVersion clientVersion) {
      return StaticComponentMap.EMPTY;
   }
}
