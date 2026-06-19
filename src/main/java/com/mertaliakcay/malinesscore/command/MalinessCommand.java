package com.mertaliakcay.malinesscore.command;

import com.mertaliakcay.malinesscore.MaliNessCore;
import com.mertaliakcay.malinesscore.systems.feed.FeedCommand;
import com.mertaliakcay.malinesscore.systems.feed.FeedSystem;
import com.mertaliakcay.malinesscore.systems.heal.HealCommand;
import com.mertaliakcay.malinesscore.systems.heal.HealSystem;
import com.mertaliakcay.malinesscore.systems.god.GodCommand;
import com.mertaliakcay.malinesscore.systems.god.GodSystem;
import com.mertaliakcay.malinesscore.systems.home.HomeMnCommand;
import com.mertaliakcay.malinesscore.systems.home.HomeSystem;
import com.mertaliakcay.malinesscore.systems.health.HealthCommand;
import com.mertaliakcay.malinesscore.systems.health.HealthSystem;
import com.mertaliakcay.malinesscore.systems.hunger.HungerCommand;
import com.mertaliakcay.malinesscore.systems.hunger.HungerSystem;
import com.mertaliakcay.malinesscore.systems.saturate.SaturateCommand;
import com.mertaliakcay.malinesscore.systems.saturate.SaturateSystem;
import com.mertaliakcay.malinesscore.systems.saturation.SaturationCommand;
import com.mertaliakcay.malinesscore.systems.saturation.SaturationSystem;
import com.mertaliakcay.malinesscore.systems.AbstractGameSystem;
import com.mertaliakcay.malinesscore.util.CommandSuggestions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MalinessCommand implements CommandExecutor, TabCompleter {

    public static final String PERM_RELOAD = "maliness-core.reload";

    private final MaliNessCore plugin;
    private HealSystem healSystem;
    private HealCommand healCommand;
    private FeedSystem feedSystem;
    private FeedCommand feedCommand;
    private HealthSystem healthSystem;
    private HealthCommand healthCommand;
    private HungerSystem hungerSystem;
    private HungerCommand hungerCommand;
    private SaturateSystem saturateSystem;
    private SaturateCommand saturateCommand;
    private SaturationSystem saturationSystem;
    private SaturationCommand saturationCommand;
    private GodSystem godSystem;
    private GodCommand godCommand;
    private HomeSystem homeSystem;
    private HomeMnCommand homeMnCommand;

    public MalinessCommand(MaliNessCore plugin) {
        this.plugin = plugin;
    }

    public void setHeal(HealSystem healSystem, HealCommand healCommand) {
        this.healSystem = healSystem;
        this.healCommand = healCommand;
    }

    public void clearHeal() {
        this.healSystem = null;
        this.healCommand = null;
    }

    public void setFeed(FeedSystem feedSystem, FeedCommand feedCommand) {
        this.feedSystem = feedSystem;
        this.feedCommand = feedCommand;
    }

    public void clearFeed() {
        this.feedSystem = null;
        this.feedCommand = null;
    }

    public void setHealth(HealthSystem healthSystem, HealthCommand healthCommand) {
        this.healthSystem = healthSystem;
        this.healthCommand = healthCommand;
    }

    public void clearHealth() {
        this.healthSystem = null;
        this.healthCommand = null;
    }

    public void setHunger(HungerSystem hungerSystem, HungerCommand hungerCommand) {
        this.hungerSystem = hungerSystem;
        this.hungerCommand = hungerCommand;
    }

    public void clearHunger() {
        this.hungerSystem = null;
        this.hungerCommand = null;
    }

    public void setSaturate(SaturateSystem saturateSystem, SaturateCommand saturateCommand) {
        this.saturateSystem = saturateSystem;
        this.saturateCommand = saturateCommand;
    }

    public void clearSaturate() {
        this.saturateSystem = null;
        this.saturateCommand = null;
    }

    public void setSaturation(SaturationSystem saturationSystem, SaturationCommand saturationCommand) {
        this.saturationSystem = saturationSystem;
        this.saturationCommand = saturationCommand;
    }

    public void clearSaturation() {
        this.saturationSystem = null;
        this.saturationCommand = null;
    }

    public void setGod(GodSystem godSystem, GodCommand godCommand) {
        this.godSystem = godSystem;
        this.godCommand = godCommand;
    }

    public void clearGod() {
        this.godSystem = null;
        this.godCommand = null;
    }

    public void setHome(HomeSystem homeSystem, HomeMnCommand homeMnCommand) {
        this.homeSystem = homeSystem;
        this.homeMnCommand = homeMnCommand;
    }

    public void clearHome() {
        this.homeSystem = null;
        this.homeMnCommand = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            MnCommandHelp.send(plugin, this, sender, 1);
            return true;
        }

        if (args.length == 1 && isHelpPage(args[0])) {
            MnCommandHelp.send(plugin, this, sender, Integer.parseInt(args[0]));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(PERM_RELOAD)) {
                plugin.getPluginLang().send(sender, "reload-no-permission");
                return true;
            }
            plugin.reloadPlugin(sender);
            return true;
        }

        if (isHealSubcommand(args[0])) {
            if (!dispatchSystem(sender, healSystem, args[0])) {
                return true;
            }
            healCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isFeedSubcommand(args[0])) {
            if (!dispatchSystem(sender, feedSystem, args[0])) {
                return true;
            }
            feedCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isHealthSubcommand(args[0])) {
            if (!dispatchSystem(sender, healthSystem, args[0])) {
                return true;
            }
            healthCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isHungerSubcommand(args[0])) {
            if (!dispatchSystem(sender, hungerSystem, args[0])) {
                return true;
            }
            hungerCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isSaturateSubcommand(args[0])) {
            if (!dispatchSystem(sender, saturateSystem, args[0])) {
                return true;
            }
            saturateCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isSaturationSubcommand(args[0])) {
            if (!dispatchSystem(sender, saturationSystem, args[0])) {
                return true;
            }
            saturationCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isGodSubcommand(args[0])) {
            if (!dispatchSystem(sender, godSystem, args[0])) {
                return true;
            }
            godCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (HomeMnCommand.isHomeSubcommand(args[0])) {
            if (!dispatchSystem(sender, homeSystem, args[0])) {
                return true;
            }
            homeMnCommand.handle(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
        return true;
    }

    private boolean dispatchSystem(CommandSender sender, AbstractGameSystem system, String subcommand) {
        if (system == null) {
            plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", subcommand);
            return false;
        }

        if (!system.isEnabled()) {
            system.getLang().send(sender, "system-disabled");
            return false;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> subcommands = topLevelSubcommands(sender);

        if (args.length == 0) {
            return CommandSuggestions.filter(subcommands, "");
        }

        if (args.length == 1) {
            if (CommandSuggestions.isExactMatch(args[0], subcommands)) {
                return Collections.emptyList();
            }
            return CommandSuggestions.filter(subcommands, args[0]);
        }

        if (args.length >= 2) {
            return nestedTabComplete(sender, command, alias, args[0], Arrays.copyOfRange(args, 1, args.length));
        }

        return Collections.emptyList();
    }

    private List<String> topLevelSubcommands(CommandSender sender) {
        List<String> subcommands = new ArrayList<>();
        if (sender.hasPermission(PERM_RELOAD)) {
            subcommands.add("reload");
        }
        if (isHealAvailable(sender)) {
            subcommands.add("heal");
            subcommands.add(HealSystem.ALIAS_TURKISH);
        }
        if (isFeedAvailable(sender)) {
            subcommands.add("feed");
            subcommands.add(FeedSystem.ALIAS_TURKISH);
        }
        if (isHealthAvailable(sender)) {
            subcommands.add("health");
            subcommands.add(HealthSystem.ALIAS_TURKISH);
        }
        if (isHungerAvailable(sender)) {
            subcommands.add("hunger");
            subcommands.add(HungerSystem.ALIAS_TURKISH);
        }
        if (isSaturateAvailable(sender)) {
            subcommands.add("saturate");
            subcommands.add(SaturateSystem.ALIAS_TURKISH);
        }
        if (isSaturationAvailable(sender)) {
            subcommands.add("saturation");
            subcommands.add(SaturationSystem.ALIAS_TURKISH);
        }
        if (isGodAvailable(sender)) {
            subcommands.add("god");
            subcommands.add(GodSystem.ALIAS_TURKISH);
        }
        addAvailableHomeSubcommands(sender, subcommands);
        return subcommands;
    }

    private void addAvailableHomeSubcommands(CommandSender sender, List<String> subcommands) {
        if (homeSystem == null || !homeSystem.isEnabled()) {
            return;
        }
        if (isHomeSetAvailable(sender)) {
            subcommands.add("sethome");
            subcommands.add(HomeSystem.ALIAS_SETHOME);
        }
        if (isHomeTeleportAvailable(sender)) {
            subcommands.add("home");
            subcommands.add(HomeSystem.ALIAS_HOME_1);
            subcommands.add(HomeSystem.ALIAS_HOME_2);
        }
        if (isHomeDeleteAvailable(sender)) {
            subcommands.add("delhome");
            subcommands.add("remhome");
        }
        if (isHomeListAvailable(sender)) {
            subcommands.add("homes");
        }
        if (isHomeRenameAvailable(sender)) {
            subcommands.add("renamehome");
            subcommands.add(HomeSystem.ALIAS_RENAME_1);
            subcommands.add(HomeSystem.ALIAS_RENAME_2);
        }
    }

    private boolean isHelpPage(String arg) {
        try {
            return Integer.parseInt(arg) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private List<String> nestedTabComplete(CommandSender sender, Command command, String alias, String subcommand, String[] nestedArgs) {
        if (isHealSubcommand(subcommand)) {
            if (!isHealAvailable(sender) || healCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(healCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isFeedSubcommand(subcommand)) {
            if (!isFeedAvailable(sender) || feedCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(feedCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isHealthSubcommand(subcommand)) {
            if (!isHealthAvailable(sender) || healthCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(healthCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isHungerSubcommand(subcommand)) {
            if (!isHungerAvailable(sender) || hungerCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(hungerCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isSaturateSubcommand(subcommand)) {
            if (!isSaturateAvailable(sender) || saturateCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(saturateCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isSaturationSubcommand(subcommand)) {
            if (!isSaturationAvailable(sender) || saturationCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(saturationCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isGodSubcommand(subcommand)) {
            if (!isGodAvailable(sender) || godCommand == null) {
                return Collections.emptyList();
            }
            return nullableList(godCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (HomeMnCommand.isHomeSubcommand(subcommand)) {
            if (homeMnCommand == null || !isHomeSubcommandAvailable(sender, subcommand)) {
                return Collections.emptyList();
            }
            return nullableList(homeMnCommand.onTabComplete(sender, subcommand, nestedArgs));
        }

        return Collections.emptyList();
    }

    private boolean isHomeSubcommandAvailable(CommandSender sender, String subcommand) {
        if (HomeMnCommand.isSetHome(subcommand)) {
            return isHomeSetAvailable(sender);
        }
        if (HomeMnCommand.isHome(subcommand)) {
            return isHomeTeleportAvailable(sender);
        }
        if (HomeMnCommand.isDelHome(subcommand)) {
            return isHomeDeleteAvailable(sender);
        }
        if (HomeMnCommand.isHomes(subcommand)) {
            return isHomeListAvailable(sender);
        }
        if (HomeMnCommand.isRenameHome(subcommand)) {
            return isHomeRenameAvailable(sender);
        }
        return false;
    }

    private List<String> nullableList(List<String> suggestions) {
        return suggestions == null ? Collections.emptyList() : suggestions;
    }

    private boolean isHealSubcommand(String arg) {
        return arg.equalsIgnoreCase("heal") || arg.equalsIgnoreCase(HealSystem.ALIAS_TURKISH);
    }

    private boolean isFeedSubcommand(String arg) {
        return arg.equalsIgnoreCase("feed") || arg.equalsIgnoreCase(FeedSystem.ALIAS_TURKISH);
    }

    private boolean isHealthSubcommand(String arg) {
        return arg.equalsIgnoreCase("health") || arg.equalsIgnoreCase(HealthSystem.ALIAS_TURKISH);
    }

    private boolean isHungerSubcommand(String arg) {
        return arg.equalsIgnoreCase("hunger") || arg.equalsIgnoreCase(HungerSystem.ALIAS_TURKISH);
    }

    private boolean isSaturateSubcommand(String arg) {
        return arg.equalsIgnoreCase("saturate") || arg.equalsIgnoreCase(SaturateSystem.ALIAS_TURKISH);
    }

    private boolean isSaturationSubcommand(String arg) {
        return arg.equalsIgnoreCase("saturation") || arg.equalsIgnoreCase(SaturationSystem.ALIAS_TURKISH);
    }

    private boolean isGodSubcommand(String arg) {
        return arg.equalsIgnoreCase("god") || arg.equalsIgnoreCase(GodSystem.ALIAS_TURKISH);
    }

    boolean isHealAvailable(CommandSender sender) {
        return healSystem != null && healSystem.isEnabled()
                && (sender.hasPermission(HealSystem.PERM_USE) || sender.hasPermission(HealSystem.PERM_OTHERS));
    }

    boolean isFeedAvailable(CommandSender sender) {
        return feedSystem != null && feedSystem.isEnabled()
                && (sender.hasPermission(FeedSystem.PERM_USE) || sender.hasPermission(FeedSystem.PERM_OTHERS));
    }

    boolean isHealthAvailable(CommandSender sender) {
        return healthSystem != null && healthSystem.isEnabled()
                && sender.hasPermission(HealthSystem.PERM_USE);
    }

    boolean isHungerAvailable(CommandSender sender) {
        return hungerSystem != null && hungerSystem.isEnabled()
                && sender.hasPermission(HungerSystem.PERM_USE);
    }

    boolean isSaturateAvailable(CommandSender sender) {
        return saturateSystem != null && saturateSystem.isEnabled()
                && (sender.hasPermission(SaturateSystem.PERM_USE) || sender.hasPermission(SaturateSystem.PERM_OTHERS));
    }

    boolean isSaturationAvailable(CommandSender sender) {
        return saturationSystem != null && saturationSystem.isEnabled()
                && sender.hasPermission(SaturationSystem.PERM_USE);
    }

    boolean isGodAvailable(CommandSender sender) {
        return godSystem != null && godSystem.isEnabled()
                && (sender.hasPermission(GodSystem.PERM_USE) || sender.hasPermission(GodSystem.PERM_OTHERS));
    }

    boolean isHomeSetAvailable(CommandSender sender) {
        return homeSystem != null && homeSystem.isEnabled()
                && sender.hasPermission(HomeSystem.PERM_SETHOME);
    }

    boolean isHomeTeleportAvailable(CommandSender sender) {
        return homeSystem != null && homeSystem.isEnabled()
                && (sender.hasPermission(HomeSystem.PERM_USE) || sender.hasPermission(HomeSystem.PERM_OTHERS_TELEPORT));
    }

    boolean isHomeDeleteAvailable(CommandSender sender) {
        return homeSystem != null && homeSystem.isEnabled()
                && (sender.hasPermission(HomeSystem.PERM_DELHOME) || sender.hasPermission(HomeSystem.PERM_OTHERS_DELETE));
    }

    boolean isHomeListAvailable(CommandSender sender) {
        return homeSystem != null && homeSystem.isEnabled()
                && (sender.hasPermission(HomeSystem.PERM_HOMES) || sender.hasPermission(HomeSystem.PERM_OTHERS_LIST));
    }

    boolean isHomeRenameAvailable(CommandSender sender) {
        return homeSystem != null && homeSystem.isEnabled()
                && sender.hasPermission(HomeSystem.PERM_RENAME);
    }
}
