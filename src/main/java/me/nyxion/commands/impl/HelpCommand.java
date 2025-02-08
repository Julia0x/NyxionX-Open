package me.nyxion.commands.impl;

import me.nyxion.commands.Command;
import me.nyxion.commands.CommandManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class HelpCommand extends Command {
    private final CommandManager commandManager;
    
    public HelpCommand(CommandManager commandManager) {
        super("help", "Shows command information", ".help [category/command]", "Info", "?", "commands");
        this.commandManager = commandManager;
        
        // Add tab completer function
        addTabCompleter(this::handleTabComplete);
        
        // Add command executor function
        addExecutor(this::handleHelp);
    }
    
    @Override
    public void execute(String[] args) {
        executeAll(args);
    }
    
    private List<String> handleTabComplete(String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            String start = args[0].toLowerCase();
            // Add categories
            commandManager.getCategories().stream()
                .filter(category -> category.toLowerCase().startsWith(start))
                .forEach(completions::add);
            // Add command names
            commandManager.getCommands().stream()
                .filter(cmd -> !cmd.isHidden())
                .filter(cmd -> cmd.getName().toLowerCase().startsWith(start))
                .map(Command::getName)
                .forEach(completions::add);
        }
        return completions;
    }
    
    private void handleHelp(String[] args) {
        if (args.length == 0) {
            showCategories();
        } else {
            handleHelpQuery(args[0]);
        }
    }
    
    private void showCategories() {
        Set<String> categories = commandManager.getCategories();
        sendMessage("§bCommand Categories:");
        for (String category : categories) {
            List<Command> categoryCommands = commandManager.getCommandsByCategory(category);
            int visibleCommands = (int) categoryCommands.stream()
                .filter(cmd -> !cmd.isHidden())
                .count();
            if (visibleCommands > 0) {
                sendMessage("§7" + category + " §8(§7" + visibleCommands + "§8)");
            }
        }
        sendMessage("§7Type " + commandManager.getPrefix() + "help <category> to view commands");
    }
    
    private void handleHelpQuery(String query) {
        query = query.toLowerCase();
        
        // Check if it's a category
        List<Command> categoryCommands = commandManager.getCommandsByCategory(query);
        if (!categoryCommands.isEmpty()) {
            showCategoryCommands(query, categoryCommands);
            return;
        }
        
        // Check if it's a command
        Command command = findCommand(query);
        if (command != null && !command.isHidden()) {
            showCommandHelp(command);
            return;
        }
        
        sendMessage("§cNo category or command found for: " + query);
    }
    
    private void showCategoryCommands(String category, List<Command> commands) {
        sendMessage("§b" + category + " Commands:");
        commands.stream()
            .filter(cmd -> !cmd.isHidden())
            .forEach(cmd -> sendMessage("§7" + cmd.getSyntax() + " §8- §7" + cmd.getDescription()));
    }
    
    private void showCommandHelp(Command command) {
        sendMessage("§bCommand: " + command.getName());
        sendMessage("§7Description: §f" + command.getDescription());
        sendMessage("§7Syntax: §f" + command.getSyntax());
        if (command.getAliases().length > 0) {
            sendMessage("§7Aliases: §f" + String.join(", ", command.getAliases()));
        }
    }
    
    private Command findCommand(String name) {
        return commandManager.getCommands().stream()
            .filter(cmd -> cmd.getName().equalsIgnoreCase(name) ||
                          java.util.Arrays.stream(cmd.getAliases())
                                        .anyMatch(alias -> alias.equalsIgnoreCase(name)))
            .findFirst()
            .orElse(null);
    }
}