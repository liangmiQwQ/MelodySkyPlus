package net.mirolls.melodyskyplus.gui;

import java.awt.*;
import net.mirolls.melodyskyplus.MelodySkyPlus;
import xyz.Melody.Event.EventHandler;
import xyz.Melody.Event.events.rendering.EventRender2D;
import xyz.Melody.GUI.Font.CFontRenderer;
import xyz.Melody.GUI.Font.FontLoaders;
import xyz.Melody.GUI.Hud.HUDElement;
import xyz.Melody.Utils.render.RenderUtil;

public class GemstoneTick extends HUDElement {
  public GemstoneTick() {
    super("GemstoneTick", 5, 70);
    setEnabled(false);
  }

  @EventHandler
  public void onRender(EventRender2D event) {
    if (this.mc.currentScreen instanceof xyz.Melody.GUI.Hud.HUDEditScreen) {
      return;
    }

    gemstoneTickRender();
  }

  public void editRender(boolean focus) {
    super.editRender(focus);
    gemstoneTickRender();
  }

  private void gemstoneTickRender() {
    int c2 = (new Color(30, 30, 30, 100)).getRGB();

    CFontRenderer font = FontLoaders.huh(20); /*2.14.5(不包括) 之后版本*/
    //    CFontRenderer font = FontLoaders.NMSL20;  /*(2.14.5及之前版本)*/

    float length =
        font.getStringWidth("GemstoneTick: " + MelodySkyPlus.nukerTicks.getCurrentTicks()) + 4;
    RenderUtil.rect(this.x, this.y, (this.x + length), (this.y + font.getHeight() + 5), 2.0F, c2);

    font.drawString(
        "GemstoneTick: " + MelodySkyPlus.nukerTicks.getCurrentTicks(),
        (this.x + 2F),
        (this.y + 2.5F),
        -1);

    this.width = length;
    this.height = 11.0F;
  }
}
