package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.InventoryUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.events.bukkit.SkriptStartEvent;
import ch.njol.skript.events.bukkit.SkriptStopEvent;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.DelayedChangeBlock;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.skript.util.SkriptColor;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.slot.InventorySlot;
import ch.njol.skript.util.slot.Slot;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent;
import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import io.papermc.paper.event.player.AbstractRespawnEvent;
import io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.block.BellResonateEvent;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.block.VaultDisplayItemEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.DragType;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.bukkit.event.player.PlayerQuitEvent.QuitReason;
import org.bukkit.event.player.PlayerRespawnEvent.RespawnReason;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.skriptlang.skript.bukkit.item.book.elements.expressions.ExprBookPages;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.lang.converter.Converter;

public final class BukkitEventValues {
   private static final ItemStack AIR_IS = new ItemStack(Material.AIR);

   public static void register(EventValueRegistry registry) {
      registry.register(EventValue.simple(WorldEvent.class, World.class, WorldEvent::getWorld));
      registry.register(EventValue.simple(StructureGrowEvent.class, Block.class, event -> event.getLocation().getBlock()));
      registry.register(
         EventValue.simple(StructureGrowEvent.class, Block[].class, event -> event.getBlocks().stream().map(BlockState::getBlock).toArray(Block[]::new))
      );
      registry.register(EventValue.builder(StructureGrowEvent.class, Block.class).getter(event -> {
         for (BlockState bs : event.getBlocks()) {
            if (bs.getLocation().equals(event.getLocation())) {
               return new BlockStateBlock(bs);
            }
         }

         return event.getLocation().getBlock();
      }).time(EventValue.Time.FUTURE).build());
      registry.register(
         EventValue.builder(StructureGrowEvent.class, Block[].class)
            .getter(event -> event.getBlocks().stream().map(BlockStateBlock::new).toArray(Block[]::new))
            .time(EventValue.Time.FUTURE)
            .build()
      );
      registry.register(EventValue.simple(WeatherEvent.class, World.class, WeatherEvent::getWorld));
      registry.register(EventValue.simple(ChunkEvent.class, Chunk.class, ChunkEvent::getChunk));
      registry.register(EventValue.simple(BlockEvent.class, Block.class, BlockEvent::getBlock));
      registry.register(EventValue.simple(BlockEvent.class, World.class, event -> event.getBlock().getWorld()));
      registry.register(EventValue.simple(BlockEvent.class, Location.class, event -> BlockUtils.getLocation(event.getBlock())));
      registry.register(EventValue.simple(BlockPlaceEvent.class, Player.class, BlockPlaceEvent::getPlayer));
      registry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class).getter(BlockPlaceEvent::getItemInHand).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(BlockPlaceEvent.class, ItemStack.class, BlockPlaceEvent::getItemInHand));
      registry.register(EventValue.builder(BlockPlaceEvent.class, ItemStack.class).getter(event -> {
         ItemStack item = event.getItemInHand().clone();
         if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() - 1);
         }

         return item;
      }).time(EventValue.Time.FUTURE).build());
      registry.register(
         EventValue.builder(BlockPlaceEvent.class, Block.class)
            .getter(event -> new BlockStateBlock(event.getBlockReplacedState()))
            .time(EventValue.Time.PAST)
            .build()
      );
      registry.register(EventValue.simple(BlockPlaceEvent.class, Direction.class, event -> {
         BlockFace bf = event.getBlockPlaced().getFace(event.getBlockAgainst());
         return bf != null ? new Direction(new double[]{bf.getModX(), bf.getModY(), bf.getModZ()}) : Direction.ZERO;
      }));
      registry.register(EventValue.builder(BlockFadeEvent.class, Block.class).getter(BlockEvent::getBlock).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(BlockFadeEvent.class, Block.class, event -> new DelayedChangeBlock(event.getBlock(), event.getNewState())));
      registry.register(
         EventValue.builder(BlockFadeEvent.class, Block.class).getter(event -> new BlockStateBlock(event.getNewState())).time(EventValue.Time.FUTURE).build()
      );
      registry.register(EventValue.simple(BlockGrowEvent.class, Block.class, event -> new BlockStateBlock(event.getNewState())));
      registry.register(EventValue.builder(BlockGrowEvent.class, Block.class).getter(BlockEvent::getBlock).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(BlockDamageEvent.class, Player.class, BlockDamageEvent::getPlayer));
      registry.register(EventValue.simple(BlockBreakEvent.class, Player.class, BlockBreakEvent::getPlayer));
      registry.register(EventValue.builder(BlockBreakEvent.class, Block.class).getter(BlockEvent::getBlock).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(BlockBreakEvent.class, Block.class, event -> new DelayedChangeBlock(event.getBlock())));
      registry.register(EventValue.builder(BlockFromToEvent.class, Block.class).getter(BlockFromToEvent::getToBlock).time(EventValue.Time.FUTURE).build());
      registry.register(EventValue.simple(BlockIgniteEvent.class, Player.class, BlockIgniteEvent::getPlayer));
      registry.register(EventValue.simple(BlockIgniteEvent.class, Block.class, BlockEvent::getBlock));
      registry.register(EventValue.simple(BlockDispenseEvent.class, ItemStack.class, BlockDispenseEvent::getItem));
      registry.register(EventValue.builder(BlockCanBuildEvent.class, Block.class).getter(BlockEvent::getBlock).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(BlockCanBuildEvent.class, Block.class, event -> {
         BlockState state = event.getBlock().getState();
         state.setType(event.getMaterial());
         return new BlockStateBlock(state, true);
      }));
      if (Skript.methodExists(BlockCanBuildEvent.class, "getPlayer")) {
         registry.register(EventValue.simple(BlockCanBuildEvent.class, Player.class, BlockCanBuildEvent::getPlayer));
      }

      registry.register(EventValue.simple(SignChangeEvent.class, Player.class, SignChangeEvent::getPlayer));
      registry.register(EventValue.simple(SignChangeEvent.class, Component[].class, event -> event.lines().toArray(new Component[0])));
      registry.register(
         EventValue.builder(EntityEvent.class, Entity.class)
            .getter(EntityEvent::getEntity)
            .excludes(EntityDamageEvent.class, EntityDeathEvent.class)
            .excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
            .build()
      );
      registry.register(
         EventValue.builder(EntityEvent.class, CommandSender.class)
            .getter(EntityEvent::getEntity)
            .excludes(EntityDamageEvent.class, EntityDeathEvent.class)
            .excludedErrorMessage("Use 'attacker' and/or 'victim' in damage/death events")
            .build()
      );
      registry.register(EventValue.simple(EntityEvent.class, World.class, event -> event.getEntity().getWorld()));
      registry.register(EventValue.simple(EntityEvent.class, Location.class, event -> event.getEntity().getLocation()));
      registry.register(
         EventValue.builder(EntityEvent.class, EntityData.class)
            .getter(event -> EntityData.fromEntity(event.getEntity()))
            .excludes(EntityDamageEvent.class, EntityDeathEvent.class)
            .excludedErrorMessage("Use 'type of attacker/victim' in damage/death events.")
            .build()
      );
      registry.register(EventValue.simple(EntityDamageEvent.class, DamageCause.class, EntityDamageEvent::getCause));
      registry.register(
         EventValue.simple(EntityDamageByEntityEvent.class, Projectile.class, event -> event.getDamager() instanceof Projectile projectile ? projectile : null)
      );
      registry.register(EventValue.simple(EntityDeathEvent.class, ItemStack[].class, event -> event.getDrops().toArray(new ItemStack[0])));
      registry.register(
         EventValue.simple(
            EntityDeathEvent.class,
            Projectile.class,
            event -> event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent entityEvent
                  && entityEvent.getDamager() instanceof Projectile projectile
               ? projectile
               : null
         )
      );
      registry.register(EventValue.simple(EntityDeathEvent.class, DamageCause.class, event -> {
         EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
         return damageEvent == null ? null : damageEvent.getCause();
      }));
      if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlock")) {
         registry.register(EventValue.simple(ProjectileHitEvent.class, Block.class, ProjectileHitEvent::getHitBlock));
      }

      registry.register(EventValue.builder(ProjectileHitEvent.class, Entity.class).getter(event -> {
         assert false;

         return event.getEntity();
      }).excludes(ProjectileHitEvent.class).excludedErrorMessage("Use 'projectile' and/or 'shooter' in projectile hit events").build());
      registry.register(EventValue.simple(ProjectileHitEvent.class, Projectile.class, ProjectileHitEvent::getEntity));
      if (Skript.methodExists(ProjectileHitEvent.class, "getHitBlockFace")) {
         registry.register(EventValue.simple(ProjectileHitEvent.class, Direction.class, event -> {
            BlockFace theHitFace = event.getHitBlockFace();
            return theHitFace == null ? null : new Direction(theHitFace, 1.0);
         }));
      }

      registry.register(EventValue.builder(ProjectileLaunchEvent.class, Entity.class).getter(event -> {
         assert false;

         return event.getEntity();
      }).excludes(ProjectileLaunchEvent.class).excludedErrorMessage("Use 'projectile' and/or 'shooter' in shoot events").build());
      if (Skript.classExists("com.destroystokyo.paper.event.entity.ProjectileCollideEvent")) {
         registry.register(EventValue.simple(ProjectileCollideEvent.class, Projectile.class, ProjectileCollideEvent::getEntity));
         registry.register(EventValue.simple(ProjectileCollideEvent.class, Entity.class, ProjectileCollideEvent::getCollidedWith));
      }

      registry.register(EventValue.simple(ProjectileLaunchEvent.class, Projectile.class, ProjectileLaunchEvent::getEntity));
      registry.register(EventValue.simple(EntityTameEvent.class, Entity.class, EntityTameEvent::getEntity));
      registry.register(
         EventValue.builder(EntityChangeBlockEvent.class, Block.class).getter(EntityChangeBlockEvent::getBlock).time(EventValue.Time.PAST).build()
      );
      registry.register(EventValue.simple(EntityChangeBlockEvent.class, Block.class, EntityChangeBlockEvent::getBlock));
      registry.register(EventValue.simple(EntityChangeBlockEvent.class, BlockData.class, EntityChangeBlockEvent::getBlockData));
      registry.register(
         EventValue.builder(EntityChangeBlockEvent.class, BlockData.class).getter(EntityChangeBlockEvent::getBlockData).time(EventValue.Time.FUTURE).build()
      );
      registry.register(
         EventValue.simple(AreaEffectCloudApplyEvent.class, LivingEntity[].class, event -> event.getAffectedEntities().toArray(new LivingEntity[0]))
      );
      registry.register(
         EventValue.simple(AreaEffectCloudApplyEvent.class, PotionEffectType.class, new Converter<AreaEffectCloudApplyEvent, PotionEffectType>() {
            private final boolean HAS_POTION_TYPE_METHOD = Skript.methodExists(AreaEffectCloud.class, "getBasePotionType");

            public PotionEffectType convert(AreaEffectCloudApplyEvent event) {
               if (this.HAS_POTION_TYPE_METHOD) {
                  PotionType base = event.getEntity().getBasePotionType();
                  return base != null ? base.getEffectType() : null;
               } else {
                  return event.getEntity().getBasePotionData().getType().getEffectType();
               }
            }
         })
      );
      registry.register(EventValue.simple(ItemSpawnEvent.class, ItemStack.class, event -> event.getEntity().getItemStack()));
      registry.register(EventValue.simple(LightningStrikeEvent.class, Entity.class, LightningStrikeEvent::getLightning));
      if (Skript.classExists("com.destroystokyo.paper.event.entity.EndermanAttackPlayerEvent")) {
         registry.register(EventValue.simple(EndermanAttackPlayerEvent.class, Player.class, EndermanAttackPlayerEvent::getPlayer));
      }

      registry.register(EventValue.simple(PlayerEvent.class, Player.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(PlayerEvent.class, World.class, event -> event.getPlayer().getWorld()));
      registry.register(EventValue.simple(PlayerBedEnterEvent.class, Block.class, PlayerBedEnterEvent::getBed));
      registry.register(EventValue.simple(PlayerBedLeaveEvent.class, Block.class, PlayerBedLeaveEvent::getBed));
      registry.register(EventValue.simple(PlayerBucketFillEvent.class, Block.class, PlayerBucketEvent::getBlockClicked));
      registry.register(EventValue.builder(PlayerBucketFillEvent.class, Block.class).getter(event -> {
         BlockState state = event.getBlockClicked().getState();
         state.setType(Material.AIR);
         return new BlockStateBlock(state, true);
      }).time(EventValue.Time.FUTURE).build());
      registry.register(
         EventValue.builder(PlayerBucketEmptyEvent.class, Block.class)
            .getter(event -> event.getBlockClicked().getRelative(event.getBlockFace()))
            .time(EventValue.Time.PAST)
            .build()
      );
      registry.register(EventValue.simple(PlayerBucketEmptyEvent.class, Block.class, event -> {
         BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
         state.setType(event.getBucket() == Material.WATER_BUCKET ? Material.WATER : Material.LAVA);
         return new BlockStateBlock(state, true);
      }));
      registry.register(EventValue.simple(PlayerDropItemEvent.class, Player.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(PlayerDropItemEvent.class, Item.class, PlayerDropItemEvent::getItemDrop));
      registry.register(EventValue.simple(PlayerDropItemEvent.class, ItemStack.class, event -> event.getItemDrop().getItemStack()));
      registry.register(EventValue.simple(PlayerDropItemEvent.class, Entity.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(EntityDropItemEvent.class, Item.class, EntityDropItemEvent::getItemDrop));
      registry.register(EventValue.simple(EntityDropItemEvent.class, ItemStack.class, event -> event.getItemDrop().getItemStack()));
      registry.register(EventValue.simple(PlayerPickupItemEvent.class, Player.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(PlayerPickupItemEvent.class, Item.class, PlayerPickupItemEvent::getItem));
      registry.register(EventValue.simple(PlayerPickupItemEvent.class, ItemStack.class, event -> event.getItem().getItemStack()));
      registry.register(EventValue.simple(PlayerPickupItemEvent.class, Entity.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(EntityPickupItemEvent.class, Entity.class, EntityPickupItemEvent::getEntity));
      registry.register(EventValue.simple(EntityPickupItemEvent.class, Item.class, EntityPickupItemEvent::getItem));
      registry.register(EventValue.simple(EntityPickupItemEvent.class, ItemType.class, event -> new ItemType(event.getItem().getItemStack())));
      registry.register(
         EventValue.builder(PlayerItemConsumeEvent.class, ItemStack.class)
            .getter(PlayerItemConsumeEvent::getItem)
            .registerChanger(Changer.ChangeMode.SET, PlayerItemConsumeEvent::setItem)
            .build()
      );
      registry.register(EventValue.simple(PlayerItemBreakEvent.class, ItemStack.class, PlayerItemBreakEvent::getBrokenItem));
      registry.register(EventValue.simple(PlayerInteractEntityEvent.class, Entity.class, PlayerInteractEntityEvent::getRightClicked));
      registry.register(EventValue.simple(PlayerInteractEntityEvent.class, ItemStack.class, event -> {
         EquipmentSlot hand = event.getHand();
         if (hand == EquipmentSlot.HAND) {
            return event.getPlayer().getInventory().getItemInMainHand();
         } else {
            return hand == EquipmentSlot.OFF_HAND ? event.getPlayer().getInventory().getItemInOffHand() : null;
         }
      }));
      registry.register(EventValue.simple(PlayerInteractEvent.class, ItemStack.class, PlayerInteractEvent::getItem));
      registry.register(EventValue.simple(PlayerInteractEvent.class, Block.class, PlayerInteractEvent::getClickedBlock));
      registry.register(
         EventValue.simple(
            PlayerInteractEvent.class,
            Direction.class,
            event -> new Direction(new double[]{event.getBlockFace().getModX(), event.getBlockFace().getModY(), event.getBlockFace().getModZ()})
         )
      );
      registry.register(EventValue.simple(PlayerShearEntityEvent.class, Entity.class, PlayerShearEntityEvent::getEntity));
      registry.register(EventValue.simple(PlayerMoveEvent.class, Block.class, event -> event.getTo().clone().subtract(0.0, 0.5, 0.0).getBlock()));
      registry.register(EventValue.builder(PlayerMoveEvent.class, Location.class).getter(PlayerMoveEvent::getFrom).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(PlayerMoveEvent.class, Location.class, PlayerMoveEvent::getTo));
      registry.register(EventValue.builder(PlayerMoveEvent.class, Chunk.class).getter(event -> event.getFrom().getChunk()).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(PlayerMoveEvent.class, Chunk.class, event -> event.getTo().getChunk()));
      registry.register(EventValue.simple(PlayerItemDamageEvent.class, ItemStack.class, PlayerItemDamageEvent::getItem));
      registry.register(EventValue.simple(PlayerItemMendEvent.class, Player.class, PlayerEvent::getPlayer));
      registry.register(EventValue.simple(PlayerItemMendEvent.class, ItemStack.class, PlayerItemMendEvent::getItem));
      registry.register(EventValue.simple(PlayerItemMendEvent.class, Entity.class, PlayerItemMendEvent::getExperienceOrb));
      registry.register(EventValue.simple(HangingEvent.class, Hanging.class, HangingEvent::getEntity));
      registry.register(EventValue.simple(HangingEvent.class, World.class, event -> event.getEntity().getWorld()));
      registry.register(EventValue.simple(HangingEvent.class, Location.class, event -> event.getEntity().getLocation()));
      registry.register(
         EventValue.simple(
            HangingBreakEvent.class,
            Entity.class,
            event -> event instanceof HangingBreakByEntityEvent hangingBreakByEntityEvent ? hangingBreakByEntityEvent.getRemover() : null
         )
      );
      registry.register(EventValue.simple(HangingPlaceEvent.class, Player.class, HangingPlaceEvent::getPlayer));
      registry.register(EventValue.simple(VehicleEvent.class, Vehicle.class, VehicleEvent::getVehicle));
      registry.register(EventValue.simple(VehicleEvent.class, World.class, event -> event.getVehicle().getWorld()));
      registry.register(EventValue.simple(VehicleExitEvent.class, LivingEntity.class, VehicleExitEvent::getExited));
      registry.register(EventValue.simple(VehicleEnterEvent.class, Entity.class, VehicleEnterEvent::getEntered));
      registry.register(EventValue.simple(VehicleDamageEvent.class, Entity.class, VehicleDamageEvent::getAttacker));
      registry.register(EventValue.simple(VehicleDestroyEvent.class, Entity.class, VehicleDestroyEvent::getAttacker));
      registry.register(EventValue.simple(VehicleEvent.class, Entity.class, event -> event.getVehicle().getPassenger()));
      registry.register(EventValue.simple(ServerCommandEvent.class, CommandSender.class, ServerCommandEvent::getSender));
      registry.register(EventValue.simple(CommandEvent.class, String[].class, CommandEvent::getArgs));
      registry.register(EventValue.simple(CommandEvent.class, CommandSender.class, CommandEvent::getSender));
      registry.register(EventValue.simple(CommandEvent.class, World.class, e -> e.getSender() instanceof Player ? ((Player)e.getSender()).getWorld() : null));
      registry.register(
         EventValue.simple(CommandEvent.class, Block.class, event -> event.getSender() instanceof BlockCommandSender sender ? sender.getBlock() : null)
      );
      registry.register(EventValue.simple(ScriptEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender()));
      registry.register(EventValue.simple(SkriptStartEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender()));
      registry.register(EventValue.simple(SkriptStopEvent.class, CommandSender.class, event -> Bukkit.getConsoleSender()));
      registry.register(EventValue.simple(InventoryClickEvent.class, Player.class, event -> event.getWhoClicked() instanceof Player player ? player : null));
      registry.register(EventValue.simple(InventoryClickEvent.class, World.class, event -> event.getWhoClicked().getWorld()));
      registry.register(EventValue.simple(InventoryClickEvent.class, ItemStack.class, InventoryClickEvent::getCurrentItem));
      registry.register(
         EventValue.simple(
            InventoryClickEvent.class,
            Slot.class,
            event -> {
               Inventory invi = event.getClickedInventory();
               if (invi == null) {
                  return null;
               } else {
                  int slotIndex = event.getSlot();
                  return (Slot)(invi instanceof PlayerInventory itemStacks && slotIndex >= 36
                     ? new ch.njol.skript.util.slot.EquipmentSlot(itemStacks.getHolder(), slotIndex)
                     : new InventorySlot(invi, slotIndex, event.getRawSlot()));
               }
            }
         )
      );
      registry.register(EventValue.simple(InventoryClickEvent.class, InventoryAction.class, InventoryClickEvent::getAction));
      registry.register(EventValue.simple(InventoryClickEvent.class, ClickType.class, InventoryClickEvent::getClick));
      registry.register(EventValue.simple(InventoryClickEvent.class, Inventory.class, InventoryClickEvent::getClickedInventory));
      registry.register(EventValue.simple(InventoryDragEvent.class, Player.class, event -> event.getWhoClicked() instanceof Player player ? player : null));
      registry.register(EventValue.simple(InventoryDragEvent.class, World.class, event -> event.getWhoClicked().getWorld()));
      registry.register(
         EventValue.builder(InventoryDragEvent.class, ItemStack.class).getter(InventoryDragEvent::getOldCursor).time(EventValue.Time.PAST).build()
      );
      registry.register(EventValue.simple(InventoryDragEvent.class, ItemStack.class, InventoryDragEvent::getCursor));
      registry.register(EventValue.simple(InventoryDragEvent.class, ItemStack[].class, event -> event.getNewItems().values().toArray(new ItemStack[0])));
      registry.register(EventValue.simple(InventoryDragEvent.class, Slot[].class, event -> {
         List<Slot> slots = new ArrayList<>(event.getRawSlots().size());
         InventoryView view = event.getView();

         for (Integer rawSlot : event.getRawSlots()) {
            Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
            Integer slot = InventoryUtils.convertSlot(view, rawSlot);
            if (inventory != null && slot != null) {
               if (inventory instanceof PlayerInventory && slot >= 36) {
                  slots.add(new ch.njol.skript.util.slot.EquipmentSlot(((PlayerInventory)view.getBottomInventory()).getHolder(), slot));
               } else {
                  slots.add(new InventorySlot(inventory, slot));
               }
            }
         }

         return slots.toArray(new Slot[0]);
      }));
      registry.register(
         EventValue.simple(InventoryDragEvent.class, ClickType.class, event -> event.getType() == DragType.EVEN ? ClickType.LEFT : ClickType.RIGHT)
      );
      registry.register(EventValue.simple(InventoryDragEvent.class, Inventory[].class, event -> {
         Set<Inventory> inventories = new HashSet<>();
         InventoryView view = event.getView();

         for (Integer rawSlot : event.getRawSlots()) {
            Inventory inventory = InventoryUtils.getInventory(view, rawSlot);
            if (inventory != null) {
               inventories.add(inventory);
            }
         }

         return inventories.toArray(new Inventory[0]);
      }));
      if (Skript.classExists("com.destroystokyo.paper.event.inventory.PrepareResultEvent")) {
         registry.register(EventValue.simple(PrepareAnvilEvent.class, ItemStack.class, PrepareResultEvent::getResult));
      }

      registry.register(EventValue.simple(BlockFertilizeEvent.class, Player.class, BlockFertilizeEvent::getPlayer));
      registry.register(
         EventValue.simple(BlockFertilizeEvent.class, Block[].class, event -> event.getBlocks().stream().map(BlockState::getBlock).toArray(Block[]::new))
      );
      registry.register(EventValue.simple(PrepareItemCraftEvent.class, Slot.class, event -> new InventorySlot(event.getInventory(), 0)));
      registry.register(EventValue.simple(PrepareItemCraftEvent.class, ItemStack.class, event -> {
         ItemStack item = event.getInventory().getResult();
         return item != null ? item : AIR_IS;
      }));
      registry.register(EventValue.simple(PrepareItemCraftEvent.class, Player.class, event -> {
         List<HumanEntity> viewers = event.getInventory().getViewers();
         if (viewers.isEmpty()) {
            return null;
         } else {
            HumanEntity first = viewers.get(0);
            return first instanceof Player player ? player : null;
         }
      }));
      registry.register(
         EventValue.simple(CraftItemEvent.class, String.class, event -> event.getRecipe() instanceof Keyed keyed ? keyed.getKey().toString() : null)
      );
      registry.register(
         EventValue.simple(PrepareItemCraftEvent.class, String.class, event -> event.getRecipe() instanceof Keyed keyed ? keyed.getKey().toString() : null)
      );
      registry.register(EventValue.simple(CraftItemEvent.class, ItemStack.class, event -> {
         Recipe recipe = event.getRecipe();
         return recipe instanceof ComplexRecipe ? event.getCurrentItem() : recipe.getResult();
      }));
      registry.register(EventValue.simple(InventoryEvent.class, Inventory.class, InventoryEvent::getInventory));
      registry.register(EventValue.simple(InventoryOpenEvent.class, Player.class, event -> (Player)event.getPlayer()));
      registry.register(EventValue.simple(InventoryCloseEvent.class, Player.class, event -> (Player)event.getPlayer()));
      if (Skript.classExists("org.bukkit.event.inventory.InventoryCloseEvent$Reason")) {
         registry.register(EventValue.simple(InventoryCloseEvent.class, Reason.class, InventoryCloseEvent::getReason));
      }

      registry.register(EventValue.simple(InventoryPickupItemEvent.class, Inventory.class, InventoryPickupItemEvent::getInventory));
      registry.register(EventValue.simple(InventoryPickupItemEvent.class, Item.class, InventoryPickupItemEvent::getItem));
      registry.register(EventValue.simple(InventoryPickupItemEvent.class, ItemStack.class, event -> event.getItem().getItemStack()));
      registry.register(EventValue.simple(PortalCreateEvent.class, World.class, WorldEvent::getWorld));
      registry.register(
         EventValue.simple(PortalCreateEvent.class, Block[].class, event -> event.getBlocks().stream().map(BlockState::getBlock).toArray(Block[]::new))
      );
      if (Skript.methodExists(PortalCreateEvent.class, "getEntity")) {
         registry.register(EventValue.simple(PortalCreateEvent.class, Entity.class, PortalCreateEvent::getEntity));
      }

      registry.register(EventValue.builder(PlayerEditBookEvent.class, ItemStack.class).getter(event -> {
         ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
         book.setItemMeta(event.getPreviousBookMeta());
         return book;
      }).time(EventValue.Time.PAST).build());
      registry.register(EventValue.simple(PlayerEditBookEvent.class, ItemStack.class, event -> {
         ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
         book.setItemMeta(event.getNewBookMeta());
         return book;
      }));
      registry.register(
         EventValue.builder(PlayerEditBookEvent.class, Component[].class)
            .getter(event -> ExprBookPages.getPages(event.getPreviousBookMeta()).toArray(new Component[0]))
            .time(EventValue.Time.PAST)
            .build()
      );
      registry.register(
         EventValue.simple(PlayerEditBookEvent.class, Component[].class, event -> ExprBookPages.getPages(event.getNewBookMeta()).toArray(new Component[0]))
      );
      registry.register(EventValue.simple(ItemDespawnEvent.class, Item.class, ItemDespawnEvent::getEntity));
      registry.register(EventValue.simple(ItemDespawnEvent.class, ItemStack.class, event -> event.getEntity().getItemStack()));
      registry.register(EventValue.simple(ItemMergeEvent.class, Item.class, ItemMergeEvent::getEntity));
      registry.register(EventValue.builder(ItemMergeEvent.class, Item.class).getter(ItemMergeEvent::getTarget).time(EventValue.Time.FUTURE).build());
      registry.register(EventValue.simple(ItemMergeEvent.class, ItemStack.class, event -> event.getEntity().getItemStack()));
      registry.register(EventValue.simple(PlayerTeleportEvent.class, TeleportCause.class, PlayerTeleportEvent::getCause));
      if (Skript.classExists("io.papermc.paper.event.entity.EntityMoveEvent")) {
         registry.register(EventValue.simple(EntityMoveEvent.class, Location.class, EntityMoveEvent::getFrom));
         registry.register(EventValue.builder(EntityMoveEvent.class, Location.class).getter(EntityMoveEvent::getTo).time(EventValue.Time.FUTURE).build());
      }

      registry.register(EventValue.simple(CreatureSpawnEvent.class, SpawnReason.class, CreatureSpawnEvent::getSpawnReason));
      if (Skript.classExists("org.bukkit.event.player.AbstractRespawnEvent")) {
         registry.register(EventValue.simple(PlayerRespawnEvent.class, RespawnReason.class, AbstractRespawnEvent::getRespawnReason));
      } else {
         try {
            Method method = PlayerRespawnEvent.class.getMethod("getRespawnReason");
            registry.register(EventValue.simple(PlayerRespawnEvent.class, RespawnReason.class, event -> {
               try {
                  return (RespawnReason)method.invoke(event);
               } catch (Exception var3) {
                  return null;
               }
            }));
         } catch (NoSuchMethodException var2) {
         }
      }

      registry.register(EventValue.simple(FireworkExplodeEvent.class, Firework.class, FireworkExplodeEvent::getEntity));
      registry.register(EventValue.simple(FireworkExplodeEvent.class, FireworkEffect.class, event -> {
         List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
         return effects.isEmpty() ? null : effects.get(0);
      }));
      registry.register(EventValue.simple(FireworkExplodeEvent.class, Color[].class, event -> {
         List<FireworkEffect> effects = event.getEntity().getFireworkMeta().getEffects();
         if (effects.isEmpty()) {
            return null;
         } else {
            List<Color> colors = new ArrayList<>();

            for (FireworkEffect fireworkEffect : effects) {
               for (org.bukkit.Color color : fireworkEffect.getColors()) {
                  if (SkriptColor.fromBukkitColor(color) != null) {
                     colors.add(SkriptColor.fromBukkitColor(color));
                  } else {
                     colors.add(ColorRGB.fromBukkitColor(color));
                  }
               }
            }

            return colors.isEmpty() ? null : colors.toArray(Color[]::new);
         }
      }));
      registry.register(EventValue.simple(PlayerRiptideEvent.class, ItemStack.class, PlayerRiptideEvent::getItem));
      if (Skript.classExists("io.papermc.paper.event.player.PlayerInventorySlotChangeEvent")) {
         registry.register(EventValue.simple(PlayerInventorySlotChangeEvent.class, ItemStack.class, PlayerInventorySlotChangeEvent::getNewItemStack));
         registry.register(
            EventValue.builder(PlayerInventorySlotChangeEvent.class, ItemStack.class)
               .getter(PlayerInventorySlotChangeEvent::getOldItemStack)
               .time(EventValue.Time.PAST)
               .build()
         );
         registry.register(EventValue.simple(PlayerInventorySlotChangeEvent.class, Slot.class, event -> {
            PlayerInventory inv = event.getPlayer().getInventory();
            int slotIndex = event.getSlot();
            return (Slot)(slotIndex >= 36 ? new ch.njol.skript.util.slot.EquipmentSlot(event.getPlayer(), slotIndex) : new InventorySlot(inv, slotIndex));
         }));
      }

      registry.register(EventValue.simple(PrepareItemEnchantEvent.class, Player.class, PrepareItemEnchantEvent::getEnchanter));
      registry.register(EventValue.simple(PrepareItemEnchantEvent.class, ItemStack.class, PrepareItemEnchantEvent::getItem));
      registry.register(EventValue.simple(PrepareItemEnchantEvent.class, Block.class, PrepareItemEnchantEvent::getEnchantBlock));
      registry.register(EventValue.simple(EnchantItemEvent.class, Player.class, EnchantItemEvent::getEnchanter));
      registry.register(EventValue.simple(EnchantItemEvent.class, ItemStack.class, EnchantItemEvent::getItem));
      registry.register(
         EventValue.simple(
            EnchantItemEvent.class,
            EnchantmentType[].class,
            event -> event.getEnchantsToAdd()
               .entrySet()
               .stream()
               .map(entry -> new EnchantmentType((Enchantment)entry.getKey(), (Integer)entry.getValue()))
               .toArray(EnchantmentType[]::new)
         )
      );
      registry.register(EventValue.simple(EnchantItemEvent.class, Block.class, EnchantItemEvent::getEnchantBlock));
      registry.register(EventValue.simple(HorseJumpEvent.class, Entity.class, HorseJumpEvent::getEntity));
      if (Skript.classExists("io.papermc.paper.event.player.PlayerTradeEvent")) {
         registry.register(EventValue.simple(PlayerTradeEvent.class, AbstractVillager.class, PlayerTradeEvent::getVillager));
      }

      registry.register(
         EventValue.builder(PlayerChangedWorldEvent.class, World.class).getter(PlayerChangedWorldEvent::getFrom).time(EventValue.Time.PAST).build()
      );
      registry.register(EventValue.simple(PlayerEggThrowEvent.class, Egg.class, PlayerEggThrowEvent::getEgg));
      if (Skript.classExists("io.papermc.paper.event.player.PlayerStopUsingItemEvent")) {
         registry.register(
            EventValue.simple(PlayerStopUsingItemEvent.class, Timespan.class, event -> new Timespan(Timespan.TimePeriod.TICK, event.getTicksHeldFor()))
         );
         registry.register(EventValue.simple(PlayerStopUsingItemEvent.class, ItemType.class, event -> new ItemType(event.getItem())));
      }

      registry.register(EventValue.simple(EntityResurrectEvent.class, Slot.class, event -> {
         EquipmentSlot hand = event.getHand();
         EntityEquipment equipment = event.getEntity().getEquipment();
         return equipment != null && hand != null ? new ch.njol.skript.util.slot.EquipmentSlot(equipment, hand) : null;
      }));
      registry.register(
         EventValue.simple(PlayerItemHeldEvent.class, Slot.class, event -> new InventorySlot(event.getPlayer().getInventory(), event.getNewSlot()))
      );
      registry.register(
         EventValue.builder(PlayerItemHeldEvent.class, Slot.class)
            .getter(event -> new InventorySlot(event.getPlayer().getInventory(), event.getPreviousSlot()))
            .time(EventValue.Time.PAST)
            .build()
      );
      if (Skript.isRunningMinecraft(1, 14)) {
         registry.register(EventValue.simple(PlayerPickupArrowEvent.class, Projectile.class, PlayerPickupArrowEvent::getArrow));
      }

      registry.register(EventValue.simple(PlayerPickupArrowEvent.class, ItemStack.class, event -> event.getItem().getItemStack()));
      if (Skript.classExists("org.bukkit.event.player.PlayerQuitEvent$QuitReason")) {
         registry.register(EventValue.simple(PlayerQuitEvent.class, QuitReason.class, PlayerQuitEvent::getReason));
      }

      if (Skript.classExists("io.papermc.paper.event.player.PlayerStonecutterRecipeSelectEvent")) {
         registry.register(EventValue.simple(PlayerStonecutterRecipeSelectEvent.class, ItemStack.class, event -> event.getStonecuttingRecipe().getResult()));
      }

      registry.register(EventValue.simple(EntityTransformEvent.class, Entity[].class, event -> event.getTransformedEntities().stream().toArray(Entity[]::new)));
      registry.register(EventValue.simple(EntityTransformEvent.class, TransformReason.class, EntityTransformEvent::getTransformReason));
      if (Skript.classExists("org.bukkit.event.block.BellRingEvent")) {
         registry.register(EventValue.simple(BellRingEvent.class, Entity.class, BellRingEvent::getEntity));
         registry.register(EventValue.simple(BellRingEvent.class, Direction.class, event -> new Direction(event.getDirection(), 1.0)));
      } else if (Skript.classExists("io.papermc.paper.event.block.BellRingEvent")) {
         registry.register(EventValue.simple(io.papermc.paper.event.block.BellRingEvent.class, Entity.class, BellRingEvent::getEntity));
      }

      if (Skript.classExists("org.bukkit.event.block.BellResonateEvent")) {
         registry.register(EventValue.simple(BellResonateEvent.class, Entity[].class, event -> event.getResonatedEntities().toArray(new LivingEntity[0])));
      }

      registry.register(EventValue.simple(InventoryMoveItemEvent.class, Inventory.class, InventoryMoveItemEvent::getSource));
      registry.register(
         EventValue.builder(InventoryMoveItemEvent.class, Inventory.class).getter(InventoryMoveItemEvent::getDestination).time(EventValue.Time.FUTURE).build()
      );
      registry.register(EventValue.simple(InventoryMoveItemEvent.class, Block.class, event -> event.getSource().getLocation().getBlock()));
      registry.register(
         EventValue.builder(InventoryMoveItemEvent.class, Block.class)
            .getter(event -> event.getDestination().getLocation().getBlock())
            .time(EventValue.Time.FUTURE)
            .build()
      );
      registry.register(EventValue.simple(InventoryMoveItemEvent.class, ItemStack.class, InventoryMoveItemEvent::getItem));
      registry.register(EventValue.simple(EntityRegainHealthEvent.class, RegainReason.class, EntityRegainHealthEvent::getRegainReason));
      registry.register(EventValue.simple(FurnaceExtractEvent.class, Player.class, FurnaceExtractEvent::getPlayer));
      registry.register(
         EventValue.simple(FurnaceExtractEvent.class, ItemStack[].class, event -> new ItemStack[]{ItemStack.of(event.getItemType(), event.getItemAmount())})
      );
      registry.register(
         EventValue.builder(BlockDropItemEvent.class, Block.class)
            .getter(event -> new BlockStateBlock(event.getBlockState()))
            .time(EventValue.Time.PAST)
            .build()
      );
      registry.register(EventValue.simple(BlockDropItemEvent.class, Player.class, BlockDropItemEvent::getPlayer));
      registry.register(
         EventValue.simple(BlockDropItemEvent.class, ItemStack[].class, event -> event.getItems().stream().map(Item::getItemStack).toArray(ItemStack[]::new))
      );
      registry.register(EventValue.simple(BlockDropItemEvent.class, Entity[].class, event -> event.getItems().toArray(Entity[]::new)));
      registry.register(EventValue.simple(PlayerExpCooldownChangeEvent.class, ChangeReason.class, PlayerExpCooldownChangeEvent::getReason));
      registry.register(
         EventValue.simple(PlayerExpCooldownChangeEvent.class, Timespan.class, event -> new Timespan(Timespan.TimePeriod.TICK, event.getNewCooldown()))
      );
      registry.register(
         EventValue.builder(PlayerExpCooldownChangeEvent.class, Timespan.class)
            .getter(event -> new Timespan(Timespan.TimePeriod.TICK, event.getPlayer().getExpCooldown()))
            .time(EventValue.Time.PAST)
            .build()
      );
      registry.register(EventValue.simple(VehicleMoveEvent.class, Location.class, VehicleMoveEvent::getTo));
      registry.register(EventValue.builder(VehicleMoveEvent.class, Location.class).getter(VehicleMoveEvent::getFrom).time(EventValue.Time.PAST).build());
      if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
         registry.register(
            EventValue.builder(BeaconEffectEvent.class, PotionEffectType.class)
               .getter(event -> event.getEffect().getType())
               .excludes(BeaconEffectEvent.class)
               .excludedErrorMessage("Use 'applied effect' in beacon effect events.")
               .build()
         );
         registry.register(EventValue.simple(BeaconEffectEvent.class, Player.class, BeaconEffectEvent::getPlayer));
      }

      if (Skript.classExists("io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent")) {
         registry.register(EventValue.simple(PlayerChangeBeaconEffectEvent.class, Block.class, PlayerChangeBeaconEffectEvent::getBeacon));
      }

      if (Skript.classExists("com.destroystokyo.paper.event.player.PlayerElytraBoostEvent")) {
         registry.register(EventValue.simple(PlayerElytraBoostEvent.class, ItemStack.class, PlayerElytraBoostEvent::getItemStack));
         registry.register(EventValue.simple(PlayerElytraBoostEvent.class, Entity.class, PlayerElytraBoostEvent::getFirework));
      }

      if (Skript.classExists("org.bukkit.event.block.VaultDisplayItemEvent")) {
         registry.register(
            EventValue.builder(VaultDisplayItemEvent.class, ItemStack.class)
               .getter(VaultDisplayItemEvent::getDisplayItem)
               .registerChanger(Changer.ChangeMode.SET, VaultDisplayItemEvent::setDisplayItem)
               .build()
         );
      }

      registry.register(
         EventValue.simple(
            VillagerCareerChangeEvent.class, org.bukkit.event.entity.VillagerCareerChangeEvent.ChangeReason.class, VillagerCareerChangeEvent::getReason
         )
      );
      registry.register(
         EventValue.builder(VillagerCareerChangeEvent.class, Profession.class)
            .getter(VillagerCareerChangeEvent::getProfession)
            .registerChanger(Changer.ChangeMode.SET, (event, profession) -> {
               if (profession != null) {
                  event.setProfession(profession);
               }
            })
            .build()
      );
      registry.register(
         EventValue.builder(VillagerCareerChangeEvent.class, Profession.class)
            .getter(event -> event.getEntity().getProfession())
            .time(EventValue.Time.PAST)
            .build()
      );
   }
}
