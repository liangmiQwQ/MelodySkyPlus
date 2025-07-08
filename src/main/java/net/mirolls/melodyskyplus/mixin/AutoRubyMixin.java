package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.libs.AutoHeatStage;
import net.mirolls.melodyskyplus.modules.Failsafe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.Melody.Client;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.Player.EventPreUpdate;
import xyz.Melody.Event.events.world.EventTick;
import xyz.Melody.Event.value.IValAction;
import xyz.Melody.Event.value.Numbers;
import xyz.Melody.Event.value.Option;
import xyz.Melody.Event.value.Value;
import xyz.Melody.GUI.Notification.NotificationPublisher;
import xyz.Melody.GUI.Notification.NotificationType;
import xyz.Melody.Utils.Helper;
import xyz.Melody.Utils.game.PlayerListUtils;
import xyz.Melody.Utils.game.ScoreboardUtils;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.math.RotationUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("rawtypes")
@Mixin(value = AutoRuby.class, remap = false)
public class AutoRubyMixin {

  private final Numbers<Double> melodySkyPlus$heatLimit = new Numbers<>("HeatLimit", 95.0, 1.0, 100.0, 1.0);
  @Shadow
  public boolean started;
  @Shadow
  private TimerUtil ewTimer;
  @Shadow
  private boolean etherWarped;
  @Shadow
  private BlockPos nextBP;
  @Shadow
  private TimerUtil timer;
  @Shadow
  private ArrayList<Entity> yogs;
  @Shadow
  private Numbers<Double> yogRange;
  @Shadow
  private Option<Boolean> rcKill;
  @Shadow
  private boolean killingYogs;
  @Shadow
  private TimerUtil attackTimer;
  @Shadow
  private Numbers<Double> weaponSlot;
  @Shadow
  private Option<Boolean> faceDown;
  @Shadow
  private Option<Boolean> aim;
  private Option<Boolean> melodySkyPlus$autoHeat = null;
  private int melodySkyPlus$reactingTick = -1;
  private AutoHeatStage melodySkyPlus$stage = AutoHeatStage.WORKING;
  private int melodySkyPlus$prevItem;

  private void melodySkyPlus$switchToJasper() {
    Minecraft mc = Minecraft.getMinecraft();
    if (Verify.isVerified() && started) {
      if (mc.thePlayer.getHeldItem() != null && !ItemUtils.getSkyBlockID(mc.thePlayer.getHeldItem()).contains("GEMSTONE_DRILL") || mc.thePlayer.getHeldItem() == null) {
        for (int i = 0; i < 9; ++i) {
          ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
          if (itemStack != null && itemStack.getItem() != null && ItemUtils.getSkyBlockID(itemStack).contains("GEMSTONE_DRILL")) {
            mc.thePlayer.inventory.currentItem = i;
            break;
          }
        }
      }
    }
  }

