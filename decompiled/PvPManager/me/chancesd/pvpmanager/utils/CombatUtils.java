package me.chancesd.pvpmanager.utils;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.integration.hook.placeholderapi.PlaceholderProcessor;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.scheduler.ScheduleUtils;
import me.chancesd.pvpmanager.sdutils.utils.ChatUtils;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.sdutils.utils.ReflectionUtil;
import me.chancesd.pvpmanager.setting.Conf;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

public final class CombatUtils {
   private static final DecimalFormat decimalFormat = new DecimalFormat();
   private static PlaceholderProcessor cachedPlaceholderProcessor;

   private CombatUtils() {
   }

   public static String formatTo2Digits(double value) {
      return decimalFormat.format(value);
   }

   public static boolean hasTimePassed(long toggleTime, int cooldown) {
      return hasTimePassedMs(toggleTime, cooldown * 1000L);
   }

   public static boolean hasTimePassedMs(long toggleTime, long cooldown) {
      return System.currentTimeMillis() - toggleTime >= cooldown;
   }

   public static int getTimeLeft(long startTime, int time) {
      return (int)(getTimeLeftMs(startTime, time * 1000L) / 1000L);
   }

   public static long getTimeLeftMs(long startTime, long time) {
      return startTime + time - System.currentTimeMillis();
   }

   public static void executeCommands(List<String> commands, Player player, String playerName, String victim) {
      if (!commands.isEmpty()) {
         for (String command : commands) {
            try {
               String preparedCommand = command.replace("{player}", playerName)
                  .replace("{victim}", victim)
                  .replace("{item}", getItemDisplay(player.getItemInHand()));
               if (preparedCommand.toLowerCase().startsWith("!console")) {
                  ScheduleUtils.executeConsoleCommand(preparedCommand.substring(9));
               } else if (preparedCommand.toLowerCase().startsWith("!player")) {
                  player.performCommand(preparedCommand.substring(8));
               } else {
                  ScheduleUtils.executeConsoleCommand(preparedCommand);
               }
            } catch (CommandException var7) {
               Log.warning("Error executing command: \"" + command + "\" for player: " + playerName);
               Log.warning("This error comes from the command and it's respective plugin below:");
               Log.warning(var7.getMessage(), var7);
            }
         }
      }
   }

   public static void executeCommands(List<String> commands, Player player, String playerName) {
      executeCommands(commands, player, playerName, "");
   }

   private static String getItemDisplay(ItemStack item) {
      if (item.hasItemMeta()) {
         ItemMeta itemMeta = item.getItemMeta();
         if (itemMeta.hasDisplayName()) {
            return itemMeta.getDisplayName();
         }

         if (MCVersion.isAtLeast(MCVersion.V1_20_5) && itemMeta.hasItemName()) {
            return itemMeta.getItemName();
         }
      }

      return item.getType().name();
   }

   public static final boolean isPvP(EntityDamageByEntityEvent event) {
      Entity attacker = event.getDamager();
      Entity defender = event.getEntity();
      if (!(defender instanceof Player)) {
         return false;
      } else if (attacker instanceof Player) {
         return isValidPvPAttack(attacker, defender);
      } else {
         if (attacker instanceof Projectile || MCVersion.isAtLeast(MCVersion.V1_9) && attacker instanceof AreaEffectCloud) {
            ProjectileSource projSource = getSource(attacker);
            if (projSource instanceof Player) {
               Entity shooter = (Entity)projSource;
               if (isValidPvPAttack(shooter, defender)) {
                  return shouldCountDamage(event);
               }
            }
         }

         if (attacker instanceof TNTPrimed tnt) {
            Entity tntAttacker = tnt.getSource();
            if (tntAttacker instanceof Player && isValidPvPAttack(tntAttacker, defender)) {
               return true;
            }
         }

         return false;
      }
   }

   private static boolean isValidPvPAttack(Entity attacker, Entity defender) {
      return Conf.SELF_TAG.asBool() || !defender.equals(attacker);
   }

   private static boolean shouldCountDamage(EntityDamageByEntityEvent event) {
      return !Conf.IGNORE_NO_DMG_HITS.asBool() || event.getDamage() != 0.0;
   }

   public static final boolean isPvP(EntityCombustByEntityEvent event) {
      Entity attacker = event.getCombuster();
      Entity defender = event.getEntity();
      if (!(defender instanceof Player)) {
         return false;
      } else if (attacker instanceof Player) {
         return true;
      } else {
         if (attacker instanceof Projectile projectile) {
            ProjectileSource projSource = projectile.getShooter();
            if (projSource instanceof Player) {
               Entity shooter = (Entity)projSource;
               return isValidPvPAttack(shooter, defender);
            }
         }

         return false;
      }
   }

