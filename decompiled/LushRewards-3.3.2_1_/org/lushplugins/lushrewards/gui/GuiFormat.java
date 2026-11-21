package org.lushplugins.lushrewards.gui;

import com.google.common.collect.TreeMultimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.lushplugins.lushrewards.LushRewards;

public class GuiFormat {
   private final String title;
   private final GuiFormat.GuiTemplate template;

   public GuiFormat(String title, GuiFormat.GuiTemplate template) {
      this.title = title;
      this.template = template;
   }

   public String getTitle() {
      return this.title;
   }

   public GuiFormat.GuiTemplate getTemplate() {
      return this.template;
   }

   public static class GuiTemplate {
      private final List<String> rows;
      public static final GuiFormat.GuiTemplate DEFAULT = new GuiFormat.GuiTemplate(
         new String[]{"RRRRRRRRR", "RRRRRRRRR", "RRRRRRRRR", "RRRR#####", "#########", "####P####"}
      );
      public static final GuiFormat.GuiTemplate COMPACT = new GuiFormat.GuiTemplate(new String[]{"RRRRRRR N"});
      public static final GuiFormat.GuiTemplate COMPACT_PROFILE = new GuiFormat.GuiTemplate(new String[]{"RRRRRRR P"});
      public static final GuiFormat.GuiTemplate BORDERED = new GuiFormat.GuiTemplate(new String[]{"#########", "RRRRRRR#N", "#########"});
      public static final GuiFormat.GuiTemplate BORDERED_LARGE = new GuiFormat.GuiTemplate(
         new String[]{"#########", "#RRRRRRR#", "#RRRRRRR#", "#RRRRRRR#", "#RRRRRRR#", "####P####"}
      );
      public static final GuiFormat.GuiTemplate DAILY_REWARDS_PLUS = new GuiFormat.GuiTemplate(
         new String[]{"RRRRRRRRR", "RRRRRRRRR", "#RRRRRRR#", "##RRRRR##", "#########", "####P####"}
      );
      public static final GuiFormat.GuiTemplate NDAILY_REWARDS = new GuiFormat.GuiTemplate(new String[]{"#########", "#RRRRRRR#", "#########"});

      public GuiTemplate() {
         this.rows = new ArrayList<>();
      }

      public GuiTemplate(String[] rows) {
         this(Arrays.asList(rows));
      }

      public GuiTemplate(List<String> rows) {
         if (rows.size() == 0) {
            LushRewards.getInstance().getLogger().warning("Failed to load template, no lines detected.");
            this.rows = new ArrayList<>();
         } else {
            for (String row : rows) {
               if (row.length() != 9) {
                  LushRewards.getInstance().getLogger().warning("Failed to load template, lines must be 9 characters long.");
                  this.rows = new ArrayList<>();
                  return;
               }
            }

            this.rows = rows;
         }
      }

      public List<String> getRows() {
         return this.rows;
      }

      public void setRow(int row, String format) {
         if (format.length() != 9) {
            LushRewards.getInstance().getLogger().warning("Failed to load row format, lines must be 9 characters long.");
         } else {
            this.rows.set(row, format);
         }
      }

      public void addRow(String format) {
         if (format.length() != 9) {
            LushRewards.getInstance().getLogger().warning("Failed to load row format, lines must be 9 characters long.");
         } else {
            this.rows.add(format);
         }
      }

      public int getRowCount() {
         return this.rows.size();
      }

      public TreeMultimap<Character, Integer> getSlotMap() {
         TreeMultimap<Character, Integer> slotMap = TreeMultimap.create();

         for (int slot = 0; slot < this.getRowCount() * 9; slot++) {
            char character = this.getCharAt(slot);
            slotMap.put(character, slot);
         }

         return slotMap;
      }

      public char getCharAt(int slot) {
         int currRow = (int)Math.ceil((slot + 1) / 9.0F) - 1;
         int slotInRow = slot % 9;
         return this.rows.get(currRow).charAt(slotInRow);
      }

      public int countChar(char character) {
         int count = 0;

         for (String row : this.rows) {
            for (int i = 0; i < row.length(); i++) {
               if (row.charAt(i) == character) {
                  count++;
               }
            }
         }

         return count;
      }

      public static GuiFormat.GuiTemplate valueOf(String string) {
         String var1 = string.toUpperCase();
         switch (var1) {
            case "DEFAULT":
               return DEFAULT;
            case "COMPACT":
               return COMPACT;
            case "COMPACT_PROFILE":
               return COMPACT_PROFILE;
            case "BORDERED":
               return BORDERED;
            case "BORDERED_LARGE":
               return BORDERED_LARGE;
            case "DAILY_REWARDS_PLUS":
               return DAILY_REWARDS_PLUS;
            case "NDAILY_REWARDS":
               return NDAILY_REWARDS;
            default:
               LushRewards.getInstance().getLogger().warning("Invalid template type, setting to default.");
               return DEFAULT;
         }
      }
   }
}
