package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;

import java.util.Iterator;

public class AutoGold extends Module {
  private static AutoGold INSTANCE;
  private final TimerUtil walkTimer;
  private int findingGoldTicks;
  private Numbers<Double> walkTime = new Numbers<>("WalkTime(s)", 4.0, 1.0, 10.0, 0.5);


  public AutoGold() {
    super("AutoGold", new String[]{""}, ModuleType.Mining);

    walkTimer = (new TimerUtil()).reset();

    this.setModInfo("Auto mine gold and find path in Mines of Divan");
  }

  public static AutoGold getINSTANCE() {
    if (INSTANCE == null) {
      Iterator<Module> var2 = ModuleManager.modules.iterator();

      Module m;
      do {
        if (!var2.hasNext()) {
          return null;
        }

        m = var2.next();
      } while (m.getClass() != AutoGold.class);

      INSTANCE = (AutoGold) m;
    }
    return INSTANCE;
  }

  public void findGold() {
    if (findingGoldTicks < 0) {
      // 最复杂的部分
      findingGoldTicks = 0;
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);

      if (ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
        ModuleManager.getModuleByName("GoldNuker").setEnabled(false);
      }


    }
  }

  private Rotation gotoAir() {
    // 寻找八个方向 找哪个方向最有出息: 45度斜上角
    SidesAroundPlayer sidesAroundPlayer = new SidesAroundPlayer();

    BlockPos checkingBP = mc.thePlayer.getPosition().up(); // head block
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 9 /* 12 / √2 */; j++) {
        if (i == 0) {
          checkingBP = checkingBP.south();
        } else if (i == 1) {
          checkingBP = checkingBP.west();
        } else if (i == 2) {
          checkingBP = checkingBP.north();
        } else {
          checkingBP = checkingBP.east();
        }

        if (mc.theWorld.getBlockState(checkingBP).getBlock() != Blocks.air) {
          break;
        }
        checkingBP = checkingBP.up();
        if (mc.theWorld.getBlockState(checkingBP).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          sidesAroundPlayer.south += Math.sqrt(2);
        } else if (i == 1) {
          sidesAroundPlayer.west += Math.sqrt(2);
        } else if (i == 2) {
          sidesAroundPlayer.north += Math.sqrt(2);
        } else {
          sidesAroundPlayer.east += Math.sqrt(2);
        }
      }
    }

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 7 /* 12 / √3 */; j++) {
        if (i == 0) {
          checkingBP = checkingBP.south();
        } else if (i == 1) {
          checkingBP = checkingBP.west();
        } else if (i == 2) {
          checkingBP = checkingBP.north();
        } else {
          checkingBP = checkingBP.east();
        }
        if (mc.theWorld.getBlockState(checkingBP).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          checkingBP = checkingBP.west();
        } else if (i == 1) {
          checkingBP = checkingBP.north();
        } else if (i == 2) {
          checkingBP = checkingBP.east();
        } else {
          checkingBP = checkingBP.south();
        }
        if (mc.theWorld.getBlockState(checkingBP).getBlock() != Blocks.air) {
          break;
        }

        checkingBP = checkingBP.up();
        if (mc.theWorld.getBlockState(checkingBP).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          sidesAroundPlayer.southwest += Math.sqrt(3);
        } else if (i == 1) {
          sidesAroundPlayer.northwest += Math.sqrt(3);
        } else if (i == 2) {
          sidesAroundPlayer.northeast += Math.sqrt(3);
        } else {
          sidesAroundPlayer.southeast += Math.sqrt(3);
        }
      }
    }

    return new Rotation(0f, 45f);
  }

  @EventHandler
  private void onTick(EventTick event) {
    if (findingGoldTicks >= 0) {
      findingGoldTicks++;
    } else {
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);

      if (walkTimer.hasReached(1000 - (walkTime.getValue() * 1000)/* 走路的时间 */)) {
        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), true);
      }
      if (walkTimer.hasReached(1000)) {
        walkTimer.reset();
        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);
      }
    }
  }

  @Override
  public void onDisable() {
    if (ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(false);
    }
    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
  }

  @Override
  public void onEnable() {
    SkyblockArea area = new SkyblockArea();
    area.updateCurrentArea();
    if (!area.isIn(Areas.Crystal_Hollows)) {
      ModuleManager.getModuleByName("AutoGold").setEnabled(false);
      return;
    }

    if (!ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(true);
    }
    walkTimer.reset();
  }
}

class SidesAroundPlayer {
  public double south;
  public double west;
  public double north;
  public double east;
  public double southwest;
  public double northwest;
  public double northeast;
  public double southeast;

  public double getMaxSide() {
    double max = 0;

    if (south > max) {
      max = south;
    }
    if (west > max) {
      max = west;
    }
    if (north > max) {
      max = north;
    }
    if (east > max) {
      max = east;
    }
    if (southwest > max) {
      max = southwest;
    }
    if (northwest > max) {
      max = northwest;
    }
    if (northeast > max) {
      max = northeast;
    }
    if (southeast > max) {
      max = southeast;
    }

    return max;
  }
}
