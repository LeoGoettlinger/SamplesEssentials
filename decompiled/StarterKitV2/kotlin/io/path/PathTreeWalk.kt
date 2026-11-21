package kotlin.io.path

import java.nio.file.FileSystemLoopException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.Arrays
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.IntrinsicsKt
import kotlin.coroutines.jvm.internal.SpillingKt
import kotlin.jvm.functions.Function2
import kotlin.jvm.internal.InlineMarker

internal class PathTreeWalk(start: Path, vararg options: Any) : Sequence<Path> {
   private final val start: Path
   private final val options: Array<out PathWalkOption>

   private final val followLinks: Boolean
      private final get() {
         return ArraysKt.contains(this.options, PathWalkOption.FOLLOW_LINKS);
      }


   private final val linkOptions: Array<LinkOption>
      private final get() {
         return LinkFollowing.INSTANCE.toLinkOptions(this.getFollowLinks());
      }


   private final val includeDirectories: Boolean
      private final get() {
         return ArraysKt.contains(this.options, PathWalkOption.INCLUDE_DIRECTORIES);
      }


   private final val isBFS: Boolean
      private final get() {
         return ArraysKt.contains(this.options, PathWalkOption.BREADTH_FIRST);
      }


   init {
      this.start = start;
      this.options = options;
   }

   public override operator fun iterator(): Iterator<Path> {
      return if (this.isBFS()) this.bfsIterator() else this.dfsIterator();
   }

   private suspend inline fun SequenceScope<Path>.yieldIfNeeded(node: PathNode, entriesReader: DirectoryEntriesReader, entriesAction: (List<PathNode>) -> Unit) {
      val path: Path = node.getPath();
      if (node.getParent() != null) {
         PathsKt.checkFileName(path);
      }

      var var9: Array<LinkOption> = access$getLinkOptions(this);
      var9 = Arrays.copyOf(var9, var9.length);
      if (Files.isDirectory(path, Arrays.copyOf(var9, var9.length))) {
         if (PathTreeWalkKt.access$createsCycle(node)) {
            throw new FileSystemLoopException(path.toString());
         }

         if (access$getIncludeDirectories(this)) {
            InlineMarker.mark(0);
            `$this$yieldIfNeeded`.yield(path, `$completion`);
            InlineMarker.mark(1);
         }

         var9 = access$getLinkOptions(this);
         var9 = Arrays.copyOf(var9, var9.length);
         if (Files.isDirectory(path, Arrays.copyOf(var9, var9.length))) {
            entriesAction.invoke(entriesReader.readEntries(node));
         }
      } else {
         var9 = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
         if (Files.exists(path, Arrays.copyOf(var9, var9.length))) {
            InlineMarker.mark(0);
            `$this$yieldIfNeeded`.yield(path, `$completion`);
            InlineMarker.mark(1);
            return Unit.INSTANCE;
         }
      }

      return Unit.INSTANCE;
   }

