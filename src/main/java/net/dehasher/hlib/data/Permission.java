package net.dehasher.hlib.data;

import lombok.Getter;

import java.util.stream.Stream;

@SuppressWarnings("NonAsciiCharacters")
public enum Permission {
	HLIB_BYPASS_COOLDOWN_COMMAND(false), // Позволяет отправлять команды без задержки.
	HLIB_BYPASS_LIMIT_COMMAND(false), // Позволяет отправлять команды без лимита.

	CMI_COMMAND_FLY(false),
	CMI_COMMAND_WALKSPEED(false),
	CMI_COMMAND_FLYSPEED(false),
	CMI_SEEVANISHED(false),

	HCONTRACTS_HIDE(false), // На игрока с этим правом нельзя взять контракт.
	HCLANS_USE(true), // Позволяет использовать команду /clans.
	HCLANS_ADMIN(false), // Позволяет удалять чужие кланы и перезагружать плагин.
	HCLANS_SPY(false), // Позволяет следить за сообщениями кланов.
	HSEX_BYPASS(false), // Позволяет ебаца без кд и со своим полом. (осуждаю)
	HANTIRELOG_BYPASS(false), // Позволяет обходить антирелог.
	HMARRY_USE(true), // Позволяет использовать команду /marry.
	HMARRY_SPY(false), // Позволяет следить за сообщениями пар.
	HMARRY_ADMIN(false), // Позволяет перезагружать плагин.

	HIMAGES_AVAILABLE(false),
	HIMAGES_SILENT(false),
	HIMAGES_PERMANENTLY(false),
	HIMAGES_WORLDGUARD(false),
	HIMAGES_UNLIMITED(false),
	HIMAGES_SCALE(false),
	HIMAGES_MOD(false),

	HCORE_BYPASS_AI(false), // Позволяет отправлять сообщения к нейросети без задержки и запросы в нейросеть никто не видит.
	HCORE_BYPASS_PVP(false), // Позволяет использовать возможности по типу /fly на пвп-арене.
	HCORE_BYPASS_PVP_COMMANDS(false), // Позволяет использовать команды на пвп-арене.
	HCORE_BYPASS_SYMBOLS(false), // Позволяет использовать запрещённые символы.
	HCORE_BYPASS_FULLSERVER(false), // Позволяет игроку заходить на заполненный сервер.
	HCORE_BYPASS_ADVERTISEMENT(false), // Позволяет отправлять рекламные ссылки в чат.
	HCORE_BYPASS_COMMANDS_ALL(false), // Позволяет отправлять команды, при block.send.commands.enabled: true.
	HCORE_BYPASS_COMMANDS_PLACEHOLDERS(false), // Позволяет отправлять плейсхолдеры в чат.
	HCORE_BYPASS_COMMANDS_COLON(false), // Позволяет отправлять команды, при block.colon-commands: true.
	HCORE_BYPASS_COMMANDS_FAWE(false), // Позволяет отправлять команды, при fix-exploits.fawe-patterns.enabled: true.
	HCORE_BYPASS_COOLDOWN_EGG(false), // Позволяет спавнить мобов с помощью яиц без задержки.
	HCORE_BYPASS_COOLDOWN_EMOTES(false), // Позволяет использовать эмоции без задержки.
	HCORE_BYPASS_CUSTOMCOOLDOWN(false), // Позволяет использовать команды из файла customcooldown.yml без задержки всегда.
	HCORE_BYPASS_CHATSPAMMING(false), // Позволяет спамить в чат.
	HCORE_BYPASS_EVENTS(false), // Позволяет обходить выключенные эвенты.
	HCORE_BYPASS_PLAYERDISGUISES(false), // Позволяет игрокам превращаться в любых игроков.
	HCORE_BYPASS_VOICE(false), // Позволяет игроку использовать голосовой чат без KLauncher'a и WLauncher'a.
	HCORE_BYPASS_ITEMS(false), // Позволяет использовать предметы без всяких проверок.
	HCORE_BYPASS_ITEMS_UNBREAKABLE(false), // Позволяет использовать нерушимые предметы.
	HCORE_BYPASS_WORLDEDIT(false), // Позволяет сетать без лимита.
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
	HCORE_INFORM_ANTIREDSTONECLOCK(false), // Позволяет получать уведомление о том, что игрок спамит редстоуном.

