package org.btuk.minecraft.plugin;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import org.btuk.minecraft.commands.CommandManager;
import org.btuk.minecraft.commands.ReloadCommand;
import org.btuk.minecraft.config.PluginConfig;

public abstract class ReloadablePlugin extends JavaPlugin {

    @Getter
    private PluginConfig config;

    @Override
    public void onEnable() {
        this.config = new PluginConfig(this);

        CommandManager commandManager = new CommandManager(this);
        commandManager.registerCommand(getReloadCommand());

        startPlugin(commandManager);

        commandManager.enableCommands();
    }

    @Override
    public void onDisable() {
        // Run any logic required before disabling the plugin.
    }

    /**
     * Reloads the config.
     */
    public void reload() {
        if (config != null) {
            this.config.reload();
        }
    }

    /**
     * Method to start the plugin.
     *
     * @param commandManager the commands manager to register commands with.
     */
    protected abstract void startPlugin(CommandManager commandManager);

    protected abstract ReloadCommand getReloadCommand();
}
