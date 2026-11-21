package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PacketEntityInteractionManager {
   private static PacketEntityInteractionManager instance;
   private final Map<Integer, PacketEntityInteractionManager.InteractionEntry> entityIdMap = new ConcurrentHashMap<>();

   private PacketEntityInteractionManager() {
   }

   public static PacketEntityInteractionManager getInstance() {
      if (instance == null) {
         instance = new PacketEntityInteractionManager();
      }

      return instance;
   }

   public void register(int entityId, PacketEntityInterface entity) {
      PacketEntityInterface packetEntity = Objects.requireNonNull(entity, "entity");
      this.entityIdMap
         .put(
            entityId,
            new PacketEntityInteractionManager.InteractionEntry(
               packetEntity, context -> PacketEntityInteractionManager.RoutingDecision.ROUTE, packetEntity::handleInteraction
            )
         );
   }

   public PacketEntityInteractionManager.Registration registerHandler(int entityId, PacketEntityInteractionManager.InteractionHandler handler) {
      PacketEntityInteractionManager.InteractionHandler legacyHandler = Objects.requireNonNull(handler, "handler");
      return this.registerContextHandler(
         entityId, context -> PacketEntityInteractionManager.RoutingDecision.ROUTE, (player, context) -> legacyHandler.handle(player, context.isAttack())
      );
   }

   public PacketEntityInteractionManager.Registration registerContextHandler(int entityId, PacketEntityInteractionManager.ContextualInteractionHandler handler) {
      return this.registerContextHandler(entityId, context -> PacketEntityInteractionManager.RoutingDecision.ROUTE, handler);
   }

   public PacketEntityInteractionManager.Registration registerContextHandler(
      int entityId, PacketEntityInteractionManager.InteractionRoutingPolicy routingPolicy, PacketEntityInteractionManager.ContextualInteractionHandler handler
   ) {
      PacketEntityInteractionManager.InteractionEntry entry = new PacketEntityInteractionManager.InteractionEntry(
         null, Objects.requireNonNull(routingPolicy, "routingPolicy"), Objects.requireNonNull(handler, "handler")
      );
      this.entityIdMap.put(entityId, entry);
      return new PacketEntityInteractionManager.Registration(entityId, entry);
   }

   public void unregister(int entityId) {
      this.entityIdMap.remove(entityId);
   }

   public PacketEntityInterface getByEntityId(int entityId) {
      PacketEntityInteractionManager.InteractionEntry entry = this.entityIdMap.get(entityId);
      return entry == null ? null : entry.packetEntity();
   }

   public boolean isRegistered(int entityId) {
      return this.entityIdMap.containsKey(entityId);
   }

   public PacketEntityInteractionManager.Dispatch captureDispatch(int entityId) {
      PacketEntityInteractionManager.InteractionEntry entry = this.entityIdMap.get(entityId);
      return entry == null ? null : new PacketEntityInteractionManager.Dispatch(entityId, entry);
   }

   public boolean handleInteraction(Player player, int entityId, boolean isAttack) {
      return this.handleInteraction(player, entityId, isAttack ? PacketInteractionContext.attack() : PacketInteractionContext.interact(EquipmentSlot.HAND));
   }

   public boolean handleInteraction(Player player, int entityId, PacketInteractionContext context) {
      PacketEntityInteractionManager.InteractionEntry entry = this.entityIdMap.get(entityId);
      if (entry == null) {
         return false;
      } else {
         entry.handler().handle(player, Objects.requireNonNull(context, "context"));
         return true;
      }
   }

   public void shutdown() {
      this.entityIdMap.clear();
   }

   @FunctionalInterface
   public interface ContextualInteractionHandler {
      void handle(Player var1, PacketInteractionContext var2);
   }

   public final class Dispatch {
      private final int entityId;
      private final PacketEntityInteractionManager.InteractionEntry entry;

      private Dispatch(int entityId, PacketEntityInteractionManager.InteractionEntry entry) {
         this.entityId = entityId;
         this.entry = entry;
      }

      public boolean handle(Player player, boolean isAttack) {
         return this.handle(player, isAttack ? PacketInteractionContext.attack() : PacketInteractionContext.interact(EquipmentSlot.HAND));
      }

      public boolean handle(Player player, PacketInteractionContext context) {
         if (PacketEntityInteractionManager.this.entityIdMap.get(this.entityId) != this.entry) {
            return false;
         } else {
            this.entry.handler().handle(player, Objects.requireNonNull(context, "context"));
            return true;
         }
      }

      public PacketEntityInteractionManager.PreparedInteraction prepare(PacketInteractionContext context) {
         PacketInteractionContext interactionContext = Objects.requireNonNull(context, "context");
         if (PacketEntityInteractionManager.this.entityIdMap.get(this.entityId) != this.entry) {
            return PacketEntityInteractionManager.this.new PreparedInteraction(
               this.entityId, this.entry, interactionContext, PacketEntityInteractionManager.RoutingDecision.PASS
            );
         } else {
            PacketEntityInteractionManager.RoutingDecision decision = Objects.requireNonNull(
               this.entry.routingPolicy().decide(interactionContext), "routingPolicy decision"
            );
            return PacketEntityInteractionManager.this.new PreparedInteraction(this.entityId, this.entry, interactionContext, decision);
         }
      }
   }

   private record InteractionEntry(
      PacketEntityInterface packetEntity,
      PacketEntityInteractionManager.InteractionRoutingPolicy routingPolicy,
      PacketEntityInteractionManager.ContextualInteractionHandler handler
   ) {
   }

   @FunctionalInterface
   public interface InteractionHandler {
      void handle(Player var1, boolean var2);
   }

   @FunctionalInterface
   public interface InteractionRoutingPolicy {
      PacketEntityInteractionManager.RoutingDecision decide(PacketInteractionContext var1);
   }

   public final class PreparedInteraction {
      private final int entityId;
      private final PacketEntityInteractionManager.InteractionEntry entry;
      private final PacketInteractionContext context;
      private final PacketEntityInteractionManager.RoutingDecision decision;

      private PreparedInteraction(
         int entityId,
         PacketEntityInteractionManager.InteractionEntry entry,
         PacketInteractionContext context,
         PacketEntityInteractionManager.RoutingDecision decision
      ) {
         this.entityId = entityId;
         this.entry = entry;
         this.context = context;
         this.decision = decision;
      }

      public PacketEntityInteractionManager.RoutingDecision decision() {
         return this.decision;
      }

      public boolean route(Player player) {
         if (this.decision == PacketEntityInteractionManager.RoutingDecision.ROUTE
            && PacketEntityInteractionManager.this.entityIdMap.get(this.entityId) == this.entry) {
            this.entry.handler().handle(player, this.context);
            return true;
         } else {
            return false;
         }
      }
   }

   public final class Registration implements AutoCloseable {
      private final int entityId;
      private final PacketEntityInteractionManager.InteractionEntry entry;

      private Registration(int entityId, PacketEntityInteractionManager.InteractionEntry entry) {
         this.entityId = entityId;
         this.entry = entry;
      }

      @Override
      public void close() {
         PacketEntityInteractionManager.this.entityIdMap.remove(this.entityId, this.entry);
      }
   }

   public static enum RoutingDecision {
      ROUTE,
      CONSUME,
      PASS;
   }
}
