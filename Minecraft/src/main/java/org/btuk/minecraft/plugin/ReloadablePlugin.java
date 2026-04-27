package org.btuk.minecraft.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import org.btuk.minecraft.commands.CommandManager;
import org.btuk.minecraft.commands.ReloadCommand;
import org.btuk.minecraft.config.Config;

public abstract class ReloadablePlugin extends JavaPlugin {

    private Config config;

    @Override
    public void onEnable() {
        this.config = loadConfig();

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
     * Loads the config from the config.yml file.
     *
     * @return the config instance.
     */
    protected abstract Config loadConfig();

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
