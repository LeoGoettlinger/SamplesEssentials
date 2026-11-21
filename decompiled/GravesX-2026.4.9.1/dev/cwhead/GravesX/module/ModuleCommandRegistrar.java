package dev.cwhead.GravesX.module;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.module.command.GravesXModuleCommand;
import dev.cwhead.GravesX.module.command.GravesXModuleTabCompleter;
import dev.cwhead.GravesX.module.util.ModuleInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

final class ModuleCommandRegistrar {
   private final Graves plugin;
   private final Map<String, List<PluginCommand>> cmds = new LinkedHashMap<>();
   private final Map<String, List<Permission>> perms = new LinkedHashMap<>();

   ModuleCommandRegistrar(Graves plugin) {
      this.plugin = plugin;
   }

   void registerFor(ModuleManager.LoadedModule lm) {
      this.registerPermissions(lm);
      this.registerCommands(lm);
   }

   void unregisterFor(ModuleManager.LoadedModule lm) {
      List<PluginCommand> cmdList = this.cmds.remove(lm.info.name());
      if (cmdList != null) {
         for (PluginCommand pc : cmdList) {
            if (pc.getTabCompleter() instanceof GravesXModuleTabCompleter tab) {
               tab.onUnregister();
            }

            this.unregister(pc);
         }
      }

      List<Permission> permList = this.perms.remove(lm.info.name());
      if (permList != null) {
         for (Permission p : permList) {
            try {
               Bukkit.getPluginManager().removePermission(p);
            } catch (Throwable var7) {
            }
         }
      }
   }

   private void registerPermissions(ModuleManager.LoadedModule lm) {
      Map<String, ModuleInfo.PermissionDef> defs = lm.info.permissions();
      if (defs != null && !defs.isEmpty()) {
         ArrayList<Permission> out = new ArrayList<>();

         for (ModuleInfo.PermissionDef def : defs.values()) {
            if (def != null && def.node() != null && !def.node().isBlank()) {
               String node = def.node();
               String desc = Optional.ofNullable(def.description()).orElse("");
               String defStr = Optional.ofNullable(def.defaultValue()).orElse("FALSE");
               PermissionDefault permDefault = PermissionDefault.FALSE;

               try {
                  permDefault = PermissionDefault.valueOf(defStr.trim().toUpperCase(Locale.ROOT));
               } catch (Exception var13) {
               }

               Permission perm = new Permission(node, desc, permDefault);
               if (Bukkit.getPluginManager().getPermission(perm.getName()) == null) {
                  Bukkit.getPluginManager().addPermission(perm);

                  try {
                     Bukkit.getPluginManager().recalculatePermissionDefaults(perm);
                  } catch (Throwable var12) {
                  }

                  out.add(perm);
               }
            }
         }

         if (!out.isEmpty()) {
            this.perms.put(lm.info.name(), out);
         }
      }
   }

   private void registerCommands(ModuleManager.LoadedModule lm) {
      Map<String, ModuleInfo.CommandDef> defs = lm.info.commands();
      if (defs != null && !defs.isEmpty()) {
         SimpleCommandMap map = this.commandMap();
         if (map == null) {
            this.plugin.debugMessage("CommandMap is null; cannot register YAML commands for module " + lm.info.name(), 2);
         } else {
            new ArrayList();

            for (ModuleInfo.CommandDef def : defs.values()) {
               if (def != null && def.name() != null && !def.name().isBlank()) {
                  String name = def.name();
                  PluginCommand pc = this.newPluginCommand(name, this.plugin);
                  if (pc == null) {
                     this.plugin.debugMessage("Failed to create PluginCommand for '" + name + "' (module " + lm.info.name() + ")", 2);
                  } else {
                     String yamlDesc = Optional.ofNullable(def.description()).orElse("");
                     String yamlUsage = Optional.ofNullable(def.usage()).orElse("/" + name);
                     String yamlPerm = Optional.ofNullable(def.permission()).orElse("");
                     List<String> yamlAliases = new ArrayList<>(def.aliases());
                     CommandExecutor exec = this.execInstance(def.executor(), lm, pc);
                     GravesXModuleTabCompleter tab = this.tabInstance(def.tabCompleter(), lm, pc);
                     if (tab == null && exec instanceof GravesXModuleTabCompleter gmtc) {
                        tab = gmtc;
                     }

                     if (exec instanceof GravesXModuleCommand g) {
                        if (yamlDesc.isEmpty() && g.getDescription() != null) {
                           yamlDesc = g.getDescription();
                        }

                        if (yamlUsage.isEmpty() && g.getUsage() != null) {
                           yamlUsage = g.getUsage();
                        }

                        if (yamlPerm.isEmpty() && g.getPermission() != null) {
                           yamlPerm = g.getPermission();
                        }

                        if (yamlAliases.isEmpty() && g.getAliases() != null) {
                           yamlAliases = new ArrayList<>(g.getAliases());
                        }

                        String providedName = g.getName();
                        if (providedName != null && !providedName.isEmpty() && !providedName.equalsIgnoreCase(name) && !yamlAliases.contains(providedName)) {
                           yamlAliases.add(providedName);
                        }
                     }

                     pc.setDescription(yamlDesc);
                     pc.setUsage(yamlUsage);
                     pc.setPermission(yamlPerm);
                     if (!yamlAliases.isEmpty()) {
                        pc.setAliases(yamlAliases);
                     }

                     if (exec != null) {
                        pc.setExecutor(exec);
                     }

                     if (tab != null) {
                        pc.setTabCompleter(tab);
                        tab.onRegister(lm.context, pc);
                     }

                     if (map.register(this.plugin.getName().toLowerCase(Locale.ROOT), pc)) {
                        this.cmds.computeIfAbsent(lm.info.name(), k -> new ArrayList<>()).add(pc);
                        this.plugin.debugMessage("Registered YAML command '/" + name + "' for module " + lm.info.name(), 1);
                     } else {
                        this.plugin
                           .debugMessage(
                              "Failed to register YAML command '/" + name + "' for module " + lm.info.name() + " (command map rejected registration)", 2
                           );
                     }
                  }
               }
            }
         }
      }
   }

