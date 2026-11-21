package net.coreprotect.listener.player;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.CoreProtect;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.block.CampfireStartListener;
import net.coreprotect.listener.player.inspector.BlockInspector;
import net.coreprotect.listener.player.inspector.ContainerInspector;
import net.coreprotect.listener.player.inspector.InteractionInspector;
import net.coreprotect.listener.player.inspector.SignInspector;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.thread.CacheHandler;
import net.coreprotect.thread.Scheduler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class PlayerInteractListener extends Queue implements Listener {
   public static ConcurrentHashMap<String, Object[]> lastInspectorEvent = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, Object[]> suspiciousBlockEvent = new ConcurrentHashMap<>();
   private final BlockInspector blockInspector = new BlockInspector();
   private final SignInspector signInspector = new SignInspector();
   private final ContainerInspector containerInspector = new ContainerInspector();
   private final InteractionInspector interactionInspector = new InteractionInspector();

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   protected void onPlayerInspect(PlayerInteractEvent event) {
      Player player = event.getPlayer();
      World world = player.getWorld();
      if (Boolean.TRUE.equals(ConfigHandler.inspecting.get(player.getName()))) {
         if (!player.hasPermission("coreprotect.inspect")) {
            Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_PERMISSION));
            ConfigHandler.inspecting.put(player.getName(), false);
         } else {
            if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
               BlockState checkBlock = event.getClickedBlock().getState();
               int x = checkBlock.getX();
               int y = checkBlock.getY();
               int z = checkBlock.getZ();
               BlockData checkBlockData = checkBlock.getBlockData();
               if (checkBlockData instanceof Bisected
                  && !(checkBlockData instanceof Waterlogged)
                  && ((Bisected)checkBlockData).getHalf().equals(Half.TOP)
                  && y > BukkitAdapter.ADAPTER.getMinHeight(world)) {
                  checkBlock = world.getBlockAt(checkBlock.getX(), checkBlock.getY() - 1, checkBlock.getZ()).getState();
               }

               if (checkBlockData instanceof Bed) {
                  Bed bed = (Bed)checkBlockData;
                  if (bed.getPart().equals(Part.HEAD)) {
                     checkBlock = event.getClickedBlock().getRelative(bed.getFacing().getOppositeFace()).getState();
                  }
               }

               this.blockInspector.performBlockLookup(player, checkBlock);
               if (checkBlockData instanceof Bisected) {
                  PlayerInteractUtils.handleBisectedBlockVisualization(player, event.getClickedBlock(), world);
               } else {
                  Block block = event.getClickedBlock();
                  int blockX = block.getX();
                  int blockY = block.getY();
                  int blockZ = block.getZ();
                  Block x1 = world.getBlockAt(blockX + 1, blockY, blockZ);
                  Block x2 = world.getBlockAt(blockX - 1, blockY, blockZ);
                  Block z1 = world.getBlockAt(blockX, blockY, blockZ + 1);
                  Block z2 = world.getBlockAt(blockX, blockY, blockZ - 1);
                  player.sendBlockChange(x1.getLocation(), x1.getBlockData());
                  player.sendBlockChange(x2.getLocation(), x2.getBlockData());
                  player.sendBlockChange(z1.getLocation(), z1.getBlockData());
                  player.sendBlockChange(z2.getLocation(), z2.getBlockData());
               }

               event.setCancelled(true);
            } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
               Block block = event.getClickedBlock();
               if (block != null) {
                  Material type = block.getType();
                  boolean isInteractBlock = BlockGroup.INTERACT_BLOCKS.contains(type);
                  boolean isContainerBlock = BlockGroup.CONTAINERS.contains(type);
                  boolean isSignBlock = BukkitAdapter.ADAPTER.isSign(type);
                  if (!isInteractBlock && !isContainerBlock && !isSignBlock) {
                     boolean performLookup = true;
                     EquipmentSlot eventHand = event.getHand();
                     String uuid = event.getPlayer().getUniqueId().toString();
                     long systemTime = System.currentTimeMillis();
                     if (lastInspectorEvent.get(uuid) != null) {
                        Object[] lastEvent = lastInspectorEvent.get(uuid);
                        long lastTime = (Long)lastEvent[0];
                        EquipmentSlot lastHand = (EquipmentSlot)lastEvent[1];
                        long timeSince = systemTime - lastTime;
                        if (timeSince < 50L && !eventHand.equals(lastHand)) {
                           performLookup = false;
                        }
                     }

                     if (performLookup) {
                        BlockState finalBlock = event.getClickedBlock().getRelative(event.getBlockFace()).getState();
                        this.blockInspector.performAirBlockLookup(player, finalBlock);
                        ItemUtils.updateInventory(event.getPlayer());
                        lastInspectorEvent.put(uuid, new Object[]{systemTime, eventHand});
                        if (event.hasItem()) {
                           Material eventItem = event.getItem().getType();
                           if (eventItem.isBlock() && eventItem.createBlockData() instanceof Bisected) {
                              int xx = finalBlock.getX();
                              int yx = finalBlock.getY();
                              int zx = finalBlock.getZ();
                              int worldMaxHeight = world.getMaxHeight();
                              if (yx < worldMaxHeight - 1) {
                                 Block blockBisected = world.getBlockAt(xx, yx + 1, zx);
                                 player.sendBlockChange(blockBisected.getLocation(), blockBisected.getBlockData());
                              }

                              int worldMinHeight = BukkitAdapter.ADAPTER.getMinHeight(world);
                              if (yx > worldMinHeight) {
                                 Block blockBisected = world.getBlockAt(xx, yx - 1, zx);
                                 player.sendBlockChange(blockBisected.getLocation(), blockBisected.getBlockData());
                              }
                           }
                        }
                     }

                     event.setCancelled(true);
                  } else {
                     Block clickedBlock = event.getClickedBlock();
                     if (isSignBlock) {
                        Location location = clickedBlock.getLocation();
                        this.signInspector.performSignLookup(player, location);
                        event.setCancelled(true);
                     } else if (isContainerBlock && Config.getConfig(world).ITEM_TRANSACTIONS) {
                        Location location = null;
                        if (type.equals(Material.CHEST) || type.equals(Material.TRAPPED_CHEST) || BukkitAdapter.ADAPTER.isCopperChest(type)) {
                           Chest chest = (Chest)clickedBlock.getState();
                           InventoryHolder inventoryHolder = chest.getInventory().getHolder();
                           if (inventoryHolder instanceof DoubleChest) {
                              DoubleChest doubleChest = (DoubleChest)inventoryHolder;
                              location = doubleChest.getLocation();
                           } else {
                              location = chest.getLocation();
                           }
                        }

                        if (location == null) {
                           location = clickedBlock.getLocation();
                        }

                        this.containerInspector.performContainerLookup(player, location);
                        event.setCancelled(true);
                     } else if (isInteractBlock) {
                        Block interactBlock = clickedBlock;
                        if (BlockGroup.DOORS.contains(type)) {
                           int yxx = clickedBlock.getY() - 1;
                           Block blockUnder = clickedBlock.getWorld().getBlockAt(clickedBlock.getX(), yxx, clickedBlock.getZ());
                           if (blockUnder.getType().equals(type)) {
                              interactBlock = blockUnder;
                           }
                        }

                        this.interactionInspector.performInteractionLookup(player, interactBlock);
                        if (!BlockGroup.SAFE_INTERACT_BLOCKS.contains(type) || player.isSneaking()) {
                           event.setCancelled(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   protected void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
         World world = event.getClickedBlock().getWorld();
         if (event.useInteractedBlock() != Result.DENY) {
            Block block = event.getClickedBlock();
            if (block.getType() == Material.DRAGON_EGG) {
               PlayerInteractUtils.clickedDragonEgg(event.getPlayer(), block);
            }

            if (Config.getConfig(world).BLOCK_BREAK) {
               Block relativeBlock = event.getClickedBlock().getRelative(event.getBlockFace());
               if (BlockGroup.FIRE.contains(relativeBlock.getType())) {
                  Player player = event.getPlayer();
                  Material type = relativeBlock.getType();
                  Queue.queueBlockBreak(player.getName(), relativeBlock.getState(), type, relativeBlock.getBlockData().getAsString(), 0);
               }
            }
         }
      } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
         Player player = event.getPlayer();
         Block blockx = event.getClickedBlock();
         World world = player.getWorld();
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && blockx != null) {
            Material type = blockx.getType();
            if (event.useInteractedBlock() != Result.DENY) {
               boolean isCake = false;
               if (BukkitAdapter.ADAPTER.isSign(type)) {
                  Set<Material> dyeSet = EnumSet.of(
                     Material.BLACK_DYE,
                     Material.BLUE_DYE,
                     Material.BROWN_DYE,
                     Material.CYAN_DYE,
                     Material.GRAY_DYE,
                     Material.GREEN_DYE,
                     Material.LIGHT_BLUE_DYE,
                     Material.LIGHT_GRAY_DYE,
                     Material.LIME_DYE,
                     Material.MAGENTA_DYE,
                     Material.ORANGE_DYE,
                     Material.PINK_DYE,
                     Material.PURPLE_DYE,
                     Material.RED_DYE,
                     Material.WHITE_DYE,
                     Material.YELLOW_DYE
                  );
                  Material handType = null;
                  ItemStack mainHand = player.getInventory().getItemInMainHand();
                  if (event.getHand().equals(EquipmentSlot.HAND) && mainHand != null) {
                     handType = mainHand.getType();
                  }

                  if (handType != null
                     && (dyeSet.contains(handType) || handType.name().endsWith("INK_SAC") || handType == Material.HONEYCOMB)
                     && Config.getConfig(blockx.getWorld()).SIGN_TEXT) {
                     BlockState blockState = blockx.getState();
                     Sign sign = (Sign)blockState;
                     String line1 = PaperAdapter.ADAPTER.getLine(sign, 0);
                     String line2 = PaperAdapter.ADAPTER.getLine(sign, 1);
                     String line3 = PaperAdapter.ADAPTER.getLine(sign, 2);
                     String line4 = PaperAdapter.ADAPTER.getLine(sign, 3);
                     String line5 = PaperAdapter.ADAPTER.getLine(sign, 4);
                     String line6 = PaperAdapter.ADAPTER.getLine(sign, 5);
                     String line7 = PaperAdapter.ADAPTER.getLine(sign, 6);
                     String line8 = PaperAdapter.ADAPTER.getLine(sign, 7);
                     boolean isFront = true;
                     int oldColor = BukkitAdapter.ADAPTER.getColor(sign, isFront);
                     int oldColorSecondary = BukkitAdapter.ADAPTER.getColor(sign, !isFront);
                     boolean oldFrontGlowing = BukkitAdapter.ADAPTER.isGlowing(sign, isFront);
                     boolean oldBackGlowing = BukkitAdapter.ADAPTER.isGlowing(sign, !isFront);
                     boolean oldIsWaxed = BukkitAdapter.ADAPTER.isWaxed(sign);
                     if (!oldIsWaxed) {
                        Scheduler.runTask(
                           CoreProtect.getInstance(),
                           () -> {
                              BlockState newState = block.getState();
                              if (newState instanceof Sign) {
                                 Sign newSign = (Sign)newState;
                                 int newColor = BukkitAdapter.ADAPTER.getColor(newSign, isFront);
                                 int newColorSecondary = BukkitAdapter.ADAPTER.getColor(newSign, !isFront);
                                 boolean newFrontGlowing = BukkitAdapter.ADAPTER.isGlowing(newSign, isFront);
                                 boolean newBackGlowing = BukkitAdapter.ADAPTER.isGlowing(newSign, !isFront);
                                 boolean newIsWaxed = BukkitAdapter.ADAPTER.isWaxed(newSign);
                                 boolean modifyingFront = oldBackGlowing == newBackGlowing && oldColorSecondary == newColorSecondary;
                                 if (oldColor != newColor
                                    || oldColorSecondary != newColorSecondary
                                    || oldFrontGlowing != newFrontGlowing
                                    || oldBackGlowing != newBackGlowing
                                    || oldIsWaxed != newIsWaxed) {
                                    Location location = blockState.getLocation();
                                    Queue.queueSignText(
                                       player.getName(),
                                       location,
                                       0,
                                       oldColor,
                                       oldColorSecondary,
                                       oldFrontGlowing,
                                       oldBackGlowing,
                                       oldIsWaxed,
                                       modifyingFront,
                                       line1,
                                       line2,
                                       line3,
                                       line4,
                                       line5,
                                       line6,
                                       line7,
                                       line8,
                                       1
                                    );
                                    Queue.queueBlockPlace(
                                       player.getName(),
                                       blockState,
                                       block.getType(),
                                       blockState,
                                       block.getType(),
                                       -1,
                                       0,
                                       blockState.getBlockData().getAsString()
                                    );
                                    Queue.queueSignText(
                                       player.getName(),
                                       location,
                                       2,
                                       newColor,
                                       newColorSecondary,
                                       newFrontGlowing,
                                       newBackGlowing,
                                       newIsWaxed,
                                       modifyingFront,
                                       line1,
                                       line2,
                                       line3,
                                       line4,
                                       line5,
                                       line6,
                                       line7,
                                       line8,
                                       0
                                    );
                                 }
                              }
                           },
                           blockx.getLocation()
                        );
                     }
                  }
               } else if (BlockGroup.INTERACT_BLOCKS.contains(type)) {
                  if (event.getHand().equals(EquipmentSlot.HAND) && Config.getConfig(world).PLAYER_INTERACTIONS) {
                     Block interactBlock = event.getClickedBlock();
                     if (BlockGroup.DOORS.contains(type)) {
                        int y = interactBlock.getY() - 1;
                        Block blockUnder = interactBlock.getWorld().getBlockAt(interactBlock.getX(), y, interactBlock.getZ());
                        if (blockUnder.getType().equals(type)) {
                           interactBlock = blockUnder;
                        }
                     }

                     Queue.queuePlayerInteraction(player.getName(), interactBlock.getState(), type);
                  }
               } else if (BlockGroup.LIGHTABLES.contains(type)) {
                  BlockData blockData = blockx.getBlockData();
                  if (!(blockData instanceof Lightable)
                     || !((Lightable)blockData).isLit()
                     || (!BlockGroup.CANDLES.contains(type) || event.getMaterial() != Material.AIR)
                        && (BlockGroup.CANDLES.contains(type) || !event.getMaterial().name().endsWith("_SHOVEL"))) {
                     if (CampfireStartListener.useCampfireStartEvent && (type == Material.CAMPFIRE || type == Material.SOUL_CAMPFIRE)) {
                        ItemStack handItem = null;
                        ItemStack mainHandx = player.getInventory().getItemInMainHand();
                        ItemStack offHand = player.getInventory().getItemInOffHand();
                        if (event.getHand().equals(EquipmentSlot.HAND) && mainHandx != null && mainHandx.getType() != Material.BUCKET) {
                           handItem = mainHandx;
                        } else {
                           if (!event.getHand().equals(EquipmentSlot.OFF_HAND) || offHand == null) {
                              return;
                           }

                           handItem = offHand;
                        }

                        if (player.getGameMode() != GameMode.CREATIVE) {
                           Location location = blockx.getLocation();
                           long time = System.currentTimeMillis();
                           int wid = WorldUtils.getWorldId(location.getWorld().getName());
                           int x = location.getBlockX();
                           int y = location.getBlockY();
                           int z = location.getBlockZ();
                           String coordinates = x + "." + y + "." + z + "." + wid + "." + type.name();
                           CacheHandler.interactCache.put(coordinates, new Object[]{time, handItem, player.getName()});
                        }
                     }
                  } else {
                     ((Lightable)blockData).setLit(false);
                     Queue.queueBlockPlace(player.getName(), blockx.getState(), type, blockx.getState(), type, -1, 0, blockData.getAsString());
                  }

                  isCake = type.name().endsWith(Material.CAKE.name());
               } else if (type == Material.JUKEBOX) {
                  BlockState blockState = blockx.getState();
                  if (blockState instanceof Jukebox) {
                     Jukebox jukebox = (Jukebox)blockState;
                     ItemStack jukeboxRecord = jukebox.isPlaying() ? jukebox.getRecord() : new ItemStack(Material.AIR);
                     ItemStack oldItemState = jukeboxRecord.clone();
                     ItemStack newItemState = new ItemStack(Material.AIR);
                     if (jukeboxRecord.getType() == Material.AIR) {
                        ItemStack handItemx = null;
                        ItemStack mainHandxx = player.getInventory().getItemInMainHand();
                        ItemStack offHandx = player.getInventory().getItemInOffHand();
                        if (event.getHand().equals(EquipmentSlot.HAND) && mainHandxx != null && mainHandxx.getType().name().startsWith("MUSIC_DISC")) {
                           handItemx = mainHandxx;
                        } else {
                           if (!event.getHand().equals(EquipmentSlot.OFF_HAND) || offHandx == null || !offHandx.getType().name().startsWith("MUSIC_DISC")) {
                              return;
                           }

                           handItemx = offHandx;
                        }

                        oldItemState = new ItemStack(Material.AIR);
                        newItemState = handItemx.clone();
                     }

                     if (!oldItemState.equals(newItemState)) {
                        if (Config.getConfig(player.getWorld()).PLAYER_INTERACTIONS) {
                           Queue.queuePlayerInteraction(player.getName(), blockState, type);
                        }

                        if (Config.getConfig(blockx.getWorld()).ITEM_TRANSACTIONS) {
                           boolean logDrops = player.getGameMode() != GameMode.CREATIVE;
                           ItemStack[] oldState = new ItemStack[]{oldItemState};
                           ItemStack[] newState = new ItemStack[]{newItemState};
                           PlayerInteractEntityListener.queueContainerSpecifiedItems(
                              player.getName(), Material.JUKEBOX, new Object[]{oldState, newState}, jukebox.getLocation(), logDrops
                           );
                        }
                     }
                  }
               } else if (BukkitAdapter.ADAPTER.isChiseledBookshelf(type)) {
                  BlockState blockState = blockx.getState();
                  if (blockState instanceof BlockInventoryHolder) {
                     ItemStack book = BukkitAdapter.ADAPTER.getChiseledBookshelfBook(blockState, event);
                     if (book == null) {
                        InventoryChangeListener.inventoryTransaction(player.getName(), blockState.getLocation(), null);
                     } else {
                        ItemStack oldItemStatex = book.clone();
                        ItemStack newItemStatex = new ItemStack(Material.AIR);
                        if (book.getType() == Material.AIR) {
                           ItemStack handItemx = null;
                           ItemStack mainHandxx = player.getInventory().getItemInMainHand();
                           ItemStack offHandx = player.getInventory().getItemInOffHand();
                           if (event.getHand().equals(EquipmentSlot.HAND) && mainHandxx != null && BukkitAdapter.ADAPTER.isBookshelfBook(mainHandxx.getType())) {
                              handItemx = mainHandxx;
                           } else {
                              if (!event.getHand().equals(EquipmentSlot.OFF_HAND)
                                 || offHandx == null
                                 || !BukkitAdapter.ADAPTER.isBookshelfBook(offHandx.getType())) {
                                 return;
                              }

                              handItemx = offHandx;
                           }

                           oldItemStatex = new ItemStack(Material.AIR);
                           newItemStatex = handItemx.clone();
                        }

                        if (!oldItemStatex.equals(newItemStatex)) {
                           if (Config.getConfig(player.getWorld()).PLAYER_INTERACTIONS) {
                              Queue.queuePlayerInteraction(player.getName(), blockState, type);
                           }

                           InventoryChangeListener.inventoryTransaction(player.getName(), blockState.getLocation(), null);
                        }
                     }
                  }
               } else if (BukkitAdapter.ADAPTER.isDecoratedPot(type)) {
                  BlockState blockState = blockx.getState();
                  InventoryChangeListener.inventoryTransaction(player.getName(), blockState.getLocation(), null);
               } else if (!BukkitAdapter.ADAPTER.isSuspiciousBlock(type)) {
                  if (type == Material.DRAGON_EGG) {
                     PlayerInteractUtils.clickedDragonEgg(player, blockx);
                  }
               } else {
                  ItemStack handItemx = null;
                  ItemStack mainHandxx = player.getInventory().getItemInMainHand();
                  ItemStack offHandx = player.getInventory().getItemInOffHand();
                  if (event.getHand().equals(EquipmentSlot.HAND) && mainHandxx != null) {
                     handItemx = mainHandxx;
                  } else if (event.getHand().equals(EquipmentSlot.OFF_HAND) && offHandx != null) {
                     handItemx = offHandx;
                  }

                  if (handItemx.getType() == Material.BRUSH) {
                     BlockState blockState = blockx.getState();
                     Location blockLocation = blockx.getLocation();
                     Scheduler.scheduleSyncDelayedTask(CoreProtect.getInstance(), () -> {
                        Material newType = block.getType();
                        if (type != newType && (newType == Material.SAND || newType == Material.GRAVEL)) {
                           long systemTime = System.currentTimeMillis();
                           boolean logChange = true;
                           if (suspiciousBlockEvent.get(player.getName()) != null) {
                              Object[] lastEvent = suspiciousBlockEvent.get(player.getName());
                              long lastTime = (Long)lastEvent[0];
                              long timeSince = systemTime - lastTime;
                              Location lastLocation = (Location)lastEvent[1];
                              if (timeSince < 5000L && blockLocation.equals(lastLocation)) {
                                 logChange = false;
                              }
                           }

                           if (logChange) {
                              Queue.queueBlockPlace(player.getName(), blockState, newType, blockState, newType, -1, 0, null);
                              suspiciousBlockEvent.put(player.getName(), new Object[]{systemTime, blockLocation});
                           }
                        }
                     }, blockLocation, 100);
                  }
               }

               if (isCake || type == Material.CAKE) {
                  boolean placeCandle = false;
                  if (type == Material.CAKE) {
                     Material handTypex = null;
                     ItemStack mainHandxxx = player.getInventory().getItemInMainHand();
                     if (event.getHand().equals(EquipmentSlot.HAND) && mainHandxxx != null) {
                        handTypex = mainHandxxx.getType();
                     }

                     if (handTypex != null && BlockGroup.CANDLES.contains(handTypex) && Config.getConfig(blockx.getWorld()).BLOCK_PLACE) {
                        BlockState blockState = blockx.getState();
                        BlockData blockData = blockState.getBlockData();
                        Material newMaterial = Material.getMaterial(handTypex.name() + "_" + Material.CAKE.name());
                        if (newMaterial != null && ((Cake)blockData).getBites() == 0) {
                           Queue.queueBlockPlace(player.getName(), blockx.getState(), blockx.getType(), blockState, newMaterial, -1, 0, blockData.getAsString());
                           placeCandle = true;
                        }
                     }
                  }

                  if (!placeCandle) {
                     String userUUID = player.getUniqueId().toString();
                     Location location = player.getLocation();
                     long time = System.currentTimeMillis();
                     int wid = WorldUtils.getWorldId(location.getWorld().getName());
                     int x = location.getBlockX();
                     int y = location.getBlockY();
                     int z = location.getBlockZ();
                     String coordinates = x + "." + y + "." + z + "." + wid + "." + userUUID;
                     CacheHandler.interactCache.put(coordinates, new Object[]{time, Material.CAKE, blockx.getState()});
                  }
               }
            }
         }

         if (event.useItemInHand() != Result.DENY) {
            List<Material> entityBlockTypes = Arrays.asList(
               Material.ARMOR_STAND,
               Material.END_CRYSTAL,
               Material.BOW,
               Material.CROSSBOW,
               Material.TRIDENT,
               Material.EXPERIENCE_BOTTLE,
               Material.SPLASH_POTION,
               Material.LINGERING_POTION,
               Material.ENDER_PEARL,
               Material.FIREWORK_ROCKET,
               Material.EGG,
               Material.SNOWBALL
            );

            try {
               entityBlockTypes.add(Material.valueOf("WIND_CHARGE"));
            } catch (Exception var26) {
            }

            ItemStack handItemxx = null;
            ItemStack mainHandxxxx = player.getInventory().getItemInMainHand();
            ItemStack offHandxx = player.getInventory().getItemInOffHand();
            if (event.getHand().equals(EquipmentSlot.HAND) && mainHandxxxx != null && entityBlockTypes.contains(mainHandxxxx.getType())) {
               handItemxx = mainHandxxxx;
            } else {
               if (!event.getHand().equals(EquipmentSlot.OFF_HAND) || offHandxx == null || !entityBlockTypes.contains(offHandxx.getType())) {
                  return;
               }

               handItemxx = offHandxx;
            }

            if (handItemxx.getType().equals(Material.END_CRYSTAL)) {
               if (blockx != null
                  && Config.getConfig(world).BLOCK_PLACE
                  && (blockx.getType().equals(Material.OBSIDIAN) || blockx.getType().equals(Material.BEDROCK))) {
                  Location crystalLocation = blockx.getLocation().clone();
                  crystalLocation.setY(crystalLocation.getY() + 1.0);
                  boolean exists = false;

                  for (Entity entity : crystalLocation.getChunk().getEntities()) {
                     if (entity instanceof EnderCrystal
                        && entity.getLocation().getBlockX() == crystalLocation.getBlockX()
                        && entity.getLocation().getBlockY() == crystalLocation.getBlockY()
                        && entity.getLocation().getBlockZ() == crystalLocation.getBlockZ()) {
                        exists = true;
                        break;
                     }
                  }

                  if (!exists) {
                     Location locationFinal = crystalLocation;
                     Scheduler.runTask(
                        CoreProtect.getInstance(),
                        () -> {
                           try {
                              boolean blockExists = false;
                              int showingBottom = 0;

                              for (Entity entityx : locationFinal.getChunk().getEntities()) {
                                 if (entityx instanceof EnderCrystal
                                    && entityx.getLocation().getBlockX() == locationFinal.getBlockX()
                                    && entityx.getLocation().getBlockY() == locationFinal.getBlockY()
                                    && entityx.getLocation().getBlockZ() == locationFinal.getBlockZ()) {
                                    EnderCrystal enderCrystal = (EnderCrystal)entityx;
                                    showingBottom = enderCrystal.isShowingBottom() ? 1 : 0;
                                    blockExists = true;
                                    break;
                                 }
                              }

                              if (blockExists) {
                                 Queue.queueBlockPlace(
                                    player.getName(),
                                    locationFinal.getBlock().getState(),
                                    locationFinal.getBlock().getType(),
                                    locationFinal.getBlock().getState(),
                                    Material.END_CRYSTAL,
                                    showingBottom,
                                    1,
                                    null
                                 );
                              }
                           } catch (Exception var9x) {
                              var9x.printStackTrace();
                           }
                        },
                        locationFinal
                     );
                  }
               }
            } else {
               Location relativeBlockLocation = player.getLocation().clone();
               relativeBlockLocation.setY(relativeBlockLocation.getY() + 1.0);
               Location blockLocation = relativeBlockLocation.clone();
               blockLocation.setY(blockLocation.getY() + 1.0);
               if (handItemxx.getType() == Material.ARMOR_STAND || handItemxx.getType() == Material.FIREWORK_ROCKET) {
                  if (blockx == null) {
                     return;
                  }

                  Block relativeBlock = blockx.getRelative(event.getBlockFace());
                  relativeBlockLocation = relativeBlock.getLocation();
                  blockLocation = blockx.getLocation();
               }

               String relativeBlockKey = world.getName()
                  + "-"
                  + relativeBlockLocation.getBlockX()
                  + "-"
                  + relativeBlockLocation.getBlockY()
                  + "-"
                  + relativeBlockLocation.getBlockZ();
               String blockKey = world.getName() + "-" + blockLocation.getBlockX() + "-" + blockLocation.getBlockY() + "-" + blockLocation.getBlockZ();
               Object[] keys = new Object[]{System.currentTimeMillis(), relativeBlockKey, blockKey, handItemxx};
               ConfigHandler.entityBlockMapper.put(player.getName(), keys);
            }
         }
      } else if (event.getAction().equals(Action.PHYSICAL)) {
         Block blockxx = event.getClickedBlock();
         if (blockxx == null || !blockxx.getType().equals(Material.FARMLAND) && !blockxx.getType().equals(Material.TURTLE_EGG)) {
            return;
         }

         World worldx = blockxx.getWorld();
         if (event.useInteractedBlock() != Result.DENY && Config.getConfig(worldx).BLOCK_BREAK) {
            Player playerx = event.getPlayer();
            if (blockxx.getType().equals(Material.FARMLAND)) {
               Block blockAbove = worldx.getBlockAt(blockxx.getX(), blockxx.getY() + 1, blockxx.getZ());
               Material type = blockAbove.getType();
               if (!type.equals(Material.AIR) && !type.equals(Material.CAVE_AIR)) {
                  Queue.queueBlockBreak(playerx.getName(), blockAbove.getState(), type, blockAbove.getBlockData().getAsString(), 0);
               }
            }

            Queue.queueBlockBreak(playerx.getName(), blockxx.getState(), blockxx.getType(), blockxx.getBlockData().getAsString(), 0);
            Queue.queueBlockPlaceDelayed(playerx.getName(), blockxx.getLocation(), blockxx.getType(), null, null, 0);
         }
      }
   }
}
