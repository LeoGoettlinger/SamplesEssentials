package su.samplecraft.essentials.modules.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.Locale;

/**
 * Merged from {@code SmartSpawner-1.8.3.jar} (github.nighter.smartspawner.SmartSpawner).
 * <p>
 * Core interactions: spawn eggs change the mob of a spawner, {@code /smartspawner info}
 * shows the current type/limits and {@code /smartspawner set <entity>} edits it.
 * Optional accelerated spawn timing comes from the original plugin's
 * {@code spawn-delay} settings.
 */
public final class SmartSpawnerModule extends EssentialsModule implements Listener {

    public SmartSpawnerModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("world", "smartspawner", "SmartSpawner-1.8.3.jar", "Spawner editing with eggs and accelerated spawn timing");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.smartspawner", "Edit spawners", org.bukkit.permissions.PermissionDefault.OP);
        command("smartspawner", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.smartspawner")) {
                player.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            Block target = player.getTargetBlockExact(Math.max(3, cfgInt("max-distance", 8)));
            if (target == null || !(target.getState() instanceof CreatureSpawner spawner)) {
                player.sendMessage(msgComponent("not-a-spawner"));
                return true;
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("set")) {
                EntityType type;
                try {
                    type = EntityType.valueOf(args[1].toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    player.sendMessage(msgComponent("unknown-entity", "entity", args[1]));
                    return true;
                }
                if (!applySpawner(spawner, type)) {
                    player.sendMessage(msgComponent("not-a-mob", "entity", type.name()));
                    return true;
                }
                player.sendMessage(msgComponent("changed", "type", type.name().toLowerCase(Locale.ROOT)));
                return true;
            }
            EntityType type = spawner.getSpawnedType();
            player.sendMessage(msgComponent("info",
                    "type", type == null ? "none" : type.name().toLowerCase(Locale.ROOT),
                    "min", String.valueOf(spawner.getMinSpawnDelay()),
                    "max", String.valueOf(spawner.getMaxSpawnDelay()),
                    "count", String.valueOf(spawner.getSpawnCount()),
                    "range", String.valueOf(spawner.getRequiredPlayerRange())));
            return true;
        });
        onDisableHook(this::unbindCommands);
        if (cfgBool("rename-with-spawn-eggs", true)) {
            listen(this);
        }
    }

    private boolean applySpawner(CreatureSpawner spawner, EntityType type) {
        try {
            spawner.setSpawnedType(type);
        } catch (IllegalArgumentException e) {
            return false;
        }
        int delayMin = cfgInt("spawn-delay.min-ticks", -1);
        int delayMax = cfgInt("spawn-delay.max-ticks", -1);
        if (delayMin > 0) {
            spawner.setMinSpawnDelay(delayMin);
        }
        if (delayMax > 0) {
            spawner.setMaxSpawnDelay(delayMax);
        }
        int count = cfgInt("spawn-count", -1);
        if (count > 0) {
            spawner.setSpawnCount(count);
        }
        int range = cfgInt("required-player-range", -1);
        if (range > 0) {
            spawner.setRequiredPlayerRange(range);
        }
        spawner.update(true);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.SPAWNER) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.getType().name().endsWith("_SPAWN_EGG")) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission("samplese.smartspawner")) {
            return;
        }
        String entityKey = item.getType().name().replace("_SPAWN_EGG", "");
        EntityType type;
        try {
            type = EntityType.valueOf(entityKey);
        } catch (IllegalArgumentException e) {
            return;
        }
        if (!(block.getState() instanceof CreatureSpawner spawner)) {
            return;
        }
        event.setCancelled(true);
        if (!applySpawner(spawner, type)) {
            player.sendMessage(msgComponent("not-a-mob", "entity", entityKey));
            return;
        }
        if (!player.getGameMode().name().equals("CREATIVE")) {
            item.setAmount(item.getAmount() - 1);
        }
        player.sendMessage(msgComponent("changed", "type", type.name().toLowerCase(Locale.ROOT)));
    }
}
