package net.mirolls.melodyskyplus.modules;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.react.failsafe.BedrockBoatReact;
import net.mirolls.melodyskyplus.react.failsafe.BedrockHouseReact;
import net.mirolls.melodyskyplus.react.failsafe.GeneralReact;
import net.mirolls.melodyskyplus.react.failsafe.TPCheckReact;
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
import xyz.Melody.Utils.Item.ItemUtils;
import xyz.Melody.Utils.WindowsNotification;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.timer.TimerUtil;
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
  public long nowTick = 0;
  public long lastLegitTeleport = -16;
  public long lastJump = -16;

  private BlockPos lastLocation = null;
  private boolean reacting = false;

  public Failsafe() {
    super("Failsafe", ModuleType.QOL);
    antiFakePlayerCheck = new Option<>("AntiFakePlayerCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.fakePlayerCheckMessage.setEnabled(true);
      }
    });
    antiBedrockBoatCheck = new Option<>("AntiBedrockBoatCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.bedrockCheckMessage.setEnabled(true);
      }
    });
    this.addValues(sysNotification, resumeTime, antiFakePlayerCheck, fakePlayerCheckMessage, antiBedrockBoatCheck, bedrockCheckMessage);
    this.setModInfo("Anti staffs.");
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

  private boolean isDoingMarco() {
    boolean returnValue = false;
    for (Module module : ModuleManager.modules) {

      if (module.getName().toLowerCase().contains("nuker") && module.isEnabled() && !module.excepted) {
        if (module.getName().toLowerCase().contains("gemstonenuker") ||
            module.getName().toLowerCase().contains("mithrilnuker")) { // 是宝石努克
          if (AutoRuby.getINSTANCE().isEnabled()) {// 在开启了坳头宝石的情况下的
            if (AutoRuby.getINSTANCE().started) { // 并且运行了.ar start
              returnValue = true;
            }
          } else { // 开启了秘银努克 但是没开坳头宝石
            returnValue = true;
          }
        } else { // 其他努克
          returnValue = true;
        }
      }

      if (module.getName().toLowerCase().contains("autofish") && module.isEnabled()) {
        Helper.sendMessage("1" + module.getName().toLowerCase());
        returnValue = true;
      }
    }

    if (returnValue) {
      this.onEnable();
      return true;
    } else {
      this.onDisable();
      return false;
    }
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

    react(true);
    if (bedrockHouse) {
      BedrockHouseReact.react();
    } else {
      if (antiBedrockBoatCheck.getValue()) {
        BedrockBoatReact.react(bedrockCheckMessage.getValue());
      }
    }
  }

  private void checkMarcoChecked() {
    Object[] info = antiFakePlayerCheck.getValue() ? CustomPlayerInRange.redirectPlayerInRange(true, 20, true) : null;

    if (!reacting) {
      if (isDoingMarco()) {
        if (antiFakePlayerCheck.getValue() && info != null) {
          if ((Boolean) info[0]) {
            if (info[1] == mc.thePlayer.getName()) {
              react(true);
              GeneralReact.react(CustomPlayerInRange.findPlayer((String) info[1]), fakePlayerCheckMessage.getValue());
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
                GeneralReact.react(CustomPlayerInRange.findPlayer((String) info[1]), fakePlayerCheckMessage.getValue());
              } // else: 正常假人 直接忽略
            });
          }
        }

        // 先进行基岩部分的检查
        // 如果基岩部分检查出来有问题就不进行下一部分的检查了
        BlockPos blockPosDown = mc.thePlayer.getPosition().down();
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


        // 记录lastLocation (failsafe的一部分)
        boolean legitTeleporting =
            Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_VOID")
                || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_END")
                || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "GRAPPLING_HOOK")
                || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_LEECH");

        GameSettings gameSettings = this.mc.gameSettings;
        lastLegitTeleport = legitTeleporting ? nowTick : lastLegitTeleport;

        lastJump = gameSettings.keyBindForward.isKeyDown() ? nowTick : lastJump;
        if (nowTick > 20 && nowTick - lastLegitTeleport > 20 && nowTick - lastJump > 20) {
          if (!gameSettings.keyBindForward.isKeyDown() && !gameSettings.keyBindBack.isKeyDown() && !gameSettings.keyBindRight.isKeyDown() && !gameSettings.keyBindLeft.isKeyDown()) {
            if (mc.thePlayer.fallDistance < 0.8) {
              if (!mc.thePlayer.capabilities.isFlying) {
                if (lastLocation != null && MathUtil.distanceToPos(lastLocation, mc.thePlayer.getPosition()) > 0.8) {
                  // 1 tick 你最多走5米吧 你就算1s走15m你1tick也只能走0.75米 你能走5m都是超人了
                  // 判定为macro checked
                  // 直接上TPReact了 因为基岩已经另外处理过了
                  react(true);
                  TPCheckReact.react();
                }
              }
            }
          }
        }

        lastLocation = mc.thePlayer.getPosition();
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

    nowTick++;
  }

  @EventHandler
  public void onTick(EventTick event) {
    this.checkMarcoChecked();
  }

  private void react(boolean delay) {
    if (!reacting) { // 这个前提是为了防止部分react同时触发(考虑到假人飞行的问题)
      resumeTimer.reset();
      Minecraft mc = Minecraft.getMinecraft();
      this.reacting = true;
      new Thread(() -> {
        try {
          if (delay) {
            Thread.sleep(500 + new Random().nextInt(1000));
          }
          this.disableMacros();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }).start();
      Client.async(() -> {
        try {
          Thread.sleep(100L);
          KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        } catch (Exception e) {
          MelodySkyPlus.LOGGER.error(e.getMessage());
        }
      });
      Client.warn();
      Helper.sendMessage("[Melody+ Failsafe] Alert! Macro Check! ");
      NotificationPublisher.queue("Melody+ Failsafe", "Alert! Macro Check!", NotificationType.ERROR, 7000);
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
    // 同时这里打断所有正在运行的React
    MelodySkyPlus.rotationLib.stop();
    MelodySkyPlus.walkLib.stop();
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

    this.mods.clear();
    super.onDisable();
  }

  public void onEnable() {
    this.resumeTimer.reset();
    this.reacting = false;
    this.lastLegitTeleport = -16;
    this.nowTick = 0;
    this.lastLocation = null;
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
    lastLocation = null;
    this.reacting = false;
    this.resumeTimer.reset();
    nowTick = 0;
    lastLegitTeleport = -16;
  }
}


