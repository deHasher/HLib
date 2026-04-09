package net.dehasher.hlib.data;

import lombok.Getter;

import java.util.stream.Stream;

@SuppressWarnings("NonAsciiCharacters")
public enum Permission {
    CMI_COMMAND_FLY(false, false),
    CMI_COMMAND_WALKSPEED(false, false),
    CMI_COMMAND_FLYSPEED(false, false),
    CMI_SEEVANISHED(false, false),

    HCONTRACTS_HIDE(false, false), // На игрока с этим правом нельзя взять контракт.
    HCLANS_USE(false, true), // Позволяет использовать команду /clans.
    HCLANS_ADMIN(true, false), // Позволяет удалять чужие кланы и перезагружать плагин.
    HCLANS_SPY(false, false), // Позволяет следить за сообщениями кланов.
    HSEX_BYPASS(false, false), // Позволяет ебаца без кд и со своим полом. (осуждаю)
    HANTIRELOG_BYPASS(false, false), // Позволяет обходить антирелог.
    HMARRY_USE(false, true), // Позволяет использовать команду /marry.
    HMARRY_SPY(false, false), // Позволяет следить за сообщениями пар.
    HMARRY_ADMIN(true, false), // Позволяет перезагружать плагин.

    HIMAGES_AVAILABLE(true, false),
    HIMAGES_WORLDGUARD(true, false),
    HIMAGES_UNLIMITED(true, false),
    HIMAGES_SCALE(true, false),
    HIMAGES_MOD(false, false),
    HCINEMA_BYPASS(true, false),
    HCINEMA_ALLOW_JOINTOTHEATER(false, false),

    HCORE_BYPASS_AI(false, false), // Позволяет отправлять сообщения к нейросети без задержки и запросы в нейросеть никто не видит.
    HCORE_BYPASS_PVP(true, false), // Позволяет использовать возможности по типу /fly на пвп-арене.
    HCORE_BYPASS_PVP_COMMANDS(false, false), // Позволяет использовать команды на пвп-арене.
    HCORE_BYPASS_SYMBOLS(true, false), // Позволяет использовать запрещённые символы.
    HCORE_BYPASS_FULLSERVER(false, false), // Позволяет игроку заходить на заполненный сервер.
    HCORE_BYPASS_ADVERTISEMENT(false, false), // Позволяет отправлять рекламные ссылки в чат.
    HCORE_BYPASS_COMMANDS_ALL(true, false), // Позволяет отправлять команды, при block.send.commands.enabled: true.
    HCORE_BYPASS_COMMANDS_PLACEHOLDERS(true, false), // Позволяет отправлять плейсхолдеры в чат.
    HCORE_BYPASS_COMMANDS_COLON(true, false), // Позволяет отправлять команды, при block.colon-commands: true.
    HCORE_BYPASS_COMMANDS_FAWE(true, false), // Позволяет отправлять команды, при fix-exploits.fawe-patterns.enabled: true.
    HCORE_BYPASS_COOLDOWN_EGG(false, false), // Позволяет спавнить мобов с помощью яиц без задержки.
    HCORE_BYPASS_COOLDOWN_COMMAND(false, false), // Позволяет отправлять команды без задержки.
    HCORE_BYPASS_COOLDOWN_EMOTES(false, false), // Позволяет использовать эмоции без задержки.
    HCORE_BYPASS_LIMIT_COMMAND(false, false), // Позволяет отправлять команды без лимита.
    HCORE_BYPASS_CUSTOMCOOLDOWN(false, false), // Позволяет использовать команды из файла customcooldown.yml без задержки всегда.
    HCORE_BYPASS_CHATSPAMMING(false, false), // Позволяет спамить в чат.
    HCORE_BYPASS_EVENTS(true, false), // Позволяет обходить выключенные эвенты.
    HCORE_BYPASS_PLAYERDISGUISES(false, false), // Позволяет игрокам превращаться в любых игроков.
    HCORE_BYPASS_KLVOICE(false, false), // Позволяет игроку использовать голосовой чат без KLauncher'a.
    HCORE_BYPASS_ITEMS(true, false), // Позволяет использовать предметы без всяких проверок.
    HCORE_BYPASS_ITEMS_UNBREAKABLE(false, false), // Позволяет использовать нерушимые предметы.
    HCORE_BYPASS_WORLDEDIT(true, false), // Позволяет сетать без лимита.
    HCORE_REJECT_TP(false, false), // Заменяет все телепортации к вам на /tpa и /tpahere.

