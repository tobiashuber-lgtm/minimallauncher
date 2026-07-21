# GHOSTS Launcher – Etappe 1 + 2 + 3

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

## Update: Feinschliff nach dem ersten kompletten Test
- **Alle/Keine auswählen** bei App-Sperren, Sichtbarkeit, Zeitlimit und
  Benachrichtigungs-Stummschaltung
- **App-Drawer A-Z-Index**: breitere Reaktionsfläche, große Buchstaben-
  Vorschau beim Ziehen, Liste filtert live auf den berührten Buchstaben
- **Kalender**: langes Drücken aufs Datum öffnet direkt "Neuer Termin"
  (kurzes Antippen bleibt: Kalender-App öffnen)
- **App-Start-Animation**: zentriertes Einblenden (Scale + Fade) statt
  Wischen von rechts
- **Modus-Anzeige**: jetzt immer sichtbar (auch im Standard-Modus) und
  direkt antippbar zum Wechseln, unabhängig vom Zahnrad/Einstellungen
- **Modi**: pro Modus jetzt zusätzlich einstellbar - eigene Uhr-Akzentfarbe,
  Dock an/aus, Statusleiste an/aus, Benachrichtigungen bestimmter Apps
  stummschalten (siehe Hinweis unten)
- **Vertikale Wischgesten** (oben/unten) frei zuweisbar: Taschenlampe,
  Notizen öffnen, App-Drawer öffnen, oder eine bestimmte App
- **Dock**: Slots können jetzt auch komplett leer/ausgeblendet sein;
  Standard-Icons sind jetzt eigene, saubere Mono-Style-Vektor-Icons statt
  der alten Android-System-Symbole
- **Alle Dialoge** (Zeitlimit, Modus erstellen/löschen, Umbenennen, ...)
  jetzt im einheitlichen Schwarz/Weiß-Mono-Look

### Wichtiger technischer Hinweis: Benachrichtigungen "ausschalten"
Eine App kann eine andere App nicht wirklich stummschalten - das entscheidet
technisch immer nur Android bzw. der Nutzer selbst. Was hier stattdessen
gebaut wurde: Ein Hintergrunddienst (Benachrichtigungszugriff nötig, einmalig
in Einstellungen zu aktivieren) entfernt eingehende Benachrichtigungen
gewählter Apps sofort wieder automatisch, solange ein bestimmter Modus aktiv
ist. Für dich fühlt sich das wie "aus" an, technisch ist es "sofort entfernt
statt verhindert" - ein kurzes Aufblitzen ist möglich.

## Update: Feinschliff Runde 2
- **Standard-Modus umbenennbar** (Einstellungen → Modi verwalten) - intern
  bleibt er "Standard", nur die Anzeige ändert sich überall
- **A-Z-Index**: deutlich breitere Reaktionsfläche, große Vorschau jetzt in
  Space Mono Bold, kein Kasten mehr im Hintergrund (schlichter)
- **A-Z-Index Loslassen**: bleibt jetzt an der Position des zuletzt
  berührten Buchstabens, statt zu A ganz oben zurückzuspringen
- **Uhr-Klick**: robuster gemacht (mehr Hersteller-Fallbacks + richtige
  Reihenfolge der Versuche)
