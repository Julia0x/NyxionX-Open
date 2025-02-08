package me.nyxion.module.impl.render;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

public class HUDModule extends Module {
    private final BooleanSetting showWatermark = new BooleanSetting("Watermark", "Show client watermark", true);
    private final BooleanSetting showSession = new BooleanSetting("Session", "Show session time", true);
    private final BooleanSetting showTime = new BooleanSetting("Time", "Show current time", true);
    private final BooleanSetting showFPS = new BooleanSetting("FPS", "Show FPS counter", true);
    private final BooleanSetting showCoords = new BooleanSetting("Coordinates", "Show player coordinates", true);
    private final BooleanSetting showArrayList = new BooleanSetting("ArrayList", "Show enabled modules", true);
    private final ModeSetting arrayListMode = new ModeSetting("ArrayList Mode", "Style of the ArrayList", "Rainbow", "Rainbow", "Fade", "Static");
    private final ColorSetting arrayListColor = new ColorSetting("ArrayList Color", "Color for static mode", new Color(90, 200, 255));

    public HUDModule() {
        super("HUD", "Customize HUD elements", Category.RENDER);
        setEnabled(true);
        setKeyBind(Keyboard.KEY_NONE);

        addSetting(showWatermark);
        addSetting(showSession);
        addSetting(showTime);
        addSetting(showFPS);
        addSetting(showCoords);
        addSetting(showArrayList);
        addSetting(arrayListMode);
        addSetting(arrayListColor);
    }

    public boolean isWatermarkEnabled() { return isEnabled() && showWatermark.getValue(); }
    public boolean isSessionEnabled() { return isEnabled() && showSession.getValue(); }
    public boolean isTimeEnabled() { return isEnabled() && showTime.getValue(); }
    public boolean isFPSEnabled() { return isEnabled() && showFPS.getValue(); }
    public boolean isCoordsEnabled() { return isEnabled() && showCoords.getValue(); }
    public boolean isArrayListEnabled() { return isEnabled() && showArrayList.getValue(); }
    public String getArrayListMode() { return arrayListMode.getValue(); }
    public Color getArrayListColor() { return arrayListColor.getValue(); }
}