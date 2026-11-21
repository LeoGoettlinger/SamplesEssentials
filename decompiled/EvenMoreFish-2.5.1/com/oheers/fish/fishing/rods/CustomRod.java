package com.oheers.fish.fishing.rods;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.fishing.rods.ICustomRod;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.nbt.NbtKeys;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.recipe.EMFRecipe;
import com.oheers.fish.recipe.RecipeUtil;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomRod extends ConfigBase implements ICustomRod {
   @NonNull
   private final String id = this.validateId();
   private final List<IRarity> allowedRarities = this.loadAllowedRarities();
   private final List<IFish> allowedFish = this.loadAllowedFish();
   private final EMFRecipe<?> recipe;
   private final ItemFactory factory = ItemFactory.itemFactory(this.getConfig());

   public CustomRod(@NonNull File file) throws InvalidConfigurationException {
      super(file, EvenMoreFish.getInstance(), false);
      this.recipe = this.loadRecipe();
   }

   private String validateId() throws InvalidConfigurationException {
      String id = this.getConfig().getString("id");
      if (id == null) {
         throw new InvalidConfigurationException("CustomRod " + this.getFileName() + " has no configured id.");
      } else {
         return id;
      }
   }

   public List<IRarity> loadAllowedRarities() {
      return "ALL".equalsIgnoreCase(this.getConfig().getString("allowed-rarities"))
         ? FishManager.getInstance().getRarityMap().values().stream().toList()
         : this.getConfig().getStringList("allowed-rarities").stream().map(id -> FishManager.getInstance().getRarity(id)).filter(Objects::nonNull).toList();
   }

   public List<IFish> loadAllowedFish() {
      Section section = this.getConfig().getSection("allowed-fish");
      return section == null ? List.of() : section.getRoutesAsStrings(false).stream().flatMap(rarityId -> {
         IRarity rarity = FishManager.getInstance().getRarity(rarityId);
         if (rarity == null) {
            Logging.warn("Rarity '" + rarityId + "' not found for custom rod '" + this.getId() + "'.");
            return Stream.empty();
         } else if (!this.allowedRarities.contains(rarity)) {
            Logging.warn("Rarity '" + rarityId + "' is not allowed for custom rod '" + this.getId() + "'.");
            return Stream.empty();
         } else {
            return section.getStringList(rarityId).stream().map(rarity::getFish).filter(Objects::nonNull);
         }
      }).toList();
   }

   private EMFRecipe<?> loadRecipe() {
      Section section = this.getConfig().getSection("recipe");
      return section == null ? null : RecipeUtil.getRecipe(section, this.getRecipeKey(), this.create());
   }

   @NonNull
   @Override
   public String getId() {
      return this.id;
   }

   @NonNull
   private NamespacedKey getRecipeKey() {
      return new NamespacedKey(EvenMoreFish.getInstance(), "customrod-" + this.getId());
   }

   @Override
   public boolean isDisabled() {
      return this.getConfig().getBoolean("disabled");
   }

   public ItemFactory getFactory() {
      return this.factory;
   }

   @NonNull
   @Override
   public ItemStack create() {
      ItemStack item = this.factory.createItem();
      NBTHolder.itemStack(item).setString(NbtKeys.EMF_ROD_ID.get(), this.getId());
      return item;
   }

   @NonNull
   @Override
   public List<IRarity> getAllowedRarities() {
      return this.allowedRarities;
   }

   @NonNull
   @Override
   public List<IFish> getAllowedFish() {
      return this.allowedFish;
   }

   @Nullable
   public EMFRecipe<?> getRecipe() {
      return this.recipe;
   }
}
