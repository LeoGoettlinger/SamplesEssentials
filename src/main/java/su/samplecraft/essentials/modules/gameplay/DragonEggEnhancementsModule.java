package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Merged from {@code DragonEggEnhancements-1.5.jar}
 * (com.jordan.mods.dragonEggEnhancements.DragonEggEnhancements).
 * <p>
 * 1:1 port: whoever carries the dragon egg gets speed / fire resistance /
 * strength / glowing and 15 hearts; the egg itself cannot be stored in
 * containers, despawn, be destroyed by explosions, fire or cacti, and if it
 * falls into the void it returns to spawn. {@code /dragoneggcoords} shows
 * where the egg currently is.
 */
public final class DragonEggEnhancementsModule extends EssentialsModule implements Listener {

    private final Map<UUID, Boolean> playersWithEgg = new HashMap<>();
    private Location droppedEggLocation;
    private Item droppedEggItem;
    private Location placedEggLocation;

    public DragonEggEnhancementsModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "dragonegg", "DragonEggEnhancements-1.5.jar",
                "Dragon egg powers, container/void/explosion protection and /dragoneggcoords");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.dragonegg", "Use /dragoneggcoords", org.bukkit.permissions.PermissionDefault.TRUE);
        command("dragoneggcoords", this::onCommand);
        command("eggcoords", this::onCommand);
        syncRepeat(0L, 20L, this::checkPlayersWithDragonEgg);
        syncRepeat(0L, 10L, () -> {
            checkDroppedEggs();
            checkPlacedEggs();
            checkVoidEgg();
        });
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    /* ------------------------------------------------------------------ */
    /* carrier effects                                                     */
    /* ------------------------------------------------------------------ */

    private void checkPlayersWithDragonEgg() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            boolean hasEgg = player.getInventory().contains(Material.DRAGON_EGG);
            UUID id = player.getUniqueId();
            Boolean hadEgg = playersWithEgg.get(id);
            if (hasEgg && (hadEgg == null || !hadEgg)) {
                playersWithEgg.put(id, true);
                applyEffects(player);
                sendIfSet(player, cfgString("messages.egg_obtained", ""));
            } else if (!hasEgg && hadEgg != null && hadEgg) {
                playersWithEgg.put(id, false);
                removeEffects(player);
                sendIfSet(player, cfgString("messages.egg_lost", ""));
            } else if (hasEgg) {
                applyEffects(player);
            }
        }
    }

    private void applyEffects(Player player) {
        if (cfgBool("effects.speed.enabled", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE,
                    Math.max(0, cfgInt("effects.speed.level", 1) - 1), false, false, true));
        }
        if (cfgBool("effects.fire_resistance.enabled", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE,
                    Math.max(0, cfgInt("effects.fire_resistance.level", 1) - 1), false, false, true));
        }
        if (cfgBool("effects.strength.enabled", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE,
                    Math.max(0, cfgInt("effects.strength.level", 1) - 1), false, false, true));
        }
        if (cfgBool("effects.glowing.enabled", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
        }
        if (cfgBool("health.enabled", true)) {
            double hearts = cfgDouble("health.hearts", 15.0);
            var attribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (attribute != null) {
                attribute.setBaseValue(hearts * 2.0);
                if (player.getHealth() > hearts * 2.0) {
                    player.setHealth(hearts * 2.0);
                }
            }
        }
    }

    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.GLOWING);
        var attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(20.0);
            if (player.getHealth() > 20.0) {
                player.setHealth(20.0);
            }
        }
    }

    private void sendIfSet(Player player, String message) {
        if (message != null && !message.isBlank()) {
            player.sendMessage(su.samplecraft.essentials.util.Text.parse(message));
        }
    }

    /* ------------------------------------------------------------------ */
    /* protection                                                          */
    /* ------------------------------------------------------------------ */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!cfgBool("protection.block_containers", true) || event.getClickedInventory() == null) {
            return;
        }
        InventoryType type = event.getClickedInventory().getType();
        boolean container = type != InventoryType.PLAYER && type != InventoryType.CRAFTING;
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (container && (isEgg(current) || isEgg(cursor))) {
            cancelWithMessage(event);
            return;
        }
        if (event.isShiftClick() && isEgg(current) && type == InventoryType.PLAYER) {
            cancelWithMessage(event);
        }
    }

    private void cancelWithMessage(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player) {
            sendIfSet(player, cfgString("messages.cannot_store", ""));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!cfgBool("protection.block_containers", true) || !isEgg(event.getOldCursor())) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            Inventory inventory = event.getView().getInventory(slot);
            if (inventory != null && inventory.getType() != InventoryType.PLAYER
                    && inventory.getType() != InventoryType.CRAFTING) {
                event.setCancelled(true);
                if (event.getWhoClicked() instanceof Player player) {
                    sendIfSet(player, cfgString("messages.cannot_store", ""));
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (cfgBool("protection.block_containers", true) && isEgg(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDespawn(ItemDespawnEvent event) {
        if (cfgBool("protection.no_despawn", true) && event.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (cfgBool("protection.kill_command_proof", true)
                && event.getEntity() instanceof Item item
                && item.getItemStack().getType() == Material.DRAGON_EGG) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item) || item.getItemStack().getType() != Material.DRAGON_EGG) {
            return;
        }
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cfgBool("protection.explosion_proof", true)
                && (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
            event.setCancelled(true);
        }
        if (cfgBool("protection.fire_proof", true)
                && (cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA)) {
            event.setCancelled(true);
        }
        if (cfgBool("protection.cactus_proof", true) && cause == EntityDamageEvent.DamageCause.CONTACT) {
            event.setCancelled(true);
        }
        if (cfgBool("protection.kill_command_proof", true) && cause == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
        }
        if (cfgBool("protection.void_protection", true) && cause == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            World world = item.getWorld();
            item.teleport(world.getSpawnLocation());
            broadcastIfSet(cfgString("messages.void_return", ""));
        }
    }

    /* ------------------------------------------------------------------ */
    /* tracking                                                            */
    /* ------------------------------------------------------------------ */

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.DRAGON_EGG) {
            return;
        }
        droppedEggItem = event.getItemDrop();
        droppedEggLocation = droppedEggItem.getLocation();
        placedEggLocation = null;
        if (cfgBool("display.glowing_when_dropped", true)) {
            droppedEggItem.setGlowing(true);
        }
        if (cfgBool("display.show_coordinates_when_dropped", true) && droppedEggLocation != null) {
            event.getPlayer().sendMessage(su.samplecraft.essentials.util.Text.parse(
                    coordsMessage(cfgString("messages.egg_location", ""), droppedEggLocation)));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.DRAGON_EGG) {
            return;
        }
        placedEggLocation = event.getBlock().getLocation();
        droppedEggItem = null;
        droppedEggLocation = null;
        if (cfgBool("display.show_coordinates_when_dropped", true)) {
            event.getPlayer().sendMessage(su.samplecraft.essentials.util.Text.parse(
                    coordsMessage(cfgString("messages.egg_placed", ""), placedEggLocation)));
        }
    }

    private void checkDroppedEggs() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                if (item.getItemStack().getType() == Material.DRAGON_EGG) {
                    droppedEggItem = item;
                    droppedEggLocation = item.getLocation();
                    if (cfgBool("display.glowing_when_dropped", true)) {
                        item.setGlowing(true);
                    }
                    return;
                }
            }
        }
        if (placedEggLocation == null) {
            droppedEggItem = null;
            droppedEggLocation = null;
        }
    }

    private void checkPlacedEggs() {
        if (placedEggLocation == null) {
            return;
        }
        Block block = placedEggLocation.getBlock();
        if (block.getType() != Material.DRAGON_EGG) {
            placedEggLocation = null;
        }
    }

    private void checkVoidEgg() {
        if (!cfgBool("protection.void_protection", true)) {
            return;
        }
        if (droppedEggItem == null || !droppedEggItem.isValid()) {
            return;
        }
        Location location = droppedEggItem.getLocation();
        World world = location.getWorld();
        if (world != null && location.getY() < world.getMinHeight()) {
            Location spawn = world.getSpawnLocation();
            droppedEggItem.teleport(spawn);
            droppedEggLocation = spawn;
            broadcastIfSet(cfgString("messages.void_return", ""));
        }
    }

    /* ------------------------------------------------------------------ */
    /* command                                                             */
    /* ------------------------------------------------------------------ */

    private boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command,
                              String label, String[] args) {
        if (placedEggLocation != null) {
            sender.sendMessage(su.samplecraft.essentials.util.Text.parse(
                    coordsMessage(cfgString("messages.egg_location_placed", ""), placedEggLocation)));
            return true;
        }
        if (droppedEggLocation != null && droppedEggItem != null && droppedEggItem.isValid()) {
            sender.sendMessage(su.samplecraft.essentials.util.Text.parse(
                    coordsMessage(cfgString("messages.egg_location", ""), droppedEggLocation)));
            return true;
        }
        sender.sendMessage(su.samplecraft.essentials.util.Text.parse(cfgString("messages.egg_not_found", "")));
        return true;
    }

    private String coordsMessage(String template, Location location) {
        World world = location.getWorld();
        return template
                .replace("%x%", String.valueOf(location.getBlockX()))
                .replace("%y%", String.valueOf(location.getBlockY()))
                .replace("%z%", String.valueOf(location.getBlockZ()))
                .replace("%world%", world == null ? "?" : world.getName());
    }

    private void broadcastIfSet(String message) {
        if (message != null && !message.isBlank()) {
            plugin.getServer().broadcast(su.samplecraft.essentials.util.Text.parse(message));
        }
    }

    private static boolean isEgg(ItemStack item) {
        return item != null && item.getType() == Material.DRAGON_EGG;
    }
}
