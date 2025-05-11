package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.StringUtils;
import net.mirolls.melodyskyplus.client.AntiBug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.Melody.System.Managers.Client.FriendManager;
import xyz.Melody.System.Managers.Skyblock.Area.Areas;
import xyz.Melody.System.Managers.Skyblock.Area.SkyblockArea;
import xyz.Melody.Utils.Vec3d;
import xyz.Melody.Utils.game.PlayerListUtils;
import xyz.Melody.module.modules.macros.Fishing.AutoFish;

@Mixin(value = AutoFish.class, remap = false)
public class AutoFishMixin {

  /**
   * @author liangmimi
   * @reason fix cannot kill fire eel bug
   */
  @Overwrite
  private boolean filterateEntity(EntityLivingBase e, Vec3d player, double dist) {
    Minecraft mc = Minecraft.getMinecraft();

    if (AntiBug.isBugRemoved()) {
      Vec3d eVec = Vec3d.of(e.getPositionVector());
      if (e != mc.thePlayer && !(player.distanceTo(eVec) > dist)) {
        if (e instanceof EntityLivingBase && e.isEntityAlive()) {
          if (!(e instanceof EntityArmorStand) && !FriendManager.isFriend(e.getName())) {

            SkyblockArea mySkyblockArea = new SkyblockArea();// 这里新建而不是用Client下的原因是裤头的混淆
            mySkyblockArea.updateCurrentArea();

            if (e.isInvisible() && mySkyblockArea.getCurrentArea() != Areas.Crimson_Island && !(e instanceof EntityZombie)) {
              return false;
            }

            return e.getName() != null && PlayerListUtils.tabContains(StringUtils.stripControlCodes(e.getName()));
          } else {
            return true;
          }
        } else {
          return true;
        }
      } else {
        return true;
      }
    } else {
      Vec3d eVec = Vec3d.of(e.getPositionVector());
      if (e != mc.thePlayer && !(player.distanceTo(eVec) > dist)) {
        if (e instanceof EntityLivingBase && e.isEntityAlive()) {
          if (!(e instanceof EntityArmorStand) && !e.isInvisible() && !FriendManager.isFriend(e.getName())) {
            return e.getName() != null && PlayerListUtils.tabContains(StringUtils.stripControlCodes(e.getName()));
          } else {
            return true;
          }
        } else {
          return true;
        }
      } else {
        return true;
      }
    }
  }
}
