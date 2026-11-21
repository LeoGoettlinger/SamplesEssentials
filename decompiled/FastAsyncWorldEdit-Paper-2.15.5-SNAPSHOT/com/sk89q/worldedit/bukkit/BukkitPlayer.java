package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.util.TaskManager;
import com.sk89q.util.StringUtil;
import com.sk89q.wepif.VaultResolver;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.adapter.bukkit.TextAdapter;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachment;
import org.enginehub.linbus.tree.LinCompoundTag;

public class BukkitPlayer extends AbstractPlayerActor {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final Player player;
   private final WorldEditPlugin plugin;
   private PermissionAttachment permAttachment = null;

   @Deprecated
   public BukkitPlayer(@Nullable Player player) {
      super((Map<String, Object>)(player != null ? getExistingMap(WorldEditPlugin.getInstance(), player) : new ConcurrentHashMap<>()));
      this.plugin = WorldEditPlugin.getInstance();
      this.player = player;
   }

   @Deprecated
   public BukkitPlayer(@Nonnull WorldEditPlugin plugin, @Nullable Player player) {
      this.plugin = plugin;
      this.player = player;
      if (player != null && Settings.settings().CLIPBOARD.USE_DISK) {
         BukkitPlayer cached = WorldEditPlugin.getInstance().getCachedPlayer(player);
         if (cached == null) {
            this.loadClipboardFromDisk();
         }
      }
   }

   private static Map<String, Object> getExistingMap(WorldEditPlugin plugin, Player player) {
      BukkitPlayer cached = plugin.getCachedPlayer(player);
      return (Map<String, Object>)(cached != null ? cached.getRawMeta() : new ConcurrentHashMap<>());
   }

   @Override
   public UUID getUniqueId() {
      return this.player.getUniqueId();
   }

   @Override
   public BaseItemStack getItemInHand(HandSide handSide) {
      ItemStack itemStack = handSide == HandSide.MAIN_HAND ? this.player.getInventory().getItemInMainHand() : this.player.getInventory().getItemInOffHand();
      return BukkitAdapter.adapt(itemStack);
   }

   @Override
   public BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException {
      ItemStack itemStack = handSide == HandSide.MAIN_HAND ? this.player.getInventory().getItemInMainHand() : this.player.getInventory().getItemInOffHand();
      return BukkitAdapter.asBlockState(itemStack).toBaseBlock();
   }

   @Override
   public String getName() {
      return this.player.getName();
   }

   @Override
   public String getDisplayName() {
      return this.player.getDisplayName();
   }

   @Override
   public void giveItem(BaseItemStack itemStack) {
      PlayerInventory inv = this.player.getInventory();
      ItemStack newItem = BukkitAdapter.adapt(itemStack);
      TaskManager.taskManager().sync(() -> {
         if (itemStack.getType().id().equalsIgnoreCase(WorldEdit.getInstance().getConfiguration().wandItem)) {
            inv.remove(newItem);
         }

         ItemStack item = this.player.getInventory().getItemInMainHand();
         this.player.getInventory().setItemInMainHand(newItem);
         HashMap<Integer, ItemStack> overflow = inv.addItem(new ItemStack[]{item});
         if (!overflow.isEmpty()) {
            for (Entry<Integer, ItemStack> entry : overflow.entrySet()) {
               ItemStack stack = entry.getValue();
               if (stack.getType() != Material.AIR && stack.getAmount() > 0) {
                  Item dropped = this.player.getWorld().dropItem(this.player.getLocation(), stack);
                  PlayerDropItemEvent event = new PlayerDropItemEvent(this.player, dropped);
                  Bukkit.getPluginManager().callEvent(event);
                  if (event.isCancelled()) {
                     dropped.remove();
                  }
               }
            }
         }

         this.player.updateInventory();
         return null;
      });
   }

   @Deprecated
   @Override
   public void printRaw(String msg) {
      for (String part : msg.split("\n")) {
         this.player.sendMessage(part);
      }
   }

   @Deprecated
   @Override
   public void print(String msg) {
      for (String part : msg.split("\n")) {
         this.player.sendMessage("§d" + part);
      }
   }

   @Deprecated
   @Override
   public void printDebug(String msg) {
      for (String part : msg.split("\n")) {
         this.player.sendMessage("§7" + part);
      }
   }

   @Deprecated
   @Override
   public void printError(String msg) {
      for (String part : msg.split("\n")) {
         this.player.sendMessage("§c" + part);
      }
   }

   @Override
   public void print(Component component) {
      component = Caption.color(TranslatableComponent.of("prefix", component), this.getLocale());
      TextAdapter.sendMessage(this.player, WorldEditText.format(component, this.getLocale()));
   }

   @Override
   public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
      World world = this.player.getWorld();
      if (pos instanceof Location loc) {
         Extent extent = loc.getExtent();
         if (extent instanceof com.sk89q.worldedit.world.World) {
            world = Bukkit.getWorld(((com.sk89q.worldedit.world.World)extent).getName());
         }
      }

