package com.earth2me.essentials;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.ess3.nms.refl.ReflUtil;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.BanList.Type;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.map.MapView;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class OfflinePlayerStub implements Player {
   protected final transient OfflinePlayer base;
   private final transient Server server;
   private transient Location location = new Location(null, 0.0, 0.0, 0.0, 0.0F, 0.0F);
   private transient World world;
   private boolean allowFlight = false;
   private boolean isFlying = false;
   private String name;

   public OfflinePlayerStub(final UUID uuid, final Server server) {
      this.server = server;
      this.world = (World)server.getWorlds().get(0);
      this.base = server.getOfflinePlayer(uuid);
      this.name = this.base.getName();
   }

   public OfflinePlayerStub(final String name, final Server server) {
      this.server = server;
      this.world = (World)server.getWorlds().get(0);
      this.base = server.getOfflinePlayer(name);
      this.name = name;
   }

   public void sendMessage(final String string) {
   }

   public String getDisplayName() {
      return this.base.getName();
   }

   public void setDisplayName(final String string) {
   }

   public InetSocketAddress getAddress() {
      return null;
   }

   public void kickPlayer(final String string) {
   }

   public PlayerInventory getInventory() {
      return null;
   }

   public ItemStack getItemInHand() {
      return null;
   }

   public void setItemInHand(final ItemStack is) {
   }

   public double getHealth() {
      return 0.0;
   }

   public void setHealth(final double d) {
   }

   public boolean isInsideVehicle() {
      return false;
   }

   public boolean leaveVehicle() {
      return false;
   }

   public Vehicle getVehicle() {
      return null;
   }

   public Location getLocation() {
      return this.location;
   }

   public void setLocation(final Location loc) {
      this.location = loc;
      this.world = loc.getWorld();
   }

   public World getWorld() {
      return this.world;
   }

   public void teleportTo(final Location lctn) {
      this.location = lctn;
      this.world = this.location.getWorld();
   }

   public void teleportTo(final Entity entity) {
      this.teleportTo(entity.getLocation());
   }

   public int getEntityId() {
      return -1;
   }

   public BlockFace getFacing() {
      return null;
   }

   public boolean performCommand(final String string) {
      return false;
   }

   public int getRemainingAir() {
      return 0;
   }

   public void setRemainingAir(final int i) {
   }

   public int getMaximumAir() {
      return 0;
   }

   public void setMaximumAir(final int i) {
   }

   public boolean isSneaking() {
      return false;
   }

   public void setSneaking(final boolean bln) {
   }

   public void updateInventory() {
   }

   public void awardAchievement(final Achievement achievement) {
   }

   public void removeAchievement(final Achievement achievement) {
   }

   public boolean hasAchievement(final Achievement achievement) {
      return false;
   }

   public void chat(final String string) {
   }

   public double getEyeHeight() {
      return 0.0;
   }

   public double getEyeHeight(final boolean bln) {
      return 0.0;
   }

   public List<Block> getLineOfSight(final Set<Material> mat, final int i) {
      return Collections.emptyList();
   }

   public Block getTargetBlock(final Set<Material> mat, final int i) {
      return null;
   }

   public List<Block> getLastTwoTargetBlocks(final Set<Material> mat, final int i) {
      return Collections.emptyList();
   }

   public int getFireTicks() {
      return 0;
   }

   public void setFireTicks(final int i) {
   }

   public int getMaxFireTicks() {
      return 0;
   }

   public void remove() {
   }

   public Server getServer() {
      return this.server;
   }

   public Vector getMomentum() {
      return this.getVelocity();
   }

   public void setMomentum(final Vector vector) {
   }

   public Vector getVelocity() {
      return new Vector(0, 0, 0);
   }

   public void setVelocity(final Vector vector) {
   }

   public double getHeight() {
      return 0.0;
   }

   public double getWidth() {
      return 0.0;
   }

   public void damage(final double d) {
   }

   public void damage(final double d, final Entity entity) {
   }

   public Location getEyeLocation() {
      return null;
   }

   public void sendRawMessage(final String string) {
   }

   public Location getCompassTarget() {
      return null;
   }

   public void setCompassTarget(final Location lctn) {
   }

   public int getMaximumNoDamageTicks() {
      return 0;
   }

   public void setMaximumNoDamageTicks(final int i) {
   }

   public double getLastDamage() {
      return 0.0;
   }

   public void setLastDamage(final double d) {
   }

   public int getNoDamageTicks() {
      return 0;
   }

   public void setNoDamageTicks(final int i) {
   }

   public boolean teleport(final Location lctn) {
      return false;
   }

   public boolean teleport(final Entity entity) {
      return false;
   }

   public Entity getPassenger() {
      return null;
   }

   public boolean setPassenger(final Entity entity) {
      return false;
   }

   public List<Entity> getPassengers() {
      return null;
   }

   public boolean addPassenger(final Entity passenger) {
      return false;
   }

   public boolean removePassenger(final Entity passenger) {
      return false;
   }

   public boolean isEmpty() {
      return true;
   }

   public boolean eject() {
      return false;
   }

   public void saveData() {
   }

   public void loadData() {
   }

   public boolean isSleeping() {
      return false;
   }

   public int getSleepTicks() {
      return 0;
   }

   public List<Entity> getNearbyEntities(final double d, final double d1, final double d2) {
      return Collections.emptyList();
   }

   public boolean isDead() {
      return true;
   }

   public float getFallDistance() {
      return 0.0F;
   }

   public void setFallDistance(final float f) {
   }

   public boolean isSleepingIgnored() {
      return true;
   }

   public void setSleepingIgnored(final boolean bln) {
   }

   public void incrementStatistic(final Statistic ststc) {
   }

   public void decrementStatistic(final Statistic statistic) throws IllegalArgumentException {
   }

   public void incrementStatistic(final Statistic ststc, final int i) {
   }

   public void decrementStatistic(final Statistic statistic, final int i) throws IllegalArgumentException {
   }

   public void setStatistic(final Statistic statistic, final int i) throws IllegalArgumentException {
   }

   public int getStatistic(final Statistic statistic) throws IllegalArgumentException {
      return 0;
   }

   public void incrementStatistic(final Statistic ststc, final Material mtrl) {
   }

   public void decrementStatistic(final Statistic statistic, final Material material) throws IllegalArgumentException {
   }

   public int getStatistic(final Statistic statistic, final Material material) throws IllegalArgumentException {
      return 0;
   }

   public void incrementStatistic(final Statistic ststc, final Material mtrl, final int i) {
   }

   public void decrementStatistic(final Statistic statistic, final Material material, final int i) throws IllegalArgumentException {
   }

   public void setStatistic(final Statistic statistic, final Material material, final int i) throws IllegalArgumentException {
   }

   public void incrementStatistic(final Statistic statistic, final EntityType entityType) throws IllegalArgumentException {
   }

   public void decrementStatistic(final Statistic statistic, final EntityType entityType) throws IllegalArgumentException {
   }

   public int getStatistic(final Statistic statistic, final EntityType entityType) throws IllegalArgumentException {
      return 0;
   }

   public void incrementStatistic(final Statistic statistic, final EntityType entityType, final int i) throws IllegalArgumentException {
   }

   public void decrementStatistic(final Statistic statistic, final EntityType entityType, final int i) {
   }

   public void setStatistic(final Statistic statistic, final EntityType entityType, final int i) {
   }

   public void playNote(final Location lctn, final byte b, final byte b1) {
   }

   public void sendBlockChange(final Location lctn, final Material mtrl, final byte b) {
   }

   public EntityDamageEvent getLastDamageCause() {
      return null;
   }

   public void setLastDamageCause(final EntityDamageEvent ede) {
   }

   public void playEffect(final Location lctn, final Effect effect, final int i) {
   }

   public boolean sendChunkChange(final Location lctn, final int i, final int i1, final int i2, final byte[] bytes) {
      return true;
   }

   public void sendBlockChange(final Location location, final int i, final byte b) {
   }

   public void playNote(final Location lctn, final Instrument i, final Note note) {
   }

   public void setPlayerTime(final long l, final boolean bln) {
   }

   public long getPlayerTime() {
      return 0L;
   }

   public long getPlayerTimeOffset() {
      return 0L;
   }

   public boolean isPlayerTimeRelative() {
      return false;
   }

   public void resetPlayerTime() {
   }

   public boolean isPermissionSet(final String string) {
      return false;
   }

   public boolean isPermissionSet(final Permission prmsn) {
      return false;
   }

   public boolean hasPermission(final String string) {
      return false;
   }

   public boolean hasPermission(final Permission prmsn) {
      return false;
   }

   public PermissionAttachment addAttachment(final Plugin plugin, final String string, final boolean bln) {
      return null;
   }

   public PermissionAttachment addAttachment(final Plugin plugin) {
      return null;
   }

   public PermissionAttachment addAttachment(final Plugin plugin, final String string, final boolean bln, final int i) {
      return null;
   }

   public PermissionAttachment addAttachment(final Plugin plugin, final int i) {
      return null;
   }

   public void removeAttachment(final PermissionAttachment pa) {
   }

   public void recalculatePermissions() {
   }

   public Set<PermissionAttachmentInfo> getEffectivePermissions() {
      return Collections.emptySet();
   }

   public void sendMap(final MapView mv) {
   }

   public GameMode getGameMode() {
      return GameMode.SURVIVAL;
   }

   public void setGameMode(final GameMode gm) {
   }

   public int getLevel() {
      return 0;
   }

   public void setLevel(final int i) {
   }

   public int getTotalExperience() {
      return 0;
   }

   public void setTotalExperience(final int i) {
   }

   public float getExhaustion() {
      return 0.0F;
   }

   public void setExhaustion(final float f) {
   }

   public float getSaturation() {
      return 0.0F;
   }

   public void setSaturation(final float f) {
   }

   public int getFoodLevel() {
      return 0;
   }

   public void setFoodLevel(final int i) {
   }

   public boolean isSprinting() {
      return false;
   }

   public void setSprinting(final boolean bln) {
   }

   public String getPlayerListName() {
      return this.name;
   }

   public void setPlayerListName(final String name) {
   }

   public int getTicksLived() {
      return 0;
   }

   public void setTicksLived(final int i) {
   }

   public double getMaxHealth() {
      return 0.0;
   }

   public void setMaxHealth(final double i) {
   }

   public void giveExp(final int i) {
   }

   public float getExp() {
      return 0.0F;
   }

   public void setExp(final float f) {
   }

   public boolean teleport(final Location lctn, final TeleportCause tc) {
      return false;
   }

   public boolean teleport(final Entity entity, final TeleportCause tc) {
      return false;
   }

   public Player getKiller() {
      return null;
   }

   public void sendPluginMessage(final Plugin plugin, final String string, final byte[] bytes) {
   }

   public Set<String> getListeningPluginChannels() {
      return Collections.emptySet();
   }

   public boolean getAllowFlight() {
      return this.allowFlight;
   }

   public void setAllowFlight(final boolean bln) {
      this.allowFlight = bln;
   }

   public void setBedSpawnLocation(final Location lctn, final boolean force) {
   }

   public void playEffect(final EntityEffect ee) {
   }

   public void hidePlayer(final Player player) {
   }

   public void hidePlayer(final Plugin plugin, final Player player) {
   }

   public void showPlayer(final Player player) {
   }

   public void showPlayer(final Plugin plugin, final Player player) {
   }

   public boolean canSee(final Player player) {
      return false;
   }

   public boolean addPotionEffect(final PotionEffect pe) {
      return false;
   }

   public boolean addPotionEffect(final PotionEffect pe, final boolean bln) {
      return false;
   }

   public boolean addPotionEffects(final Collection<PotionEffect> clctn) {
      return false;
   }

   public boolean hasPotionEffect(final PotionEffectType pet) {
      return false;
   }

   public PotionEffect getPotionEffect(final PotionEffectType type) {
      return null;
   }

   public void removePotionEffect(final PotionEffectType pet) {
   }

   public Collection<PotionEffect> getActivePotionEffects() {
      return Collections.emptyList();
   }

   public <T extends Projectile> T launchProjectile(final Class<? extends T> arg0) {
      return null;
   }

   public EntityType getType() {
      return EntityType.PLAYER;
   }

   public <T> void playEffect(final Location lctn, final Effect effect, final T t) {
   }

   public boolean setWindowProperty(final Property prprt, final int i) {
      return false;
   }

   public InventoryView getOpenInventory() {
      return null;
   }

   public InventoryView openInventory(final Inventory invntr) {
      return null;
   }

   public InventoryView openWorkbench(final Location lctn, final boolean bln) {
      return null;
   }

   public InventoryView openEnchanting(final Location lctn, final boolean bln) {
      return null;
   }

   public void openInventory(final InventoryView iv) {
   }

   public InventoryView openMerchant(final Villager trader, final boolean force) {
      return null;
   }

   public InventoryView openMerchant(final Merchant merchant, final boolean force) {
      return null;
   }

   public void closeInventory() {
   }

   public ItemStack getItemOnCursor() {
      return null;
   }

   public void setItemOnCursor(final ItemStack is) {
   }

   public boolean hasCooldown(final Material material) {
      return false;
   }

   public int getCooldown(final Material material) {
      return 0;
   }

   public void setCooldown(final Material material, final int ticks) {
   }

   public void setMetadata(final String string, final MetadataValue mv) {
   }

   public List<MetadataValue> getMetadata(final String string) {
      return Collections.emptyList();
   }

   public boolean hasMetadata(final String string) {
      return false;
   }

   public void removeMetadata(final String string, final Plugin plugin) {
   }

   public boolean isConversing() {
      return false;
   }

   public void acceptConversationInput(final String string) {
   }

   public boolean beginConversation(final Conversation c) {
      return false;
   }

   public void abandonConversation(final Conversation c) {
   }

   public void sendMessage(final String[] strings) {
   }

   public boolean isBlocking() {
      return false;
   }

   public boolean isHandRaised() {
      return false;
   }

   public void abandonConversation(final Conversation arg0, final ConversationAbandonedEvent arg1) {
   }

   public boolean isFlying() {
      return this.isFlying;
   }

   public void setFlying(final boolean arg0) {
      this.isFlying = arg0;
   }

   public int getExpToLevel() {
      return 0;
   }

   public Entity getShoulderEntityLeft() {
      return null;
   }

   public void setShoulderEntityLeft(final Entity entity) {
   }

   public Entity getShoulderEntityRight() {
      return null;
   }

   public void setShoulderEntityRight(final Entity entity) {
   }

   public boolean hasLineOfSight(final Entity entity) {
      return false;
   }

   public boolean isValid() {
      return false;
   }

   public float getFlySpeed() {
      return 0.1F;
   }

   public void setFlySpeed(final float value) throws IllegalArgumentException {
   }

   public float getWalkSpeed() {
      return 0.2F;
   }

   public void setWalkSpeed(final float value) throws IllegalArgumentException {
   }

   public Inventory getEnderChest() {
      return null;
   }

   public MainHand getMainHand() {
      return null;
   }

   public void playSound(final Location arg0, final Sound arg1, final float arg2, final float arg3) {
   }

   public void giveExpLevels(final int i) {
   }

   public boolean getRemoveWhenFarAway() {
      return false;
   }

   public void setRemoveWhenFarAway(final boolean bln) {
   }

   public EntityEquipment getEquipment() {
      return null;
   }

   public boolean getCanPickupItems() {
      return false;
   }

   public void setCanPickupItems(final boolean bln) {
   }

   public Location getLocation(final Location lctn) {
      return lctn;
   }

   public void setTexturePack(final String string) {
   }

   public void setResourcePack(final String s) {
   }

   public void setResourcePack(final String url, final byte[] hash) {
   }

   public void resetMaxHealth() {
   }

   public String getCustomName() {
      return null;
   }

   public void setCustomName(final String string) {
   }

   public boolean isCustomNameVisible() {
      return false;
   }

   public void setCustomNameVisible(final boolean bln) {
   }

   public boolean isGlowing() {
      return false;
   }

   public void setGlowing(final boolean flag) {
   }

   public boolean isInvulnerable() {
      return false;
   }

   public void setInvulnerable(final boolean flag) {
   }

   public boolean isSilent() {
      return false;
   }

   public void setSilent(final boolean flag) {
   }

   public boolean hasGravity() {
      return false;
   }

   public void setGravity(final boolean gravity) {
   }

   public int getPortalCooldown() {
      return 0;
   }

   public void setPortalCooldown(final int cooldown) {
   }

   public Set<String> getScoreboardTags() {
      return null;
   }

   public boolean addScoreboardTag(final String tag) {
      return false;
   }

   public boolean removeScoreboardTag(final String tag) {
      return false;
   }

   public PistonMoveReaction getPistonMoveReaction() {
      return null;
   }

   public WeatherType getPlayerWeather() {
      return null;
   }

   public void setPlayerWeather(final WeatherType arg0) {
   }

   public void resetPlayerWeather() {
   }

   public boolean isOnGround() {
      return false;
   }

   public Scoreboard getScoreboard() {
      return null;
   }

   public void setScoreboard(final Scoreboard scrbrd) throws IllegalArgumentException, IllegalStateException {
   }

   public void playSound(final Location arg0, final String arg1, final float arg2, final float arg3) {
   }

   public void playSound(final Location location, final Sound sound, final SoundCategory category, final float volume, final float pitch) {
   }

   public void playSound(final Location location, final String sound, final SoundCategory category, final float volume, final float pitch) {
   }

   public void stopSound(final Sound sound) {
   }

   public void stopSound(final String sound) {
   }

   public void stopSound(final Sound sound, final SoundCategory category) {
   }

   public void stopSound(final String sound, final SoundCategory category) {
   }

   public boolean isHealthScaled() {
      return false;
   }

   public void setHealthScaled(final boolean arg0) {
   }

   public double getHealthScale() {
      return 0.0;
   }

   public void setHealthScale(final double arg0) throws IllegalArgumentException {
   }

   public boolean isLeashed() {
      return false;
   }

   public Entity getLeashHolder() throws IllegalStateException {
      return null;
   }

   public boolean setLeashHolder(final Entity arg0) {
      return false;
   }

   public boolean isGliding() {
      return false;
   }

   public void setGliding(final boolean gliding) {
   }

   public void setAI(final boolean ai) {
   }

   public boolean hasAI() {
      return false;
   }

   public boolean isCollidable() {
      return false;
   }

   public void setCollidable(final boolean collidable) {
   }

   public <T extends Projectile> T launchProjectile(final Class<? extends T> type, final Vector vector) {
      return null;
   }

   public void sendSignChange(final Location arg0, final String[] arg1) throws IllegalArgumentException {
   }

   public Location getBedSpawnLocation() {
      return null;
   }

   public void setBedSpawnLocation(final Location lctn) {
   }

   public String getName() {
      return this.name;
   }

   public void setName(final String name) {
      this.name = this.base.getName();
      if (this.name == null) {
         this.name = name;
      }
   }

   public UUID getUniqueId() {
      return this.base.getUniqueId();
   }

   public boolean isOp() {
      return this.base.isOp();
   }

   public void setOp(final boolean value) {
      this.base.setOp(value);
   }

   public boolean isOnline() {
      return this.base.isOnline();
   }

   public boolean isBanned() {
      return this.base.getName() == null && this.getName() != null ? this.server.getBanList(Type.NAME).isBanned(this.getName()) : this.base.isBanned();
   }

   @Deprecated
   public void setBanned(final boolean banned) {
      if (ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_12_R1)) {
         throw new UnsupportedOperationException("Cannot call setBanned on MC 1.12 and higher.");
      } else {
         if (this.base.getName() == null && this.getName() != null) {
            if (banned) {
               this.server.getBanList(Type.NAME).addBan(this.getName(), null, null, null);
            } else {
               this.server.getBanList(Type.NAME).pardon(this.getName());
            }
         }

         try {
            Method method = this.base.getClass().getDeclaredMethod("setBanned", boolean.class);
            method.invoke(this.base, banned);
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var3) {
            var3.printStackTrace();
         }
      }
   }

   public boolean isWhitelisted() {
      return this.base.isWhitelisted();
   }

   public void setWhitelisted(final boolean value) {
      this.base.setWhitelisted(value);
   }

   public Player getPlayer() {
      return this.base.getPlayer();
   }

   public long getFirstPlayed() {
      return this.base.getFirstPlayed();
   }

   public long getLastPlayed() {
      return this.base.getLastPlayed();
   }

   public boolean hasPlayedBefore() {
      return this.base.hasPlayedBefore();
   }

   public Map<String, Object> serialize() {
      return this.base.serialize();
   }

   public Entity getSpectatorTarget() {
      return null;
   }

   public void setSpectatorTarget(final Entity entity) {
   }

   public void resetTitle() {
   }

   public void spawnParticle(final Particle particle, final Location location, final int count) {
   }

   public void spawnParticle(final Particle particle, final double x, final double y, final double z, final int count) {
   }

   public <T> void spawnParticle(final Particle particle, final Location location, final int count, final T data) {
   }

   public <T> void spawnParticle(final Particle particle, final double x, final double y, final double z, final int count, final T data) {
   }

   public void spawnParticle(
      final Particle particle, final Location location, final int count, final double offsetX, final double offsetY, final double offsetZ
   ) {
   }

   public void spawnParticle(
      final Particle particle,
      final double x,
      final double y,
      final double z,
      final int count,
      final double offsetX,
      final double offsetY,
      final double offsetZ
   ) {
   }

   public <T> void spawnParticle(
      final Particle particle, final Location location, final int count, final double offsetX, final double offsetY, final double offsetZ, final T data
   ) {
   }

   public <T> void spawnParticle(
      final Particle particle,
      final double x,
      final double y,
      final double z,
      final int count,
      final double offsetX,
      final double offsetY,
      final double offsetZ,
      final T data
   ) {
   }

   public void spawnParticle(
      final Particle particle, final Location location, final int count, final double offsetX, final double offsetY, final double offsetZ, final double extra
   ) {
   }

   public void spawnParticle(
      final Particle particle,
      final double x,
      final double y,
      final double z,
      final int count,
      final double offsetX,
      final double offsetY,
      final double offsetZ,
      final double extra
   ) {
   }

   public <T> void spawnParticle(
      final Particle particle,
      final Location location,
      final int count,
      final double offsetX,
      final double offsetY,
      final double offsetZ,
      final double extra,
      final T data
   ) {
   }

   public <T> void spawnParticle(
      final Particle particle,
      final double x,
      final double y,
      final double z,
      final int count,
      final double offsetX,
      final double offsetY,
      final double offsetZ,
      final double extra,
      final T data
   ) {
   }

   public AdvancementProgress getAdvancementProgress(final Advancement advancement) {
      return null;
   }

   public String getLocale() {
      return null;
   }

   public void sendTitle(final String title, final String subtitle) {
   }

   public void sendTitle(final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
   }

   public AttributeInstance getAttribute(final Attribute attribute) {
      return null;
   }
}
