package com.sk89q.worldedit.extension.factory.parser;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.extent.inventory.SlottableBlockBag;
import com.fastasyncworldedit.core.jnbt.JSON2NBT;
import com.fastasyncworldedit.core.jnbt.NBTException;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.limit.PropertyRemap;
import com.fastasyncworldedit.core.util.MathMan;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.world.block.BlanketBaseBlock;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class DefaultBlockParser extends InputParser<BaseBlock> {
   private static final String[] EMPTY_STRING_ARRAY = new String[0];

   public DefaultBlockParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   private static BaseBlock getBlockInHand(Actor actor, HandSide handSide) throws InputParseException {
      if (actor instanceof Player) {
         try {
            return ((Player)actor).getBlockInHand(handSide);
         } catch (NotABlockException var3) {
            throw new InputParseException(var3.getRichMessage());
         } catch (WorldEditException var4) {
            throw new InputParseException(Caption.of("worldedit.error.unknown", var4.getRichMessage()), var4);
         }
      } else {
         throw new InputParseException(Caption.of("worldedit.error.parser.player-only", TextComponent.of(handSide == HandSide.MAIN_HAND ? "hand" : "offhand")));
      }
   }

   public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
      String originalInput = input;
      input = input.replace(";", "|");
      Exception suppressed = null;

      try {
         BaseBlock modified = this.parseLogic(input, context);
         if (modified != null) {
            return modified;
         }
      } catch (Exception var6) {
         suppressed = var6;
      }

      try {
         return this.parseLogic(originalInput, context);
      } catch (Exception var7) {
         if (suppressed != null) {
            var7.addSuppressed(suppressed);
         }

         throw var7;
      }
   }

   private String woolMapper(String string) {
      String var2 = string.toLowerCase(Locale.ROOT);
      switch (var2) {
         case "white":
            return BlockTypes.WHITE_WOOL.id();
         case "black":
            return BlockTypes.BLACK_WOOL.id();
         case "blue":
            return BlockTypes.BLUE_WOOL.id();
         case "brown":
            return BlockTypes.BROWN_WOOL.id();
         case "cyan":
            return BlockTypes.CYAN_WOOL.id();
         case "gray":
         case "grey":
            return BlockTypes.GRAY_WOOL.id();
         case "green":
            return BlockTypes.GREEN_WOOL.id();
         case "light_blue":
         case "lightblue":
            return BlockTypes.LIGHT_BLUE_WOOL.id();
         case "light_gray":
         case "light_grey":
         case "lightgray":
         case "lightgrey":
            return BlockTypes.LIGHT_GRAY_WOOL.id();
         case "lime":
            return BlockTypes.LIME_WOOL.id();
         case "magenta":
            return BlockTypes.MAGENTA_WOOL.id();
         case "orange":
            return BlockTypes.ORANGE_WOOL.id();
         case "pink":
            return BlockTypes.PINK_WOOL.id();
         case "purple":
            return BlockTypes.PURPLE_WOOL.id();
         case "yellow":
            return BlockTypes.YELLOW_WOOL.id();
         case "red":
            return BlockTypes.RED_WOOL.id();
         default:
            return string;
      }
   }

   public static Map<Property<?>, Object> parseProperties(BlockType type, String[] stateProperties, ParserContext context, boolean nullNotError) throws NoMatchException {
      Map<Property<?>, Object> blockStates = new HashMap<>();
      if (context != null && context.getActor() != null && !context.getActor().getLimit().isUnlimited()) {
         for (String input : context.getActor().getLimit().DISALLOWED_BLOCKS) {
            if ((input.indexOf(91) != -1 || input.indexOf(93) != -1) && type.id().equalsIgnoreCase(input.substring(0, input.indexOf(91)))) {
               String[] properties = input.substring(input.indexOf(91) + 1, input.indexOf(93)).split(",");
               Set<String> blocked = Arrays.stream(properties).filter(s -> {
                  for (String in : stateProperties) {
                     if (in.equalsIgnoreCase(s)) {
                        return true;
                     }
                  }

                  return false;
               }).collect(Collectors.toSet());
               if (!blocked.isEmpty()) {
                  throw new DisallowedUsageException(Caption.of("fawe.error.limit.disallowed-property", TextComponent.of(input)));
               }
            }
         }
      }

      if (stateProperties.length > 0) {
         for (String parseableData : stateProperties) {
            try {
               String[] parts = parseableData.split("=");
               if (parts.length != 2) {
                  if (nullNotError) {
                     return null;
                  }

                  throw new InputParseException(Caption.of("worldedit.error.parser.bad-state-format", TextComponent.of(parseableData)));
               }

               Property<Object> propertyKey = (Property<Object>)type.getPropertyMap().get(parts[0]);
               if (propertyKey == null) {
                  if (context != null && context.getActor() != null) {
                     if (nullNotError) {
                        return null;
                     }

                     throw new NoMatchException(Caption.of("worldedit.error.parser.unknown-property", TextComponent.of(parts[0]), TextComponent.of(type.id())));
                  }

                  WorldEdit.logger.debug("Unknown property " + parts[0] + " for block " + type.id());
                  return Maps.newHashMap();
               }

               if (blockStates.containsKey(propertyKey)) {
                  if (nullNotError) {
                     return null;
                  }

                  throw new InputParseException(Caption.of("worldedit.error.parser.duplicate-property", TextComponent.of(parts[0])));
               }

               Object value;
               try {
                  value = propertyKey.getValueFor(parts[1]);
               } catch (IllegalArgumentException var15) {
                  if (nullNotError) {
                     return null;
                  }

                  throw new NoMatchException(
                     Caption.of("worldedit.error.parser.unknown-value", TextComponent.of(parts[1]), TextComponent.of(propertyKey.getName()))
                  );
               }

               if (context != null
                  && context.getActor() != null
                  && !context.getActor().getLimit().isUnlimited()
                  && context.getActor().getLimit().REMAP_PROPERTIES != null
                  && !context.getActor().getLimit().REMAP_PROPERTIES.isEmpty()) {
                  for (PropertyRemap remap : context.getActor().getLimit().REMAP_PROPERTIES) {
                     Object newValue = remap.apply(type, value);
                     if (newValue != value) {
                        value = newValue;
                        break;
                     }
                  }
               }

               blockStates.put(propertyKey, value);
            } catch (DisallowedUsageException | NoMatchException var16) {
               throw var16;
            } catch (Exception var17) {
               if (nullNotError) {
                  return null;
               }

               throw new InputParseException(Caption.of("worldedit.error.parser.bad-state-format", TextComponent.of(parseableData)));
            }
         }
      }

      return blockStates;
   }

   @Override
   public Stream<String> getSuggestions(String input, ParserContext context) {
      int idx = input.lastIndexOf(91);
      if (idx < 0) {
         return SuggestionHelper.getNamespacedRegistrySuggestions(BlockType.REGISTRY, input);
      } else {
         String blockType = input.substring(0, idx);
         BlockType type = BlockTypes.get(blockType.toLowerCase(Locale.ROOT));
         if (type == null) {
            String lowerBlockType = blockType.toLowerCase(Locale.ROOT);
            switch (lowerBlockType) {
               case "hand":
               case "offhand":
                  if (context.getActor() instanceof Player player) {
                     BaseItemStack itemInHand = player.getItemInHand(lowerBlockType.equals("hand") ? HandSide.MAIN_HAND : HandSide.OFF_HAND);
                     if (itemInHand.getType().hasBlockType()) {
                        type = itemInHand.getType().getBlockType();
                     }
                  }
                  break;
               case "pos1":
                  World world = context.getWorld();
                  LocalSession session = context.getSession();
                  if (world != null && session != null) {
                     try {
                        BlockVector3 primaryPosition = session.getRegionSelector(world).getPrimaryPosition();
                        type = world.getBlock(primaryPosition).getBlockType();
                     } catch (IncompleteRegionException var12) {
                     }
                  }
            }

            if (type == null) {
               return Stream.empty();
            }
         }

         String props = input.substring(idx + 1);
         return props.isEmpty()
            ? type.getProperties().stream().map(p -> input + p.getName() + "=")
            : SuggestionHelper.getBlockPropertySuggestions(blockType, type, props);
      }
   }

   @Nonnull
   private BaseBlock parseLogic(String input, ParserContext context) throws InputParseException {
      String[] blockAndExtraData = input.trim().split("(?<!^)\\|");
      blockAndExtraData[0] = this.woolMapper(blockAndExtraData[0]);
      Map<Property<?>, Object> blockStates = new HashMap<>();
      BlockState state = null;
      if (context.isTryingLegacy()) {
         try {
            String[] split = blockAndExtraData[0].split(":", 2);
            if (split.length == 0) {
               throw new InputParseException(Caption.of("worldedit.error.parser.invalid-colon"));
            }

            if (split.length == 1) {
               state = LegacyMapper.getInstance().getBlockFromLegacy(Integer.parseInt(split[0]));
            } else if (MathMan.isInteger(split[0])) {
               int id = Integer.parseInt(split[0]);
               int data = Integer.parseInt(split[1]);
               if (data < 0 || data >= 16) {
                  throw new InputParseException(Caption.of("fawe.error.parser.invalid-data", TextComponent.of(data)));
               }

               state = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
            } else {
               BlockType type = BlockTypes.get(split[0].toLowerCase(Locale.ROOT));
               if (type != null) {
                  int data = Integer.parseInt(split[1]);
                  if (data < 0 || data >= 16) {
                     throw new InputParseException(Caption.of("fawe.error.parser.invalid-data", TextComponent.of(data)));
                  }

                  state = LegacyMapper.getInstance().getBlockFromLegacy(type.getLegacyCombinedId() >> 4, data);
               }
            }
         } catch (NumberFormatException var19) {
         }
      }

      CompoundTag nbt = null;
      if (state == null) {
         String stateString = null;
         int stateStart = blockAndExtraData[0].indexOf(91);
         String typeString;
         if (stateStart == -1) {
            typeString = blockAndExtraData[0];
         } else {
            typeString = blockAndExtraData[0].substring(0, stateStart);
            if (stateStart + 1 >= blockAndExtraData[0].length()) {
               throw new InputParseException(Caption.of("worldedit.error.parser.hanging-lbracket", TextComponent.of(stateStart)));
            }

            int stateEnd = blockAndExtraData[0].lastIndexOf(93);
            if (stateEnd < 0) {
               throw new InputParseException(Caption.of("worldedit.error.parser.missing-rbracket"));
            }

            stateString = blockAndExtraData[0].substring(stateStart + 1, blockAndExtraData[0].length() - 1);
         }

         String[] stateProperties = EMPTY_STRING_ARRAY;
         if (stateString != null) {
            stateProperties = stateString.split(",");
         }

         if (typeString.isEmpty()) {
            throw new InputParseException(Caption.of("worldedit.error.parser.bad-state-format", TextComponent.of(blockAndExtraData[0])));
         }

         if ("hand".equalsIgnoreCase(typeString) || "h".equalsIgnoreCase(typeString)) {
            BaseBlock blockInHand = getBlockInHand(context.requireActor(), HandSide.MAIN_HAND);
            state = blockInHand.toBlockState();
            nbt = blockInHand.getNbtData();
         } else if ("offhand".equalsIgnoreCase(typeString) || "oh".equalsIgnoreCase(typeString)) {
            BaseBlock blockInHand = getBlockInHand(context.requireActor(), HandSide.OFF_HAND);
            state = blockInHand.toBlockState();
            nbt = blockInHand.getNbtData();
         } else if (typeString.matches("pos[0-9]+")) {
            int index = Integer.parseInt(typeString.replaceAll("[a-z]+", ""));
            World world = context.requireWorld();

            BlockVector3 primaryPosition;
            try {
               primaryPosition = context.requireSession().getRegionSelector(world).getVertices().get(index - 1);
            } catch (IncompleteRegionException var18) {
               throw new InputParseException(Caption.of("worldedit.error.incomplete-region"));
            }

            state = world.getBlock(primaryPosition);
            nbt = state.getNbtData();
         } else if (typeString.matches("slot[0-9]+")) {
            int slot = Integer.parseInt(typeString.substring(4)) - 1;
            if (!(context.requireActor() instanceof Player player)) {
               throw new InputParseException(Caption.of("worldedit.command.player-only"));
            }

            if (!(player.getInventoryBlockBag() instanceof SlottableBlockBag slottable)) {
               throw new InputParseException(Caption.of("fawe.error.unsupported"));
            }

            BaseItem item = slottable.getItem(slot);
            if (!item.getType().hasBlockType()) {
               throw new InputParseException(Caption.of("worldedit.error.not-a-block"));
            }

            state = item.getType().getBlockType().getDefaultState();
            nbt = item.getNbtData();
         } else {
            BlockType type = BlockTypes.parse(typeString.toLowerCase(Locale.ROOT), context);
            if (type != null) {
               state = type.getDefaultState();
            }

            if (state == null) {
               throw new NoMatchException(Caption.of("fawe.error.invalid-block-type", TextComponent.of(input)));
            }

            nbt = state.getNbtData();
         }

         blockStates.putAll(parseProperties(state.getBlockType(), stateProperties, context, false));
         if (context.isPreferringWildcard()) {
            if (stateString != null && !stateString.isEmpty()) {
               FuzzyBlockState.Builder fuzzyBuilder = FuzzyBlockState.builder();
               fuzzyBuilder.type(state.getBlockType());

               for (Entry<Property<?>, Object> blockState : blockStates.entrySet()) {
                  Property<Object> objProp = (Property<Object>)blockState.getKey();
                  fuzzyBuilder.withProperty(objProp, blockState.getValue());
               }

               state = fuzzyBuilder.build();
            } else {
               state = new FuzzyBlockState(state);
            }
         } else {
            for (Entry<Property<?>, Object> blockState : blockStates.entrySet()) {
               Property<Object> objProp = (Property<Object>)blockState.getKey();
               state = state.with(objProp, blockState.getValue());
            }
         }
      }

      if (state == null) {
         throw new NoMatchException(Caption.of("worldedit.error.unknown-block", TextComponent.of(input)));
      } else {
         if (blockAndExtraData.length > 1 && blockAndExtraData[1].startsWith("{")) {
            String joined = StringMan.join(Arrays.copyOfRange(blockAndExtraData, 1, blockAndExtraData.length), "|");

            try {
               nbt = JSON2NBT.getTagFromJson(joined);
            } catch (NBTException var17) {
               throw new NoMatchException(TextComponent.of(var17.getMessage()));
            }
         }

         BlockType blockType = state.getBlockType();
         if (context.isRestricted()) {
            Actor actor = context.requireActor();
            if (actor != null) {
               if (!actor.hasPermission("worldedit.anyblock")
                  && this.worldEdit.getConfiguration().disallowedBlocks.contains(blockType.id().toLowerCase(Locale.ROOT))) {
                  throw new DisallowedUsageException(Caption.of("worldedit.error.disallowed-block", TextComponent.of(blockType.id())));
               }

               FaweLimit limit = actor.getLimit();
               if (!limit.isUnlimited() && limit.DISALLOWED_BLOCKS.contains(blockType.id().toLowerCase(Locale.ROOT))) {
                  throw new DisallowedUsageException(Caption.of("fawe.error.limit.disallowed-block", TextComponent.of(blockType.id())));
               }
            }
         }

         boolean allowWorkingDefault = nbt != null && (context.getActor() == null || context.getActor().hasPermission("worldedit.anyblock.nbt"));
         if (!DeprecationUtil.isSign(blockType) || blockAndExtraData.length <= 1 && !allowWorkingDefault) {
            if (blockType != BlockTypes.SPAWNER || blockAndExtraData.length <= 1 && !allowWorkingDefault) {
               if ((blockType == BlockTypes.PLAYER_HEAD || blockType == BlockTypes.PLAYER_WALL_HEAD) && (blockAndExtraData.length > 1 || allowWorkingDefault)) {
                  if (blockAndExtraData.length == 1) {
                     return this.validate(context, new SkullBlock(state));
                  } else {
                     String typex = blockAndExtraData[1];
                     return this.validate(context, new SkullBlock(state, typex.replace(" ", "_")));
                  }
               } else {
                  if (nbt == null) {
                     nbt = state.getNbtData();
                  }

                  BaseBlock result;
                  if (nbt != null) {
                     result = (BaseBlock)(!blockStates.isEmpty() ? state.toBaseBlock(nbt) : new BlanketBaseBlock(state, nbt));
                  } else {
                     result = !blockStates.isEmpty() ? new BaseBlock(state) : state.toBaseBlock();
                  }

                  return this.validate(context, result);
               }
            } else {
               String mobName;
               if (blockAndExtraData.length > 1) {
                  mobName = blockAndExtraData[1];
                  EntityType ent = EntityTypes.get(mobName.toLowerCase(Locale.ROOT));
                  if (ent == null) {
                     throw new NoMatchException(Caption.of("worldedit.error.unknown-entity", TextComponent.of(mobName)));
                  }

                  mobName = ent.id();
                  if (!this.worldEdit.getPlatformManager().queryCapability(Capability.USER_COMMANDS).isValidMobType(mobName)) {
                     throw new NoMatchException(Caption.of("worldedit.error.unknown-mob", TextComponent.of(mobName)));
                  }
               } else {
                  mobName = EntityTypes.PIG.id();
               }

               return this.validate(context, new MobSpawnerBlock(state, mobName));
            }
         } else {
            String[] text = new String[]{
               blockAndExtraData.length > 1 ? blockAndExtraData[1] : "",
               blockAndExtraData.length > 2 ? blockAndExtraData[2] : "",
               blockAndExtraData.length > 3 ? blockAndExtraData[3] : "",
               blockAndExtraData.length > 4 ? blockAndExtraData[4] : ""
            };
            return this.validate(context, new SignBlock(state, text));
         }
      }
   }

   private <T extends BlockStateHolder> T validate(ParserContext context, T holder) {
      if (context.isRestricted()) {
         Actor actor = context.requireActor();
         if (!actor.hasPermission("worldedit.anyblock") && this.worldEdit.getConfiguration().checkDisallowedBlocks(holder)) {
            throw new DisallowedUsageException(Caption.of("worldedit.error.disallowed-block", TextComponent.of(String.valueOf(holder))));
         }

         CompoundTag nbt = holder.getNbtData();
         if (nbt != null) {
            if (actor.hasPermission("worldedit.anyblock.nbt")) {
               return holder;
            }

            if (!nbt.equals(holder.getBlockType().getDefaultState().getNbtData())) {
               throw new DisallowedUsageException(Caption.of("fawe.error.nbt.forbidden", TextComponent.of("worldedit.anyblock.nbt")));
            }

            if (!actor.hasPermission("worldedit.anyblock.default-nbt")) {
               throw new DisallowedUsageException(Caption.of("fawe.error.nbt.forbidden", TextComponent.of("worldedit.anyblock.default-nbt")));
            }
         }
      }

      return holder;
   }
}
