package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Client;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender3D;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.math.Rotation;
import xyz.Melody.Utils.math.RotationUtil;
import xyz.Melody.Utils.render.RenderUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Mining.GoldNuker;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

public class AutoGold extends Module {
  private static AutoGold INSTANCE;
  private final TimerUtil walkTimer;
  private final Numbers<Double> walkTime = new Numbers<>("WalkTime(s)", 0.4, 0.0, 1.0, 0.05);
  private int findingGoldTick = -1;
  private int rotateDoneTick = -2147483647;
  private int rotateToGoldDoneTick = -2147483647;
  private BlockPos targetBlock = null;

  private int prevItem;


  public AutoGold() {
    super("AutoGold", new String[]{""}, ModuleType.Mining);

    this.addValues(walkTime);
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
    rotateDoneTick = 0;
    if (findingGoldTick < 0) {
      // 最复杂的部分
      findingGoldTick = 0;
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
      KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);

      if (ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
        ModuleManager.getModuleByName("GoldNuker").setEnabled(false);
      }

      if (this.mc.thePlayer.getHeldItem() != null && !ItemUtils.getSkyBlockID(this.mc.thePlayer.getHeldItem()).equals("ASPECT_OF_THE_VOID") || this.mc.thePlayer.getHeldItem() == null) {
        for (int i = 0; i < 9; ++i) {
          ItemStack itemStack = this.mc.thePlayer.inventory.mainInventory[i];
          if (itemStack != null && itemStack.getItem() != null && ItemUtils.getSkyBlockID(itemStack).equals("ASPECT_OF_THE_VOID")) {
            prevItem = this.mc.thePlayer.inventory.currentItem;
            this.mc.thePlayer.inventory.currentItem = i;
            break;
          }
        }
      }

      MelodySkyPlus.rotationLib.setSpeedCoefficient(3.0F);
      MelodySkyPlus.rotationLib.setTargetRotation(gotoAir());
      MelodySkyPlus.rotationLib.startRotating();
      MelodySkyPlus.rotationLib.setCallBack(() -> Objects.requireNonNull(AutoGold.getINSTANCE()).rotateDone());
    }
  }

  public void rotateDone() {
    rotateDoneTick = findingGoldTick + 4 /*模拟正常人类反应速度*/;
  }

  public void rotateToGoldDone() {
    rotateToGoldDoneTick = findingGoldTick + 10 /*让人类可以看清*/;
  }

  private Rotation gotoAir() {
    SidesAroundPlayer sidesAroundPlayer = new SidesAroundPlayer();

    BlockPos checkingBlockPosHead = mc.thePlayer.getPosition().up(); // head block
    BlockPos checkingBlockPosFoot = mc.thePlayer.getPosition(); // foot block
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 12; j++) {
        if (i == 0) {
          checkingBlockPosHead = checkingBlockPosHead.south();
          checkingBlockPosFoot = checkingBlockPosFoot.south();
        } else if (i == 1) {
          checkingBlockPosHead = checkingBlockPosHead.west();
          checkingBlockPosFoot = checkingBlockPosFoot.west();
        } else if (i == 2) {
          checkingBlockPosHead = checkingBlockPosHead.north();
          checkingBlockPosFoot = checkingBlockPosFoot.north();
        } else {
          checkingBlockPosHead = checkingBlockPosHead.east();
          checkingBlockPosFoot = checkingBlockPosFoot.east();
        }

        if (mc.theWorld.getBlockState(checkingBlockPosHead).getBlock() != Blocks.air) {
          break;
        }
        if (mc.theWorld.getBlockState(checkingBlockPosFoot).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          sidesAroundPlayer.south += 1;
        } else if (i == 1) {
          sidesAroundPlayer.west += 1;
        } else if (i == 2) {
          sidesAroundPlayer.north += 1;
        } else {
          sidesAroundPlayer.east += 1;
        }
      }
    }

    checkingBlockPosHead = mc.thePlayer.getPosition().up(); // head block
    checkingBlockPosFoot = mc.thePlayer.getPosition(); // foot block
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 9 /* 12 / √2 */; j++) {
        if (i == 0) {
          checkingBlockPosHead = checkingBlockPosHead.south();
          checkingBlockPosFoot = checkingBlockPosFoot.south();
        } else if (i == 1) {
          checkingBlockPosHead = checkingBlockPosHead.north();
          checkingBlockPosFoot = checkingBlockPosFoot.north();
        } else if (i == 2) {
          checkingBlockPosHead = checkingBlockPosHead.north();
          checkingBlockPosFoot = checkingBlockPosFoot.north();
        } else {
          checkingBlockPosHead = checkingBlockPosHead.south();
          checkingBlockPosFoot = checkingBlockPosFoot.south();
        }

        if (mc.theWorld.getBlockState(checkingBlockPosHead).getBlock() != Blocks.air) {
          break;
        }
        if (mc.theWorld.getBlockState(checkingBlockPosFoot).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          checkingBlockPosHead = checkingBlockPosHead.west();
          checkingBlockPosFoot = checkingBlockPosFoot.west();
        } else if (i == 1) {
          checkingBlockPosHead = checkingBlockPosHead.west();
          checkingBlockPosFoot = checkingBlockPosFoot.west();
        } else if (i == 2) {
          checkingBlockPosHead = checkingBlockPosHead.east();
          checkingBlockPosFoot = checkingBlockPosFoot.east();
        } else {
          checkingBlockPosHead = checkingBlockPosHead.east();
          checkingBlockPosFoot = checkingBlockPosFoot.east();
        }

        if (mc.theWorld.getBlockState(checkingBlockPosHead).getBlock() != Blocks.air) {
          break;
        }
        if (mc.theWorld.getBlockState(checkingBlockPosFoot).getBlock() != Blocks.air) {
          break;
        }

        if (i == 0) {
          sidesAroundPlayer.southwest += Math.sqrt(2);
        } else if (i == 1) {
          sidesAroundPlayer.northwest += Math.sqrt(2);
        } else if (i == 2) {
          sidesAroundPlayer.northeast += Math.sqrt(2);
        } else {
          sidesAroundPlayer.southeast += Math.sqrt(2);
        }
      }
    }

    return new Rotation(sidesAroundPlayer.getMaxSideYaw(), (new Random().nextBoolean() ? -1 : 1) * new Random().nextInt(45));
  }

  @EventHandler
  public void onRender(EventRender3D event) {
    if (this.targetBlock != null) {
      RenderUtil.drawFullBlockESP(this.targetBlock, new Color(44, 125, 173, 200), event.getPartialTicks());
    }
  }

  @EventHandler
  private void onTick(EventTick event) {
    if (ModuleManager.getModuleByName("AutoGold").isEnabled()) {
      if (findingGoldTick >= 0) {
        findingGoldTick++;
        if (rotateDoneTick == findingGoldTick) {
          // 如果已经旋转完毕了
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), true);
        }
        if (findingGoldTick >= rotateDoneTick && findingGoldTick <= rotateDoneTick + 60 && rotateDoneTick != -2147483647) {
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), true);
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
          if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
          }
        }
        if (findingGoldTick == rotateDoneTick + 60) {
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);
        }
        if (findingGoldTick == rotateDoneTick + 120) {
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);

          try {
            // 我们先检测周围是否有金子
            GoldNuker goldNuker = (GoldNuker) ModuleManager.getModuleByName("GoldNuker");
            Field field = GoldNuker.class.getDeclaredField("range");
            field.setAccessible(true);
            Numbers<Double> range = (Numbers<Double>) field.get(goldNuker);

            for (BlockPos blockPos : BlockPos.getAllInBox(
                mc.thePlayer.getPosition().up().add(-1 * range.getValue(), -1 * range.getValue(), -1 * range.getValue()),
                mc.thePlayer.getPosition().up().add(range.getValue(), range.getValue(), range.getValue())
            )) {
              if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.gold_block) {
                resume();
                return;
              }
            }
          } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
          rotateToGold();
        }
        if (findingGoldTick == rotateToGoldDoneTick) {
          Client.rightClick();
        }
        if (findingGoldTick == rotateToGoldDoneTick + 20) {
          this.mc.thePlayer.inventory.currentItem = prevItem;
          KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), true);
        }

        if (findingGoldTick == rotateToGoldDoneTick + 40) {
          resume();
        }
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
  }

  private void resume() {
    findingGoldTick = -1;
    rotateDoneTick = -2147483647;
    rotateToGoldDoneTick = -2147483647;
    targetBlock = null;
    if (!ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(true);
    }
  }


  private void rotateToGold() {
    BlockPos replaceBlock = null;
    for (BlockPos blockPos : BlockPos.getAllInBox(mc.thePlayer.getPosition().add(-35, -6, -35), mc.thePlayer.getPosition().add(35, 5, 35))) {
      // 寻找下一个金子
      if (mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.air) {
        if (RotationUtil.rayTrace(blockPos)) {
          if (mc.theWorld.getBlockState(blockPos.up()).getBlock() == Blocks.air && mc.theWorld.getBlockState(blockPos.up().up()).getBlock() == Blocks.air) {

            if (mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.gold_block) {
              int aroundBlocks = 0;
              if (mc.theWorld.getBlockState(blockPos.down()).getBlock() == Blocks.gold_block) {
                aroundBlocks++;
              }
              if (mc.theWorld.getBlockState(blockPos.east()).getBlock() == Blocks.gold_block) {
                aroundBlocks++;
              }
              if (mc.theWorld.getBlockState(blockPos.north()).getBlock() == Blocks.gold_block) {
                aroundBlocks++;
              }
              if (mc.theWorld.getBlockState(blockPos.west()).getBlock() == Blocks.gold_block) {
                aroundBlocks++;
              }
              if (mc.theWorld.getBlockState(blockPos.south()).getBlock() == Blocks.gold_block) {
                aroundBlocks++;
              }

              if (aroundBlocks >= 2) {

                this.targetBlock = blockPos;
                // 周围有东西
                // 找到了targetBlock
                MelodySkyPlus.rotationLib.setSpeedCoefficient(2F);
                MelodySkyPlus.rotationLib.setTargetRotation(RotationUtil.posToRotation(blockPos));
                MelodySkyPlus.rotationLib.startRotating();
                MelodySkyPlus.rotationLib.setCallBack(() -> Objects.requireNonNull(AutoGold.getINSTANCE()).rotateToGoldDone());
                return;
              }
            } else {
              replaceBlock = blockPos;
            }
          }
        }
      }
    }

    if (replaceBlock != null) {

      MelodySkyPlus.rotationLib.setSpeedCoefficient(2F);
      MelodySkyPlus.rotationLib.setTargetRotation(RotationUtil.posToRotation(replaceBlock));
      MelodySkyPlus.rotationLib.startRotating();
      MelodySkyPlus.rotationLib.setCallBack(() -> Objects.requireNonNull(AutoGold.getINSTANCE()).rotateToGoldDone());
    } else {
      Helper.sendMessage("Cannot Find Gold Blocks at all. Maybe you're out of Mines of Divan.");

    }
  }


  @Override
  public void onDisable() {
    findingGoldTick = -1;
    rotateDoneTick = -2147483647;
    rotateToGoldDoneTick = -2147483647;
    if (ModuleManager.getModuleByName("GoldNuker").isEnabled()) {
      ModuleManager.getModuleByName("GoldNuker").setEnabled(false);
    }
    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindSneak.getKeyCode(), false);
    KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindForward.getKeyCode(), false);
  }

  @Override
  public void onEnable() {
    findingGoldTick = -1;
    rotateDoneTick = -2147483647;
    rotateToGoldDoneTick = -2147483647;
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

  public float getMaxSideYaw() {
    double max = 0;
    float yaw = 0;

    if (south > max) {
      max = south;
      yaw = 0;
    }
    if (west > max) {
      max = west;
      yaw = 90;
    }
    if (north > max) {
      max = north;
      yaw = 180;
    }
    if (east > max) {
      max = east;
      yaw = -90;
    }
    if (southwest > max) {
      max = southwest;
      yaw = 45;
    }
    if (northwest > max) {
      max = northwest;
      yaw = 135;
    }
    if (northeast > max) {
      max = northeast;
      yaw = -135;
    }
    if (southeast > max) {
      yaw = -45;
    }

    return yaw;
  }
}