   public static final boolean isNPC(Entity entity) {
      return entity.hasMetadata("NPC");
   }

   public static boolean canFly(Player p) {
      return p.isFlying() || p.getAllowFlight();
   }

   public static void checkGlide(CombatPlayer combatPlayer) {
      Player p = combatPlayer.getPlayer();
      if (p.isGliding()) {
         Location playerLocation = p.getLocation();
         p.setGliding(false);
         combatPlayer.getExemptions().setCanBypassTeleportBlock(true);
         ScheduleUtils.teleport(p, playerLocation, "Failed to teleport player after disabling glide")
            .thenAccept(success -> combatPlayer.getExemptions().setCanBypassTeleportBlock(false));
         p.setFallDistance(-200.0F);
         if (Conf.PUSHBACK_REMOVE_ELYTRA.asBool()) {
            removeElytra(p);
         }
      }
   }

   public static void removeElytra(Player p) {
      ItemStack chestplate = p.getInventory().getChestplate();
      Location playerLocation = p.getLocation();
      if (chestplate != null && chestplate.getType() == Material.ELYTRA && playerLocation != null) {
         p.getInventory().setChestplate(null);
         Map<Integer, ItemStack> item = p.getInventory().addItem(new ItemStack[]{chestplate});
         if (!item.isEmpty()) {
            p.getWorld().dropItemNaturally(playerLocation, item.values().stream().findFirst().orElse(chestplate));
         }
      }
   }

   public static void fakeItemStackDrop(Player player, ItemStack[] inventory) {
      Location playerLocation = player.getLocation();
      World playerWorld = player.getWorld();

      for (ItemStack itemstack : inventory) {
         if (itemstack != null && !itemstack.getType().equals(Material.AIR)) {
            playerWorld.dropItemNaturally(playerLocation, itemstack);
         }
      }
   }

   public static boolean isOnline(@NotNull String name) {
      return Bukkit.getPlayer(name) != null;
   }

   public static boolean isOnline(@NotNull UUID uuid) {
      return Bukkit.getPlayer(uuid) != null;
   }

   public static boolean isReal(@NotNull UUID id) {
      return Bukkit.getPlayer(id) != null;
   }

   public static boolean isWorldExcluded(String worldName) {
      return Conf.WORLD_EXCLUSIONS.asSet().contains(worldName);
   }

   public static boolean hasHarmfulPotion(AreaEffectCloud areaCloud) {
      if (MCVersion.isAtLeast(MCVersion.V1_20_2)) {
         PotionType basePotionType = areaCloud.getBasePotionType();
         if (basePotionType == null) {
            return false;
         } else {
            List<PotionEffect> potionTypes = basePotionType.getPotionEffects();
            return !potionTypes.isEmpty() && potionTypes.stream().anyMatch(p -> isHarmfulPotion(p.getType()));
         }
      } else {
         PotionEffectType potionEffectType = null;

         try {
            potionEffectType = (PotionEffectType)ReflectionUtil.invokeMethods(areaCloud, "getBasePotionData", "getType", "getEffectType");
         } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | IllegalAccessException var3) {
            Log.severe("Error getting potion type of lingering potion", var3);
         }

         return potionEffectType != null && isHarmfulPotion(potionEffectType);
      }
   }

   public static boolean isHarmfulPotion(PotionEffectType type) {
      return Conf.HARMFUL_POTIONS.asSet().contains(type.getName());
   }

   public static boolean recursiveContainsCommand(String[] givenCommand, List<String> list) {
      boolean contains = false;

      for (int i = 0; i < givenCommand.length; i++) {
         StringBuilder args = new StringBuilder(givenCommand[0]);

         for (int j = 1; j <= i; j++) {
            args.append(" ").append(givenCommand[j]);
         }

         if (list.contains(args.toString().toLowerCase())) {
            contains = true;
            break;
         }
      }

      return contains;
   }

   public static String truncateString(String text, int size) {
      return text.substring(0, Math.min(text.length(), size));
   }

   public static String processPlaceholders(Player player, String message) {
      return message == null ? message : ChatUtils.setPlaceholders(player, message, (p, msg) -> {
         if (cachedPlaceholderProcessor == null) {
            cachedPlaceholderProcessor = new PlaceholderProcessor(PvPManager.getInstance());
         }

         return cachedPlaceholderProcessor.replacePlaceholders(p, msg);
      });
   }

   private static ProjectileSource getSource(Entity entity) {
      return entity instanceof Projectile projectile ? projectile.getShooter() : ((AreaEffectCloud)entity).getSource();
   }

   static {
      decimalFormat.setMaximumFractionDigits(2);
   }
}
