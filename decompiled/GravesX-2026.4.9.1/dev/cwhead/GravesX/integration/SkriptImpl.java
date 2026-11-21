package dev.cwhead.GravesX.integration;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.api.grave.GraveCreationAPI;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.api.util.UtilAPI;
import dev.cwhead.GravesX.api.world.LocationAPI;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveAbandoned;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveAutoLoot;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveBlockPlace;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveBreak;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveClose;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveCompassAdd;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveCompassUse;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveCreate;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveExplode;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveItemTake;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveLooted;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveObituaryAdd;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveOpen;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveParticle;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePistonExtend;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePlayerHeadDrop;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePostCreate;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePostTeleport;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePreExplode;
import dev.cwhead.GravesX.event.integration.skript.EvtGravePreTeleport;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveProjectileHit;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveProtectionCreate;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveProtectionExpired;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveTeleport;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveTimeout;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveVirtualOpen;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveWalkOver;
import dev.cwhead.GravesX.event.integration.skript.EvtGraveZombieSpawn;
import dev.cwhead.GravesX.event.integration.skript.conditions.CondGraveHasAvailableSlot;
import dev.cwhead.GravesX.event.integration.skript.conditions.CondGraveLocked;
import dev.cwhead.GravesX.event.integration.skript.conditions.CondIsGrave;
import dev.cwhead.GravesX.event.integration.skript.conditions.CondIsNearGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffAbandonGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffAddItemToGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffAutoLootGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffBreakGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffCreateGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffDropGraveItems;
import dev.cwhead.GravesX.event.integration.skript.effects.EffRemoveGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffRemoveOldestGrave;
import dev.cwhead.GravesX.event.integration.skript.effects.EffSetGraveProtection;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventBlock;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventBlockExp;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventBlockType;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventDirection;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventEntity;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventEntityType;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventGrave;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventInventoryAction;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventInventoryView;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventItemStack;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventLivingEntity;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventLocation;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventMovedBlocks;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventPistonBlock;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventPlayer;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprEventTargetEntity;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraveAmount;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraveFromUUID;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraveUUID;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraveUUIDs;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraveViewerUUID;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprGraves;
import dev.cwhead.GravesX.event.integration.skript.expressions.ExprNextAvailableGraveSlot;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.Builder;

public class SkriptImpl {
   private static SkriptAddon activeSkriptAddon;
   private static GraveCreationAPI activeGraveCreationAPI;
   private static GraveManagementAPI activeGraveManagementAPI;
   private final Graves plugin;
   private SkriptAddon skriptAddon;

   public SkriptImpl(Graves plugin) {
      this.plugin = plugin;
      this.register();
   }

   private void register() {
      try {
         this.skriptAddon = Skript.instance().registerAddon(Graves.class, this.plugin.getName());
         activeSkriptAddon = this.skriptAddon;
         LocationAPI locationAPI = new LocationAPI(this.plugin);
         UtilAPI utilAPI = new UtilAPI(this.plugin, locationAPI);
         activeGraveManagementAPI = new GraveManagementAPI(this.plugin);
         activeGraveCreationAPI = new GraveCreationAPI(this.plugin, locationAPI, utilAPI, activeGraveManagementAPI);
         SkriptTypes.register();
         this.registerStaticSyntaxClasses();
         this.registerExpressions();
         this.plugin.integrationMessage("Skript integration loaded successfully.");
      } catch (Exception var3) {
         this.plugin.getLogger().severe("Failed to load Skript implementation");
         this.plugin.logStackTrace(var3);
      }
   }

