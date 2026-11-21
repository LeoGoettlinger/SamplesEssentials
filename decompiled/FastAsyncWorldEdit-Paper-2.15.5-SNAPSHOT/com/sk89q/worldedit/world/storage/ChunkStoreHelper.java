package com.sk89q.worldedit.world.storage;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.chunk.AnvilChunk;
import com.sk89q.worldedit.world.chunk.AnvilChunk13;
import com.sk89q.worldedit.world.chunk.AnvilChunk15;
import com.sk89q.worldedit.world.chunk.AnvilChunk16;
import com.sk89q.worldedit.world.chunk.AnvilChunk17;
import com.sk89q.worldedit.world.chunk.AnvilChunk18;
import com.sk89q.worldedit.world.chunk.Chunk;
import com.sk89q.worldedit.world.chunk.OldChunk;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class ChunkStoreHelper {
   public static CompoundTag readCompoundTag(ChunkStoreHelper.ChunkDataInputSupplier input) throws DataException, IOException {
      try (
         InputStream stream = input.openInputStream();
         NBTInputStream nbt = new NBTInputStream(stream);
      ) {
         Tag tag = nbt.readNamedTag().getTag();
         if (!(tag instanceof CompoundTag var4)) {
            throw new ChunkStoreException("CompoundTag expected for chunk; got " + tag.getClass().getName());
         }
      }

      return var4;
   }

   public static Chunk getChunk(CompoundTag rootTag) throws DataException {
      return getChunk(rootTag, () -> null);
   }

   public static Chunk getChunk(CompoundTag rootTag, Supplier<CompoundTag> entitiesTag) throws DataException {
      int dataVersion = rootTag.getInt("DataVersion");
      if (dataVersion == 0) {
         dataVersion = -1;
      }

      Platform platform = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING);
      int currentDataVersion = platform.getDataVersion();
      if ((dataVersion > 0 || hasLevelSections(rootTag)) && dataVersion < currentDataVersion) {
         DataFixer dataFixer = platform.getDataFixer();
         if (dataFixer != null) {
            rootTag = new CompoundTag(dataFixer.fixUp(DataFixer.FixTypes.CHUNK, rootTag.toLinTag(), dataVersion));
            dataVersion = currentDataVersion;
         }
      }

      if (dataVersion >= 2860) {
         return new AnvilChunk18(rootTag, () -> {
            CompoundTag compoundTag = entitiesTag.get();
            return compoundTag == null ? null : compoundTag.toLinTag();
         });
      } else {
         Map<String, Tag<?, ?>> children = rootTag.getValue();
         CompoundTag tag = null;

         for (Entry<String, Tag<?, ?>> entry : children.entrySet()) {
            if (entry.getKey().equals("Level")) {
               if (!(entry.getValue() instanceof CompoundTag)) {
                  throw new ChunkStoreException("CompoundTag expected for 'Level'; got " + entry.getValue().getClass().getName());
               }

               tag = (CompoundTag)entry.getValue();
               break;
            }
         }

         if (tag == null) {
            throw new ChunkStoreException("Missing root 'Level' tag");
         } else if (dataVersion >= 2724) {
            return new AnvilChunk17(tag, entitiesTag);
         } else if (dataVersion >= 2566) {
            return new AnvilChunk16(tag);
         } else if (dataVersion >= 2225) {
            return new AnvilChunk15(tag);
         } else if (dataVersion >= 1519) {
            return new AnvilChunk13(tag);
         } else {
            Map<String, Tag<?, ?>> tags = tag.getValue();
            return (Chunk)(tags.containsKey("Sections") ? new AnvilChunk(tag) : new OldChunk(tag));
         }
      }
   }

   private static boolean hasLevelSections(CompoundTag rootTag) {
      Map<String, Tag<?, ?>> children = rootTag.getValue();
      Tag<?, ?> levelTag = children.get("Level");
      return levelTag instanceof CompoundTag ? ((CompoundTag)levelTag).getValue().containsKey("Sections") : false;
   }

   private ChunkStoreHelper() {
   }

   @FunctionalInterface
   public interface ChunkDataInputSupplier {
      InputStream openInputStream() throws DataException, IOException;
   }
}
