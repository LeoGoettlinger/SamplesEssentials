package dev.demora.demoralegendaryweapons.weapons.chainsawsword;

import dev.demora.demoralegendaryweapons.AttributeCompat;
import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class ChainsawSwordListener implements Listener {
   private final DemoraLegendaryWeapons plugin;
   private final ChainsawSword chainsawSword;
   private final Map<UUID, Long> cooldowns = new HashMap<>();
   private final Set<UUID> bleeding = new HashSet<>();

   public ChainsawSwordListener(DemoraLegendaryWeapons plugin, ChainsawSword chainsawSword) {
      this.plugin = plugin;
      this.chainsawSword = chainsawSword;
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      Action action = event.getAction();
      if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.chainsawSword.isChainsawSword(item)) {
            if (this.chainsawSword.hasCurse(item)) {
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.chainsaw-curse-ready"));
            } else if (this.isOnCooldown(player)) {
               long remaining = this.getRemainingCooldown(player);
               player.sendMessage(this.plugin.getMessageWithPrefix("weapon-messages.chainsaw-cooldown").replace("{time}", String.valueOf(remaining)));
            } else {
               this.chainsawSword.setCurse(item, true);
               this.cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + this.getCooldownMillis());
               player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 0.6F);
            }
         }
      }
   }

   @EventHandler
   public void onDamage(EntityDamageByEntityEvent event) {
      if (event.getDamager() instanceof Player) {
         Player player = (Player)event.getDamager();
         ItemStack item = player.getInventory().getItemInMainHand();
         if (this.chainsawSword.isChainsawSword(item)) {
            if (event.getEntity() instanceof LivingEntity living) {
               double max = AttributeCompat.getMaxHealth() != null && living.getAttribute(AttributeCompat.getMaxHealth()) != null
                  ? living.getAttribute(AttributeCompat.getMaxHealth()).getValue()
                  : living.getHealth();
               if (max > 0.0 && living.getHealth() / max < 0.2) {
                  event.setDamage(event.getDamage() + 1.5);
               }
            }

            if (this.chainsawSword.hasCurse(item)) {
               this.chainsawSword.setCurse(item, false);
               event.setDamage(event.getDamage() * 1.5);
               if (event.getEntity() instanceof LivingEntity livingx) {
                  this.applyBleed(livingx, player);
               }

               player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VEX_AMBIENT, 1.0F, 0.8F);
            }
         }
      }
   }

   @EventHandler
   public void onPlayerDeath(PlayerDeathEvent event) {
      Player victim = event.getEntity();
      Player killer = victim.getKiller();
      if (killer != null) {
         ItemStack item = killer.getInventory().getItemInMainHand();
         if (this.chainsawSword.isChainsawSword(item)) {
            killer.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1, true, true));
         }
      }
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.cooldowns.remove(event.getPlayer().getUniqueId());
   }

   private boolean isOnCooldown(Player player) {
      Long end = this.cooldowns.get(player.getUniqueId());
      if (end == null) {
         return false;
      } else if (System.currentTimeMillis() >= end) {
         this.cooldowns.remove(player.getUniqueId());
         return false;
      } else {
         return true;
      }
   }

   public boolean isOnCurseCooldown(Player player) {
      return this.isOnCooldown(player);
   }

   private long getRemainingCooldown(Player player) {
      Long end = this.cooldowns.get(player.getUniqueId());
      if (end == null) {
         return 0L;
      } else {
         long diff = end - System.currentTimeMillis();
         if (diff <= 0L) {
            this.cooldowns.remove(player.getUniqueId());
            return 0L;
         } else {
            return (long)Math.ceil(diff / 1000.0);
         }
      }
   }

   public long getRemainingCurseCooldown(Player player) {
      return this.getRemainingCooldown(player);
   }

   private void applyBleed(LivingEntity target, Player source) {
      final UUID id = target.getUniqueId();
      if (!this.bleeding.contains(id)) {
         this.bleeding.add(id);
         final int duration = this.getBleedDurationTicks();
         if (duration <= 0) {
            this.bleeding.remove(id);
         } else {
            final double damage = this.getBleedDamage();
            (new BukkitRunnable() {
                  private int ticks = 0;

                  public void run() {
                     if (target.isValid() && !target.isDead()) {
                        target.getWorld()
                           .spawnParticle(
                              Particle.DUST, target.getLocation().add(0.0, 1.0, 0.0), 20, 0.4, 0.6, 0.4, 0.0, new DustOptions(Color.fromRGB(255, 0, 0), 1.2F)
                           );
                        target.damage(damage, source);
                        this.ticks += 20;
                        if (this.ticks >= duration) {
                           ChainsawSwordListener.this.bleeding.remove(id);
                           this.cancel();
                        }
                     } else {
                        ChainsawSwordListener.this.bleeding.remove(id);
                        this.cancel();
                     }
                  }
               })
               .runTaskTimer(this.plugin, 0L, 20L);
         }
      }
   }

   private long getCooldownMillis() {
      long seconds = this.plugin.getConfig().getLong("weapons.chainsaw_sword.curse-cooldown-seconds", 20L);
      if (seconds < 0L) {
         seconds = 0L;
      }

      return seconds * 1000L;
   }

   private int getBleedDurationTicks() {
      int seconds = this.plugin.getConfig().getInt("weapons.chainsaw_sword.bleed-duration-seconds", 3);
      if (seconds < 0) {
         seconds = 0;
      }

      return seconds * 20;
   }

   private double getBleedDamage() {
      double damage = this.plugin.getConfig().getDouble("weapons.chainsaw_sword.bleed-damage", 1.0);
      if (damage < 0.0) {
         damage = 0.0;
      }

      return damage;
   }
}
