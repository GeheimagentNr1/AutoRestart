# AutoRestart: Migration auf Minecraft 26.1 - 26.3 (Design)

Branch: `develop_26.1` (existiert bereits). Kein Release in diesem Scope.

## Ziel

Ein einziges AutoRestart-Jar, das auf Minecraft 26.1 bis 26.3 mit NeoForge läuft, plus fünf Server-Testpacks und die fünf zugehörigen CurseForge/Overwolf-Client-Instanzen zum Testen. Wenn ein Jar für alle Versionen nicht möglich ist, wird der Split dokumentiert (Range zurückziehen, ggf. weitere Branches).

## Versionsmatrix

Regel: immer der neueste Stable-Build, gibt es keinen, der neueste Beta-Build (Stand 2026-09-29).

| Testpack | NeoForge | Art |
|---|---|---|
| 26.1 | 26.1.0.19-beta | Beta |
| 26.1.1 | 26.1.1.15-beta | Beta |
| 26.1.2 | 26.1.2.112 | Stable |
| 26.2 | 26.2.0.88 | Stable |
| 26.3 | 26.3.0.36-beta | Beta |

Die Liste wird vor dem Anlegen erneut gegen `https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge` geprüft.

## 1. Code-Port

- **Kompilierbasis ist die niedrigste Version** (`26.1.0.19-beta`, `minecraft_version=26.1`), damit nur APIs genutzt werden, die in allen Zielversionen existieren. Gegen 26.1.2 zu kompilieren würde nur 26.1.2 bis 26.3 garantieren.
- `minecraft_version_range=[26.1,26.4)`, `minecraft_versions` als JSON-Array aller Zielversionen.
- Java: Toolchain in `build.gradle` auf 25 (`jdk-25.0.4.7-hotspot`). Ob 26.1.0 schon Java 25 braucht, wird beim ersten Build geprüft, Ergebnis fließt in die Start-Skripte.
- Plugin `net.neoforged.moddev` (aktuell `2.0.+`) auf Kompatibilität mit 26.x prüfen.
- Vorgehen: NeoForge-Primer für 26.1 gegen die tatsächlich genutzten APIs abgleichen (Commands/Permissions, Server-Stop, Tick-Events, Config, `@Mod`), Compile-Fehler iterativ beheben. Root-`CLAUDE.md` und `Docs/migrations/` zuerst prüfen.
- Kompatibilitätsprüfung nach oben: zusätzlich mit `-Pneoforge_version=…` gegen 26.1.1, 26.1.2, 26.2, 26.3 kompilieren, danach dasselbe Jar in allen Testpacks starten.
- GameTests bleiben wie in 1.21.11 entfernt (offener Punkt). JUnit-Tests (`TimeUnitTest`, `TimingTest`, `TpsHelperTest`) müssen weiter laufen.

## 2. Server-Testpacks

Pro Version `C:\MinecraftModding\Testpacks\TestPack <ver>` nach Vorlage `TestPack 1.21.11`:

- Nativ ohne Docker (AutoRestart startet seinen Prozess selbst neu, siehe `Docs/testing/docker-vs-native.md`).
- NeoForge-Installation, `eula.txt`, `server.properties` (RCON wie in der Vorlage), `user_jvm_args.txt`.
- `start.cmd`, `start_debug.cmd`, `run.bat`, `run.sh`: `PATH` auf Java 25, `libraries/net/neoforged/neoforge/<nf-version>/win_args.txt`, `cd` auf den jeweiligen Testpack-Ordner.
- Server-Config `world/serverconfig/auto_restart-server.toml`: Werte wie in der Vorlage, aber `restart_command` zeigt auf `start.cmd` des jeweiligen Packs.
- `mods/`: AutoRestart-Jar. `mcpfabric` nur, wenn es einen passenden 26.x-Build gibt; dann eigene `config/mcpfabric.config.json` mit Server-Port 25599.
- Da alle Packs denselben Port nutzen (25565/25575), läuft immer nur ein Server gleichzeitig.

## 3. Client-Instanzen (CurseForge/Overwolf)

Die Instanzen `Testpack 26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3` unter `J:\software\Overwolf\Minecraft\Instances\` existieren nur als Hülle (`mods\`, `minecraftinstance.json`).

- `mods\` anlegen (falls nötig) und dasselbe Jar hineinkopieren.
- **Client-Configs sind nicht die Server-Configs.** Sie liegen in `<Instanz>\config\` und enthalten zusätzlich `*-client.toml` und `*-server.toml` (Singleplayer), `mcpfabric.config.json` mit Client-Port 25600 und eigenem Token. AutoRestart hat auf dem Client keine Config (Server-only-Mod). Deshalb: Configs aus einer Client-Instanz (Vorlage `Testpack 1.21.11`) übertragen, nicht aus dem Server-Testpack, und keine Server-Pfade wie `restart_command` mitnehmen.
- Übertragen wird nur, was für 26.x noch gültig ist (`fml.toml`, `neoforge-*.toml`, ggf. `mcpfabric.config.json` mit Client-Port). Mod-Configs von Mods ohne 26.x-Build bleiben weg.

## 4. Verifikation

Pro Testpack: Server nativ starten, im Log prüfen, dass `auto_restart` lädt, per RCON `/restart` auslösen und kontrollieren, dass `restart_command` den Server neu startet. Nur selbst gestartete Server werden wieder gestoppt.

## 5. Doku

- `AutoRestart/CLAUDE.md`: Branch-Liste, Versionsrange, Java 25, Testpack-Tabelle.
- Root-`CLAUDE.md`: Breaking-Changes-Tabelle und Range-Tabelle erweitern.
- Neu: `Docs/migrations/1.21.11-to-26.1.md`.
- `Docs/testing/docker-vs-native.md`: Tabelle um die fünf Versionen (Server- und Client-Pfade) ergänzen.

## Offene Risiken

- Für 26.1.0, 26.1.1 und 26.3 gibt es nur Beta-Builds, sie können sich noch ändern.
- 26.2/26.3 können APIs ändern; dann greift der dokumentierte Range-Split.
- Es ist offen, ob es `mcpfabric`-Builds für 26.x gibt.
