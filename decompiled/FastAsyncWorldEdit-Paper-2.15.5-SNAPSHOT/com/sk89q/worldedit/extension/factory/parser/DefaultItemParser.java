package com.sk89q.worldedit.extension.factory.parser;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.util.Locale;
import java.util.stream.Stream;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.tree.LinCompoundTag;

public class DefaultItemParser extends InputParser<BaseItem> {
   public DefaultItemParser(WorldEdit worldEdit) {
      super(worldEdit);
   }

   @Override
   public Stream<String> getSuggestions(String input) {
      return SuggestionHelper.getNamespacedRegistrySuggestions(ItemType.REGISTRY, input);
   }

   public BaseItem parseFromInput(String input, ParserContext context) throws InputParseException {
      LinCompoundTag itemNbtData = null;
      BaseItem item = null;
      if (context.isTryingLegacy()) {
         try {
            String[] split = input.split(":");
            if (split.length == 0) {
               throw new InputParseException(Caption.of("worldedit.error.parser.invalid-colon"));
            }

            ItemType itemType;
            if (split.length == 1) {
               itemType = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]));
            } else {
               itemType = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }

            if (itemType != null) {
               item = new BaseItem(itemType);
            }
         } catch (NumberFormatException var12) {
         }
      }

      if (item == null) {
         String nbtString = null;
         int nbtStart = input.indexOf(123);
         String typeString;
         if (nbtStart == -1) {
            typeString = input;
         } else {
            typeString = input.substring(0, nbtStart);
            if (nbtStart + 1 >= input.length()) {
               throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.hanging-lbracket", TextComponent.of(nbtStart)));
            }

            int stateEnd = input.lastIndexOf(125);
            if (stateEnd < 0) {
               throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbracket"));
            }

            nbtString = input.substring(nbtStart);
         }

         ItemType itemTypex;
         if ("hand".equalsIgnoreCase(typeString) || "h".equalsIgnoreCase(typeString)) {
            BaseItemStack heldItem = this.getItemInHand(context.requireActor(), HandSide.MAIN_HAND);
            itemTypex = heldItem.getType();
            itemNbtData = heldItem.getNbt();
         } else if (!"offhand".equalsIgnoreCase(typeString) && !"oh".equalsIgnoreCase(typeString)) {
            itemTypex = ItemTypes.get(typeString.toLowerCase(Locale.ROOT));
         } else {
            BaseItemStack heldItem = this.getItemInHand(context.requireActor(), HandSide.OFF_HAND);
            itemTypex = heldItem.getType();
            itemNbtData = heldItem.getNbt();
         }

         if (itemTypex == null) {
            throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-item", TextComponent.of(input)));
         }

         if (nbtString != null) {
            LinCompoundTag otherTag;
            try {
               otherTag = LinStringIO.readFromStringUsing(nbtString, LinCompoundTag::readFrom);
            } catch (NbtParseException var11) {
               throw new NoMatchException(
                  TranslatableComponent.of("worldedit.error.invalid-nbt", TextComponent.of(input), TextComponent.of(var11.getMessage()))
               );
            }

            if (itemNbtData == null) {
               itemNbtData = otherTag;
            } else {
               itemNbtData = itemNbtData.toBuilder().putAll(otherTag.value()).build();
            }
         }

         item = new BaseItem(itemTypex, itemNbtData == null ? null : LazyReference.computed(itemNbtData));
      }

      return item;
   }

   private BaseItemStack getItemInHand(Actor actor, HandSide handSide) throws InputParseException {
      if (actor instanceof Player) {
         return ((Player)actor).getItemInHand(handSide);
      } else {
         throw new InputParseException(Caption.of("worldedit.error.parser.player-only", TextComponent.of(handSide == HandSide.MAIN_HAND ? "hand" : "offhand")));
      }
   }
}
