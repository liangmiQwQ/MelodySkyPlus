package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.client.ModulePlus;
import net.mirolls.melodyskyplus.event.ClientPacketEvent;
import net.mirolls.melodyskyplus.utils.BlockStateStoreUtils;
import net.mirolls.melodyskyplus.utils.BlockUtils;
import net.mirolls.melodyskyplus.utils.EtherWarpUtils;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;
import xyz.Melody.Utils.render.RenderUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class AutoHollow extends ModulePlus {
  // static & final
  public static AutoHollow INSTANCE;
  private final PacketManager packetManager = new PacketManager();
  private final List<BlockPos> posesMined = new ArrayList<>();
  // public
  public Stage stage;
  public boolean started = false;
  public int currentIndex;
  // settings
  public Numbers<Double> pickaxeSlot = new Numbers<>("Pickaxe Slot", 3.0, 1.0, 9.0, 1.0);
  public Numbers<Double> aotvSold = new Numbers<>("Aotv Slot", 2.0, 1.0, 9.0, 1.0);
  public Option<Boolean> blatant = new Option<>("Blatant", false);
  // privates
  private List<BlockPos> stones = new ArrayList<>();
  private Set<BlockPos> completeStones = new HashSet<>();
  private List<BlockPos> stonesToMineThisTime = new ArrayList<>();
  private List<BlockPos> etherWarpPoints = new ArrayList<>();

  public AutoHollow() {
    super("AutoHollow", ModuleType.Mining);
    this.setModInfo("Auto dig a hollow to use AutoGemstone. ");
    this.addValues(pickaxeSlot, aotvSold, blatant);
    this.except();
  }

  public static AutoHollow getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoHollow.class);

      INSTANCE = (AutoHollow) m;
    }
    return INSTANCE;
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (AutoRuby.getINSTANCE().wps.size() > 1) {
      for (BlockPos pos : stones) {
        RenderUtil.drawFullBlockESP(pos, new Color(8, 125, 13, 40), event.getPartialTicks());
      }
      for (BlockPos pos : etherWarpPoints) {
        RenderUtil.drawFullBlockESP(pos, new Color(101, 4, 131, 108), event.getPartialTicks());
      }
      for (BlockPos pos : stonesToMineThisTime) {
        RenderUtil.drawFullBlockESP(pos, new Color(0, 255, 244, 40), event.getPartialTicks());
      }
    }
  }

  @EventHandler
  public void onTick(EventPreUpdate event) {
    AutoRuby ar = AutoRuby.getINSTANCE();
    if (started && ar != null) {
      if (stage == Stage.LEFT_CLICK_MINE) {
        Vec3d next;
        if (currentIndex + 1 < AutoRuby.getINSTANCE().wps.size()) {
          next = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(currentIndex + 1));
        } else {
          next = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(0));
        }
        Rotation rotation = RotationUtil.vec3ToRotation(next);

        mc.thePlayer.rotationYaw = PlayerUtils.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 40F);
        mc.thePlayer.rotationPitch = PlayerUtils.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 30F);

        if (Math.abs(mc.thePlayer.rotationYaw - rotation.getYaw()) < 5 && Math.abs(mc.thePlayer.rotationPitch - rotation.getPitch()) < 5) {
          mc.thePlayer.inventory.currentItem = pickaxeSlot.getValue().intValue() - 1;
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
        }

        if ((packetManager.firstMined && packetManager.packetSendTimer.hasReached(1000)) || packetManager.packetSendTimer.hasReached(5000)) {
          stonesToMineThisTime = filterAir(stonesToMineThisTime);
          stage = Stage.PACKET_MINE_FIRST;
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }
      } else if (stage == Stage.PACKET_MINE_FIRST) {
        packetMine(false);
      } else if (stage == Stage.WALK) {
        // 暴力: 使用AOTV移动
        // 非暴力: 走路过去

        BlockStateStoreUtils store = new BlockStateStoreUtils();
        BlockPos target = PlayerUtils.getPlayerLocation();
        double targetDistanceSq = BlockUtils.calcDistanceSq(new Vec3d(target.getX(), target.getY() + mc.thePlayer.getEyeHeight(), target.getZ()), new Vec3d(stones.get(0)));

        for (BlockPos pos : BlockPos.getAllInBox(PlayerUtils.getPlayerLocation().add(-10, -10, -10), PlayerUtils.getPlayerLocation().add(10, 10, 10))) {
          if (store.getBlockState(pos).getBlock().getMaterial().isSolid()) {
            if (store.getBlockState(pos.up()).getBlock() == Blocks.air && store.getBlockState(pos.up().up()).getBlock() == Blocks.air) {

              double posDistanceSq = BlockUtils.calcDistanceSq(new Vec3d(pos.getX(), pos.getY() + mc.thePlayer.getEyeHeight(), pos.getZ()), new Vec3d(stones.get(0)));
              if (targetDistanceSq > posDistanceSq * (completeStones.contains(pos.up()) ? 0.5 : 1)) {
                target = pos;
              }
            }
          }
        }

        if (blatant.getValue()) {
          if (etherWarpPoints.isEmpty()) {
            etherWarpPoints = EtherWarpUtils.findWayToEtherWarp(target, 3, 6, 15);
          }
        }
      }
    }
  }

  @SubscribeEvent
  public void onSendPacket(ClientPacketEvent event) {
    if (event.packet instanceof C07PacketPlayerDigging) {
      packetManager.packetSendTimer.reset();
      packetManager.firstMined = true;
    }
  }

  public void packetMine(boolean next) {
    Runnable onFinished = () -> {
      if (next) {
        next();
      } else {
        stage = Stage.WALK;
      }
    };

    if (!stonesToMineThisTime.isEmpty()) {
      BlockPos pos = stonesToMineThisTime.get(0);

      if (PlayerUtils.distanceToPos(pos) < 5 && PlayerUtils.rayTrace(pos)) {
        Rotation rotation = RotationUtil.posToRotation(pos);

        mc.thePlayer.rotationYaw = PlayerUtils.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 40F);
        mc.thePlayer.rotationPitch = PlayerUtils.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 30F);

        if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.air) {
          if (RotationUtil.isLookingAtBlock(pos)) {
            if (!posesMined.contains(pos)) {
              mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, mc.thePlayer.getHorizontalFacing()));
              posesMined.add(pos);
            }
            mc.thePlayer.swingItem();
          }
        } else {
          stonesToMineThisTime = filterAir(stonesToMineThisTime);
        }

      } else {
        onFinished.run();
      }
    } else {
      if (stones.isEmpty()) {
        stage = Stage.GO_TO_END;
      } else {
        onFinished.run();
      }
    }
  }


  public void start() {
    int index = AutoRuby.getINSTANCE().wps.indexOf(PlayerUtils.getPlayerLocation().down());
    if (index != -1) {
      // 存在
      Vec3d start = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(index));
      Vec3d eyes = new Vec3d(start.getX(), start.getY() + 0.5 + mc.thePlayer.getEyeHeight(), start.getZ());
      Vec3d end;
      if (index + 1 < AutoRuby.getINSTANCE().wps.size()) {
        end = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(index + 1));
      } else {
        end = Vec3d.ofCenter(AutoRuby.getINSTANCE().wps.get(0));
      }

      stones = BlockUtils.getDoubleHeightBlocksBetween(eyes, end);
      completeStones = new HashSet<>(stones);
      posesMined.clear();
      started = true;
      currentIndex = index;
      next();
    } else {
      Helper.sendMessage("Please stand on a point to start AutoHollow. ");
    }
  }

  public List<BlockPos> filterAir(List<BlockPos> pos) {
    return pos.stream().filter((e) ->
        mc.theWorld.getBlockState(e).getBlock() != Blocks.air
    ).collect(Collectors.toList());
  }

  public void clear() {
    stage = null;
    packetManager.reset();
    started = false;
    stones.clear();
    completeStones.clear();
    posesMined.clear();
    currentIndex = -1;
  }

  public void next() {
    // next指的是在一个current中完成一个轮回后运行的代码
    if (started) {
      stage = Stage.LEFT_CLICK_MINE;
      packetManager.reset();

      // filter air
      stones = filterAir(stones);
      // 生成 stonesToMineThisTime
      stonesToMineThisTime = stones.stream().filter((e) -> e.distanceSq(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ) < 25).collect(Collectors.toList());
      // 在stones里移除这些
      stones.removeAll(stonesToMineThisTime);

      if (stonesToMineThisTime.isEmpty()) {
        Helper.sendMessage("Sorry, program has met some trouble, please dig to the next pos by yourself.");
        clear();
      }
    }
  }

  @Override
  public void onEnable() {
    super.onEnable();

    clear();
  }

  public enum Stage {
    LEFT_CLICK_MINE,
    PACKET_MINE_FIRST,
    PACKET_MINE_SECOND,
    WALK,
    FALLING,
    GO_TO_END,
  }
}

class PacketManager {
  public final TimerUtil packetSendTimer = new TimerUtil();
  public boolean firstMined = false;

  public void reset() {
    firstMined = false;
    packetSendTimer.reset();
  }
}