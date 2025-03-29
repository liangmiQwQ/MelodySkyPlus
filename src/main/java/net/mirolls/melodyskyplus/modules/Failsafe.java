package net.mirolls.melodyskyplus.modules;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.libs.CustomPlayerInRange;
import net.mirolls.melodyskyplus.libs.TPCheckDetector;
import net.mirolls.melodyskyplus.react.failsafe.BedrockBoatReact;
import net.mirolls.melodyskyplus.react.failsafe.BedrockHouseReact;
import net.mirolls.melodyskyplus.react.failsafe.FakePlayerCheckReact;
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
import xyz.Melody.Utils.WindowsNotification;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.math.MathUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.FMLModules.PlayerSoundHandler;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

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
  public TextValue<String> TPCheckMessage = new TextValue<>("TPCheckMessage", "wtf?,???,????,wtf???,?,t??,w?");
  public long nowTick = 0;
  public long lastLegitTeleport = -16;
  public long lastHurt = -16;
  public long lastJump = -16;
  //  private BlockPos lastLocation = null;
  private boolean reacting = false;
//  private boolean nextCheckTP = false;


  public Failsafe() {
    super("Failsafe", ModuleType.Mining);
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
    antiTPCheck = new Option<>("AntiTPCheck", true, (val) -> {
      if (getINSTANCE() != null) {
        INSTANCE.TPCheckMessage.setEnabled(true);
      }
    });
    this.addValues(sysNotification, resumeTime, antiFakePlayerCheck, fakePlayerCheckMessage, antiBedrockBoatCheck, bedrockCheckMessage, antiTPCheck, TPCheckMessage);
    this.setModInfo("Anti staffs while doing macro (ONLY WORK ON AUTO_GEMSTONE).");
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
    return AutoRuby.getINSTANCE().isEnabled() && AutoRuby.getINSTANCE().started && GemstoneNuker.getINSTANCE().isEnabled();
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

  private void checkMarcoChecked() {
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
        int x = (int) (mc.thePlayer.posX - mc.thePlayer.posX % 1 - 1);
        int y = (int) (mc.thePlayer.posY - mc.thePlayer.posY % 1);
        int z = (int) (mc.thePlayer.posZ - mc.thePlayer.posZ % 1 - 1);
        BlockPos posPlayer = new BlockPos(x, y, z); // Minecraft提供的.getPosition不好用 返回的位置经常有较大的误差 这样是最保险的
        BlockPos blockPosDown = posPlayer.down();
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


        if (antiTPCheck.getValue() && nowTick > 20) {
          // 记录lastLocation
          boolean legitTeleporting =
              Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_VOID")
                  || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_END")
                  || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "GRAPPLING_HOOK")
                  || Objects.equals(ItemUtils.getSkyBlockID(mc.thePlayer.inventory.getCurrentItem()), "ASPECT_OF_THE_LEECH");

          GameSettings gameSettings = this.mc.gameSettings;
          lastLegitTeleport = legitTeleporting ? nowTick : lastLegitTeleport;

          lastJump = gameSettings.keyBindJump.isKeyDown() ? nowTick : lastJump;

          int warnLevel = TPCheckDetector.checkPositionChange();
          if (warnLevel > 0 && nowTick > 20 && nowTick - lastLegitTeleport > 40 && nowTick - lastHurt > 30) {
            int checkMotion = TPCheckDetector.checkMotion();
            if ((checkMotion != 0 && (warnLevel += checkMotion) > 3) || warnLevel > 19) { // 如果能通过Motion发现这个事情不是很对劲了
              int checkVelocity = TPCheckDetector.checkVelocity();
              int checkEnvironmentChange = TPCheckDetector.checkEnvironmentChange();

              warnLevel += checkVelocity;
              warnLevel += checkEnvironmentChange;

              if (warnLevel >= 20) {
                // 多个检测 你都有点问题你可以去死了
                Helper.sendMessage("Bad Luck. You was checked by Admin through TP. "
                    + "WarnLevelL: " + warnLevel + "; CheckMotion" + checkMotion + "; CheckVelocity" + checkVelocity + "; CheckEnvironmentChange" + checkEnvironmentChange);
                react(true);
                TPCheckReact.react(TPCheckMessage.getValue());
              } else {
                if (nowTick - lastJump > 50 && mc.thePlayer.fallDistance < 0.1) {
                  if (!gameSettings.keyBindForward.isKeyDown() && !gameSettings.keyBindBack.isKeyDown() && !gameSettings.keyBindRight.isKeyDown() && !gameSettings.keyBindLeft.isKeyDown()) {
                    if (!mc.thePlayer.capabilities.isFlying) {
                      if (!mc.thePlayer.isInLava()
                          && !mc.thePlayer.isPotionActive(Potion.jump)
                          && !mc.thePlayer.capabilities.isFlying
                          && !mc.thePlayer.isRiding() && mc.thePlayer.onGround) {
                        react(true);
                        TPCheckReact.react(TPCheckMessage.getValue());
                      }
                    }
                  }
                }
              }
            }
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

    nowTick++;
  }


  @EventHandler
  public void onAttack(LivingAttackEvent event) {
    if (event.entity instanceof EntityPlayer) {
      if (event.entity.getUniqueID() == mc.thePlayer.getUniqueID()) {
        // 玩家被殴打了
        lastHurt = nowTick;
      }
    }
  }

  @EventHandler
  public void onTick(EventTick event) {
    this.checkMarcoChecked();
    if (nowTick % 20 == 0) {
      TPCheckDetector.saveEnvironment();
    }
    TPCheckDetector.lastMotionZ = mc.thePlayer.motionZ;
    TPCheckDetector.lastMotionX = mc.thePlayer.motionX;

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
    this.lastHurt = -16;
    this.nowTick = 0;
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
    nowTick = 0;
    lastLegitTeleport = -16;
    lastHurt = -16;
  }
}