    HCORE_ALLOW_LOCAL(false, false), // Позволяет писать в локальный чат новичкам.
    HCORE_ALLOW_GLOBAL(false, false), // Позволяет писать в глобальный чат новичкам.
    HCORE_ALLOW_MSG(false, false), // Позволяет писать в лс новичкам.
    HCORE_ALLOW_AI(false, false), // Позволяет использовать AI.
    HCORE_ALLOW_DRILL3X3(false, false), // Позволяет использовать бур.
    HCORE_ALLOW_ENDERSHULKERS(false, false), // Позволяет хранение допустим 3-х шалкеров в эндер-сундуке.
    HCORE_ALLOW_ENDERSHULKERS_ALL(false, false), // Позволяет хранение бесконечного количества шалкеров в эндер-сундуке.
    HCORE_ALLOW_JOINTOPVPARENA(false, false), // Позволяет телепортироваться на пвп-арену.

    HCORE_COMBAT_INDICATORS(false, false), // Если у игрока не будет этого права - индикаторы урона не будут отображаться.

    HCORE_INFORM_AI(false, false), // Позволяет получать уведомление о том, что игрок отправил запрос в AI.
    HCORE_INFORM_DEBUG(false, false), // Позволяет получать сообщения отладки.
    HCORE_INFORM_LOG4J(false, false), // Позволяет получать уведомление о том, что игрок попытался внедрить вредоносный код.
    HCORE_INFORM_REPORT(false, false), // Позволяет получать отправленные игроками репорты.
    HCORE_INFORM_ANTIREDSTONECLOCK(false, false), // Позволяет получать уведомление о том, что игрок спамит редстоуном.

    HCORE_HIDE_ANNOUNCER(false, false), // Позволяет не получать объявления в чате.
    HCORE_CUSTOMCOOLDOWN(false, false), // Позволяет использовать команды с определённой задержкой.
    HCORE_GM3WATCHER(false, false), // Позволяет видеть всех игроков в /gm 3, находясь при этом в любом другом игровом режиме.

