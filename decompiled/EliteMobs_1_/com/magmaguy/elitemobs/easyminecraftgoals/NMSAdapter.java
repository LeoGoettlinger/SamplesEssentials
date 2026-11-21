package com.magmaguy.elitemobs.easyminecraftgoals;

import com.magmaguy.elitemobs.easyminecraftgoals.ammunition.RangedAmmunition;
import com.magmaguy.elitemobs.easyminecraftgoals.constants.OverridableWanderPriority;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.BukkitCustomEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.BukkitCustomEntityImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.CustomEntityDefinition;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.CustomEntityLifecycleHook;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.CustomEntityPropertySchema;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.CustomEntityViewerHook;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.FakeCustomEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.customentity.FakeCustomEntityImpl;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractPacketBundle;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.DamageIndicatorClamp;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItem;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeItemSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeTextSettings;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInteractionManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketEntityInterface;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketInteractionEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketModelEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.PacketTextEntity;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.LongRangePathfindingHandle;
import com.magmaguy.elitemobs.magmacore.ai.MindActionSink;
import com.magmaguy.elitemobs.magmacore.ai.MindFailureListener;
import com.magmaguy.elitemobs.magmacore.ai.MindHost;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class NMSAdapter {
   public RangedAmmunition grantOrdinaryAmmunition(Plugin plugin, Predicate<Player> eligible) {
      throw new UnsupportedOperationException("Native ammunition is not supported by this adapter");
   }

   public float getConsumptionSeconds(ItemStack item) {
      throw new UnsupportedOperationException("Consumption duration is unavailable on this adapter");
   }

   public ItemStack withConsumptionSeconds(ItemStack item, float seconds) {
      throw new UnsupportedOperationException("Consumption duration is unavailable on this adapter");
   }

   public abstract void damageWithoutCooldown(LivingEntity var1, double var2, Entity var4);

   public int getDamageCooldownTicks(LivingEntity entity) {
      return entity.getNoDamageTicks();
   }

   public void setDamageCooldownTicks(LivingEntity entity, int ticks) {
      entity.setNoDamageTicks(ticks);
   }

   public MindHost createMindHost(MindFailureListener failureListener) {
      if (NMSManager.pluginProvider == null) {
         throw new IllegalStateException("NMSManager must be initialized before creating a Mind host");
      } else {
         return this.createMindHost(new NamespacedKey(NMSManager.pluginProvider, "native_mind"), failureListener);
      }
   }

   public MindHost createMindHost(NamespacedKey hostIdentity, MindFailureListener failureListener) {
      return this.createMindHost(hostIdentity, failureListener, MindActionSink.rejecting());
   }

   public MindHost createMindHost(NamespacedKey hostIdentity, MindFailureListener failureListener, MindActionSink actionSink) {
      Objects.requireNonNull(hostIdentity, "hostIdentity");
      Objects.requireNonNull(failureListener, "failureListener");
      Objects.requireNonNull(actionSink, "actionSink");
      throw new UnsupportedOperationException("Native mind bodies are not supported by this adapter");
   }

   public boolean isMindBody(Entity entity) {
      return false;
   }

   public void shutdownMindHost() {
   }

   public abstract boolean move(LivingEntity var1, double var2, Location var4);

   public Optional<TransientMovementOverride> beginFlee(LivingEntity livingEntity, Location threatLocation, double speedModifier) {
      Objects.requireNonNull(livingEntity, "livingEntity");
      Objects.requireNonNull(threatLocation, "threatLocation");
      return Optional.empty();
   }

   public final Optional<PathfindingHandle> createPathfindingHandle(LivingEntity livingEntity) {
      Objects.requireNonNull(livingEntity, "livingEntity");

      OverridableWanderPriority priority;
      try {
         priority = OverridableWanderPriority.valueOf(livingEntity.getType().name());
      } catch (IllegalArgumentException var4) {
         return Optional.empty();
      }

      return this.createPathfindingHandle(livingEntity, priority.priority).map(nativeHandle -> new LongRangePathfindingHandle(livingEntity, nativeHandle));
   }

   protected Optional<PathfindingHandle> createPathfindingHandle(LivingEntity livingEntity, int priority) {
      return Optional.empty();
   }

   public boolean removeFreeWill(LivingEntity livingEntity) {
      Objects.requireNonNull(livingEntity, "livingEntity");
      return false;
   }

   public boolean isPositionEntityTicking(Location location) {
      Objects.requireNonNull(location, "location");
      return false;
   }

   public abstract boolean forcedMove(LivingEntity var1, double var2, Location var4);

   public abstract void universalMove(LivingEntity var1, double var2, Location var4);

   public abstract AbstractWanderBackToPoint wanderBackToPoint(LivingEntity var1, Location var2, double var3, int var5, OverridableWanderPriority var6);

   public abstract boolean setCustomHitbox(Entity var1, float var2, float var3, boolean var4);

   public boolean setPassengerOffset(Entity entity, double offsetX, double offsetY, double offsetZ) {
      return false;
   }

   public abstract float getBodyRotation(Entity var1);

   public abstract PacketModelEntity createPacketArmorStandEntity(Location var1);

   public abstract PacketModelEntity createPacketDisplayEntity(Location var1);

   public abstract PacketTextEntity createPacketTextArmorStandEntity(Location var1);

   public abstract void doNotMove(LivingEntity var1);

   @Nullable
   public AbstractWanderBackToPoint wanderBackToPoint(
      @NonNull LivingEntity livingEntity, @NonNull Location blockLocation, double maximumDistanceFromPoint, int maxDurationTicks
   ) {
      OverridableWanderPriority overridableWanderPriority;
      try {
         overridableWanderPriority = OverridableWanderPriority.valueOf(livingEntity.getType().name());
      } catch (Exception var8) {
         NMSManager.pluginProvider
            .getLogger()
            .warning(
               "[EasyMinecraftPathfinding] Attempted to assign return point to entity type "
                  + livingEntity.getType().name()
                  + " which is not a currently accepted entity type!"
            );
         return null;
      }

      return this.wanderBackToPoint(livingEntity, blockLocation, maximumDistanceFromPoint, maxDurationTicks, overridableWanderPriority);
   }

   public boolean canReach(LivingEntity livingEntity, Location location) {
      return false;
   }

   public abstract void setBlockInNativeDataPalette(World var1, int var2, int var3, int var4, BlockData var5, boolean var6);

   public abstract AbstractPacketBundle createPacketBundle();

   public PacketEntityInterface createPacketEntity(EntityType entityType, Location location) {
      throw new UnsupportedOperationException("createPacketEntity is not supported by this adapter");
   }

   public FakeCustomEntity createFakeCustomEntity(Location location, CustomEntityDefinition definition) {
      return new FakeCustomEntityImpl(this, location, definition);
   }

   public FakeCustomEntity.Builder fakeCustomEntityBuilder() {
      return new NMSAdapter.FakeCustomEntityBuilderImpl(this);
   }

   public BukkitCustomEntity createBukkitCustomEntity(Entity entity, CustomEntityDefinition definition) {
      return new BukkitCustomEntityImpl(entity, definition);
   }

   public BukkitCustomEntity.Builder bukkitCustomEntityBuilder() {
      return new NMSAdapter.BukkitCustomEntityBuilderImpl(this);
   }

   public PacketInteractionEntity createPacketInteractionEntity(Location location, float width, float height) {
      throw new UnsupportedOperationException("createPacketInteractionEntity is only supported in Minecraft 1.21.11+");
   }

   public void initializePacketInteractionListener(Plugin plugin) {
   }

   public void shutdownPacketInteractionListener() {
      PacketEntityInteractionManager.getInstance().shutdown();
   }

   public void setDamageIndicatorParticleCap(int max) {
      DamageIndicatorClamp.setMaxParticles(max);
   }

   public int getDamageIndicatorParticleCap() {
      return DamageIndicatorClamp.getMaxParticles();
   }

   public boolean hideScoreboardNumbers(Objective objective) {
      return false;
   }

   public abstract FakeText createFakeText(Location var1, FakeTextSettings var2);

   public FakeText.Builder fakeTextBuilder() {
      return new NMSAdapter.FakeTextBuilderImpl(this);
   }

   public boolean supportsFakeItems() {
      return false;
   }

   public FakeItem createFakeItem(Location location, FakeItemSettings settings) {
      throw new UnsupportedOperationException("createFakeItem is only supported in Minecraft 1.21.4+");
   }

   public FakeItem.Builder fakeItemBuilder() {
      return new NMSAdapter.FakeItemBuilderImpl(this);
   }

   private static class BukkitCustomEntityBuilderImpl implements BukkitCustomEntity.Builder {
      private final NMSAdapter adapter;
      private final CustomEntityDefinition.Builder definitionBuilder = CustomEntityDefinition.builder("magmacore:custom_entity");
      private boolean carrierEntityTypeSet;

      BukkitCustomEntityBuilderImpl(NMSAdapter adapter) {
         this.adapter = adapter;
      }

      @Override
      public BukkitCustomEntity.Builder identifier(String identifier) {
         this.definitionBuilder.identifier(identifier);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder carrierEntityType(EntityType entityType) {
         this.definitionBuilder.carrierEntityType(entityType);
         this.carrierEntityTypeSet = true;
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder dimensions(float width, float height) {
         this.definitionBuilder.dimensions(width, height);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder scale(float scale) {
         this.definitionBuilder.scale(scale);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder color(Integer color) {
         this.definitionBuilder.color(color);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder variant(Integer variant) {
         this.definitionBuilder.variant(variant);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder tracked(boolean tracked) {
         this.definitionBuilder.tracked(tracked);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder propertySchema(CustomEntityPropertySchema schema) {
         this.definitionBuilder.propertySchema(schema);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder onShow(CustomEntityViewerHook showHook) {
         this.definitionBuilder.onShow(showHook);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder onHide(CustomEntityViewerHook hideHook) {
         this.definitionBuilder.onHide(hideHook);
         return this;
      }

      @Override
      public BukkitCustomEntity.Builder onRemove(CustomEntityLifecycleHook removeHook) {
         this.definitionBuilder.onRemove(removeHook);
         return this;
      }

      @Override
      public BukkitCustomEntity build(Entity entity) {
         if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
         } else {
            if (!this.carrierEntityTypeSet) {
               this.definitionBuilder.carrierEntityType(entity.getType());
            }

            return this.adapter.createBukkitCustomEntity(entity, this.definitionBuilder.build());
         }
      }
   }

   private static class FakeCustomEntityBuilderImpl implements FakeCustomEntity.Builder {
      private final NMSAdapter adapter;
      private final CustomEntityDefinition.Builder definitionBuilder = CustomEntityDefinition.builder("magmacore:custom_entity");

      FakeCustomEntityBuilderImpl(NMSAdapter adapter) {
         this.adapter = adapter;
      }

      @Override
      public FakeCustomEntity.Builder identifier(String identifier) {
         this.definitionBuilder.identifier(identifier);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder carrierEntityType(EntityType entityType) {
         this.definitionBuilder.carrierEntityType(entityType);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder dimensions(float width, float height) {
         this.definitionBuilder.dimensions(width, height);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder scale(float scale) {
         this.definitionBuilder.scale(scale);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder color(Integer color) {
         this.definitionBuilder.color(color);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder variant(Integer variant) {
         this.definitionBuilder.variant(variant);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder tracked(boolean tracked) {
         this.definitionBuilder.tracked(tracked);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder propertySchema(CustomEntityPropertySchema schema) {
         this.definitionBuilder.propertySchema(schema);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder onShow(CustomEntityViewerHook showHook) {
         this.definitionBuilder.onShow(showHook);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder onHide(CustomEntityViewerHook hideHook) {
         this.definitionBuilder.onHide(hideHook);
         return this;
      }

      @Override
      public FakeCustomEntity.Builder onRemove(CustomEntityLifecycleHook removeHook) {
         this.definitionBuilder.onRemove(removeHook);
         return this;
      }

      @Override
      public FakeCustomEntity build(Location location) {
         return this.adapter.createFakeCustomEntity(location, this.definitionBuilder.build());
      }
   }

   private static class FakeItemBuilderImpl implements FakeItem.Builder {
      private final NMSAdapter adapter;
      private final FakeItemSettings settings = new FakeItemSettings();

      FakeItemBuilderImpl(NMSAdapter adapter) {
         this.adapter = adapter;
      }

      @Override
      public FakeItem.Builder itemStack(ItemStack itemStack) {
         this.settings.setItemStack(itemStack);
         return this;
      }

      @Override
      public FakeItem.Builder billboard(Billboard billboard) {
         this.settings.setBillboard(billboard);
         return this;
      }

      @Override
      public FakeItem.Builder scale(float scale) {
         this.settings.setScale(scale);
         return this;
      }

      @Override
      public FakeItem.Builder viewRange(float range) {
         this.settings.setViewRange(range);
         return this;
      }

      @Override
      public FakeItem.Builder glowing(boolean glowing) {
         this.settings.setGlowing(glowing);
         return this;
      }

      @Override
      public FakeItem.Builder customName(String name) {
         this.settings.setCustomName(name);
         return this;
      }

      @Override
      public FakeItem.Builder customNameVisible(boolean visible) {
         this.settings.setCustomNameVisible(visible);
         return this;
      }

      @Override
      public FakeItem build(Location location) {
         return this.adapter.createFakeItem(location, this.settings);
      }
   }

   private static class FakeTextBuilderImpl implements FakeText.Builder {
      private final NMSAdapter adapter;
      private final FakeTextSettings settings = new FakeTextSettings();

      FakeTextBuilderImpl(NMSAdapter adapter) {
         this.adapter = adapter;
      }

      @Override
      public FakeText.Builder text(String text) {
         this.settings.setText(text);
         return this;
      }

      @Override
      public FakeText.Builder backgroundColor(Color color) {
         this.settings.setBackgroundColor(color);
         return this;
      }

      @Override
      public FakeText.Builder backgroundColor(int argb) {
         this.settings.setBackgroundArgb(argb);
         return this;
      }

      @Override
      public FakeText.Builder textOpacity(byte opacity) {
         this.settings.setTextOpacity(opacity);
         return this;
      }

      @Override
      public FakeText.Builder billboard(Billboard billboard) {
         this.settings.setBillboard(billboard);
         return this;
      }

      @Override
      public FakeText.Builder alignment(FakeText.TextAlignment alignment) {
         this.settings.setAlignment(alignment);
         return this;
      }

      @Override
      public FakeText.Builder shadow(boolean shadow) {
         this.settings.setShadow(shadow);
         return this;
      }

      @Override
      public FakeText.Builder seeThrough(boolean seeThrough) {
         this.settings.setSeeThrough(seeThrough);
         return this;
      }

      @Override
      public FakeText.Builder lineWidth(int width) {
         this.settings.setLineWidth(width);
         return this;
      }

      @Override
      public FakeText.Builder viewRange(float range) {
         this.settings.setViewRange(range);
         return this;
      }

      @Override
      public FakeText.Builder teleportDuration(int ticks) {
         this.settings.setTeleportDuration(ticks);
         return this;
      }

      @Override
      public FakeText.Builder scale(float scale) {
         this.settings.setScale(scale);
         return this;
      }

      @Override
      public FakeText.Builder translation(float x, float y, float z) {
         this.settings.setTranslation(x, y, z);
         return this;
      }

      @Override
      public FakeText.Builder viewerFilter(Predicate<Player> viewerFilter) {
         this.settings.setViewerFilter(viewerFilter);
         return this;
      }

      @Override
      public FakeText build(Location location) {
         return this.adapter.createFakeText(location, this.settings);
      }
   }
}
