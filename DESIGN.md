# Design System — Knieja Redesign

<!-- impeccable:design-schema 1 -->

## World: Forest & Sand (Modern Scout Modernism)

Projekt wizualny dedykowany harcerskiej aplikacji operacyjnej 8 UDH „Knieja”. Łączy leśną tradycję puszczańską z ergonomicznym, nowoczesnym minimalizmem narzędzi operacyjnych. Odrzuca infantylne emoji i pstrokate ozdobniki na rzecz precyzji, wektorowych symboli MDI i czytelnej typografii.

## Mode

**Operate**: Kluczowy jest szybki dostęp do zadań, doskonała czytelność tabel i wyników w słońcu oraz po zmroku, natychmiastowa reakcja przycisków w terenie i wyraźny podział ról.

## Color Palette

### Light Mode (Dzień / Biuro / Zbiórka)
- `--bg`: `#f6f4ee` (ciepły piaskowy pergamin)
- `--card`: `#ffffff` (czysta biel kontenerów)
- `--card-sub`: `#edf0eb` (delikatnie zabarwione tło sekcji i kafelków)
- `--ink`: `#1a261e` (głęboki ciemny grafit igliwia)
- `--mut`: `#606e64` (czytelny, stonowany szaro-zielony odcień drugorzędny)
- `--line`: `#ded9ce` (subtelna, ciepła krawędź 1px)
- `--pri`: `#1e4a30` (szlachetna głęboka zieleń boru)
- `--pri-hover`: `#163824`
- `--pri-ink`: `#ffffff`
- `--acc`: `#2e7049` (świeża zieleń akcentowa)
- `--gold`: `#b88318` (herbata i stare złoto dla punktów, podium i sprawności)
- `--gold-bg`: `#fef6e7`
- `--warn`: `#c0482b` (ostrzegawcza ceglasta czerwień)
- `--warn-bg`: `#fdf0ed`
- `--ok`: `#277a48`
- `--ok-bg`: `#edf7f0`

### Dark Mode (Noc / Alarmy / Ognisko)
- `--bg`: `#121714` (ciemny leśny węgiel)
- `--card`: `#1a221d` (grafit z nutą mchu)
- `--card-sub`: `#222d26` (podwyższone tło paneli)
- `--ink`: `#edf2ee` (jasny, czytelny odcień tekstu)
- `--mut`: `#8fa093` (przyjazna dla oka szarość mchu)
- `--line`: `#2b3830` (dyskretne linie podziału)
- `--pri`: `#3b8758` (rozświetlona leśna zieleń)
- `--pri-hover`: `#499e69`
- `--pri-ink`: `#0d1711`
- `--acc`: `#5ab079`
- `--gold`: `#e4ab3c`
- `--gold-bg`: `#2b2413`
- `--warn`: `#e26a4f`
- `--warn-bg`: `#2e1a15`
- `--ok`: `#4bb373`
- `--ok-bg`: `#15291d`

## Typography

- **Rodzina**: `system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif`
- **Cyfry tabelaryczne**: `font-variant-numeric: tabular-nums lining-nums;` gwarantuje idealne wyrównanie liczb w tabelach i rankingach.
- **Skala**:
  - Tytuł sekcji (H1/Header): `1.25rem` (`20px`), waga `700`, tracking `-0.01em`
  - Nagłówek karty (H2): `1.05rem` (`17px`), waga `600`
  - Podtytuł (H3): `0.95rem` (`15px`), waga `600`
  - Tekst podstawowy (Body): `0.9375rem` (`15px`), interlinia `1.5`
  - Drobne etykiety (Meta/Captions): `0.8125rem` (`13px`)

## Iconography: Material Design Icons (MDI)

- Wszystkie emoji zostały bezwzględnie usunięte z interfejsu.
- Zastosowano wektorowy moduł MDI SVG renderowany inline (`icons.js`), co zapewnia:
  - 100% niezależności od połączenia internetowego (idealne na biwakach i w lesie).
  - Skalowalność do dowolnego rozmiaru ekranu (retina / 4K / budżetowe telefony).
  - Precyzyjny dobór kolorów przez `currentColor`.
  - Spójną grubością linii i siatkę 24x24 px.

## Component Architecture

- **Nawigacja**: Przypięty do góry, horyzontalnie przewijalny pasek z czytelnymi zakładkami, ikoną MDI oraz wskaźnikiem aktywnej zakładki.
- **Karty (Cards)**: Ramka 1px w kolorze `--line`, zaokrąglenie `12px`, subtelny cień `0 2px 6px rgba(0,0,0,0.04)`.
- **Przyciski**:
  - `.btn`: Dyskretny, z tłem `--card-sub` i ramką `--line`.
  - `.btn-pri`: Główna akcja, wypełnienie `--pri`, kontrastowy tekst `--pri-ink`.
  - `.btn-danger`: Działanie destrukcyjne (usunięcie), ramka i tekst `--warn`.
  - `.btn-icon`: Klawisze z samą ikoną lub ikoną z tekstem w standardzie flexbox z odstępem `6px`.
- **Tabele i Listy**: Elastyczne, przewijalne horyzontalnie (`.table-scroll`), z naprzemiennymi wierszami lub wyraźnymi liniami podziału.
- **Modal dialog**: Dedykowany, wyśrodkowany dialog potwierdzeń i akcji z klawiszem zamknięcia i obsługą klawiatury (Escape).
