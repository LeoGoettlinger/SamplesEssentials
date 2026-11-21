package com.viaversion.viabackwards.protocol.v1_13to1_12_2.data;

import com.viaversion.viabackwards.protocol.v1_13to1_12_2.Protocol1_13To1_12_2;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ParticleIdMappings1_12_2 {
   private static final ParticleIdMappings1_12_2.ParticleData[] particles;

   public static ParticleIdMappings1_12_2.ParticleData getMapping(int id) {
      return particles[id];
   }

   private static ParticleIdMappings1_12_2.ParticleData rewrite(int replacementId) {
      return new ParticleIdMappings1_12_2.ParticleData(replacementId);
   }

   private static ParticleIdMappings1_12_2.ParticleData rewrite(int replacementId, ParticleIdMappings1_12_2.ParticleHandler handler) {
      return new ParticleIdMappings1_12_2.ParticleData(replacementId, handler);
   }

   static {
      ParticleIdMappings1_12_2.ParticleHandler blockHandler = new ParticleIdMappings1_12_2.ParticleHandler() {
         @Override
         public int[] rewrite(Protocol1_13To1_12_2 protocol, PacketWrapper wrapper) {
            return this.rewrite((Integer)wrapper.read(Types.VAR_INT));
         }

         @Override
         public int[] rewrite(Protocol1_13To1_12_2 protocol, List<com.viaversion.viaversion.api.minecraft.Particle.ParticleData<?>> data) {
            return this.rewrite((Integer)data.get(0).getValue());
         }

         private int[] rewrite(int newType) {
            int blockType = Protocol1_13To1_12_2.MAPPINGS.getNewBlockStateId(newType);
            int type = blockType >> 4;
            int meta = blockType & 15;
            return new int[]{type + (meta << 12)};
         }

         @Override
         public boolean isBlockHandler() {
            return true;
         }
      };
      particles = new ParticleIdMappings1_12_2.ParticleData[]{
         rewrite(16),
         rewrite(20),
         rewrite(35),
         rewrite(37, blockHandler),
         rewrite(4),
         rewrite(29),
         rewrite(9),
         rewrite(44),
         rewrite(42),
         rewrite(19),
         rewrite(18),
         rewrite(30, new ParticleIdMappings1_12_2.ParticleHandler() {
            @Override
            public int[] rewrite(Protocol1_13To1_12_2 protocol, PacketWrapper wrapper) {
               float r = (Float)wrapper.read(Types.FLOAT);
               float g = (Float)wrapper.read(Types.FLOAT);
               float b = (Float)wrapper.read(Types.FLOAT);
               float scale = (Float)wrapper.read(Types.FLOAT);
               if (r == 0.0) {
                  r = Float.MIN_NORMAL;
               }

               wrapper.set(Types.FLOAT, 3, r);
               wrapper.set(Types.FLOAT, 4, g);
               wrapper.set(Types.FLOAT, 5, b);
               wrapper.set(Types.FLOAT, 6, scale);
               wrapper.set(Types.INT, 1, 0);
               return null;
            }

            @Override
            public int[] rewrite(Protocol1_13To1_12_2 protocol, List<com.viaversion.viaversion.api.minecraft.Particle.ParticleData<?>> data) {
               return null;
            }
         }),
         rewrite(13),
         rewrite(41),
         rewrite(10),
         rewrite(25),
         rewrite(43),
         rewrite(15),
         rewrite(2),
         rewrite(1),
         rewrite(46, blockHandler),
         rewrite(3),
         rewrite(6),
         rewrite(26),
         rewrite(21),
         rewrite(34),
         rewrite(14),
         rewrite(36, new ParticleIdMappings1_12_2.ParticleHandler() {
            @Override
            public int[] rewrite(Protocol1_13To1_12_2 protocol, PacketWrapper wrapper) {
               return this.rewrite(protocol, (Item)wrapper.read(Types.ITEM1_13));
            }

            @Override
            public int[] rewrite(Protocol1_13To1_12_2 protocol, List<com.viaversion.viaversion.api.minecraft.Particle.ParticleData<?>> data) {
               return this.rewrite(protocol, (Item)data.get(0).getValue());
            }

            private int[] rewrite(Protocol1_13To1_12_2 protocol, Item newItem) {
               Item item = protocol.getItemRewriter().handleItemToClient(null, newItem);
               return new int[]{item.identifier(), item.data()};
            }
         }),
         rewrite(33),
         rewrite(31),
         rewrite(12),
         rewrite(27),
         rewrite(22),
         rewrite(23),
         rewrite(0),
         rewrite(24),
         rewrite(39),
         rewrite(11),
         rewrite(48),
         rewrite(12),
         rewrite(45),
         rewrite(47),
         rewrite(7),
         rewrite(5),
         rewrite(17),
         rewrite(4),
         rewrite(4),
         rewrite(4),
         rewrite(18),
         rewrite(18)
      };
   }

   public static final class ParticleData {
      private final int historyId;
      private final ParticleIdMappings1_12_2.ParticleHandler handler;

      private ParticleData(int historyId, ParticleIdMappings1_12_2.ParticleHandler handler) {
         this.historyId = historyId;
         this.handler = handler;
      }

      private ParticleData(int historyId) {
         this(historyId, null);
      }

      @Nullable
      public int[] rewriteData(Protocol1_13To1_12_2 protocol, PacketWrapper wrapper) {
         return this.handler == null ? null : this.handler.rewrite(protocol, wrapper);
      }

      @Nullable
      public int[] rewriteMeta(Protocol1_13To1_12_2 protocol, List<com.viaversion.viaversion.api.minecraft.Particle.ParticleData<?>> data) {
         return this.handler == null ? null : this.handler.rewrite(protocol, data);
      }

      public int getHistoryId() {
         return this.historyId;
      }

      public ParticleIdMappings1_12_2.ParticleHandler getHandler() {
         return this.handler;
      }
   }

   public interface ParticleHandler {
      int[] rewrite(Protocol1_13To1_12_2 var1, PacketWrapper var2);

      int[] rewrite(Protocol1_13To1_12_2 var1, List<com.viaversion.viaversion.api.minecraft.Particle.ParticleData<?>> var2);

      default boolean isBlockHandler() {
         return false;
      }
   }
}
