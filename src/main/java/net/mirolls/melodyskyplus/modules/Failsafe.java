package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.react.BedrockBoatCheck;
import net.mirolls.melodyskyplus.react.FakePlayerCheckReact;
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
  public TextValue<String> fakePlayerCheckMessage = new TextValue<>("FakePlayerMessage", "wtf?,???,????,wtf???,?,t??,w?");
  private long lastLegitTeleport = 0;
  private long nowTick = -1;
  private BlockPos lastLocation = null;
  private boolean reacting = false;

  public Failsafe() {
    super("Failsafe", ModuleType.QOL);
    this.addValues(sysNotification, resumeTime, fakePlayerCheckMessage);
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

  private void checkMarcoChecked() {
    Object[] info = CustomPlayerInRange.redirectPlayerInRange(true, 20, true);

    if (!reacting) {
      if ((Boolean) info[0]) {
        if (info[1] == mc.thePlayer.getName()) {
          react(true);
          FakePlayerCheckReact.react(CustomPlayerInRange.findPlayer((String) info[1]), fakePlayerCheckMessage.getValue());
        }
      } else if (info[2] != "NOT_THIS") {
        // 假人
        // 注册检查器,确认玩家是否飞行
        EntityPlayer targetPlayer = CustomPlayerInRange.findPlayer((String) info[2]);

        MelodySkyPlus.checkPlayerFlying.resetCheck();
        MelodySkyPlus.checkPlayerFlying.setPlayer(targetPlayer);
        MelodySkyPlus.checkPlayerFlying.setChecking(true);
        MelodySkyPlus.checkPlayerFlying.setCallBack(result -> {
          if (targetPlayer != null && result && MathUtil.distanceToEntity(targetPlayer, mc.thePlayer) < 3) {
            react(false);
            FakePlayerCheckReact.react(CustomPlayerInRange.findPlayer((String) info[1]), fakePlayerCheckMessage.getValue());
          } // else: 正常假人 直接忽略
        });
      }

      // 记录lastLocation (failsafe的一部分)
      boolean legitTeleporting =
          Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_VOID")
              || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_END")
              || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "GRAPPLING_HOOK")
              || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_LEECH");

      if (nowTick - lastLegitTeleport > 15) {
        if (lastLocation != null && MathUtil.distanceToPos(lastLocation, mc.thePlayer.playerLocation) > 5) {
          // 1 tick 你最多走5米吧 你就算1s走15m你1tick也只能走0.75米 你能走5m都是超人了
          // 判定为macro checked
          BedrockBoatCheck.react();
        }
      }

      lastLocation = mc.thePlayer.playerLocation;
      lastLegitTeleport = legitTeleporting ? nowTick : lastLegitTeleport;
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
    this.mods.clear();
    super.onDisable();
  }

  public void onEnable() {
    this.resumeTimer.reset();
    this.reacting = false;
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
  }
}


