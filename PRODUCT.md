# HarcPunkty — Platforma Rywalizacji Zastępów Harcerskich

<!-- impeccable:product-schema 1 -->

## Platform

Web (PWA) + Android APK (WebView Native Shell z automatycznym updaterem)

## Purpose

Uniwersalny, w pełni konfigurowalny system ewidencji punktacji, rywalizacji zastępów, zgłaszania zbiórek, alarmów mundurowych, sprawności oraz terenowej gry na kodach QR dla dowolnej drużyny, szczepu lub hufca harcerskiego.

## Users & Roles

- **Kadra / Administrator**: Posiada pełen wgląd w punktację, zatwierdza lub odrzuca zbiórki i sprawności, zarządza alarmami i akcjami, generuje kody QR punktów terenowych, zarządza kontami harcerzy, edytuje punktację i konfigurację kwartałów.
- **Zastępowy**: Zgłasza zbiórki swojego zastępu (obecności, mundury), wysyła meldunki z alarmów ze zdjęciem, składa wnioski o sprawności harcerzy swojego zastępu, monitoruje wyniki.
- **Szeregowy**: Monitoruje pozycję swojego zastępu w tabeli kwartalnej, skanuje kody QR na grach terenowych i zbiórkach, składa wnioski o własne sprawności, zarządza swoim profilem.

## Key Workflows

1. **Tablica wyników (Dashboard)**: Kwartalna punktacja zastępów, gładkie wykresy monotoniczne Fritsch-Carlsona dla dowolnej liczby zastępów, historia punktów, podium.
2. **Zbiórki zastępów ze streakiem**: Zgłaszanie zbiórek z podziałem na obecność i umundurowanie harcerzy, bonusy za regularność tydzień po tygodniu, weryfikacja przez kadrę.
3. **Akcje i Alarmy**: Zgłaszanie obecności na grach drużyny oraz błyskawiczne meldunki na alarmach mundurowych (aparat/galeria) z automatycznym naliczaniem punktów wg progów czasowych.
4. **Sprawności harcerskie**: Składanie i akceptacja prób na sprawności (1-3 gwiazdki), historia przyznanych stopni.
5. **Kody QR & Punkty terenowe**: Generator kodów QR dla kadry (opcjonalny link przekierowania do zadań/formularzy), skaner aparatem telefonu dla harcerzy, natychmiastowe powiadomienia dźwiękowe i toast dla kadry, eksport PNG i drukowanie kart punktów.
6. **Zarządzanie kontami**: Generowanie kont zastępami, import grupowy, eksport pakietów haseł, bezpieczna zmiana haseł, weryfikacja tożsamości admina.

## Brand & Customization

- **Konfiguracja w 1 pliku**: `config.js` pozwala w 2 minuty ustawić nazwę jednostki, dowolną liczbę zastępów z kolorami, klucze Firebase oraz zasady punktacji.
- **Wbudowany Asystent (Setup Wizard)**: Pomaga skonfigurować aplikację bezpośrednio w przeglądarce i przetestować połączenie.
- **Styl**: Impeccable Design System z wektorowymi ikonami MDI (zero emotikonów) — ciemny motyw nocny na zbiórki przy ognisku i alarmy, jasny motyw dzienny do biura i tablicy wyników.
