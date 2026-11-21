package su.samplecraft.essentials.modules.general;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.samplecraft.essentials.SamplesEssentials;
import su.samplecraft.essentials.module.EssentialsModule;
import su.samplecraft.essentials.util.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Merged from {@code RHSignItem-Paper-1.21_R9.jar} ("Item Sign System").
 * <p>
 * Signs the item in your hand by appending lore lines (signer + date/time), and
 * lets you lock/unlock/delete the signature or rename the item. All state lives in
 * the item's persistent data container exactly like the original:
 * {@code signed}, {@code owner}, {@code locked}, {@code locked-by} and {@code lines}
 * (number of lore lines that belong to the signature, so deletion can trim them).
 * <p>
 * Commands: {@code /sign [text]} (greedy text; bare {@code /sign} uses the SHORT
 * template), {@code /sign delete}, {@code /sign lock}, {@code /sign unlock},
 * {@code /sign help}, {@code /sign version} and {@code /rename <text>}.
 * Config supports the content templates, date/time formats, stack limit,
 * item blacklist, deny-items-with-lore, auto-lock, offhand "ink sac" mode and
 * the Vault cost/action toggles.
 */
public final class SignItemModule extends EssentialsModule {

    private static final String[] CONTENT_DEFAULT = {
            "<white>%text%",
            "",
            "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date%<reset> <yellow><bold>%time%<reset>"
    };

    private static final String CONTENT_SHORT =
            "<gray>Signed by <green><bold>%player%<reset><gray> on <yellow><bold>%date%<reset> <yellow><bold>%time%<reset>";

    private final NamespacedKey signedKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey lockedKey;
    private final NamespacedKey lockedByKey;
    private final NamespacedKey linesKey;

    public SignItemModule(SamplesEssentials plugin) {
        super("general", "signitem", "RHSignItem-Paper-1.21_R9.jar",
                "Signs, locks and renames items (lore signature with signer + date)");
        this.signedKey = new NamespacedKey(plugin, "signitem_signed");
        this.ownerKey = new NamespacedKey(plugin, "signitem_owner");
        this.lockedKey = new NamespacedKey(plugin, "signitem_locked");
        this.lockedByKey = new NamespacedKey(plugin, "signitem_locked-by");
        this.linesKey = new NamespacedKey(plugin, "signitem_lines");
    }

    @Override
    protected void onEnable() {
        ensureDefaultFile("config.yml");
        reloadConfig();
        command("sign", new SignCommand());
        command("rename", new RenameCommand());
        onDisableHook(this::unbindCommands);
    }

    /* ------------------------------------------------------------------ */
    /* config helpers                                                     */
    /* ------------------------------------------------------------------ */

    private List<String> template(boolean shortTemplate) {
        String path = shortTemplate ? "settings.content.SHORT" : "settings.content.DEFAULT";
        List<String> lines = cfgStringList(path);
        if (lines.isEmpty()) {
            return shortTemplate ? List.of(CONTENT_SHORT) : List.of(CONTENT_DEFAULT);
        }
        return lines;
    }

    private String dateFormat() {
        return cfgString("settings.formats.date", "dd/MM/yyyy");
    }

    private String timeFormat() {
        return cfgString("settings.formats.time", "HH:mm:ss");
    }

    private int maxStackSize() {
        int configured = cfgInt("settings.max-stack-size", 1);
        if (configured <= 0 || configured > 64) {
            return 64;
        }
        return configured;
    }

    private boolean soundsEnabled() {
        return cfgBool("settings.enable-sounds", true);
    }

    private boolean vaultEnabled() {
        return cfgBool("settings.vault.enabled", true);
    }

    private double vaultCost() {
        return cfgDouble("settings.vault.cost", 250.0D);
    }

    private boolean vaultAction(String action) {
        return cfgBool("settings.vault.actions." + action, true);
    }

    /* ------------------------------------------------------------------ */
    /* item helpers                                                       */
    /* ------------------------------------------------------------------ */

    private ItemMeta meta(ItemStack item) {
        if (item == null || item.getType().isAir() || item.getAmount() <= 0) {
            return null;
        }
        return item.getItemMeta();
    }

    private String nameOf(String uuid) {
        if (uuid == null) {
            return "?";
        }
        try {
            String name = plugin.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
            return name != null ? name : uuid;
        } catch (IllegalArgumentException e) {
            return uuid;
        }
    }

