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
            plugin.getPluginLang().send(sender, "mn-root-usage");
            return true;
        }

        if (isHealSubcommand(args[0])) {
            if (healCommand == null || healSystem == null) {
                if (healSystem != null) {
                    healSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            healCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isFeedSubcommand(args[0])) {
            if (feedCommand == null || feedSystem == null) {
                if (feedSystem != null) {
                    feedSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            feedCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isHealthSubcommand(args[0])) {
            if (healthCommand == null || healthSystem == null) {
                if (healthSystem != null) {
                    healthSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            healthCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isHungerSubcommand(args[0])) {
            if (hungerCommand == null || hungerSystem == null) {
                if (hungerSystem != null) {
                    hungerSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            hungerCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isSaturateSubcommand(args[0])) {
            if (saturateCommand == null || saturateSystem == null) {
                if (saturateSystem != null) {
                    saturateSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            saturateCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isSaturationSubcommand(args[0])) {
            if (saturationCommand == null || saturationSystem == null) {
                if (saturationSystem != null) {
                    saturationSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            saturationCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (isGodSubcommand(args[0])) {
            if (godCommand == null || godSystem == null) {
                if (godSystem != null) {
                    godSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            godCommand.handle(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        if (HomeMnCommand.isHomeSubcommand(args[0])) {
            if (homeMnCommand == null || homeSystem == null) {
                if (homeSystem != null) {
                    homeSystem.getLang().send(sender, "system-disabled");
                } else {
                    plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
                }
                return true;
            }
            homeMnCommand.handle(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        plugin.getPluginLang().send(sender, "mn-unknown-subcommand", "subcommand", args[0]);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> subcommands = topLevelSubcommands();

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

    private List<String> topLevelSubcommands() {
        List<String> subcommands = new ArrayList<>();
        if (healSystem != null && healSystem.isEnabled()) {
            subcommands.add("heal");
            subcommands.add(HealSystem.ALIAS_TURKISH);
        }
        if (feedSystem != null && feedSystem.isEnabled()) {
            subcommands.add("feed");
            subcommands.add(FeedSystem.ALIAS_TURKISH);
        }
        if (healthSystem != null && healthSystem.isEnabled()) {
            subcommands.add("health");
            subcommands.add(HealthSystem.ALIAS_TURKISH);
        }
        if (hungerSystem != null && hungerSystem.isEnabled()) {
            subcommands.add("hunger");
            subcommands.add(HungerSystem.ALIAS_TURKISH);
        }
        if (saturateSystem != null && saturateSystem.isEnabled()) {
            subcommands.add("saturate");
            subcommands.add(SaturateSystem.ALIAS_TURKISH);
        }
        if (saturationSystem != null && saturationSystem.isEnabled()) {
            subcommands.add("saturation");
            subcommands.add(SaturationSystem.ALIAS_TURKISH);
        }
        if (godSystem != null && godSystem.isEnabled()) {
            subcommands.add("god");
            subcommands.add(GodSystem.ALIAS_TURKISH);
        }
        if (homeSystem != null && homeSystem.isEnabled()) {
            subcommands.addAll(HomeMnCommand.subcommandNames());
        }
        return subcommands;
    }

    private List<String> nestedTabComplete(CommandSender sender, Command command, String alias, String subcommand, String[] nestedArgs) {
        if (isHealSubcommand(subcommand) && healCommand != null) {
            return nullableList(healCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isFeedSubcommand(subcommand) && feedCommand != null) {
            return nullableList(feedCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isHealthSubcommand(subcommand) && healthCommand != null) {
            return nullableList(healthCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isHungerSubcommand(subcommand) && hungerCommand != null) {
            return nullableList(hungerCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isSaturateSubcommand(subcommand) && saturateCommand != null) {
            return nullableList(saturateCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isSaturationSubcommand(subcommand) && saturationCommand != null) {
            return nullableList(saturationCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (isGodSubcommand(subcommand) && godCommand != null) {
            return nullableList(godCommand.onTabComplete(sender, command, alias, nestedArgs));
        }

        if (HomeMnCommand.isHomeSubcommand(subcommand) && homeMnCommand != null) {
            return nullableList(homeMnCommand.onTabComplete(sender, subcommand, nestedArgs));
        }

        return Collections.emptyList();
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
}
