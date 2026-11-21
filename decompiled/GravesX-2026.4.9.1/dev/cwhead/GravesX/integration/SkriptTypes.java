package dev.cwhead.GravesX.integration;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public final class SkriptTypes {
   private SkriptTypes() {
   }

   public static void register() {
      registerGraveType();
      registerGraveBlockType();
   }

   private static void registerGraveType() {
      if (Classes.getExactClassInfo(Grave.class) == null) {
         Classes.registerClass(
            new ClassInfo(Grave.class, "grave")
               .user(new String[]{"graves?"})
               .name("Grave")
               .description(new String[]{"A GravesX grave object."})
               .usage(new String[]{"grave"})
               .examples(new String[]{"broadcast \"%event-grave%\""})
               .parser(new Parser<Grave>() {
                  @Nullable
                  public Grave parse(String s, ParseContext context) {
                     return null;
                  }

                  public boolean canParse(ParseContext context) {
                     return false;
                  }

                  public String toString(Grave grave, int flags) {
                     return String.valueOf(grave);
                  }

                  public String toVariableNameString(Grave grave) {
                     return String.valueOf(grave);
                  }
               })
         );
      }
   }

   private static void registerGraveBlockType() {
      if (Classes.getExactClassInfo(BlockData.BlockType.class) == null) {
         Classes.registerClass(
            new ClassInfo(BlockData.BlockType.class, "graveblocktype")
               .user(new String[]{"grave ?block ?types?"})
               .name("Grave Block Type")
               .description(new String[]{"A GravesX grave block type."})
               .usage(new String[]{"grave block type"})
               .examples(new String[]{"broadcast \"%event-block-type%\""})
               .parser(new Parser<BlockData.BlockType>() {
                  @Nullable
                  public BlockData.BlockType parse(String s, ParseContext context) {
                     String normalized = s.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);

                     try {
                        return BlockData.BlockType.valueOf(normalized);
                     } catch (IllegalArgumentException var5) {
                        return null;
                     }
                  }

                  public String toString(BlockData.BlockType blockType, int flags) {
                     return blockType.name().toLowerCase(Locale.ROOT).replace('_', ' ');
                  }

                  public String toVariableNameString(BlockData.BlockType blockType) {
                     return blockType.name().toLowerCase(Locale.ROOT);
                  }
               })
         );
      }
   }
}
