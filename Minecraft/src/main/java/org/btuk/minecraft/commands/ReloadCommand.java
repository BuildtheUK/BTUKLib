package org.btuk.minecraft.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import org.btuk.minecraft.misc.ComponentUtils;
import org.btuk.minecraft.plugin.ReloadablePlugin;

public abstract class ReloadCommand extends Command {

    private final ReloadablePlugin plugin;

    protected ReloadCommand(ReloadablePlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract String getPluginName();

    protected abstract String getReloadPermission();

    @Override
    public String getLabel() {
        return getPluginName();
    }

    @Override
    public String getDescription() {
        return "Command to reload the plugin configuration.";
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String @NonNull [] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (commandSourceStack.getSender() instanceof Player player && !player.hasPermission(getReloadPermission())) {
                player.sendMessage(ComponentUtils.error("You do not have permission to use this command."));
                return;
            }
            plugin.reload();
            commandSourceStack.getSender().sendMessage(ComponentUtils.success("The plugin configuration has been reloaded."));
            return;
        }

        handleArgs(commandSourceStack, args);
    }

    protected abstract void handleArgs(CommandSourceStack commandSourceStack, String[] args);
}
