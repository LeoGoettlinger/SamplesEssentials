package com.magmaguy.elitemobs.easyminecraftgoals.customentity;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public interface FakeCustomEntity extends CustomEntityHandle {
   @Override
   CustomEntityDefinition definition();

   PacketEntityInterface packetEntity();

   @Override
   int getEntityId();

   @Override
   Location getLocation();

   @Override
   UUID getUniqueId();

   @Override
   Set<UUID> getViewers();

   void displayTo(Player var1);

   void displayTo(UUID var1);

   void hideFrom(Player var1);

   void hideFrom(UUID var1);

   void remove();

   void teleport(Location var1);

   void setVisible(boolean var1);

   void mountTo(int var1);

   void dismount();

   @Override
   void setScale(float var1);

   @Override
   void setColor(Integer var1);

   @Override
   void setVariant(Integer var1);

   @Override
   void setHitbox(float var1, float var2);

   @Override
   void setProperties(Map<String, ?> var1);

   @Override
   void setProperty(String var1, Object var2);

   public interface Builder {
      FakeCustomEntity.Builder identifier(String var1);

      FakeCustomEntity.Builder carrierEntityType(EntityType var1);

      FakeCustomEntity.Builder dimensions(float var1, float var2);

      FakeCustomEntity.Builder scale(float var1);

      FakeCustomEntity.Builder color(Integer var1);

      FakeCustomEntity.Builder variant(Integer var1);

      FakeCustomEntity.Builder tracked(boolean var1);

      FakeCustomEntity.Builder propertySchema(CustomEntityPropertySchema var1);

      FakeCustomEntity.Builder onShow(CustomEntityViewerHook var1);

      FakeCustomEntity.Builder onHide(CustomEntityViewerHook var1);

      FakeCustomEntity.Builder onRemove(CustomEntityLifecycleHook var1);

      FakeCustomEntity build(Location var1);
   }
}