   private CommandExecutor execInstance(String fqcn, ModuleManager.LoadedModule lm, Command cmd) {
      if (fqcn != null && !fqcn.isEmpty()) {
         try {
            Class<?> c = Class.forName(fqcn, true, lm.cl);
            return !CommandExecutor.class.isAssignableFrom(c) ? null : this.instantiate(c, lm.context, CommandExecutor.class);
         } catch (Throwable var5) {
            this.plugin.getLogger().severe("Executor load failed for " + fqcn + ": " + var5.getMessage());
            this.plugin.logStackTrace(var5);
            return null;
         }
      } else {
         return null;
      }
   }

   private GravesXModuleTabCompleter tabInstance(String fqcn, ModuleManager.LoadedModule lm, Command cmd) {
      if (fqcn != null && !fqcn.isEmpty()) {
         try {
            Class<?> c = Class.forName(fqcn, true, lm.cl);
            return !GravesXModuleTabCompleter.class.isAssignableFrom(c) ? null : this.instantiate(c, lm.context, GravesXModuleTabCompleter.class);
         } catch (Throwable var5) {
            this.plugin.getLogger().severe("TabCompleter load failed for " + fqcn + ": " + var5.getMessage());
            this.plugin.logStackTrace(var5);
            return null;
         }
      } else {
         return null;
      }
   }

   private <T> T instantiate(Class<?> c, ModuleContext ctx, Class<T> type) throws Exception {
      try {
         Constructor<?> cons = c.getDeclaredConstructor(ModuleContext.class);
         cons.setAccessible(true);
         return (T)cons.newInstance(ctx);
      } catch (NoSuchMethodException var6) {
         Constructor<?> consx = c.getDeclaredConstructor();
         consx.setAccessible(true);
         return (T)consx.newInstance();
      }
   }

   private SimpleCommandMap commandMap() {
      try {
         Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         f.setAccessible(true);
         return (SimpleCommandMap)f.get(Bukkit.getServer());
      } catch (Throwable var2) {
         return null;
      }
   }

   private PluginCommand newPluginCommand(String name, Graves owner) {
      try {
         Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
         c.setAccessible(true);
         return c.newInstance(name, owner);
      } catch (Throwable var4) {
         return null;
      }
   }

   private void unregister(PluginCommand pc) {
      try {
         SimpleCommandMap map = this.commandMap();
         if (map == null) {
            return;
         }

         try {
            pc.unregister(map);
         } catch (Throwable var5) {
         }

         Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
         f.setAccessible(true);
         Map<String, Command> known = (Map<String, Command>)f.get(map);
         known.values().removeIf(cmd -> cmd == pc);
      } catch (Throwable var6) {
      }
   }