   private void registerStaticSyntaxClasses() throws ClassNotFoundException {
      this.initializeClass(EvtGraveAbandoned.class);
      this.initializeClass(EvtGraveAutoLoot.class);
      this.initializeClass(EvtGraveBlockPlace.class);
      this.initializeClass(EvtGraveBreak.class);
      this.initializeClass(EvtGraveClose.class);
      this.initializeClass(EvtGraveCompassAdd.class);
      this.initializeClass(EvtGraveCompassUse.class);
      this.initializeClass(EvtGraveCreate.class);
      this.initializeClass(EvtGraveExplode.class);
      this.initializeClass(EvtGraveItemTake.class);
      this.initializeClass(EvtGraveLooted.class);
      this.initializeClass(EvtGraveObituaryAdd.class);
      this.initializeClass(EvtGraveOpen.class);
      this.initializeClass(EvtGraveParticle.class);
      this.initializeClass(EvtGravePistonExtend.class);
      this.initializeClass(EvtGravePlayerHeadDrop.class);
      this.initializeClass(EvtGravePostCreate.class);
      this.initializeClass(EvtGravePostTeleport.class);
      this.initializeClass(EvtGravePreExplode.class);
      this.initializeClass(EvtGravePreTeleport.class);
      this.initializeClass(EvtGraveProjectileHit.class);
      this.initializeClass(EvtGraveProtectionCreate.class);
      this.initializeClass(EvtGraveProtectionExpired.class);
      this.initializeClass(EvtGraveTeleport.class);
      this.initializeClass(EvtGraveTimeout.class);
      this.initializeClass(EvtGraveVirtualOpen.class);
      this.initializeClass(EvtGraveWalkOver.class);
      this.initializeClass(EvtGraveZombieSpawn.class);
      this.initializeClass(EffCreateGrave.class);
      this.initializeClass(EffRemoveGrave.class);
      this.initializeClass(EffBreakGrave.class);
      this.initializeClass(EffAutoLootGrave.class);
      this.initializeClass(EffAbandonGrave.class);
      this.initializeClass(EffDropGraveItems.class);
      this.initializeClass(EffRemoveOldestGrave.class);
      this.initializeClass(EffSetGraveProtection.class);
      this.initializeClass(EffAddItemToGrave.class);
      this.initializeClass(CondIsNearGrave.class);
      this.initializeClass(CondIsGrave.class);
      this.initializeClass(CondGraveLocked.class);
      this.initializeClass(CondGraveHasAvailableSlot.class);
      this.initializeClass(ExprGraveFromUUID.class);
      this.initializeClass(ExprGraveUUID.class);
      this.initializeClass(ExprGraveUUIDs.class);
      this.initializeClass(ExprGraves.class);
      this.initializeClass(ExprGraveAmount.class);
      this.initializeClass(ExprGraveViewerUUID.class);
      this.initializeClass(ExprNextAvailableGraveSlot.class);
   }

   private void initializeClass(Class<?> clazz) throws ClassNotFoundException {
      Class.forName(clazz.getName(), true, clazz.getClassLoader());
   }

   private void registerExpressions() {
      this.registerExpression(ExprEventGrave.class, Grave.class, "[the] event[-]grave");
      this.registerExpression(ExprEventEntity.class, Entity.class, "[the] event[-]entity");
      this.registerExpression(ExprEventTargetEntity.class, LivingEntity.class, "[the] event[-]target[-]entity");
      this.registerExpression(ExprEventEntityType.class, EntityType.class, "[the] event[-]entity[-]type");
      this.registerExpression(ExprEventLocation.class, Location.class, "[the] event[-]location");
      this.registerExpression(ExprEventInventoryView.class, InventoryView.class, "[the] event[-]inventory[-]view");
      this.registerExpression(ExprEventLivingEntity.class, LivingEntity.class, "[the] event[-]living[-]entity");
      this.registerExpression(ExprEventBlockType.class, BlockData.BlockType.class, "[the] event[-]block[-]type");
      this.registerExpression(ExprEventBlockExp.class, Integer.class, "[the] event[-]blockexp", "[the] event[-]block[-]exp", "[the] event[-]block[-]experience");
      this.registerExpression(ExprEventBlock.class, Block.class, "[the] event[-]block");
      this.registerExpression(ExprEventPistonBlock.class, Block.class, "[the] event[-]piston[-]block");
      this.registerExpression(ExprEventDirection.class, BlockFace.class, "[the] event[-]direction");
      this.registerExpression(ExprEventMovedBlocks.class, List.class, "[the] event[-]moved[-]blocks");
      this.registerExpression(ExprEventPlayer.class, Player.class, "[the] event[-]player");
      this.registerExpression(ExprEventItemStack.class, ItemStack.class, "[the] event[-]itemstack", "[the] event[-]item");
      this.registerExpression(ExprEventInventoryAction.class, InventoryAction.class, "[the] event[-]inventoryaction", "[the] event[-]inventory[-]action");
   }

   private void registerExpression(Class<? extends Expression<?>> expressionClass, Class<?> returnType, String... patterns) {
      this.registerExpressionTyped(expressionClass, returnType, patterns);
   }

   private <R, E extends Expression<R>> void registerExpressionTyped(Class<E> expressionClass, Class<R> returnType, String... patterns) {
      org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression<E, R> info = ((Builder)org.skriptlang.skript.registration.DefaultSyntaxInfos.Expression.builder(
               expressionClass, returnType
            )
            .addPatterns(patterns))
         .build();
      this.skriptAddon.syntaxRegistry().register(SyntaxRegistry.EXPRESSION, info);
   }

   public static SkriptAddon getActiveSkriptAddon() {
      return activeSkriptAddon;
   }

   public static GraveCreationAPI getGraveCreationAPI() {
      return activeGraveCreationAPI;
   }

   public static GraveManagementAPI getGraveManagementAPI() {
      return activeGraveManagementAPI;
   }

   public SkriptAddon getSkriptAddon() {
      return this.skriptAddon;
   }
}