	HCORE_ANNOUNCER(false), // Позволяет получать объявления в чате.
	HCORE_CUSTOMCOOLDOWN(false), // Позволяет использовать команды с определённой задержкой.
	HCORE_GM3WATCHER(false), // Позволяет видеть всех игроков в /gm 3, находясь при этом в любом другом игровом режиме.
	HCORE_CHATGAMES(false), // Позволяет участвовать в чат-играх.

	HCORE_COMMAND_死(false),
	HCORE_COMMAND_ARROW(false),
	HCORE_COMMAND_ANNOUNCER(false),
	HCORE_COMMAND_FINDER(false),
	HCORE_COMMAND_BROADCAST(false),
	HCORE_COMMAND_FREE(false),
	HCORE_COMMAND_CLAIMTOPREWARD(false),
	HCORE_COMMAND_HRELOAD(false),
	HCORE_COMMAND_PHRELOAD(false),
	HCORE_COMMAND_ID(false),
	HCORE_COMMAND_DYE(false),
	HCORE_COMMAND_EMOJI(false),
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
	HCORE_COMMAND_SCALE(false),
	HCORE_COMMAND_SPIT(false),
	HCORE_COMMAND_SPIT_EXEMPT(false),
	HCORE_COMMAND_SPIT_CRASH(false),
	HCORE_COMMAND_SPIT_BYPASS(false),
	HCORE_COMMAND_HEAD(false),
	HCORE_COMMAND_RCON(false),
	HCORE_COMMAND_RAPE(false),
	HCORE_COMMAND_RAPE_EXEMPT(false),
	HCORE_COMMAND_MASSTEMPEBLAN(false),
	HCORE_COMMAND_THROWN(false),
	HCORE_COMMAND_STAND(false),
	HCORE_COMMAND_STAND_EXECUTE(false),
	HCORE_COMMAND_STAND_EXECUTE_PLAYER(false),
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
	HCORE_COMMAND_RAGE(false),
	HCORE_COMMAND_REPORT(false),
	HCORE_COMMAND_REPORT_EXEMPT(false),
	HCORE_COMMAND_CHECK(false),
	HCORE_COMMAND_STICKER(false),
	HCORE_COMMAND_SCHEDULE(false),
	HCORE_COMMAND_EBLAN(false),
	HCORE_COMMAND_WHITELIST(false),
	HCORE_COMMAND_UNEBLAN(false),
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
	HCORE_COMMAND_WINDOW(false),
	HCORE_COMMAND_WINDOW_EXEMPT(false),

	HIMAGES_COMMAND_IMAGE(false),
	HPROTECT_COMMAND_PROTECT(false),
	HPROTECT_COMMAND_PROTECT_USE(false),
	HPROTECT_COMMAND_PROTECT_BYPASS(false),
	HBUYER_COMMAND_BUYER(false),
	HSEX_COMMAND_SEX(false),
	HCONTRACTS_COMMAND_CONTRACT(false),
	HMARRY_COMMAND_MARRY(false),
	HCRATES_COMMAND_CRATES(false),
	HCLANS_COMMAND_CLAN(false),
	HKALIAN_COMMAND_SETKALIAN(false),

	WORLDGUARD_REGION_LIMIT_BLOCK_UNLIMITED(false),
	WORLDGUARD_REGION_LIMIT_COUNT_UNLIMITED(false);

	@Getter
	private final String value;
	@Getter
	private final boolean isAdminSkip;

	Permission(boolean isAdminSkip) {
		this.value = this.name().toLowerCase().replace("_", ".");
		this.isAdminSkip = isAdminSkip;
	}

	public static Permission getEnum(String permission) {
		return Stream.of(values())
				.filter(list -> list.value.equalsIgnoreCase(permission.replace("_", ".")))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Wrong permission! Permission: " + permission));
	}
}