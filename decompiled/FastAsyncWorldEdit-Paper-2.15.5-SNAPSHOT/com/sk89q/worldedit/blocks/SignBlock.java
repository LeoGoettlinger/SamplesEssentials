package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.gson.GsonUtil;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated
public class SignBlock extends LegacyBaseBlockWrapper {
   private String[] text;
   private static final String EMPTY = "{\"text\":\"\"}";

   public SignBlock(BlockState blockState, String[] text) {
      super(blockState);
      if (text == null) {
         this.text = new String[]{"{\"text\":\"\"}", "{\"text\":\"\"}", "{\"text\":\"\"}", "{\"text\":\"\"}"};
      } else {
         for (int i = 0; i < text.length; i++) {
            if (text[i].isEmpty()) {
               text[i] = "{\"text\":\"\"}";
            } else {
               text[i] = "{\"text\":" + GsonUtil.stringValue(text[i]) + "}";
            }
         }

         this.text = text;
      }
   }

   private boolean isLegacy() {
      int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion();
      return dataVersion < 3463;
   }

   public String[] getText() {
      return this.text;
   }

   public void setText(String[] text) {
      if (text == null) {
         throw new IllegalArgumentException("Can't set null text for a sign");
      } else {
         this.text = text;
      }
   }

   @Deprecated
   @Override
   public boolean hasNbtData() {
      return true;
   }

   @Override
   public String getNbtId() {
      return "minecraft:sign";
   }

   @Deprecated
   @Override
   public CompoundTag getNbtData() {
      Map<String, Tag<?, ?>> values = new HashMap<>();
      if (this.isLegacy()) {
         values.put("Text1", new StringTag(this.text[0]));
         values.put("Text2", new StringTag(this.text[1]));
         values.put("Text3", new StringTag(this.text[2]));
         values.put("Text4", new StringTag(this.text[3]));
      } else {
         ListTag<?, ?> messages = new ListTag(StringTag.class, Arrays.stream(this.text).map(StringTag::new).collect(Collectors.toList()));
         Map<String, Tag<?, ?>> frontTextTag = new HashMap<>();
         frontTextTag.put("messages", messages);
         values.put("front_text", new CompoundTag(frontTextTag));
      }

      return new CompoundTag(values);
   }

   @Deprecated
   @Override
   public void setNbtData(CompoundTag rootTag) {
      if (rootTag != null) {
         Map<String, Tag<?, ?>> values = rootTag.getValue();
         this.text = new String[]{"{\"text\":\"\"}", "{\"text\":\"\"}", "{\"text\":\"\"}", "{\"text\":\"\"}"};
         Tag<?, ?> t = values.get("id");
         if (t instanceof StringTag && ((StringTag)t).getValue().equals(this.getNbtId())) {
            if (this.isLegacy()) {
               t = values.get("Text1");
               if (t instanceof StringTag) {
                  this.text[0] = ((StringTag)t).getValue();
               }

               t = values.get("Text2");
               if (t instanceof StringTag) {
                  this.text[1] = ((StringTag)t).getValue();
               }

               t = values.get("Text3");
               if (t instanceof StringTag) {
                  this.text[2] = ((StringTag)t).getValue();
               }

               t = values.get("Text4");
               if (t instanceof StringTag) {
                  this.text[3] = ((StringTag)t).getValue();
               }
            } else {
               CompoundTag frontTextTag = (CompoundTag)values.get("front_text");
               ListTag<?, ?> messagesTag = frontTextTag.getListTag("messages");

               for (int i = 0; i < messagesTag.getValue().size(); i++) {
                  StringTag tag = (StringTag)messagesTag.getValue().get(i);
                  this.text[i] = tag.getValue();
               }
            }
         } else {
            throw new RuntimeException(String.format("'%s' tile entity expected", this.getNbtId()));
         }
      }
   }
}
