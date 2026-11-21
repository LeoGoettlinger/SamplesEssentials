package com.fastasyncworldedit.core.registry.state;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PropertyGroup<G, A> {
   public static final PropertyGroup<Integer, Integer> LEVEL = new PropertyGroup.PropertyGroupBuilder()
      .add(PropertyKey.LEVEL)
      .add(PropertyKey.LAYERS, o -> o << 1, o -> o >> 1)
      .setDefault(15)
      .build();
   private final G defaultValue;
   private final PropertyGroup.PropertyFunction[] states;
   private static final Function VOID_FUNCTION = o -> o;

   private PropertyGroup(PropertyGroup.PropertyFunction[] states, G defaultValue) {
      this.states = states;
      this.defaultValue = defaultValue;
   }

   public <B extends BlockStateHolder<B>> G get(BlockStateHolder<B> state) {
      BlockType type = state.getBlockType();
      PropertyGroup.PropertyFunction func = this.states[type.getInternalId()];
      if (func == null) {
         return this.defaultValue;
      } else {
         Object value = state.getState(func.key);
         return func.getFunc.apply((G)value);
      }
   }

   public <B extends BlockStateHolder<B>> BlockStateHolder<B> set(BlockStateHolder<B> state, A value) {
      BlockType type = state.getBlockType();
      PropertyGroup.PropertyFunction func = this.states[type.getInternalId()];
      if (func != null) {
         value = func.setFunc.apply(value);
         state = state.with(func.key, value);
      }

      return state;
   }

   private static class PropertyFunction<G, A> {
      private final Function<A, A> setFunc;
      private final Function<G, G> getFunc;
      private final Property key;

      public PropertyFunction(Property key, Function<G, G> getProcessor, Function<A, A> setProcessor) {
         this.key = key;
         this.getFunc = getProcessor;
         this.setFunc = setProcessor;
      }
   }

   public static class PropertyGroupBuilder<G, A> {
      private final List<Object[]> funcs = new ArrayList<>();
      private G defaultValue;

      public PropertyGroup.PropertyGroupBuilder add(PropertyKey key) {
         return this.add(key, null, null);
      }

      public PropertyGroup.PropertyGroupBuilder add(PropertyKey key, Function<Object, G> getProcessor, Function<A, A> setProcessor) {
         if (getProcessor == null) {
            getProcessor = PropertyGroup.VOID_FUNCTION;
         }

         if (setProcessor == null) {
            setProcessor = PropertyGroup.VOID_FUNCTION;
         }

         Object[] pf = new Object[]{key, getProcessor, setProcessor};
         this.funcs.add(pf);
         return this;
      }

      public PropertyGroup.PropertyGroupBuilder setDefault(G value) {
         this.defaultValue = value;
         return this;
      }

      public PropertyGroup build() {
         PropertyGroup.PropertyFunction[] states = new PropertyGroup.PropertyFunction[BlockTypes.size()];

         for (BlockType type : BlockTypesCache.values) {
            for (Object[] func : this.funcs) {
               Property prop;
               if ((prop = type.getProperty((PropertyKey)func[0])) != null) {
                  PropertyGroup.PropertyFunction pf = new PropertyGroup.PropertyFunction(prop, (Function<G, G>)func[1], (Function<A, A>)func[2]);
                  states[type.getInternalId()] = pf;
                  break;
               }
            }
         }

         return new PropertyGroup(states, this.defaultValue);
      }
   }
}
