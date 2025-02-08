package me.nyxion.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Command {
    protected final Minecraft mc = Minecraft.getMinecraft();
    private final String name;
    private final String description;
    private final String[] aliases;
    private final String syntax;
    private final String[] category;
    private final boolean hidden;
    private final List<Function<String[], List<String>>> tabCompleters = new ArrayList<>();
    private final List<Consumer<String[]>> executors = new ArrayList<>();
    
    public Command(String name, String description, String syntax, String category, String... aliases) {
        this(name, description, syntax, category, false, aliases);
    }
    
    public Command(String name, String description, String syntax, String category, boolean hidden, String... aliases) {
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.category = new String[]{category};
        this.aliases = aliases;
        this.hidden = hidden;
    }
    
    public abstract void execute(String[] args);
    
    protected void addExecutor(Consumer<String[]> executor) {
        executors.add(executor);
    }
    
    protected void addTabCompleter(Function<String[], List<String>> completer) {
        tabCompleters.add(completer);
    }
    
    public List<String> tabComplete(String[] args) {
        List<String> completions = new ArrayList<>();
        for (Function<String[], List<String>> completer : tabCompleters) {
            List<String> result = completer.apply(args);
            if (result != null) {
                completions.addAll(result);
            }
        }
        return completions;
    }
    
    protected void executeAll(String[] args) {
        for (Consumer<String[]> executor : executors) {
            executor.accept(args);
        }
    }
    
    protected void sendMessage(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§bNyxion§8] §7" + message));
        }
    }
    
    protected void sendUsage() {
        sendMessage("§cUsage: " + syntax);
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String[] getAliases() {
        return aliases;
    }
    
    public String getSyntax() {
        return syntax;
    }
    
    public String[] getCategory() {
        return category;
    }
    
    public boolean isHidden() {
        return hidden;
    }
}