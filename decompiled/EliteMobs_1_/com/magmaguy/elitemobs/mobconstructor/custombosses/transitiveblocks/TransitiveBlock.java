package com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks;

import com.magmaguy.elitemobs.config.LegacyValueConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

public class TransitiveBlock {
   private final BlockData blockData;
   private final Vector relativeLocation;

   public boolean isAir() {
      return this.blockData.getMaterial().isAir();
   }

   public TransitiveBlock(BlockData blockData, Vector relativeLocation) {
      this.blockData = blockData;
      this.relativeLocation = relativeLocation;
   }

   public static List<TransitiveBlock> serializeTransitiveBlocks(List<String> deserializedList, String filename) {
      List<TransitiveBlock> transitiveBlocks = new ArrayList<>();

      for (String deserializedString : deserializedList) {
         try {
            String[] elements = deserializedString.split("/");
            String[] vector = elements[0].split(",");
            double x = Double.parseDouble(vector[0]);
            double y = Double.parseDouble(vector[1]);
            double z = Double.parseDouble(vector[2]);
            BlockData blockData = Bukkit.getServer().createBlockData(LegacyValueConverter.parseDeserializedBlocks(elements[1]));
            transitiveBlocks.add(new TransitiveBlock(blockData, new Vector(x, y, z)));
         } catch (Exception var14) {
            Logger.warn("Failed to serialize Transitive Block! Issue with entry " + deserializedString + " in file " + filename);
         }
      }

      return transitiveBlocks;
   }

   @Generated
   public BlockData getBlockData() {
      return this.blockData;
   }

   @Generated
   public Vector getRelativeLocation() {
      return this.relativeLocation;
   }
}