   private fun dfsIterator(): Iterator<Path> {
      return SequencesKt.iterator(
         (
            new Function2<SequenceScope<? super Path>, Continuation<? super Unit>, Object>(this, null) {
               Object L$1;
               Object L$2;
               Object L$3;
               Object L$4;
               Object L$5;
               Object L$6;
               Object L$7;
               Object L$8;
               Object L$9;
               Object L$10;
               Object L$11;
               int I$0;
               int label;

               {
                  super(2, `$completionx`);
                  this.this$0 = `$receiver`;
               }

               // $VF: Irreducible bytecode was duplicated to produce valid code
               @Override
               public final Object invokeSuspend(Object $result) {
                  val `$this$iterator`: SequenceScope = this.L$0 as SequenceScope;
                  val var19: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                  var stack: ArrayDeque;
                  var entriesReader: DirectoryEntriesReader;
                  var startNode: PathNode;
                  switch (this.label) {
                     case 0: {
                        ResultKt.throwOnFailure(`$result`);
                        stack = new ArrayDeque();
                        entriesReader = new DirectoryEntriesReader(PathTreeWalk.access$getFollowLinks(this.this$0));
                        startNode = new PathNode(
                           PathTreeWalk.access$getStart$p(this.this$0),
                           PathTreeWalkKt.access$keyOf(PathTreeWalk.access$getStart$p(this.this$0), PathTreeWalk.access$getLinkOptions(this.this$0)),
                           null
                        );
                        val var23: PathTreeWalk = this.this$0;
                        val var44: Path = startNode.getPath();
                        if (startNode.getParent() != null) {
                           PathsKt.checkFileName(var44);
                        }

                        var var49: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var23);
                        var49 = Arrays.copyOf(var49, var49.length);
                        if (Files.isDirectory(var44, Arrays.copyOf(var49, var49.length))) {
                           if (PathTreeWalkKt.access$createsCycle(startNode)) {
                              throw new FileSystemLoopException(var44.toString());
                           }

                           if (PathTreeWalk.access$getIncludeDirectories(var23)) {
                              val var10002: Continuation = this;
                              this.L$0 = `$this$iterator`;
                              this.L$1 = stack;
                              this.L$2 = entriesReader;
                              this.L$3 = startNode;
                              this.L$4 = var23;
                              this.L$5 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                              this.L$6 = startNode;
                              this.L$7 = entriesReader;
                              this.L$8 = var44;
                              this.I$0 = 0;
                              this.label = 1;
                              if (`$this$iterator`.yield(var44, var10002) === var19) {
                                 return var19;
                              }
                           }

                           var49 = PathTreeWalk.access$getLinkOptions(var23);
                           var49 = Arrays.copyOf(var49, var49.length);
                           if (Files.isDirectory(var44, Arrays.copyOf(var49, var49.length))) {
                              startNode.setContentIterator(entriesReader.readEntries(startNode).iterator());
                              stack.addLast(startNode);
                           }
                        } else {
                           var49 = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
                           if (Files.exists(var44, Arrays.copyOf(var49, var49.length))) {
                              val var68: Continuation = this;
                              this.L$0 = `$this$iterator`;
                              this.L$1 = stack;
                              this.L$2 = entriesReader;
                              this.L$3 = SpillingKt.nullOutSpilledVariable(startNode);
                              this.L$4 = SpillingKt.nullOutSpilledVariable(var23);
                              this.L$5 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                              this.L$6 = SpillingKt.nullOutSpilledVariable(startNode);
                              this.L$7 = SpillingKt.nullOutSpilledVariable(entriesReader);
                              this.L$8 = SpillingKt.nullOutSpilledVariable(var44);
                              this.I$0 = 0;
                              this.label = 2;
                              if (`$this$iterator`.yield(var44, var68) === var19) {
                                 return var19;
                              }
                           }
                        }
                        break;
                     }
                     case 1: {
                        val var39: Int = this.I$0;
                        val var43: Path = this.L$8 as Path;
                        val var35: DirectoryEntriesReader = this.L$7 as DirectoryEntriesReader;
                        val var31: PathNode = this.L$6 as PathNode;
                        val var27: SequenceScope = this.L$5 as SequenceScope;
                        val var22: PathTreeWalk = this.L$4 as PathTreeWalk;
                        startNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        stack = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        var var47: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var22);
                        var47 = Arrays.copyOf(var47, var47.length);
                        if (Files.isDirectory(var43, Arrays.copyOf(var47, var47.length))) {
                           startNode.setContentIterator(var35.readEntries(var31).iterator());
                           stack.addLast(startNode);
                        }
                        break;
                     }
                     case 2: {
                        val var38: Int = this.I$0;
                        val var42: Path = this.L$8 as Path;
                        val var34: DirectoryEntriesReader = this.L$7 as DirectoryEntriesReader;
                        val var30: PathNode = this.L$6 as PathNode;
                        val var26: SequenceScope = this.L$5 as SequenceScope;
                        val var21: PathTreeWalk = this.L$4 as PathTreeWalk;
                        startNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        stack = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        break;
                     }
                     case 3: {
                        val var46: Int = this.I$0;
                        val var55: Path = this.L$11 as Path;
                        val var45: DirectoryEntriesReader = this.L$10 as DirectoryEntriesReader;
                        val var41: PathNode = this.L$9 as PathNode;
                        val var37: SequenceScope = this.L$8 as SequenceScope;
                        val var33: PathTreeWalk = this.L$7 as PathTreeWalk;
                        val var29: PathNode = this.L$6 as PathNode;
                        val var25: java.util.Iterator = this.L$5 as java.util.Iterator;
                        val var20: PathNode = this.L$4 as PathNode;
                        startNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        stack = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        var var16: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var33);
                        var16 = Arrays.copyOf(var16, var16.length);
                        if (Files.isDirectory(var55, Arrays.copyOf(var16, var16.length))) {
                           var29.setContentIterator(var45.readEntries(var41).iterator());
                           stack.addLast(var29);
                        }
                        break;
                     }
                     case 4: {
                        val `$i$f$yieldIfNeeded`: Int = this.I$0;
                        val `path$iv`: Path = this.L$11 as Path;
                        val `entriesReader$iv`: DirectoryEntriesReader = this.L$10 as DirectoryEntriesReader;
                        val `node$iv`: PathNode = this.L$9 as PathNode;
                        val `$this$yieldIfNeeded$iv`: SequenceScope = this.L$8 as SequenceScope;
                        val `this_$iv`: PathTreeWalk = this.L$7 as PathTreeWalk;
                        val pathNode: PathNode = this.L$6 as PathNode;
                        val topIterator: java.util.Iterator = this.L$5 as java.util.Iterator;
                        val topNode: PathNode = this.L$4 as PathNode;
                        startNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        stack = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        break;
                     }
                     default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                  }

                  while (!stack.isEmpty()) {
                     val var24: PathNode = stack.last() as PathNode;
                     val var10000: java.util.Iterator = var24.getContentIterator();
                     if (var10000.hasNext()) {
                        val var32: PathNode = var10000.next() as PathNode;
                        val var36: PathTreeWalk = this.this$0;
                        val `path$ivx`: Path = var32.getPath();
                        if (var32.getParent() != null) {
                           PathsKt.checkFileName(`path$ivx`);
                        }

                        var var61: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var36);
                        var61 = Arrays.copyOf(var61, var61.length);
                        if (Files.isDirectory(`path$ivx`, Arrays.copyOf(var61, var61.length))) {
                           if (PathTreeWalkKt.access$createsCycle(var32)) {
                              throw new FileSystemLoopException(`path$ivx`.toString());
                           }

                           if (PathTreeWalk.access$getIncludeDirectories(var36)) {
                              val var69: Continuation = this;
                              this.L$0 = `$this$iterator`;
                              this.L$1 = stack;
                              this.L$2 = entriesReader;
                              this.L$3 = SpillingKt.nullOutSpilledVariable(startNode);
                              this.L$4 = SpillingKt.nullOutSpilledVariable(var24);
                              this.L$5 = SpillingKt.nullOutSpilledVariable(var10000);
                              this.L$6 = var32;
                              this.L$7 = var36;
                              this.L$8 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                              this.L$9 = var32;
                              this.L$10 = entriesReader;
                              this.L$11 = `path$ivx`;
                              this.I$0 = 0;
                              this.label = 3;
                              if (`$this$iterator`.yield(`path$ivx`, var69) === var19) {
                                 return var19;
                              }
                           }

                           var61 = PathTreeWalk.access$getLinkOptions(var36);
                           var61 = Arrays.copyOf(var61, var61.length);
                           if (Files.isDirectory(`path$ivx`, Arrays.copyOf(var61, var61.length))) {
                              var32.setContentIterator(entriesReader.readEntries(var32).iterator());
                              stack.addLast(var32);
                           }
                        } else {
                           var61 = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
                           if (Files.exists(`path$ivx`, Arrays.copyOf(var61, var61.length))) {
                              val var70: Continuation = this;
                              this.L$0 = `$this$iterator`;
                              this.L$1 = stack;
                              this.L$2 = entriesReader;
                              this.L$3 = SpillingKt.nullOutSpilledVariable(startNode);
                              this.L$4 = SpillingKt.nullOutSpilledVariable(var24);
                              this.L$5 = SpillingKt.nullOutSpilledVariable(var10000);
                              this.L$6 = SpillingKt.nullOutSpilledVariable(var32);
                              this.L$7 = SpillingKt.nullOutSpilledVariable(var36);
                              this.L$8 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                              this.L$9 = SpillingKt.nullOutSpilledVariable(var32);
                              this.L$10 = SpillingKt.nullOutSpilledVariable(entriesReader);
                              this.L$11 = SpillingKt.nullOutSpilledVariable(`path$ivx`);
                              this.I$0 = 0;
                              this.label = 4;
                              if (`$this$iterator`.yield(`path$ivx`, var70) === var19) {
                                 return var19;
                              }
                           }
                        }
                     } else {
                        stack.removeLast();
                     }
                  }

                  return Unit.INSTANCE;
               }

