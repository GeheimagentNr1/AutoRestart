# CLAUDE.md - Auto Restart

Auto Restart startet einen Minecraft-Server automatisch neu: zu festen Uhrzeiten, bei leerem Server, bei zu niedrigen TPS, nach einem Absturz oder per Befehl `/restart`. Mod-ID `auto_restart`, Package `de.geheimagentnr1.auto_restart`. **Server-only**: NeoForge per `@Mod( value = MODID, dist = Dist.DEDICATED_SERVER )`, Forge 1.20+ per `DistExecutor.safeRunWhenOn( Dist.DEDICATED_SERVER, .. )` in `initMod()`, ältere Forge-Branches per `DISPLAYTEST`/`IGNORESERVERONLY`. Im Client und im Singleplayer lädt der Mod, tut aber nichts.

Diese Datei ist auf allen `develop_*`-Branches identisch; Quelle: `../Docs/mod-claude/AutoRestart.md` (Änderungen dort, dann `Docs/tools/sync_mod_claude_md.sh`). Versionsstand: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Versionen

- Forge: `develop_1.14.4` .. `develop_1.21`, `develop_1.21.1_forge` (letzter Forge-Stand 1.21.1).
- NeoForge: drei Jars - `develop_1.21.1` (1.21.1 - 1.21.10), `develop_1.21.11` (nur 1.21.11, `hasPermission(int)` entfernt) und `develop_26.1` (ein Jar 26.1 - 26.3, keine Java-Änderungen gegenüber 1.21.11).
- Ranges, Tags und Java-Versionen pro Branch: [`../Docs/VERSIONEN.md`](../Docs/VERSIONEN.md).

## Abhängigkeiten

- Keine Mod-Abhängigkeiten.
- Forge 1.20 - 1.21.1-Forge: MinecraftForgeAPI per `jarJar` gebündelt (`AbstractMod`, `registerConfig`, `registerEventHandler`), siehe [`../Docs/forge/README.md`](../Docs/forge/README.md).

## Architektur

- `AutoRestart` - Mod-Klasse; registriert Server-Config, `ServerRestarter`, `RestartCommand` und `AutoRestartTask` (NeoForge direkt im Konstruktor auf `NeoForge.EVENT_BUS`; Forge 1.20+ über `AbstractMod` und `ModCommandsRegisterFactory`).
- `config/ServerConfig` - `ModConfigSpec` mit allen Optionen, parst Zeiten lazy (`ensureLoaded()`); `AutoRestartTime` (HH:MM), `Timing` + `TimeUnit` (`5s`, `7m`, `2h`).
- `task/AutoRestartTask` - `TimerTask`, startet 60 s nach Mod-Konstruktion und prüft jede Sekunde: Leer-Server-Verzögerung, Low-TPS (Server und jede Dimension über `TpsHelper`), Restart-Zeiten mit Warnungen im Chat. Der Server wird erst über `ServerStartedEvent` gesetzt.
- `util/ServerRestarter` - schreibt den Status nach `./auto_restart/restart` (`-1` = Absturz/läuft, `0` = normaler Stopp, `1` = Neustart; `StopType.ordinal() - 1`), stoppt per `server.halt( false )` und startet in `ServerStoppedEvent` ggf. `restart_command` per `ProcessBuilder`.
- `script/restart_loop.sh`, `restart_start.sh`, `restart.cmd` im Repo: Beispiel-Startskripte für den externen Neustart (liest `auto_restart/restart`, `0` beendet die Schleife).

## Funktionen, Befehle, Konfiguration

- `/restart` - Rechtestufe 4 (`hasPermission( 4 )` bis 1.21.10, ab 1.21.11 `Commands.LEVEL_OWNERS`).
- Config `world/serverconfig/auto_restart-server.toml` (Server-Config, pro Welt):
  - `[restart]` `use_external_restart_script` (Server läuft in einer Skript-Schleife, Mod startet selbst nichts) bzw. `restart_command` (nur der Pfad zum Startskript, ohne Argumente).
  - `[auto_restart]` `enabled`, `times` (24-h-Format), `on_crash`, `warning_times`.
  - `[on_empty_restart]` `enabled`, `delay`; `[low_tps_restart]` `enabled`, `minium_tps_level` (sic), `delay`.
- Alle Trigger sind standardmäßig aus; ist `times` leer, setzt der Mod `auto_restart.enabled` intern auf `false`.

## Fallstricke

- **Timer darf nicht sterben**: `AutoRestartTask.run()` fängt `RuntimeException`, wartet auf den gestarteten Server und prüft nicht mehr, sobald ein Neustart läuft oder der Server stoppt (Config ist dann entladen). Gefixt in 3.0.2 bzw. Forge `1.20.1-2.0.3` (vorher: Timer tot vor Serverstart, doppelte Neustarts).
- **Statusdatei `auto_restart/restart`**: bis einschließlich 3.0.2/2.0.3 wurde sie nicht angelegt, wenn der Ordner schon existierte (`mkdirs()` liefert dann `false`). Seit 2026-10-13 auf allen Branches gefixt (`isDirectory() || mkdirs()`), noch nicht released.
- **Kein Docker**: der Mod startet den eigenen Server-Prozess neu, das kollidiert mit dem Container-Lifecycle - nur nativ testen ([`../Docs/testing/docker-vs-native.md`](../Docs/testing/docker-vs-native.md)).
- GameTest-Smoke-Test `elements/gametests/AutoRestartGameTests` nur auf `develop_1.21.1`; ab 1.21.11 ersatzlos gestrichen (Annotation-GameTests entfernt); Unit-Tests für `Timing`, `TimeUnit`, `TpsHelper` laufen auf allen NeoForge-Branches.
- Rechte-Umbau 1.21.11: [`../Docs/migrations/1.21.10-to-1.21.11.md`](../Docs/migrations/1.21.10-to-1.21.11.md); Tooling 26.1 (Gradle 9.2.1, Lombok 1.18.48): [`../Docs/migrations/1.21.11-to-26.1.md`](../Docs/migrations/1.21.11-to-26.1.md).

## Testen

- `restart_command` muss pro Testpack auf dessen eigene `start.cmd` zeigen; leer = `/restart` stoppt nur. In älteren Packs stehen noch Testwerte (u. a. Leer-Server-Neustart nach 2 Minuten) - automatisierte Tests ohne Spieler kurz halten oder vorher per RCON stoppen. `pack.sh` schaltet den Leerlauf-Neustart in Forge-Packs ab.
- Prüfen: `/restart` per RCON → Server stoppt und startet über `start.cmd` neu; geplante Zeit → Warnungen im Chat, genau **ein** Neustart; `auto_restart/restart` enthält danach den erwarteten Wert. Low-TPS ist praktisch nicht manuell testbar.
- `../Docs/testing/tools/neoforge_smoke_all.sh` prüft an den Jar-Grenzen einen geplanten Neustart (muss genau einmal passieren).
- Allgemeine Regeln: [`../Docs/testing/README.md`](../Docs/testing/README.md).

## Links

- Wiki: https://github.com/GeheimagentNr1/AutoRestart/wiki (Seitennamen mit `:`, Hinweise in [`../Docs/releasing/project-pages.md`](../Docs/releasing/project-pages.md))
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/auto-restart (Projekt-ID 389711)
- Modrinth: https://modrinth.com/mod/auto-restart (Projekt-ID `N93mhFhT`)
- [`../Docs/migrations/BREAKING_CHANGES.md`](../Docs/migrations/BREAKING_CHANGES.md), [`../Docs/releasing/release-process.md`](../Docs/releasing/release-process.md)
