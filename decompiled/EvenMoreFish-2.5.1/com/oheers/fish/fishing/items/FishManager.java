package com.oheers.fish.fishing.items;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.boost.RarityBoostRegistry;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.data.FishRarityKey;
import com.oheers.fish.database.data.UserFishRarityKey;
import com.oheers.fish.database.model.fish.FishStats;
import com.oheers.fish.database.model.user.UserFishStats;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.fishing.items.config.FishConversions;
import com.oheers.fish.fishing.items.config.RarityConversions;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.plugin.PluginDataManager;
import com.oheers.fish.utils.WeightedRandom;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishManager extends AbstractFishManager<IRarity> {
   private static FishManager instance;

   private FishManager() {
   }

   @NonNull
   public static FishManager getInstance() {
      if (instance == null) {
         instance = new FishManager();
      }

      return instance;
   }

   @Override
   protected void performPreLoadConversions() {
      new RarityConversions().performCheck();
      new FishConversions().performCheck();
   }

   @Override
   protected void loadItems() {
      this.loadItemsFromFiles("rarities", this::loadRaritySafely, Rarity::getId, rarity -> this.shouldSkipRarity(rarity, this.getItemMap()));
   }

   @Override
   protected void logLoadedItems() {
      int totalFish = this.getItemMap().values().stream().mapToInt(rarity -> rarity.getOriginalFishList().size()).sum();
      EvenMoreFish.getInstance().getLogger().info(() -> "Loaded FishManager with %d Rarities and %d Fish.".formatted(this.getItemMap().size(), totalFish));
   }

   @Nullable
   @Override
   public IRarity getRarity(@NonNull String rarityName) {
      return this.getItem(rarityName);
   }

   @Nullable
   @Override
   public IFish getFish(@NonNull String rarityName, @NonNull String fishName) {
      IRarity rarity = this.getRarity(rarityName);
      return rarity != null ? rarity.getFish(fishName) : null;
   }

   @Nullable
   @Override
   public IFish getFish(@Nullable ItemStack item) {
      if (item != null && !item.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
         String nameString = holder.getString(NbtKeys.EMF_FISH_NAME.get());
         String playerString = holder.getString(NbtKeys.EMF_FISH_PLAYER.get());
         String rarityString = holder.getString(NbtKeys.EMF_FISH_RARITY.get());
         Float length = holder.getFloat(NbtKeys.EMF_FISH_LENGTH.get());
         Integer randomIndex = holder.getInteger(NbtKeys.EMF_FISH_RANDOM_INDEX.get());
         if (nameString == null) {
            Logging.debug("ItemStack does not have a fish name set. It is not a fish.");
            return null;
         } else if (rarityString == null) {
            Logging.debug("ItemStack does not have a rarity set. It is not a fish.");
            return null;
         } else {
            RarityKey key = RarityKey.of(rarityString, nameString);
            if (key == null) {
               return null;
            } else {
               IFish fish = key.getFish();
               if (randomIndex != null) {
                  fish.getFactory().setRandomIndex(randomIndex);
               }

               fish.setLength(length);
               if (playerString != null) {
                  try {
                     fish.setFisherman(UUID.fromString(playerString));
                  } catch (IllegalArgumentException var11) {
                     fish.setFisherman((OfflinePlayer)null);
                  }
               }

               return fish;
            }
         }
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public IFish getFish(@Nullable Skull skull, @Nullable Player fisher) {
      if (skull == null) {
         return null;
      } else {
         PersistentDataContainer pdc = skull.getPersistentDataContainer();
         String nameString = (String)pdc.get(NbtKeys.EMF_FISH_NAME.get(), PersistentDataType.STRING);
         String playerString = (String)pdc.get(NbtKeys.EMF_FISH_PLAYER.get(), PersistentDataType.STRING);
         String rarityString = (String)pdc.get(NbtKeys.EMF_FISH_RARITY.get(), PersistentDataType.STRING);
         Float lengthFloat = (Float)pdc.get(NbtKeys.EMF_FISH_LENGTH.get(), PersistentDataType.FLOAT);
         Integer randomIndex = (Integer)pdc.get(NbtKeys.EMF_FISH_RANDOM_INDEX.get(), PersistentDataType.INTEGER);
         if (nameString == null) {
            Logging.debug("Skull does not have a fish name set. It is not a fish.");
            return null;
         } else if (rarityString == null) {
            Logging.debug("Skull does not have a rarity set. It is not a fish.");
            return null;
         } else {
            RarityKey key = RarityKey.of(rarityString, nameString);
            if (key == null) {
               return null;
            } else {
               IFish fish = key.getFish();
               fish.setLength(lengthFloat);
               if (randomIndex != null) {
                  fish.getFactory().setRandomIndex(randomIndex);
               }

               if (playerString != null) {
                  try {
                     fish.setFisherman(UUID.fromString(playerString));
                  } catch (IllegalArgumentException var12) {
                     fish.setFisherman((OfflinePlayer)null);
                  }
               } else if (fisher != null) {
                  fish.setFisherman(fisher);
               }

               return fish;
            }
         }
      }
   }

   @Nullable
   @Override
   public IFish getFish(@Nullable Entity itemEntity) {
      return itemEntity instanceof Item item ? this.getFish(item.getItemStack()) : null;
   }

   @Override
   public boolean isFish(@Nullable ItemStack item) {
      return item != null && !item.isEmpty() ? NBTHolder.itemStack(item).hasKey(NbtKeys.EMF_FISH_NAME.get()) : false;
   }

   @Override
   public boolean isFish(@Nullable Skull skull) {
      return skull == null ? false : NBTHolder.skull(skull).hasKey(NbtKeys.EMF_FISH_NAME.get());
   }

   @Override
   public boolean isFish(@Nullable Entity itemEntity) {
      return itemEntity instanceof Item item ? this.isFish(item.getItemStack()) : false;
   }

   @Override
   public void setFishNbt(@NonNull ItemStack item, @NonNull IFish fish) {
      if (!item.isEmpty()) {
         NBTHolder<ItemStack> holder = NBTHolder.itemStack(item);
         this.setFishNbt(holder, fish);
      }
   }

   @Override
   public void setFishNbt(@NonNull Skull skull, @NonNull IFish fish) {
      NBTHolder<Skull> holder = NBTHolder.skull(skull);
      this.setFishNbt(holder, fish);
   }

   @NonNull
   @Override
   public TreeMap<String, IRarity> getRarityMap() {
      return this.getItemMap();
   }

   public IRarity getRandomWeightedRarity(
      Player fisher,
      double boostRate,
      @NonNull Set<IRarity> boostedRarities,
      Set<IRarity> totalRarities,
      @Nullable CustomRod customRod,
      @NonNull RequirementContext requirementContext
   ) {
      List<IRarity> allowedRarities = this.filterByCustomRod(
         this.getAllowedRarities(fisher, boostRate, boostedRarities, totalRarities, requirementContext), customRod
      );
      if (allowedRarities.isEmpty()) {
         EvenMoreFish.getInstance().getLogger().severe("No rarities available for " + (fisher != null ? fisher.getName() : "N/A"));
         return null;
      } else {
         IRarity selected = this.selectRandomRarity(allowedRarities, boostRate, boostedRarities, fisher, requirementContext.getLocation());
         return selected != null && this.isFishingAllowedInCompetition() ? selected : null;
      }
   }

   @Nullable
   public IRarity getWeightedRarity(
      @Nullable Player fisher,
      @NonNull Set<IRarity> totalRarities,
      @NonNull ToDoubleFunction<IRarity> weightFunction,
      @Nullable CustomRod customRod,
      @NonNull RequirementContext requirementContext
   ) {
      List<IRarity> allowedRarities = this.getAvailableRarities(fisher, totalRarities, customRod, requirementContext);
      if (allowedRarities.isEmpty()) {
         return null;
      } else {
         IRarity selected = WeightedRandom.pick(allowedRarities, weightFunction, EvenMoreFish.RANDOM);
         return selected != null && this.isFishingAllowedInCompetition() ? selected : null;
      }
   }

   public IFish getFish(
      IRarity rarity,
      Location location,
      Player player,
      double boostRate,
      List<IFish> boostedFish,
      boolean doRequirementChecks,
      @Nullable Processor<?> processor,
      @Nullable CustomRod customRod,
      @NonNull RequirementContext context
   ) {
      if (rarity == null || rarity.getOriginalFishList().isEmpty()) {
         rarity = this.getRandomWeightedRarity(player, 1.0, Collections.emptySet(), Set.copyOf(this.getItemMap().values()), customRod, context);
         if (rarity == null) {
            return null;
         }
      }

      List<IFish> available = rarity.getFishList()
         .stream()
         .filter(fish -> this.isFishAllowed(fish, boostRate, boostedFish, processor, customRod, context, doRequirementChecks))
         .collect(Collectors.toList());
      if (available.isEmpty()) {
         this.logNoFishAvailable(rarity, location, customRod);
         return null;
      } else {
         IFish selected = this.getRandomWeightedFish(available, boostRate, boostedFish);
         return this.isFishingAllowedInCompetition() ? selected : null;
      }
   }

   @Nullable
   public IFish getWeightedFish(
      @Nullable IRarity rarity,
      @Nullable Location location,
      @Nullable Player player,
      @NonNull ToDoubleFunction<IFish> weightFunction,
      boolean doRequirementChecks,
      @Nullable Processor<?> processor,
      @Nullable CustomRod customRod
   ) {
      if (rarity == null) {
         return null;
      } else {
         List<IFish> available = this.getAvailableFish(rarity, location, player, doRequirementChecks, processor, customRod);
         if (available.isEmpty()) {
            this.logNoFishAvailable(rarity, location, customRod);
            return null;
         } else {
            IFish selected = WeightedRandom.pick(available, weightFunction, EvenMoreFish.RANDOM);
            return this.isFishingAllowedInCompetition() ? selected : null;
         }
      }
   }

   @NonNull
   public List<IRarity> getAvailableRarities(
      @Nullable Player fisher, @NonNull Set<IRarity> totalRarities, @Nullable CustomRod customRod, @NonNull RequirementContext requirementContext
   ) {
      return this.filterByCustomRod(this.getAllowedRarities(fisher, 1.0, Collections.emptySet(), totalRarities, requirementContext), customRod);
   }

   @Nullable
   public IFish getRandomWeightedFish(@NonNull List<IFish> fishList, double boostRate, @Nullable List<IFish> boostedFish) {
      if (fishList.isEmpty()) {
         return null;
      } else {
         ToDoubleFunction<IFish> weightFunction = FishManager::getBaseFishWeight;
         Set<IFish> boostedSet = (Set<IFish>)(boostedFish != null ? new HashSet<>(boostedFish) : Collections.emptySet());
         return WeightedRandom.pick(fishList, weightFunction, boostRate, boostedSet, EvenMoreFish.RANDOM);
      }
   }

   @NonNull
   public List<IFish> getAvailableFish(
      @NonNull IRarity rarity,
      @Nullable Location location,
      @Nullable Player player,
      boolean doRequirementChecks,
      @Nullable Processor<?> processor,
      @Nullable CustomRod customRod
   ) {
      RequirementContext context = new RequirementContext(location != null ? location.getWorld() : null, location, player, null, null, null);
      return rarity.getFishList()
         .stream()
         .filter(fish -> this.isFishAllowedByCustomRod(fish, customRod))
         .filter(fish -> this.isFishAllowedByProcessor(fish, processor))
         .filter(fish -> this.meetsRequirements(fish, doRequirementChecks, context))
         .filter(this::isFishWithinGlobalCatchLimit)
         .filter(fish -> this.isFishWithinPlayerCatchLimit(fish, player))
         .collect(Collectors.toList());
   }

   public static double getBaseFishWeight(@NonNull IFish fish) {
      return fish.getWeight() == 0.0 ? 1.0 : fish.getWeight();
   }

   public static boolean hasRemainingCatches(int catchLimit, int caught) {
      return catchLimit <= 0 || caught < catchLimit;
   }

   private void logNoFishAvailable(IRarity rarity, Location location, CustomRod rod) {
      String biome = location != null && location.getWorld() != null ? location.getWorld().getBiome(location).name() : "unknown biome";
      EvenMoreFish.getInstance()
         .getLogger()
         .warning(
            () -> "No fish available for rarity %s at %s in biome %s (Custom Rod: %b)"
               .formatted(
                  rarity.getId(),
                  location != null ? "x=%.1f,y=%.1f,z=%.1f".formatted(location.getX(), location.getY(), location.getZ()) : "null location",
                  biome,
                  rod != null
               )
         );
   }

   private void setFishNbt(@NonNull NBTHolder<?> holder, @NonNull IFish fish) {
      holder.setAutoSave(false);
      float length = fish.getLength();
      if (!fish.isLengthless()) {
         holder.setFloat(NbtKeys.EMF_FISH_LENGTH.get(), length);
      }

      UUID fisherman = fish.getFishermanUUID();
      if (!fish.hasFishermanDisabled() && fisherman != null) {
         holder.setString(NbtKeys.EMF_FISH_PLAYER.get(), fisherman.toString());
      }

      holder.setString(NbtKeys.EMF_FISH_NAME.get(), fish.getId());
      holder.setString(NbtKeys.EMF_FISH_RARITY.get(), fish.getRarity().getId());
      holder.setInteger(NbtKeys.EMF_FISH_RANDOM_INDEX.get(), fish.getFactory().getRandomIndex());
      holder.save();
   }

   private Rarity loadRaritySafely(File file) throws InvalidConfigurationException {
      EvenMoreFish.getInstance().debug("Loading " + file.getName() + " rarity");
      return new Rarity(file);
   }

   private boolean shouldSkipRarity(IRarity rarity, Map<String, IRarity> rarityMap) {
      if (rarity.isDisabled()) {
         return true;
      } else {
         String id = rarity.getId();
         if (rarityMap.containsKey(id)) {
            EvenMoreFish.getInstance().getLogger().warning("Duplicate rarity ID '" + id + "' found. Skipping.");
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean isFishAllowed(
      IFish fish, double boostRate, List<IFish> boostedFish, Processor<?> processor, CustomRod customRod, RequirementContext context, boolean doRequirements
   ) {
      return this.isFishAllowedByCustomRod(fish, customRod)
         && this.isFishBoosted(fish, boostRate, boostedFish)
         && this.isFishAllowedByProcessor(fish, processor)
         && this.meetsRequirements(fish, doRequirements, context)
         && this.isFishWithinGlobalCatchLimit(fish)
         && this.isFishWithinPlayerCatchLimit(fish, context.getPlayer());
   }

   private boolean isFishWithinGlobalCatchLimit(@NonNull IFish fish) {
      int catchLimit = fish.getGlobalCatchLimit();
      if (catchLimit > 0 && DatabaseUtil.isDatabaseOnline()) {
         PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
         if (!dataManager.isFishStatsPreloaded()) {
            return true;
         } else {
            FishStats stats = dataManager.getFishStatsDataManager().peek(FishRarityKey.of(fish).toString());
            int caught = stats == null ? 0 : stats.getQuantity();
            return hasRemainingCatches(catchLimit, caught);
         }
      } else {
         return true;
      }
   }

   private boolean isFishWithinPlayerCatchLimit(@NonNull IFish fish, @Nullable Player player) {
      int catchLimit = fish.getPlayerCatchLimit();
      if (catchLimit > 0 && DatabaseUtil.isDatabaseOnline() && player != null) {
         PluginDataManager dataManager = EvenMoreFish.getInstance().getPluginDataManager();
         int userId = dataManager.getUserManager().getUserId(player.getUniqueId());
         if (userId == 0) {
            EvenMoreFish.getInstance()
               .getLogger()
               .warning("Cannot check player catch amount because user id could not be resolved for " + player.getUniqueId());
            return true;
         } else if (!dataManager.isUserFishStatsPreloaded(userId)) {
            return true;
         } else {
            UserFishStats stats = dataManager.getUserFishStatsDataManager().peek(UserFishRarityKey.of(userId, fish).toString());
            int caught = stats == null ? 0 : stats.getQuantity();
            return hasRemainingCatches(catchLimit, caught);
         }
      } else {
         return true;
      }
   }

   private boolean isFishAllowedByCustomRod(IFish fish, CustomRod rod) {
      return rod == null || rod.getAllowedFish().isEmpty() || rod.getAllowedFish().contains(fish);
   }

   private boolean isFishBoosted(IFish fish, double boostRate, List<IFish> boostedFish) {
      return boostRate == -1.0 || boostedFish == null || boostedFish.contains(fish);
   }

   private boolean isFishAllowedByProcessor(IFish fish, Processor<?> processor) {
      return processor == null || processor.canUseFish(fish);
   }

   private boolean meetsRequirements(IFish fish, boolean doChecks, RequirementContext context) {
      return !doChecks || fish.getRequirement().check(context);
   }

   private List<IRarity> filterByCustomRod(List<IRarity> rarities, CustomRod rod) {
      return rod != null ? rarities.stream().filter(r -> rod.getAllowedRarities().contains(r)).toList() : rarities;
   }

   private List<IRarity> getAllowedRarities(
      Player fisher, double boostRate, Set<IRarity> boostedRarities, Set<IRarity> totalRarities, @NonNull RequirementContext requirementContext
   ) {
      if (fisher == null) {
         return new ArrayList<>(totalRarities);
      } else {
         String region = FishUtils.getRegionName(fisher.getLocation());
         return this.getItemMap()
            .values()
            .stream()
            .filter(r -> !this.shouldSkipRarity(r, boostRate, boostedRarities, fisher))
            .filter(r -> r.getRequirement().check(requirementContext))
            .flatMap(
               r -> (Stream<? extends IRarity>)Collections.nCopies((int)Math.max(1.0, MainConfig.getInstance().getRegionBoost(region, r.getId())), r).stream()
            )
            .toList();
      }
   }

   private boolean shouldSkipRarity(IRarity rarity, double boostRate, Set<IRarity> boostedRarities, Player fisher) {
      return boostedRarities != null && boostRate == -1.0 && !boostedRarities.contains(rarity)
         || rarity.getPermission() != null && !fisher.hasPermission(rarity.getPermission());
   }

   private boolean isFishingAllowedInCompetition() {
      return CompetitionManager.getInstance().isCompetitionActive() || !MainConfig.getInstance().isFishCatchOnlyInCompetition();
   }

   private IRarity selectRandomRarity(List<IRarity> rarities, double boostRate, Set<IRarity> boosted, @Nullable Player fisher, @Nullable Location location) {
      return WeightedRandom.pick(rarities, this.externallyBoostedWeight(fisher, location), boostRate, boosted, EvenMoreFish.RANDOM);
   }

   private ToDoubleFunction<IRarity> externallyBoostedWeight(@Nullable Player fisher, @Nullable Location location) {
      RarityBoostRegistry boosts = RarityBoostRegistry.getInstance();
      return fisher != null && location != null && !boosts.isEmpty()
         ? rarity -> rarity.getWeight() * boosts.combinedMultiplier(fisher, location, rarity.getId())
         : IRarity::getWeight;
   }
}
