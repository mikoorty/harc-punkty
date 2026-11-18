# ⚜️ HarcPunkty

> **Nowoczesny, w pełni konfigurowalny system punktacji całorocznej zastępów dla drużyn i szczepów harcerskich.**  
> Web App (PWA) + dedykowana aplikacja na Androida (APK) z automatycznym updaterem, grami terenowymi na kodach QR i synchronizacją czasu rzeczywistego (Firebase).

[![GitHub Pages](https://github.com/mikoorty/harc-punkty/actions/workflows/pages.yml/badge.svg)](https://github.com/mikoorty/harc-punkty/actions/workflows/pages.yml)
[![Android APK](https://github.com/mikoorty/harc-punkty/actions/workflows/apk.yml/badge.svg)](https://github.com/mikoorty/harc-punkty/actions/workflows/apk.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![Impeccable Design](https://img.shields.io/badge/UI-Impeccable%20MDI-1e4a30)](https://pictogrammers.com/library/mdi/)

---

## 🌟 Główne możliwości

- 🏆 **Punktacja kwartalna i całoroczna**:
  - Gładkie wykresy monotoniczne Fritsch-Carlsona pokazujące dynamikę punktów narastająco tydzień po tygodniu.
  - Automatyczne wyliczanie miejsc na podium, sumy punktów i historii.
  - Wsparcie dla **dowolnej liczby zastępów** (2, 4, 6, 8, 12...) z unikalnymi kolorami.
- ⛺ **Zbiórki zastępów ze streakiem**:
  - Zgłaszanie zbiórek przez Zastępowych (obecność, umundurowanie, program).
  - Bonusy punktowe za systematyczność zbiórek tydzień po tygodniu (streak).
  - Akceptacja lub odrzucanie raportów przez Kadrę z informacją zwrotną.
- 🚨 **Alarmy mundurowe i powiadomienia**:
  - Ogłaszanie alarmów ze stoperem.
  - Błyskawiczne meldunki harcerzy ze zdjęciem (aparat/galeria) z punktacją według progów czasowych.
- 🎯 **Gry terenowe i kody QR**:
  - Generator kodów QR dla punktów kontrolnych i zadań (z opcjonalnym linkiem do formularza/instrukcji).
  - Skaner kodów QR wbudowany bezpośrednio w przeglądarkę i aplikację Android.
  - **Powiadomienia w czasie rzeczywistym**: Kadra słyszy sygnał dźwiękowy i widzi powiadomienie toast w momencie, gdy harcerz odnajdzie i zeskanuje kod w lesie!
  - Drukowanie kart punktów kontrolnych i eksport do PNG.
- 🎖️ **Sprawności harcerskie**:
  - Składanie wniosków o sprawności (1, 2, 3 gwiazdki) indywidualnie lub dla całego zastępu.
- 👥 **Zarządzanie kontami i bezpieczeństwo**:
  - Trzy poziomy ról: Harcerz (szeregowy), Zastępowy, Kadra / Administrator.
  - Generator kont, import grupowy z listy, eksport pakietów haseł do wydruku dla zastępów.
  - Pierwsze uruchomienie: automatyczna bezpieczna inicjalizacja pierwszego konta administratora.
- 📱 **Aplikacja Android (APK)**:
  - Pełnoekranowy WebView shell z obsługą aparatu, plików i drukowania.
  - Automatyczny in-app updater powiadamiający o nowym wydaniu APK na GitHubie.

---

## 🚀 Szybki start (Fork & Deploy w 5 minut)

Chcesz uruchomić system dla swojej drużyny? To banalnie proste:

### Krok 1: Sforkuj repozytorium
Kliknij przycisk **Fork** w prawym górnym rogu tego repozytorium, aby utworzyć własną kopię na swoim koncie GitHub.

### Krok 2: Włącz GitHub Pages
1. W swoim sforkowanym repozytorium wejdź w **Settings** → **Pages**.
2. W sekcji **Build and deployment** zmień **Source** na: **GitHub Actions**.
3. Twoja strona zostanie opublikowana pod adresem: `https://<twoj-login>.github.io/<nazwa-repozytorium>/`.

### Krok 3: Podłącz darmową bazę Firebase
Aplikacja działa w oparciu o darmowy plan Firebase (Authentication + Firestore):
1. Wejdź na [Firebase Console](https://console.firebase.google.com/) i utwórz darmowy projekt.
2. Włącz **Authentication** (metoda logowania: **Email/Password**).
3. Utwórz bazę **Firestore Database** w trybie produkcyjnym.
4. Wklej zawartość pliku [`firestore.rules`](./firestore.rules) w zakładce **Firestore → Rules** i kliknij **Publish**.
5. W ustawieniach projektu (**Project Settings → General → Your apps**) kliknij ikonę przeglądarki `</>` (Web app) i skopiuj obiekt konfiguracyjny `firebaseConfig`.

👉 **Szczegółowy przewodnik krok po kroku ze zrzutami ekranu znajdziesz w pliku [FIREBASE_SETUP.md](./FIREBASE_SETUP.md).**

### Krok 4: Skonfiguruj swoją drużynę w `config.js`
Otwórz plik [`config.js`](./config.js) bezpośrednio na GitHubie i wpisz swoje dane:

```javascript
export const CONFIG = {
  unit: {
    name: "1. Warszawska Drużyna Harcerzy „Czarne Wilki”",
    shortName: "Czarne Wilki",
    appTitle: "HarcPunkty",
    subtitle: "Punktacja całoroczna zastępów",
    icon: "pine-tree", // pine-tree, campfire, compass, flag, shield, tent
    themeColor: "#1e4a30"
  },

  patrols: [
    { name: "Wilki", color: "#e06c28" },
    { name: "Kruki", color: "#2aa198" },
    { name: "Niedźwiedzie", color: "#268bd2" },
    { name: "Borsuki", color: "#859900" }
  ],

  firebase: {
    apiKey: "AIzaSy...",
    authDomain: "twoj-projekt.firebaseapp.com",
    projectId: "twoj-projekt",
    storageBucket: "twoj-projekt.firebasestorage.app",
    messagingSenderId: "123456789",
    appId: "1:123456789:web:abcdef"
  }
};
```
Po zapisaniu zmian (`Commit changes`) GitHub Actions automatycznie zaktualizuje Twoją stronę!

---

## 📱 Automatyczne budowanie APK na Androida

Projekt zawiera gotowy workflow GitHub Actions ([`.github/workflows/apk.yml`](./.github/workflows/apk.yml)), który kompiluje natywną aplikację Android dla Twojego forka:

1. W swoim repozytorium przejdź do zakładki **Actions** → **Aplikacja Android (APK)**.
2. Kliknij **Run workflow** (lub po prostu edytuj cokolwiek w folderze `android/`).
3. Po zakończeniu budowania w zakładce **Releases** Twojego repozytorium pojawi się gotowy do pobrania plik `HarcPunkty.apk`.
4. Aplikacja automatycznie łączy się z Twoją stroną na GitHub Pages i oferuje auto-aktualizacje harcerzom!

---

## 🎨 Impeccable Design System

- **Zero emotikonów**: Wszystkie ikony w systemie to ostre, skalowalne wektory Material Design Icons ([icons.js](./icons.js)).
- **Dual Theme**:
  - 🌲 **Tryb ciemny (Forest Night)**: zoptymalizowany pod zbiórki po zmroku, alarmy mundurowe i oszczędzanie baterii.
  - 📜 **Tryb jasny (Sand & Parchment)**: styl inspirowany harcerską kroniką i pergaminem, idealny na biwakowe tablice i do biura.
- **Dostępność i Mobile First**: Pełna responsywność od ekranów 320px do 4K.

---

## 📄 Licencja

Projekt udostępniony na licencji **MIT**. Możesz go dowolnie używać, modyfikować i wdrażać w swoim środowisku harcerskim (ZHP, ZHR, FSE, SH, itp.).

Czuwaj! ⚜️
