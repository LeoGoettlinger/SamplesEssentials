package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.command.SuggestInputParseException;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.math.FastBitSet;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.fastasyncworldedit.core.registry.state.PropertyKeySet;
import com.fastasyncworldedit.core.util.MutableCharSequence;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.world.block.BlanketBaseBlock;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlockMaskBuilder {
   private static final BlockMaskBuilder.Operator GREATER = (a, b) -> a > b;
   private static final BlockMaskBuilder.Operator LESS = (a, b) -> a < b;
   private static final BlockMaskBuilder.Operator EQUAL = (a, b) -> a == b;
   private static final BlockMaskBuilder.Operator EQUAL_OR_NULL = (a, b) -> a == b;
   private static final BlockMaskBuilder.Operator GREATER_EQUAL = (a, b) -> a >= b;
   private static final BlockMaskBuilder.Operator LESS_EQUAL = (a, b) -> a <= b;
   private static final BlockMaskBuilder.Operator NOT = (a, b) -> a != b;
   private static final long[] ALL = new long[0];
   private final long[][] bitSets;
   private final ParserContext context;
   private boolean[] ordinals;
   private boolean optimizedStates = true;

   public BlockMaskBuilder() {
      this(new long[BlockTypes.size()][]);
   }

   public BlockMaskBuilder(ParserContext context) {
      this(new long[BlockTypes.size()][], context);
   }

   protected BlockMaskBuilder(long[][] bitSets) {
      this.bitSets = bitSets;
      this.context = new ParserContext();
   }

   protected BlockMaskBuilder(long[][] bitSets, ParserContext context) {
      this.bitSets = bitSets;
      this.context = context;
   }

   private boolean handleRegex(BlockType blockType, PropertyKey key, String regex, BlockMaskBuilder.FuzzyStateAllowingBuilder builder) {
      Property<Object> property = blockType.getProperty(key);
      if (property == null) {
         return false;
      } else {
         boolean result = false;
         List<Object> values = property.getValues();

         for (int i = 0; i < values.size(); i++) {
            if (values.get(i).toString().matches(regex)) {
               builder.allow(property, i);
               result = true;
            }
         }

         return result;
      }
   }

   private boolean handleOperator(
      BlockType blockType, PropertyKey key, BlockMaskBuilder.Operator operator, CharSequence stringValue, BlockMaskBuilder.FuzzyStateAllowingBuilder builder
   ) {
      Property<Object> property = blockType.getProperty(key);
      if (property == null) {
         return false;
      } else {
         int index = property.getIndexFor(stringValue);
         List<Object> values = property.getValues();
         boolean result = false;

         for (int i = 0; i < values.size(); i++) {
            if (operator.test(index, i)) {
               builder.allow(property, i);
               result = true;
            }
         }

         return result;
      }
   }

   private boolean handleRegexOrOperator(
      BlockType type, PropertyKey key, BlockMaskBuilder.Operator operator, CharSequence value, BlockMaskBuilder.FuzzyStateAllowingBuilder builder
   ) {
      if (!type.hasProperty(key) && operator == EQUAL) {
         return false;
      } else if (value.length() == 0) {
         return false;
      } else {
         return (operator == EQUAL || operator == EQUAL_OR_NULL) && !StringMan.isAlphanumericUnd(value)
            ? this.handleRegex(type, key, value.toString(), builder)
            : this.handleOperator(type, key, operator, value, builder);
      }
   }

   private void add(BlockMaskBuilder.FuzzyStateAllowingBuilder builder) {
      long[] states = this.bitSets[builder.getType().getInternalId()];
      if (states == ALL) {
         this.bitSets[builder.getType().getInternalId()] = states = FastBitSet.create(builder.getType().getMaxStateId() + 1);
         FastBitSet.unsetAll(states);
      }

      this.applyRecursive(0, builder.getType().getInternalId(), builder, states);
   }

   private void applyRecursive(int propertiesIndex, int state, BlockMaskBuilder.FuzzyStateAllowingBuilder builder, long[] states) {
      AbstractProperty<?> current = (AbstractProperty<?>)builder.getType().getProperties().get(propertiesIndex);
      List<?> values = current.getValues();
      if (propertiesIndex + 1 < builder.getType().getProperties().size()) {
         for (int i = 0; i < values.size(); i++) {
            if (builder.allows(current) || builder.allows(current, i)) {
               int newState = current.modifyIndex(state, i);
               this.applyRecursive(propertiesIndex + 1, newState, builder, states);
            }
         }
      } else {
         for (int ix = 0; ix < values.size(); ix++) {
            if (builder.allows(current) || builder.allows(current, ix)) {
               int index = current.modifyIndex(state, ix) >> BlockTypesCache.BIT_OFFSET;
               FastBitSet.set(states, index);
            }
         }
      }
   }

   public BlockMaskBuilder addRegex(final String input) throws InputParseException {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<?> fut = executor.submit(
         () -> {
            if (input.charAt(input.length() - 1) == ']') {
               int propStart = StringMan.findMatchingBracket(input, input.length() - 1);
               if (propStart == -1) {
                  throw new InputParseException(Caption.of("fawe.error.no-block-found", TextComponent.of(input)));
               }

               MutableCharSequence charSequence = MutableCharSequence.getTemporal();
               charSequence.setString(input);
               charSequence.setSubstring(0, propStart);
               List<BlockType> blockTypeList;
               List<BlockMaskBuilder.FuzzyStateAllowingBuilder> builders;
               if (StringMan.isAlphanumericUnd(charSequence)) {
                  BlockType type = BlockTypes.parse(charSequence.toString(), this.context);
                  blockTypeList = Collections.singletonList(type);
                  builders = Collections.singletonList(new BlockMaskBuilder.FuzzyStateAllowingBuilder(type));
                  this.add(type);
               } else {
                  String regex = charSequence.toString();
                  blockTypeList = new ArrayList<>();
                  builders = new ArrayList<>();
                  Pattern pattern = Pattern.compile("(minecraft:)?" + regex);

                  for (BlockType type : BlockTypesCache.values) {
                     if (pattern.matcher(type.id()).find()) {
                        blockTypeList.add(type);
                        builders.add(new BlockMaskBuilder.FuzzyStateAllowingBuilder(type));
                        this.add(type);
                     }
                  }

                  if (blockTypeList.isEmpty()) {
                     throw new InputParseException(Caption.of("fawe.error.no-block-found", TextComponent.of(input)));
                  }
               }

               charSequence.setSubstring(0, 0);
               PropertyKey key = null;
               int length = input.length();
               int last = propStart + 1;
               BlockMaskBuilder.Operator operator = null;

               for (int i = last; i < length; i++) {
                  char c = input.charAt(i);
                  switch (c) {
                     case '!':
                     case '<':
                     case '=':
                     case '>':
                     case '~':
                        charSequence.setSubstring(last, i);
                        boolean extra = input.charAt(i + 1) == '=';
                        if (extra) {
                           i++;
                        }
                        operator = switch (c) {
                           case '!' -> NOT;
                           case '<' -> extra ? LESS_EQUAL : LESS;
                           case '=' -> EQUAL;
                           case '>' -> extra ? GREATER_EQUAL : GREATER;
                           case '~' -> EQUAL_OR_NULL;
                           default -> operator;
                        };
                        if (charSequence.length() > 0 || key == null) {
                           key = PropertyKey.getByName(charSequence);
                           if (key == null) {
                              this.suggest(input, charSequence.toString(), blockTypeList);
                           }
                        }

                        last = i + 1;
                        break;
                     case '(':
                     case '[':
                     case '{':
                        int next = StringMan.findMatchingBracket(input, i);
                        if (next != -1) {
                           i = next;
                        }
                        break;
                     case ',':
                     case ']':
                        charSequence.setSubstring(last, i);
                        if (key == null && (key = PropertyKey.getByName(charSequence)) == null) {
                           this.suggest(input, charSequence.toString(), blockTypeList);
                        }

                        if (operator == null) {
                           throw new SuggestInputParseException(
                              Caption.of("fawe.error.no-operator-for-input", input), () -> Arrays.asList("=", "~", "!", "<", ">", "<=", ">=")
                           );
                        }

                        for (int index = 0; index < blockTypeList.size(); index++) {
                           if (!this.handleRegexOrOperator(blockTypeList.get(index), key, operator, charSequence, builders.get(index))) {
                              blockTypeList.remove(index);
                              builders.remove(index);
                              index--;
                           }
                        }

                        key = null;
                        operator = null;
                        last = i + 1;
                  }
               }

               for (BlockMaskBuilder.FuzzyStateAllowingBuilder builder : builders) {
                  if (builder.allows()) {
                     this.add(builder);
                  }
               }
            } else if (StringMan.isAlphanumericUnd(input)) {
               this.add(BlockTypes.parse(input, this.context));
            } else {
               boolean success = false;

               for (BlockType myType : BlockTypesCache.values) {
                  if (myType.id().matches("(minecraft:)?" + input)) {
                     this.add(myType);
                     success = true;
                  }
               }

               if (!success) {
                  throw new InputParseException(Caption.of("fawe.error.no-block-found", TextComponent.of(input)));
               }
            }
         }
      );

      try {
         fut.get(5L, TimeUnit.MILLISECONDS);
      } catch (ExecutionException var9) {
         if (var9.getCause() instanceof InputParseException) {
            throw (InputParseException)var9.getCause();
         }
      } catch (TimeoutException | InterruptedException var10) {
      } finally {
         executor.shutdown();
      }

      return this;
   }

   private void suggest(String input, String property, Collection<BlockType> finalTypes) throws InputParseException {
      throw new SuggestInputParseException(
         Caption.of("worldedit.error.parser.unknown-property", property, input),
         () -> {
            Set<PropertyKey> keys = PropertyKeySet.empty();
            finalTypes.forEach(t -> t.getProperties().forEach(p -> keys.add(p.getKey())));
            return keys.stream()
               .map(PropertyKey::getName)
               .filter(p -> StringMan.blockStateMatches(property, p))
               .sorted(StringMan.blockStateComparator(property))
               .collect(Collectors.toList());
         }
      );
   }

   public boolean isEmpty() {
      return Arrays.stream(this.bitSets).noneMatch(Objects::nonNull);
   }

   public BlockMaskBuilder addAll() {
      Arrays.fill(this.bitSets, ALL);
      this.reset(true);
      return this;
   }

   private void reset(boolean optimized) {
      this.ordinals = null;
      this.optimizedStates = optimized;
   }

   public BlockMaskBuilder clear() {
      Arrays.fill(this.bitSets, null);
      this.reset(true);
      return this;
   }

   public BlockMaskBuilder remove(BlockType type) {
      this.bitSets[type.getInternalId()] = null;
      return this;
   }

   public <T extends BlockStateHolder<T>> BlockMaskBuilder remove(BlockStateHolder<T> state) {
      BlockType type = state.getBlockType();
      int i = type.getInternalId();
      long[] states = this.bitSets[i];
      if (states != null) {
         if (states == ALL) {
            this.bitSets[i] = states = FastBitSet.create(type.getMaxStateId() + 1);
            Arrays.fill(states, -1L);
         }

         int stateId = state.getInternalPropertiesId();
         FastBitSet.clear(states, stateId);
         this.reset(false);
      }

      return this;
   }

   public BlockMaskBuilder filter(BlockType type) {
      for (int i = 0; i < this.bitSets.length; i++) {
         if (i != type.getInternalId()) {
            this.bitSets[i] = null;
         }
      }

      return this;
   }

   public <T extends BlockStateHolder<T>> BlockMaskBuilder filter(BlockStateHolder<T> state) {
      this.filter(state.getBlockType());
      BlockType type = state.getBlockType();
      int i = type.getInternalId();
      long[] states = this.bitSets[i];
      if (states != null) {
         int stateId = state.getInternalPropertiesId();
         boolean set = true;
         if (states == ALL) {
            this.bitSets[i] = states = FastBitSet.create(type.getMaxStateId() + 1);
         } else {
            set = FastBitSet.get(states, stateId);
            Arrays.fill(states, 0L);
         }

         if (set) {
            FastBitSet.set(states, stateId);
         } else {
            this.bitSets[i] = null;
         }

         this.reset(true);
      }

      return this;
   }

   public BlockMaskBuilder filter(Predicate<BlockType> allow) {
      for (int i = 0; i < this.bitSets.length; i++) {
         BlockType type = BlockTypes.get(i);
         if (!allow.test(type)) {
            this.bitSets[i] = null;
         }
      }

      return this;
   }

   public BlockMaskBuilder add(BlockType type) {
      this.bitSets[type.getInternalId()] = ALL;
      return this;
   }

   public <T extends BlockStateHolder<T>> BlockMaskBuilder add(BlockStateHolder<T> state) {
      BlockType type = state.getBlockType();
      if (state instanceof BlanketBaseBlock) {
         return this.add(type);
      } else {
         int i = type.getInternalId();
         long[] states = this.bitSets[i];
         if (states != ALL) {
            if (states == null) {
               this.bitSets[i] = states = FastBitSet.create(type.getMaxStateId() + 1);
            }

            int stateId = state.getInternalPropertiesId();
            FastBitSet.set(states, stateId);
            this.reset(false);
         }

         return this;
      }
   }

   public <T extends BlockStateHolder<T>> BlockMaskBuilder addBlocks(Collection<T> blocks) {
      for (BlockStateHolder<T> block : blocks) {
         this.add(block);
      }

      return this;
   }

   public BlockMaskBuilder addTypes(Collection<BlockType> blocks) {
      for (BlockType block : blocks) {
         this.add(block);
      }

      return this;
   }

   public <T extends BlockStateHolder<T>> BlockMaskBuilder addBlocks(T... blocks) {
      for (BlockStateHolder<T> block : blocks) {
         this.add(block);
      }

      return this;
   }

   public BlockMaskBuilder addTypes(BlockType... blocks) {
      for (BlockType block : blocks) {
         this.add(block);
      }

      return this;
   }

   public BlockMaskBuilder addAll(Predicate<BlockType> allow) {
      for (int i = 0; i < this.bitSets.length; i++) {
         BlockType type = BlockTypes.get(i);
         if (allow.test(type)) {
            this.bitSets[i] = ALL;
         }
      }

      return this;
   }

   public BlockMaskBuilder optimize() {
      if (!this.optimizedStates) {
         for (int i = 0; i < this.bitSets.length; i++) {
            long[] bitSet = this.bitSets[i];
            if (bitSet != null && bitSet != ALL) {
               BlockType type = BlockTypes.get(i);
               int maxStateId = type.getMaxStateId();
               if (maxStateId == 0) {
                  if (bitSet[0] == 0L) {
                     this.bitSets[i] = null;
                  } else {
                     this.bitSets[i] = ALL;
                  }
               } else {
                  int set = 0;
                  int clear = 0;

                  for (AbstractProperty<?> prop : type.getProperties()) {
                     List<?> values = prop.getValues();

                     for (int j = 0; j < values.size(); j++) {
                        int localI = j << prop.getBitOffset() >> BlockTypesCache.BIT_OFFSET;
                        if (FastBitSet.get(bitSet, localI)) {
                           set++;
                        } else {
                           clear++;
                        }
                     }
                  }

                  if (set == 0) {
                     this.bitSets[i] = null;
                  } else if (clear == 0) {
                     this.bitSets[i] = ALL;
                  }
               }
            }
         }

         this.reset(true);
      }

      return this;
   }

   private boolean[] getOrdinals() {
      if (this.ordinals == null) {
         this.ordinals = new boolean[BlockTypesCache.states.length];

         for (int i = 0; i < BlockTypesCache.values.length; i++) {
            long[] bitSet = this.bitSets[i];
            if (bitSet != null) {
               BlockType type = BlockTypesCache.values[i];
               if (bitSet == ALL) {
                  for (BlockState state : type.getAllStates()) {
                     this.ordinals[state.getOrdinal()] = true;
                  }
               } else {
                  for (BlockState state : type.getAllStates()) {
                     if (FastBitSet.get(bitSet, state.getInternalPropertiesId())) {
                        this.ordinals[state.getOrdinal()] = true;
                     }
                  }
               }
            }
         }
      }

      return this.ordinals;
   }

   public BlockMask build(Extent extent) {
      return new BlockMask(extent, this.getOrdinals());
   }

   private static class FuzzyStateAllowingBuilder {
      private final BlockType type;
      private final Map<Property<?>, List<Integer>> masked = new HashMap<>();

      private FuzzyStateAllowingBuilder(BlockType type) {
         this.type = type;
      }

      private BlockType getType() {
         return this.type;
      }

      private List<Property<?>> getMaskedProperties() {
         return this.masked.entrySet().stream().filter(e -> !e.getValue().isEmpty()).map(Entry::getKey).collect(Collectors.toList());
      }

      private void allow(Property<?> property, int index) {
         Preconditions.checkNotNull(property);
         if (!this.type.hasProperty(property.getKey())) {
            throw new IllegalArgumentException(String.format("Property %s cannot be applied to block type %s", property.getName(), this.type.id()));
         } else {
            this.masked.computeIfAbsent(property, k -> new ArrayList<>()).add(index);
         }
      }

      private boolean allows() {
         return !this.masked.isEmpty() && !this.masked.values().stream().anyMatch(List::isEmpty);
      }

      private boolean allows(Property<?> property) {
         return !this.masked.containsKey(property);
      }

      private boolean allows(Property<?> property, int index) {
         return !this.masked.containsKey(property) ? true : this.masked.get(property).contains(index);
      }
   }

   private interface Operator {
      boolean test(int left, int right);
   }
}
