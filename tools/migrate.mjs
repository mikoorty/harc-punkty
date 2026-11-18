// tools/migrate.mjs
// Skrypt migracji kont i danych ze starej bazy (kniejapunkty) do nowej (knieja-punkty)

const OLD_CONFIG = {
  apiKey: "AIzaSyC9Q1oNT1iNANO3JCMsG-18nWVIiwl7VXI",
  projectId: "kniejapunkty",
  domain: "@kniejapunkty.firebaseapp.com"
};

const NEW_CONFIG = {
  apiKey: "AIzaSyCY5P2_S_vQgqxpCcvXQM6bqAaoSZG7W8A",
  projectId: "knieja-punkty",
  domain: "@knieja-punkty.firebaseapp.com"
};

function decodeValue(v) {
  if (!v) return null;
  if (v.stringValue !== undefined) return v.stringValue;
  if (v.integerValue !== undefined) return parseInt(v.integerValue, 10);
  if (v.doubleValue !== undefined) return v.doubleValue;
  if (v.booleanValue !== undefined) return v.booleanValue;
  if (v.timestampValue !== undefined) return v.timestampValue;
  if (v.nullValue !== undefined) return null;
  if (v.mapValue !== undefined) {
    const res = {};
    for (const [k, val] of Object.entries(v.mapValue.fields || {})) {
      res[k] = decodeValue(val);
    }
    return res;
  }
  if (v.arrayValue !== undefined) {
    return (v.arrayValue.values || []).map(decodeValue);
  }
  return v;
}

function encodeValue(v) {
  if (v === null || v === undefined) return { nullValue: null };
  if (typeof v === 'boolean') return { booleanValue: v };
  if (typeof v === 'number') {
    if (Number.isInteger(v)) return { integerValue: String(v) };
    return { doubleValue: v };
  }
  if (typeof v === 'string') return { stringValue: v };
  if (Array.isArray(v)) return { arrayValue: { values: v.map(encodeValue) } };
  if (typeof v === 'object') {
    const fields = {};
    for (const [k, val] of Object.entries(v)) {
      fields[k] = encodeValue(val);
    }
    return { mapValue: { fields } };
  }
  return { stringValue: String(v) };
}

function decodeDoc(doc) {
  const data = {};
  for (const [k, v] of Object.entries(doc.fields || {})) {
    data[k] = decodeValue(v);
  }
  const parts = doc.name.split('/');
  data.id = parts[parts.length - 1];
  return data;
}

async function login(apiKey, email, password) {
  const res = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${apiKey}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, returnSecureToken: true })
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error?.message || 'Błąd logowania');
  return data;
}

async function signUp(apiKey, email, password) {
  const res = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${apiKey}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, returnSecureToken: true })
  });
  const data = await res.json();
  if (!res.ok) {
    if (data.error?.message === 'EMAIL_EXISTS') {
      return await login(apiKey, email, password);
    }
    throw new Error(data.error?.message || 'Błąd rejestracji');
  }
  return data;
}

async function getCollection(projectId, token, colName) {
  const url = `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents/${colName}?pageSize=300`;
  const headers = token ? { Authorization: `Bearer ${token}` } : {};
  const res = await fetch(url, { headers });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(`Błąd pobierania kolekcji ${colName}: ${data.error?.message || res.statusText}`);
  }
  return (data.documents || []).map(decodeDoc);
}

async function setDocument(projectId, token, path, obj) {
  const fields = {};
  for (const [k, v] of Object.entries(obj)) {
    if (k === 'id') continue;
    fields[k] = encodeValue(v);
  }
  const url = `https://firestore.googleapis.com/v1/projects/${projectId}/databases/(default)/documents/${path}`;
  const res = await fetch(url, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    },
    body: JSON.stringify({ fields })
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(`Błąd zapisu ${path}: ${data.error?.message || res.statusText}`);
  }
  return data;
}

