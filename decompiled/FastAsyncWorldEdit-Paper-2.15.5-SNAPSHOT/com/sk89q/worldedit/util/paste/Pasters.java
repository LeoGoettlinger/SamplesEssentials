package com.sk89q.worldedit.util.paste;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class Pasters {
   private static final ListeningExecutorService executor = MoreExecutors.listeningDecorator(
      new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2L, TimeUnit.SECONDS, new SynchronousQueue<>())
   );

   private Pasters() {
   }

   static ListeningExecutorService getExecutor() {
      return executor;
   }
}