               @Override
               public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                  val var3: Function2 = new <anonymous constructor>(this.this$0, `$completion`);
                  var3.L$0 = value;
                  return var3 as Continuation<Unit>;
               }

               public final Object invoke(SequenceScope<? super Path> p1, Continuation<? super Unit> p2) {
                  return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
               }
            }
         ) as (SequenceScope<? super Path>?, Continuation<? super Unit>?) -> Any
      );
   }

   private fun bfsIterator(): Iterator<Path> {
      return SequencesKt.iterator(
         (
            new Function2<SequenceScope<? super Path>, Continuation<? super Unit>, Object>(this, null) {
               Object L$1;
               Object L$2;
               Object L$3;
               Object L$4;
               Object L$5;
               Object L$6;
               Object L$7;
               Object L$8;
               int I$0;
               int label;

               {
                  super(2, `$completionx`);
                  this.this$0 = `$receiver`;
               }

               // $VF: Irreducible bytecode was duplicated to produce valid code
               @Override
               public final Object invokeSuspend(Object $result) {
                  val `$this$iterator`: SequenceScope = this.L$0 as SequenceScope;
                  val var16: Any = IntrinsicsKt.getCOROUTINE_SUSPENDED();
                  var queue: ArrayDeque;
                  var entriesReader: DirectoryEntriesReader;
                  switch (this.label) {
                     case 0:
                        ResultKt.throwOnFailure(`$result`);
                        queue = new ArrayDeque();
                        entriesReader = new DirectoryEntriesReader(PathTreeWalk.access$getFollowLinks(this.this$0));
                        queue.addLast(
                           new PathNode(
                              PathTreeWalk.access$getStart$p(this.this$0),
                              PathTreeWalkKt.access$keyOf(PathTreeWalk.access$getStart$p(this.this$0), PathTreeWalk.access$getLinkOptions(this.this$0)),
                              null
                           )
                        );
                        break;
                     case 1: {
                        val var24: Int = this.I$0;
                        val var26: Path = this.L$8 as Path;
                        val var23: DirectoryEntriesReader = this.L$7 as DirectoryEntriesReader;
                        val var22: PathNode = this.L$6 as PathNode;
                        val var21: SequenceScope = this.L$5 as SequenceScope;
                        val var19: PathTreeWalk = this.L$4 as PathTreeWalk;
                        val var17: PathNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        queue = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        var var13: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var19);
                        var13 = Arrays.copyOf(var13, var13.length);
                        if (Files.isDirectory(var26, Arrays.copyOf(var13, var13.length))) {
                           queue.addAll(var23.readEntries(var22));
                        }
                        break;
                     }
                     case 2: {
                        val `$i$f$yieldIfNeeded`: Int = this.I$0;
                        val `path$iv`: Path = this.L$8 as Path;
                        val `entriesReader$iv`: DirectoryEntriesReader = this.L$7 as DirectoryEntriesReader;
                        val `node$iv`: PathNode = this.L$6 as PathNode;
                        val `$this$yieldIfNeeded$iv`: SequenceScope = this.L$5 as SequenceScope;
                        val `this_$iv`: PathTreeWalk = this.L$4 as PathTreeWalk;
                        val pathNode: PathNode = this.L$3 as PathNode;
                        entriesReader = this.L$2 as DirectoryEntriesReader;
                        queue = this.L$1 as ArrayDeque;
                        ResultKt.throwOnFailure(`$result`);
                        break;
                     }
                     default:
                        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                  }

                  while (!queue.isEmpty()) {
                     val var18: PathNode = queue.removeFirst() as PathNode;
                     val var20: PathTreeWalk = this.this$0;
                     val var27: Path = var18.getPath();
                     if (var18.getParent() != null) {
                        PathsKt.checkFileName(var27);
                     }

                     var var29: Array<LinkOption> = PathTreeWalk.access$getLinkOptions(var20);
                     var29 = Arrays.copyOf(var29, var29.length);
                     if (Files.isDirectory(var27, Arrays.copyOf(var29, var29.length))) {
                        if (PathTreeWalkKt.access$createsCycle(var18)) {
                           throw new FileSystemLoopException(var27.toString());
                        }

                        if (PathTreeWalk.access$getIncludeDirectories(var20)) {
                           val var10002: Continuation = this;
                           this.L$0 = `$this$iterator`;
                           this.L$1 = queue;
                           this.L$2 = entriesReader;
                           this.L$3 = SpillingKt.nullOutSpilledVariable(var18);
                           this.L$4 = var20;
                           this.L$5 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                           this.L$6 = var18;
                           this.L$7 = entriesReader;
                           this.L$8 = var27;
                           this.I$0 = 0;
                           this.label = 1;
                           if (`$this$iterator`.yield(var27, var10002) === var16) {
                              return var16;
                           }
                        }

                        var29 = PathTreeWalk.access$getLinkOptions(var20);
                        var29 = Arrays.copyOf(var29, var29.length);
                        if (Files.isDirectory(var27, Arrays.copyOf(var29, var29.length))) {
                           queue.addAll(entriesReader.readEntries(var18));
                        }
                     } else {
                        var29 = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
                        if (Files.exists(var27, Arrays.copyOf(var29, var29.length))) {
                           val var36: Continuation = this;
                           this.L$0 = `$this$iterator`;
                           this.L$1 = queue;
                           this.L$2 = entriesReader;
                           this.L$3 = SpillingKt.nullOutSpilledVariable(var18);
                           this.L$4 = SpillingKt.nullOutSpilledVariable(var20);
                           this.L$5 = SpillingKt.nullOutSpilledVariable(`$this$iterator`);
                           this.L$6 = SpillingKt.nullOutSpilledVariable(var18);
                           this.L$7 = SpillingKt.nullOutSpilledVariable(entriesReader);
                           this.L$8 = SpillingKt.nullOutSpilledVariable(var27);
                           this.I$0 = 0;
                           this.label = 2;
                           if (`$this$iterator`.yield(var27, var36) === var16) {
                              return var16;
                           }
                        }
                     }
                  }

                  return Unit.INSTANCE;
               }

               @Override
               public final Continuation<Unit> create(Object value, Continuation<?> $completion) {
                  val var3: Function2 = new <anonymous constructor>(this.this$0, `$completion`);
                  var3.L$0 = value;
                  return var3 as Continuation<Unit>;
               }

               public final Object invoke(SequenceScope<? super Path> p1, Continuation<? super Unit> p2) {
                  return (this.create(p1, p2) as <unrepresentable>).invokeSuspend(Unit.INSTANCE);
               }
            }
         ) as (SequenceScope<? super Path>?, Continuation<? super Unit>?) -> Any
      );
   }
}
