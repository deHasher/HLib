package net.dehasher.hlib.data;

import lombok.Getter;

import java.util.stream.Stream;

@SuppressWarnings("NonAsciiCharacters")
public enum Permission {
    CMI_COMMAND_FLY(false),
    CMI_COMMAND_WALKSPEED(false),
    CMI_COMMAND_FLYSPEED(false),
    CMI_SEEVANISHED(false),

    HCONTRACTS_HIDE(false), // На игрока с этим правом нельзя взять контракт.
    HCLANS_ADMIN(true), // Позволяет удалять чужие кланы и перезагружать плагин.
    HCLANS_SPY(false), // Позволяет следить за сообщениями кланов.
    HSEX_BYPASS(false), // Позволяет ебаца без кд и со своим полом. (осуждаю)
    HANTIRELOG_BYPASS(false), // Позволяет обходить антирелог.
    HMARRY_SPY(false), // Позволяет следить за сообщениями пар.
    HMARRY_ADMIN(true), // Позволяет перезагружать плагин.

    HIMAGES_AVAILABLE(true),
    HIMAGES_WORLDGUARD(true),
    HIMAGES_UNLIMITED(true),
    HIMAGES_SCALE(true),
    HIMAGES_MOD(false),
    HCINEMA_BYPASS(true),
    HCINEMA_ALLOW_JOINTOTHEATER(false),

    HCORE_BYPASS_AI(false), // Позволяет отправлять сообщения к нейросети без задержки и запросы в нейросеть никто не видит.
    HCORE_BYPASS_PVP(true), // Позволяет использовать возможности по типу /fly на пвп-арене.
    HCORE_BYPASS_PVP_COMMANDS(false), // Позволяет использовать команды на пвп-арене.
    HCORE_BYPASS_SYMBOLS(true), // Позволяет использовать запрещённые символы.
    HCORE_BYPASS_FULLSERVER(false), // Позволяет игроку заходить на заполненный сервер.
    HCORE_BYPASS_ADVERTISEMENT(false), // Позволяет отправлять рекламные ссылки в чат.
    HCORE_BYPASS_COMMANDS_ALL(true), // Позволяет отправлять команды, при block.send.commands.enabled: true.
    HCORE_BYPASS_COMMANDS_PLACEHOLDERS(true), // Позволяет отправлять плейсхолдеры в чат.
    HCORE_BYPASS_COMMANDS_COLON(true), // Позволяет отправлять команды, при block.colon-commands: true.
    HCORE_BYPASS_COMMANDS_FAWE(true), // Позволяет отправлять команды, при fix-exploits.fawe-patterns.enabled: true.
    HCORE_BYPASS_COOLDOWN_EGG(false), // Позволяет спавнить мобов с помощью яиц без задержки.
    HCORE_BYPASS_COOLDOWN_COMMAND(false), // Позволяет отправлять команды без задержки.
    HCORE_BYPASS_COOLDOWN_EMOTES(false), // Позволяет использовать эмоции без задержки.
    HCORE_BYPASS_LIMIT_COMMAND(false), // Позволяет отправлять команды без лимита.
    HCORE_BYPASS_CUSTOMCOOLDOWN(false), // Позволяет использовать команды из файла customcooldown.yml без задержки всегда.
    HCORE_BYPASS_CHATSPAMMING(false), // Позволяет спамить в чат.
    HCORE_BYPASS_EVENTS(true), // Позволяет обходить выключенные эвенты.
    HCORE_BYPASS_PLAYERDISGUISES(false), // Позволяет игрокам превращаться в любых игроков.
    HCORE_BYPASS_KLVOICE(false), // Позволяет игроку использовать голосовой чат без KLauncher'a.
    HCORE_BYPASS_ITEMS(true), // Позволяет использовать предметы без всяких проверок.
    HCORE_BYPASS_ITEMS_UNBREAKABLE(false), // Позволяет использовать нерушимые предметы.
    HCORE_BYPASS_WORLDEDIT(true), // Позволяет сетать без лимита.
    HCORE_REJECT_TP(false), // Заменяет все телепортации к вам на /tpa и /tpahere.

