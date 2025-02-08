package me.nyxion.commands;

import me.nyxion.commands.impl.*;
import me.nyxion.events.EventHandler;
import me.nyxion.events.EventPriority;
import me.nyxion.events.impl.ChatEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CommandManager {
    private final List<Command> commands = new CopyOnWriteArrayList<>();
    private final String prefix = ".";
    private final Map<String, List<Command>> categoryMap = new HashMap<>();
    
    public void init() {
        // Register default commands
        registerCommand(new HelpCommand(this));
        registerCommand(new InfoCommand());

        // Initialize category map
        updateCategoryMap();
        
        // Initialize category map
        updateCategoryMap();
    }
    
    public void registerCommand(Command command) {
        commands.add(command);
        updateCategoryMap();
    }
    
    public void unregisterCommand(Command command) {
        commands.remove(command);
        updateCategoryMap();
    }
    
    private void updateCategoryMap() {
        categoryMap.clear();
        for (Command command : commands) {
            for (String category : command.getCategory()) {
                categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(command);
            }
        }
    }
    
    public List<Command> getCommands() {
        return new ArrayList<>(commands);
    }
    
    public List<Command> getCommandsByCategory(String category) {
        return categoryMap.getOrDefault(category, new ArrayList<>());
    }
    
    public Set<String> getCategories() {
        return new HashSet<>(categoryMap.keySet());
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(ChatEvent event) {
        String message = event.getMessage();
        if (message.startsWith(prefix)) {
            event.setCancelled(true);
            executeCommand(message);
        }
    }
    
    public void executeCommand(String message) {
        String[] split = message.substring(prefix.length()).split(" ");
        String commandName = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        
        Command command = findCommand(commandName);
        if (command != null) {
            try {
                command.execute(args);
            } catch (Exception e) {
                sendMessage("§cError executing command: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            sendMessage("§cUnknown command. Type " + prefix + "help for help.");
        }
    }
    
    public List<String> tabComplete(String input) {
        if (!input.startsWith(prefix)) return new ArrayList<>();
        
        String[] split = input.substring(prefix.length()).split(" ");
        if (split.length == 0) return new ArrayList<>();
        
        if (split.length == 1) {
            String commandStart = split[0].toLowerCase();
            return commands.stream()
                .filter(cmd -> !cmd.isHidden())
                .filter(cmd -> cmd.getName().toLowerCase().startsWith(commandStart) ||
                             Arrays.stream(cmd.getAliases())
                                   .anyMatch(alias -> alias.toLowerCase().startsWith(commandStart)))
                .map(Command::getName)
                .collect(Collectors.toList());
        }
        
        Command command = findCommand(split[0]);
        if (command != null) {
            return command.tabComplete(Arrays.copyOfRange(split, 1, split.length));
        }
        
        return new ArrayList<>();
    }
    
    private Command findCommand(String name) {
        return commands.stream()
            .filter(cmd -> cmd.getName().equalsIgnoreCase(name) ||
                          Arrays.stream(cmd.getAliases())
                                .anyMatch(alias -> alias.equalsIgnoreCase(name)))
            .findFirst()
            .orElse(null);
    }
    
    public void sendMessage(String message) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§8[§bNyxion§8] §7" + message)
            );
        }
    }
}