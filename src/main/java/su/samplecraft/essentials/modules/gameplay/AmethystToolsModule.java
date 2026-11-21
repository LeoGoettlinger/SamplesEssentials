package su.samplecraft.essentials.modules.gameplay;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.samplecraft.essentials.module.EssentialsModule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Merged from {@code amethysttools-1.2.1.jar} (com.amethysttools.AmethystToolsPlugin).
 * <p>
 * Amethyst tools are regular netherite tools tagged with the persistent data
 * key {@code samplese:amethysttools_expires}. While active they mine the
 * configured area (default 3x3x1), respect the blocked-material list and drop
 * like a normal tool. They expire after {@code expiry.days} days.
 */
public final class AmethystToolsModule extends EssentialsModule implements Listener {

    public static final org.bukkit.NamespacedKey EXPIRES_KEY =
            new org.bukkit.NamespacedKey("samplese", "amethysttools_expires");

    private final Set<Material> blocked = new HashSet<>();

    public AmethystToolsModule(su.samplecraft.essentials.SamplesEssentials plugin) {
        super("gameplay", "amethysttools", "amethysttools-1.2.1.jar", "Area-mining amethyst tools that expire after a few days");
    }

    @Override
    protected void onEnable() {
        for (String raw : cfgStringList("blocked-materials")) {
            Material material = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
            if (material != null) {
                blocked.add(material);
            }
        }
        ensurePermission("samplese.amethysttools", "Receive amethyst tools", org.bukkit.permissions.PermissionDefault.OP);
        command("amethysttools", (sender, command, label, args) -> {
            if (!sender.hasPermission("samplese.amethysttools")) {
                sender.sendMessage(plugin.getLang().component("no-permission"));
                return true;
            }
            String type = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "pickaxe";
            Player target = sender instanceof Player p ? p : null;
            if (sender instanceof Player && args.length > 1) {
                Player other = plugin.getServer().getPlayerExact(args[1]);
                if (other != null) {
                    target = other;
                }
            }
            if (target == null) {
                sender.sendMessage(plugin.getLang().component("players-only"));
                return true;
            }
            ItemStack tool = build(type);
            if (tool == null) {
                sender.sendMessage(msgComponent("unknown-tool", "items", String.join(", ", toolKeys())));
                return true;
            }
            target.getInventory().addItem(tool);
            target.sendMessage(msgComponent("received", "tool", type));
            if (!target.equals(sender)) {
                sender.sendMessage(msgComponent("gave", "tool", type, "player", target.getName()));
            }
            return true;
        });
        onDisableHook(this::unbindCommands);
        listen(this);
    }

    private List<String> toolKeys() {
        List<String> out = new ArrayList<>();
        var section = config().getConfigurationSection("items");
        if (section != null) {
            out.addAll(section.getKeys(false));
        }
        return out;
    }

    private ItemStack build(String key) {
        var section = config().getConfigurationSection("items." + key);
        if (section == null) {
            return null;
        }
        Material material = Material.matchMaterial(section.getString("material", "NETHERITE_PICKAXE"));
        if (material == null || !material.isItem()) {
            return null;
        }
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.displayName(su.samplecraft.essentials.util.Text.parse(section.getString("name", "&dAmethyst Tool")));
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        long expiresInMillis = Math.max(1L, cfgInt("expiry.days", 3) * 24L * 60L * 60L * 1000L);
        for (String line : section.getStringList("lore")) {
            lore.add(su.samplecraft.essentials.util.Text.parse(
                    line.replace("%time%", formatDuration(expiresInMillis))));
        }
        meta.lore(lore);
        var enchantSection = section.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            for (String enchantName : enchantSection.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase(Locale.ROOT));
                if (enchantment != null) {
                    meta.addEnchant(enchantment, Math.max(1, enchantSection.getInt(enchantName, 1)), true);
                }
            }
        }
        if (section.getBoolean("unbreakable", false)) {
            meta.setUnbreakable(true);
        }
        if (section.getBoolean("glow", true)) {
            meta.setEnchantmentGlintOverride(true);
        }
        meta.getPersistentDataContainer().set(EXPIRES_KEY, PersistentDataType.LONG,
                System.currentTimeMillis() + expiresInMillis);
        item.setItemMeta(meta);
        return item;
    }

    private String formatDuration(long millis) {
        long days = millis / 86_400_000L;
        long hours = (millis % 86_400_000L) / 3_600_000L;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        long minutes = (millis % 3_600_000L) / 60_000L;
        return hours + "h " + minutes + "m";
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isAmethystTool(hand)) {
            return;
        }
        Long expires = hand.getItemMeta() == null ? null
                : hand.getItemMeta().getPersistentDataContainer().get(EXPIRES_KEY, PersistentDataType.LONG);
        if (expires == null) {
            return;
        }
        if (System.currentTimeMillis() > expires) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.sendMessage(msgComponent("tool-expired"));
            return;
        }
        int radius = Math.max(1, cfgInt("mining.radius", 1));
        int depth = Math.max(1, cfgInt("mining.depth", 1));
        if (radius <= 1 && depth <= 1) {
            return; // vanilla behaviour
        }
        if (blocked.contains(event.getBlock().getType())) {
            return;
        }

        List<Block> targets = new ArrayList<>();
        int broken = 0;
        int maxArea = Math.max(1, cfgInt("mining.max-blocks", 64));
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy < depth; dy++) {
                    if (dx == 0 && dz == 0 && dy == 0) {
                        continue;
                    }
                    Block block = event.getBlock().getRelative(dx, dy, dz);
                    if (block.getType().isAir() || blocked.contains(block.getType())
                            || block.getType().getHardness() < 0) {
                        continue;
                    }
                    targets.add(block);
                    if (targets.size() >= maxArea) {
                        break;
                    }
                }
            }
        }
        // every extra block costs one durability point (never unbreakable: the
        // item is a regular tool, so it can still break like in the original)
        for (Block block : targets) {
            if (hand.getType().getMaxDurability() > 0
                    && hand.getDurability() >= hand.getType().getMaxDurability() - 1) {
                break;
            }
            block.breakNaturally(hand);
            if (hand.getType().getMaxDurability() > 0) {
                hand.setDurability((short) (hand.getDurability() + 1));
            }
            broken++;
        }
    }

    private boolean isAmethystTool(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(EXPIRES_KEY, PersistentDataType.LONG);
    }
}
