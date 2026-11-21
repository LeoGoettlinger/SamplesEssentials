package org.sethomegui.SetHomeGUI.libs.updater.operators;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.SetHomeGUI.libs.block.Block;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.block.implementation.TerminatedBlock;
import org.sethomegui.SetHomeGUI.libs.engine.ExtendedConstructor;
import org.sethomegui.SetHomeGUI.libs.engine.ExtendedRepresenter;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.MappingNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Node;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.representer.BaseRepresenter;
import org.sethomegui.SetHomeGUI.libs.route.Route;
import org.sethomegui.SetHomeGUI.libs.settings.general.GeneralSettings;
import org.sethomegui.SetHomeGUI.libs.settings.updater.MergeRule;
import org.sethomegui.SetHomeGUI.libs.settings.updater.UpdaterSettings;

public class Merger {
   private static final Merger INSTANCE = new Merger();

   public static void merge(@NotNull Section document, @NotNull Section defaults, @NotNull UpdaterSettings settings) {
      INSTANCE.iterate(document, defaults, settings);
   }

   private void iterate(Section document, Section defaults, UpdaterSettings settings) {
      Set<Object> documentKeys = new HashSet<>(document.getStoredValue().keySet());
      boolean sort = settings.getOptionSorting() == UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS;
      Map<Object, Block<?>> sorted = sort ? document.getRoot().getGeneralSettings().getDefaultMap() : null;

      for (Entry<Object, Block<?>> entry : defaults.getStoredValue().entrySet()) {
         Object key = entry.getKey();
         Route route = Route.from(key);
         documentKeys.remove(key);
         Block<?> documentBlock = document.getOptionalBlock(route).orElse(null);
         Block<?> defaultBlock = entry.getValue();
         if (documentBlock != null) {
            if (documentBlock.isIgnored()) {
               documentBlock.setIgnored(false);
               if (documentBlock instanceof Section) {
                  this.resetIgnored((Section)documentBlock);
               }

               if (sort) {
                  sorted.put(key, documentBlock);
               }
            } else {
               boolean isDocumentBlockSection = documentBlock instanceof Section;
               boolean isDefaultBlockSection = defaultBlock instanceof Section;
               if (isDefaultBlockSection && isDocumentBlockSection) {
                  this.iterate((Section)documentBlock, (Section)defaultBlock, settings);
                  if (sort) {
                     sorted.put(key, documentBlock);
                  }
               } else if (sort) {
                  sorted.put(
                     key,
                     this.getPreservedValue(
                        settings.getMergeRules(), documentBlock, () -> this.cloneBlock(defaultBlock, document), isDocumentBlockSection, isDefaultBlockSection
                     )
                  );
               } else {
                  document.set(
                     route,
                     this.getPreservedValue(
                        settings.getMergeRules(), documentBlock, () -> this.cloneBlock(defaultBlock, document), isDocumentBlockSection, isDefaultBlockSection
                     )
                  );
               }
            }
         } else if (sort) {
            sorted.put(key, this.cloneBlock(defaultBlock, document));
         } else {
            document.set(route, this.cloneBlock(defaultBlock, document));
         }
      }

      if (settings.isKeepAll()) {
         if (sort) {
            documentKeys.forEach(keyx -> {
               Block var10000 = sorted.put(keyx, document.getStoredValue().get(keyx));
            });
            document.repopulate(sorted);
         }
      } else {
         for (Object key : documentKeys) {
            Route route = Route.fromSingleKey(key);
            Block<?> block = document.getOptionalBlock(route).orElse(null);
            if (block != null && block.isIgnored()) {
               block.setIgnored(false);
               if (block instanceof Section) {
                  this.resetIgnored((Section)block);
               }

               if (sort) {
                  sorted.put(key, block);
               }
            } else if (!sort) {
               document.remove(route);
            }
         }

         if (sort) {
            document.repopulate(sorted);
         }
      }
   }

   private void resetIgnored(@NotNull Section section) {
      section.getStoredValue().values().forEach(block -> {
         block.setIgnored(false);
         if (block instanceof Section) {
            this.resetIgnored((Section)block);
         }
      });
   }

   @NotNull
   private Block<?> cloneBlock(@NotNull Block<?> block, @NotNull Section newParent) {
      return (Block<?>)(block instanceof Section ? this.cloneSection((Section)block, newParent) : this.cloneTerminated((TerminatedBlock)block, newParent));
   }

   @NotNull
   private Section cloneSection(@NotNull Section section, @NotNull Section newParent) {
      if (section.getRoute() == null) {
         throw new IllegalArgumentException("Cannot clone the root!");
      } else {
         YamlDocument root = section.getRoot();
         GeneralSettings generalSettings = root.getGeneralSettings();
         BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings());
         ExtendedConstructor constructor = new ExtendedConstructor(
            root.getLoaderSettings().buildEngineSettings(generalSettings), generalSettings.getSerializer()
         );
         Node represented = representer.represent(section);
         constructor.constructSingleDocument(Optional.of(represented));
         section = new Section(newParent.getRoot(), newParent, section.getRoute(), this.moveComments(represented), (MappingNode)represented, constructor);
         constructor.clear();
         return section;
      }
   }

   @NotNull
   private TerminatedBlock cloneTerminated(@NotNull TerminatedBlock entry, @NotNull Section newParent) {
      YamlDocument root = newParent.getRoot();
      GeneralSettings generalSettings = root.getGeneralSettings();
      BaseRepresenter representer = new ExtendedRepresenter(generalSettings, root.getDumperSettings());
      ExtendedConstructor constructor = new ExtendedConstructor(root.getLoaderSettings().buildEngineSettings(generalSettings), generalSettings.getSerializer());
      Node represented = representer.represent(entry.getStoredValue());
      constructor.constructSingleDocument(Optional.of(represented));
      entry = new TerminatedBlock(entry, constructor.getConstructed(represented));
      constructor.clear();
      return entry;
   }

   private Node moveComments(@NotNull Node node) {
      ScalarNode scalarNode = new ScalarNode(Tag.STR, "", ScalarStyle.PLAIN);
      scalarNode.setBlockComments(node.getBlockComments());
      scalarNode.setInLineComments(node.getInLineComments());
      scalarNode.setEndComments(node.getEndComments());
      node.setBlockComments(Collections.emptyList());
      node.setInLineComments(null);
      node.setEndComments(null);
      return scalarNode;
   }

   @NotNull
   private Block<?> getPreservedValue(
      @NotNull Map<MergeRule, Boolean> rules,
      @NotNull Block<?> documentBlock,
      @NotNull Supplier<Block<?>> defaultBlock,
      boolean documentBlockIsSection,
      boolean defaultBlockIsSection
   ) {
      return rules.get(MergeRule.getFor(documentBlockIsSection, defaultBlockIsSection)) ? documentBlock : defaultBlock.get();
   }
}
