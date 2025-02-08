package me.nyxion;

import me.nyxion.commands.CommandManager;
import me.nyxion.config.Config;
import me.nyxion.events.EventManager;
import me.nyxion.gui.HUD;
import me.nyxion.module.ModuleManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = Nyxion.MODID, version = Nyxion.VERSION)
public class Nyxion {
    public static final String MODID = "nyxion";
    public static final String VERSION = "1.8.9";

    private static Nyxion instance;
    private final EventManager eventManager;
    private final CommandManager commandManager;
    private final ModuleManager moduleManager;
    private final HUD hud;

    public Nyxion() {
        instance = this;
        this.eventManager = new EventManager();
        this.commandManager = new CommandManager();
        this.moduleManager = new ModuleManager();
        this.hud = new HUD();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("[Nyxion] Pre-initialization phase started!");
        Config.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("[Nyxion] Initialization phase started!");

        // Register to Forge event bus
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hud);

        // Initialize event system first
        this.eventManager.init();

        // Register managers with event system
        this.eventManager.register(this.commandManager);
        this.eventManager.register(this.moduleManager);

        // Initialize systems
        this.commandManager.init();
        this.moduleManager.init();

        // Load config after modules are initialized
        Config.loadConfig();

        System.out.println("[Nyxion] Systems initialized successfully!");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("[Nyxion] Post-initialization phase completed!");
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) {
            int keyCode = Keyboard.getEventKey();
            moduleManager.onKey(keyCode);
        }
    }

    public static Nyxion getInstance() {
        return instance;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public HUD getHUD() {
        return hud;
    }
}