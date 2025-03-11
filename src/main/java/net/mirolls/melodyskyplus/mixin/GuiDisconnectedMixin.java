package net.mirolls.melodyskyplus.mixin;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.mirolls.melodyskyplus.modules.AutoReconnect;
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

  @Inject(method = "initGui", at = @At("RETURN"))
  public void initGui(CallbackInfo ci) {

    buttonList.add(new GuiButton(1,
            this.width / 2 - 100,
            this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + GAP,
            "Reconnect"
        )
    );
    buttonList.add(new GuiButton(1,
            this.width / 2 - 100,
            this.height / 2 + this.field_175353_i / 2 + this.fontRendererObj.FONT_HEIGHT + GAP * 2,
            "Auto Reconnect"
        )
    );
  }

  @Inject(method = "actionPerformed", at = @At("RETURN"))
  protected void actionPerformed(GuiButton button, CallbackInfo ci) {
    if (button.id == 1) {
      FMLClientHandler.instance().connectToServer(new GuiMainMenu(), new ServerData("server", Objects.requireNonNull(AutoReconnect.getInstance()).host, false));
    }
  }

}