export async function runMigration(adminLogin, adminPassword) {
  console.log(`\n🚀 Rozpoczynam migrację z bazy ${OLD_CONFIG.projectId} do ${NEW_CONFIG.projectId}...`);

  const oldEmail = adminLogin.includes('@') ? adminLogin : (adminLogin + OLD_CONFIG.domain);
  console.log(`1. Logowanie do starej bazy jako ${oldEmail}...`);
  const oldAuth = await login(OLD_CONFIG.apiKey, oldEmail, adminPassword);
  console.log(`✓ Zalogowano pomyślnie do starej bazy (UID: ${oldAuth.localId})`);

  console.log(`2. Pobieranie użytkowników i haseł ze starej bazy...`);
  const oldUsers = await getCollection(OLD_CONFIG.projectId, oldAuth.idToken, 'users');
  console.log(`✓ Pobrano ${oldUsers.length} użytkowników.`);

  let oldSecrets = [];
  try {
    oldSecrets = await getCollection(OLD_CONFIG.projectId, oldAuth.idToken, 'sekrety');
    console.log(`✓ Pobrano ${oldSecrets.length} zapisanych haseł.`);
  } catch (e) {
    console.warn(`! Uwaga: nie udało się pobrać kolekcji sekrety (${e.message}). Użytkownicy otrzymają hasło domyślne lub hasło administratora.`);
  }
  const secretsMap = {};
  for (const s of oldSecrets) {
    if (s.id && s.haslo) secretsMap[s.id] = s.haslo;
  }

  // Pobierz także config
  console.log(`3. Kopiowanie konfiguracji (punktacja, progi)...`);
  try {
    const resCfg = await fetch(`https://firestore.googleapis.com/v1/projects/${OLD_CONFIG.projectId}/databases/(default)/documents/config/main`);
    const cfgData = await resCfg.json();
    if (cfgData.fields) {
      // tymczasowy login na nowe konto admina do zapisu
    }
  } catch (e) {}

  console.log(`4. Klonowanie kont do nowej bazy...`);
  let createdCount = 0;
  let adminToken = null;

  for (const u of oldUsers) {
    const loginName = u.login || (u.email ? u.email.split('@')[0] : u.id);
    const newEmail = loginName + NEW_CONFIG.domain;
    const password = secretsMap[u.id] || (loginName === adminLogin ? adminPassword : "start123");

    try {
      const newAuth = await signUp(NEW_CONFIG.apiKey, newEmail, password);
      const newUid = newAuth.localId;

      if (loginName === adminLogin || u.admin || u.rola === 'kadra') {
        adminToken = newAuth.idToken;
      }

      // Zapisujemy profil w users
      await setDocument(NEW_CONFIG.projectId, newAuth.idToken, `users/${newUid}`, {
        email: newEmail,
        login: loginName,
        imie: u.imie || loginName,
        rola: u.rola || "szeregowy",
        admin: !!u.admin,
        zastep: u.zastep || "brak",
        poprzednieId: u.id
      });

      // Zapisujemy w loginy
      await setDocument(NEW_CONFIG.projectId, newAuth.idToken, `loginy/${loginName}`, {
        email: newEmail,
        uid: newUid
      });

      // Zapisujemy w sekrety
      await setDocument(NEW_CONFIG.projectId, newAuth.idToken, `sekrety/${newUid}`, {
        haslo: password
      });

      createdCount++;
      console.log(`  [${createdCount}/${oldUsers.length}] Utworzono: ${loginName} (${u.imie || ''}) - rola: ${u.rola}`);
    } catch (err) {
      console.error(`  ❌ Błąd przy tworzeniu konta ${loginName}: ${err.message}`);
    }
  }

  // Kopiowanie konfiguracji i danych historycznych
  if (adminToken) {
    try {
      const resCfg = await fetch(`https://firestore.googleapis.com/v1/projects/${OLD_CONFIG.projectId}/databases/(default)/documents/config/main`);
      const cfgData = await resCfg.json();
      if (cfgData.fields) {
        const decodedCfg = decodeDoc(cfgData);
        await setDocument(NEW_CONFIG.projectId, adminToken, 'config/main', decodedCfg);
        console.log(`✓ Skopiowano konfigurację punktacji (/config/main).`);
      }
    } catch (e) {
      console.warn(`! Błąd kopiowania config:`, e.message);
    }

    const dataCols = ['zbiorkiZastepu', 'alarmy', 'meldunki', 'sprawnosci', 'akcje', 'korekty'];
    console.log(`\n5. Kopiowanie danych (zbiórki, alarmy, meldunki)...`);
    for (const col of dataCols) {
      try {
        const resCol = await fetch(`https://firestore.googleapis.com/v1/projects/${OLD_CONFIG.projectId}/databases/(default)/documents/${col}?pageSize=300`);
        if (!resCol.ok) continue;
        const colData = await resCol.json();
        const docs = colData.documents || [];
        if (docs.length > 0) {
          console.log(`  Pobieram ${docs.length} dokumentów z ${col}...`);
          for (const d of docs) {
            const docId = d.name.split('/').pop();
            const decoded = decodeDoc(d);
            await setDocument(NEW_CONFIG.projectId, adminToken, `${col}/${docId}`, decoded);
          }
          console.log(`  ✓ Skopiowano ${docs.length} dokumentów do ${col}.`);
        }
      } catch (err) {
        console.warn(`! Błąd przy kopiowaniu ${col}:`, err.message);
      }
    }
  }

  console.log(`\n🎉 Gotowe! Pomyślnie przeniesiono ${createdCount} z ${oldUsers.length} kont do nowego projektu ${NEW_CONFIG.projectId}.`);
  return { success: true, count: createdCount, total: oldUsers.length };
}

// Uruchomienie z linii poleceń
if (process.argv[1] && process.argv[1].endsWith('migrate.mjs')) {
  const login = process.argv[2];
  const password = process.argv[3];
  if (!login || !password) {
    console.log("Użycie: node tools/migrate.mjs <login> <haslo>");
    console.log("Przykład: node tools/migrate.mjs mikoorty twojeHaslo");
    process.exit(1);
  }
  runMigration(login, password).catch(e => {
    console.error("Błąd krytyczny migracji:", e);
    process.exit(1);
  });
}
