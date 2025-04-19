package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.mirolls.melodyskyplus.client.AntiBug;
import net.mirolls.melodyskyplus.modules.AutoReconnect.AutoReconnect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(GuiDisconnected.class)
public class GuiDisconnectedMixin extends GuiScreen {
  private static final int GAP = 22;
  @Shadow
  private int field_175353_i;
  private boolean reconnecting = false;

  @Inject(method = "initGui", at = @At("RETURN"))
  public void initGui(CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      buttonList.add(new GuiButton(8090,
              this.width / 2 - 100,
              this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + GAP,
              "Reconnect"
          )
      );
      buttonList.add(new GuiButton(8091,
              this.width / 2 - 100,
              this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + GAP * 2,
              "Auto Reconnect"
          )
      );
      if (Objects.requireNonNull(AutoReconnect.getInstance()).isEnabled()) {
        reconnecting = true;
        // 自动重新链接
        autoReconnect();
      }
    }
  }

  @Inject(method = "actionPerformed", at = @At("RETURN"))
  protected void actionPerformed(GuiButton button, CallbackInfo ci) {
    if (AntiBug.isBugRemoved()) {
      if (button.id == 8090) {
        reconnecting = true;
        reconnect();
      }
      if (button.id == 8091) {
        boolean setValue = !Objects.requireNonNull(AutoReconnect.getInstance()).isEnabled();
        Objects.requireNonNull(AutoReconnect.getInstance()).setEnabled(setValue);
        if (setValue && !reconnecting) {
          reconnecting = true;
          autoReconnect();
        } else if (!setValue && reconnecting) {
          reconnecting = false;
          // 进行设置
        }
      }
    }
  }

  private void reconnect() {
    if (AntiBug.isBugRemoved()) {
      if (reconnecting) {
        reconnecting = false;
        mc.addScheduledTask(() -> {
          FMLClientHandler.instance().connectToServer(new GuiMainMenu(), new ServerData("server", Objects.requireNonNull(AutoReconnect.getInstance()).host, false));
        });
      }
    }
  }

  private void autoReconnect() {
    if (AntiBug.isBugRemoved()) {
      Objects.requireNonNull(AutoReconnect.getInstance()).reconnect((second) -> {
        GuiButton reconnectButton = null;
        for (GuiButton button : buttonList) {
          if (button.id == 8091) {
            reconnectButton = button;
          }
        }
        if (reconnectButton != null) {
          reconnectButton.displayString = "Auto Reconnect" + " (" + second + ")";
        }
      }, () -> {
        if (AutoReconnect.getInstance().isEnabled()) {
          reconnect();
        }
      });
    }
  }
}
