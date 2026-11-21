package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.gson.VectorAdapter;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public final class LegacyMapper {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static LegacyMapper INSTANCE;
   private final ResourceLoader resourceLoader;
   private final Int2ObjectArrayMap<Integer> blockStateToLegacyId4Data = new Int2ObjectArrayMap();
   private final Int2ObjectArrayMap<Integer> extraId4DataToStateId = new Int2ObjectArrayMap();
   private final int[] blockArr = new int[4096];
   private final BiMap<Integer, ItemType> itemMap = HashBiMap.create();
   private final Map<String, String> blockEntries = new HashMap<>();
   private final Map<String, BlockState> stringToBlockMap = new HashMap<>();
   private final Multimap<BlockState, String> blockToStringMap = HashMultimap.create();

   private LegacyMapper() {
      this.resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader();

      try {
         this.loadFromResource();
      } catch (Throwable var2) {
         LOGGER.warn("Failed to load the built-in legacy id registry", var2);
      }
   }

   private void loadFromResource() throws IOException {
      GsonBuilder gsonBuilder = new GsonBuilder();
      gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
      Gson gson = gsonBuilder.disableHtmlEscaping().create();
      URL url = this.resourceLoader.getResource(LegacyMapper.class, "legacy.json");
      if (url == null) {
         throw new IOException("Could not find legacy.json");
      } else {
         String data = Resources.toString(url, Charset.defaultCharset());
         LegacyMapper.LegacyDataFile dataFile = (LegacyMapper.LegacyDataFile)gson.fromJson(data, (new TypeToken<LegacyMapper.LegacyDataFile>() {}).getType());
         DataFixer fixer = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataFixer();
         ParserContext parserContext = new ParserContext();
         parserContext.setPreferringWildcard(false);
         parserContext.setRestricted(false);
         parserContext.setTryLegacy(false);

         for (Entry<String, String> blockEntry : dataFile.blocks.entrySet()) {
            String id = blockEntry.getKey();
            String value = blockEntry.getValue();
            Integer combinedId = this.getCombinedId(blockEntry.getKey());
            this.blockEntries.put(id, value);
            BlockState state = null;

            try {
               state = BlockState.get(null, blockEntry.getValue());
               BlockType type = state.getBlockType();
               if (type.hasProperty(PropertyKey.WATERLOGGED)) {
                  state = state.with(PropertyKey.WATERLOGGED, false);
               }
            } catch (InputParseException var20) {
               BlockFactory blockFactory = WorldEdit.getInstance().getBlockFactory();
               if (fixer != null) {
                  try {
                     String newEntry = fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, value, 1631);
                     state = blockFactory.parseFromInput(newEntry, parserContext).toImmutableState();
                  } catch (InputParseException var19) {
                  }
               }

               if (state == null) {
                  try {
                     state = blockFactory.parseFromInput(value, parserContext).toImmutableState();
                  } catch (InputParseException var18) {
                  }
               }

               if (state == null) {
                  LOGGER.error("Unknown block: {}. Neither the DataFixer nor defaulting worked to recognize this block.", value);
               } else {
                  this.blockToStringMap.put(state, id);
                  this.stringToBlockMap.put(id, state);
               }
            }

            if (state != null) {
               this.blockArr[combinedId] = state.getInternalId();
               this.blockStateToLegacyId4Data.put(state.getInternalId(), combinedId);
               this.blockStateToLegacyId4Data.putIfAbsent(state.getInternalBlockTypeId(), combinedId);
            }
         }

         for (int id = 0; id < 256; id++) {
            int combinedId = id << 4;
            int base = this.blockArr[combinedId];
            if (base != 0) {
               for (int data_ = 0; data_ < 16; combinedId++) {
                  if (this.blockArr[combinedId] == 0) {
                     this.blockArr[combinedId] = base;
                  }

                  data_++;
               }
            }
         }

         for (Entry<String, String> itemEntry : dataFile.items.entrySet()) {
            String idx = itemEntry.getKey();
            String value = itemEntry.getValue();
            ItemType type = ItemTypes.get(value);
            if (type == null && fixer != null) {
               value = fixer.fixUp(DataFixer.FixTypes.ITEM_TYPE, value, 1631);
               type = ItemTypes.get(value);
            }

            if (type == null) {
               LOGGER.error("Unknown item: {}. Neither the DataFixer nor defaulting worked to recognize this item.", value);
            } else {
               try {
                  this.itemMap.put(this.getCombinedId(idx), type);
               } catch (Exception var17) {
               }
            }
         }
      }
   }

   private int getCombinedId(String input) {
      String[] split = input.split(":");
      return (Integer.parseInt(split[0]) << 4) + (split.length == 2 ? Integer.parseInt(split[1]) : 0);
   }

   @Nullable
   public ItemType getItemFromLegacy(int legacyId) {
      return (ItemType)this.itemMap.get(legacyId << 4);
   }

   public ItemType getItemFromLegacy(String input) {
      if (input.startsWith("minecraft:")) {
         input = input.substring(10);
      }

      return (ItemType)this.itemMap.get(this.getCombinedId(input));
   }

   public BlockState getBlockFromLegacy(String input) {
      if (input.startsWith("minecraft:")) {
         input = input.substring(10);
      }

      try {
         return BlockState.getFromInternalId(this.blockArr[this.getCombinedId(input)]);
      } catch (InputParseException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Nullable
   public ItemType getItemFromLegacy(int legacyId, int data) {
      return (ItemType)this.itemMap.get((legacyId << 4) + data);
   }

   @Nullable
   public Integer getLegacyCombined(ItemType itemType) {
      return (Integer)this.itemMap.inverse().get(itemType);
   }

   @Nullable
   public int[] getLegacyFromItem(ItemType itemType) {
      Integer combinedId = this.getLegacyCombined(itemType);
      return combinedId == null ? null : new int[]{combinedId >> 4, combinedId & 15};
   }

   @Nullable
   public BlockState getBlockFromLegacy(int legacyId) {
      return this.getBlock(legacyId << 4);
   }

   @Nullable
   public BlockState getBlockFromLegacyCombinedId(int combinedId) {
      return this.getBlock(combinedId);
   }

   @Nullable
   public BlockState getBlockFromLegacy(int legacyId, int data) {
      return this.getBlock((legacyId << 4) + data);
   }

   private BlockState getBlock(int combinedId) {
      if (combinedId < this.blockArr.length) {
         try {
            int internalId = this.blockArr[combinedId];
            if (internalId == 0) {
               return null;
            }

            try {
               return BlockState.getFromInternalId(internalId);
            } catch (InputParseException var5) {
               var5.printStackTrace();
            }
         } catch (IndexOutOfBoundsException var6) {
            return null;
         }
      }

      Integer extra = (Integer)this.extraId4DataToStateId.get(combinedId);
      if (extra == null) {
         extra = (Integer)this.extraId4DataToStateId.get(combinedId & 4080);
      }

      if (extra != null) {
         try {
            return BlockState.getFromInternalId(extra);
         } catch (InputParseException var4) {
            var4.printStackTrace();
         }
      }

      return null;
   }

   public void register(int id, int data, BlockStateHolder state) {
      Integer combinedId = (id << 4) + data;
      this.extraId4DataToStateId.put(combinedId, state.getInternalId());
      this.blockStateToLegacyId4Data.putIfAbsent(state.getInternalId(), combinedId);
   }

   @Nullable
   public Integer getLegacyCombined(BlockState blockState) {
      Integer result = (Integer)this.blockStateToLegacyId4Data.get(blockState.getInternalId());
      if (result == null) {
         result = (Integer)this.blockStateToLegacyId4Data.get(blockState.getInternalBlockTypeId());
      }

      return result;
   }

   @Nullable
   public Integer getLegacyCombined(BlockType type) {
      return (Integer)this.blockStateToLegacyId4Data.get(type.getDefaultState().getInternalId());
   }

   @Deprecated
   public int[] getLegacyFromBlock(BlockState blockState) {
      Integer combinedId = this.getLegacyCombined(blockState);
      return combinedId == null ? null : new int[]{combinedId >> 4, combinedId & 15};
   }

   public static LegacyMapper getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new LegacyMapper();
      }

      return INSTANCE;
   }

   private static class LegacyDataFile {
      private Map<String, String> blocks;
      private Map<String, String> items;
   }
}
