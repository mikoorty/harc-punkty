package pl.knieja.punkty;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

/**
 * HarcPunkty — aplikacja otwierająca https://mikoorty.github.io/harc-punkty/ w pełnym oknie.
 * Obsługuje: okno, aparat/galerię, zapis plików, drukowanie oraz automatyczne aktualizacje APK.
 */
public class MainActivity extends Activity {
    static final String START = "https://mikoorty.github.io/harc-punkty/";
    static final String HOST = "mikoorty.github.io";
    static final String VERSION_URL = "https://mikoorty.github.io/harc-punkty/apk_version.json";
    static final String GH_RELEASES_URL = "https://api.github.com/repos/mikoorty/harc-punkty/releases/latest";

    static final int REQ_FILE = 1;
    static final int REQ_CAMERA_PERMISSION = 2;
    static final int REQ_INSTALL_UNKNOWN = 3;

    private WebView web;
    private WebView printView;
    private ValueCallback<Uri[]> fileCallback;
    private Uri cameraUri;
    private PermissionRequest pendingPermissionRequest;

    private File updateApkFile = null;
    private AlertDialog downloadDialog = null;
    private ProgressBar downloadProgressBar = null;
    private TextView downloadPercentText = null;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setUserAgentString(s.getUserAgentString() + " KniejaApp/1");
        CookieManager.getInstance().setAcceptCookie(true);
        web.addJavascriptInterface(new Bridge(), "KniejaApp");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (HOST.equals(u.getHost()) || "panbadek.github.io".equals(u.getHost())) return false;          // nasza strona — zostaje w aplikacji
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;                                          // obce linki — w przeglądarce
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean wantsVideo = false;
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r)) {
                            wantsVideo = true;
                            break;
                        }
                    }
                    if (wantsVideo) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            request.grant(request.getResources());
                        } else {
                            pendingPermissionRequest = request;
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
                        }
                    } else {
                        request.grant(request.getResources());
                    }
                });
            }

            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                cameraUri = null;
                Intent target;
                if (params.isCaptureEnabled()) {                      // „Zrób zdjęcie” → aparat
                    try {
                        File f = File.createTempFile("zdjecie_", ".jpg", getCacheDir());
                        cameraUri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                        target = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        target.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                        target.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        target = galleryIntent();
                    }
                } else {                                              // „Wybierz z galerii”
                    target = galleryIntent();
                }
                try {
                    startActivityForResult(target, REQ_FILE);
                } catch (Exception e) {
                    fileCallback.onReceiveValue(null);
                    fileCallback = null;
                    toast("Nie można otworzyć aparatu ani galerii");
                }
                return true;
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Knieja")
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setOnCancelListener(d -> result.confirm())
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Knieja")
                        .setMessage(message)
                        .setPositiveButton("OK", (d, w) -> result.confirm())
                        .setNegativeButton("Anuluj", (d, w) -> result.cancel())
                        .setOnCancelListener(d -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                final EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Knieja")
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton("OK", (d, w) -> result.confirm(input.getText().toString()))
                        .setNegativeButton("Anuluj", (d, w) -> result.cancel())
                        .setOnCancelListener(d -> result.cancel())
                        .show();
                return true;
            }
        });

        if (state != null) web.restoreState(state);
        else web.loadUrl(START);

        // Automatyczne sprawdzenie aktualizacji w tle po uruchomieniu aplikacji
        web.postDelayed(() -> checkForAppUpdate(false), 2000);
    }

    private Intent galleryIntent() {
        Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("image/*");
        return Intent.createChooser(pick, "Wybierz zdjęcie");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Jeśli użytkownik wraca z ustawień instalowania nieznanych aplikacji i wyraził zgodę
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().canRequestPackageInstalls()) {
            if (updateApkFile != null && updateApkFile.exists()) {
                File toInstall = updateApkFile;
                updateApkFile = null;
                launchInstallIntent(toInstall);
            }
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (request == REQ_INSTALL_UNKNOWN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().canRequestPackageInstalls()) {
                if (updateApkFile != null && updateApkFile.exists()) {
                    File toInstall = updateApkFile;
                    updateApkFile = null;
                    launchInstallIntent(toInstall);
                }
            } else {
                toast("Brak uprawnienia do instalacji aktualizacji.");
            }
            return;
        }

        if (request != REQ_FILE || fileCallback == null) { super.onActivityResult(request, result, data); return; }
        Uri[] out = null;
        if (result == RESULT_OK) {
            if (data != null && data.getData() != null) out = new Uri[]{ data.getData() };
            else if (cameraUri != null) out = new Uri[]{ cameraUri };
        }
        fileCallback.onReceiveValue(out);
        fileCallback = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> {
                    if (pendingPermissionRequest != null) {
                        try {
                            pendingPermissionRequest.grant(pendingPermissionRequest.getResources());
                        } catch (Exception ignored) {}
                        pendingPermissionRequest = null;
                    }
                    web.evaluateJavascript("window.onCameraPermissionGranted && window.onCameraPermissionGranted();", null);
                });
            } else {
                runOnUiThread(() -> {
                    if (pendingPermissionRequest != null) {
                        try {
                            pendingPermissionRequest.deny();
                        } catch (Exception ignored) {}
                        pendingPermissionRequest = null;
                    }
                });
                toast("Wymagane jest zezwolenie na aparat, aby skanować kody QR.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show());
    }

    private long getCurrentVersionCode() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? pInfo.getLongVersionCode() : pInfo.versionCode;
        } catch (Exception e) {
            return 1;
        }
    }

    private String getCurrentVersionName() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

    /** Sprawdzanie aktualizacji aplikacji Android */
    public void checkForAppUpdate(final boolean userInitiated) {
        new Thread(() -> {
            try {
                int remoteCode = -1;
                String remoteName = "";
                String apkUrl = "";
                String message = "";

                // 1. Sprawdź plik apk_version.json z GitHub Pages
                try {
                    URL u = new URL(VERSION_URL + "?t=" + System.currentTimeMillis());
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);
                    conn.setUseCaches(false);
                    if (conn.getResponseCode() == 200) {
                        String jsonStr = readStream(conn.getInputStream());
                        JSONObject j = new JSONObject(jsonStr);
                        remoteCode = j.optInt("versionCode", -1);
                        remoteName = j.optString("versionName", "");
                        apkUrl = j.optString("apkUrl", "");
                        message = j.optString("message", "");
                    }
                    conn.disconnect();
                } catch (Exception ignored) {}

                // 2. Fallback: sprawdź GitHub Releases API
                if (remoteCode <= 0) {
                    try {
                        URL u = new URL(GH_RELEASES_URL);
                        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                        conn.setRequestProperty("User-Agent", "KniejaApp/" + getCurrentVersionName());
                        conn.setConnectTimeout(6000);
                        conn.setReadTimeout(6000);
                        if (conn.getResponseCode() == 200) {
                            String jsonStr = readStream(conn.getInputStream());
                            JSONObject j = new JSONObject(jsonStr);
                            String tag = j.optString("tag_name", "");
                            if (tag.startsWith("apk-")) {
                                remoteCode = Integer.parseInt(tag.substring(4));
                                remoteName = "1." + remoteCode;
                            }
                            apkUrl = "https://github.com/mikoorty/harc-punkty/releases/latest/download/HarcPunkty.apk";
                        }
                        conn.disconnect();
                    } catch (Exception ignored) {}
                }

                final long curCode = getCurrentVersionCode();
                if (remoteCode > curCode && !apkUrl.isEmpty()) {
                    final String fName = remoteName.isEmpty() ? ("1." + remoteCode) : remoteName;
                    final String fUrl = apkUrl;
                    final String fMsg = message.isEmpty()
                            ? ("Dostępna jest nowa wersja aplikacji HarcPunkty (" + fName + ").\n\nCzy chcesz ją teraz pobrać i zaktualizować?")
                            : (message + "\n\nCzy chcesz ją teraz pobrać i zaktualizować?");
                    runOnUiThread(() -> showUpdatePrompt(fName, fUrl, fMsg));
                } else if (userInitiated) {
                    runOnUiThread(() -> toast("Aplikacja HarcPunkty jest aktualna (wersja " + getCurrentVersionName() + ")."));
                }
            } catch (Exception e) {
                if (userInitiated) runOnUiThread(() -> toast("Nie udało się sprawdzić aktualizacji."));
            }
        }).start();
    }

    private void showUpdatePrompt(String newVersionName, final String downloadUrl, String message) {
        new AlertDialog.Builder(this)
                .setTitle("🚀 Dostępna nowa wersja aplikacji!")
                .setMessage(message)
                .setPositiveButton("Pobierz i zainstaluj", (dialog, which) -> downloadAndInstallApk(downloadUrl))
                .setNegativeButton("Przypomnij później", null)
                .show();
    }

    private void downloadAndInstallApk(final String downloadUrl) {
        // Nowoczesne okno dialogowe z paskiem postępu pobierania
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        downloadProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        downloadProgressBar.setMax(100);
        downloadProgressBar.setIndeterminate(false);
        layout.addView(downloadProgressBar);

        downloadPercentText = new TextView(this);
        downloadPercentText.setText("Pobrano: 0%");
        downloadPercentText.setPadding(0, pad / 2, 0, 0);
        layout.addView(downloadPercentText);

        downloadDialog = new AlertDialog.Builder(this)
                .setTitle("Pobieranie aktualizacji...")
                .setMessage("Pobieram nową wersję HarcPunkty. Proszę czekać.")
                .setView(layout)
                .setCancelable(false)
                .create();
        downloadDialog.show();

        new Thread(() -> {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                // Obsługa przekierowań HTTP (302/307 do AWS S3)
                int status = conn.getResponseCode();
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == 307 || status == 308) {
                    String newUrl = conn.getHeaderField("Location");
                    conn.disconnect();
                    url = new URL(newUrl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                }

                int fileLength = conn.getContentLength();
                File updateDir = new File(getCacheDir(), "updates");
                if (!updateDir.exists()) updateDir.mkdirs();
                final File targetApk = new File(updateDir, "HarcPunkty_update.apk");
                if (targetApk.exists()) targetApk.delete();

                try (InputStream in = conn.getInputStream();
                     OutputStream out = new FileOutputStream(targetApk)) {
                    byte[] buf = new byte[8192];
                    long total = 0;
                    int count;
                    while ((count = in.read(buf)) != -1) {
                        total += count;
                        if (fileLength > 0) {
                            final int progress = (int) (total * 100 / fileLength);
                            runOnUiThread(() -> {
                                if (downloadProgressBar != null) downloadProgressBar.setProgress(progress);
                                if (downloadPercentText != null) downloadPercentText.setText("Pobrano: " + progress + "%");
                            });
                        }
                        out.write(buf, 0, count);
                    }
                    out.flush();
                }
                conn.disconnect();

                updateApkFile = targetApk;
                runOnUiThread(() -> {
                    if (downloadDialog != null && downloadDialog.isShowing()) {
                        try { downloadDialog.dismiss(); } catch (Exception ignored) {}
                    }
                    promptInstallApk(targetApk);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (downloadDialog != null && downloadDialog.isShowing()) {
                        try { downloadDialog.dismiss(); } catch (Exception ignored) {}
                    }
                    toast("Błąd pobierania aktualizacji: " + e.getMessage());
                });
            }
        }).start();
    }

    private void promptInstallApk(File apkFile) {
        if (apkFile == null || !apkFile.exists()) return;

        // Na Android 8.0+ sprawdź uprawnienie do instalowania nieznanych aplikacji
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                new AlertDialog.Builder(this)
                        .setTitle("Zezwolenie na instalację")
                        .setMessage("Aby zainstalować zaktualizowaną wersję, system Android wymaga zezwolenia na instalowanie aplikacji z tego źródła.\n\nWybierz „Przejdź do ustawień” i włącz opcję „Zezwalaj z tego źródła”, a instalacja rozpocznie się automatycznie.")
                        .setPositiveButton("Przejdź do ustawień", (d, w) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQ_INSTALL_UNKNOWN);
                        })
                        .setNegativeButton("Anuluj", null)
                        .show();
                return;
            }
        }

        launchInstallIntent(apkFile);
    }

    private void launchInstallIntent(File apkFile) {
        try {
            Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            toast("Nie udało się uruchomić instalatora: " + e.getMessage());
        }
    }

    private static String readStream(InputStream is) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int count;
        while ((count = is.read(buf)) != -1) bo.write(buf, 0, count);
        return bo.toString("UTF-8");
    }

    /** Funkcje dostępne dla strony jako window.KniejaApp */
    class Bridge {
        @JavascriptInterface
        public void saveFile(String base64, String name, String mime) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                boolean saved = false;
                if (Build.VERSION.SDK_INT >= 29) {
                    try {
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
                        cv.put(MediaStore.Downloads.MIME_TYPE, mime);
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                        if (uri != null) {
                            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                                if (os != null) {
                                    os.write(bytes);
                                    saved = true;
                                    toast("Zapisano w folderze Pobrane: " + name);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.w("KniejaApp", "MediaStore insert failed", e);
                    }
                }
                if (!saved) {
                    File f = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name);
                    try (FileOutputStream os = new FileOutputStream(f)) { os.write(bytes); }
                    Uri u = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                    Intent share = new Intent(Intent.ACTION_SEND).setType(mime)
                            .putExtra(Intent.EXTRA_STREAM, u).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(share, name));
                }
            } catch (Exception e) {
                toast("Nie udało się zapisać pliku");
            }
        }

        @JavascriptInterface
        public void copyToClipboard(String text) {
            runOnUiThread(() -> {
                try {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("Knieja", text));
                        toast("Skopiowano do schowka");
                    }
                } catch (Exception e) {
                    Log.w("KniejaApp", "copyToClipboard error", e);
                }
            });
        }

        @JavascriptInterface
        public void toast(String msg) {
            MainActivity.this.toast(msg);
        }

        @JavascriptInterface
        public boolean hasCameraPermission() {
            return checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }

        @JavascriptInterface
        public void requestCameraPermission() {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
            } else {
                runOnUiThread(() -> {
                    web.evaluateJavascript("window.onCameraPermissionGranted && window.onCameraPermissionGranted();", null);
                });
            }
        }

        @JavascriptInterface
        public void checkForUpdates() {
            MainActivity.this.checkForAppUpdate(true);
        }

        @JavascriptInterface
        public String getAppVersion() {
            return getCurrentVersionName();
        }

        @JavascriptInterface
        public int getAppVersionCode() {
            return (int) getCurrentVersionCode();
        }

        @JavascriptInterface
        public void printHtml(String html, String title) {
            runOnUiThread(() -> {
                try {
                    WebView pw = new WebView(MainActivity.this);
                    pw.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView v, String url) {
                            try {
                                PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                                if (pm != null) {
                                    pm.print(title, v.createPrintDocumentAdapter(title), new PrintAttributes.Builder().build());
                                } else {
                                    toast("Usługa drukowania jest niedostępna");
                                }
                            } catch (Exception pe) {
                                Log.w("KniejaApp", "Print error", pe);
                                toast("Błąd podczas drukowania");
                            }
                        }
                    });
                    pw.loadDataWithBaseURL(START, html, "text/html", "utf-8", null);
                    printView = pw;     // trzymamy referencję, żeby nie zniknął w trakcie drukowania
                } catch (Exception e) {
                    toast("Błąd drukowania");
                }
            });
        }
    }
}