      World finalWorld = world;
      return TaskManager.taskManager().sync(() -> this.player.teleport(new org.bukkit.Location(finalWorld, pos.x(), pos.y(), pos.z(), yaw, pitch)));
   }

   @Override
   public String[] getGroups() {
      return this.plugin.getPermissionsResolver().getGroups(this.player);
   }

   @Override
   public BlockBag getInventoryBlockBag() {
      return new BukkitPlayerBlockBag(this.player);
   }

   @Override
   public GameMode getGameMode() {
      return GameModes.get(this.player.getGameMode().name().toLowerCase(Locale.ROOT));
   }

   @Override
   public void setGameMode(GameMode gameMode) {
      this.player.setGameMode(org.bukkit.GameMode.valueOf(gameMode.id().toUpperCase(Locale.ROOT)));
   }

   @Override
   public boolean hasPermission(String perm) {
      return !this.plugin.getLocalConfiguration().noOpPermissions && this.player.isOp()
         || this.plugin.getPermissionsResolver().hasPermission(this.player.getWorld().getName(), this.player, perm);
   }

   @Override
   public void setPermission(String permission, boolean value) {
      boolean usesuperperms = VaultResolver.perms == null;
      if (VaultResolver.perms != null) {
         if (value) {
            if (!VaultResolver.perms.playerAdd(this.player, permission)) {
               usesuperperms = true;
            }
         } else if (!VaultResolver.perms.playerRemove(this.player, permission)) {
            usesuperperms = true;
         }
      }

      if (usesuperperms) {
         if (this.permAttachment == null) {
            this.permAttachment = this.plugin.getPermissionAttachmentManager().getOrAddAttachment(this.player);
         }

         if (this.permAttachment == null) {
            LOGGER.warn("Attempted to set permission for offline player `{}`, UUID: `{}`?!", this.player.getName(), this.player.getUniqueId());
            return;
         }

         this.permAttachment.setPermission(permission, value);
      }
   }

   @Override
   public com.sk89q.worldedit.world.World getWorld() {
      return BukkitAdapter.adapt(this.player.getWorld());
   }

   @Override
   public void dispatchCUIEvent(CUIEvent event) {
      String[] params = event.getParameters();
      String send = event.getTypeId();
      if (params.length > 0) {
         send = send + "|" + StringUtil.joinString(params, "|");
      }

      this.player.sendPluginMessage(this.plugin, "worldedit:cui", send.getBytes(StandardCharsets.UTF_8));
   }

   public Player getPlayer() {
      return this.player;
   }

   @Override
   public boolean isAllowedToFly() {
      return this.player.getAllowFlight();
   }

   @Override
   public void setFlying(boolean flying) {
      this.player.setFlying(flying);
   }

   @Override
   public BaseEntity getState() {
      throw new UnsupportedOperationException("Cannot create a state from this object");
   }

   @Override
   public Location getLocation() {
      org.bukkit.Location nativeLocation = this.player.getLocation();
      Vector3 position = BukkitAdapter.asVector(nativeLocation);
      return new Location(this.getWorld(), position, nativeLocation.getYaw(), nativeLocation.getPitch());
   }

   @Override
   public boolean setLocation(Location location) {
      return this.player.teleport(BukkitAdapter.adapt(location));
   }

   @Override
   public Locale getLocale() {
      return TextUtils.getLocaleByMinecraftTag(this.player.getLocale());
   }

   @Override
   public void sendAnnouncements() {
      if (!WorldEditPlugin.getInstance().getLifecycledBukkitImplAdapter().isValid()) {
         this.print(
            Caption.of(
               "worldedit.version.bukkit.unsupported-adapter",
               TextComponent.of("https://intellectualsites.github.io/download/fawe.html", TextColor.AQUA)
                  .clickEvent(ClickEvent.openUrl("https://intellectualsites.github.io/download/fawe.html"))
            )
         );
      }
   }

   @Nullable
   @Override
   public <T> T getFacet(Class<? extends T> cls) {
      return null;
   }

   @Override
   public SessionKey getSessionKey() {
      return new BukkitPlayer.SessionKeyImpl(this.player);
   }

   @Override
   public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, @Nullable B block) {
      org.bukkit.Location loc = new org.bukkit.Location(this.player.getWorld(), pos.x(), pos.y(), pos.z());
      BaseBlock baseBlock;
      if (block != null) {
         baseBlock = block.toBaseBlock();
      } else {
         baseBlock = this.getExtent().getFullBlock(pos);
      }

      BlockData data = BukkitAdapter.adapt(baseBlock);
      this.player.sendBlockChange(loc, data);
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         LinCompoundTag nbtData = baseBlock.getNbt();
         if (nbtData != null && data.createBlockState() instanceof TileState tileState) {
            adapter.sendFakeNBT(this.player, pos, tileState, nbtData);
         }
      }
   }

   @Override
   public void sendFakeOP() {
      BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
      if (adapter != null) {
         adapter.sendFakeOP(this.player);
      }
   }

   @Override
   public void sendTitle(Component title, Component sub) {
      String titleStr = WorldEditText.reduceToText(title, this.getLocale());
      String subStr = WorldEditText.reduceToText(sub, this.getLocale());
      this.player.sendTitle(titleStr, subStr, 0, 70, 20);
   }

   @Override
   public void unregister() {
      this.player.removeMetadata("WE", WorldEditPlugin.getInstance());
      this.plugin.getPermissionAttachmentManager().removeAttachment(this.player);
      super.unregister();
   }

   static class SessionKeyImpl implements SessionKey {
      private final UUID uuid;
      private final String name;

      SessionKeyImpl(Player player) {
         this.uuid = player.getUniqueId();
         this.name = player.getName();
      }

      @Override
      public UUID getUniqueId() {
         return this.uuid;
      }

      @Nullable
      @Override
      public String getName() {
         return this.name;
      }

      @Override
      public boolean isActive() {
         return Bukkit.getServer().getPlayer(this.uuid) != null;
      }

      @Override
      public boolean isPersistent() {
         return true;
      }
   }
}
