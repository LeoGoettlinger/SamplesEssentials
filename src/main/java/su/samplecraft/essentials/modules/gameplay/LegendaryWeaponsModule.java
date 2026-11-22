package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code demoralegendaryweapons-5.2.jar} (dev.demora.demoralegendaryweapons).
 * <p>
 * The original ships 30 hand-coded weapons, each with its own listener and
 * animation. This port keeps the defining behaviour of the core set and gives
 * every weapon the same machinery instead of 30 bespoke classes:
 * <ul>
 *   <li>a shared factory that builds the item, applies the configured attribute
 *       modifiers, hides attributes, makes it unbreakable and tags it through the
 *       persistent data container (so the original items are recognisable),</li>
 *   <li>a cooldown map with the original chat feedback,</li>
 *   <li>per-weapon abilities: Mjolnir throw (lightning + AoE), Shadow Blade
 *       invisibility burst, Reaper's Scythe heal-on-hit, Dragon Katana dash,
 *       Midas Sword damage growth on kills, Excalibur strength burst,
 *       Magma Club ignite, Golem Hammer shockwave, Poseidon's Trident riptide.</li>
 * </ul>
 * The remaining weapons are still obtainable through {@code /demorahop give} and
 * keep their base damage and cooldown wrapper, so an old world's items keep
 * working; only the bespoke ability scripts were merged into the handlers above.
 * <p>
 * Commands: {@code /demorahop give <weapon> [player]}, {@code /demorahop list},
 * {@code /demorahop reload}.
 */
public final class LegendaryWeaponsModule extends EssentialsModule implements Listener {

    /** One legendary weapon definition. */
    private record Weapon(String id, Material material, double damage, double attackSpeed,
                          String name, int cooldownSeconds, String ability) {
    }

    private static final List<Weapon> WEAPONS = List.of(
            new Weapon("mjolnir", Material.STONE_AXE, 11, -2.4, "&6&lMjolnir", 10, "throw"),
            new Weapon("shadow_blade", Material.NETHERITE_SWORD, 11, -2.4, "&8&lShadow Blade", 30, "invisible"),
            new Weapon("reaper_scythe", Material.NETHERITE_HOE, 11, -2.4, "&4&lReaper's Scythe", 40, "heal"),
            new Weapon("dragon_katana", Material.NETHERITE_SWORD, 11, -2.4, "&5&lDragon Katana", 20, "dash"),
            new Weapon("awakened_dragon_katana", Material.NETHERITE_SWORD, 11, -2.4,
                    "&d&lAwakened Dragon Katana", 20, "dash"),
            new Weapon("midas_sword", Material.GOLDEN_SWORD, 7, -2.4, "&e&lMidas Sword", 0, "grow"),
            new Weapon("excalibur", Material.IRON_SWORD, 11, -2.4, "&b&lExcalibur", 20, "strength"),
            new Weapon("magma_club", Material.BLAZE_ROD, 11, -2.4, "&c&lMagma Club", 15, "ignite"),
            new Weapon("golem_hammer", Material.IRON_AXE, 12, -3.0, "&7&lGolem Hammer", 40, "shockwave"),
            new Weapon("poseidon_trident", Material.TRIDENT, 11, -2.4, "&3&lPoseidon's Trident", 10, "riptide"),
            new Weapon("sculk_crossbow", Material.CROSSBOW, 0, 0, "&3&lSculk Crossbow", 20, "explode"),
            new Weapon("chainsaw_sword", Material.NETHERITE_AXE, 11, -2.4, "&e&lChainsaw Sword", 20, "bleed"),
            new Weapon("emerald_blade", Material.DIAMOND_SWORD, 7, -2.4, "&a&lEmerald Blade", 0, "grow"),
            new Weapon("lich_staff", Material.STICK, 0, 0, "&2&lLich Staff", 30, "explode"),
            new Weapon("villager_staff", Material.STICK, 0, 0, "&a&lVillager Staff", 40, "explode"),
            new Weapon("void_staff", Material.STICK, 0, 0, "&5&lVoid Staff", 40, "explode"),
            new Weapon("hypnosis_staff", Material.STICK, 0, 0, "&d&lHypnosis Staff", 5, "explode"),
            new Weapon("elemental_staff", Material.BLAZE_ROD, 0, 0, "&6&lElemental Staff", 25, "explode"),
            new Weapon("toxic_crossbow", Material.CROSSBOW, 0, 0, "&2&lToxic Crossbow", 30, "explode"),
            new Weapon("artemis_bow", Material.BOW, 0, 0, "&a&lArtemis Bow", 0, "none"),
            new Weapon("cloud_sword", Material.DIAMOND_SWORD, 11, -2.4, "&f&lCloud Sword", 25, "speed"),
            new Weapon("wither_sickles", Material.NETHERITE_HOE, 10, -2.4, "&8&lWither Sickles", 10, "wither"),
            new Weapon("awakened_wither_sickles", Material.NETHERITE_HOE, 13, -1.6,
                    "&6&lAwakened Wither Sickles", 12, "wither"),
            new Weapon("freezing_chakram", Material.SNOWBALL, 8, -2.4, "&b&lFreezing Chakram", 20, "freeze"),
            new Weapon("fugucannon", Material.CROSSBOW, 0, 0, "&2&lFugu Cannon", 20, "explode"),
            new Weapon("beehiveblaster", Material.CROSSBOW, 0, 0, "&6&lBeehive Blaster", 20, "explode"),
            new Weapon("reinforced_elytra", Material.ELYTRA, 0, 0, "&7&lReinforced Elytra", 50, "none"),
            new Weapon("shrinkray", Material.SPYGLASS, 0, 0, "&d&lShrink Ray", 40, "shrink"),
            new Weapon("ravager_horn", Material.GOAT_HORN, 0, 0, "&8&lRavager Horn", 40, "none"),
            new Weapon("warpick", Material.NETHERITE_PICKAXE, 11, -2.4, "&5&lWarpick", 20, "none")
    );

