package com.mertaliakcay.malinesscore.systems.control;

import com.mertaliakcay.malinesscore.MaliNessCore;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class SystemsBasicCommand implements BasicCommand {

    private final MaliNessCore plugin;
    private final SystemControlService controlService;

    public SystemsBasicCommand(MaliNessCore plugin, SystemControlService controlService) {
        this.plugin = plugin;
        this.controlService = controlService;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();

        if (!(sender instanceof Player)) {
            plugin.getPluginLang().send(sender, "systems-console-unsupported");
            return;
        }

        if (!controlService.canList(sender)) {
            plugin.getPluginLang().send(sender, "systems-no-list-permission");
            return;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                plugin.getPluginLang().send(sender, "systems-list-invalid-page");
                return;
            }
        }

        SystemsListHelp.send(plugin, controlService, sender, page);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return true;
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3");
        }
        return List.of();
    }
}
