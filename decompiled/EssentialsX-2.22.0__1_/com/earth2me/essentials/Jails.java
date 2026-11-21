package com.earth2me.essentials;

import com.earth2me.essentials.config.ConfigurateUtil;
import com.earth2me.essentials.config.EssentialsConfiguration;
import com.earth2me.essentials.config.entities.LazyLocation;
import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.ess3.api.IJails;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.PluginManager;

public class Jails implements IJails {
   private static boolean enabled = false;
   private final net.ess3.api.IEssentials ess;
   private final EssentialsConfiguration config;
   private final Map<String, LazyLocation> jails = new HashMap<>();

   public Jails(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
      this.config = new EssentialsConfiguration(new File(ess.getDataFolder(), "jail.yml"));
      this.reloadConfig();
   }

   @Override
   public void reloadConfig() {
      synchronized (this.jails) {
         this.config.load();
         this.jails.clear();
         CommentedConfigurationNode jailsNode = this.config.getSection("jails");

         for (Entry<String, CommentedConfigurationNode> entry : ConfigurateUtil.getMap(jailsNode).entrySet()) {
            CommentedConfigurationNode jailNode = entry.getValue();
            String worldId = jailNode.node(new Object[]{"world"}).getString();
            if (worldId != null && !worldId.isEmpty()) {
               this.jails
                  .put(
                     entry.getKey().toLowerCase(Locale.ENGLISH),
                     new LazyLocation(
                        worldId,
                        jailNode.node(new Object[]{"world-name"}).getString(""),
                        jailNode.node(new Object[]{"x"}).getDouble(),
                        jailNode.node(new Object[]{"y"}).getDouble(),
                        jailNode.node(new Object[]{"z"}).getDouble(),
                        jailNode.node(new Object[]{"yaw"}).getFloat(),
                        jailNode.node(new Object[]{"pitch"}).getFloat()
                     )
                  );
            }
         }

         this.checkRegister();
      }
   }

   private void registerListeners() {
      enabled = true;
      PluginManager pluginManager = this.ess.getServer().getPluginManager();
      Jails.JailListener blockListener = new Jails.JailListener();
      pluginManager.registerEvents(blockListener, this.ess);
      if (this.ess.getSettings().isDebug()) {
         this.ess.getLogger().log(Level.INFO, "Registering Jail listener");
      }
   }

   public void resetListener() {
      enabled = false;
      this.checkRegister();
   }

   private void checkRegister() {
      if (!enabled && this.getCount() > 0) {
         this.registerListeners();
      }
   }

   @Override
   public void startTransaction() {
      this.config.startTransaction();
   }

   @Override
   public void stopTransaction(final boolean blocking) {
      this.config.stopTransaction(blocking);
   }

   @Override
   public Location getJail(String jailName) throws Exception {
      if (jailName == null) {
         throw new TranslatableException("jailNotExist");
      } else {
         jailName = jailName.toLowerCase(Locale.ENGLISH);
         synchronized (this.jails) {
            if (!this.jails.containsKey(jailName)) {
               throw new TranslatableException("jailNotExist");
            } else {
               Location location = this.jails.get(jailName).location();
               if (location == null) {
                  throw new TranslatableException("jailWorldNotExist");
               } else {
                  return location;
               }
            }
         }
      }
   }

   @Override
   public Collection<String> getList() throws Exception {
      synchronized (this.jails) {
         return new ArrayList<>(this.jails.keySet());
      }
   }

   @Override
   public void removeJail(String jail) throws Exception {
      if (jail != null) {
         jail = jail.toLowerCase(Locale.ENGLISH);
         synchronized (this.jails) {
            if (this.jails.remove(jail) != null) {
               this.config.getSection("jails").node(new Object[]{jail}).set(null);
               this.config.save();
            }
         }
      }
   }

   @Override
   public void sendToJail(final net.ess3.api.IUser user, final String jailName, final CompletableFuture<Boolean> future) throws Exception {
      if (jailName != null && !jailName.isEmpty()) {
         String jail = jailName.toLowerCase(Locale.ENGLISH);
         synchronized (this.jails) {
            if (this.jails.containsKey(jail)) {
               if (user.getBase().isOnline()) {
                  user.getAsyncTeleport().now(this.getJail(jail), false, TeleportCause.COMMAND, future);
                  future.thenAccept(success -> user.setJail(jail));
                  return;
               }

               user.setJail(jail);
            }
         }
      }
   }

   @Override
   public void setJail(String jailName, final Location loc) throws Exception {
      jailName = jailName.toLowerCase(Locale.ENGLISH);
      synchronized (this.jails) {
         this.jails.put(jailName, LazyLocation.fromLocation(loc));
         this.config.setProperty("jails." + jailName, loc);
         this.config.save();
      }
   }