    private void sound(Player player, boolean success) {
        if (!soundsEnabled()) {
            return;
        }
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,
                success ? 0.4F : 0.4F, success ? 0.9F : 0.6F);
    }

    private void message(CommandSender sender, String key, String... kv) {
        sender.sendMessage(Text.parse(msg(key, kv)));
    }

    /**
     * Vault gate, mirrors {@code VaultMoneyHandler#shouldCancelStatus}. Returns
     * {@code true} when the action must be cancelled because the player cannot pay.
     */
    private boolean cannotAfford(Player player, String action) {
        if (!vaultEnabled() || !vaultAction(action)) {
            return false;
        }
        var economy = economy();
        if (economy == null) {
            return false;
        }
        double cost = vaultCost();
        if (economy.has(player, cost)) {
            return false;
        }
        if (cfgBool("settings.vault.messages.payment-failed", true)) {
            message(player, "vault-withdraw-fail", "cost", formatCost(cost));
        }
        sound(player, false);
        return true;
    }

    /** Vault withdraw, mirrors {@code VaultMoneyHandler#shouldCancelPayment}. */
    private boolean charge(Player player, String action) {
        if (!vaultEnabled() || !vaultAction(action)) {
            return false;
        }
        var economy = economy();
        if (economy == null) {
            return false;
        }
        double cost = vaultCost();
        if (!economy.has(player, cost)) {
            if (cfgBool("settings.vault.messages.payment-failed", true)) {
                message(player, "vault-withdraw-fail", "cost", formatCost(cost));
            }
            sound(player, false);
            return true;
        }
        economy.withdrawPlayer(player, cost);
        if (cfgBool("settings.vault.messages.payment-successful", true)) {
            message(player, "vault-withdraw-success", "cost", formatCost(cost));
        }
        return false;
    }

    private String formatCost(double cost) {
        if (cost == Math.floor(cost)) {
            return String.valueOf((long) cost);
        }
        return String.format(Locale.ROOT, "%.2f", cost);
    }

    private net.milkbowl.vault.economy.Economy economy() {
        var registration = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.economy.Economy.class);
        return registration == null ? null : registration.getProvider();
    }

    private String now(String pattern) {
        try {
            return new SimpleDateFormat(pattern, Locale.ROOT).format(new Date());
        } catch (IllegalArgumentException e) {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ROOT).format(new Date());
        }
    }

    /**
     * Builds the lore lines for a signature. {@code text} is only used by the
     * DEFAULT template ({@code %text%}); multi-line output re-inherits the last
     * colour tag on the following lines, like the original implementation.
     */
    private List<Component> buildLore(Player player, String text, boolean shortTemplate) {
        List<Component> result = new ArrayList<>();
        String lastFormat = "";
        java.util.regex.Pattern formatPattern = java.util.regex.Pattern.compile("(<[^>]+>|&[0-9a-fk-or])");
        for (String templateLine : template(shortTemplate)) {
            String parsed = templateLine
                    .replace("%text%", text == null ? "" : text)
                    .replace("%player%", player.getName())
                    .replace("%date%", now(dateFormat()))
                    .replace("%time%", now(timeFormat()));
            String[] split = parsed.split("\n", -1);
            for (int i = 0; i < split.length; i++) {
                var matcher = formatPattern.matcher(split[i]);
                while (matcher.find()) {
                    lastFormat = matcher.group();
                }
                if (i > 0) {
                    split[i] = lastFormat + split[i];
                }
                result.add(Text.parse(split[i]));
            }
        }
        return result;
    }

    private boolean blacklisted(Material type) {
        String name = type.toString();
        for (String entry : cfgStringList("settings.item-blacklist")) {
            if (entry.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /** {@code offhand-item-mode}: consumes the configured item from the offhand. */
    private boolean consumeOffhand(Player player) {
        if (!cfgBool("settings.offhand-item-mode.enabled", false)) {
            return true;
        }
        String expected = cfgString("settings.offhand-item-mode.type", "GLOW_INK_SAC");
        Material want = Material.matchMaterial(expected == null ? "GLOW_INK_SAC" : expected);
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand.getType().isAir()) {
            message(player, "no-item-in-offhand");
            sound(player, false);
            return false;
        }
        if (want != null && offhand.getType() != want) {
            message(player, "wrong-item-in-offhand", "item", want.name());
            sound(player, false);
            return false;
        }
        if (cfgBool("settings.offhand-item-mode.use-on-action", true)) {
            int durability = offhand.getType().getMaxDurability();
            if (durability > 0) {
                ItemMeta meta = offhand.getItemMeta();
                if (meta instanceof Damageable damageable) {
                    int next = damageable.getDamage() + 1;
                    if (next >= durability) {
                        player.getInventory().setItemInOffHand(null);
                    } else {
                        damageable.setDamage(next);
                        offhand.setItemMeta(meta);
                    }
                }
            } else {
                offhand.setAmount(offhand.getAmount() - 1);
            }
        }
        return true;
    }

    private void autoLock(Player player, ItemMeta meta) {
        if (!cfgBool("settings.auto-lock.enabled", false)) {
            return;
        }
        boolean permissionBased = cfgBool("settings.auto-lock.permission-based", true);
        if (permissionBased && !player.hasPermission("samplese.signitem.autolock")) {
            return;
        }
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(lockedKey, PersistentDataType.BOOLEAN, true);
        data.set(lockedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    /* ------------------------------------------------------------------ */
    /* /sign                                                              */
    /* ------------------------------------------------------------------ */

    private final class SignCommand implements CommandExecutor, TabCompleter {

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                message(sender, "no-console-command");
                return true;
            }
            if (args.length == 0) {
                return sign(player, null, true);
            }
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "delete" -> {
                    return delete(player);
                }
                case "lock" -> {
                    return lock(player);
                }
                case "unlock" -> {
                    return unlock(player);
                }
                case "help" -> {
                    sendHelp(player);
                    return true;
                }
                case "version" -> {
                    player.sendMessage(Text.parse(msg("version", "version", plugin.getPluginMeta().getVersion())));
                    return true;
                }
                default -> {
                    return sign(player, String.join(" ", args), false);
                }
            }
        }

        private void sendHelp(Player player) {
            for (String line : cfgStringList("help-lines")) {
                player.sendMessage(Text.parse(line));
            }
        }

        /** Returns false when the command is blocked by permission/inventory rules. */
        private boolean sign(Player player, String text, boolean shortTemplate) {
            if (cannotAfford(player, "sign")) {
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = meta(item);
            if (meta == null) {
                message(player, "no-item");
                sound(player, false);
                return false;
            }
            if (item.getAmount() > maxStackSize()) {
                message(player, "stack-limit", "stacksize", String.valueOf(maxStackSize()));
                sound(player, false);
                return false;
            }
            if (blacklisted(item.getType())) {
                message(player, "blacklisted");
                sound(player, false);
                return false;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                lockedFeedback(player, data);
                sound(player, false);
                return false;
            }
            if (data.has(signedKey, PersistentDataType.BOOLEAN)) {
                String owner = nameOf(data.get(ownerKey, PersistentDataType.STRING));
                message(player, player.getUniqueId().toString()
                        .equals(data.get(ownerKey, PersistentDataType.STRING))
                        ? "already-signed-own" : "already-signed", "player", owner);
                sound(player, false);
                return false;
            }
            if (!consumeOffhand(player)) {
                return false;
            }
            List<Component> lore = meta.lore();
            List<Component> base = lore == null ? new ArrayList<>() : new ArrayList<>(lore);
            if (!base.isEmpty() && cfgBool("settings.deny-items-with-lore", false)) {
                message(player, "blocked-lore");
                sound(player, false);
                return false;
            }
            List<Component> added = buildLore(player, text, shortTemplate);
            base.addAll(added);
            meta.lore(base);
            if (charge(player, "sign")) {
                return false;
            }
            data.set(signedKey, PersistentDataType.BOOLEAN, true);
            data.set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
            data.set(linesKey, PersistentDataType.INTEGER,
                    data.getOrDefault(linesKey, PersistentDataType.INTEGER, 0) + added.size());
            item.setItemMeta(meta);
            autoLock(player, meta);
            item.setItemMeta(meta);
            message(player, "sign");
            sound(player, true);
            return true;
        }

        private void lockedFeedback(Player player, PersistentDataContainer data) {
            String lockedBy = data.get(lockedByKey, PersistentDataType.STRING);
            message(player, player.getUniqueId().toString().equals(lockedBy) ? "locked-own" : "locked",
                    "player", nameOf(lockedBy));
        }

        private boolean delete(Player player) {
            if (cannotAfford(player, "delete")) {
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = meta(item);
            if (meta == null) {
                message(player, "no-item");
                sound(player, false);
                return false;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                lockedFeedback(player, data);
                sound(player, false);
                return false;
            }
            if (!data.has(signedKey, PersistentDataType.BOOLEAN)) {
                message(player, "not-signed");
                sound(player, false);
                return false;
            }
            String owner = data.get(ownerKey, PersistentDataType.STRING);
            boolean own = player.getUniqueId().toString().equals(owner);
            if (!own && !player.hasPermission("samplese.signitem.admin")) {
                message(player, "wrong-sign-owner", "player", nameOf(owner));
                sound(player, false);
                return false;
            }
            if (!data.has(linesKey, PersistentDataType.INTEGER)) {
                message(player, "not-signed");
                sound(player, false);
                return false;
            }
            if (charge(player, "delete")) {
                return false;
            }
            data.remove(signedKey);
            data.remove(ownerKey);
            List<Component> lore = meta.lore();
            if (lore != null && !lore.isEmpty()) {
                int signatureLines = data.getOrDefault(linesKey, PersistentDataType.INTEGER, 0);
                meta.lore(lore.subList(0, Math.max(0, lore.size() - signatureLines)));
            }
            data.remove(linesKey);
            item.setItemMeta(meta);
            message(player, "delete");
            sound(player, true);
            return true;
        }

        private boolean lock(Player player) {
            if (cannotAfford(player, "lock")) {
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = meta(item);
            if (meta == null) {
                message(player, "no-item");
                sound(player, false);
                return false;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                String lockedBy = data.get(lockedByKey, PersistentDataType.STRING);
                message(player, player.getUniqueId().toString().equals(lockedBy)
                        ? "already-locked-own" : "already-locked", "player", nameOf(lockedBy));
                sound(player, false);
                return false;
            }
            if (charge(player, "lock")) {
                return false;
            }
            data.set(lockedKey, PersistentDataType.BOOLEAN, true);
            data.set(lockedByKey, PersistentDataType.STRING, player.getUniqueId().toString());
            item.setItemMeta(meta);
            message(player, "lock");
            sound(player, true);
            return true;
        }

        private boolean unlock(Player player) {
            if (cannotAfford(player, "unlock")) {
                return false;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = meta(item);
            if (meta == null) {
                message(player, "no-item");
                sound(player, false);
                return false;
            }
            PersistentDataContainer data = meta.getPersistentDataContainer();
            if (!data.has(lockedKey, PersistentDataType.BOOLEAN)) {
                message(player, "not-locked");
                sound(player, false);
                return false;
            }
            String lockedBy = data.get(lockedByKey, PersistentDataType.STRING);
            if (!player.getUniqueId().toString().equals(lockedBy)
                    && !player.hasPermission("samplese.signitem.admin")) {
                message(player, "wrong-lock-owner", "player", nameOf(lockedBy));
                sound(player, false);
                return false;
            }
            if (charge(player, "unlock")) {
                return false;
            }
            data.remove(lockedKey);
            data.remove(lockedByKey);
            item.setItemMeta(meta);
            message(player, "unlock");
            sound(player, true);
            return true;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                          @NotNull String alias, String @NotNull [] args) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>(List.of("delete", "lock", "unlock", "help", "version"));
                String prefix = args[0].toLowerCase(Locale.ROOT);
                options.removeIf(option -> !option.startsWith(prefix));
                return options;
            }
            return List.of();
        }
    }

    private final class RenameCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                 @NotNull String label, String @NotNull [] args) {
            if (!(sender instanceof Player player)) {
                message(sender, "no-console-command");
                return true;
            }
            if (args.length == 0) {
                sendRenameHelp(player);
                return true;
            }
            if (cannotAfford(player, "rename")) {
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = meta(item);
            if (meta == null) {
                message(player, "no-item");
                sound(player, false);
                return true;
            }
            if (charge(player, "rename")) {
                return true;
            }
            meta.displayName(Text.parse(String.join(" ", args))
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
            message(player, "rename");
            sound(player, true);
            return true;
        }

        private void sendRenameHelp(Player player) {
            for (String line : cfgStringList("rename-help-lines")) {
                player.sendMessage(Text.parse(line));
            }
        }
    }
}
