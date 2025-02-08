package me.nyxion.config;

import com.google.gson.*;
import me.nyxion.Nyxion;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;

import java.awt.Color;
import java.io.*;
import java.util.*;

public class Config {
    private static final String CONFIG_DIR = "config";
    private static final String MODULES_FILE = CONFIG_DIR + "/modules.json";
    private static final String GUI_CONFIG_FILE = CONFIG_DIR + "/gui.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser JSON_PARSER = new JsonParser();

    public static void init() {
        // Create config directory if it doesn't exist
        new File(CONFIG_DIR).mkdirs();
    }

    public static void saveConfig() {
        try {
            JsonObject config = new JsonObject();
            JsonArray modulesArray = new JsonArray();

            for (Module module : Nyxion.getInstance().getModuleManager().getModules()) {
                JsonObject moduleObj = new JsonObject();
                moduleObj.addProperty("name", module.getName());
                moduleObj.addProperty("enabled", module.isEnabled());
                moduleObj.addProperty("keybind", module.getKeyBind());

                JsonObject settingsObj = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    settingsObj.add(setting.getName(), serializeSetting(setting));
                }
                moduleObj.add("settings", settingsObj);

                modulesArray.add(moduleObj);
            }

            config.add("modules", modulesArray);

            // Save with pretty printing
            try (FileWriter writer = new FileWriter(MODULES_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void savePanelPositions(Map<String, int[]> positions) {
        try {
            JsonObject config = new JsonObject();
            JsonObject positionsObj = new JsonObject();

            for (Map.Entry<String, int[]> entry : positions.entrySet()) {
                JsonArray pos = new JsonArray();
                pos.add(entry.getValue()[0]);
                pos.add(entry.getValue()[1]);
                positionsObj.add(entry.getKey(), pos);
            }

            config.add("positions", positionsObj);

            try (FileWriter writer = new FileWriter(GUI_CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save GUI config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, int[]> loadPanelPositions() {
        Map<String, int[]> positions = new HashMap<>();
        try {
            File file = new File(GUI_CONFIG_FILE);
            if (!file.exists()) return positions;

            try (FileReader reader = new FileReader(file)) {
                JsonObject config = JSON_PARSER.parse(reader).getAsJsonObject();
                JsonObject positionsObj = config.getAsJsonObject("positions");

                for (Map.Entry<String, JsonElement> entry : positionsObj.entrySet()) {
                    JsonArray pos = entry.getValue().getAsJsonArray();
                    positions.put(entry.getKey(), new int[]{
                            pos.get(0).getAsInt(),
                            pos.get(1).getAsInt()
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load GUI config: " + e.getMessage());
            e.printStackTrace();
        }
        return positions;
    }

    private static JsonElement serializeSetting(Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            return new JsonPrimitive(((BooleanSetting) setting).getValue());
        } else if (setting instanceof NumberSetting) {
            return new JsonPrimitive(((NumberSetting) setting).getValue());
        } else if (setting instanceof ModeSetting) {
            return new JsonPrimitive(((ModeSetting) setting).getValue());
        } else if (setting instanceof ColorSetting) {
            Color color = ((ColorSetting) setting).getValue();
            JsonObject colorObj = new JsonObject();
            colorObj.addProperty("r", color.getRed());
            colorObj.addProperty("g", color.getGreen());
            colorObj.addProperty("b", color.getBlue());
            colorObj.addProperty("a", color.getAlpha());
            return colorObj;
        } else if (setting instanceof ListSetting) {
            JsonArray array = new JsonArray();
            ((ListSetting<?>) setting).getValue().forEach(element -> array.add(new JsonPrimitive(element.toString())));
            return array;
        } else if (setting instanceof StringSetting) {
            return new JsonPrimitive(((StringSetting) setting).getValue());
        }
        return JsonNull.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private static void deserializeSetting(Setting<?> setting, JsonElement element) {
        try {
            if (setting instanceof BooleanSetting) {
                ((BooleanSetting) setting).setValue(element.getAsBoolean());
            } else if (setting instanceof NumberSetting) {
                ((NumberSetting) setting).setValue(element.getAsDouble());
            } else if (setting instanceof ModeSetting) {
                ((ModeSetting) setting).setValue(element.getAsString());
            } else if (setting instanceof ColorSetting) {
                JsonObject colorObj = element.getAsJsonObject();
                Color color = new Color(
                        colorObj.get("r").getAsInt(),
                        colorObj.get("g").getAsInt(),
                        colorObj.get("b").getAsInt(),
                        colorObj.get("a").getAsInt()
                );
                ((ColorSetting) setting).setValue(color);
            } else if (setting instanceof ListSetting) {
                List<String> list = new ArrayList<>();
                element.getAsJsonArray().forEach(e -> list.add(e.getAsString()));
                ((ListSetting<String>) setting).setValue(list);
            } else if (setting instanceof StringSetting) {
                ((StringSetting) setting).setValue(element.getAsString());
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize setting " + setting.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        try {
            File file = new File(MODULES_FILE);
            if (!file.exists()) return;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                JsonObject config = JSON_PARSER.parse(reader).getAsJsonObject();
                JsonArray modulesArray = config.getAsJsonArray("modules");

                for (Module module : Nyxion.getInstance().getModuleManager().getModules()) {
                    for (JsonElement element : modulesArray) {
                        JsonObject moduleObj = element.getAsJsonObject();
                        if (moduleObj.get("name").getAsString().equals(module.getName())) {
                            // Load enabled state
                            boolean enabled = moduleObj.get("enabled").getAsBoolean();
                            if (enabled != module.isEnabled()) {
                                module.toggle();
                            }

                            // Load keybind
                            module.setKeyBind(moduleObj.get("keybind").getAsInt());

                            // Load settings
                            JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                            for (Setting<?> setting : module.getSettings()) {
                                JsonElement settingElement = settingsObj.get(setting.getName());
                                if (settingElement != null && !settingElement.isJsonNull()) {
                                    deserializeSetting(setting, settingElement);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}