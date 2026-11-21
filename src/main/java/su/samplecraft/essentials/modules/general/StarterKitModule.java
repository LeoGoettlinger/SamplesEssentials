package su.samplecraft.essentials.modules.general;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import su.samplecraft.essentials.config.ModuleStorage;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Merged from {@code StarterKitV2.jar} (dev.hercoon.starterKit.StarterKit).
 * <p>
 * Gives a configurable kit on first join (once per player, tracked in
 * {@code general/starterkit/data/received.yml}) and via {@code /starterkit}.
 * Item format in config.yml: {@code MATERIAL:amount} - e.g. {@code BREAD:16}.
 */
public final class StarterKitModule extends EssentialsModule implements Listener {

    private ModuleStorage storage;
    private int taskId = -1;

    public StarterKitModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("general", "starterkit", "StarterKitV2.jar", "First-join starter kit with /starterkit");
    }

    @Override
    protected void onEnable() {
        ensurePermission("samplese.starterkit", "Receive the starter kit", org.bukkit.permissions.PermissionDefault.TRUE);
        storage = new ModuleStorage(plugin, getDataFolder(), "received.yml");
        command("starterkit", (sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            if (!player.hasPermission("samplese.starterkit")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            give(player, true);
            return true;
        });
        onDisableHook(() -> {
            unbindCommands();
            if (taskId != -1) {
                plugin.getServer().getScheduler().cancelTask(taskId);
            }
        });
        if (cfgBool("give-on-first-join", true)) {
            listen(this);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (storage.yaml().getBoolean("players." + player.getUniqueId(), false)) {
            return;
        }
        long delay = Math.max(1L, cfgInt("join-delay-ticks", 20));
        // run once, after the join sequence finished, so items are not lost on first spawn sync
        taskId = plugin.getServer().getScheduler().runTaskLater(plugin, () -> give(player, false), delay).getTaskId();
    }

    private void give(Player player, boolean manual) {
        if (!manual && storage.yaml().getBoolean("players." + player.getUniqueId(), false)) {
            return;
        }
        List<String> items = cfgStringList("items");
        int given = 0;
        for (String entry : items) {
            String[] parts = entry.split(":", 2);
            Material material = Material.matchMaterial(parts[0].trim().toUpperCase(Locale.ROOT));
            if (material == null || !material.isItem()) {
                plugin.getLogger().warning("general/starterkit: unknown material '" + parts[0] + "'");
                continue;
            }
            int amount = parts.length > 1 ? parse(parts[1]) : 1;
            amount = Math.max(1, Math.min(amount, material.getMaxStackSize()));
            player.getInventory().addItem(new ItemStack(material, amount));
            given++;
        }
        if (given == 0) {
            player.sendMessage(plugin.getLang().component("starterkit-failed"));
            return;
        }
        storage.yaml().set("players." + player.getUniqueId(), true);
        storage.yaml().set("names." + player.getUniqueId(), player.getName());
        storage.saveLater();
        send(player, "given", "amount", String.valueOf(given));
    }

    private int parse(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
