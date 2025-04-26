package net.mirolls.melodyskyplus.modules;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.path.EventHook;
import net.mirolls.melodyskyplus.path.exec.AbilityExec;
import net.mirolls.melodyskyplus.path.exec.PathExec;
import net.mirolls.melodyskyplus.path.find.AStarPathFinder;
import net.mirolls.melodyskyplus.path.find.PathPos;
import net.mirolls.melodyskyplus.path.optimization.JumpOptimization;
import net.mirolls.melodyskyplus.path.optimization.PathOptimizer;
import net.mirolls.melodyskyplus.path.type.Node;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.pathfinding.PathProcessor;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.ArrayList;
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
  private final TimerUtil timer = new TimerUtil();
  public List<PathPos> aStarPath = new ArrayList<>();
  public List<Node> path = new ArrayList<>();
  public int retryTimes;
  public int tick;
  private EventHook finishedHook = null;
  private EventHook failedHook = null;
  private BlockPos end;
  private Vec3d lastVec = null;
  private boolean findingPath = false;


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
    go(PlayerUtils.getPlayerLocation(), end);
  }

  public void go(BlockPos start, BlockPos end) {
    clear();
    this.end = end;
    PathExec.area = null;
    PathExec.abilityExec = new AbilityExec();


    if (segmentation.getValue()) {
      aStarPath = new AStarPathFinder(miningAllowed.getValue(), jumpBoost.getValue()).findPath(start, end, length.getValue().intValue());
    } else {
      aStarPath = new AStarPathFinder(miningAllowed.getValue(), jumpBoost.getValue()).findPath(start, end);
    }

    path = new JumpOptimization(true)
        .optimize(new PathOptimizer()
            .optimize(aStarPath));


    if (aStarPath == null || path == null) {
      Helper.sendMessage("Cannot found path");
      failed();
      strongClear(false);
      throw new IllegalStateException("Path no Found");
    }
  }

  public void onFailed(EventHook hook) {
    failedHook = hook;
  }

  public void onFinished(EventHook hook) {
    finishedHook = hook;
  }

  public void failed() {
    if (failedHook != null) {
      failedHook.hook();
    }
  }

  public void finished() {
    if (finishedHook != null) {
      finishedHook.hook();
    }
  }


  @EventHandler
  public void onTick(EventTick event) {
    if (tick >= 0) {
      tick++;

      if (tick == 40 && !path.isEmpty() && !aStarPath.isEmpty()) {
        // retryTimes处理装置
        retryTimes--;
        tick = 0;
      }
    }


    if (!path.isEmpty() && !aStarPath.isEmpty() && timer.hasReached(500)) {
      // 如果卡住了

      if ((PathExec.abilityExec.tick == -1 || PathExec.abilityExec.tick > 400) && (PathExec.mineExec.tick == -1 || PathExec.mineExec.tick > 400)) {
        // 如果没有在abilityExec或者说 ability卡死了

        if (mc.thePlayer.onGround && lastVec != null && lastVec.distanceTo(new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) < 0.6) {

          if (retryTimes < 5) {
            go(end);
            retryTimes++;
            return;
          } else {
            // 出问题了 无法走到节点
            Helper.sendMessage("Sorry Cannot exec the path");
            failed();
            strongClear(false);
            return;
          }
        }
      }

      // 分段寻路
      if (!path.get(path.size() - 1).getPos().equals(end) && segmentation.getValue() && !findingPath) {

        // 分段查询 添加多段
        if (path.size() < length.getValue() / 12 || MathUtil.distanceToPos(PlayerUtils.getPlayerLocation(), aStarPath.get(aStarPath.size() - 1).getPos()) < length.getValue() / 2) {

          findingPath = true;
          new Thread(() -> {
            // 异步操作
            List<PathPos> newPath = new AStarPathFinder(miningAllowed.getValue(), jumpBoost.getValue()).findPath(path.get(path.size() - 1).getPos(), end, length.getValue().intValue());

            if (newPath == null) {
              Helper.sendMessage("Cannot found path");
              failed();
              strongClear(false);
              return;
            }

            if (path.get(path.size() - 1).getPos().equals(newPath.get(0).getPos())) {
              newPath.remove(0);

              aStarPath.addAll(newPath);
              path.addAll(new JumpOptimization(true)
                  .optimize(new PathOptimizer()
                      .optimize(newPath)));
            }

            findingPath = false;
          }).start();
        }
      }

      timer.reset();
      lastVec = new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }
  }

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    if (!this.path.isEmpty() && !this.aStarPath.isEmpty()) {
      Helper.sendMessage(EnumChatFormatting.YELLOW + "[MacroProtection]" + EnumChatFormatting.GRAY + " Stopped " + EnumChatFormatting.LIGHT_PURPLE + this.getName() + EnumChatFormatting.GRAY + " due to World Change.");
      failed();
      strongClear(false);
    }
  }


  public void clear() {
    aStarPath.clear();
    path.clear();
    end = null;
    PathProcessor.releaseControls();
  }

  public void strongClear(boolean start) {
    clear();
    retryTimes = 0;
    tick = start ? 0 : -1;
    timer.reset();
    PathExec.area = null;
    PathExec.abilityExec = new AbilityExec();
    onFailed(null);
    onFinished(null);
  }
}
