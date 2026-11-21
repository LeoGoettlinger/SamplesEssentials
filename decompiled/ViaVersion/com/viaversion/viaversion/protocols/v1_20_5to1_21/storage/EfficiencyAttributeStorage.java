package com.viaversion.viaversion.protocols.v1_20_5to1_21.storage;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.Protocol1_20_5To1_21;
import com.viaversion.viaversion.protocols.v1_20_5to1_21.packet.ClientboundPackets1_21;
import java.util.List;
import java.util.stream.Stream;

public final class EfficiencyAttributeStorage {
   private static final EfficiencyAttributeStorage.EnchantAttributeModifier EFFICIENCY = new EfficiencyAttributeStorage.EnchantAttributeModifier(
      "minecraft:enchantment.efficiency/mainhand", 19, 0.0, level -> level * level + 1
   );
   private static final EfficiencyAttributeStorage.EnchantAttributeModifier SOUL_SPEED = new EfficiencyAttributeStorage.EnchantAttributeModifier(
      "minecraft:enchantment.soul_speed", 21, 0.1, level -> 0.04 + (level - 1) * 0.01
   );
   private static final EfficiencyAttributeStorage.EnchantAttributeModifier SWIFT_SNEAK = new EfficiencyAttributeStorage.EnchantAttributeModifier(
      "minecraft:enchantment.swift_sneak", 25, 0.3, level -> level * 0.15
   );
   private static final EfficiencyAttributeStorage.EnchantAttributeModifier AQUA_AFFINITY = new EfficiencyAttributeStorage.EnchantAttributeModifier(
      "minecraft:enchantment.aqua_affinity", 28, 0.2, level -> level * 4, (byte)2
   );
   private static final EfficiencyAttributeStorage.EnchantAttributeModifier DEPTH_STRIDER = new EfficiencyAttributeStorage.EnchantAttributeModifier(
      "minecraft:enchantment.depth_strider", 30, 0.0, level -> level / 3.0
   );
   private static final EfficiencyAttributeStorage.ActiveEnchants DEFAULT = new EfficiencyAttributeStorage.ActiveEnchants(
      -1,
      new EfficiencyAttributeStorage.ActiveEnchant(EFFICIENCY, 0, 0),
      new EfficiencyAttributeStorage.ActiveEnchant(SOUL_SPEED, 0, 0),
      new EfficiencyAttributeStorage.ActiveEnchant(SWIFT_SNEAK, 0, 0),
      new EfficiencyAttributeStorage.ActiveEnchant(AQUA_AFFINITY, 0, 0),
      new EfficiencyAttributeStorage.ActiveEnchant(DEPTH_STRIDER, 0, 0)
   );
   private final Object lock = new Object();
   private volatile boolean attributesSent = true;
   private volatile boolean loginSent;
   private EfficiencyAttributeStorage.ActiveEnchants activeEnchants = DEFAULT;

   public void setEnchants(int entityId, UserConnection connection, EfficiencyAttributeStorage.ActiveEnchants enchants) {
      if (this.activeEnchants != enchants) {
         synchronized (this.lock) {
            this.activeEnchants = entityId == -1 ? enchants : enchants.withEntityId(entityId);
            this.attributesSent = false;
         }

         this.sendAttributesPacket(connection, false);
      }
   }

   public EfficiencyAttributeStorage.ActiveEnchants activeEnchants() {
      return this.activeEnchants;
   }

   public void onLoginSent(int entityId, UserConnection connection) {
      synchronized (this.lock) {
         this.activeEnchants = this.activeEnchants.withEntityId(entityId);
      }

      this.loginSent = true;
      this.sendAttributesPacket(connection, false);
   }

   public void onRespawn(UserConnection connection) {
      this.sendAttributesPacket(connection, true);
   }

