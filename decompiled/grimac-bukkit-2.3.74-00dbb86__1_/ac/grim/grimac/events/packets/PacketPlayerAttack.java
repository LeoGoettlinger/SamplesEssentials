package ac.grim.grimac.events.packets;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.impl.badpackets.BadPacketsW;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerAbstract;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketListenerPriority;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketReceiveEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.manager.server.ServerVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.attribute.Attributes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.ItemStack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.GameMode;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientSpectateEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntity;
import ac.grim.grimac.utils.data.packetentity.PacketEntityHorse;

public class PacketPlayerAttack extends PacketListenerAbstract {
   public PacketPlayerAttack() {
      super(PacketListenerPriority.LOW);
   }

   @Override
   public boolean isPreVia() {
      return true;
   }

   @Override
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
         WrapperPlayClientInteractEntity interact = new WrapperPlayClientInteractEntity(event);
         GrimPlayer player = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (player == null) {
            return;
         }

         int entityId = interact.getEntityId();
         if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
            this.onAttack(event, player, entityId);
         } else {
            if (this.isInvalidEntity(event, player, entityId)) {
               return;
            }

            if (interact.getAction() == WrapperPlayClientInteractEntity.InteractAction.INTERACT
               && player.compensatedEntities.getEntity(entityId) instanceof PacketEntityHorse
               && player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_13)) {
               player.packetStateData.horseInteractCausedForcedRotation = true;
            }
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
         WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
         if (packet.getAction() == DiggingAction.STAB) {
            GrimPlayer playerx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
            if (playerx == null) {
               return;
            }

            if (playerx.isResetItemUsageOnAttack()) {
               GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(playerx.platformPlayer);
            }
         }
      }

      if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
         GrimPlayer playerxx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerxx == null) {
            return;
         }

         WrapperPlayClientAttack packet = new WrapperPlayClientAttack(event);
         this.onAttack(event, playerxx, packet.getEntityId());
      }

      if (event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
         GrimPlayer playerxx = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(event.getUser());
         if (playerxx == null) {
            return;
         }

         WrapperPlayClientSpectateEntity packet = new WrapperPlayClientSpectateEntity(event);
         this.onAttack(event, playerxx, packet.getEntityId());
      }
   }

   private void onAttack(PacketReceiveEvent event, GrimPlayer player, int entityId) {
      if (!this.isInvalidEntity(event, player, entityId)) {
         if (player.isResetItemUsageOnAttack()) {
            GrimAPI.INSTANCE.getItemResetHandler().resetItemUsage(player.platformPlayer);
         }

         if (!(player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_DAMAGE) <= 0.0)) {
            ItemStack heldItem = player.inventory.getHeldItem();
            PacketEntity entity = player.compensatedEntities.getEntity(entityId);
            if (entity != null
               && (
                  !entity.isLivingEntity
                     || entity.getType() == EntityTypes.PLAYER
                     || entity.getType() == EntityTypes.PAINTING
                     || entity.getType() == EntityTypes.ENDER_DRAGON && player.getClientVersion().isOlderThan(ClientVersion.V_1_21_2)
               )) {
               int knockbackLevel = player.getClientVersion().isOlderThan(ClientVersion.V_1_21) && heldItem != null
                  ? heldItem.getEnchantmentLevel(EnchantmentTypes.KNOCKBACK)
                  : 0;
               boolean hasNegativeKB = knockbackLevel < 0;
               boolean isLegacyPlayer = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8);
               boolean sufficientCooldownProgress = isLegacyPlayer || player.attackCooldown.getMinimumProgress() > 0.9F;
               if (!isLegacyPlayer) {
                  knockbackLevel = Math.max(knockbackLevel, 0);
               }

               if ((!player.lastSprinting || hasNegativeKB || !sufficientCooldownProgress) && knockbackLevel <= 0) {
                  if (!isLegacyPlayer && player.lastSprinting) {
                     if (player.maxAttackSlow > 0
                        && PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_9)
                        && player.compensatedEntities.self.getAttributeValue(Attributes.ATTACK_SPEED) < 16.0) {
                        return;
                     }

                     player.maxAttackSlow++;
                  }
               } else {
                  player.minAttackSlow++;
                  player.maxAttackSlow++;
                  if (knockbackLevel == 0) {
                     player.maxAttackSlow = player.minAttackSlow = 1;
                  }
               }
            }

            if (player.gamemode != GameMode.SPECTATOR) {
               player.attackCooldown.reset();
            }
         }
      }
   }

   private boolean isInvalidEntity(PacketReceiveEvent event, GrimPlayer player, int entityId) {
      if (!player.compensatedEntities.entityMap.containsKey(entityId)
         && !player.compensatedEntities.serverPositionsMap.containsKey(entityId)
         && (!player.compensatedEntities.entitiesRemovedThisTick.contains(entityId) || player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_14))) {
         BadPacketsW badPacketsW = player.checkManager.getCheck(BadPacketsW.class);
         if (badPacketsW.flag("entityId=" + entityId) && badPacketsW.shouldModifyPackets()) {
            event.setCancelled(true);
            player.onPacketCancel();
         }

         return true;
      } else {
         return false;
      }
   }
}
