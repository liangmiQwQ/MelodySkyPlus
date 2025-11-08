package net.mirolls.melodyskyplus.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import net.mirolls.melodyskyplus.Verify;
import net.mirolls.melodyskyplus.client.AntiBug;
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
import xyz.Melody.Utils.game.PlayerListUtils;
import xyz.Melody.Utils.game.item.ItemUtils;
import xyz.Melody.Utils.math.RotationUtil;
import xyz.Melody.Utils.timer.TimerUtil;
import xyz.Melody.module.modules.macros.Mining.AutoRuby;
import xyz.Melody.module.modules.macros.Mining.GemstoneNuker;

@SuppressWarnings("rawtypes")
@Mixin(value = AutoRuby.class, remap = false)
public class AutoRubyMixin {

  private final Option<Boolean> melodySkyPlus$autoCloseGui = new Option<>("Auto Close GUI", true);
  @Shadow public boolean started;
  @Shadow private TimerUtil ewTimer;
  @Shadow private boolean etherWarped;
  @Shadow private BlockPos nextBP;
  @Shadow private TimerUtil timer;
  @Shadow private ArrayList<Entity> yogs;
  @Shadow private Numbers<Double> yogRange;
  @Shadow private Option<Boolean> rcKill;
  @Shadow private boolean killingYogs;
  @Shadow private TimerUtil attackTimer;
  @Shadow private Numbers<Double> weaponSlot;
  @Shadow private Option<Boolean> faceDown;
  @Shadow private Option<Boolean> aim;

  private void melodySkyPlus$switchToJasper() {
    Minecraft mc = Minecraft.getMinecraft();
    if (Verify.isVerified() && started) {
      if (mc.thePlayer.getHeldItem() != null
              && !ItemUtils.getSkyBlockID(mc.thePlayer.getHeldItem()).contains("GEMSTONE_DRILL")
          || mc.thePlayer.getHeldItem() == null) {
        for (int i = 0; i < 9; ++i) {
          ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
          if (itemStack != null
              && itemStack.getItem() != null
              && ItemUtils.getSkyBlockID(itemStack).contains("GEMSTONE_DRILL")) {
            mc.thePlayer.inventory.currentItem = i;
            break;
          }
        }
      }
    }
  }

