package com.sk89q.worldedit.extension.platform;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;

public class PlayerProxy extends AbstractPlayerActor {
   private final Player basePlayer;
   private final Actor permActor;
   private final Actor cuiActor;
   private final World world;
   private Vector3 offset = Vector3.ZERO;

   public PlayerProxy(Player player) {
      this(player, player, player, player.getWorld());
   }

   public PlayerProxy(Player basePlayer, Actor permActor, Actor cuiActor, World world) {
      super(basePlayer.getRawMeta());
      Preconditions.checkNotNull(basePlayer);
      Preconditions.checkNotNull(permActor);
      Preconditions.checkNotNull(cuiActor);
      Preconditions.checkNotNull(world);
      this.basePlayer = basePlayer;
      this.permActor = permActor;
      this.cuiActor = cuiActor;
      this.world = world;
   }

   @Override
   public UUID getUniqueId() {
      return this.basePlayer.getUniqueId();
   }

   @Override
   public BaseItemStack getItemInHand(HandSide handSide) {
      return this.basePlayer.getItemInHand(handSide);
   }

   @Override
   public void giveItem(BaseItemStack itemStack) {
      this.basePlayer.giveItem(itemStack);
   }

   @Override
   public BlockBag getInventoryBlockBag() {
      return this.basePlayer.getInventoryBlockBag();
   }

   @Override
   public String getName() {
      return this.basePlayer.getName();
   }

   @Override
   public String getDisplayName() {
      return this.basePlayer.getDisplayName();
   }

   @Override
   public BaseEntity getState() {
      throw new UnsupportedOperationException("Can't getState() on a player");
   }

   @Override
   public Location getLocation() {
      Location loc = this.basePlayer.getLocation();
      return new Location(loc.getExtent(), loc.add(this.offset), loc.getDirection());
   }

   @Override
   public boolean setLocation(Location location) {
      return this.basePlayer.setLocation(location);
   }

   @Override
   public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
      return this.basePlayer.trySetPosition(pos, pitch, yaw);
   }

   @Override
   public World getWorld() {
      return this.world == null ? this.basePlayer.getWorld() : this.world;
   }

   @Override
   public void printRaw(String msg) {
      this.basePlayer.print(TextComponent.of(msg));
   }

   @Override
   public void printDebug(String msg) {
      this.basePlayer.printDebug(TextComponent.of(msg));
   }

   @Override
   public void print(String msg) {
      this.basePlayer.printInfo(TextComponent.of(msg));
   }

   @Override
   public void printError(String msg) {
      this.basePlayer.printError(TextComponent.of(msg));
   }

   @Override
   public void print(Component component) {
      this.basePlayer.print(component);
   }

   @Override
   public String[] getGroups() {
      return this.permActor.getGroups();
   }

   @Override
   public boolean hasPermission(String perm) {
      return this.permActor.hasPermission(perm);
   }

   @Override
   public boolean togglePermission(String permission) {
      return this.permActor.togglePermission(permission);
   }

   @Override
   public void setPermission(String permission, boolean value) {
      this.permActor.setPermission(permission, value);
   }

   @Override
   public void dispatchCUIEvent(CUIEvent event) {
      this.cuiActor.dispatchCUIEvent(event);
   }

   @Override
   public void sendAnnouncements() {
      this.basePlayer.sendAnnouncements();
   }

   @Nullable
   @Override
   public <T> T getFacet(Class<? extends T> cls) {
      return this.basePlayer.getFacet(cls);
   }

   @Override
   public SessionKey getSessionKey() {
      return this.basePlayer.getSessionKey();
   }

   @Override
   public GameMode getGameMode() {
      return this.basePlayer.getGameMode();
   }

   @Override
   public void setGameMode(GameMode gameMode) {
      this.basePlayer.setGameMode(gameMode);
   }

   @Override
   public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, @Nullable B block) {
      this.basePlayer.sendFakeBlock(pos, block);
   }

   @Override
   public void sendFakeOP() {
      this.basePlayer.sendFakeOP();
   }

   @Override
   public void findFreePosition(final Location searchPos) {
      this.basePlayer.findFreePosition(searchPos);
   }

   @Override
   public void setOnGround(final Location searchPos) {
      this.basePlayer.setOnGround(searchPos);
   }

   @Override
   public void findFreePosition() {
      this.basePlayer.findFreePosition();
   }

   @Override
   public boolean ascendLevel() {
      return this.basePlayer.ascendLevel();
   }

   @Override
   public boolean descendLevel() {
      return this.basePlayer.descendLevel();
   }

   @Override
   public boolean ascendToCeiling(final int clearance) {
      return this.basePlayer.ascendToCeiling(clearance);
   }

   @Override
   public boolean ascendToCeiling(final int clearance, final boolean alwaysGlass) {
      return this.basePlayer.ascendToCeiling(clearance, alwaysGlass);
   }

   @Override
   public boolean ascendUpwards(final int distance) {
      return this.basePlayer.ascendUpwards(distance);
   }

   @Override
   public boolean ascendUpwards(final int distance, final boolean alwaysGlass) {
      return this.basePlayer.ascendUpwards(distance, alwaysGlass);
   }

   @Override
   public void floatAt(int x, int y, int z, boolean alwaysGlass) {
      this.basePlayer.floatAt(x, y, z, alwaysGlass);
   }

   @Override
   public Locale getLocale() {
      return this.basePlayer.getLocale();
   }

   public static Player unwrap(Player player) {
      return player instanceof PlayerProxy ? unwrap(((PlayerProxy)player).getBasePlayer()) : player;
   }

   public void setOffset(Vector3 position) {
      this.offset = position;
   }

   @Override
   public BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException {
      return this.basePlayer.getBlockInHand(handSide);
   }

   @Override
   public boolean runAction(Runnable ifFree, boolean checkFree, boolean async) {
      return this.basePlayer.runAction(ifFree, checkFree, async);
   }

   @Override
   public void sendTitle(Component title, Component sub) {
      this.basePlayer.sendTitle(title, sub);
   }

   public Player getBasePlayer() {
      return this.basePlayer;
   }
}
