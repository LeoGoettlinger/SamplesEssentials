package com.magmaguy.elitemobs.shaded.reflections.vfs;

import java.io.IOException;
import java.io.InputStream;
import org.jboss.vfs.VirtualFile;

public class JbossFile implements Vfs.File {
   private final JbossDir root;
   private final VirtualFile virtualFile;

   public JbossFile(JbossDir root, VirtualFile virtualFile) {
      this.root = root;
      this.virtualFile = virtualFile;
   }

   @Override
   public String getName() {
      return this.virtualFile.getName();
   }

   @Override
   public String getRelativePath() {
      String filepath = this.virtualFile.getPathName();
      return filepath.startsWith(this.root.getPath()) ? filepath.substring(this.root.getPath().length() + 1) : null;
   }

   @Override
   public InputStream openInputStream() throws IOException {
      return this.virtualFile.openStream();
   }
}
