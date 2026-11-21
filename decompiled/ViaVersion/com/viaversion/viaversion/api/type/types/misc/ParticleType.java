package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.data.FullMappings;
import com.viaversion.viaversion.api.minecraft.Particle;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public class ParticleType extends DynamicType<Particle> {
   public ParticleType() {
      super(Particle.class);
   }

   public void write(ByteBuf buffer, Particle object) {
      Types.VAR_INT.writePrimitive(buffer, object.id());

      for (Particle.ParticleData<?> data : object.getArguments()) {
         data.write(buffer);
      }
   }

   public Particle read(ByteBuf buffer) {
      int type = Types.VAR_INT.readPrimitive(buffer);
      Particle particle = new Particle(type);
      this.readData(buffer, particle);
      return particle;
   }

   @Override
   protected FullMappings mappings(Protocol<?, ?, ?, ?> protocol) {
      return protocol.getMappingData().getParticleMappings();
   }

   public static final class Fillers {
      public static DynamicType<Particle>.DataFiller fill1_13_2(Protocol<?, ?, ?, ?> protocol, ParticleType type, boolean useMappedNames) {
         return type.filler(protocol, useMappedNames)
            .reader("item", ParticleType.Readers.item(protocol.getItemRewriter().mappedItemType()))
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK);
      }

      public static DynamicType<Particle>.DataFiller fill1_17(Protocol<?, ?, ?, ?> protocol, ParticleType type) {
         return fill1_13_2(protocol, type, true)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("vibration", ParticleType.Readers.VIBRATION);
      }

      public static void fill1_18(Protocol<?, ?, ?, ?> protocol, ParticleType type) {
         fill1_17(protocol, type).reader("block_marker", ParticleType.Readers.BLOCK);
      }

      public static void fill1_19(Protocol<?, ?, ?, ?> protocol, ParticleType type) {
         type.filler(protocol)
            .reader("item", ParticleType.Readers.item(protocol.getItemRewriter().mappedItemType()))
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("vibration", ParticleType.Readers.VIBRATION1_19)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);
      }

      public static DynamicType<Particle>.DataFiller fill1_20_3(Protocol<?, ?, ?, ?> protocol, ParticleType type) {
         return type.filler(protocol)
            .reader("item", ParticleType.Readers.item(protocol.getItemRewriter().mappedItemType()))
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK);
      }

      public static void fill1_20_5(Protocol<?, ?, ?, ?> protocol, ParticleType type) {
         fill1_20_3(protocol, type).reader("dust_pillar", ParticleType.Readers.BLOCK).reader("entity_effect", ParticleType.Readers.COLOR);
      }

      public static void fill1_21_2(Protocol<?, ?, ?, ?> protocol) {
         protocol.mappedTypes()
            .particle()
            .filler(protocol)
            .reader("item", ParticleType.Readers.item(protocol.getItemRewriter().mappedItemType()))
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("block_crumble", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST1_21_2)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION1_21_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR)
            .reader("trail", ParticleType.Readers.TRAIL1_21_2);
      }

      public static DynamicType<Particle>.DataFiller fill1_21_4(Protocol<?, ?, ?, ?> protocol) {
         return protocol.mappedTypes()
            .particle()
            .filler(protocol)
            .reader("item", ParticleType.Readers.item(protocol.getItemRewriter().mappedItemTemplateType()))
            .reader("block", ParticleType.Readers.BLOCK)
            .reader("block_marker", ParticleType.Readers.BLOCK)
            .reader("dust_pillar", ParticleType.Readers.BLOCK)
            .reader("falling_dust", ParticleType.Readers.BLOCK)
            .reader("block_crumble", ParticleType.Readers.BLOCK)
            .reader("dust", ParticleType.Readers.DUST1_21_2)
            .reader("dust_color_transition", ParticleType.Readers.DUST_TRANSITION1_21_2)
            .reader("vibration", ParticleType.Readers.VIBRATION1_20_3)
            .reader("sculk_charge", ParticleType.Readers.SCULK_CHARGE)
            .reader("shriek", ParticleType.Readers.SHRIEK)
            .reader("entity_effect", ParticleType.Readers.COLOR)
            .reader("trail", ParticleType.Readers.TRAIL1_21_4);
      }

      public static DynamicType<Particle>.DataFiller fill1_21_5(Protocol<?, ?, ?, ?> protocol) {
         return fill1_21_4(protocol).reader("tinted_leaves", ParticleType.Readers.COLOR);
      }

      public static DynamicType<Particle>.DataFiller fill1_21_9(Protocol<?, ?, ?, ?> protocol) {
         return fill1_21_5(protocol)
            .reader("dragon_breath", ParticleType.Readers.POWER)
            .reader("effect", ParticleType.Readers.SPELL)
            .reader("instant_effect", ParticleType.Readers.SPELL)
            .reader("flash", ParticleType.Readers.COLOR);
      }

      public static void fill26_2(Protocol<?, ?, ?, ?> protocol) {
         fill1_21_9(protocol)
            .reader("geyser", ParticleType.Readers.GEYSER)
            .reader("geyser_base", ParticleType.Readers.GEYSER_BASE)
            .reader("geyser_poof", ParticleType.Readers.GEYSER_BASE)
            .reader("geyser_plume", ParticleType.Readers.GEYSER);
      }
   }

   public static final class Readers {
      public static final DynamicType.DataReader<Particle> BLOCK = (buf, particle) -> particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      public static final DynamicType.DataReader<Particle> ITEM1_13 = item(Types.ITEM1_13);
      public static final DynamicType.DataReader<Particle> ITEM1_13_2 = item(Types.ITEM1_13_2);
      public static final DynamicType.DataReader<Particle> DUST = (buf, particle) -> {
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> DUST1_21_2 = (buf, particle) -> {
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> DUST_TRANSITION = (buf, particle) -> {
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> DUST_TRANSITION1_21_2 = (buf, particle) -> {
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> VIBRATION = (buf, particle) -> {
         particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf));
         String identifier = Types.STRING.read(buf);
         particle.add(Types.STRING, identifier);
         identifier = Key.stripMinecraftNamespace(identifier);
         if (identifier.equals("block")) {
            particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf));
         } else if (identifier.equals("entity")) {
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
         } else {
            Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + identifier);
         }

         particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> VIBRATION1_19 = (buf, particle) -> {
         String identifier = Types.STRING.read(buf);
         particle.add(Types.STRING, identifier);
         identifier = Key.stripMinecraftNamespace(identifier);
         if (identifier.equals("block")) {
            particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf));
         } else if (identifier.equals("entity")) {
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         } else {
            Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + identifier);
         }

         particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> VIBRATION1_20_3 = (buf, particle) -> {
         int sourceTypeId = Types.VAR_INT.readPrimitive(buf);
         particle.add(Types.VAR_INT, sourceTypeId);
         if (sourceTypeId == 0) {
            particle.add(Types.BLOCK_POSITION1_14, Types.BLOCK_POSITION1_14.read(buf));
         } else if (sourceTypeId == 1) {
            particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
            particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
         } else {
            Via.getPlatform().getLogger().warning("Unknown vibration path position source type: " + sourceTypeId);
         }

         particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> SCULK_CHARGE = (buf, particle) -> particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      public static final DynamicType.DataReader<Particle> SHRIEK = (buf, particle) -> particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      public static final DynamicType.DataReader<Particle> COLOR = (buf, particle) -> particle.add(Types.INT, Types.INT.readPrimitive(buf));
      public static final DynamicType.DataReader<Particle> TRAIL1_21_2 = (buf, particle) -> {
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> TRAIL1_21_4 = (buf, particle) -> {
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.DOUBLE, Types.DOUBLE.readPrimitive(buf));
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.VAR_INT, Types.VAR_INT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> POWER = (buf, particle) -> particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      public static final DynamicType.DataReader<Particle> SPELL = (buf, particle) -> {
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> GEYSER_BASE = (buf, particle) -> {
         particle.add(Types.INT, Types.INT.readPrimitive(buf));
         particle.add(Types.FLOAT, Types.FLOAT.readPrimitive(buf));
      };
      public static final DynamicType.DataReader<Particle> GEYSER = (buf, particle) -> particle.add(Types.INT, Types.INT.readPrimitive(buf));

      public static DynamicType.DataReader<Particle> item(Type<Item> item) {
         return (buf, particle) -> particle.add(item, item.read(buf));
      }
   }
}
