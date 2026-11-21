package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.packets;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInteractionManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionCallback;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionContext;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PacketInteractionEntity
   extends AbstractPacketEntity<Interaction>
   implements com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionEntity {
   private PacketInteractionCallback rightClickCallback;
   private PacketInteractionCallback leftClickCallback;
   private float width = 1.0F;
   private float height = 1.0F;

   public PacketInteractionEntity(Location location) {
      super(location);
      PacketEntityInteractionManager.getInstance().register(this.getEntityId(), this);
      this.addRemoveCallback(() -> PacketEntityInteractionManager.getInstance().unregister(this.getEntityId()));
   }

   public PacketInteractionEntity(Location location, float width, float height) {
      this(location);
      this.setSize(width, height);
   }

   protected Interaction createEntity(Location location) {
      Interaction interaction = new Interaction(EntityTypes.ap, this.getNMSLevel(location));
      interaction.a_(location.getX(), location.getY(), location.getZ());
      return interaction;
   }

   @Override
   public void setSize(float width, float height) {
      this.width = width;
      this.height = height;
      this.entity.a(width);
      this.entity.b(height);
      this.syncMetadata();
   }

   @Override
   public float getWidth() {
      return this.width;
   }

   @Override
   public float getHeight() {
      return this.height;
   }

   @Override
   public int getEntityId() {
      return this.entity.ao();
   }

   @Override
   public void setRightClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
      this.rightClickCallback = callback == null ? null : (player, entity, context) -> callback.accept(player, entity);
   }

   @Override
   public void setLeftClickCallback(BiConsumer<Player, PacketEntityInterface> callback) {
      this.leftClickCallback = callback == null ? null : (player, entity, context) -> callback.accept(player, entity);
   }

   @Override
   public void setContextualRightClickCallback(PacketInteractionCallback callback) {
      this.rightClickCallback = callback;
   }

   @Override
   public void setContextualLeftClickCallback(PacketInteractionCallback callback) {
      this.leftClickCallback = callback;
   }

   @Override
   public void handleInteraction(Player player, boolean isAttack) {
      this.handleInteraction(player, isAttack ? PacketInteractionContext.attack() : PacketInteractionContext.interact(EquipmentSlot.HAND));
   }

   @Override
   public void handleInteraction(Player player, PacketInteractionContext context) {
      PacketInteractionCallback callback = context.isAttack() ? this.leftClickCallback : this.rightClickCallback;
      if (callback != null) {
         callback.accept(player, this, context);
      }
   }
}
