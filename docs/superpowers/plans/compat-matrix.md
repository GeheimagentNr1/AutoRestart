# Kompatibilitätsmatrix AutoRestart 26.1 - 26.3

Kompilierbasis: NeoForge 26.1.0.19-beta, Java 25, Gradle 9.2.1.

| Minecraft | NeoForge | Compile (`compileJava`) | Runtime (Testpack) |
|---|---|---|---|
| 26.1 | 26.1.0.19-beta | OK | OK (Start + `/restart` per RCON) |
| 26.1.1 | 26.1.1.15-beta | OK | OK (Start + `/restart` per RCON) |
| 26.1.2 | 26.1.2.112 | OK | OK (Start + `/restart` per RCON) |
| 26.2 | 26.2.0.88 | OK | OK (Start + `/restart` per RCON) |
| 26.3 | 26.3.0.36-beta | OK | OK (Start + `/restart` per RCON) |

Runtime-Test: pro Testpack nativ mit `start.cmd` (Java 25) gestartet, `auto_restart` im Log geladen, Config gelesen, `/restart` per RCON ausgelöst, `restart_command` startete einen neuen Prozess (neues `latest.log`, erneutes `Done`). Getestet mit Jar `AutoRestart-26.1-3.0.1.jar`.

Nebenbefund (nicht 26.x-spezifisch): `ServerRestarter.saveToFile` meldet "Restart File could not be created", wenn `auto_restart/` bereits existiert (`exists() || mkdirs() && createNewFile()`). Wird nur ausgelöst, wenn das Verzeichnis leer vorab angelegt wurde.
