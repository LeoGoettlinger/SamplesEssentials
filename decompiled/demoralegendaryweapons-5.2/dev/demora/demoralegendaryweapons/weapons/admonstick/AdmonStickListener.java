package dev.demora.demoralegendaryweapons.weapons.admonstick;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AdmonStickListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final AdmonStick admonStick;
   private final DragonRideManager dragonRideManager;

   public AdmonStickListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.admonStick = new AdmonStick(plugin);
      this.dragonRideManager = new DragonRideManager(plugin);
   }

   public AdmonStick getAdmonStick() {
      return this.admonStick;
   }

   @EventHandler
   public void onChat(AsyncChatEvent event) {
      String message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
      if (message.equalsIgnoreCase("admr")) {
         event.setCancelled(true);
         Player player = event.getPlayer();
         Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (player.isOp()) {
               player.getInventory().addItem(new ItemStack[]{this.admonStick.createAdmonStick()});
            }
         });
      }
   }

   @EventHandler
   public void onPlayerInteract(PlayerInteractEvent event) {
      if (event.getHand() == EquipmentSlot.HAND) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.admonStick.isAdmonStick(item)) {
            Action action = event.getAction();
            if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
               if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                  event.setCancelled(true);
                  int index = this.admonStick.getAbilityIndex(item);
                  this.useAbility(player, index);
               }
            } else {
               event.setCancelled(true);
               int index = this.admonStick.cycleAbility(item);
               player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize("Прототип: " + this.admonStick.getAbilityName(index)));
               player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.4F);
            }
         }
      }
   }

   private void useAbility(Player player, int index) {
      switch (index) {
         case 0:
            this.castFireCircle(player);
            break;
         case 1:
            this.dragonRideManager.summon(player);
      }
   }

   @EventHandler
   public void onDismount(EntityDismountEvent event) {
      this.dragonRideManager.handleDismount(event.getEntity(), event.getDismounted());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.dragonRideManager.handleQuit(event.getPlayer());
   }

   private void castFireCircle(Player player) {
      Location center = player.getLocation().clone();
      final World world = center.getWorld();
      if (world != null) {
         final int pillarCount = 8;
         double radius = 7.0;
         final int pillarDelayTicks = 10;
         final int totalTicks = 200;
         final double maxHeight = 6.0;
         final int riseTicks = 12;
         final List<Location> bases = new ArrayList<>();

         for (int i = 0; i < pillarCount; i++) {
            double angle = (Math.PI * 2) / pillarCount * i;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            bases.add(new Location(world, x, center.getY(), z));
         }

         world.playSound(center, Sound.ITEM_FIRECHARGE_USE, 1.2F, 0.7F);
         (new BukkitRunnable() {
            private int tick = 0;
            private final boolean[] erupted = new boolean[pillarCount];

            public void run() {
               if (this.tick >= totalTicks) {
                  for (Location base : bases) {
                     world.spawnParticle(Particle.EXPLOSION, base.clone().add(0.0, 1.0, 0.0), 4, 0.4, 0.6, 0.4, 0.0);
                     world.spawnParticle(Particle.FLAME, base.clone().add(0.0, maxHeight / 2.0, 0.0), 80, 0.4, maxHeight / 2.0, 0.4, 0.12);
                     world.spawnParticle(Particle.LAVA, base.clone().add(0.0, 1.0, 0.0), 25, 0.3, 0.6, 0.3, 0.0);
                     world.spawnParticle(Particle.LARGE_SMOKE, base.clone().add(0.0, 1.5, 0.0), 30, 0.3, 0.8, 0.3, 0.05);
                     world.playSound(base, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.2F);
                  }

                  this.cancel();
               } else {
                  for (int i = 0; i < pillarCount; i++) {
                     int start = i * pillarDelayTicks;
                     if (this.tick >= start) {
                        Location base = bases.get(i);
                        if (!this.erupted[i]) {
                           this.erupted[i] = true;
                           world.playSound(base, Sound.ENTITY_BLAZE_SHOOT, 0.9F, 0.8F);
                        }

                        int local = this.tick - start;
                        double progress = Math.min(1.0, (double)local / riseTicks);
                        double currentHeight = maxHeight * progress;

                        for (double y = 0.0; y <= currentHeight; y += 0.4) {
                           Location point = base.clone().add(0.0, y, 0.0);
                           world.spawnParticle(Particle.FLAME, point, 1, 0.12, 0.06, 0.12, 0.01);
                           if (y % 1.2 < 0.4) {
                              world.spawnParticle(Particle.SMALL_FLAME, point, 1, 0.15, 0.05, 0.15, 0.0);
                           }
                        }

                        Location head = base.clone().add(0.0, currentHeight, 0.0);
                        world.spawnParticle(Particle.LAVA, head, 1, 0.1, 0.1, 0.1, 0.0);
                        world.spawnParticle(Particle.SMALL_FLAME, head, 4, 0.15, 0.1, 0.15, 0.02);
                     }
                  }

                  this.tick++;
               }
            }
         }).runTaskTimer(this.plugin, 0L, 1L);
      }
   }
}