   private void sendAttributesPacket(UserConnection connection, boolean forceSendAll) {
      EfficiencyAttributeStorage.ActiveEnchants enchants;
      synchronized (this.lock) {
         if (!forceSendAll && (!this.loginSent || this.attributesSent)) {
            return;
         }

         enchants = this.activeEnchants;
         this.attributesSent = true;
      }

      PacketWrapper attributesPacket = PacketWrapper.create(ClientboundPackets1_21.UPDATE_ATTRIBUTES, connection);
      attributesPacket.write(Types.VAR_INT, enchants.entityId());
      List<EfficiencyAttributeStorage.ActiveEnchant> list = Stream.of(
            enchants.efficiency(), enchants.soulSpeed(), enchants.swiftSneak(), enchants.aquaAffinity(), enchants.depthStrider()
         )
         .filter(enchantx -> forceSendAll || enchantx.previousLevel != enchantx.level)
         .toList();
      attributesPacket.write(Types.VAR_INT, list.size());

      for (EfficiencyAttributeStorage.ActiveEnchant enchant : list) {
         EfficiencyAttributeStorage.EnchantAttributeModifier modifier = enchant.modifier;
         attributesPacket.write(Types.VAR_INT, modifier.attributeId);
         attributesPacket.write(Types.DOUBLE, modifier.baseValue);
         if (enchant.level > 0) {
            attributesPacket.write(Types.VAR_INT, 1);
            attributesPacket.write(Types.STRING, modifier.key);
            attributesPacket.write(Types.DOUBLE, enchant.modifier.modifierFunction.get(enchant.level));
            attributesPacket.write(Types.BYTE, modifier.operation);
         } else {
            attributesPacket.write(Types.VAR_INT, 0);
         }
      }

      attributesPacket.scheduleSend(Protocol1_20_5To1_21.class);
   }

   public record ActiveEnchant(EfficiencyAttributeStorage.EnchantAttributeModifier modifier, int previousLevel, int level) {
      public ActiveEnchant(EfficiencyAttributeStorage.ActiveEnchant from, int level) {
         this(from.modifier, from.level, level);
      }
   }

   public record ActiveEnchants(
      int entityId,
      EfficiencyAttributeStorage.ActiveEnchant efficiency,
      EfficiencyAttributeStorage.ActiveEnchant soulSpeed,
      EfficiencyAttributeStorage.ActiveEnchant swiftSneak,
      EfficiencyAttributeStorage.ActiveEnchant aquaAffinity,
      EfficiencyAttributeStorage.ActiveEnchant depthStrider
   ) {
      private EfficiencyAttributeStorage.ActiveEnchants withEntityId(int entityId) {
         return this.entityId == entityId
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(entityId, this.efficiency, this.soulSpeed, this.swiftSneak, this.aquaAffinity, this.depthStrider);
      }

      public EfficiencyAttributeStorage.ActiveEnchants efficiency(int level) {
         return this.efficiency.level == level
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(
               this.entityId,
               new EfficiencyAttributeStorage.ActiveEnchant(this.efficiency, level),
               this.soulSpeed,
               this.swiftSneak,
               this.aquaAffinity,
               this.depthStrider
            );
      }

      public EfficiencyAttributeStorage.ActiveEnchants soulSpeed(int level) {
         return this.soulSpeed.level == level
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(
               this.entityId,
               this.efficiency,
               new EfficiencyAttributeStorage.ActiveEnchant(this.soulSpeed, level),
               this.swiftSneak,
               this.aquaAffinity,
               this.depthStrider
            );
      }

      public EfficiencyAttributeStorage.ActiveEnchants swiftSneak(int level) {
         return this.swiftSneak.level == level
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(
               this.entityId,
               this.efficiency,
               this.soulSpeed,
               new EfficiencyAttributeStorage.ActiveEnchant(this.swiftSneak, level),
               this.aquaAffinity,
               this.depthStrider
            );
      }

      public EfficiencyAttributeStorage.ActiveEnchants aquaAffinity(int level) {
         return this.aquaAffinity.level == level
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(
               this.entityId,
               this.efficiency,
               this.soulSpeed,
               this.swiftSneak,
               new EfficiencyAttributeStorage.ActiveEnchant(this.aquaAffinity, level),
               this.depthStrider
            );
      }

      public EfficiencyAttributeStorage.ActiveEnchants depthStrider(int level) {
         return this.depthStrider.level == level
            ? this
            : new EfficiencyAttributeStorage.ActiveEnchants(
               this.entityId,
               this.efficiency,
               this.soulSpeed,
               this.swiftSneak,
               this.aquaAffinity,
               new EfficiencyAttributeStorage.ActiveEnchant(this.depthStrider, level)
            );
      }
   }

   public record EnchantAttributeModifier(
      String key, int attributeId, double baseValue, EfficiencyAttributeStorage.LevelToModifier modifierFunction, byte operation
   ) {
      private EnchantAttributeModifier(String key, int attributeId, double baseValue, EfficiencyAttributeStorage.LevelToModifier modifierFunction) {
         this(key, attributeId, baseValue, modifierFunction, (byte)0);
      }
   }

   @FunctionalInterface
   private interface LevelToModifier {
      double get(int var1);
   }
}
