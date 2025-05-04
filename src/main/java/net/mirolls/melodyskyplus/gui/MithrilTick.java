package net.mirolls.melodyskyplus.gui;

import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender2D;
import xyz.Melody.GUI.CustomUI.HUDApi;
import xyz.Melody.GUI.Font.CFontRenderer;
import xyz.Melody.GUI.Font.FontLoaders;
import xyz.Melody.Utils.render.RenderUtil;

import java.awt.*;

public class MithrilTick extends HUDApi {
  public MithrilTick() {
    super("MithrilTick", 5, 70);
    setEnabled(false);
  }


  @EventHandler
  public void onRender(EventRender2D event) {
    if (this.mc.currentScreen instanceof xyz.Melody.GUI.CustomUI.HUDScreen) {
      return;
    }

    gemstoneTickRender();
  }

  public void InScreenRender() {
    gemstoneTickRender();
  }

  private void gemstoneTickRender() {
    int c2 = (new Color(30, 30, 30, 100)).getRGB();

//    CFontRenderer font = FontLoaders.huhLight(20); /*2.14.5(不包括) 之后版本*/
    CFontRenderer font = FontLoaders.NMSL20;  /*(2.14.5及之前版本)*/

    RenderUtil.drawFastRoundedRect(this.x, this.y,
        (this.x + font.getStringWidth("MithrilTick: " + MelodySkyPlus.nukerTicks.getCurrentTicks()) + 8),
        (this.y + font.getStringHeight("MithrilTick: " + MelodySkyPlus.nukerTicks.getCurrentTicks()) + 6),
        1.0F, c2);
    font.drawString(
        "MithrilTick: " + MelodySkyPlus.nukerTicks.getCurrentTicks(),
        (this.x + 4), (this.y + 4), -1
    );
  }
}