- **Dock**: eigenes Icon pro Slot wählbar (10 Mono-Icons + "App-eigenes
  Icon"), unabhängig von der zugewiesenen App
- **App-Start-Animation**: nutzt jetzt zusätzlich die neuere Android-14-API
  für Übergänge - falls dein Gerät trotzdem von rechts wischt, ist das
  wahrscheinlich eine Hersteller-Einstellung (z.B. Samsung/Xiaomi-Skin),
  die eigene, launcher-übergreifende Übergangsanimationen erzwingt und sich
  von einer einzelnen App aus nicht immer überschreiben lässt

## Update: Feinschliff Runde 3
- **A-Z-Index Loslassen**: Buchstabe bleibt jetzt exakt oben angeheftet
  (wie in der gefilterten Ansicht) - bei Buchstaben nahe dem Ende (z.B. W)
  kann dadurch Leerraum darunter entstehen, das ist gewollt. Normales
  Scrollen danach bleibt ganz regulär auf den Listenanfang/-ende begrenzt.
- **Geist-Icon**: hat jetzt Augen
- **Vertikale Wischgesten**: Fix für zuverlässigere Erkennung (Android-
  GestureDetector-Stolperstein behoben). Wichtig: dafür muss in
  Einstellungen → Gesten erst eine Aktion (nicht "Keine") zugewiesen sein
- **Zeitlimit-Countdown**: schwebende Anzeige oben am Bildschirmrand
  (Space Mono Bold), zählt die gewählten Minuten runter. Braucht einmalig
  die "Andere Apps überlagern"-Berechtigung (Einstellungen → "Countdown-
  Anzeige beim Zeitlimit" antippen zum Aktivieren) - ohne diese Berechtigung
  funktioniert das Zeitlimit trotzdem (kein Countdown sichtbar, aber der
  Launcher holt sich nach Ablauf trotzdem automatisch in den Vordergrund)

## Update: Feinschliff Runde 4
- **Zeitlimit-Sessions**: Wird dieselbe App innerhalb des gewählten
  Zeitfensters erneut geöffnet, läuft der Timer einfach weiter (kein neuer
  Dialog). Wird eine andere App gestartet, wird das alte Zeitlimit verworfen
  (keine überlappenden Timer). Nach Ablauf wird jetzt zusätzlich versucht,
  die App wirklich zu beenden (`killBackgroundProcesses`, funktioniert
  zuverlässig für Apps, die dann im Hintergrund sind - nicht alle
  Geräte/Apps lassen sich so beenden, aber der Home-Screen erscheint in
  jedem Fall)
- **Countdown-Overlay**: verschwindet jetzt automatisch, sobald du zum
  Home-Screen zurückkehrst (der Timer im Hintergrund läuft trotzdem weiter)
- **Vertikale Wischgesten entfernt** - haben nicht zuverlässig genug
  funktioniert, sind jetzt komplett raus (auch aus den Einstellungen)
- **GHOSTS Launcher-Schriftzug** unten in den Einstellungen ist jetzt ein
  Link zu instagram.com/ghostsmakemusic (Optik bleibt unverändert)

## Update: Großes Feinschliff-Paket
- **Sprache (Deutsch/Englisch)**: wirkt auf Datum/Wochentag und den
  Haupt-Einstellungsbildschirm. Tiefere Verwaltungs-Screens (Favoriten
  auswählen, Apps umbenennen, Modi verwalten, ...) bleiben vorerst auf
  Deutsch - eine komplette Übersetzung aller Screens wäre ein deutlich
  größeres Folge-Update.
- **Farb-Regler**: zeigen jetzt eine kleine Live-Vorschau der Farbe direkt
  neben dem Wert (Hintergrund/Schrift/Akzent, auch pro Modus)
- **Datum-Klick/Halten**: frei einstellbar (Kalender öffnen / Neuer Termin
  / Nichts), sowohl fürs Antippen als auch fürs Halten separat
- **Standard-Modus**: jetzt auch "bearbeitbar" (Favoriten-Zugang), andere
  Modi jetzt umbenennbar (alle zugehörigen Daten wandern automatisch mit)
- **Fonts reduziert** auf die gewünschten 4: Space Mono, Space Mono Bold,
  Space Grotesk, Space Grotesk Bold - beide Familien SIL Open Font License
  1.1, frei weitergebbar (auch kommerziell), Lizenztexte in
  `font_licenses/`
- **Favoriten-Reihenfolge**: in "Favoriten auswählen" → "Reihenfolge
  anpassen" antippen, Zeilen per Ziehen sortieren, oder "A-Z sortieren"
  für automatische alphabetische Sortierung
- **App-Drawer, großer Umbau**:
  - Buchstaben-Leiste beginnt jetzt bei ca. 1/3 der Bildschirmhöhe (oberes
    Drittel reagiert nicht), zusätzlicher `#`-Eintrag für Apps ohne
    Anfangsbuchstaben (Zahlen/Symbole)
  - Kurzes Vibrations-Feedback beim Wechsel auf einen neuen Buchstaben
  - Graue Buchstaben-Überschriften vor jeder Gruppe (wie im Kontakte-Buch)
  - Scroll-Verhalten beim Loslassen: Buchstabe wird oben angeheftet,
    **außer** man ist schon nah am Ende der Liste - dann bleibt die
    Ansicht einfach stehen statt hochzuspringen (Annäherung an das
    Kontakte-App-Verhalten per Bildschirm-Zeilen-Schätzung, kein
    pixelgenauer Nachbau)
- **Notizen**: jetzt zeilenbasiert mit einfacher Formatierung - drei
  kleine Knöpfe oben (Fett / Bullet-Punkt · / Checkliste ☑), wirken auf
  die aktuell fokussierte Zeile. "+ Zeile" fügt eine neue Zeile hinzu.

## Update: Feinschliff Runde 5
- **Datum-Klick/Halten** wieder fest wie ursprünglich (Google Kalender /
  Neuer Termin) - die Einstellbarkeit dafür wurde wieder entfernt
- **Vollständige Übersetzung**: Sprache wirkt jetzt auf ALLE Screens
  (Modi verwalten/bearbeiten, Favoriten, Sichtbarkeit, Umbenennen, Dock,
  Zeitlimit, Benachrichtigungen) - nicht mehr nur den Hauptbildschirm
- **App-Drawer, komplett überarbeitetes Buchstaben-Verhalten**:
  - `#`-Bereich am unteren Ende ist jetzt doppelt so groß/reagiert
    entsprechend früher - leichter mit dem Daumen zu treffen
  - Kein "gefiltertes Einzelansicht" mehr: die normale App-Liste mit
    Buchstaben-Überschriften bewegt sich beim Ziehen einfach mit,
    aktuelle Gruppe bleibt weiß, alles andere wird grau abgedimmt
  - Buchstabe ohne Apps: springt automatisch zur nächstgelegenen Gruppe
    mit Apps (zuerst vorwärts, sonst rückwärts gesucht)
  - Loslassen: keine weitere Bewegung mehr - Liste bleibt exakt dort
    stehen, wo das Ziehen sie hingebracht hat
- **Notizen, großer Umbau**:
  - Automatischer Zeilenumbruch beim Tippen (kein manuelles "+Zeile" mehr)
  - Enter erzeugt eine neue Zeile darunter - bei Bullet-Punkt oder
    Checkliste bleibt der Typ erhalten (wie bei einer einfachen
    Google-Docs-Liste)
  - Titel "Notepad" in Space Mono Bold
  - Mülleimer-Symbol oben rechts löscht die komplette Notiz (mit
    Bestätigungsabfrage Ja/Nein)

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
