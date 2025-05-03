package net.mirolls.melodyskyplus.modules;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.event.ServerPacketEvent;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.react.failsafe.BedrockBoatReact;
import net.mirolls.melodyskyplus.react.failsafe.BedrockHouseReact;
import net.mirolls.melodyskyplus.react.failsafe.FakePlayerCheckReact;
import net.mirolls.melodyskyplus.react.failsafe.TPCheckReact;
import net.mirolls.melodyskyplus.utils.PlayerUtils;
import xyz.Melody.Client;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.TextValue;
import xyz.Melody.GUI.Notification.NotificationPublisher;
import xyz.Melody.GUI.Notification.NotificationType;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.WindowsNotification;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.FMLModules.PlayerSoundHandler;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

public class Failsafe extends Module {
  private static Failsafe INSTANCE;
  private final ArrayList<Module> mods = new ArrayList<>();
  private final TimerUtil resumeTimer = (new TimerUtil()).reset();
  public Option<Boolean> sysNotification = new Option<>("System Notification", true);
  public Numbers<Double> resumeTime = new Numbers<>("Time Resume(s)", 300.0, 60.0, 600.0, 10.0);
  public Option<Boolean> antiFakePlayerCheck;
  public TextValue<String> fakePlayerCheckMessage = new TextValue<>("FakePlayerMessage", "wtf?,???,????,wtf???,?,t??,w?");
  public Option<Boolean> antiBedrockBoatCheck;
  public TextValue<String> bedrockCheckMessage = new TextValue<>("BedrockBoatMessage", "wtf?,???,????,wtf???,?,t??,w?");
  public Option<Boolean> antiTPCheck;
  public Numbers<Double> TPCheckDistance = new Numbers<>("TPCheckDistance", 4.0, 0.1, 20.0, 0.1);

  public TextValue<String> TPCheckMessage = new TextValue<>("TPCheckMessage", "wtf?,???,????,wtf???,?,t??,w?");
  public long lastLegitTeleport = -16;
  private boolean reacting = false;


