package net.mirolls.melodyskyplus.modules;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.client.ModulePlus;
import net.mirolls.melodyskyplus.libs.AutoHeatStage;
import net.mirolls.melodyskyplus.libs.GuiUtil;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.System.Managers.Client.ModuleManager;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.game.ScoreboardUtils;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.module.Module;
import xyz.Melody.module.ModuleType;
import xyz.Melody.module.modules.macros.Fishing.AutoFish;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;

import java.util.ArrayList;
import java.util.List;

public class AutoHeat extends ModulePlus {
  private final List<Module> mods;
  private final Numbers<Double> heatLimit = new Numbers<>("HeatLimit", 95.0, 1.0, 100.0, 1.0);
  private int reactingTick;
  private AutoHeatStage stage;
  private int prevItem;


  public AutoHeat() {
    super("AutoHeat", ModuleType.Mining);
    this.mods = new ArrayList<>();
    this.reactingTick = -1;
    this.stage = AutoHeatStage.WORKING;

    this.setModInfo("Auto drink and buy water when in mf.");
    this.addValues(heatLimit);
    this.except();
  }

  private static boolean isDoingMarco() {
    AutoFish AutoFishINSTANCE = null;
    for (Module m : ModuleManager.modules) {
      if (m.getClass() == AutoFish.class) {
        AutoFishINSTANCE = (AutoFish) m;
      }
    }

    return (AutoRuby.getINSTANCE().isEnabled() && AutoRuby.getINSTANCE().started) || (AutoFishINSTANCE != null && AutoFishINSTANCE.isEnabled());
  }

  @EventHandler
  public void onTick(EventTick tick) {
    // ====== 以下为检测部分 ======
    List<String> scoreBoard = ScoreboardUtils.getScoreboard();

    int heat = 0;
    for (String line : scoreBoard) {
      if (line.toLowerCase().contains("heat:")) {
        heat = GuiUtil.getHeat(line.replaceAll(".*Heat: §[a-f0-9]", ""));
        break;
      }
    }

    if (heat >= heatLimit.getValue() && reactingTick == -1) {
      // 如果马口开启
      if (isDoingMarco()) {
        Helper.sendMessage("Found heat too high (" + heat + "), start to junk some water.");
        reactingTick = 0;
        stage = AutoHeatStage.DRINKING;
        // 关闭马口
        disableMacros();
        MelodySkyPlus.jasperUsed.setJasperUsed(false);
      }
    }

    // ====== 以下为反应部分 =====
    if (reactingTick > -1) {
      reactingTick++;

      // 喝水处理
      if (stage == AutoHeatStage.DRINKING) {
        if (reactingTick == 5) {
          // 切换物品
          for (int i = 0; i < 9; ++i) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item.getDisplayName().contains("Water") && item.getDisplayName().contains("Bottle")) {
              prevItem = mc.thePlayer.inventory.currentItem;
              mc.thePlayer.inventory.currentItem = i;
            }
          }
        } else if (reactingTick == 15) {
          // 喝水
          ItemStack item = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);

          if (item.getDisplayName().contains("Water") && item.getDisplayName().contains("Bottle")) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
            MelodySkyPlus.drinkingLib.register(() -> {
              reactingTick = 0;
              stage = AutoHeatStage.CALLING;
            });
          } else {
            Helper.sendMessage("Missing Water Bottle in hotbar.");
            reactingTick = -1;
            // 开启马口
            reEnableMacros();
          }
        }
      }


      // 打电话
      if (stage == AutoHeatStage.CALLING) {
        if (reactingTick == 5) {
          // 找手机
          for (int i = 0; i < 9; ++i) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item != null && ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
              mc.thePlayer.inventory.currentItem = i;
            }
          }
        } else if (reactingTick == 15) {
          // 打电话
          ItemStack item = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
          if (ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem));
          } else {
            Helper.sendMessage("Missing AbiPhone in hotbar.");
            reactingTick = -1;
            // 开启马口
            reEnableMacros();
          }
        } else if (reactingTick == 35) {
          // 找人
          GuiScreen gui = mc.currentScreen;
          if (gui instanceof GuiChest) {
            Container container = ((GuiChest) gui).inventorySlots;
            if (container instanceof ContainerChest) {
              String chestName = GuiUtil.getGuiName(gui);
              if (chestName.startsWith("Abiphone")) {
                for (Slot slot : container.inventorySlots) {
                  ItemStack item = slot.getStack(); // 获取item
                  if (StringUtils.stripControlCodes(item.getDisplayName()).equals("Alchemist")) {
                    // 找到对应的人了
                    GuiUtil.clickSlot(slot.slotNumber, 0, 0);
                    break;
                  }
                }
              }
            }
          } else {
            reactingTick = 14; // 重新返回上一步 打开电话
          }
        } else if (reactingTick > 35) {
          // 跳转下一个阶段

          GuiScreen gui = mc.currentScreen;
          if (gui instanceof GuiChest) {
            Container container = ((GuiChest) gui).inventorySlots;
            if (container instanceof ContainerChest) {
              String chestName = GuiUtil.getGuiName(gui);
              if (chestName.startsWith("Alchemist")) {
                // 下个阶段
                reactingTick = 0;
                stage = AutoHeatStage.TRADING;
              }
            }
          }

        }
      }


      // 交易
      if (stage == AutoHeatStage.TRADING) {
        if (reactingTick == 20) {
          // 卖水
          GuiScreen gui = mc.currentScreen;
          if (gui instanceof GuiChest) {
            Container container = ((GuiChest) gui).inventorySlots;
            if (container instanceof ContainerChest) {
              String chestName = GuiUtil.getGuiName(gui);
              if (chestName.startsWith("Alchemist")) {
                for (int i = 0; i < 9; ++i) {
                  ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
                  if (item.getDisplayName().contains("Glass") && item.getDisplayName().contains("Bottle")) {
                    GuiUtil.clickSlot(i + 81, 0, 0);
                  }
                }
              }
            }
          } else {
            mc.thePlayer.closeScreen();
            stage = AutoHeatStage.CALLING;
            reactingTick = 14; // 重新返回上一步 打开电话
          }
        } else if (reactingTick == 50) {
          GuiScreen gui = mc.currentScreen;
          if (gui instanceof GuiChest) {
            Container container = ((GuiChest) gui).inventorySlots;
            if (container instanceof ContainerChest) {
              String chestName = GuiUtil.getGuiName(gui);
              if (chestName.startsWith("Alchemist")) {
                for (Slot slot : container.inventorySlots) {
                  // 买水
                  ItemStack item = slot.getStack(); // 获取item
                  if (StringUtils.stripControlCodes(item.getDisplayName()).contains("Water") && StringUtils.stripControlCodes(item.getDisplayName()).contains("Bottle")) {
                    GuiUtil.clickSlot(slot.getSlotIndex(), 0, 0);
                    // 买水
                    break;
                  }
                }
              }
            }
          } else {
            mc.thePlayer.closeScreen();
            stage = AutoHeatStage.CALLING;
            reactingTick = 14; // 重新返回上一步 打开电话
          }
        } else if (reactingTick == 60) {
          mc.thePlayer.closeScreen();
          Helper.sendMessage("Bought water and drank successfully");
        } else if (reactingTick == 70) {
          mc.thePlayer.inventory.currentItem = prevItem;
          reactingTick = -1;
          // 开启马口
          reEnableMacros();
          stage = AutoHeatStage.WORKING;
        }
      }
    }

  }

  public void onEnable() {
    super.onEnable();

    reactingTick = -1;
    stage = AutoHeatStage.WORKING;
    this.mods.clear();
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
}