    HCORE_COMMAND_死(false, false),
    HCORE_COMMAND_ARROW(false, false),
    HCORE_COMMAND_FINDER(false, false),
    HCORE_COMMAND_CHECKPLAYED(false, false),
    HCORE_COMMAND_BROADCAST(false, false),
    HCORE_COMMAND_FREE(false, false),
    HCORE_COMMAND_CLAIMTOPREWARD(false, false),
    HCORE_COMMAND_HRELOAD(false, false),
    HCORE_COMMAND_PHRELOAD(false, false),
    HCORE_COMMAND_ID(false, false),
    HCORE_COMMAND_DYE(false, false),
    HCORE_COMMAND_CRY(false, false),
    HCORE_COMMAND_CRY_OTHERS(false, false),
    HCORE_COMMAND_CRY_EXEMPT(false, false),
    HCORE_COMMAND_CRY_BYPASS(false, false),
    HCORE_COMMAND_VOMIT(false, false),
    HCORE_COMMAND_JUMP(false, false),
    HCORE_COMMAND_ROLL(false, false),
    HCORE_COMMAND_ITEMSKIN(false, false),
    HCORE_COMMAND_ITEMSKIN_ADMIN(false, false),
    HCORE_COMMAND_BDEBUG(false, false),
    HCORE_COMMAND_PDEBUG(false, false),
    HCORE_COMMAND_SPIT(false, false),
    HCORE_COMMAND_SPIT_EXEMPT(false, false),
    HCORE_COMMAND_SPIT_CRASH(false, false),
    HCORE_COMMAND_SPIT_BYPASS(false, false),
    HCORE_COMMAND_HEAD(false, false),
    HCORE_COMMAND_RCON(true, false),
    HCORE_COMMAND_RAPE(true, false),
    HCORE_COMMAND_RAPE_EXEMPT(true, false),
    HCORE_COMMAND_MASSTEMPEBLAN(false, false),
    HCORE_COMMAND_THROWN(false, false),
    HCORE_COMMAND_STAND(false, false),
    HCORE_COMMAND_STAND_EXECUTE(false, false),
    HCORE_COMMAND_STAND_EXECUTE_PLAYER(true, false),
    HCORE_COMMAND_STAND_EXECUTE_VISUALISE(false, false),
    HCORE_COMMAND_STAND_EXECUTE_EXEMPT(false, false),
    HCORE_COMMAND_PREFIX(false, false),
    HCORE_COMMAND_PREFIX_CHAT(false, false),
    HCORE_COMMAND_PREFIX_TAB(false, false),
    HCORE_COMMAND_PREFIX_BYPASS(false, false),
    HCORE_COMMAND_SETSPAWN(false, false),
    HCORE_COMMAND_SKIN(false, false),
    HCORE_COMMAND_KISS(false, false),
    HCORE_COMMAND_PISS(false, false),
    HCORE_COMMAND_PISS_EXEMPT(false, false),
    HCORE_COMMAND_PISS_CRASH(false, false),
    HCORE_COMMAND_PISS_BYPASS(false, false),
    HCORE_COMMAND_RAGE(true, false),
    HCORE_COMMAND_REPORT(false, false),
    HCORE_COMMAND_REPORT_EXEMPT(false, false),
    HCORE_COMMAND_CHECK(false, false),
    HCORE_COMMAND_STICKER(false, false),
    HCORE_COMMAND_SCHEDULE(true, false),
    HCORE_COMMAND_SPIGET(false, false),
    HCORE_COMMAND_EBLAN(true, false),
    HCORE_COMMAND_WHITELIST(true, false),
    HCORE_COMMAND_UNEBLAN(true, false),
    HCORE_COMMAND_WHERE(false, false),
    HCORE_COMMAND_WHERE_BYPASS(false, false),
    HCORE_COMMAND_WHERE_EXEMPT(false, false),
    HCORE_COMMAND_ALERT(false, false),
    HCORE_COMMAND_SPAWN(false, false),
    HCORE_COMMAND_SPAWN_OTHERS(false, false),
    HCORE_COMMAND_SPAWN_EXEMPT(false, false),
    HCORE_COMMAND_CRASH(false, false),
    HCORE_COMMAND_CRASH_EXEMPT(false, false),
    HCORE_COMMAND_CRASH_BYPASS(false, false),
    HCORE_COMMAND_WINDOW(true, false),
    HCORE_COMMAND_WINDOW_EXEMPT(true, false),

    HIMAGES_COMMAND_IMAGE(false, false),
    HPROTECT_COMMAND_PROTECT(false, false),
    HPROTECT_COMMAND_BYPASS(false, false),
    HBUYER_COMMAND_BUYER(false, false),
    HSEX_COMMAND_SEX(false, false),
    HCONTRACTS_COMMAND_CONTRACT(false, false),
    HMARRY_COMMAND_MARRY(false, false),
    HCRATES_COMMAND_CRATES(true, false),
    HCLANS_COMMAND_CLAN(false, false),
    HKALIAN_COMMAND_SETKALIAN(false, false),

    HCINEMA_COMMAND_PLAY(false, false),
    HCINEMA_COMMAND_PLAY_YOUTUBE(false, false),
    HCINEMA_COMMAND_PLAY_RUTUBE(false, false),
    HCINEMA_COMMAND_PLAY_FILE(false, false),
    HCINEMA_COMMAND_PLAY_TWITCH(false, false),
    HCINEMA_COMMAND_SKIP(false, false),
    HCINEMA_COMMAND_MAKEVIDEOBANNER(false, false),
    HCINEMA_COMMAND_FORWARD(false, false),
    HCINEMA_COMMAND_FORCESKIP(false, false),
    HCINEMA_COMMAND_ROOM(false, false),

    WORLDGUARD_REGION_LIMIT_BLOCK_UNLIMITED(false, false),
    WORLDGUARD_REGION_LIMIT_COUNT_UNLIMITED(false, false);

    @Getter
    private final String value;
    @Getter
    private final boolean strict;
    @Getter
    private final boolean isAdminSkip;

    Permission(boolean strict, boolean isAdminSkip) {
        this.value = this.name().toLowerCase().replace("_", ".");
        this.strict = strict;
        this.isAdminSkip = isAdminSkip;
    }

    public static Permission getEnum(String permission) {
        return Stream.of(values())
                .filter(list -> list.value.equalsIgnoreCase(permission.replace("_", ".")))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Wrong permission! Permission: " + permission));
    }
}