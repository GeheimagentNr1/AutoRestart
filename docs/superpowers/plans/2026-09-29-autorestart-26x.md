# AutoRestart 26.1 - 26.3 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** AutoRestart als ein Jar auf Minecraft 26.1 - 26.3 lauffähig machen und dafür fünf Server-Testpacks plus fünf Client-Instanzen bereitstellen.

**Architecture:** Der Mod (ca. 800 Zeilen, Server-only) wird gegen die niedrigste Zielversion (NeoForge `26.1.0.19-beta`) kompiliert. Danach wird gegen alle anderen Zielversionen gegengeprüft. Testpacks entstehen nach der Vorlage `TestPack 1.21.11`, native ohne Docker. Client-Configs stammen aus Client-Instanzen, nie aus Server-Testpacks.

**Tech Stack:** Java 25, Gradle (`net.neoforged.moddev`), NeoForge 26.x, PowerShell, RCON.

**Spec:** `docs/superpowers/specs/2026-09-29-autorestart-26x-design.md`

## Global Constraints

- Branch: `develop_26.1` im Repo `C:\MinecraftModding\ActiveMods\AutoRestart`.
- Kompilierbasis: `neoforge_version=26.1.0.19-beta`, `minecraft_version=26.1`, `minecraft_version_range=[26.1,26.4)`.
- Java 25: `C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot`.
- NeoForge-Regel: neuester Stable-Build, sonst neuester Beta-Build. Matrix: 26.1 -> `26.1.0.19-beta`, 26.1.1 -> `26.1.1.15-beta`, 26.1.2 -> `26.1.2.112`, 26.2 -> `26.2.0.88`, 26.3 -> `26.3.0.36-beta`.
- Testpack-Ordner: `C:\MinecraftModding\Testpacks\TestPack <ver>`. Client: `J:\software\Overwolf\Minecraft\Instances\Testpack <ver>`.
- Kein Docker für AutoRestart. Ports 25565/25575: immer nur ein Server gleichzeitig.
- Client-Configs nicht aus Server-Testpacks kopieren, keine Server-Pfade wie `restart_command` auf den Client.
- Code-Stil: Leerzeichen nach `(` und vor `)`, `@NotNull` aus `org.jetbrains.annotations`, keine Wildcard-Imports.
- Commit-Nachrichten enden mit `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Kein Release, kein Push.

## Review Focus

- `restart_command` in der Server-Config zeigt versehentlich auf den Pfad eines anderen Testpacks (z.B. 1.21.11): Neustart startet die falsche Version.
- Jar läuft auf 26.1.0, aber `NoSuchMethodError`/`NoClassDefFoundError` erst zur Laufzeit auf 26.2/26.3 (Kompilieren allein reicht nicht).
- `neoforge_version_range` bleibt `[21.1,)`, sodass der Mod-Loader die Range nicht wirklich auf 26.x einschränkt.
- Server-Config wird in eine Client-Instanz oder Client-Config (Port 25600, Token) in ein Server-Pack kopiert.
- Alter Server-Prozess belegt Port 25565/25575, das nächste Testpack startet scheinbar, ist aber nicht erreichbar.

---

### Task 1: Build auf 26.1 / Java 25 umstellen und Code kompilierbar machen

**Files:**
- Modify: `gradle.properties` (Zeilen `minecraft_version`, `minecraft_version_range`, `minecraft_versions`, `neoforge_version`, `neoforge_version_range`, `loader_version_range`)
- Modify: `build.gradle:37` (Toolchain), ggf. `build.gradle` Plugin-Zeile `net.neoforged.moddev`
- Modify: bei Bedarf `src/main/java/de/geheimagentnr1/auto_restart/**`
- Test: `src/test/java/de/geheimagentnr1/auto_restart/**` (bestehende JUnit-Tests)

**Interfaces:**
- Produces: `build/libs/AutoRestart-26.1-<mod_version>.jar` (Dateiname laut Build), das die Tasks 2 bis 5 verwenden.

- [ ] **Step 1: Primer lesen und mit genutzten APIs abgleichen**

Lies `https://docs.neoforged.net/primer/docs/26.1/` und suche nach den APIs, die der Mod nutzt: `Commands.hasPermission`, `Commands.LEVEL_OWNERS`, `CommandSourceStack`, `RegisterCommandsEvent`, `ServerTickEvent`, `ServerStoppingEvent`, `ModContainer.registerConfig`, `ModConfigSpec`, `@Mod( dist = ... )`. Notiere jede Änderung in `Docs/migrations/1.21.11-to-26.1.md` (wird in Task 6 fertiggestellt). Prüfe außerdem die Root-`CLAUDE.md`-Tabelle "Bekannte Breaking Changes".

Run (zum Abgleich der genutzten Symbole): `grep -rhn "^import" src/main/java | sort -u`
Expected: Liste aller Imports, die gegen den Primer geprüft werden.

- [ ] **Step 2: `gradle.properties` anpassen**

```properties
minecraft_version=26.1
minecraft_version_range=[26.1,26.4)
minecraft_versions=["26.1","26.1.1","26.1.2","26.2","26.3"]
neoforge_version=26.1.0.19-beta
neoforge_version_range=[26.1,)
```

`loader_version_range` bleibt `[4,)`, außer der Primer nennt eine höhere FML-Version (dann dort anpassen).

- [ ] **Step 3: Java-Toolchain auf 25 setzen**

In `build.gradle` Zeile 37:

```groovy
java.toolchain.languageVersion = JavaLanguageVersion.of(25)
```

Den Kommentar darüber auf "Java 25" anpassen.

- [ ] **Step 4: Build ausführen, Fehler ansehen**

Run (PowerShell):
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
./gradlew build -x test
```
Expected: entweder BUILD SUCCESSFUL oder Compile-Fehler bzw. ein Plugin-Fehler.

Wenn das Plugin `net.neoforged.moddev` mit 26.x nicht funktioniert: neueste 2.x-Version auf `https://plugins.gradle.org/plugin/net.neoforged.moddev` nachschlagen, in `build.gradle` eintragen und wiederholen. Wenn Lombok `1.18.34` mit Java 25 nicht funktioniert: `lombok_version` in `gradle.properties` auf die neueste Version heben.

- [ ] **Step 5: Compile-Fehler einzeln beheben**

Pro Fehler: Symbol im Primer nachschlagen, minimal ändern, Build wiederholen (`./gradlew build -x test`), bis BUILD SUCCESSFUL. Jede Änderung mit Vorher/Nachher in `Docs/migrations/1.21.11-to-26.1.md` festhalten. Es dürfen nur APIs benutzt werden, die es in 26.1.0 gibt (Kompilierbasis).

- [ ] **Step 6: Unit-Tests laufen lassen**

Run: `./gradlew test`
Expected: `TimeUnitTest`, `TimingTest`, `TpsHelperTest` PASS. Bei Fehlschlag Ursache beheben (JUnit-Version prüfen), nicht Tests löschen.

- [ ] **Step 7: Jar-Datei und Inhalt prüfen**

Run: `ls build/libs` und `unzip -p build/libs/*.jar META-INF/neoforge.mods.toml | grep versionRange`
Expected: Jar vorhanden, `versionRange="[26.1,)"` für NeoForge und `"[26.1,26.4)"` für Minecraft.

- [ ] **Step 8: Commit**

```bash
git add gradle.properties build.gradle src docs
git commit -m "Port to Minecraft 26.1, Java 25

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>"
```

---

### Task 2: Kompatibilitätsmatrix kompilieren

**Files:**
- Create: `docs/superpowers/plans/compat-matrix.md` (Ergebnisse)

**Interfaces:**
- Consumes: kompilierender Stand aus Task 1.
- Produces: pro NeoForge-Version Ergebnis `OK`/`FAIL` mit Fehlertext. Das entscheidet, ob `minecraft_version_range=[26.1,26.4)` bleibt.

- [ ] **Step 1: Gegen alle Zielversionen bauen**

Run (PowerShell, Working Tree unverändert lassen, nur per Property überschreiben):
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot"
foreach ($nf in '26.1.0.19-beta','26.1.1.15-beta','26.1.2.112','26.2.0.88','26.3.0.36-beta') {
  "== $nf"
  ./gradlew clean compileJava "-Pneoforge_version=$nf" 2>&1 | Select-Object -Last 15
}
```
Expected: pro Version BUILD SUCCESSFUL oder konkrete Fehler. Falls Gradle wegen `minecraft_version` nicht zur NeoForge-Version passt, zusätzlich `-Pminecraft_version=<mc>` mit der zur Version gehörenden Minecraft-Version setzen (26.1, 26.1.1, 26.1.2, 26.2, 26.3).

- [ ] **Step 2: Ergebnis dokumentieren**

Tabelle in `docs/superpowers/plans/compat-matrix.md` (Version, Ergebnis, Fehlertext). Bei FAIL für 26.2 oder 26.3: Ursache im Primer nachschlagen. Kann sie ohne API-Bruch gegenüber 26.1 gefixt werden (gleicher Code für beide)? Dann fixen. Sonst `minecraft_version_range` auf die letzte kompatible Version begrenzen und den Split in der Doku vermerken.

- [ ] **Step 3: Kompilierbasis wiederherstellen und Commit**

Run: `./gradlew clean build -x test` (ohne `-P`-Overrides)
Expected: BUILD SUCCESSFUL mit `26.1.0.19-beta`.

```bash
git add docs gradle.properties
git commit -m "Document compile compatibility matrix for 26.1 - 26.3

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>"
```

---

### Task 3: Server-Testpacks anlegen

**Files:**
- Create: `C:\MinecraftModding\Testpacks\TestPack 26.1`, `26.1.1`, `26.1.2`, `26.2`, `26.3` (je `mods\`, `libraries\`, `start.cmd`, `start_debug.cmd`, `run.bat`, `run.sh`, `user_jvm_args.txt`, `eula.txt`, `server.properties`, `world\serverconfig\auto_restart-server.toml`)

**Interfaces:**
- Consumes: Jar aus Task 1, Vorlage `C:\MinecraftModding\Testpacks\TestPack 1.21.11`.
- Produces: pro Pack ein per `start.cmd` startbarer Server. `restart_command` verweist auf `start.cmd` des eigenen Packs.

- [ ] **Step 1: Verzeichnisse und NeoForge-Installer**

Run (PowerShell; `$java` ist Java 25):
```powershell
$java = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot\bin\java.exe"
$src  = "C:\MinecraftModding\Testpacks\TestPack 1.21.11"
$matrix = @{ '26.1'='26.1.0.19-beta'; '26.1.1'='26.1.1.15-beta'; '26.1.2'='26.1.2.112'; '26.2'='26.2.0.88'; '26.3'='26.3.0.36-beta' }
foreach ($mc in $matrix.Keys) {
  $nf  = $matrix[$mc]
  $dst = "C:\MinecraftModding\Testpacks\TestPack $mc"
  New-Item -ItemType Directory -Force "$dst\mods" | Out-Null
  $inst = "$dst\neoforge-$nf-installer.jar"
  Invoke-WebRequest "https://maven.neoforged.net/releases/net/neoforged/neoforge/$nf/neoforge-$nf-installer.jar" -OutFile $inst
  Push-Location $dst
  & $java -jar $inst --installServer
  Pop-Location
  Remove-Item $inst -ErrorAction SilentlyContinue
}
```
Expected: pro Pack existiert `libraries\net\neoforged\neoforge\<nf>\win_args.txt`. Fehler bei einem Pack (z.B. Java-Version) beheben und nur dieses Pack wiederholen.

- [ ] **Step 2: Basisdateien aus der Vorlage kopieren**

```powershell
foreach ($mc in $matrix.Keys) {
  $dst = "C:\MinecraftModding\Testpacks\TestPack $mc"
  Copy-Item "$src\eula.txt","$src\server.properties","$src\user_jvm_args.txt" $dst -Force
  New-Item -ItemType Directory -Force "$dst\world\serverconfig","$dst\auto_restart" | Out-Null
}
```
Expected: `eula.txt` (`eula=true`), RCON-Werte aus der Vorlage vorhanden. Kein `world\`-Inhalt und keine Mod-Configs aus der Vorlage übernehmen.

- [ ] **Step 3: Start-Skripte schreiben**

```powershell
foreach ($mc in $matrix.Keys) {
  $nf  = $matrix[$mc]
  $dst = "C:\MinecraftModding\Testpacks\TestPack $mc"
  $javaBin = 'PATH="C:\Program Files\Eclipse Adoptium\jdk-25.0.4.7-hotspot\bin";%PATH%'
  $args = "libraries/net/neoforged/neoforge/$nf/win_args.txt"
  Set-Content "$dst\start.cmd" -Encoding ascii -Value @(
    $javaBin,
    "cd `"$dst`"",
    "start java @user_jvm_args.txt @$args")
  Set-Content "$dst\start_debug.cmd" -Encoding ascii -Value @(
    $javaBin,
    "cd `"$dst`"",
    "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 @user_jvm_args.txt @$args",
    "pause")
}
```
Expected: `start.cmd` je Pack mit dem Pfad zum eigenen Pack. `run.bat`/`run.sh` liefert der Installer bereits, dort prüfen, dass `win_args.txt`/`unix_args.txt` auf die richtige Version zeigen.

- [ ] **Step 4: AutoRestart-Server-Config schreiben**

Inhalt wie `world\serverconfig\auto_restart-server.toml` der Vorlage, aber `restart_command` auf das eigene Pack:

```powershell
$tpl = Get-Content "$src\world\serverconfig\auto_restart-server.toml" -Raw
foreach ($mc in $matrix.Keys) {
  $dst = "C:\MinecraftModding\Testpacks\TestPack $mc"
  $cfg = $tpl -replace 'restart_command = ".*"', ('restart_command = "' + ("$dst\start.cmd").Replace('\','\\') + '"')
  Set-Content "$dst\world\serverconfig\auto_restart-server.toml" -Value $cfg -Encoding utf8
}
Select-String 'restart_command' "C:\MinecraftModding\Testpacks\TestPack *\world\serverconfig\auto_restart-server.toml"
```
Expected: fünf Treffer, jeder Pfad enthält die eigene Versionsnummer (Review-Focus: keine `1.21.11` in einem 26.x-Pack).

- [ ] **Step 5: Jar in die Packs kopieren**

```powershell
$jar = Get-ChildItem C:\MinecraftModding\ActiveMods\AutoRestart\build\libs\*.jar | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
foreach ($mc in $matrix.Keys) { Copy-Item $jar.FullName "C:\MinecraftModding\Testpacks\TestPack $mc\mods\" -Force }
Get-ChildItem "C:\MinecraftModding\Testpacks\TestPack 26.*\mods" | Select-Object FullName
```
Expected: in jedem `mods\` genau ein AutoRestart-Jar. Ob es einen `mcpfabric`-Build für 26.x auf Modrinth gibt, wird hier geprüft (`https://modrinth.com/mod/mcpfabric/versions`); wenn ja, mit `config\mcpfabric.config.json` (Server-Port 25599) ergänzen, sonst als Lücke in der Doku (Task 6) vermerken.

- [ ] **Step 6: Commit entfällt**

Testpacks liegen außerhalb der Git-Repos. Stattdessen Ergebnis in Task 5/6 dokumentieren.

---

### Task 4: Client-Instanzen befüllen

**Files:**
- Create/Modify: `J:\software\Overwolf\Minecraft\Instances\Testpack 26.1\mods\`, dito `26.1.1`, `26.1.2`, `26.2`, `26.3` sowie je `config\`

**Interfaces:**
- Consumes: Jar aus Task 1, Client-Config-Vorlage `J:\software\Overwolf\Minecraft\Instances\Testpack 1.21.11\config`.

- [ ] **Step 1: `mods\` prüfen/anlegen und Jar kopieren**

```powershell
$inst = "J:\software\Overwolf\Minecraft\Instances"
foreach ($mc in '26.1','26.1.1','26.1.2','26.2','26.3') {
  New-Item -ItemType Directory -Force "$inst\Testpack $mc\mods" | Out-Null
  Copy-Item $jar.FullName "$inst\Testpack $mc\mods\" -Force
}
```
Expected: pro Instanz das Jar in `mods\`.

- [ ] **Step 2: Client-Configs übertragen (nur Client-Vorlage)**

Übertragen wird ausschließlich aus `Testpack 1.21.11` (Client), nur diese Dateien: `fml.toml`, `neoforge-client.toml`, `neoforge-common.toml`, `neoforge-server.toml`.

```powershell
$tplCfg = "$inst\Testpack 1.21.11\config"
foreach ($mc in '26.1','26.1.1','26.1.2','26.2','26.3') {
  $d = "$inst\Testpack $mc\config"
  New-Item -ItemType Directory -Force $d | Out-Null
  foreach ($f in 'fml.toml','neoforge-client.toml','neoforge-common.toml','neoforge-server.toml') {
    Copy-Item "$tplCfg\$f" $d -Force
  }
}
```
Expected: pro Instanz vier Dateien. Nicht kopiert werden: `auto_restart*.toml` (existiert auf dem Client nicht), Mod-Configs anderer Mods, `*.bak`, sowie die Server-Datei `TestPack ...\world\serverconfig\*`. Ist eine Datei in der Vorlage nicht vorhanden oder von NeoForge 26.x in einem anderen Format, diese weglassen; NeoForge erzeugt sie beim ersten Start.

- [ ] **Step 3: `mcpfabric.config.json` (nur wenn Task 3 einen 26.x-Build gefunden hat)**

Client-Datei aus `Testpack 1.21.11\config\mcpfabric.config.json` kopieren, Port `25600` beibehalten, nicht die Server-Datei (Port 25599) verwenden.

- [ ] **Step 4: Prüfen**

Run: `Get-ChildItem "$inst\Testpack 26.*" -Recurse -File | Select-Object FullName`
Expected: pro Instanz `mods\<jar>` und die Config-Dateien, keine `auto_restart*.toml`.

---

### Task 5: Verifikation pro Testpack

**Files:**
- Read: `C:\MinecraftModding\Testpacks\TestPack <ver>\logs\latest.log`

**Interfaces:**
- Consumes: Testpacks aus Task 3 (jeweils nur ein Server gleichzeitig).

- [ ] **Step 1: Sicherstellen, dass Ports frei sind**

Run: `Get-NetTCPConnection -LocalPort 25565,25575 -ErrorAction SilentlyContinue | Select-Object LocalPort,OwningProcess`
Expected: keine Ausgabe. Sonst prüfen, was den Port belegt. Nur selbst gestartete Server beenden.

- [ ] **Step 2: Server starten und Log prüfen (Beispiel 26.1)**

Run: `Start-Process "C:\MinecraftModding\Testpacks\TestPack 26.1\start.cmd"`, danach warten, bis im Log `Done (` steht:
```powershell
Select-String -Path "C:\MinecraftModding\Testpacks\TestPack 26.1\logs\latest.log" -Pattern 'auto_restart','Done \(','ERROR'
```
Expected: `auto_restart` wird geladen, `Done (...)` vorhanden, keine ERROR-Zeilen von `auto_restart`.

- [ ] **Step 3: Neustart per RCON auslösen**

Mit `mcp__rcon-testpack__rcon` den Befehl `restart` senden (der Server muss vorher per `check_server_status` erreichbar sein). Danach im Log prüfen, dass der Server herunterfährt und `start.cmd` einen neuen Prozess startet (`Done (` erscheint erneut).
Expected: zweiter Start im gleichen Pack, Log zeigt erneut `auto_restart`.

- [ ] **Step 4: Server stoppen**

Per RCON `stop`. Erst danach das nächste Testpack starten (Step 1 wiederholen).

- [ ] **Step 5: Schritte 1 bis 4 für 26.1.1, 26.1.2, 26.2, 26.3 wiederholen**

Ergebnis pro Version (OK/FAIL, Fehlertext) in `docs/superpowers/plans/compat-matrix.md` ergänzen. Bei FAIL auf 26.2/26.3 wie in Task 2 Step 2 vorgehen (fixen oder Range begrenzen), danach Task 2 Step 3 und Task 3 Step 5 für das neu gebaute Jar wiederholen.

- [ ] **Step 6: Commit**

```bash
git add docs
git commit -m "Record runtime verification results for 26.1 - 26.3

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>"
```

---

### Task 6: Doku aktualisieren

**Files:**
- Modify: `AutoRestart/CLAUDE.md`
- Modify: `C:\MinecraftModding\ActiveMods\CLAUDE.md` (Breaking-Changes-Tabelle, Range-Tabelle)
- Create: `C:\MinecraftModding\ActiveMods\Docs\migrations\1.21.11-to-26.1.md`
- Modify: `C:\MinecraftModding\ActiveMods\Docs\testing\docker-vs-native.md` (Tabelle "Mods müssen an zwei Stellen liegen")

- [ ] **Step 1: Migrationsnotiz schreiben**

`Docs/migrations/1.21.11-to-26.1.md` nach dem Muster von `1.21.10-to-1.21.11.md`: pro Änderung Abschnitt mit Vorher/Nachher-Code (aus Task 1 Step 5), Java 25, Plugin-/Lombok-Änderungen, Ergebnis der Kompatibilitätsmatrix, Hinweis zur Beta-Regel für NeoForge.

- [ ] **Step 2: Testpack-Tabelle erweitern**

In `docker-vs-native.md` fünf Zeilen ergänzen (Server `C:\MinecraftModding\Testpacks\TestPack <ver>\mods\`, Client `J:\software\Overwolf\Minecraft\Instances\Testpack <ver>\mods\`). Zusätzlich einen Absatz: Client-Configs unterscheiden sich von Server-Configs, nicht kopieren.

- [ ] **Step 3: Root-`CLAUDE.md` und `AutoRestart/CLAUDE.md` anpassen**

Root: neue Zeilen in der Breaking-Changes-Tabelle (mit Link auf die Migrationsnotiz), AutoRestart-Range in der Range-Tabelle. `AutoRestart/CLAUDE.md`: Branch `develop_26.1` in die Branch-Liste, Java 25 in den Testing-Hinweisen, Testpack-Verweis.

- [ ] **Step 4: Commit (nur AutoRestart-Repo)**

```bash
cd C:/MinecraftModding/ActiveMods/AutoRestart
git add CLAUDE.md docs
git commit -m "Update docs for 26.1 - 26.3 port

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>"
```

`Docs/` und Root-`CLAUDE.md` liegen außerhalb des AutoRestart-Repos und werden nur geändert, nicht committet.

---

## Self-Review

- **Spec-Abdeckung:** Versionsmatrix (Global Constraints), Code-Port (Task 1), Gegenkompilierung (Task 2), Server-Testpacks (Task 3), Client-Instanzen samt Config-Regel (Task 4), Verifikation (Task 5), Doku (Task 6). Keine Lücke.
- **Platzhalter:** keine. Die Compile-Fehler aus Task 1 Step 5 sind erst im Lauf bekannt. Das Vorgehen ist beschrieben, nicht der Fix.
- **Konsistenz:** `$matrix`, `$src`, `$jar`, `$inst` werden in Task 3 und 4 in derselben PowerShell-Sitzung benötigt. Bei neuer Sitzung die Definitionen aus Task 3 Step 1/5 und Task 4 Step 1 wiederholen.
- **Review Focus:** Falscher `restart_command` (Task 3 Step 4), Laufzeitfehler statt Compile-Fehler (Task 5), `neoforge_version_range` (Task 1 Step 2/7), Config-Vermischung (Task 4 Step 2), belegte Ports (Task 5 Step 1) sind jeweils in einem Task abgedeckt.
