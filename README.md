# Minimal Launcher – Etappe 1

Der minimalistische Launcher mit einem echten, nahtlosen Wisch-Karussell:
**Notizen ← Home → App-Drawer**, alle drei Seiten hängen an einem Band und
lassen sich 1:1 mit dem Finger hin- und herziehen (kein hartes Umschalten
mehr wie in der allerersten Version).

- **Home**: Uhrzeit (klickbar → öffnet die Uhr-/Wecker-App), Datum (klickbar →
  Google Kalender, mit Web-Fallback), Akku-Balken, Favoriten-Liste (aktuell
  fest hinterlegt, freie Auswahl folgt in Etappe 2), Dock unten mit
  Telefon/Kamera/Nachrichten/Mail (per System-Icons, echte Funktion)
- **App-Drawer** (nach links wischen): Suche, alphabetische Liste, A-Z
  Schnellsprung-Leiste am rechten Rand zum Antippen/Ziehen
- **Notizen** (nach rechts wischen): einfaches, lokal gespeichertes Textfeld
- Zurück-Taste des Handys bringt immer zum Home-Screen zurück
- Die obere System-Statusleiste (Uhr/WLAN/Akku von Android selbst) ist
  standardmäßig ausgeblendet für ein cleaneres Bild

Noch NICHT enthalten (kommt in späteren Etappen): Einstellungen-Screen,
Fonts/Farben/Größen anpassen, Nutzungszeit-Tracking, App verstecken/
umbenennen, Zeitlimit-Dialog für Social-Media-Apps, Modi-System, freie
Zuordnung der Dock-Icons und der vertikalen Wischgesten (oben/unten).

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
