package su.samplecraft.essentials;

import org.bukkit.plugin.java.JavaPlugin;
import su.samplecraft.essentials.config.MainConfig;
import su.samplecraft.essentials.core.SamplesEssentialsCommand;
import su.samplecraft.essentials.lang.Lang;
import su.samplecraft.essentials.module.ModuleManager;
import su.samplecraft.essentials.modules.chat.AnnouncementModule;
import su.samplecraft.essentials.modules.chat.ChatClearModule;
import su.samplecraft.essentials.modules.chat.ChatFormatterModule;
import su.samplecraft.essentials.modules.chat.FancyTagsModule;
import su.samplecraft.essentials.modules.moderation.InventoryAccessModule;
import su.samplecraft.essentials.modules.moderation.MaintenanceModule;
import su.samplecraft.essentials.modules.economy.AuctionModule;
import su.samplecraft.essentials.modules.economy.BountyModule;
import su.samplecraft.essentials.modules.economy.OrdersModule;
import su.samplecraft.essentials.modules.economy.VaultBridgeModule;
import su.samplecraft.essentials.modules.gameplay.AmethystToolsModule;
import su.samplecraft.essentials.modules.gameplay.BiggerEnderChestModule;
import su.samplecraft.essentials.modules.gameplay.DarkDropModule;
import su.samplecraft.essentials.modules.gameplay.DragonEggEnhancementsModule;
import su.samplecraft.essentials.modules.gameplay.GravesModule;
import su.samplecraft.essentials.modules.gameplay.LevelToolsModule;
import su.samplecraft.essentials.modules.gameplay.OpenSitModule;
import su.samplecraft.essentials.modules.gameplay.SpawnElytraModule;
import su.samplecraft.essentials.modules.general.LocalInventoryModule;
import su.samplecraft.essentials.modules.general.RewardsModule;
import su.samplecraft.essentials.modules.general.StarterKitModule;
import su.samplecraft.essentials.modules.moderation.FreezeModule;
import su.samplecraft.essentials.modules.moderation.StaffToolsModule;
import su.samplecraft.essentials.modules.rtp.EzRtpModule;
import su.samplecraft.essentials.modules.social.FriendsModule;
import su.samplecraft.essentials.modules.rtp.PremiumRtpModule;
import su.samplecraft.essentials.modules.rtp.SimpleRtpModule;
import su.samplecraft.essentials.modules.world.ChunkManagerModule;
import su.samplecraft.essentials.modules.world.HomesModule;
import su.samplecraft.essentials.modules.world.SmartSpawnerModule;

/**
 * SamplesEssentials - all-in-one essentials suite for Paper 26.2-26.3.
 * <p>
 * Every plugin from the old server became a toggleable module. Modules are
 * grouped in categorias, each one gets its own folder with a config, and the
 * language files live in {@code plugins/SamplesEssentials/lang/}.
 * <p>
 * Author: SampleCraft
 */
public final class SamplesEssentials extends JavaPlugin {

    private static SamplesEssentials instance;

    private MainConfig mainConfig;
    private Lang lang;
    private ModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;
        long started = System.currentTimeMillis();

        if (!getDataFolder().isDirectory() && !getDataFolder().mkdirs()) {
            getLogger().warning("Could not create plugin data folder " + getDataFolder());
        }

        // The registry has to exist before the config, because the config
        // auto-generates one toggle per registered module.
        moduleManager = new ModuleManager(this);
        registerModules();

        mainConfig = new MainConfig(this);
        mainConfig.load();

        lang = new Lang(this);
        lang.load();

        SamplesEssentialsCommand root = new SamplesEssentialsCommand();
        if (getCommand("samplese") != null) {
            getCommand("samplese").setExecutor(root);
            getCommand("samplese").setTabCompleter(root);
        }

        moduleManager.enableConfiguredModules();

        getLogger().info("SamplesEssentials " + getPluginMeta().getVersion()
                + " enabled " + moduleManager.getActiveCount() + "/" + moduleManager.getTotalCount()
                + " modules in " + (System.currentTimeMillis() - started) + "ms (lang=" + lang.locale() + ")");
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
    }

    private void registerModules() {
        // categoria: chat
        moduleManager.register(new ChatClearModule(this));
        moduleManager.register(new ChatFormatterModule(this));
        moduleManager.register(new FancyTagsModule(this));
        moduleManager.register(new AnnouncementModule(this));
        // categoria: economy
        moduleManager.register(new VaultBridgeModule(this));
        moduleManager.register(new AuctionModule(this));
        moduleManager.register(new BountyModule(this));
        moduleManager.register(new OrdersModule(this));
        // categoria: general
        moduleManager.register(new StarterKitModule(this));
        moduleManager.register(new LocalInventoryModule(this));
        moduleManager.register(new RewardsModule(this));
        // categoria: gameplay
        moduleManager.register(new AmethystToolsModule(this));
        moduleManager.register(new BiggerEnderChestModule(this));
        moduleManager.register(new DarkDropModule(this));
        moduleManager.register(new DragonEggEnhancementsModule(this));
        moduleManager.register(new GravesModule(this));
        moduleManager.register(new LevelToolsModule(this));
        moduleManager.register(new OpenSitModule(this));
        moduleManager.register(new SpawnElytraModule(this));
        // categoria: moderation
        moduleManager.register(new StaffToolsModule(this));
        moduleManager.register(new FreezeModule(this));
        moduleManager.register(new MaintenanceModule(this));
        moduleManager.register(new InventoryAccessModule(this));
        // categoria: rtp
        moduleManager.register(new EzRtpModule(this));
        moduleManager.register(new PremiumRtpModule(this));
        moduleManager.register(new SimpleRtpModule(this));
        // categoria: social
        moduleManager.register(new FriendsModule(this));
        // categoria: world
        moduleManager.register(new ChunkManagerModule(this));
        moduleManager.register(new HomesModule(this));
        moduleManager.register(new SmartSpawnerModule(this));
    }

    public static SamplesEssentials getInstance() {
        return instance;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public Lang getLang() {
        return lang;
    }

    public ModuleManager getModules() {
        return moduleManager;
    }
}