   @Override
   public int getCount() {
      try {
         return this.getList().size();
      } catch (Exception var2) {
         return 0;
      }
   }

   private class JailListener implements Listener {
      private JailListener() {
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailBlockBreak(final BlockBreakEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && !user.isAuthorized("essentials.jail.allow-break")) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailBlockPlace(final BlockPlaceEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && !user.isAuthorized("essentials.jail.allow-place")) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailBlockDamage(final BlockDamageEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && !user.isAuthorized("essentials.jail.allow-block-damage")) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailEntityDamageByEntity(final EntityDamageByEntityEvent event) {
         if (event.getCause() == DamageCause.ENTITY_ATTACK && event.getEntity().getType() == EntityType.PLAYER) {
            Entity damager = event.getDamager();
            if (damager.getType() == EntityType.PLAYER) {
               if (this.shouldIgnore((Player)damager)) {
                  return;
               }

               User user = Jails.this.ess.getUser((Player)damager);
               if (user != null && user.isJailed()) {
                  event.setCancelled(true);
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailPlayerInteract(final PlayerInteractEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && !user.isAuthorized("essentials.jail.allow-interact")) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.LOW,
         ignoreCancelled = true
      )
      public void onJailPlayerGameModeChange(final PlayerGameModeChangeEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed()) {
               event.setCancelled(true);
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onJailPlayerRespawn(final PlayerRespawnEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
               try {
                  event.setRespawnLocation(Jails.this.getJail(user.getJail()));
               } catch (Exception var4) {
                  if (Jails.this.ess.getSettings().isDebug()) {
                     Jails.this.ess
                        .getLogger()
                        .log(
                           Level.INFO,
                           Jails.this.ess
                              .getAdventureFacet()
                              .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), var4.getLocalizedMessage())),
                           (Throwable)var4
                        );
                  } else {
                     Jails.this.ess
                        .getLogger()
                        .log(
                           Level.INFO,
                           Jails.this.ess
                              .getAdventureFacet()
                              .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), var4.getLocalizedMessage()))
                        );
                  }
               }
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGH
      )
      public void onJailPlayerTeleport(final PlayerTeleportEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
               try {
                  event.setTo(Jails.this.getJail(user.getJail()));
               } catch (Exception var4) {
                  if (Jails.this.ess.getSettings().isDebug()) {
                     Jails.this.ess
                        .getLogger()
                        .log(
                           Level.INFO,
                           Jails.this.ess
                              .getAdventureFacet()
                              .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), var4.getLocalizedMessage())),
                           (Throwable)var4
                        );
                  } else {
                     Jails.this.ess
                        .getLogger()
                        .log(
                           Level.INFO,
                           Jails.this.ess
                              .getAdventureFacet()
                              .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), var4.getLocalizedMessage()))
                        );
                  }
               }

               user.sendTl("jailMessage");
            }
         }
      }

      @EventHandler(
         priority = EventPriority.HIGHEST
      )
      public void onJailPlayerJoin(final PlayerJoinEvent event) {
         if (!this.shouldIgnore(event.getPlayer())) {
            User user = Jails.this.ess.getUser(event.getPlayer());
            long currentTime = System.currentTimeMillis();
            user.checkJailTimeout(currentTime);
            if (user.isJailed() && user.getJail() != null && !user.getJail().isEmpty()) {
               CompletableFuture<Boolean> future = new CompletableFuture<>();
               future.exceptionally(
                  ex -> {
                     if (Jails.this.ess.getSettings().isDebug()) {
                        Jails.this.ess
                           .getLogger()
                           .log(
                              Level.INFO,
                              Jails.this.ess
                                 .getAdventureFacet()
                                 .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage())),
                              ex
                           );
                     } else {
                        Jails.this.ess
                           .getLogger()
                           .log(
                              Level.INFO,
                              Jails.this.ess
                                 .getAdventureFacet()
                                 .miniToLegacy(I18n.tlLiteral("returnPlayerToJailError", user.getName(), ex.getLocalizedMessage()))
                           );
                     }

                     return false;
                  }
               );
               future.thenAccept(success -> user.sendTl("jailMessage"));

               try {
                  Jails.this.sendToJail(user, user.getJail(), future);
               } catch (Exception var7) {
                  future.completeExceptionally(var7);
               }
            }
         }
      }

      private boolean shouldIgnore(final Player base) {
         return base.getUniqueId().version() == 2 || base.hasMetadata("NPC");
      }
   }
}
