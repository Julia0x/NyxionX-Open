package me.nyxion.gui;

import me.nyxion.Nyxion;
import me.nyxion.config.Config;
import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.*;

public class ClickGUI extends GuiScreen {
    private static final int PANEL_WIDTH = 120;
    private static final int HEADER_HEIGHT = 20;
    private static final int MODULE_HEIGHT = 15;
    private static final int SETTING_HEIGHT = 15;
    private static final Color ACCENT_COLOR = new Color(90, 200, 255);
    private static final int GRID_SIZE = 10;
    private static final int MAX_VISIBLE_MODULES = 15;

    private final List<Panel> panels = new ArrayList<>();
    private Panel draggingPanel = null;
    private int dragX, dragY;
    private Module bindingModule = null;

    public ClickGUI() {
        loadPanelPositions();
    }

    private void loadPanelPositions() {
        Map<String, int[]> positions = Config.loadPanelPositions();
        int defaultX = 10;

        for (Category category : Category.values()) {
            int[] pos = positions.getOrDefault(category.getName(), new int[]{defaultX, 10});
            panels.add(new Panel(category, pos[0], pos[1]));
            defaultX += PANEL_WIDTH + 10;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawGrid(sr);

        if (draggingPanel != null) {
            draggingPanel.x = Math.round((mouseX - dragX) / (float)GRID_SIZE) * GRID_SIZE;
            draggingPanel.y = Math.round((mouseY - dragY) / (float)GRID_SIZE) * GRID_SIZE;
        }

        for (int i = panels.size() - 1; i >= 0; i--) {
            panels.get(i).draw(mouseX, mouseY);
        }

        if (bindingModule != null) {
            String text = "§bPress a key for §f" + bindingModule.getName() + " §7(ESC to cancel)";
            drawCenteredString(mc.fontRendererObj, text, width / 2, height / 2, -1);
        }

        handleScroll();
    }

    private void handleScroll() {
        int scroll = Mouse.getDWheel();
        if (scroll != 0) {
            for (Panel panel : panels) {
                if (panel.isMouseOver(Mouse.getX() / 2, Mouse.getY() / 2)) {
                    panel.scroll(scroll > 0 ? -1 : 1);
                    break;
                }
            }
        }
    }

    private void drawGrid(ScaledResolution sr) {
        for (int x = 0; x < sr.getScaledWidth(); x += GRID_SIZE) {
            drawVerticalLine(x, 0, sr.getScaledHeight(), new Color(255, 255, 255, 15).getRGB());
        }
        for (int y = 0; y < sr.getScaledHeight(); y += GRID_SIZE) {
            drawHorizontalLine(0, sr.getScaledWidth(), y, new Color(255, 255, 255, 15).getRGB());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (bindingModule != null) return;

        for (Panel panel : panels) {
            if (panel.isMouseOver(mouseX, mouseY)) {
                if (mouseButton == 0) {
                    draggingPanel = panel;
                    dragX = mouseX - panel.x;
                    dragY = mouseY - panel.y;
                    panels.remove(panel);
                    panels.add(0, panel);
                } else if (mouseButton == 1) {
                    panel.expanded = !panel.expanded;
                }
                return;
            }

            if (panel.expanded) {
                panel.handleClick(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        draggingPanel = null;
        for (Panel panel : panels) {
            panel.handleRelease(mouseX, mouseY, state);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (bindingModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                bindingModule.setKeyBind(Keyboard.KEY_NONE);
            } else {
                bindingModule.setKeyBind(keyCode);
            }
            bindingModule = null;
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            savePanelPositions();
        }
    }

    private void savePanelPositions() {
        Map<String, int[]> positions = new HashMap<>();
        for (Panel panel : panels) {
            positions.put(panel.category.getName(), new int[]{panel.x, panel.y});
        }
        Config.savePanelPositions(positions);
    }

    @Override
    public void onGuiClosed() {
        savePanelPositions();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class Panel {
        private final Category category;
        private int x, y;
        private boolean expanded = true;
        private final List<Module> modules;
        private Module selectedModule;
        private int scrollOffset = 0;
        private NumberSetting draggingSlider = null;

        public Panel(Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.modules = Nyxion.getInstance().getModuleManager().getModulesByCategory(category);
        }

        public void draw(int mouseX, int mouseY) {
            // Panel header
            drawRect(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT,
                    new Color(40, 40, 45, 255).getRGB());
            mc.fontRendererObj.drawStringWithShadow(category.getName(),
                    x + 5, y + HEADER_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f, -1);
            mc.fontRendererObj.drawStringWithShadow(expanded ? "▼" : "▶",
                    x + PANEL_WIDTH - 15, y + HEADER_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
                    new Color(200, 200, 200).getRGB());

            if (!expanded) return;

            int moduleY = y + HEADER_HEIGHT;
            int visibleModules = Math.min(MAX_VISIBLE_MODULES, modules.size() - scrollOffset);

            for (int i = scrollOffset; i < scrollOffset + visibleModules; i++) {
                Module module = modules.get(i);
                drawModule(module, moduleY, mouseX, mouseY);
                moduleY += MODULE_HEIGHT;

                if (module == selectedModule) {
                    for (Setting<?> setting : module.getSettings()) {
                        if (setting instanceof StringSetting) continue; // Skip string settings
                        drawSetting(setting, moduleY, mouseX, mouseY);
                        moduleY += SETTING_HEIGHT;
                    }
                }
            }
        }

        private void drawModule(Module module, int y, int mouseX, int mouseY) {
            int bgColor = module.isEnabled() ?
                    new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 100).getRGB() :
                    new Color(30, 30, 35, 180).getRGB();
            drawRect(x, y, x + PANEL_WIDTH, y + MODULE_HEIGHT, bgColor);

            mc.fontRendererObj.drawStringWithShadow(module.getName(),
                    x + 5, y + MODULE_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
                    module.isEnabled() ? -1 : new Color(200, 200, 200).getRGB());

            String keyName = Keyboard.getKeyName(module.getKeyBind());
            if (module.getKeyBind() != Keyboard.KEY_NONE) {
                mc.fontRendererObj.drawStringWithShadow("[" + keyName + "]",
                        x + PANEL_WIDTH - mc.fontRendererObj.getStringWidth("[" + keyName + "]") - 5,
                        y + MODULE_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
                        new Color(150, 150, 150).getRGB());
            }
        }

        private void drawSetting(Setting<?> setting, int y, int mouseX, int mouseY) {
            drawRect(x + 5, y, x + PANEL_WIDTH - 5, y + SETTING_HEIGHT,
                    new Color(20, 20, 25, 180).getRGB());

            if (setting instanceof BooleanSetting) {
                drawBooleanSetting((BooleanSetting) setting, y);
            } else if (setting instanceof NumberSetting) {
                drawNumberSetting((NumberSetting) setting, y, mouseX);
            } else if (setting instanceof ModeSetting) {
                drawModeSetting((ModeSetting) setting, y);
            }
        }

        private void drawBooleanSetting(BooleanSetting setting, int y) {
            drawRect(x + 8, y + 4, x + 15, y + 11,
                    setting.getValue() ? ACCENT_COLOR.getRGB() : new Color(50, 50, 55).getRGB());
            mc.fontRendererObj.drawStringWithShadow(setting.getName(),
                    x + 20, y + SETTING_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
                    new Color(200, 200, 200).getRGB());
        }

        private void drawNumberSetting(NumberSetting setting, int y, int mouseX) {
            float progress = (float)((setting.getValue() - setting.getMin()) /
                    (setting.getMax() - setting.getMin()));

            if (draggingSlider == setting) {
                float value = (mouseX - (x + 8)) / (float)(PANEL_WIDTH - 16);
                value = Math.max(0, Math.min(1, value));
                setting.setValue(setting.getMin() + value * (setting.getMax() - setting.getMin()));
            }

            mc.fontRendererObj.drawStringWithShadow(setting.getName(),
                    x + 8, y + 2, new Color(200, 200, 200).getRGB());

            drawRect(x + 8, y + SETTING_HEIGHT - 4,
                    x + PANEL_WIDTH - 8, y + SETTING_HEIGHT - 2,
                    new Color(40, 40, 45).getRGB());
            drawRect(x + 8, y + SETTING_HEIGHT - 4,
                    x + 8 + (int)((PANEL_WIDTH - 16) * progress), y + SETTING_HEIGHT - 2,
                    ACCENT_COLOR.getRGB());

            String value = String.format("%.2f", setting.getValue());
            mc.fontRendererObj.drawStringWithShadow(value,
                    x + PANEL_WIDTH - 8 - mc.fontRendererObj.getStringWidth(value),
                    y + 2, new Color(200, 200, 200).getRGB());
        }

        private void drawModeSetting(ModeSetting setting, int y) {
            mc.fontRendererObj.drawStringWithShadow(setting.getName() + ": §b" + setting.getValue(),
                    x + 8, y + SETTING_HEIGHT / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f,
                    new Color(200, 200, 200).getRGB());
        }

        public void handleClick(int mouseX, int mouseY, int mouseButton) {
            int moduleY = y + HEADER_HEIGHT;
            int visibleModules = Math.min(MAX_VISIBLE_MODULES, modules.size() - scrollOffset);

            for (int i = scrollOffset; i < scrollOffset + visibleModules; i++) {
                Module module = modules.get(i);
                if (isMouseOverModule(mouseX, mouseY, moduleY)) {
                    if (mouseButton == 0) {
                        module.toggle();
                    } else if (mouseButton == 1) {
                        selectedModule = selectedModule == module ? null : module;
                    } else if (mouseButton == 2) {
                        bindingModule = module;
                    }
                    return;
                }
                moduleY += MODULE_HEIGHT;

                if (module == selectedModule) {
                    for (Setting<?> setting : module.getSettings()) {
                        if (setting instanceof StringSetting) continue;
                        if (isMouseOverSetting(mouseX, mouseY, moduleY)) {
                            handleSettingClick(setting, mouseX, mouseButton);
                        }
                        moduleY += SETTING_HEIGHT;
                    }
                }
            }
        }

        public void handleRelease(int mouseX, int mouseY, int state) {
            draggingSlider = null;
        }

        private void handleSettingClick(Setting<?> setting, int mouseX, int mouseButton) {
            if (setting instanceof BooleanSetting && mouseButton == 0) {
                ((BooleanSetting) setting).toggle();
            } else if (setting instanceof ModeSetting && mouseButton == 0) {
                ((ModeSetting) setting).cycle();
            } else if (setting instanceof NumberSetting && mouseButton == 0) {
                draggingSlider = (NumberSetting) setting;
            }
        }

        public void scroll(int amount) {
            scrollOffset = Math.max(0, Math.min(modules.size() - MAX_VISIBLE_MODULES,
                    scrollOffset + amount));
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                    mouseY >= y && mouseY <= y + HEADER_HEIGHT;
        }

        private boolean isMouseOverModule(int mouseX, int mouseY, int moduleY) {
            return mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                    mouseY >= moduleY && mouseY <= moduleY + MODULE_HEIGHT;
        }

        private boolean isMouseOverSetting(int mouseX, int mouseY, int settingY) {
            return mouseX >= x + 5 && mouseX <= x + PANEL_WIDTH - 5 &&
                    mouseY >= settingY && mouseY <= settingY + SETTING_HEIGHT;
        }
    }
}