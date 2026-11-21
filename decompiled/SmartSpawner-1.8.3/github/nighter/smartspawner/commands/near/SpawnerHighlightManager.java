package github.nighter.smartspawner.commands.near;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpawnerHighlightManager implements Listener {
   public static final int MAX_RADIUS = 10000;
   private static final int MAX_HIGHLIGHTS = 10000;
   private static final long HIGHLIGHT_DURATION_TICKS = 600L;
   private static final long BOSSBAR_RESULT_TICKS = 100L;
   private static final String BOSSBAR_ANALYZING_FALLBACK = "Scanning nearby spawners... {percent}%";
   private static final String BOSSBAR_FOUND_FALLBACK = "Found {count} spawner(s) within {radius} blocks.";
   private static final String BOSSBAR_NOT_FOUND_FALLBACK = "No spawners found within {radius} blocks.";
   private static final String VIEW_GUI_BUTTON_FALLBACK = " [View in GUI]";
   private final SmartSpawner plugin;
   private final Map<UUID, SpawnerHighlightManager.ScanSession> activeSessions = new ConcurrentHashMap<>();

   public SpawnerHighlightManager(SmartSpawner plugin) {
      this.plugin = plugin;
   }

   public void startScan(Player player, int radius) {
      UUID uuid = player.getUniqueId();
      SpawnerHighlightManager.ScanSession existing = this.activeSessions.remove(uuid);
      if (existing != null) {
         existing.cancelled.set(true);
         this.cleanupSession(existing, player);
      }

      Location playerLoc = player.getLocation().clone();
      String worldName = playerLoc.getWorld().getName();
      double radiusSq = (double)radius * radius;
      BossBar bossBar = BossBar.bossBar(
         Component.text(
            this.plugin.getLanguageManager().getCommandConfig("near.bossbar.analyzing", "Scanning nearby spawners... {percent}%", Map.of("percent", "0")),
            NamedTextColor.AQUA
         ),
         0.0F,
         Color.BLUE,
         Overlay.PROGRESS
      );
      SpawnerHighlightManager.ScanSession session = new SpawnerHighlightManager.ScanSession(uuid, bossBar);
      this.activeSessions.put(uuid, session);
      player.showBossBar(bossBar);
      player.updateCommands();
      this.plugin.getMessageService().sendMessage(player, "near.scan_start", Map.of("radius", String.valueOf(radius)));
      Scheduler.runTaskAsync(
         () -> {
            if (!session.cancelled.get()) {
               Set<SpawnerData> worldSpawners = this.plugin.getSpawnerManager().getSpawnersInWorld(worldName);
               if (worldSpawners != null && !worldSpawners.isEmpty()) {
                  List<SpawnerData> snapshot = new ArrayList<>(worldSpawners);
                  int total = snapshot.size();
                  List<SpawnerData> nearby = new ArrayList<>();

                  for (int i = 0; i < total; i++) {
                     if (session.cancelled.get()) {
                        return;
                     }

                     SpawnerData spawner = snapshot.get(i);
                     Location loc = spawner.getSpawnerLocation();
                     if (loc != null && loc.getWorld() != null) {
                        double dx = loc.getX() - playerLoc.getX();
                        double dy = loc.getY() - playerLoc.getY();
                        double dz = loc.getZ() - playerLoc.getZ();
                        if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                           nearby.add(spawner);
                           if (nearby.size() >= 10000) {
                              break;
                           }
                        }

                        if (i % 50 == 0 || i == total - 1) {
                           float progress = (float)(i + 1) / total;
                           int pct = (int)(progress * 100.0F);
                           bossBar.name(
                              Component.text(
                                 this.plugin
                                    .getLanguageManager()
                                    .getCommandConfig(
                                       "near.bossbar.analyzing", "Scanning nearby spawners... {percent}%", Map.of("percent", String.valueOf(pct))
                                    ),
                                 NamedTextColor.AQUA
                              )
                           );
                           bossBar.progress(progress);
                        }
                     }
                  }

                  if (!session.cancelled.get()) {
                     Scheduler.runTask(() -> {
                        if (!session.cancelled.get()) {
                           this.finalizeScan(player, session, nearby, radius);
                        }
                     });
                  }
               } else {
                  Scheduler.runTask(() -> {
                     if (!session.cancelled.get()) {
                        this.finalizeScan(player, session, Collections.emptyList(), radius);
                     }
                  });
               }
            }
         }
      );
   }

   public void cancelScan(Player player) {
      SpawnerHighlightManager.ScanSession session = this.activeSessions.remove(player.getUniqueId());
      if (session == null) {
         this.plugin.getMessageService().sendMessage(player, "near.no_active_scan");
      } else {
         session.cancelled.set(true);
         this.cleanupSession(session, player);
         player.updateCommands();
         this.plugin.getMessageService().sendMessage(player, "near.scan_cancelled");
      }
   }

   public boolean hasActiveSession(UUID uuid) {
      return this.activeSessions.containsKey(uuid);
   }

   public List<SpawnerData> getSessionSpawners(UUID uuid) {
      SpawnerHighlightManager.ScanSession session = this.activeSessions.get(uuid);
      return session != null ? Collections.unmodifiableList(session.scannedSpawners) : Collections.emptyList();
   }

   private void finalizeScan(Player player, SpawnerHighlightManager.ScanSession session, List<SpawnerData> spawners, int radius) {
      if (!player.isOnline()) {
         this.cleanupSession(session, null);
      } else {
         int count = spawners.size();
         session.scannedSpawners = new ArrayList<>(spawners);
         if (count == 0) {
            session.bossBar
               .name(
                  Component.text(
                     this.plugin
                        .getLanguageManager()
                        .getCommandConfig("near.bossbar.not_found", "No spawners found within {radius} blocks.", Map.of("radius", String.valueOf(radius))),
                     NamedTextColor.RED
                  )
               );
            session.bossBar.color(Color.RED);
         } else {
            session.bossBar
               .name(
                  Component.text(
                     this.plugin
                        .getLanguageManager()
                        .getCommandConfig(
                           "near.bossbar.found",
                           "Found {count} spawner(s) within {radius} blocks.",
                           Map.of("count", String.valueOf(count), "radius", String.valueOf(radius))
                        ),
                     NamedTextColor.GREEN
                  )
               );
            session.bossBar.color(Color.GREEN);
         }

         session.bossBar.progress(1.0F);

         for (SpawnerData spawner : spawners) {
            if (session.cancelled.get()) {
               return;
            }

            Location loc = spawner.getSpawnerLocation();
            if (loc != null) {
               this.spawnHighlight(player, session, loc);
            }
         }

         Map<String, String> resultPlaceholders = Map.of("count", String.valueOf(count), "radius", String.valueOf(radius));
         if (count > 0) {
            String foundMsg = this.plugin.getLanguageManager().getMessage("near.scan_found", resultPlaceholders);
            Component textPart = foundMsg != null && !foundMsg.startsWith("Missing")
               ? LegacyComponentSerializer.legacySection().deserialize(foundMsg)
               : Component.empty();
            Component viewGuiHint = ((Builder)Component.text()
                  .append(
                     ((TextComponent)Component.text(
                              this.plugin.getLanguageManager().getCommandConfig("near.view_gui.button", " [View in GUI]"), NamedTextColor.GOLD
                           )
                           .clickEvent(ClickEvent.callback(audience -> {
                              if (audience instanceof Player clickPlayer) {
                                 this.plugin.getNearResultGUI().openNearResultGUI(clickPlayer, 1);
                              }
                           })))
                        .hoverEvent(
                           HoverEvent.showText(
                              Component.text(
                                 this.plugin
                                    .getLanguageManager()
                                    .getCommandConfig(
                                       "near.view_gui.hover", "Click to browse the {count} spawner(s) found nearby", Map.of("count", String.valueOf(count))
                                    ),
                                 NamedTextColor.GRAY
                              )
                           )
                        )
                  ))
               .build();
            player.sendMessage(((Builder)((Builder)Component.text().append(textPart)).append(viewGuiHint)).build());
            String soundKey = this.plugin.getLanguageManager().getSound("near.scan_found");
            if (soundKey != null) {
               try {
                  player.playSound(player.getLocation(), soundKey, 1.0F, 1.0F);
               } catch (Exception var12) {
               }
            }
         } else {
            this.plugin.getMessageService().sendMessage(player, "near.scan_none", resultPlaceholders);
         }

         Scheduler.runTaskLater(() -> {
            Player p = this.plugin.getServer().getPlayer(session.playerUUID);
            if (p != null && p.isOnline()) {
               Scheduler.runEntityTask(p, () -> p.hideBossBar(session.bossBar));
            }
         }, 100L);
         session.expiryTask = Scheduler.runTaskLater(() -> {
            SpawnerHighlightManager.ScanSession current = this.activeSessions.get(session.playerUUID);
            if (current == session) {
               this.activeSessions.remove(session.playerUUID);
               session.cancelled.set(true);
               Player p = this.plugin.getServer().getPlayer(session.playerUUID);
               this.cleanupSession(session, p);
               if (p != null && p.isOnline()) {
                  p.updateCommands();
                  this.plugin.getMessageService().sendMessage(p, "near.highlights_expired");
               }
            }
         }, 600L);
      }
   }

   private void spawnHighlight(Player player, SpawnerHighlightManager.ScanSession session, Location loc) {
      Scheduler.runLocationTask(loc, () -> {
         if (!session.cancelled.get() && player.isOnline()) {
            World world = loc.getWorld();
            if (world != null) {
               Location spawnLoc = loc.getBlock().getLocation();
               BlockDisplay display = (BlockDisplay)world.spawn(spawnLoc, BlockDisplay.class, bd -> {
                  bd.setBlock(Material.SPAWNER.createBlockData());
                  bd.setGlowing(true);
                  bd.setVisibleByDefault(false);
                  bd.setPersistent(false);
               });
               session.highlights.add(display);
               Scheduler.runEntityTask(player, () -> {
                  if (player.isOnline()) {
                     player.showEntity(this.plugin, display);
                  }
               });
            }
         }
      });
   }

   private void cleanupSession(SpawnerHighlightManager.ScanSession session, Player player) {
      if (player != null && player.isOnline()) {
         Scheduler.runEntityTask(player, () -> player.hideBossBar(session.bossBar));
      }

      if (session.expiryTask != null) {
         session.expiryTask.cancel();
         session.expiryTask = null;
      }

      List<BlockDisplay> copy = new ArrayList<>(session.highlights);
      session.highlights.clear();

      for (BlockDisplay bd : copy) {
         if (bd.isValid()) {
            Scheduler.runEntityTask(bd, () -> {
               if (bd.isValid()) {
                  bd.remove();
               }
            });
         }
      }
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      SpawnerHighlightManager.ScanSession session = this.activeSessions.remove(event.getPlayer().getUniqueId());
      if (session != null) {
         session.cancelled.set(true);
         this.cleanupSession(session, null);
      }
   }

   public void cleanup() {
      for (SpawnerHighlightManager.ScanSession session : this.activeSessions.values()) {
         session.cancelled.set(true);
         if (session.expiryTask != null) {
            session.expiryTask.cancel();
         }

         for (BlockDisplay bd : session.highlights) {
            if (bd.isValid()) {
               bd.remove();
            }
         }

         session.highlights.clear();
      }

      this.activeSessions.clear();
   }

   private static final class ScanSession {
      final UUID playerUUID;
      final BossBar bossBar;
      final CopyOnWriteArrayList<BlockDisplay> highlights = new CopyOnWriteArrayList<>();
      final AtomicBoolean cancelled = new AtomicBoolean(false);
      volatile Scheduler.Task expiryTask;
      volatile List<SpawnerData> scannedSpawners = Collections.emptyList();

      ScanSession(UUID playerUUID, BossBar bossBar) {
         this.playerUUID = playerUUID;
         this.bossBar = bossBar;
      }
   }
}