   void registerDynamicCommand(ModuleManager.LoadedModule lm, String name, GravesXModuleCommand command) {
      if (lm != null && command != null && name != null && !name.isBlank()) {
         SimpleCommandMap map = this.commandMap();
         if (map == null) {
            this.plugin.debugMessage("CommandMap is null; cannot register dynamic command '/" + name + "' for module " + lm.info.name(), 2);
         } else {
            PluginCommand pc = this.newPluginCommand(name, this.plugin);
            if (pc == null) {
               this.plugin.debugMessage("Failed to create PluginCommand for dynamic '/" + name + "' (module " + lm.info.name() + ")", 2);
            } else {
               String desc = Optional.ofNullable(command.getDescription()).orElse("");
               String usage = Optional.ofNullable(command.getUsage()).orElse("/" + name);
               String perm = Optional.ofNullable(command.getPermission()).orElse("");
               List<String> aliases = new ArrayList<>();
               if (command.getAliases() != null) {
                  aliases.addAll(command.getAliases());
               }

               String providedName = command.getName();
               if (providedName != null && !providedName.isEmpty() && !providedName.equalsIgnoreCase(name) && !aliases.contains(providedName)) {
                  aliases.add(providedName);
               }

               pc.setDescription(desc);
               pc.setUsage(usage);
               pc.setPermission(perm);
               if (!aliases.isEmpty()) {
                  pc.setAliases(aliases);
               }

               GravesXModuleTabCompleter tab = null;
               if (command instanceof GravesXModuleTabCompleter gmtc) {
                  tab = gmtc;
               }

               pc.setExecutor(command);
               if (tab != null) {
                  pc.setTabCompleter(tab);
                  tab.onRegister(lm.context, pc);
               }

               if (map.register(this.plugin.getName().toLowerCase(Locale.ROOT), pc)) {
                  this.cmds.computeIfAbsent(lm.info.name(), k -> new ArrayList<>()).add(pc);
                  this.plugin.debugMessage("Registered dynamic command '/" + name + "' for module " + lm.info.name(), 1);
               } else {
                  this.plugin
                     .debugMessage(
                        "Failed to register dynamic command '/" + name + "' for module " + lm.info.name() + " (command map rejected registration)", 2
                     );
               }
            }
         }
      }
   }

   void registerDynamicCommand(ModuleManager.LoadedModule lm, String name, Class<? extends GravesXModuleCommand> commandClass) {
      if (lm != null && commandClass != null && name != null && !name.isBlank()) {
         SimpleCommandMap map = this.commandMap();
         if (map == null) {
            this.plugin.debugMessage("CommandMap is null; cannot register dynamic class-based command '/" + name + "' for module " + lm.info.name(), 2);
         } else {
            PluginCommand pc = this.newPluginCommand(name, this.plugin);
            if (pc == null) {
               this.plugin.debugMessage("Failed to create PluginCommand for dynamic class-based '/" + name + "' (module " + lm.info.name() + ")", 2);
            } else {
               GravesXModuleCommand command;
               try {
                  command = this.instantiate(commandClass, lm.context, GravesXModuleCommand.class);
               } catch (Exception var14) {
                  this.plugin.getLogger().severe("Dynamic command instantiation failed for " + commandClass.getName() + ": " + var14.getMessage());
                  this.plugin.logStackTrace(var14);
                  return;
               }

               String desc = Optional.ofNullable(command.getDescription()).orElse("");
               String usage = Optional.ofNullable(command.getUsage()).orElse("/" + name);
               String perm = Optional.ofNullable(command.getPermission()).orElse("");
               List<String> aliases = new ArrayList<>();
               if (command.getAliases() != null) {
                  aliases.addAll(command.getAliases());
               }

               String providedName = command.getName();
               if (providedName != null && !providedName.isEmpty() && !providedName.equalsIgnoreCase(name) && !aliases.contains(providedName)) {
                  aliases.add(providedName);
               }

               pc.setDescription(desc);
               pc.setUsage(usage);
               pc.setPermission(perm);
               if (!aliases.isEmpty()) {
                  pc.setAliases(aliases);
               }

               GravesXModuleTabCompleter tab = null;
               if (command instanceof GravesXModuleTabCompleter gmtc) {
                  tab = gmtc;
               }

               pc.setExecutor(command);
               if (tab != null) {
                  pc.setTabCompleter(tab);
                  tab.onRegister(lm.context, pc);
               }

               if (map.register(this.plugin.getName().toLowerCase(Locale.ROOT), pc)) {
                  this.cmds.computeIfAbsent(lm.info.name(), k -> new ArrayList<>()).add(pc);
                  this.plugin.debugMessage("Registered dynamic class-based command '/" + name + "' for module " + lm.info.name(), 1);
               } else {
                  this.plugin
                     .debugMessage(
                        "Failed to register dynamic class-based command '/" + name + "' for module " + lm.info.name() + " (command map rejected registration)",
                        2
                     );
               }
            }
         }
      }
   }
}
