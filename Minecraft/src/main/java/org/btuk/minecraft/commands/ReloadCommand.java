package org.btuk.minecraft.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import org.btuk.minecraft.component.ComponentUtils;
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
        if (commandSourceStack.getSender() instanceof Player player && !player.hasPermission(getReloadPermission())) {
            player.sendMessage(ComponentUtils.error("You do not have permission to use this command."));
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reload();
        }
    }
}