  @ModifyArg(
      method = "<init>",
      remap = false,
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lxyz/Melody/module/modules/macros/Mining/AutoRuby;addValues([Lxyz/Melody/Event/value/Value;)V"))
  private Value[] init(Value[] originalValues) {
    if (Verify.isVerified()) {
      Value[] returnValues = Arrays.copyOf(originalValues, originalValues.length + 2);

      returnValues[returnValues.length - 2] = MelodySkyPlus.jasperUsed.autoUseJasper;
      returnValues[returnValues.length - 1] = melodySkyPlus$autoCloseGui;

      return returnValues;
    } else {
      return originalValues;
    }
  }

  // Mojang极其恶心的混淆了他的代码 导致我无法使用Redirect精准定位 只能使用恶心Inject处理currentItem
  @Inject(method = "idk", remap = false, at = @At("TAIL"))
  private void idkTail(EventTick event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved() && started) {
      if (!MelodySkyPlus.jasperUsed.isJasperUsed()
          && MelodySkyPlus.jasperUsed.autoUseJasper.getValue()) {
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
      if (!MelodySkyPlus.jasperUsed.isJasperUsed()
          && MelodySkyPlus.jasperUsed.autoUseJasper.getValue()) {
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
  @Redirect(
      method = "<init>",
      remap = false,
      at =
          @At(
              value = "NEW",
              target =
                  "(Ljava/lang/String;Ljava/lang/Object;[Lxyz/Melody/Event/value/IValAction;)Lxyz/Melody/Event/value/Option;"))
  private Option initYogToMobOption(String name, Object enabled, IValAction[] actions) {
    if (AntiBug.isBugRemoved()) {
      String newName = name.replace("Yog", "Mob");
      return new Option(newName, enabled, actions);
    } else {
      return new Option(name, enabled, actions);
    }
  }

  @SuppressWarnings("unchecked")
  @Redirect(
      method = "<init>",
      remap = false,
      at =
          @At(
              value = "NEW",
              target =
                  "(Ljava/lang/String;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;Ljava/lang/Number;[Lxyz/Melody/Event/value/IValAction;)Lxyz/Melody/Event/value/Numbers;"))
  private Numbers initYogToMobNumber(
      String name, Number value, Number min, Number max, Number inc, IValAction[] action) {
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
        if (!entity.isDead
            && entity.isEntityAlive()
            && entity instanceof EntityLivingBase
            && (double) mc.thePlayer.getDistanceToEntity(entity) < yogRange.getValue()) {
          if (entity instanceof EntityMagmaCube || entity instanceof EntityIronGolem) {
            this.yogs.add(entity);
          }

          if (entity instanceof EntityPlayer) {
            String name = entity.getName().toLowerCase();

            if (!name.contains("kalhuki tribe member")
                && !name.contains("weakling")
                && !name.contains("goblin")
                && !PlayerListUtils.isInTablist((EntityPlayer) entity)
                && !entity.equals(mc.thePlayer)) {
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
        if (!entity.isDead
            && entity.isEntityAlive()
            && entity instanceof EntityMagmaCube
            && (double) mc.thePlayer.getDistanceToEntity(entity) < this.yogRange.getValue()) {
          this.yogs.add(entity);
        }
      }

      this.yogs.sort(
          Comparator.comparingDouble((sb) -> (double) mc.thePlayer.getDistanceToEntity(sb)));
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
          NotificationPublisher.queue(
              "AutoRuby", "Mob Detected, Trying to ATTACK it.", NotificationType.WARN, 3000);
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
        NotificationPublisher.queue(
            "AutoRuby", "OKAY, Continued Mining..", NotificationType.SUCCESS, 3000);
        this.started = true;
        this.killingYogs = false;
        this.attackTimer.reset();
      }
    } else {
      if (this.rcKill.getValue()) {
        this.loadYogs();
      } else if (!this.yogs.isEmpty()) {
        this.yogs.clear();
      }

      if (!this.yogs.isEmpty()) {
        EntityMagmaCube mcube = (EntityMagmaCube) this.yogs.get(0);
        if (this.started) {
          NotificationPublisher.queue(
              "AutoRuby", "Yog Detected, Trying to FUCK it.", NotificationType.WARN, 3000);
          this.started = false;
          this.killingYogs = true;
          this.attackTimer.reset();
        }

        if (mcube != null && mcube.isEntityAlive() && this.killingYogs) {
          mc.thePlayer.inventory.currentItem = this.weaponSlot.getValue().intValue() - 1;
          if (this.rcKill.getValue()) {
            if (this.faceDown.getValue()) {
              event.setPitch(90.0F);
              if (this.attackTimer.hasReached(180.0F)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            } else {
              if (this.aim.getValue()) {
                float[] r = RotationUtil.getPredictedRotations(mcube);
                event.setYaw(r[0]);
                event.setPitch(r[1]);
              }

              if (this.attackTimer.hasReached(180.0F)) {
                Client.rightClick();
                this.attackTimer.reset();
              }
            }
          }
        }
      } else if (this.killingYogs) {
        NotificationPublisher.queue(
            "AutoRuby", "OKAY, Continued Mining..", NotificationType.SUCCESS, 3000);
        this.started = true;
        this.killingYogs = false;
        this.attackTimer.reset();
      }
    }
  }

  @Inject(method = "idk", at = @At("HEAD"), remap = false)
  private void idk(EventTick event, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {

      Minecraft mc = Minecraft.getMinecraft();

      if (started) {
        if (mc.currentScreen instanceof GuiChest) {
          mc.thePlayer.closeScreen();
        }
      }

      if (this.ewTimer.hasReached(0)
          && !this.etherWarped
          && GemstoneNuker.getINSTANCE().gemstones.isEmpty()
          && this.nextBP != null
          && timer.hasReached(150)) {
        Objects.requireNonNull(Failsafe.getINSTANCE()).lastTeleport = System.currentTimeMillis();
      }
    }
  }
}
