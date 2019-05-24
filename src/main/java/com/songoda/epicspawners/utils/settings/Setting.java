package com.songoda.epiclevels.utils.settings;

import com.songoda.epiclevels.EpicLevels;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Setting {

    AUTOSAVE("Main.Auto Save Interval In Seconds", 15,
            "The amount of time in between saving to file.",
            "This is purely a safety function to prevent against unplanned crashes or",
            "restarts. With that said it is advised to keep this enabled.",
            "If however you enjoy living on the edge, feel free to turn it off."),

    EXP_MOB("Main.Experience Gained Per Mob Kill", 2L,
            "The amount of experience gained per monster kill."),

    EXP_PLAYER("Main.Experience Gained Per Player Kill", 250L,
            "The amount of experience gained per player kill."),

    EXP_DEATH("Main.Experience lost On Death", 200L,
            "The amount of experience lost when a player is killed",
            "by another player."),

    ALLOW_NEGATIVE("Main.Allow Negative Experience", false,
            "Allow players to lose experience into negative?"),

    SEND_KILL_MESSAGE("Main.Send Message On Kill", true,
            "Should Players be notified when they kill another player?"),

    SEND_DEATH_MESSAGE("Main.Send Message On Death", true,
            "Should players be notified when killed?"),

    SEND_BROADCAST_DEATH_MESSAGE("Main.Send Broadcast On Death", true,
            "Broadcasts a players death to the whole server."),

    SEND_BROADCAST_LEVELUP_MESSAGE("Main.Send Broadcast On Levelup", true,
            "Broadcasts a players level up to the whole server."),

    BROADCAST_LEVELUP_EVERY("Main.Broadcast Levelup Every", 5,
            "How often should a level up be announced?",
            "If you enter 5 every 5 levels players will be notified."),

    MAX_LEVEL("Main.Max Level", 99,
            "The maximum allowed level.",
            "Note that once reached players will still earn experience beyond",
            "the the maximum level without the ability to level up."),

    MAX_EXP("Main.Max Experience", 200000000L,
            "The maximum allowed experience."),

    START_EXP("Main.Starting Experience", 0L,
            "The amount of experience players start with."),

    START_PVP_LEVEL("Main.Level Required For PVP", 0,
            "The minimum level required to engage or be engaged with in combat."),

    BLACKLISTED_WORLDS("Main.Blacklisted Worlds", Arrays.asList("World1", "World2", "World3"),
            "Worlds that kills are not counted in."),

    MAX_EXTRA_HEALTH("Main.Max Extra Health", 10,
            "The maximum amount of health a player can gain through levels."),

    EXTRA_HEALTH_PER_LEVEL("Main.Extra Health Per Level", 0.2,
            "The amount of health a player will gain per level.",
            "1 would be half a heart and 20 would be a 10 hearts."),

    MAX_EXTRA_DAMAGE("Main.Max Extra Damage", 2.0,
            "The maximum amount of extra damage applied to a players attack",
            "per level."),

    EXTRA_DAMAGE_PER_LEVEL("Main.Extra Damage Per Level", 0.05,
            "The amount of additional experience applied to a player per",
            "level."),

    RUN_KILLSTREAK_EVERY("Main.Run Killstreaks Reward Every", 3,
            "A player must achieve a multiple of the following number in order for the reward",
            "script to run.",
            "You can set this to 1 if you would like the script to be ran with every kill."),

    KILLSTREAK_BONUS_EXP("Main.Killstreak Bonus Experience", 0.2,
            "For each kill in a killstreak the following number will be used to boost your",
            "experience gain (streak * exp)."),

    PROGRESS_BAR_LENGTH("Main.Progress Bar Length", 36,
            "The length of the progress bar in the levels GUI."),

    PROGRESS_BAR_LENGTH_PLACEHOLDER("Main.Progress Bar Length Placeholder", 20,
            "The length of the progress bar in placeholders."),

    ANTI_GRINDER("Anti Grinder.Enabled", true,
            "Enabling this will enable the anti grinder timeout.",
            "In its default configuration it will prevent a player from killing the same player",
            "more than 3 times in 15 minutes."),

    GRINDER_INTERVAL("Anti Grinder.Interval", 900,
            "How long in seconds should a player have to wait once triggered?"),

    GRINDER_MAX("Anti Grinder.Max Kills Before Trigger", 3,
            "How many kills before trigger?"),

    GRINDER_ALERT("Anti Grinder.Alert When Triggered", true,
            "Should we alert the killer when they have reached the threshold?"),

    LEVELING_FORMULA("Formula.Leveling", "EXPONENTIAL",
            "This is the formula used when calculating a players level",
            "", "LINEAR: All levels require the same amount of experience to reach the next level.",
            "EXPONENTIAL: Levels will increasingly require more experience the higher level you are.",
            "CUSTOM: You can enter a custom JavaScript equation here to finely tune your experience gain."),

    LINEAR_INCREMENT("Formula.Linear Increment", 500,
            "The stagnant amount of experience required to level up."),

    EXPONENTIAL_BASE("Formula.Exponential Base", 500,
            "You need to know what you're doing to mess with this, feel",
            "free to put in random values and test around however."),

    EXPONENTIAL_DIVISOR("Formula.Exponential Divisor", 7,
            "You need to know what you're doing to mess with this, feel",
            "free to put in random values and test around however."),

    CUSTOM_FORMULA("Formula.Custom", "Math.pow(1.5, level) * Math.sqrt(level) * 2;",
            "This is the custom formula. You can use any JavaScript math functions here."),

    GLASS_TYPE_1("Interfaces.Glass Type 1", 7),
    GLASS_TYPE_2("Interfaces.Glass Type 2", 11),
    GLASS_TYPE_3("Interfaces.Glass Type 3", 3),

    LANGUGE_MODE("System.Language Mode", "en_US",
            "The enabled language file.",
            "More language files (if available) can be found in the plugins data folder.");

    private String setting;
    private Object option;
    private String[] comments;

    Setting(String setting, Object option, String... comments) {
        this.setting = setting;
        this.option = option;
        this.comments = comments;
    }

    Setting(String setting, Object option) {
        this.setting = setting;
        this.option = option;
        this.comments = null;
    }

    public static Setting getSetting(String setting) {
        List<Setting> settings = Arrays.stream(values()).filter(setting1 -> setting1.setting.equals(setting)).collect(Collectors.toList());
        if (settings.isEmpty()) return null;
        return settings.get(0);
    }

    public String getSetting() {
        return setting;
    }

    public Object getOption() {
        return option;
    }

    public String[] getComments() {
        return comments;
    }

    public List<String> getStringList() {
        return EpicLevels.getInstance().getConfig().getStringList(setting);
    }

    public boolean getBoolean() {
        return EpicLevels.getInstance().getConfig().getBoolean(setting);
    }

    public int getInt() {
        return EpicLevels.getInstance().getConfig().getInt(setting);
    }

    public long getLong() {
        return EpicLevels.getInstance().getConfig().getLong(setting);
    }

    public String getString() {
        return EpicLevels.getInstance().getConfig().getString(setting);
    }

    public char getChar() { return EpicLevels.getInstance().getConfig().getString(setting).charAt(0); }

    public double getDouble() {
        return EpicLevels.getInstance().getConfig().getDouble(setting);
    }
}