  public Failsafe() {
    super("Failsafe", ModuleType.Mining);
    antiFakePlayerCheck = new Option<>("AntiFakePlayerCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.fakePlayerCheckMessage.setEnabled(val);
      }
    });
    antiBedrockBoatCheck = new Option<>("AntiBedrockBoatCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.bedrockCheckMessage.setEnabled(val);
      }
    });
    antiTPCheck = new Option<>("AntiTPCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.TPCheckDistance.setEnabled(val);
        INSTANCE.TPCheckMessage.setEnabled(val);
      }
    });
    this.addValues(sysNotification, resumeTime, antiFakePlayerCheck, fakePlayerCheckMessage, antiBedrockBoatCheck, bedrockCheckMessage, antiTPCheck, TPCheckDistance, TPCheckMessage);
    this.setModInfo("Anti-staff while doing macros.");
    this.except();
  }

  public static Failsafe getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != Failsafe.class);

      INSTANCE = (Failsafe) m;
    }
    return INSTANCE;
  }

  private static boolean isDoingMarco() {
    return AutoRuby.getINSTANCE().isEnabled() && AutoRuby.getINSTANCE().started;
  }

  private void reactBedrock() {
    Minecraft mc = Minecraft.getMinecraft();

    BlockPos blockPosTesting = mc.thePlayer.getPosition();

    boolean bedrockHouse = false;
    // 先分清楚到底是基岩房子还是基岩船
    for (int i = 0; i < 5; i++) {
      blockPosTesting = blockPosTesting.up();

      if (Objects.equals(mc.theWorld.getBlockState(blockPosTesting).getBlock().getRegistryName(),
          Blocks.bedrock.getRegistryName())) {
        bedrockHouse = true;
        break;
      }
    }

    if (bedrockHouse) {
      react(true);
      BedrockHouseReact.react();
    } else {
      if (antiBedrockBoatCheck.getValue()) {
        react(true);
        BedrockBoatReact.react(bedrockCheckMessage.getValue());
      }
    }
  }

  private void tickFailsafe() {
    Object[] info = antiFakePlayerCheck.getValue() ? CustomPlayerInRange.redirectPlayerInRange(true, 20, true) : null;

    if (!reacting) {
      if (isDoingMarco()) {
        if (antiFakePlayerCheck.getValue() && info != null) {
          if ((Boolean) info[0]) {
            if (info[1] == mc.thePlayer.getName()) {
              react(true);
              FakePlayerCheckReact.react(() -> MathUtil.distanceToEntity(mc.thePlayer, Objects.requireNonNull(CustomPlayerInRange.findPlayer((String) info[1]))) < 50
                  , fakePlayerCheckMessage.getValue());
              return;
            }
          } else if (info[2] != "NOT_THIS") {
            // 假人
            // 注册检查器,确认玩家是否飞行
            EntityPlayer targetPlayer = CustomPlayerInRange.findPlayer((String) info[2]);

            MelodySkyPlus.checkPlayerFlying.resetCheck();
            MelodySkyPlus.checkPlayerFlying.setPlayer(targetPlayer);
            MelodySkyPlus.checkPlayerFlying.setChecking(true);
            MelodySkyPlus.checkPlayerFlying.setCallBack(result -> {
              if (targetPlayer != null && result && MathUtil.distanceToEntity(targetPlayer, mc.thePlayer) < 4) {
                react(false);
                FakePlayerCheckReact.react(() -> MathUtil.distanceToEntity(mc.thePlayer, Objects.requireNonNull(CustomPlayerInRange.findPlayer((String) info[1]))) < 50,
                    fakePlayerCheckMessage.getValue());
              } // else: 正常假人 直接忽略
            });
          }
        }

        // 先进行基岩部分的检查
        // 如果基岩部分检查出来有问题就不进行下一部分的检查了
        BlockPos blockPosDown = PlayerUtils.getPlayerLocation().down();
        Block blockDown = mc.theWorld.getBlockState(blockPosDown).getBlock();
        if (Objects.equals(blockDown.getRegistryName(), Blocks.bedrock.getRegistryName())) {
          // 如果脚底下的方块是基岩
          if (blockPosDown.getY() == 30) {
            // 检测周围的方块, 避免tp到最底下了了
            BlockPos blockPosTesting = mc.thePlayer.getPosition();
            boolean bedrockTest = false;
            for (int i = 0; i < 9; i++) {
              if (Objects.equals(mc.theWorld.getBlockState(blockPosTesting).getBlock().getRegistryName(),
                  Blocks.bedrock.getRegistryName())) {
                bedrockTest = true;
                break;
              }
              blockPosTesting = blockPosTesting.east();
            }

            if (bedrockTest) {
              // 是基岩船或者基岩房子
              reactBedrock();
              return;
            } // else: 正常走到基岩上了 忽略
          } else {
            // 绝对是了 洗不了
            reactBedrock();
            return;
          }
        }

      }
    } else if (this.resumeTimer.hasReached(this.resumeTime.getValue() * 1000.0)) {
      // 检查完毕了 恢复运转
      this.reEnableMacros();
      NotificationPublisher.queue("Melody+ Failsafe", "Macros resumed.", NotificationType.INFO, 5000);
      if (this.sysNotification.getValue()) {
        WindowsNotification.show("Melody+ Failsafe", "Macros resumed.");
      }

      this.reacting = false;
      this.resumeTimer.reset();
    }
  }


  private void react(boolean delay) {
    if (!reacting) { // 这个前提是为了防止部分react同时触发(考虑到假人飞行的问题)
      resumeTimer.reset();
      Minecraft mc = Minecraft.getMinecraft();

      try {
        PlayerSoundHandler.addSound("mob.ghast.charge", 5.0F, 1.5F, 5);
        PlayerSoundHandler.addSound("mob.ghast.death", 5.0F, 1.5F, 5);
        PlayerSoundHandler.addSound("mob.ghast.scream", 5.0F, 1.5F, 5);

        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.charge")));
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.death")));
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("mob.ghast.scream")));
      } catch (RuntimeException e) {
        MelodySkyPlus.LOGGER.error("Cannot play sounds while macro check!");
      }


      this.reacting = true;
      new Thread(() -> {
        try {
          Thread.sleep(100L);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);

          if (delay) {
            Thread.sleep(400 + new Random().nextInt(1000));
          }
          this.disableMacros();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
      Client.warn();
      Helper.sendMessage("[Melody+ Failsafe] Alert! Macro Check! ");
      NotificationPublisher.queue("Melody+ Failsafe", "Alert! Macro Check!", NotificationType.ERROR, (int) (resumeTime.getValue() - 1) * 1000);
      if (this.sysNotification.getValue()) {
        WindowsNotification.show("Melody+ Failsafe", "Alert! Macro Check!");
      }
    }
  }

  private void disableMacros() {
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Mining)) {
      disbandOneMacro(mod);
    }
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Farming)) {
      disbandOneMacro(mod);
    }
    for (Module mod : ModuleManager.getModulesInType(ModuleType.Fishing)) {
      disbandOneMacro(mod);
    }
  }

  public void onDisable() {
    MelodySkyPlus.LOGGER.info("onDisable is running");
    // 同时这里打断所有正在运行的React
    MelodySkyPlus.rotationLib.stop();
    MelodySkyPlus.walkLib.stop();
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);

    this.mods.clear();
    super.onDisable();
  }

  public void onEnable() {
    this.resumeTimer.reset();
    this.reacting = false;
    this.lastLegitTeleport = -16;
    super.onEnable();
  }

  private void disbandOneMacro(Module mod) {
    if (mod != this && mod.isEnabled() && !mod.excepted) {
      mod.setEnabled(false);
      this.mods.add(mod);
    }
  }

  private void reEnableMacros() {
    for (Module mod : this.mods) {
      mod.setEnabled(true);
    }

    this.mods.clear();
  }

  @SubscribeEvent
  public void clear(WorldEvent.Load event) {
    this.reacting = false;
    this.resumeTimer.reset();
    lastLegitTeleport = -16;
  }

  @EventHandler
  public void onTick(EventTick event) {
    this.tickFailsafe();
  }

  @SubscribeEvent
  public void onPacket(ServerPacketEvent event) {
    if (antiTPCheck.getValue()) {
      if (!(event.packet instanceof S08PacketPlayerPosLook)) return;

      S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;

      Vec3 currentPlayerPos = mc.thePlayer.getPositionVector();
      Vec3 packetPlayerPos = new Vec3(
          packet.getX() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X) ? currentPlayerPos.xCoord : 0),
          packet.getY() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y) ? currentPlayerPos.yCoord : 0),
          packet.getZ() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z) ? currentPlayerPos.zCoord : 0)
      );
      double distance = currentPlayerPos.distanceTo(packetPlayerPos);

      if (distance >= TPCheckDistance.getValue()) {
        final double lastReceivedPacketDistance = currentPlayerPos.distanceTo(MelodySkyPlus.packRecord.getLastPacketPosition());
        final double playerMovementSpeed = mc.thePlayer.getAttributeMap().getAttributeInstanceByName("generic.movementSpeed").getAttributeValue();
        final int ticksSinceLastPacket = (int) Math.ceil(MelodySkyPlus.packRecord.getLastPacketTime() / 50D);
        final double estimatedMovement = playerMovementSpeed * ticksSinceLastPacket;
        if (lastReceivedPacketDistance > 7.5D && Math.abs(lastReceivedPacketDistance - estimatedMovement) < 0.5)
          return;
        react(true);
        TPCheckReact.react(TPCheckMessage.getValue());
      }
    }
  }

}
