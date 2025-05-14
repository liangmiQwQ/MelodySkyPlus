package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.client.ModulePlus;
import net.mirolls.melodyskyplus.event.ClientPacketEvent;
import net.mirolls.melodyskyplus.utils.BlockUtils;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoHollow extends ModulePlus {
  public static AutoHollow INSTANCE;
  public Stage stage;
  public boolean started = false;
  public int currentIndex;
  public List<BlockPos> stones = new ArrayList<>();
  public TimerUtil packetSendTimer = new TimerUtil();
  public List<BlockPos> posesMined = new ArrayList<>();

  public Numbers<Double> pickaxeSlot = new Numbers<>("Pickaxe Slot", 3.0, 1.0, 9.0, 1.0);
  public Numbers<Double> aotvSold = new Numbers<>("Aotv Slot", 2.0, 1.0, 9.0, 1.0);
  public Option<Boolean> blatant = new Option<>("Blatant", false);

  public AutoHollow() {
    super("AutoHollow", ModuleType.Mining);
    this.setModInfo("Auto dig a hollow to use AutoGemstone. ");
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

        if (packetSendTimer.hasReached(1000)) {
          stones = filterAir(stones);
          stage = Stage.PACKET_MINE_FIRST;
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
        }
      } else if (stage == Stage.PACKET_MINE_FIRST) {
        if (!stones.isEmpty()) {
          BlockPos pos = stones.get(0);

          if (PlayerUtils.distanceToPos(pos) < 5 && PlayerUtils.rayTrace(pos)) {
            Rotation rotation = RotationUtil.posToRotation(pos);

            mc.thePlayer.rotationYaw = PlayerUtils.smoothRotation(mc.thePlayer.rotationYaw, rotation.getYaw(), 40F);
            mc.thePlayer.rotationPitch = PlayerUtils.smoothRotation(mc.thePlayer.rotationPitch, rotation.getPitch(), 30F);

            if (RotationUtil.isLookingAtBlock(pos)) {
              if (!posesMined.contains(pos)) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, mc.thePlayer.getHorizontalFacing()));
                posesMined.add(pos);
              }
              mc.thePlayer.swingItem();
            }
          } else {
            Helper.sendMessage("Finished Packet Mine");
            clear();
          }
        } else {
          Helper.sendMessage("Start To Go To End");
          stage = Stage.GO_TO_END;
        }
      }
    }
  }

  @SubscribeEvent
  public void onSendPacket(ClientPacketEvent event) {
    if (event.packet instanceof C07PacketPlayerDigging) {
      packetSendTimer.reset();
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

      stones = filterAir(BlockUtils.getBlocksBetween(eyes, end));
      posesMined.clear();
      started = true;
      stage = Stage.LEFT_CLICK_MINE;
      currentIndex = index;
      packetSendTimer.reset().pause();
    } else {
      Helper.sendMessage("Please stand on a point to start AutoHollow. ");
    }
  }

  public List<BlockPos> filterAir(List<BlockPos> pos) {
    return pos.stream().filter((e) ->
        mc.theWorld.getBlockState(e).getBlock() == Blocks.stone
    ).collect(Collectors.toList());
  }

  public void clear() {
    stage = null;
    packetSendTimer.reset();
    started = false;
    stones.clear();
    posesMined.clear();
    currentIndex = -1;
  }

  public void next() {
    // 对比start 减少了stones的设置 并且started也变成了条件
    if (started) {
      stage = Stage.LEFT_CLICK_MINE;
      packetSendTimer.reset().pause();
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

