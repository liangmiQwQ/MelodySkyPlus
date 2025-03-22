package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.mirolls.melodyskyplus.libs.AutoRubyTimer;
import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.game.ScoreboardUtils;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("rawtypes")
@Mixin(value = AutoRuby.class, remap = false)
public class AutoRubyMixin {

  private final Numbers<Double> melodySkyPlus$heatLimit = new Numbers<>("HeatLimit", 95.0, 1.0, 100.0, 1.0);
  @Shadow
  private TimerUtil ewTimer;
  @Shadow
  private boolean etherWarped;
  @Shadow
  private BlockPos nextBP;
  @Shadow
  private TimerUtil timer;
  private Option<Boolean> melodySkyPlus$autoHeat = null;
  private int reactingTick = -1;
  private int prevItem;

  @ModifyArg(method = "<init>", remap = false, at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/AutoRuby;addValues([Lxyz/Melody/Event/value/Value;)V"))
  private Value[] init(Value[] originalValues) {
    melodySkyPlus$autoHeat = new Option<>("AutoHeat", false, val -> {
      if (AutoRuby.getINSTANCE() != null) {
        melodySkyPlus$heatLimit.setEnabled(val);
      }
    });

    Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 2);

    returnValues[returnValues.length - 2] = melodySkyPlus$autoHeat;
    returnValues[returnValues.length - 1] = melodySkyPlus$heatLimit;

    return returnValues;
  }


  @Inject(method = "idk", at = @At("HEAD"), remap = false)
  private void idk(EventTick event, CallbackInfo ci) {
    Minecraft mc = Minecraft.getMinecraft();

    if (this.ewTimer.hasReached(0) && !this.etherWarped && GemstoneNuker.getINSTANCE().gemstones.isEmpty() && this.nextBP != null && timer.hasReached(150)) {
      Objects.requireNonNull(Failsafe.getINSTANCE()).lastLegitTeleport = Failsafe.getINSTANCE().nowTick;
    } else {
      // 如果没有在进行TP
      if (melodySkyPlus$autoHeat.getValue() && AutoRubyTimer.timer.hasReached(500)) {
        // 主要部分 处理AutoHeat
        List<String> scoreBoard = ScoreboardUtils.getScoreboard();

        int heat = 0;
        for (String line : scoreBoard) {
          if (line.toLowerCase().contains("heat:")) {
            heat = melodySkyPlus$getHeat(line.replaceAll(".*Heat: §[a-f0-9]", ""));
            break;
          }
        }

        if (AutoRuby.getINSTANCE().started) {
          if (heat >= melodySkyPlus$heatLimit.getValue() && reactingTick == -1) {
            if (mc.thePlayer.posY <= 64 && mc.thePlayer.posY >= 64 - 6) {
              Helper.sendMessage("Found heat too high (" + heat + "), start to junk some water.");
              if (AutoRuby.getINSTANCE().started) {
                reactingTick = 0;
                AutoRuby.getINSTANCE().started = false;
              }
            }
          }
        }
      }
    }

    if (reactingTick > -1) {
      if (reactingTick == 10) {
        for (int i = 0; i < 9; ++i) {
          ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
          if (item != null && ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
            prevItem = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = i;
          }
        }
      } else if (reactingTick == 20) {
        ItemStack item = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
        if (ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
          mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem));
        } else {
          Helper.sendMessage("Missing AbiPhone in hotbar.");
          reactingTick = -1;
          AutoRuby.getINSTANCE().started = true;
        }
      } else if (reactingTick == 30) {
        GuiScreen gui = mc.currentScreen;
        if (gui instanceof GuiChest) {
          Container container = ((GuiChest) gui).inventorySlots;
          if (container instanceof ContainerChest) {
            String chestName = this.melodySkyPlus$getGuiName(gui);
            if (chestName.startsWith("Abiphone")) {
              for (Slot slot : container.inventorySlots) {
                ItemStack item = slot.getStack(); // 获取item
                if (StringUtils.stripControlCodes(item.getDisplayName()).equals("Alchemist")) {
                  // 找到对应的人了
                  melodySkyPlus$clickSlot(slot.slotNumber, 0, 0);
                  break;
                }
              }
            }
          }
        } else {
          reactingTick = 19; // 重新返回上一步
        }
      } else if (reactingTick == 140) {
        GuiScreen gui = mc.currentScreen;
        if (gui instanceof GuiChest) {
          Container container = ((GuiChest) gui).inventorySlots;
          if (container instanceof ContainerChest) {
            String chestName = this.melodySkyPlus$getGuiName(gui);
            if (chestName.startsWith("Alchemist")) {
              for (Slot slot : container.inventorySlots) {
                // 买水
                ItemStack item = slot.getStack(); // 获取item
                if (StringUtils.stripControlCodes(item.getDisplayName()).equals("Water Bottle")) {
                  // 找到了水
                  break;
                }
              }
            }
          }
        } else {
          mc.thePlayer.closeScreen();
          reactingTick = 11; // 重新返回第一步
        }
      }
      reactingTick++;
    }
  }

  private int melodySkyPlus$getHeat(String input) {
    Pattern pattern = Pattern.compile("^\\d+");
    Matcher matcher = pattern.matcher(input.trim());

    if (matcher.find()) {
      return Integer.parseInt(matcher.group());
    } else {
      throw new IllegalArgumentException("Error: No target heat found");
    }
  }

  public String melodySkyPlus$getGuiName(GuiScreen gui) {
    return gui instanceof GuiChest ? ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText() : "";
  }

  private void melodySkyPlus$clickSlot(int slot, int button, int mode) {
    Minecraft mc = Minecraft.getMinecraft();
    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, mode, mc.thePlayer);
  }
}
