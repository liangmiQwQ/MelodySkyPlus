package net.mirolls.melodyskyplus.modules;

import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.path.find.AStarPathFinder;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.path.optimization.JumpOptimization;
import net.mirolls.melodyskyplus.path.optimization.PathOptimizer;
import net.mirolls.melodyskyplus.path.type.Node;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.Iterator;
import java.util.List;

public class SmartyPathFinder extends Module {
  public static SmartyPathFinder INSTANCE;
  public final Numbers<Double> pickaxeSlot = new Numbers<>("Pickaxe Slot", 3.0, 1.0, 9.0, 1.0);
  public final Numbers<Double> aotvSlot = new Numbers<>("Aotv Slot", 2.0, 1.0, 9.0, 1.0);
  private final Option<Boolean> jumpBoost = new Option<>("Jump Boost", true);
  private final Option<Boolean> miningAllowed = new Option<>("Mining Allowed", true);
  private final Option<Boolean> segmentation;
  private final Numbers<Double> length = new Numbers<>("Segment Length", 60.0, 30.0, 1000.0, 1.0);


  public List<PathPos> aStarPath;
  public List<Node> path;

  public SmartyPathFinder() {
    super("SmartyPathFinder", ModuleType.Others);
    segmentation = new Option<>("Segmentation", true, length::setEnabled);

    this.addValues(jumpBoost, miningAllowed, pickaxeSlot, aotvSlot, segmentation, length);

    this.setModInfo("A smart path finder with some features really cool");
  }

  public static SmartyPathFinder getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != SmartyPathFinder.class);

      INSTANCE = (SmartyPathFinder) m;
    }
    return INSTANCE;
  }

  public void go(BlockPos end) {
    int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1 - 1);
    int y = (int) (mc.thePlayer.posY - mc.thePlayer.posY % 1);
    int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1 - 1);
    BlockPos start = new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的

    go(start, end);
  }

  public void go(BlockPos start, BlockPos end) {
    if (segmentation.getValue()) {
      aStarPath = new AStarPathFinder(miningAllowed.getValue(), jumpBoost.getValue()).findPath(start, end, length.getValue().intValue());
    } else {
      aStarPath = new AStarPathFinder(miningAllowed.getValue(), jumpBoost.getValue()).findPath(start, end);
    }

    path = new JumpOptimization(true)
        .optimize(new PathOptimizer()
            .optimize(aStarPath));

    if (aStarPath == null || path == null) {
      throw new IllegalStateException("Path no Found");
    }
  }

  public void clear() {
    aStarPath.clear();
    path.clear();
  }
}
