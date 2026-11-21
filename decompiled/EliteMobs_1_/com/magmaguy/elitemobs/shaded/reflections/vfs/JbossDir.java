package com.magmaguy.elitemobs.shaded.reflections.vfs;

import java.net.URL;
import java.util.Iterator;
import java.util.Stack;
import java.util.jar.JarFile;
import org.jboss.vfs.VirtualFile;

public class JbossDir implements Vfs.Dir {
   private final VirtualFile virtualFile;

   private JbossDir(VirtualFile virtualFile) {
      this.virtualFile = virtualFile;
   }

   public static Vfs.Dir createDir(URL url) throws Exception {
      VirtualFile virtualFile = (VirtualFile)url.openConnection().getContent();
      return (Vfs.Dir)(virtualFile.isFile() ? new ZipDir(new JarFile(virtualFile.getPhysicalFile())) : new JbossDir(virtualFile));
   }

   @Override
   public String getPath() {
      return this.virtualFile.getPathName();
   }

   @Override
   public Iterable<Vfs.File> getFiles() {
      return () -> new Iterator<Vfs.File>() {
         Vfs.File entry;
         final Stack stack = new Stack();

         {
            this.entry = null;
            this.stack.addAll(JbossDir.this.virtualFile.getChildren());
         }

         @Override
         public boolean hasNext() {
            return this.entry != null || (this.entry = this.computeNext()) != null;
         }

         public Vfs.File next() {
            Vfs.File next = this.entry;
            this.entry = null;
            return next;
         }

         private Vfs.File computeNext() {
            while (!this.stack.isEmpty()) {
               VirtualFile file = (VirtualFile)this.stack.pop();
               if (!file.isDirectory()) {
                  return new JbossFile(JbossDir.this, file);
               }

               this.stack.addAll(file.getChildren());
            }

            return null;
         }
      };
   }
}
