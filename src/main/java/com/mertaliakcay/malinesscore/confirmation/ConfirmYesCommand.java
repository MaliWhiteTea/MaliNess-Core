package com.mertaliakcay.malinesscore.confirmation;

import com.mertaliakcay.malinesscore.MaliNessCore;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ConfirmYesCommand implements BasicCommand {

    private final MaliNessCore plugin;

    public ConfirmYesCommand(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (!(source.getSender() instanceof Player player)) {
            return;
        }

        if (args.length == 0) {
            String token = plugin.getConfirmationService().getPendingToken(player.getUniqueId());
            if (token == null) {
                plugin.getPluginLang().send(player, "confirm-nothing-pending");
                return;
            }
            plugin.getConfirmationService().accept(player, token);
            return;
        }

        plugin.getConfirmationService().accept(player, args[0]);
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("maliness-core.confirm.use");
    }
}
