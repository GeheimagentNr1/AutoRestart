# AGENTS.md - Auto Restart

## Projekt-Übersicht

**Auto Restart** ist ein NeoForge Minecraft Mod für Minecraft 1.21.1.
- **Mod ID**: `auto_restart`
- **Package**: `de.geheimagentnr1.auto_restart`
- **Java Version**: 21
- **NeoForge Version**: 21.1.x

Fügt automatisches Neustarten zu Minecraft-Servern hinzu.

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

- **Server-Only**: `usableOnClientSide=false` - nur für dedizierte Server
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
