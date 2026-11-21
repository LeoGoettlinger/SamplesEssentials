package com.oheers.fish.baits;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.baits.IBait;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.EconomyType;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.sort.Sortable;
import com.oheers.fish.baits.configs.BaitFileUpdates;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.baits.model.BaitData;
import com.oheers.fish.baits.model.FishChance;
import com.oheers.fish.baits.model.RarityChance;
import com.oheers.fish.baits.model.WeightModifier;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.recipe.EMFRecipe;
import com.oheers.fish.recipe.RecipeUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BaitHandler extends ConfigBase implements IBait, Sortable {
   @NonNull
   private final String id;
   private BaitData baitData;
   private ItemFactory itemFactory;
   private boolean warnedLegacyFormat;
   private final Logger logger = EvenMoreFish.getInstance().getLogger();
   private final FishManager fishManager;
   private final MainConfig mainConfig;
   private final Economy economy;
   private final EMFRecipe<?> recipe;

   public BaitHandler(@NonNull File file, FishManager fishManager, MainConfig mainConfig) throws InvalidConfigurationException {
      super(file, EvenMoreFish.getInstance(), false);
      BaitFileUpdates.update(this);
      this.fishManager = fishManager;
      this.mainConfig = mainConfig;
      this.id = this.validateAndGetId();
      this.baitData = this.loadBaitData();
      this.economy = this.fetchEconomyInstance();
      this.itemFactory = new BaitItemFactory(this.baitData.id(), this.baitData.rarities(), this.baitData.fish(), this.getConfig()).createFactory();
      this.recipe = this.loadRecipe();
   }

   private String validateAndGetId() throws InvalidConfigurationException {
      String baitId = this.getConfig().getString("id");
      if (baitId == null) {
         this.logger.warning("Rarity invalid: 'id' missing in " + this.getFileName());
         throw new InvalidConfigurationException("An ID has not been found in " + this.getFileName() + ". Please correct this.");
      } else {
         return baitId;
      }
   }

   private Economy fetchEconomyInstance() {
      if (!(this.getPurchasePrice() <= -1.0) && this.getPurchaseQuantity() > 0) {
         List<String> typeStrings = this.getConfig().getStringList("purchase.economy-types");
         if (typeStrings.isEmpty()) {
            return Economy.getInstance();
         } else {
            List<EconomyType> types = typeStrings.stream().map(EMFRegistry.ECONOMY_TYPE::get).filter(Objects::nonNull).toList();
            if (types.isEmpty()) {
               Logging.warn("No valid economy types found for bait: " + this.getId() + ". This bait will not be purchasable.");
               return null;
            } else {
               return Economy.economy(types);
            }
         }
      } else {
         return null;
      }
   }

   @NonNull
   @Override
   public ItemStack create(@NonNull OfflinePlayer player) {
      return this.itemFactory.createItem(player.getUniqueId());
   }

   @NonNull
   @Override
   public ItemStack create() {
      return this.itemFactory.createItem();
   }

   private BaitData loadBaitData() {
      Map<IRarity, WeightModifier> rarityModifiers = this.resolveRarityModifiers();
      Map<IFish, WeightModifier> fishModifiers = this.resolveFishModifiers();
      List<IRarity> rarities = List.copyOf(rarityModifiers.keySet());
      List<IFish> fish = List.copyOf(fishModifiers.keySet());
      return new BaitData(
         this.id,
         this.getConfig().getString("item.displayname", this.id),
         rarities,
         fish,
         rarityModifiers,
         fishModifiers,
         this.getConfig().getBoolean("disabled", false),
         this.getConfig().getBoolean("infinite", false),
         resolveMaxBaits(this.getConfig()),
         this.getConfig().getInt("drop-quantity", 1),
         this.getConfig().getDouble("application-weight", 100.0),
         this.getConfig().getDouble("catch-weight", 100.0),
         this.getConfig().getBoolean("can-be-caught", true),
         this.getConfig().getBoolean("disable-use-alert", false)
      );
   }

   @NonNull
   @Override
   public List<IRarity> getRarities() {
      return this.baitData.rarities();
   }

   @NonNull
   private Map<IRarity, WeightModifier> resolveRarityModifiers() {
      Section rarityModifiers = this.getConfig().getSection("rarity-modifiers");
      if (rarityModifiers != null) {
         return this.parseRarityModifiers(rarityModifiers);
      } else {
         List<String> legacyRarities = this.getConfig().getStringList("rarities");
         if (legacyRarities.isEmpty()) {
            return Map.of();
         } else {
            this.warnLegacyFormat();
            WeightModifier legacyModifier = WeightModifier.multiply(this.mainConfig.getBaitBoostRate());
            Map<IRarity, WeightModifier> resolved = new LinkedHashMap<>();

            for (String rarityName : legacyRarities) {
               IRarity rarity = FishManager.getInstance().getRarity(rarityName);
               if (rarity == null) {
                  this.logger.warning("Invalid rarity '" + rarityName + "' found in bait " + this.getId() + ".");
               } else {
                  resolved.put(rarity, legacyModifier);
               }
            }

            return Map.copyOf(resolved);
         }
      }
   }

   @NonNull
   private Map<IRarity, WeightModifier> parseRarityModifiers(@NonNull Section section) {
      Map<IRarity, WeightModifier> resolved = new LinkedHashMap<>();

      for (String rarityName : section.getRoutesAsStrings(false)) {
         IRarity rarity = FishManager.getInstance().getRarity(rarityName);
         if (rarity == null) {
            this.logger.warning("Invalid rarity '" + rarityName + "' found in bait " + this.getId() + ".");
         } else {
            try {
               resolved.put(rarity, WeightModifier.parse(section.get(rarityName)));
            } catch (IllegalArgumentException var7) {
               this.logger.warning(var7.getMessage());
            }
         }
      }

      return Map.copyOf(resolved);
   }

   @NonNull
   private Map<IFish, WeightModifier> resolveFishModifiers() {
      Section fishModifiers = this.getConfig().getSection("fish-modifiers");
      if (fishModifiers != null) {
         return this.parseFishModifiers(fishModifiers);
      } else {
         Section fishSection = this.getConfig().getSection("fish");
         if (fishSection == null) {
            EvenMoreFish.getInstance().debug("Fish section was null in bait. Returning empty list..");
            return Map.of();
         } else {
            this.warnLegacyFormat();
            Map<IFish, WeightModifier> resolved = new LinkedHashMap<>();
            WeightModifier legacyModifier = WeightModifier.multiply(this.mainConfig.getBaitBoostRate());

            for (String rarityName : fishSection.getRoutesAsStrings(false)) {
               IRarity rarity = FishManager.getInstance().getRarity(rarityName);
               if (rarity == null) {
                  this.logger.warning("Invalid rarity '" + rarityName + "' found in legacy fish config for bait " + this.getId() + ".");
               } else {
                  for (String fishName : this.getConfig().getStringList("fish." + rarityName)) {
                     IFish fish = rarity.getFish(fishName);
                     if (fish == null) {
                        this.logger.warning("Invalid fish '" + fishName + "' found under rarity '" + rarityName + "' in bait " + this.getId() + ".");
                     } else {
                        resolved.put(fish, legacyModifier);
                     }
                  }
               }
            }

            return Map.copyOf(resolved);
         }
      }
   }

   @NonNull
   private Map<IFish, WeightModifier> parseFishModifiers(@NonNull Section section) {
      Map<IFish, WeightModifier> resolved = new LinkedHashMap<>();

      for (String rarityName : section.getRoutesAsStrings(false)) {
         Section raritySection = section.getSection(rarityName);
         if (raritySection == null) {
            this.logger.warning("Invalid fish-modifiers section '" + rarityName + "' in bait " + this.getId() + ".");
         } else {
            IRarity rarity = FishManager.getInstance().getRarity(rarityName);
            if (rarity == null) {
               this.logger.warning("Invalid rarity '" + rarityName + "' found in fish-modifiers for bait " + this.getId() + ".");
            } else {
               for (String fishName : raritySection.getRoutesAsStrings(false)) {
                  IFish fish = rarity.getFish(fishName);
                  if (fish == null) {
                     this.logger.warning("Invalid fish '" + fishName + "' found under rarity '" + rarityName + "' in bait " + this.getId() + ".");
                  } else {
                     try {
                        resolved.put(fish, WeightModifier.parse(raritySection.get(fishName)));
                     } catch (IllegalArgumentException var11) {
                        this.logger.warning(var11.getMessage());
                     }
                  }
               }
            }
         }
      }

      return Map.copyOf(resolved);
   }

   @NonNull
   private List<IFish> getFish() {
      return this.baitData.fish();
   }

   @NonNull
   @Override
   public IFish chooseFish(@NonNull Player player, @NonNull Location location) {
      RequirementContext context = new RequirementContext(player.getWorld(), player.getLocation(), player, null, null, FishingType.VANILLA);
      return this.chooseFish(player, location, context, null);
   }

   @NonNull
   public IFish chooseFish(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext, @Nullable CustomRod customRod) {
      IRarity selectedRarity = this.selectRarityWithModifiers(player, requirementContext, customRod);
      IFish selectedFish = this.selectFishFromRarity(selectedRarity, player, location, customRod);
      this.processBaitUsage(player, selectedRarity, selectedFish);
      return selectedFish;
   }

   @NonNull
   private Map<IRarity, WeightModifier> getRarityModifiers() {
      return this.baitData.rarityModifiers();
   }

   @NonNull
   private Map<IFish, WeightModifier> getFishModifiers() {
      return this.baitData.fishModifiers();
   }

   @Nullable
   private IRarity selectRarityWithModifiers(@NonNull Player player, @NonNull RequirementContext requirementContext, @Nullable CustomRod customRod) {
      return this.fishManager
         .getWeightedRarity(player, Set.copyOf(this.fishManager.getRarityMap().values()), this::getEffectiveRarityWeight, customRod, requirementContext);
   }

   @Nullable
   private IFish selectFishFromRarity(@Nullable IRarity rarity, @NonNull Player player, @NonNull Location location, @Nullable CustomRod customRod) {
      return rarity == null ? null : this.fishManager.getWeightedFish(rarity, location, player, this::getEffectiveFishWeight, true, null, customRod);
   }

   private double getEffectiveRarityWeight(@NonNull IRarity rarity) {
      return this.getRarityModifiers().getOrDefault(rarity, WeightModifier.IDENTITY).apply(rarity.getWeight());
   }

   private double getEffectiveFishWeight(@NonNull IFish fish) {
      return this.getFishModifiers().getOrDefault(fish, WeightModifier.IDENTITY).apply(FishManager.getBaseFishWeight(fish));
   }

   private void processBaitUsage(@NonNull Player player, @Nullable IRarity rarity, @Nullable IFish fish) {
      if (fish != null) {
         fish.setWasBaited(true);
         fish.setFisherman(player);
         if (this.shouldAlertUsage(rarity, fish)) {
            this.alertUsage(player);
         }
      }
   }

   private boolean shouldAlertUsage(@Nullable IRarity rarity, @NonNull IFish fish) {
      return rarity != null && this.hasRarityModifier(rarity) || this.hasFishModifier(fish);
   }

   @Override
   public void handleFish(@NonNull Player player, @NonNull IFish fish, @NonNull ItemStack fishingRod) {
      if (!fish.isWasBaited()) {
         EvenMoreFish.getInstance().debug("Fish: %s was not baited, ignoring..".formatted(FishRarityKey.of(fish)));
      } else {
         EvenMoreFish.getInstance().debug("Fish: %s was baited".formatted(FishRarityKey.of(fish)));
         fish.setFisherman(player);
         if (!this.shouldConsumeBait(fish)) {
            Logging.debug("Bait %s did not modify caught fish %s; leaving bait unchanged.".formatted(this.getId(), FishRarityKey.of(fish)));
         } else {
            try {
               ApplicationResult result = BaitNBTManager.applyBaitedRodNBT(fishingRod, this, -1);
               fishingRod.setItemMeta(result.fishingRod().getItemMeta());
               EvenMoreFish.getInstance().getMetricsManager().incrementBaitsUsed(1);
            } catch (MaxBaitsReachedException | MaxBaitReachedException var5) {
               this.logger.log(Level.WARNING, var5.getMessage());
               player.sendMessage(var5.getConfigMessage().getMessage().getComponentMessage(player));
            } catch (NullPointerException var6) {
               this.logger.log(Level.SEVERE, var6.getMessage(), (Throwable)var6);
            }
         }
      }
   }

   private boolean shouldConsumeBait(@NonNull IFish fish) {
      return shouldConsumeBait(this.getRarityModifiers(), this.getFishModifiers(), fish);
   }

   static boolean shouldConsumeBait(
      @NonNull Map<IRarity, WeightModifier> rarityModifiers, @NonNull Map<IFish, WeightModifier> fishModifiers, @NonNull IFish fish
   ) {
      return rarityModifiers.containsKey(fish.getRarity()) || fishModifiers.containsKey(fish);
   }

   private boolean hasRarityModifier(@NonNull IRarity rarity) {
      return this.getRarityModifiers().containsKey(rarity);
   }

   private boolean hasFishModifier(@NonNull IFish fish) {
      return this.getFishModifiers().containsKey(fish);
   }

   private boolean hasModifiersInRarity(@NonNull IRarity rarity) {
      return this.hasRarityModifier(rarity) || this.getFishModifiers().keySet().stream().anyMatch(fish -> fish.getRarity().equals(rarity));
   }

   private void alertUsage(Player player) {
      if (!this.baitData.disableUseAlert()) {
         EMFMessage message = ConfigMessage.BAIT_USED.getMessage();
         message.setBait(this);
         message.send(player);
      }
   }

   @Override
   public int getIndex() {
      return this.getConfig().getInt("sort-index");
   }

   @Override
   public double getWeight() {
      return 0.0;
   }

   @NonNull
   @Override
   public String getId() {
      return this.id;
   }

   @NonNull
   public EMFSingleMessage getFormat() {
      String format = this.getConfig().getString("format", "<yellow>{name}");
      return EMFSingleMessage.fromString(format);
   }

   @NonNull
   public EMFSingleMessage format(@NonNull String name) {
      EMFSingleMessage message = this.getFormat();
      message.setVariable("{name}", name);
      return message;
   }

   @NonNull
   @Override
   public String getDisplayName() {
      return this.baitData.displayName();
   }

   @Override
   public boolean isSilent() {
      return this.getConfig().getBoolean("silent", false);
   }

   @Override
   public boolean hasCatchRewards() {
      return !this.getConfig().getStringList("catch-event").isEmpty();
   }

   @NonNull
   @Override
   public List<Reward> getCatchRewards() {
      return this.getConfig().getStringList("catch-event").stream().map(this::parseEventPlaceholders).map(Reward::new).toList();
   }

   @Override
   public void reload(@NonNull File configFile) {
      super.reload(configFile);
      if (this.fishManager != null && this.mainConfig != null) {
         this.baitData = this.loadBaitData();
         this.itemFactory = new BaitItemFactory(this.baitData.id(), this.baitData.rarities(), this.baitData.fish(), this.getConfig()).createFactory();
      }
   }

   @Override
   public void reload() {
      super.reload();
      if (this.fishManager != null && this.mainConfig != null) {
         this.baitData = this.loadBaitData();
         this.itemFactory = new BaitItemFactory(this.baitData.id(), this.baitData.rarities(), this.baitData.fish(), this.getConfig()).createFactory();
      }
   }

   public BaitData getBaitData() {
      return this.baitData;
   }

   private void warnLegacyFormat() {
      if (!this.warnedLegacyFormat) {
         this.warnedLegacyFormat = true;
         this.logger
            .warning(
               "Bait file '" + this.getFileName() + "' is using the pre-2.3.0 bait format. Please migrate it to 'rarity-modifiers' and/or 'fish-modifiers'."
            );
      }
   }

   @NonNull
   public List<Component> createDebugMessages(@NonNull Player player, @NonNull RequirementContext requirementContext) {
      return this.createDebugMessages(player, player.getLocation(), requirementContext);
   }

   @NonNull
   public List<Component> createDebugMessages(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext) {
      List<Component> messages = new ArrayList<>();
      List<RarityChance> rarityChances = this.calculateRarityChances(player, location, requirementContext);
      messages.add(Component.text("Bait debug for " + this.getId() + " at " + this.formatLocation(location) + " on " + player.getName()));
      if (rarityChances.isEmpty()) {
         messages.add(Component.text("No eligible rarities matched this player and location."));
         return messages;
      } else {
         messages.add(Component.text("Rarity chances:"));

         for (RarityChance rarityChance : rarityChances) {
            messages.add(
               Component.text(
                  " - %s: %s [base=%s, effective=%s, modifier=%s]"
                     .formatted(
                        rarityChance.rarity().getId(),
                        this.formatPercent(rarityChance.chance()),
                        this.formatNumber(rarityChance.baseWeight()),
                        this.formatNumber(rarityChance.effectiveWeight()),
                        rarityChance.modifier().describe()
                     )
               )
            );
         }

         List<RarityChance> modifiedRarities = rarityChances.stream().filter(rarityChancex -> this.hasModifiersInRarity(rarityChancex.rarity())).toList();
         if (modifiedRarities.isEmpty()) {
            messages.add(Component.text("This bait does not modify any currently eligible rarity or fish."));
            return messages;
         } else {
            messages.add(Component.text("Fish chances in affected rarities:"));

            for (RarityChance rarityChance : modifiedRarities) {
               messages.add(Component.text(" * " + rarityChance.rarity().getId()));

               for (FishChance fishChance : rarityChance.fishChances()) {
                  messages.add(
                     Component.text(
                        "   - %s: overall=%s, in-rarity=%s [base=%s, effective=%s, modifier=%s]"
                           .formatted(
                              fishChance.fish().getId(),
                              this.formatPercent(fishChance.overallChance()),
                              this.formatPercent(fishChance.conditionalChance()),
                              this.formatNumber(fishChance.baseWeight()),
                              this.formatNumber(fishChance.effectiveWeight()),
                              fishChance.modifier().describe()
                           )
                     )
                  );
               }
            }

            return messages;
         }
      }
   }

   @NonNull
   private List<RarityChance> calculateRarityChances(@NonNull Player player, @NonNull Location location, @NonNull RequirementContext requirementContext) {
      List<IRarity> availableRarities = this.fishManager
         .getAvailableRarities(player, Set.copyOf(this.fishManager.getRarityMap().values()), null, requirementContext);
      if (availableRarities.isEmpty()) {
         return List.of();
      } else {
         Map<IRarity, Long> rarityCounts = availableRarities.stream()
            .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
         double totalRarityWeight = rarityCounts.entrySet()
            .stream()
            .mapToDouble(entry -> this.getEffectiveRarityWeight(entry.getKey()) * entry.getValue().longValue())
            .sum();
         return rarityCounts.entrySet()
            .stream()
            .map(entry -> this.buildRarityChance(entry.getKey(), player, location, totalRarityWeight, availableRarities.size(), entry.getValue()))
            .sorted(Comparator.comparingDouble(RarityChance::chance).reversed())
            .toList();
      }
   }

   @NonNull
   private RarityChance buildRarityChance(
      @NonNull IRarity rarity, @NonNull Player player, @NonNull Location location, double totalRarityWeight, int totalCandidateCount, long multiplicity
   ) {
      double baseWeight = rarity.getWeight() * multiplicity;
      WeightModifier rarityModifier = this.getRarityModifiers().getOrDefault(rarity, WeightModifier.IDENTITY);
      double effectiveWeight = rarityModifier.apply(rarity.getWeight()) * multiplicity;
      double rarityChance = totalRarityWeight > 0.0 ? effectiveWeight / totalRarityWeight : (double)multiplicity / totalCandidateCount;
      List<FishChance> fishChances = this.calculateFishChances(rarity, player, location, rarityChance);
      return new RarityChance(rarity, baseWeight, effectiveWeight, rarityChance, rarityModifier, fishChances);
   }

   @NonNull
   private List<FishChance> calculateFishChances(@NonNull IRarity rarity, @NonNull Player player, @NonNull Location location, double rarityChance) {
      List<? extends IFish> availableFish = this.fishManager.getAvailableFish(rarity, location, player, true, null, null);
      if (availableFish.isEmpty()) {
         return List.of();
      } else {
         double totalFishWeight = availableFish.stream().mapToDouble(this::getEffectiveFishWeight).sum();
         return availableFish.stream().map(fish -> {
            double baseWeight = FishManager.getBaseFishWeight(fish);
            WeightModifier modifier = this.getFishModifiers().getOrDefault(fish, WeightModifier.IDENTITY);
            double effectiveWeight = modifier.apply(baseWeight);
            double conditionalChance = totalFishWeight > 0.0 ? effectiveWeight / totalFishWeight : 1.0 / availableFish.size();
            return new FishChance(fish, baseWeight, effectiveWeight, conditionalChance, rarityChance * conditionalChance, modifier);
         }).sorted(Comparator.comparingDouble(FishChance::overallChance).reversed()).toList();
      }
   }

   @NonNull
   private String formatLocation(@NonNull Location location) {
      String world = location.getWorld() != null ? location.getWorld().getName() : "unknown";
      return "%s %.1f %.1f %.1f".formatted(world, location.getX(), location.getY(), location.getZ());
   }

   @NonNull
   private String formatPercent(double value) {
      return String.format(Locale.ROOT, "%.2f%%", value * 100.0);
   }

   @NonNull
   private String formatNumber(double value) {
      return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
   }

   @Override
   public double getPurchasePrice() {
      return this.getConfig().getDouble("purchase.price", -1.0);
   }

   @Override
   public int getPurchaseQuantity() {
      return this.getConfig().getInt("purchase.quantity", 0);
   }

   @Nullable
   @Override
   public Economy getEconomy() {
      return this.economy;
   }

   @Override
   public boolean attemptPurchase(@NonNull Player player) {
      if (this.economy != null && !this.economy.isEmpty()) {
         double price = this.getPurchasePrice();
         if (price <= -1.0) {
            ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
            return false;
         } else {
            int quantity = this.getPurchaseQuantity();
            if (quantity <= 0) {
               ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
               return false;
            } else if (!this.economy.has(player, price)) {
               EMFMessage message = ConfigMessage.BAIT_CANNOT_AFFORD.getMessage();
               message.setVariable("{price}", this.economy.getWorthFormat(price, false));
               message.send(player);
               return false;
            } else {
               this.economy.withdraw(player, price, false);
               ItemStack baitItem = this.create(player);
               int finalQuantity = Math.min(baitItem.getMaxStackSize(), quantity);
               baitItem.setAmount(finalQuantity);
               FishUtils.giveItem(baitItem, player);
               EMFMessage message = ConfigMessage.BAIT_PURCHASED.getMessage();
               message.setAmount(finalQuantity);
               message.setVariable("{price}", this.economy.getWorthFormat(price, false));
               message.setBait(this);
               message.send(player);
               return true;
            }
         }
      } else {
         ConfigMessage.BAIT_NOT_FOR_SALE.getMessage().send(player);
         return false;
      }
   }

   @NonNull
   private NamespacedKey getRecipeKey() {
      return new NamespacedKey(EvenMoreFish.getInstance(), "bait-" + this.getId());
   }

   static int resolveMaxBaits(@NonNull Section config) {
      return config.getInt("max-baits", -1);
   }

   private EMFRecipe<?> loadRecipe() {
      Section section = this.getConfig().getSection("recipe");
      return section == null ? null : RecipeUtil.getRecipe(section, this.getRecipeKey(), this.create());
   }

   @Nullable
   public EMFRecipe<?> getRecipe() {
      return this.recipe;
   }

   private String parseEventPlaceholders(String rewardString) {
      rewardString = rewardString.replace("{displayname}", this.getDisplayName());
      return rewardString.replace("{name}", this.getId());
   }
}
