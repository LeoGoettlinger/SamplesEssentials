package su.nightexpress.nightcore.integration.currency;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightCore;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.CoreConfig;
import su.nightexpress.nightcore.integration.currency.command.CurrencyCommands;
import su.nightexpress.nightcore.integration.currency.impl.EliteMobsCurrency;
import su.nightexpress.nightcore.integration.currency.impl.ExcellentEconomyCurrency;
import su.nightexpress.nightcore.integration.currency.impl.ItemStackCurrency;
import su.nightexpress.nightcore.integration.currency.impl.PlayerPointsCurrency;
import su.nightexpress.nightcore.integration.currency.impl.VaultEconomyCurrency;
import su.nightexpress.nightcore.integration.currency.impl.VotingPluginCurrency;
import su.nightexpress.nightcore.integration.currency.impl.XPLevelsCurrency;
import su.nightexpress.nightcore.integration.currency.listener.CurrencyListener;
import su.nightexpress.nightcore.integration.currency.type.IncompleteCurrency;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.Strings;

public class CurrencyManager extends AbstractManager<NightCore> {
   public static final String FILE_NAME = "currencies.yml";
   private final CurrencyRegistry registry = new CurrencyRegistry();
   private final Map<String, Runnable> pluginProviders = new HashMap<>();
   private FileConfig config;

   public CurrencyManager(@NonNull NightCore plugin) {
      super(plugin);
   }

   @Override
   protected void onLoad() {
      EconomyBridge.register(new EconomyBridgeProvider(this.registry));
      this.config = FileConfig.loadOrExtract(this.plugin, "currencies.yml");
      this.loadProviders();
      this.loadBuiltInCurrencies();
      this.loadItemCurrencies();
      CurrencyCommands.load(this.plugin);
      this.addListener(new CurrencyListener(this.plugin, this));
      this.plugin.runTask(() -> {
         this.pluginProviders.clear();
         this.config.saveChanges();
      });
   }

   @Override
   protected void onShutdown() {
      this.pluginProviders.clear();
      CurrencyCommands.shutdown();
   }

   public void handlePluginLoad(@NonNull String pluginName) {
      Runnable provider = this.pluginProviders.get(pluginName);
      if (provider != null) {
         this.plugin.info(pluginName + " detected! Loading currency...");
         provider.run();
      }
   }

   private void loadProviders() {
      this.addExternalLoader("Vault", () -> this.loadIncompleted(VaultEconomyCurrency::new));
      this.addExternalLoader("PlayerPoints", () -> this.loadIncompleted(PlayerPointsCurrency::new));
      this.addExternalLoader("VotingPlugin", () -> this.loadIncompleted(VotingPluginCurrency::new));
      this.addExternalLoader("EliteMobs", () -> this.loadIncompleted(EliteMobsCurrency::new));
      this.addExternalLoader("CoinsEngine", () -> ExcellentEconomyCurrency.getCurrencies(CurrencyId::forCoinsEngine).forEach(this::register));
      this.addExternalLoader("ExcellentEconomy", () -> {
         ExcellentEconomyCurrency.getCurrencies(CurrencyId::forCoinsEngine).forEach(this::register);
         ExcellentEconomyCurrency.getCurrencies(CurrencyId::forExcellentEconomy).forEach(this::register);
      });
      this.pluginProviders.keySet().forEach(pluginName -> {
         Plugin currencyPlugin = this.plugin.getPluginManager().getPlugin(pluginName);
         if (currencyPlugin != null && currencyPlugin.isEnabled()) {
            this.handlePluginLoad(pluginName);
         }
      });
   }

   public void loadBuiltInCurrencies() {
      this.loadIncompleted(XPLevelsCurrency::new);
   }

   public void loadItemCurrencies() {
      String path = "Items";
      if (!this.config.contains(path)) {
         ItemStackCurrency.createDefaults().forEach(currency -> this.config.set(path + "." + currency.getInternalId(), currency));
      }

      this.config.getSection(path).forEach(sId -> {
         try {
            this.loadIncompleted(() -> ItemStackCurrency.read(this.config, path + "." + sId, sId));
         } catch (IllegalStateException var4) {
            this.plugin.error("Item currency '" + sId + "' not loaded: " + var4.getMessage());
         }
      });
   }

   @Nullable
   public ItemStackCurrency createItemCurrency(@NonNull String name, @NonNull ItemStack itemStack) {
      String id = Strings.filterForVariable(name);
      if (this.registry.contains(id)) {
         return null;
      } else {
         ItemStackCurrency currency = new ItemStackCurrency(id, itemStack);
         this.saveCurrency(currency);
         this.loadIncompleted(() -> currency);
         return currency;
      }
   }

   private void saveCurrency(@NonNull ItemStackCurrency currency) {
      this.config.set("Items." + currency.getInternalId(), currency);
      this.config.save();
   }

   private void addExternalLoader(@NonNull String pluginName, @NonNull Runnable runnable) {
      if (Plugins.isInstalled(pluginName)) {
         this.pluginProviders.put(pluginName, runnable);
      }
   }

   public <T extends IncompleteCurrency> void loadIncompleted(@NonNull Supplier<T> supplier) {
      this.register(
         supplier.get(),
         currency -> {
            String path = currency instanceof ItemStackCurrency ? "Items" : "Currencies";
            CurrencySettings settings = ConfigValue.create(
                  path + "." + currency.getInternalId() + ".Settings", CurrencySettings::load, currency.getDefaultSettings()
               )
               .read(this.config);
            currency.setSettings(settings);
         }
      );
   }

   private void register(@NonNull Currency currency) {
      this.register(currency, null);
   }

   private <T extends Currency> void register(@NonNull T currency, @Nullable Consumer<T> preRegister) {
      String id = currency.getInternalId();
      if (!CoreConfig.ECONOMY_DISABLED_PROVIDERS.get().contains(id)) {
         if (preRegister != null) {
            preRegister.accept(currency);
         }

         this.registry.register(currency);
         this.plugin.info("Currency registered: '" + id + "'.");
      }
   }
}
