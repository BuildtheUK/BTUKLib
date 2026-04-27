package org.btuk.minecraft.commands;

import io.papermc.paper.command.brigadier.BasicCommand;

import java.util.Collections;
import java.util.List;

public abstract class Command implements BasicCommand {

    public abstract String getLabel();

    public abstract String getDescription();

    public List<String> getAliases() {
        return Collections.emptyList();
    }
}
