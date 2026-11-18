/**
 * ==============================================================================
 * HarcPunkty — Główny plik konfiguracyjny
 * ==============================================================================
 * Ten plik pozwala dostosować aplikację do dowolnej drużyny harcerskiej w 2 minuty.
 * Zmień poniższe wartości, aby ustawić nazwę drużyny, zastępy, kolory i dane Firebase.
 */

export const CONFIG = {
  // 1. Nazwa i tożsamość jednostki
  unit: {
    // Pełna nazwa drużyny / szczepu (np. '8 UDH „Knieja”' lub '1. Warszawska Drużyna Harcerzy')
    name: "8 UDH „Knieja”",
    
    // Krótka nazwa jednostki
    shortName: "Knieja",
    
    // Tytuł aplikacji
    appTitle: "HarcPunkty",
    
    // Podtytuł widoczny pod nagłówkiem
    subtitle: "Punktacja całoroczna zastępów",
    
    // Ikona MDI w nagłówku: 'pine-tree', 'campfire', 'compass', 'flag', 'shield', 'tent'
    icon: "pine-tree",
    
    // Główny kolor przewodni motywu (HEX)
    themeColor: "#1e4a30"
  },

  // 2. Zastępy (Dowolna liczba zastępów! Dodaj, zmień lub usuń według potrzeb)
  patrols: [
    { id: "lisy", name: "Lisy", color: "#e06c28", icon: "paw" },
    { id: "losie", name: "Łosie", color: "#b58900", icon: "paw" },
    { id: "zubry", name: "Żubry", color: "#859900", icon: "paw" },
    { id: "rysie", name: "Rysie", color: "#2aa198", icon: "paw" },
    { id: "niedzwiedzie", name: "Niedźwiedzie", color: "#268bd2", icon: "paw" },
    { id: "gronostaje", name: "Gronostaje", color: "#d33682", icon: "paw" }
  ],

  // 3. Konfiguracja Firebase (z Firebase Console -> Project Settings -> General -> Web App)
  // Możesz podać własny projekt Firebase tutaj lub wkleić w kreatorze w przeglądarce.
  firebase: {
    apiKey: "AIzaSyCY5P2_S_vQgqxpCcvXQM6bqAaoSZG7W8A",
    authDomain: "knieja-punkty.firebaseapp.com",
    projectId: "knieja-punkty",
    storageBucket: "knieja-punkty.firebasestorage.app",
    messagingSenderId: "652089825737",
    appId: "1:652089825737:web:10e4cfa4e676f3daeb3018",
    measurementId: "G-C6WXM14FMZ"
  },

  // 4. Opcje zaawansowane
  advanced: {
    // Pozwól pierwszemu zarejestrowanemu użytkownikowi w pustej bazie objąć rolę Administratora
    allowBootstrapAdmin: true,
    
    // Opcjonalny własny adres URL do pobierania APK (jeśli pusty, aplikacja wykrywa go automatycznie z repozytorium GitHub)
    apkDownloadUrl: ""
  }
};

// Opcjonalne nadpisanie z lokalnego kreatora konfiguracji (Setup Wizard w przeglądarce)
let activeConfig = CONFIG;
try {
  const localOverride = localStorage.getItem("harc_custom_config");
  if (localOverride) {
    const parsed = JSON.parse(localOverride);
    activeConfig = {
      ...CONFIG,
      ...parsed,
      unit: { ...CONFIG.unit, ...(parsed.unit || {}) },
      firebase: { ...CONFIG.firebase, ...(parsed.firebase || {}) },
      patrols: parsed.patrols || CONFIG.patrols
    };
  }
} catch (e) {}

export const APP_CONFIG = activeConfig;
