package org.enginehub.piston;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

public class DefaultCommandManagerService implements CommandManagerService {
   private static final DefaultCommandManagerService INSTANCE = new DefaultCommandManagerService(getDefaultService());
   private final Lock sealLock = new ReentrantLock();
   private CommandManagerService defaultService;
   @Nullable
   private CommandManagerService sealedDefaultService;

   public static DefaultCommandManagerService getInstance() {
      return INSTANCE;
   }

   private static CommandManagerService getDefaultService() {
      ServiceLoader<CommandManagerService> loader = ServiceLoader.load(CommandManagerService.class, CommandManagerService.class.getClassLoader());
      return (CommandManagerService)Streams.stream(loader.iterator()).findFirst().orElseThrow(() -> new IllegalStateException("No default service available."));
   }

   private DefaultCommandManagerService(CommandManagerService defaultService) {
      this.defaultService = defaultService;
   }

   private CommandManagerService sealDelegate() {
      if (this.sealedDefaultService == null) {
         this.sealLock.lock();

         try {
            if (this.sealedDefaultService == null) {
               this.sealedDefaultService = this.defaultService;
            }
         } finally {
            this.sealLock.unlock();
         }
      }

      return this.sealedDefaultService;
   }

   public void setDefaultService(CommandManagerService defaultService) {
      this.sealLock.lock();

      try {
         Preconditions.checkState(this.sealedDefaultService == null, "Piston default service is sealed");
         this.defaultService = defaultService;
      } finally {
         this.sealLock.unlock();
      }
   }

   @Override
   public String id() {
      return this.sealDelegate().id();
   }

   @Override
   public CommandManager newCommandManager() {
      return this.sealDelegate().newCommandManager();
   }

   @Override
   public Command.Builder newCommand(String name) {
      return this.sealDelegate().newCommand(name);
   }
}
