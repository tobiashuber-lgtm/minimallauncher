# Minimal Launcher – Etappe 1 + 2 + 3

Nahtloses Wisch-Karussell **Notizen ← Home → App-Drawer**, jetzt mit einem
vollständigen Einstellungen-Screen und echter Nutzungszeit-Anzeige.

## Home-Screen
- Uhrzeit (klickbar → Uhr-/Wecker-App, 24h/12h umstellbar), Datum (klickbar →
  Google Kalender), Akku-Balken, Dock (an/aus schaltbar)
- Favoriten frei wählbar (1–10 Stück) über Einstellungen → "Favoriten auswählen"
- Kleine Nutzungszeit-Anzeige neben jeder App (heutige Minuten)
- Zahnrad-Symbol oben rechts öffnet die Einstellungen

## App-Drawer (nach links wischen)
- Suche, alphabetische Liste, A-Z Schnellsprung-Leiste
- Ausgeblendete Apps tauchen hier gar nicht auf
- Zeigt ebenfalls die Nutzungszeit pro App

## Notizen (nach rechts wischen)
- Einfaches, lokal gespeichertes Textfeld

## Einstellungen (Zahnrad auf dem Home-Screen)
- **Darstellung**: Schriftart (4 Systemschriften), Farbschema (Dunkel/Hell/
  Akzent-Rot), Uhr-Format, Statusleiste ausblenden, Schriftgrößen für Uhr/
  Datum/App-Liste einzeln regelbar
- **Home-Screen**: Anzahl Favoriten, Dock an/aus, Favoriten-Auswahl
- **Apps**: Apps aus-/einblenden (auch System-Apps), Apps umbenennen
- **Nutzungszeit**: Link zu den Android-Einstellungen für den einmalig
  nötigen Nutzungszugriff (Android verlangt das aus Datenschutzgründen –
  ein Hinweis dazu erscheint auch direkt auf dem Home-Screen, falls die
  Berechtigung noch fehlt)

## Bekannte Vereinfachungen
- Die 4 "Fonts" sind Android-System-Schriftfamilien (Mono/Kondensiert/
  Grotesk/Serif), keine echten Custom-Fonts wie bei Nothing – dafür
  bräuchte es eigene .ttf-Dateien im Projekt.
- Dock-Icons sind aktuell fest (Telefon/Kamera/Nachrichten/Mail), noch
  nicht einzeln zuweisbar.
- Vertikale Wischgesten (oben/unten) sind technisch vorbereitet, aber noch
  ohne zugewiesene Aktion.
- Modi-System (z.B. "Musik"-Modus, "Arbeit"-Modus) und der Zeitlimit-Dialog
  für Social-Media-Apps kommen in einer der nächsten Etappen.

## So bekommst du deine APK

1. GitHub-Konto erstellen (falls nötig), Repository anlegen
2. Alle Dateien und Ordner aus diesem Projekt hochladen (Ordnerstruktur muss
   erhalten bleiben, inkl. `.github/workflows/build.yml`)
3. Reiter "Actions" → warten, bis der Lauf grün ist (ca. 3–6 Minuten)
4. Im fertigen Lauf ganz unten bei "Artifacts" die Datei
   `minimal-launcher-debug` herunterladen (ZIP mit der APK drin)
5. Aufs Handy bringen, entpacken, `.apk` antippen zum Installieren
6. Bei "Installation blockiert": einmalig "Apps aus unbekannten Quellen
   installieren" für die verwendete App (Dateien/Drive/...) erlauben
7. Home-Taste drücken → "Minimal Launcher" als Standard auswählen

## So bekommst du deine APK (ohne irgendetwas zu installieren)

1. Gehe auf [github.com](https://github.com) und erstelle ein kostenloses Konto,
   falls du noch keins hast.
2. Erstelle ein neues Repository (z. B. `minimal-launcher`), am besten **public**,
   damit die kostenlosen GitHub-Actions-Minuten unbegrenzt sind.
3. Lade **alle Dateien und Ordner aus diesem Projekt** in das Repository hoch
   (per Drag & Drop im Browser über "Add file" → "Upload files" – achte darauf,
   dass die Ordnerstruktur erhalten bleibt, also auch `.github/workflows/build.yml`
   und `app/src/...`).
4. Gehe im Repository auf den Reiter **"Actions"**. Nach dem Hochladen sollte
   automatisch ein Workflow-Lauf namens "Build APK" starten (dauert ca. 3–6 Minuten).
5. Ist der Lauf grün/fertig, klicke ihn an und scrolle runter zu **"Artifacts"**.
   Dort liegt eine Datei namens `minimal-launcher-debug` zum Herunterladen –
   das ist eine ZIP-Datei, die die fertige `.apk` enthält.
6. Lade die ZIP auf dein Handy (z. B. über eine Cloud oder USB), entpacke sie
   und tippe auf die `.apk`-Datei zum Installieren.
7. Android fragt evtl. nach Erlaubnis für "Installation aus unbekannten Quellen" –
   das ist normal bei selbstgebauten Apps.
8. Nach der Installation: Home-Taste drücken → Android fragt, welcher Launcher
   verwendet werden soll → "Minimal Launcher" auswählen.

## Hinweis zur Sicherheit
Du kannst den kompletten Code hier im Projekt einsehen – es werden keine
Daten irgendwohin gesendet, alles läuft nur lokal auf deinem Gerät.