    HCORE_ALLOW_LOCAL(false), // Позволяет писать в локальный чат новичкам.
    HCORE_ALLOW_GLOBAL(false), // Позволяет писать в глобальный чат новичкам.
    HCORE_ALLOW_MSG(false), // Позволяет писать в лс новичкам.
    HCORE_ALLOW_AI(false), // Позволяет использовать AI.
    HCORE_ALLOW_DRILL3X3(false), // Позволяет использовать бур.
    HCORE_ALLOW_ENDERSHULKERS(false), // Позволяет хранение допустим 3-х шалкеров в эндер-сундуке.
    HCORE_ALLOW_ENDERSHULKERS_ALL(false), // Позволяет хранение бесконечного количества шалкеров в эндер-сундуке.
    HCORE_ALLOW_JOINTOPVPARENA(false), // Позволяет телепортироваться на пвп-арену.

    HCORE_COMBAT_INDICATORS(false), // Если у игрока не будет этого права - индикаторы урона не будут отображаться.

    HCORE_INFORM_AI(false), // Позволяет получать уведомление о том, что игрок отправил запрос в AI.
    HCORE_INFORM_DEBUG(false), // Позволяет получать сообщения отладки.
    HCORE_INFORM_LOG4J(false), // Позволяет получать уведомление о том, что игрок попытался внедрить вредоносный код.
    HCORE_INFORM_REPORT(false), // Позволяет получать отправленные игроками репорты.
    HCORE_INFORM_ANTIREDSTONECLOCK(false), // Позволяет получать уведомление о том, что игрок спамит редстоуном.

    HCORE_HIDE_ANNOUNCER(false), // Позволяет не получать объявления в чате.
    HCORE_CUSTOMCOOLDOWN(false), // Позволяет использовать команды с определённой задержкой.
    HCORE_GM3WATCHER(false), // Позволяет видеть всех игроков в /gm 3, находясь при этом в любом другом игровом режиме.

