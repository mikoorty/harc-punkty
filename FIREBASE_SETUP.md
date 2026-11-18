# 🔥 Przewodnik konfiguracji Firebase (Krok po kroku)

Darmowy plan Firebase (Spark Plan) w 100% wystarcza na potrzeby nawet dużego szczepu czy hufca (do 50 000 odczytów dziennie i 1 GB bazy danych za darmo).

Postępuj zgodnie z poniższymi krokami, aby skonfigurować bazę danych dla swojej drużyny w około 5 minut.

---

## Krok 1: Utworzenie projektu w Firebase Console

1. Otwórz [Firebase Console](https://console.firebase.google.com/) i zaloguj się swoim kontem Google.
2. Kliknij **Add project** (Dodaj projekt).
3. Podaj nazwę projektu (np. `punkty-1wdh`).
4. Google Analytics możesz wyłączyć (lub zostawić włączone) i kliknij **Create project**.
5. Poczekaj chwilę na utworzenie projektu i kliknij **Continue**.

---

## Krok 2: Włączenie modułu logowania (Authentication)

1. W menu bocznym po lewej stronie wybierz **Build** → **Authentication**.
2. Kliknij przycisk **Get started**.
3. Na liście dostawców logowania wybierz **Email/Password**.
4. Włącz pierwszy suwak: **Email/Password** (Email link pozostaw wyłączony).
5. Kliknij **Save**.

---

## Krok 3: Utworzenie bazy Firestore Database

1. W menu bocznym wybierz **Build** → **Firestore Database**.
2. Kliknij przycisk **Create database**.
3. Wybierz lokalizację serwera: zalecana dla Polski to `eur3 (europe-west)` lub `europe-central2 (Warszawa)`.
4. Wybierz tryb: **Start in production mode** (Tryb produkcyjny) i kliknij **Next** / **Create**.

---

## Krok 4: Wklejenie reguł bezpieczeństwa (Firestore Rules)

Aplikacja posiada gotowe, uniwersalne reguły bezpieczeństwa chroniące dane przed nieautoryzowanym dostępem.

1. W sekcji **Firestore Database** przejdź do górnej zakładki **Rules**.
2. Skasuj całą dotychczasową zawartość edytora i wklej zawartość pliku [`firestore.rules`](./firestore.rules) z tego repozytorium.
3. Kliknij przycisk **Publish** (Opublikuj).

---

## Krok 5: Pobranie kluczy konfiguracyjnych (Web App)

1. W lewym górnym rogu obok **Project Overview** kliknij ikonę zębatki ⚙️ → **Project settings**.
2. W zakładce **General** zjedź na sam dół do sekcji **Your apps**.
3. Kliknij ikonę przeglądarki `</>` (Web).
4. Wpisz pseudonim aplikacji (np. `HarcPunkty Web`) i kliknij **Register app** (opcję Firebase Hosting możesz pominąć).
5. Zobaczysz fragment kodu z obiektem `firebaseConfig`:

```javascript
const firebaseConfig = {
  apiKey: "AIzaSy...",
  authDomain: "twoj-projekt.firebaseapp.com",
  projectId: "twoj-projekt",
  storageBucket: "twoj-projekt.firebasestorage.app",
  messagingSenderId: "1234567890",
  appId: "1:1234567890:web:abcdef123456"
};
```

6. Skopiuj te wartości i wklej je do sekcji `firebase` w pliku [`config.js`](./config.js) w Twoim repozytorium:

```javascript
  firebase: {
    apiKey: "AIzaSy...",
    authDomain: "twoj-projekt.firebaseapp.com",
    projectId: "twoj-projekt",
    storageBucket: "twoj-projekt.firebasestorage.app",
    messagingSenderId: "1234567890",
    appId: "1:1234567890:web:abcdef123456"
  }
```

---

## Krok 6: Pierwsze logowanie i Administrator

Gdy uruchomisz swoją aplikację po raz pierwszy na nowej bazie danych:
1. Przejdź do zakładki logowania w aplikacji.
2. Zobaczysz komunikat o nowej instalacji.
3. Załóż pierwsze konto użytkownika.
4. System automatycznie nada pierwszemu kontu rolę **Kadry i Administratora** oraz zablokuje bazę dla kolejnych rejestracji bez zgody kadry.
5. Z poziomu zakładki **Konta** możesz teraz założyć konta dla zastępowych i harcerzy lub zaimportować całą drużynę z listy!
