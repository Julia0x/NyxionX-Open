package me.nyxion.module;

import me.nyxion.module.impl.combat.AutoClickerModule;
import me.nyxion.module.impl.combat.WTapModule;
import me.nyxion.module.impl.movement.*;
import me.nyxion.module.impl.misc.InsultModule;
import me.nyxion.module.impl.render.*;
import me.nyxion.module.impl.world.LegitScaffoldModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Category, List<Module>> categoryMap = new HashMap<>();

    public void init() {
        // Register modules
        registerModule(new SprintModule());
        registerModule(new SpeedModule());
        registerModule(new StepModule());
        registerModule(new WTapModule());
        registerModule(new TargetHUDModule());
        registerModule(new InsultModule());
        registerModule(new HUDModule());
        registerModule(new AutoClickerModule());
        registerModule(new LegitScaffoldModule());
        
        // Register new render modules
        registerModule(new ESPModule());
        registerModule(new BlockOverlayModule());
        registerModule(new ChamsModule());

        // Initialize category map
        updateCategoryMap();

        // Register to Forge event bus
        MinecraftForge.EVENT_BUS.register(this);

        System.out.println("[Nyxion] ModuleManager initialized with " + modules.size() + " modules");
    }

    private void registerModule(Module module) {
        modules.add(module);
        MinecraftForge.EVENT_BUS.register(module);
        updateCategoryMap();
    }

    private void updateCategoryMap() {
        categoryMap.clear();
        for (Module module : modules) {
            categoryMap.computeIfAbsent(module.getCategory(), k -> new ArrayList<>()).add(module);
        }
    }

    public List<Module> getModules() {
        return new ArrayList<>(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        return categoryMap.getOrDefault(category, new ArrayList<>());
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void onKey(int keyCode) {
        for (Module module : modules) {
            if (module.getKeyBind() == keyCode) {
                module.toggle();
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            for (Module module : modules) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        }
    }
}