    HCORE_COMMAND_死(false),
    HCORE_COMMAND_ARROW(false),
    HCORE_COMMAND_FINDER(false),
    HCORE_COMMAND_CHECKPLAYED(false),
    HCORE_COMMAND_BROADCAST(false),
    HCORE_COMMAND_FREE(false),
    HCORE_COMMAND_CLAIMTOPREWARD(false),
    HCORE_COMMAND_HRELOAD(false),
    HCORE_COMMAND_PHRELOAD(false),
    HCORE_COMMAND_ID(false),
    HCORE_COMMAND_DYE(false),
    HCORE_COMMAND_CRY(false),
    HCORE_COMMAND_CRY_OTHERS(false),
    HCORE_COMMAND_CRY_EXEMPT(false),
    HCORE_COMMAND_CRY_BYPASS(false),
    HCORE_COMMAND_VOMIT(false),
    HCORE_COMMAND_JUMP(false),
    HCORE_COMMAND_ROLL(false),
    HCORE_COMMAND_ITEMSKIN(false),
    HCORE_COMMAND_ITEMSKIN_ADMIN(false),
    HCORE_COMMAND_BDEBUG(false),
    HCORE_COMMAND_PDEBUG(false),
    HCORE_COMMAND_SPIT(false),
    HCORE_COMMAND_SPIT_EXEMPT(false),
    HCORE_COMMAND_SPIT_CRASH(false),
    HCORE_COMMAND_SPIT_BYPASS(false),
    HCORE_COMMAND_HEAD(false),
    HCORE_COMMAND_RCON(true),
    HCORE_COMMAND_RAPE(true),
    HCORE_COMMAND_RAPE_EXEMPT(true),
    HCORE_COMMAND_MASSTEMPEBLAN(false),
    HCORE_COMMAND_THROWN(false),
    HCORE_COMMAND_STAND(false),
    HCORE_COMMAND_STAND_EXECUTE(false),
    HCORE_COMMAND_STAND_EXECUTE_PLAYER(true),
    HCORE_COMMAND_STAND_EXECUTE_VISUALISE(false),
    HCORE_COMMAND_STAND_EXECUTE_EXEMPT(false),
    HCORE_COMMAND_PREFIX(false),
    HCORE_COMMAND_PREFIX_CHAT(false),
    HCORE_COMMAND_PREFIX_TAB(false),
    HCORE_COMMAND_PREFIX_BYPASS(false),
    HCORE_COMMAND_SETSPAWN(false),
    HCORE_COMMAND_SKIN(false),
    HCORE_COMMAND_KISS(false),
    HCORE_COMMAND_PISS(false),
    HCORE_COMMAND_PISS_EXEMPT(false),
    HCORE_COMMAND_PISS_CRASH(false),
    HCORE_COMMAND_PISS_BYPASS(false),
    HCORE_COMMAND_RAGE(true),
    HCORE_COMMAND_REPORT(false),
    HCORE_COMMAND_REPORT_EXEMPT(false),
    HCORE_COMMAND_CHECK(false),
    HCORE_COMMAND_STICKER(false),
    HCORE_COMMAND_SCHEDULE(true),
    HCORE_COMMAND_SPIGET(false),
    HCORE_COMMAND_EBLAN(true),
    HCORE_COMMAND_WHITELIST(true),
    HCORE_COMMAND_UNEBLAN(true),
    HCORE_COMMAND_WHERE(false),
    HCORE_COMMAND_WHERE_BYPASS(false),
    HCORE_COMMAND_WHERE_EXEMPT(false),
    HCORE_COMMAND_ALERT(false),
    HCORE_COMMAND_SPAWN(false),
    HCORE_COMMAND_SPAWN_OTHERS(false),
    HCORE_COMMAND_SPAWN_EXEMPT(false),
    HCORE_COMMAND_CRASH(false),
    HCORE_COMMAND_CRASH_EXEMPT(false),
    HCORE_COMMAND_CRASH_BYPASS(false),
    HCORE_COMMAND_WINDOW(true),
    HCORE_COMMAND_WINDOW_EXEMPT(true),

    HIMAGES_COMMAND_IMAGE(false),
    HPROTECT_COMMAND_PROTECT(false),
    HPROTECT_COMMAND_BYPASS(false),
    HBUYER_COMMAND_BUYER(false),
    HSEX_COMMAND_SEX(false),
    HCONTRACTS_COMMAND_CONTRACT(false),
    HMARRY_COMMAND_MARRY(false),
    HCRATES_COMMAND_CRATES(true),
    HCLANS_COMMAND_CLAN(false),
    HKALIAN_COMMAND_SETKALIAN(false),

    HCINEMA_COMMAND_PLAY(false),
    HCINEMA_COMMAND_PLAY_YOUTUBE(false),
    HCINEMA_COMMAND_PLAY_RUTUBE(false),
    HCINEMA_COMMAND_PLAY_FILE(false),
    HCINEMA_COMMAND_PLAY_TWITCH(false),
    HCINEMA_COMMAND_SKIP(false),
    HCINEMA_COMMAND_MAKEVIDEOBANNER(false),
    HCINEMA_COMMAND_FORWARD(false),
    HCINEMA_COMMAND_FORCESKIP(false),
    HCINEMA_COMMAND_ROOM(false),

    WORLDGUARD_REGION_LIMIT_BLOCK_UNLIMITED(false),
    WORLDGUARD_REGION_LIMIT_COUNT_UNLIMITED(false);

    @Getter
    public final String value;
    @Getter
    private final boolean strict;

    Permission(boolean strict) {
        this.value = this.name().replace("_", ".");
        this.strict = strict;
    }

    public static Permission getEnum(String permission) {
        return Stream.of(values())
                .filter(list -> list.value.equalsIgnoreCase(permission.replace("_", ".")))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Wrong permission! Permission: " + permission));
    }
}