package com.viaversion.viaversion.protocols.v1_12_2to1_13.blockconnections;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.BlockFace;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import java.util.ArrayList;
import java.util.List;

public class ChorusPlantConnectionHandler extends AbstractFenceConnectionHandler {
   private final int endstone = ConnectionData.getId("minecraft:end_stone");

   static List<ConnectionData.ConnectorInitAction> init() {
      List<ConnectionData.ConnectorInitAction> actions = new ArrayList<>(2);
      ChorusPlantConnectionHandler handler = new ChorusPlantConnectionHandler();
      actions.add(handler.getInitAction("minecraft:chorus_plant"));
      actions.add(handler.getExtraAction());
      return actions;
   }

   public ChorusPlantConnectionHandler() {
      super(null);
   }

   private ConnectionData.ConnectorInitAction getExtraAction() {
      return blockData -> {
         if (blockData.getMinecraftKey().equals("minecraft:chorus_flower")) {
            this.getBlockStates().add(blockData.getSavedBlockStateId());
         }
      };
   }

   @Override
   protected byte getStates(WrappedBlockData blockData) {
      byte states = super.getStates(blockData);
      if (blockData.getValue("up").equals("true")) {
         states = (byte)(states | 16);
      }

      if (blockData.getValue("down").equals("true")) {
         states = (byte)(states | 32);
      }

      return states;
   }

   @Override
   protected byte statesSize() {
      return 64;
   }

   @Override
   protected byte getStates(UserConnection user, BlockPosition position) {
      byte states = super.getStates(user, position);
      if (this.connects(BlockFace.TOP, this.getBlockData(user, position.getRelative(BlockFace.TOP)), false)) {
         states = (byte)(states | 16);
      }

      if (this.connects(BlockFace.BOTTOM, this.getBlockData(user, position.getRelative(BlockFace.BOTTOM)), false)) {
         states = (byte)(states | 32);
      }

      return states;
   }

   @Override
   protected boolean connects(BlockFace side, int blockState, boolean pre1_12) {
      return this.getBlockStates().contains(blockState) || side == BlockFace.BOTTOM && blockState == this.endstone;
   }
}
