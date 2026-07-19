# Minimal Launcher – Etappe 1 + 2 + 3

Nahtloses Wisch-Karussell **Notizen ← Home → App-Drawer**, mit vollständigem
Einstellungen-Screen, echten Google-Fonts und Nutzungszeit-Anzeige.

## Home-Screen
- Uhrzeit (klickbar → Uhr-/Wecker-App, 24h/12h umstellbar), Datum (klickbar →
  Google Kalender), Akku-Balken, Dock (an/aus schaltbar, frei mit eigenen
  Apps belegbar)
- Favoriten frei wählbar (1–10 Stück) über Einstellungen → "Favoriten auswählen"
- Kleine Nutzungszeit-Anzeige neben jeder App (heutige Minuten)
- Zahnrad-Symbol oben rechts öffnet die Einstellungen

## App-Drawer (nach links wischen)
- Suche, alphabetische Liste, A-Z Schnellsprung-Leiste
- Ausgeblendete Apps tauchen hier gar nicht auf
- Zeigt ebenfalls die Nutzungszeit pro App

## Notizen (nach rechts wischen)
- Einfaches, lokal gespeichertes Textfeld

## Fonts (echte Dateien, keine System-Fonts)
Alle Fonts kommen direkt von Google Fonts (OFL-Lizenz, Texte liegen im Ordner
`font_licenses/`), je einzeln für App-Liste und Uhr wählbar:
- **Space Mono** / **Space Mono Bold** – technischer "Readout"-Look
- **JetBrains Mono** / **JetBrains Mono Bold** – clean, moderner Mono-Font
- **Space Grotesk** / **Space Grotesk Bold** – moderne Grotesk, guter Allrounder
- **Anton** – ultra-fett, stark kondensiert (nur eine Gewichtung verfügbar)
- **Bebas Neue** – kondensiert, klassischer Poster-Look (nur eine Gewichtung)
- **Archivo Black** – sehr fette Grotesk (nur eine Gewichtung)

## Zeitlimit-Dialog
Bestimmte Apps (in Einstellungen → "Apps mit Zeitlimit auswählen") fragen
beim Öffnen erst "Wirklich öffnen?" mit 5/10/15-Minuten-Auswahl. Nach
Ablauf holt der Launcher automatisch den Home-Screen wieder in den
Vordergrund (technisch kann eine App ohne Root nicht aktiv geschlossen
werden, aber für dich fühlt es sich wie ein automatisches Verlassen an).

## Modi-System
Neben "Standard" lassen sich in Einstellungen → "Modi verwalten" eigene
Modi anlegen (z. B. "Arbeit", "Musik"). Pro Modus konfigurierbar:
- **Gesperrte/ausgeblendete Apps**: diese Apps sind in diesem Modus weder
  im Drawer noch als Favoriten sichtbar, und ein Start (z. B. übers Dock)
  wird mit einer kurzen Meldung blockiert
- **Eigene Favoriten**: der Home-Screen zeigt in diesem Modus nur die für
  ihn gewählten Apps
Gewechselt wird entweder in den Einstellungen oder direkt auf dem
Home-Screen (kleine Modus-Anzeige neben dem Zahnrad erscheint automatisch,
sobald ein anderer Modus als "Standard" aktiv ist – antippen zum Wechseln).

## Einstellungen (Zahnrad auf dem Home-Screen)
- **Darstellung**: Schriftart für App-Liste UND separat für die Uhr (9
  Optionen, siehe oben), Farb-Regler für Hintergrund/Schrift/Akzent
  (0–100: Schwarz → Farbkreis → Weiß, ein Regler pro Farbe), Uhr-Format,
  Statusleiste ausblenden, Schriftgrößen für Uhr/Datum/App-Liste einzeln
  regelbar
- **Home-Screen**: Anzahl Favoriten, Dock an/aus, Favoriten-Auswahl,
  Dock-Belegung frei anpassen (4 Slots, pro Slot eine beliebige installierte
  App zuweisen – zeigt dann deren echtes Icon)
- **Apps**: Apps aus-/einblenden (auch System-Apps), Apps umbenennen
- **Nutzungszeit**: Link zu den Android-Einstellungen für den einmalig
  nötigen Nutzungszugriff

## Bekannte Vereinfachungen
- Vertikale Wischgesten (oben/unten) sind technisch vorbereitet, aber noch
  ohne zugewiesene Aktion.
- Benachrichtigungsfilterung pro Chat (z. B. bei WhatsApp) wird bewusst
  nicht gebaut - dafür gibt es keine stabile, offizielle Schnittstelle,
  die eigene Chat-Stummschaltung in WhatsApp selbst ist der zuverlässigere Weg.
- Der Zeitlimit-Alarm ist ein "inexact" Alarm (Android darf ihn um ein
  paar Minuten verschieben, um Akku zu sparen) - für den gedachten Zweck
  (bewusster Umgang mit Social Media) reicht das aus, für sekundengenaues
  Timing wäre eine Sondergenehmigung nötig.

## So bekommst du deine APK (ohne etwas auf dem PC zu installieren)

1. GitHub-Konto erstellen (falls nötig), neues Repository anlegen (am besten
   **public**, damit die GitHub-Actions-Minuten unbegrenzt sind)
2. Alle Dateien und Ordner aus diesem Projekt hochladen – Ordnerstruktur muss
   erhalten bleiben (inkl. `.github/workflows/build.yml`, `app/src/...`).
   **Am Laptop/PC entpacken und hochladen**, nicht am Handy (dort flachen
   viele Zip-Apps die Ordnerstruktur ab)
3. Reiter "Actions" → warten, bis der Lauf grün ist (ca. 3–6 Minuten)
4. Im fertigen Lauf ganz unten bei "Artifacts" die Datei
   `minimal-launcher-debug` herunterladen (ZIP mit der APK drin)
5. Aufs Handy bringen, entpacken, `.apk` antippen zum Installieren
6. Bei "Installation blockiert": einmalig "Apps aus unbekannten Quellen
   installieren" für die verwendete App (Dateien/Drive/...) erlauben
7. Home-Taste drücken → "Minimal Launcher" als Standard auswählen

## Hinweis zur Sicherheit
Du kannst den kompletten Code hier im Projekt einsehen – es werden keine
Daten irgendwohin gesendet, alles läuft nur lokal auf deinem Gerät.