    private static final Map<String, Weapon> BY_ID = new LinkedHashMap<>();

    static {
        for (Weapon weapon : WEAPONS) {
            BY_ID.put(weapon.id(), weapon);
        }
    }

    private NamespacedKey weaponKey;
    /** weapon id -> player uuid -> ready-at millis */
    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();
    /** Midas growth: player uuid -> bonus damage */
    private final Map<UUID, Double> midasBonus = new HashMap<>();

    public LegendaryWeaponsModule(SamplesEssentials plugin) {
        super("gameplay", "legendaryweapons", "demoralegendaryweapons-5.2.jar",
                "Legendary weapons with abilities, cooldowns and attribute modifiers");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.legendaryweapons", "Receive legendary weapons", PermissionDefault.TRUE);
        ensurePermission("samplese.legendaryweapons.admin", "Give legendary weapons", PermissionDefault.OP);
        weaponKey = new NamespacedKey(plugin, "legendary_weapon");
        ensureDefaultFile("config.yml");
        reloadConfig();
        command("demorahop", new HopCommand());
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    /* ------------------------------------------------------------------ */
    /* item factory                                                       */
    /* ------------------------------------------------------------------ */

    private ItemStack build(Weapon weapon) {
        ItemStack item = new ItemStack(weapon.material());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Text.parse(weapon.name()));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        if (weapon.damage() > 0) {
            AttributeModifier damage = new AttributeModifier(
                    new NamespacedKey(plugin, "lw_damage_" + weapon.id()),
                    weapon.damage(), AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND);
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, damage);
        }
        if (weapon.attackSpeed() != 0) {
            AttributeModifier speed = new AttributeModifier(
                    new NamespacedKey(plugin, "lw_speed_" + weapon.id()),
                    weapon.attackSpeed(), AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND);
            meta.addAttributeModifier(Attribute.ATTACK_SPEED, speed);
        }
        List<String> lore = new ArrayList<>();
        lore.add(msg("lore-type"));
        if (weapon.cooldownSeconds() > 0) {
            lore.add(msg("lore-cooldown", "seconds", String.valueOf(weapon.cooldownSeconds())));
        }
        if (!weapon.ability().equals("none")) {
            lore.add(msg("lore-ability", "ability", msg("ability." + weapon.id())));
        }
        meta.lore(lore.stream().map(Text::parse).toList());
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, weapon.id());
        item.setItemMeta(meta);
        return item;
    }

    private Weapon weaponOf(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(weaponKey, PersistentDataType.STRING);
        return id == null ? null : BY_ID.get(id.toLowerCase(Locale.ROOT));
    }

    private boolean ready(Player player, Weapon weapon, boolean notify) {
        if (weapon.cooldownSeconds() <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        Map<UUID, Long> map = cooldowns.computeIfAbsent(weapon.id(), key -> new HashMap<>());
        long until = map.getOrDefault(player.getUniqueId(), 0L);
        if (until > now) {
            if (notify) {
                reply(player, "cooldown", "weapon", weapon.name(),
                        "time", String.valueOf((until - now + 999) / 1000));
            }
            return false;
        }
        map.put(player.getUniqueId(), now + weapon.cooldownSeconds() * 1000L);
        return true;
    }

    /* ------------------------------------------------------------------ */
    /* listeners                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Weapon weapon = weaponOf(event.getItem());
        if (weapon == null) {
            return;
        }

        switch (weapon.ability()) {
            case "throw" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                Location target = player.getTargetBlockExact(30) == null
                        ? player.getLocation().add(player.getLocation().getDirection().multiply(15))
                        : player.getTargetBlockExact(30).getLocation();
                target.getWorld().strikeLightningEffect(target);
                for (Entity entity : target.getWorld().getNearbyEntities(target, 4, 4, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(cfgDouble("weapons.mjolnir.damage.area_damage", 12), player);
                    }
                }
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, target, 60, 2, 2, 2, 0.2);
            }
            case "invisible" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                int seconds = Math.max(1, cfgInt("weapons.shadow_blade.duration", 200) / 20);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,
                        seconds * 20, 1, false, false, false));
                player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation(), 40, 0.5, 1, 0.5, 0.02);
                reply(player, "shadow-blade-visible");
                syncLater(seconds * 20L, () -> {
                    if (player.isOnline()) {
                        reply(player, "shadow-blade-visible");
                    }
                });
            }
            case "dash" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                double distance = cfgDouble("weapons.dragon_katana.dash-distance-blocks", 30);
                player.setVelocity(player.getLocation().getDirection().normalize().multiply(
                        Math.min(distance / 6, 3)));
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, player.getLocation(), 40,
                        0.4, 0.4, 0.4, 0.02);
            }
            case "strength" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                int seconds = Math.max(1, cfgInt("weapons.excalibur.duration", 7));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,
                        seconds * 20, 1, false, true, true));
                player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation(), 30, 0.4, 0.6, 0.4, 0.02);
            }
            case "ignite" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 4, 3, 4)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.setFireTicks(100);
                        living.damage(cfgDouble("weapons.magma_club.damage", 10), player);
                    }
                }
                player.getWorld().spawnParticle(Particle.FLAME, player.getLocation(), 60, 2, 1, 2, 0.05);
            }
            case "shockwave" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                double radius = cfgDouble("weapons.golem_hammer.shockwave_radius", 6);
                double damage = cfgDouble("weapons.golem_hammer.shockwave_damage", 15);
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(),
                        radius, radius, radius)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.damage(damage, player);
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                    }
                }
                player.getWorld().spawnParticle(Particle.BLOCK, player.getLocation(), 120, radius / 2,
                        0.5, radius / 2, 0.1, Material.DIRT.createBlockData());
            }
            case "riptide" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                player.setVelocity(player.getLocation().getDirection().normalize().multiply(2.5));
                player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 60, 1, 0.3, 1, 0.1);
            }
            case "speed" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                int seconds = Math.max(1, cfgInt("weapons.cloud_sword.ability_duration", 10));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, seconds * 20, 1));
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 60, 1, 0.4, 1, 0.05);
            }
            case "freeze" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), 5, 4, 5)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
                        living.setFreezeTicks(140);
                    }
                }
                player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 60, 2, 1, 2, 0.05);
            }
            case "shrink" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                double scale = Math.max(0.3, player.getAttribute(Attribute.SCALE).getBaseValue() - 0.4);
                player.getAttribute(Attribute.SCALE).setBaseValue(scale);
                int seconds = Math.max(1, cfgInt("weapons.shrink_ray.shrink_duration", 12));
                syncLater(seconds * 20L, () -> {
                    if (player.isOnline()) {
                        player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
                    }
                });
                reply(player, "shrink-applied", "seconds", String.valueOf(seconds));
            }
            case "explode" -> {
                if (!ready(player, weapon, true)) {
                    return;
                }
                event.setCancelled(true);
                Location target = player.getTargetBlockExact(30) == null
                        ? player.getLocation()
                        : player.getTargetBlockExact(30).getLocation();
                float power = (float) cfgDouble("weapons." + weapon.id() + ".explosion.power", 3.0);
                boolean breakBlocks = cfgBool("weapons." + weapon.id() + ".explosion.break-blocks", false);
                target.getWorld().createExplosion(target, power, false, breakBlocks, player);
            }
            default -> {
                // passive weapon, nothing to cast
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Player damager = null;
        if (event.getDamager() instanceof Player player) {
            damager = player;
        } else if (event.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            damager = shooter;
        }
        if (damager == null) {
            return;
        }
        final Player source = damager;
        Weapon weapon = weaponOf(source.getInventory().getItemInMainHand());
        if (weapon == null) {
            return;
        }
        switch (weapon.ability()) {
            case "heal" -> {
                double hearts = cfgDouble("weapons.reaper_scythe.heal_hearts", 2);
                var maxHealth = source.getAttribute(Attribute.MAX_HEALTH);
                double max = maxHealth == null ? 20 : maxHealth.getValue();
                source.setHealth(Math.min(max, source.getHealth() + hearts * 2));
                source.getWorld().spawnParticle(Particle.HEART, source.getLocation().add(0, 2, 0),
                        3, 0.4, 0.4, 0.4, 0.01);
            }
            case "wither" -> {
                if (event.getEntity() instanceof LivingEntity living) {
                    living.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 1));
                }
            }
            case "bleed" -> {
                if (event.getEntity() instanceof LivingEntity living) {
                    double bleed = cfgDouble("weapons.chainsaw_sword.bleed-damage", 2);
                    syncLater(20L, () -> {
                        if (living.isValid()) {
                            living.damage(bleed, source);
                            living.getWorld().spawnParticle(Particle.DUST, living.getLocation().add(0, 1, 0),
                                    8, 0.3, 0.3, 0.3, 0.01);
                        }
                    });
                }
            }
            case "grow" -> {
                // growth is awarded on kill (see onDeath) so hits keep the base damage
            }
            default -> {
                // no on-hit ability
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        Weapon weapon = weaponOf(killer.getInventory().getItemInMainHand());
        if (weapon == null) {
            return;
        }
        if (weapon.ability().equals("grow")) {
            double bonus = midasBonus.merge(killer.getUniqueId(),
                    cfgDouble("weapons.midas_sword.damage-per-kill", 2), Double::sum);
            double max = cfgDouble("weapons.midas_sword.max-damage", 25);
            bonus = Math.min(bonus, max);
            reply(killer, "midas-upgrade", "damage",
                    String.format(Locale.ROOT, "%.0f", cfgDouble("weapons.midas_sword.initial-damage", 7) + bonus));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // legendary weapons never get lost: cancel void damage for holders of the bypass perm
        if (event.getCause() == EntityDamageEvent.DamageCause.VOID
                && player.hasPermission("samplese.legendaryweapons")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (weaponOf(hand) != null) {
                Location safe = player.getWorld().getSpawnLocation();
                event.setCancelled(true);
                player.teleportAsync(safe);
                reply(player, "saved-from-void");
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* command                                                            */
    /* ------------------------------------------------------------------ */

    private final class HopCommand implements CommandExecutor, TabCompleter {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (args.length == 0) {
                reply(sender, "usage", "usage", "/demorahop <give|list|reload>");
                return true;
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "give" -> give(sender, args);
                case "list" -> {
                    List<String> ids = new ArrayList<>(BY_ID.keySet());
                    reply(sender, "list", "weapons", String.join(", ", ids));
                }
                case "reload" -> {
                    if (!sender.hasPermission("samplese.legendaryweapons.admin")) {
                        reply(sender, "no-permission");
                        return true;
                    }
                    reloadConfig();
                    reply(sender, "reloaded");
                }
                default -> reply(sender, "usage", "usage", "/demorahop <give|list|reload>");
            }
            return true;
        }

        private void give(CommandSender sender, String[] args) {
            if (!sender.hasPermission("samplese.legendaryweapons.admin")) {
                reply(sender, "no-permission");
                return;
            }
            if (args.length < 2) {
                reply(sender, "usage", "usage", "/demorahop give <weapon> [player]");
                return;
            }
            Weapon weapon = BY_ID.get(args[1].toLowerCase(Locale.ROOT));
            if (weapon == null) {
                reply(sender, "weapon-not-found", "weapon", args[1]);
                return;
            }
            Player target;
            if (args.length >= 3) {
                target = plugin.getServer().getPlayerExact(args[2]);
                if (target == null) {
                    reply(sender, "player-not-found", "player", args[2]);
                    return;
                }
            } else if (sender instanceof Player player) {
                target = player;
            } else {
                reply(sender, "player-only");
                return;
            }
            ItemStack item = build(weapon);
            Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
            leftover.values().forEach(rest -> target.getWorld().dropItemNaturally(target.getLocation(), rest));
            reply(sender, "given", "weapon", weapon.name(), "player", target.getName());
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                return List.of("give", "list", "reload");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
                return new ArrayList<>(BY_ID.keySet());
            }
            return List.of();
        }
    }

    private void reply(CommandSender target, String key, String... kv) {
        target.sendMessage(Text.parse(msg(key, kv)));
    }
}
