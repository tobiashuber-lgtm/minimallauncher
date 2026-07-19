# Minimal Launcher – Etappe 1

Dies ist das Grundgerüst des minimalistischen Launchers:

- Home-Screen mit Uhrzeit, Datum (klickbar → öffnet Google Kalender), Akku-Balken
  und einer Favoriten-Liste (aktuell fest hinterlegt, Auswahl folgt in Etappe 2)
- Swipe nach links → öffnet den App-Drawer (alphabetische Liste + Suche)
- Swipe nach rechts → Platzhalter-Meldung (echte Notizen-/App-Auswahl folgt später)

Noch NICHT enthalten (kommt in späteren Etappen): Einstellungen, Fonts/Farben/
Größen anpassen, Nutzungszeit-Tracking, App verstecken/umbenennen, Zeitlimit-
Dialog für Social-Media-Apps, Modi-System.

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
