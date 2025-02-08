package me.nyxion.commands.impl;

import me.nyxion.commands.Command;

public class InfoCommand extends Command {
    public InfoCommand() {
        super("info", "Shows information about the client", ".info", "Info", "about", "version");
        
        // Add command executor function
        addExecutor(args -> {
            sendMessage("§b§lNyxionX Client");
            sendMessage("§7Version: §f1.8.9");
            sendMessage("§7Created by: §fNyxionXTeam");
            sendMessage("§7Type §f.help §7for a list of commands");
        });
    }
    
    @Override
    public void execute(String[] args) {
        executeAll(args);
    }
}