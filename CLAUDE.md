# CLAUDE.md - Auto Restart

## Projekt-Übersicht

**Auto Restart** ist ein NeoForge Minecraft Mod, aktuell mit drei aktiven Branches:
- `master`/`develop_1.21.1` - Minecraft 1.21.1, NeoForge 21.1.x
- `develop_1.21.11` - Minecraft 1.21.11, NeoForge 21.11.x (seit 2026-09-29, siehe [`../Docs/migrations/1.21.10-to-1.21.11.md`](../Docs/migrations/1.21.10-to-1.21.11.md))
- `develop_26.1` - Minecraft 26.1 - 26.3, NeoForge 26.x, Java 25. Ein Jar, kompiliert gegen NeoForge 26.1.0.19-beta, Range `[26.1,26.4)`, siehe [`../Docs/migrations/1.21.11-to-26.1.md`](../Docs/migrations/1.21.11-to-26.1.md)

- **Mod ID**: `auto_restart`
- **Package**: `de.geheimagentnr1.auto_restart`
- **Java Version**: 21 (Branch `develop_26.1`: 25, Gradle 9.2.1)

Fügt automatisches Neustarten zu Minecraft-Servern hinzu.

**Docker-Hinweis**: Dieser Mod verwaltet seinen eigenen Server-Prozess-Neustart und funktioniert nicht zuverlässig in einem Docker-Container (Container-Lifecycle vs. eigene Restart-Logik). Zum Testen native Testpacks verwenden, siehe [`../Docs/testing/docker-vs-native.md`](../Docs/testing/docker-vs-native.md).

**Bekannter offener Punkt (Branch `develop_1.21.11`)**: Der GameTest-Smoke-Test (`elements/gametests/AutoRestartGameTests.java`) wurde ersatzlos entfernt statt migriert, da MC 1.21.11 die alte Annotation-basierte GameTest-API entfernt hat (registry-basiertes System). Eine echte GameTest-Abdeckung für diesen Branch fehlt aktuell (gilt genauso für `develop_26.1`).

**Bekannter Mod-Bug (nicht 26.x-spezifisch, ungefixt)**: `ServerRestarter.saveToFile` meldet "Restart File could not be created", wenn `auto_restart/` bereits existiert (`file.exists() || file.getParentFile().mkdirs() && file.createNewFile()`, `mkdirs()` liefert dann `false`). Das Verzeichnis deshalb in Testpacks nicht vorab anlegen.

**Testpacks 26.x** (nativ, ohne Docker): Server `C:\MinecraftModding\Testpacks\TestPack <ver>`, Client `J:\software\Overwolf\Minecraft\Instances\Testpack <ver>` mit `<ver>` = 26.1, 26.1.1, 26.1.2, 26.2, 26.3. `restart_command` in `world/serverconfig/auto_restart-server.toml` zeigt auf die `start.cmd` des jeweiligen Packs. Laufzeit-Ergebnisse: `docs/superpowers/plans/compat-matrix.md`.

## Abhängigkeiten

Keine Mod-Abhängigkeiten - eigenständiger Mod.

## Projektstruktur

```
src/main/java/de/geheimagentnr1/auto_restart/
├── AutoRestart.java             # Haupt-Mod-Klasse
├── config/
│   ├── AutoRestartTime.java     # Restart-Zeit Konfiguration
│   ├── ServerConfig.java        # Server-Konfiguration
│   ├── TimeUnit.java            # Zeit-Einheiten Enum
│   └── Timing.java              # Timing-Logik
├── elements/
│   └── commands/
│       └── RestartCommand.java  # /restart Command
├── task/
│   └── AutoRestartTask.java     # Scheduled Restart Task
└── util/
    ├── ServerRestarter.java     # Server-Neustart Logik
    ├── StopType.java            # Stop-Typen Enum
    └── TpsHelper.java           # TPS-Berechnung
```

## Besonderheiten

- **Server-Only**: `@Mod( value = MODID, dist = Dist.DEDICATED_SERVER )` — lädt nur auf dedizierten Servern
- **Scheduled Tasks**: Automatische Neustarts zu konfigurierten Zeiten
- **TPS-Monitoring**: Kann TPS überwachen

## Code-Stil

- **Annotations**: `@NotNull` aus `org.jetbrains.annotations`
- **Lombok**: Projekt nutzt Lombok
- **Formatierung**: Leerzeichen nach `(` und vor `)` bei Methodenaufrufen

## Build & Test

```bash
./gradlew build
./gradlew runServer
```

## Deployment

- **CurseForge**: `./gradlew curseforge`
- **Modrinth**: `./gradlew modrinth`

## Testing

### Java-Versionen

Verschiedene Java-Versionen sind unter `C:\Program Files\Eclipse Adoptium` installiert. Für einen Gradle-Build muss die passende Java-Version gewählt werden:

```powershell
# Java 21 für MC 1.20.5+ (NeoForge)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot"
./gradlew build

# Java 25 für MC 26.x (Branch develop_26.1, Gradle 9.2.1)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
./gradlew build
```

### Unit Tests (JUnit 5)

Für reine Logik-Tests ohne Minecraft-Abhängigkeiten:

```bash
./gradlew test
```

Tests liegen unter `src/test/java/`. Ergebnisse: `build/reports/tests/test/index.html`

### NeoForge GameTest Framework

Für Integration Tests in einer echten Minecraft-Umgebung:

```bash
./gradlew runGameTestServer
```

GameTest-Klassen werden mit `@GameTestHolder` annotiert und liegen unter `src/main/java/.../elements/gametests/`.

### CI/CD (GitHub Actions)

Der Workflow `.github/workflows/build-and-test.yml` führt automatisch aus:
1. **Build**: Kompiliert den Mod
2. **Unit Tests**: Führt JUnit Tests aus
3. **GameTests**: Startet GameTestServer (optional)

### Was kann automatisiert getestet werden?

| Aspekt | Automatisiert? | Methode |
|--------|----------------|---------|
| Utility-Klassen | ✅ | JUnit |
| Config-Parsing | ✅ | JUnit |
| Commands | ✅ | GameTest |
| Block/Item-Verhalten | ✅ | GameTest |
| Server-Restart | ⚠️ Eingeschränkt | - |
| Multi-MC-Version | ⚠️ Pro Branch | CI Matrix |

## Referenzen

- [NeoForge Migration Primer](https://docs.neoforged.net/primer/docs/) — Dokumentiert API-Aenderungen zwischen Minecraft/NeoForge-Versionen; nuetzlich fuer die Pruefung von Breaking Changes beim Upgrade auf neue Versionen

---

## Wissensdatenbank

Versionsübergreifende Migrations- und Entwicklungs-Erkenntnisse (Breaking Changes, Fixes, Testumgebungs-Patterns) werden zentral in [`../Docs/`](../Docs/) gepflegt. Bei neuen relevanten Erkenntnissen dort ergänzen, nicht nur hier.