  @ModifyArg(method = "<init>", remap = false, at = @At(value = "INVOKE", target = "Lxyz/Melody/module/modules/macros/Mining/AutoRuby;addValues([Lxyz/Melody/Event/value/Value;)V"))
  private Value[] init(Value[] originalValues) {
    if (Verify.isVerified()) {
      melodySkyPlus$reactingTick = -1;

      melodySkyPlus$autoHeat = new Option<>("AutoHeat", false, val -> {
        if (AutoRuby.getINSTANCE() != null) {
          melodySkyPlus$heatLimit.setEnabled(val);
        }
      });

      Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 3);

      returnValues[returnValues.length - 3] = melodySkyPlus$autoHeat;
      returnValues[returnValues.length - 2] = melodySkyPlus$heatLimit;
      returnValues[returnValues.length - 1] = MelodySkyPlus.jasperUsed.autoUseJasper;

      return returnValues;
    } else {
      return originalValues;
    }
  }

  // Mojang极其恶心的混淆了他的代码 导致我无法使用Redirect精准定位 只能使用恶心Inject处理currentItem
  @Inject(method = "idk", remap = false, at = @At("TAIL"))
  private void idkTail(EventTick event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved() && started) {
      if (!MelodySkyPlus.jasperUsed.isJasperUsed() && MelodySkyPlus.jasperUsed.autoUseJasper.getValue()) {
        if (Minecraft.getMinecraft().thePlayer.inventory.currentItem == 0) {
          // 如果新的currentItem被设置为0了 但是之前不是0
          // 则手动替换到jasper钻头
          melodySkyPlus$switchToJasper();
        }
      }
    }
  }

  @Inject(method = "etherWarp", remap = false, at = @At("TAIL"))
  private void etherWarpTail(BlockPos pos, CallbackInfo ci) {
    if (AntiBug.isBugRemoved() && started) {
      if (!MelodySkyPlus.jasperUsed.isJasperUsed() && MelodySkyPlus.jasperUsed.autoUseJasper.getValue()) {
        MelodySkyPlus.LOGGER.info("etherWarp executed");
        if (Minecraft.getMinecraft().thePlayer.inventory.currentItem == 0) {
          // 如果新的currentItem被设置为0了 但是之前不是0
          // 则手动替换到jasper钻头
          melodySkyPlus$switchToJasper();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Redirect(method = "<init>", remap = false, at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/Object;[Lxyz/Melody/Event/value/IValAction;)Lxyz/Melody/Event/value/Option;"))
  private Option initYogToMobOption(String name, Object enabled, IValAction[] actions) {
    if (AntiBug.isBugRemoved()) {
      String newName = name.replace("Yog", "Mob");
      return new Option(newName, enabled, actions);
    } else {
      return new Option(name, enabled, actions);
    }
  }

  @SuppressWarnings("unchecked")
  @Redirect(method = "<init>", remap = false, at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;[Lxyz/Melody/Event/value/IValAction;)Lxyz/Melody/Event/value/Numbers;"))
  private Numbers initYogToMobNumber(String name, Number value, Number min, Number max, Number inc, IValAction[] action) {
    if (AntiBug.isBugRemoved()) {
      String newName = name.replace("Yog", "Mob");

      Number newMax = max;
      if (name.equals("YogRange")) {
        newMax = 10.0;
      }
      return new Numbers(newName, value, min, newMax, inc, action);
    } else {
      return new Numbers(name, value, min, max, inc, action);
    }
  }

  /**
   * @author liangmimi
   * @reason 实现查找除了yog以外的生物 并且保证合理使用yogs数组
   */
  @Overwrite
  private void loadYogs() {
    Minecraft mc = Minecraft.getMinecraft();

    if (AntiBug.isBugRemoved()) {
      yogs.clear();
      for (Entity entity : mc.theWorld.loadedEntityList) {
        if (!entity.isDead && entity.isEntityAlive() && entity instanceof EntityLivingBase && (double) mc.thePlayer.getDistanceToEntity(entity) < yogRange.getValue()) {
          if (entity instanceof EntityMagmaCube || entity instanceof EntityIronGolem) {
            this.yogs.add(entity);
          }

          if (entity instanceof EntityPlayer) {
            String name = entity.getName().toLowerCase();

            if (!name.contains("kalhuki tribe member") && !name.contains("weakling") && !name.contains("goblin") && !PlayerListUtils.isInTablist((EntityPlayer) entity) && !entity.equals(mc.thePlayer)) {
              // TODO 研究此处mithril的名称
              if (name.contains("team treasurite")) {
                this.yogs.add(entity);
              }
            }
          }
        }
      }

      this.yogs.sort(Comparator.comparingDouble((sb) -> mc.thePlayer.getDistanceToEntity(sb)));
    } else {
      this.yogs.clear();

      for (Entity entity : mc.theWorld.loadedEntityList) {
        if (!entity.isDead && entity.isEntityAlive() && entity instanceof EntityMagmaCube && (double) mc.thePlayer.getDistanceToEntity(entity) < this.yogRange.getValue()) {
          this.yogs.add((EntityMagmaCube) entity);
        }
      }

      this.yogs.sort(Comparator.comparingDouble((sb) -> (double) mc.thePlayer.getDistanceToEntity(sb)));
    }
  }

  /**
   * @author liangmimi
   * @reason 配合新的loadYogs实现自动杀死其他生物
   */
  @EventHandler
  @Overwrite
  private void onKillYog(EventPreUpdate event) {
    Minecraft mc = Minecraft.getMinecraft();
    if (AntiBug.isBugRemoved()) {
      if (rcKill.getValue()) {
        this.loadYogs();
      } else if (!this.yogs.isEmpty()) {
        this.yogs.clear();
      }

      if (!this.yogs.isEmpty()) {
        Entity entity = this.yogs.get(0);
        if (this.started) {
          NotificationPublisher.queue("AutoRuby", "Mob Detected, Trying to ATTACK it.", NotificationType.WARN, 3000);
          this.started = false;
          this.killingYogs = true;
          this.attackTimer.reset();
        }

        if (entity != null && entity.isEntityAlive() && killingYogs) {
          mc.thePlayer.inventory.currentItem = weaponSlot.getValue().intValue() - 1;
          if (rcKill.getValue()) {
            if (faceDown.getValue()) {
              event.setPitch(90.0F);
              if (this.attackTimer.hasReached(180.0)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            } else {
              if (aim.getValue()) {
                float[] r = RotationUtil.getPredictedRotations((EntityLivingBase) entity);
                event.setYaw(r[0]);
                event.setPitch(r[1]);
              }

              if (this.attackTimer.hasReached(180.0)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            }
          }
        }
      } else if (this.killingYogs) {
        NotificationPublisher.queue("AutoRuby", "OKAY, Continued Mining..", NotificationType.SUCCESS, 3000);
        this.started = true;
        this.killingYogs = false;
        this.attackTimer.reset();
      }
    } else {
      if ((Boolean) this.rcKill.getValue()) {
        this.loadYogs();
      } else if (!this.yogs.isEmpty()) {
        this.yogs.clear();
      }

      if (!this.yogs.isEmpty()) {
        EntityMagmaCube mcube = (EntityMagmaCube) this.yogs.get(0);
        if (this.started) {
          NotificationPublisher.queue("AutoRuby", "Yog Detected, Trying to FUCK it.", NotificationType.WARN, 3000);
          this.started = false;
          this.killingYogs = true;
          this.attackTimer.reset();
        }

        if (mcube != null && mcube.isEntityAlive() && this.killingYogs) {
          mc.thePlayer.inventory.currentItem = this.weaponSlot.getValue().intValue() - 1;
          if ((Boolean) this.rcKill.getValue()) {
            if ((Boolean) this.faceDown.getValue()) {
              event.setPitch(90.0F);
              if (this.attackTimer.hasReached((double) 180.0F)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            } else {
              if ((Boolean) this.aim.getValue()) {
                float[] r = RotationUtil.getPredictedRotations(mcube);
                event.setYaw(r[0]);
                event.setPitch(r[1]);
              }

              if (this.attackTimer.hasReached((double) 180.0F)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            }
          }
        }
      } else if (this.killingYogs) {
        NotificationPublisher.queue("AutoRuby", "OKAY, Continued Mining..", NotificationType.SUCCESS, 3000);
        this.started = true;
        this.killingYogs = false;
        this.attackTimer.reset();
      }
    }

  }


  @Inject(method = "onEnable", at = @At("HEAD"), remap = false)
  public void onEnable(CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      melodySkyPlus$reactingTick = -1;
    }
  }


  @Inject(method = "idk", at = @At("HEAD"), remap = false)
  private void idk(EventTick event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      Minecraft mc = Minecraft.getMinecraft();

      if (this.ewTimer.hasReached(0) && !this.etherWarped && GemstoneNuker.getINSTANCE().gemstones.isEmpty() && this.nextBP != null && timer.hasReached(150)) {
        Objects.requireNonNull(Failsafe.getINSTANCE()).lastTeleport = System.currentTimeMillis();
      } else {
        // 如果没有在进行TP
        if (melodySkyPlus$autoHeat.getValue()) {
          if (AutoRuby.getINSTANCE().started) {
            // 主要部分 处理AutoHeat
            List<String> scoreBoard = ScoreboardUtils.getScoreboard();

            int heat = 0;
            for (String line : scoreBoard) {
              if (line.toLowerCase().contains("heat:")) {
                heat = melodySkyPlus$getHeat(line.replaceAll(".*Heat: §[a-f0-9]", ""));
                break;
              }
            }

            if (heat >= melodySkyPlus$heatLimit.getValue() && melodySkyPlus$reactingTick == -1) {
              Helper.sendMessage("Found heat too high (" + heat + "), start to junk some water.");
              if (AutoRuby.getINSTANCE().started) {
                melodySkyPlus$reactingTick = 0;
                melodySkyPlus$stage = AutoHeatStage.DRINKING;
                AutoRuby.getINSTANCE().started = false;
                MelodySkyPlus.jasperUsed.setJasperUsed(false);
              }
            }
          }
        }
      }

      if (melodySkyPlus$reactingTick > -1) {
        melodySkyPlus$reactingTick++;

        // 喝水处理
        if (melodySkyPlus$stage == AutoHeatStage.DRINKING) {
          if (melodySkyPlus$reactingTick == 5) {
            // 切换物品
            for (int i = 0; i < 9; ++i) {
              ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
              if (item.getDisplayName().contains("Water") && item.getDisplayName().contains("Bottle")) {
                melodySkyPlus$prevItem = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = i;
              }
            }
          } else if (melodySkyPlus$reactingTick == 15) {
            // 喝水
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);

            if (item.getDisplayName().contains("Water") && item.getDisplayName().contains("Bottle")) {
              KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), true);
              MelodySkyPlus.drinkingLib.register(() -> {
                melodySkyPlus$reactingTick = 0;
                melodySkyPlus$stage = AutoHeatStage.CALLING;
              });
            } else {
              Helper.sendMessage("Missing Water Bottle in hotbar.");
              melodySkyPlus$reactingTick = -1;
              AutoRuby.getINSTANCE().started = true;
            }
          }
        }


        // 打电话
        if (melodySkyPlus$stage == AutoHeatStage.CALLING) {
          if (melodySkyPlus$reactingTick == 5) {
            // 找手机
            for (int i = 0; i < 9; ++i) {
              ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
              if (item != null && ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
                mc.thePlayer.inventory.currentItem = i;
              }
            }
          } else if (melodySkyPlus$reactingTick == 15) {
            // 打电话
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
            if (ItemUtils.getSkyBlockID(item).startsWith("ABIPHONE")) {
              mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem));
            } else {
              Helper.sendMessage("Missing AbiPhone in hotbar.");
              melodySkyPlus$reactingTick = -1;
              AutoRuby.getINSTANCE().started = true;
            }
          } else if (melodySkyPlus$reactingTick == 35) {
            // 找人
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
              melodySkyPlus$reactingTick = 14; // 重新返回上一步 打开电话
            }
          } else if (melodySkyPlus$reactingTick > 35) {
            // 跳转下一个阶段

            GuiScreen gui = mc.currentScreen;
            if (gui instanceof GuiChest) {
              Container container = ((GuiChest) gui).inventorySlots;
              if (container instanceof ContainerChest) {
                String chestName = this.melodySkyPlus$getGuiName(gui);
                if (chestName.startsWith("Alchemist")) {
                  // 下个阶段
                  melodySkyPlus$reactingTick = 0;
                  melodySkyPlus$stage = AutoHeatStage.TRADING;
                }
              }
            }

          }
        }


        // 交易
        if (melodySkyPlus$stage == AutoHeatStage.TRADING) {
          if (melodySkyPlus$reactingTick == 10) {
            // 卖水
            GuiScreen gui = mc.currentScreen;
            if (gui instanceof GuiChest) {
              Container container = ((GuiChest) gui).inventorySlots;
              if (container instanceof ContainerChest) {
                String chestName = this.melodySkyPlus$getGuiName(gui);
                if (chestName.startsWith("Alchemist")) {
                  for (int i = 0; i < 9; ++i) {
                    ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
                    if (item.getDisplayName().contains("Glass") && item.getDisplayName().contains("Bottle")) {
                      melodySkyPlus$clickSlot(i + 81, 0, 0);
                    }
                  }
                }
              }
            } else {
              mc.thePlayer.closeScreen();
              melodySkyPlus$stage = AutoHeatStage.CALLING;
              melodySkyPlus$reactingTick = 14; // 重新返回上一步 打开电话
            }
          } else if (melodySkyPlus$reactingTick == 20) {
            GuiScreen gui = mc.currentScreen;
            if (gui instanceof GuiChest) {
              Container container = ((GuiChest) gui).inventorySlots;
              if (container instanceof ContainerChest) {
                String chestName = this.melodySkyPlus$getGuiName(gui);
                if (chestName.startsWith("Alchemist")) {
                  for (Slot slot : container.inventorySlots) {
                    // 买水
                    ItemStack item = slot.getStack(); // 获取item
                    if (StringUtils.stripControlCodes(item.getDisplayName()).contains("Water") && StringUtils.stripControlCodes(item.getDisplayName()).contains("Bottle")) {
                      melodySkyPlus$clickSlot(slot.getSlotIndex(), 0, 0);
                      // 买水
                      break;
                    }
                  }
                }
              }
            } else {
              mc.thePlayer.closeScreen();
              melodySkyPlus$stage = AutoHeatStage.CALLING;
              melodySkyPlus$reactingTick = 14; // 重新返回上一步 打开电话
            }
          } else if (melodySkyPlus$reactingTick == 30) {
            mc.thePlayer.closeScreen();
            Helper.sendMessage("Bought water and drank successfully");
          } else if (melodySkyPlus$reactingTick == 40) {
            mc.thePlayer.inventory.currentItem = melodySkyPlus$prevItem;
            melodySkyPlus$reactingTick = -1;
            AutoRuby.getINSTANCE().started = true;
            melodySkyPlus$stage = AutoHeatStage.WORKING;
          }
        }
      }
    }

  }

  private int melodySkyPlus$getHeat(String input) {
    if (AntiBug.isBugRemoved()) {
      Pattern pattern = Pattern.compile("^\\d+");
      Matcher matcher = pattern.matcher(input.trim());

      if (matcher.find()) {
        return Integer.parseInt(matcher.group());
      } else {
        throw new IllegalArgumentException("Error: No target heat found");
      }
    }
    return Integer.MIN_VALUE;
  }

  public String melodySkyPlus$getGuiName(GuiScreen gui) {
    if (AntiBug.isBugRemoved()) {
      return gui instanceof GuiChest ? ((ContainerChest) ((GuiChest) gui).inventorySlots).getLowerChestInventory().getDisplayName().getUnformattedText() : "";
    }
    return "";
  }

  private void melodySkyPlus$clickSlot(int slot, int button, int mode) {
    if (AntiBug.isBugRemoved()) {
      Minecraft mc = Minecraft.getMinecraft();
      mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, button, mode, mc.thePlayer);
    }
  }
}
