package com.rezero.indonesia.reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String HOME = "https://darktranslation.com/novel/baca-novel-rezero-bahasa-indonesia/";
    private static final String PREFS = "rezero_reader_prefs";
    private WebView webView;
    private ProgressBar progress;
    private TextView status;
    private SharedPreferences prefs;
    private boolean restoring = false;
    private String restoreUrl = "";
    private int restoreY = 0;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        restoreUrl = prefs.getString("last_url", HOME);
        restoreY = prefs.getInt("last_scroll", 0);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(13, 16, 28));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(9), dp(12), dp(9));
        header.setBackgroundColor(Color.rgb(24, 29, 48));
        TextView logo = new TextView(this);
        logo.setText("✦"); logo.setTextColor(Color.rgb(127, 177, 255)); logo.setTextSize(25);
        logo.setPadding(0, 0, dp(8), 0);
        header.addView(logo);
        TextView title = new TextView(this);
        title.setText("Re:Zero Reader"); title.setTextColor(Color.WHITE); title.setTextSize(17); title.setTypeface(null, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView home = button("Beranda"); home.setOnClickListener(v -> { restoreY = 0; webView.loadUrl(HOME); }); header.addView(home);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.rgb(112, 153, 255)));
        status = new TextView(this); status.setTextColor(Color.LTGRAY); status.setTextSize(11);
        status.setPadding(dp(10), dp(4), dp(10), dp(4)); status.setBackgroundColor(Color.rgb(18, 22, 36)); status.setText("Bookmark menyimpan halaman yang sedang dibuka.");

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true); settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true); settings.setLoadWithOverviewMode(true); settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); settings.setSupportZoom(true); settings.setAllowFileAccess(false); settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return false; }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                status.setText("Memuat halaman..."); status.setVisibility(View.VISIBLE); progress.setVisibility(View.VISIBLE);
                prefs.edit().putString("last_url", url).apply();
            }
            @Override public void onPageFinished(WebView view, String url) {
                addHistory(url, view.getTitle());
                if (url.equals(restoreUrl) && restoreY > 0) {
                    final int y = restoreY; restoring = true;
                    view.postDelayed(() -> { view.scrollTo(0, y); restoring = false; }, 500);
                    restoreY = 0;
                }
                status.setText("Halaman siap • posisi baca disimpan otomatis"); progress.setVisibility(View.GONE);
                status.postDelayed(() -> status.setVisibility(View.GONE), 2200);
                prefs.edit().putString("last_url", url).putString("last_title", view.getTitle()).apply();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() { @Override public void onProgressChanged(WebView view, int p) { progress.setProgress(p); progress.setVisibility(p >= 100 ? View.GONE : View.VISIBLE); } });
        webView.setOnScrollChangeListener((View v, int x, int y, int ox, int oy) -> {
            if (!restoring && webView.getUrl() != null) prefs.edit().putString("last_url", webView.getUrl()).putInt("last_scroll", y).putString("last_title", webView.getTitle()).apply();
        });

        LinearLayout nav = new LinearLayout(this); nav.setBackgroundColor(Color.rgb(24, 29, 48)); nav.setPadding(dp(5), dp(5), dp(5), dp(5));
        TextView resume = button("▶ Lanjut"); resume.setOnClickListener(v -> resumeReading()); nav.addView(resume, weight());
        TextView bookmark = button("☆ Bookmark"); bookmark.setOnClickListener(v -> saveBookmark()); nav.addView(bookmark, weight());
        TextView bookmarks = button("★ Tersimpan"); bookmarks.setOnClickListener(v -> showSaved("bookmarks", "Bookmark")); nav.addView(bookmarks, weight());
        TextView history = button("◷ Riwayat"); history.setOnClickListener(v -> showSaved("history", "Riwayat baca")); nav.addView(history, weight());

        root.addView(header); root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3))); root.addView(status); root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1)); root.addView(nav);
        setContentView(root);
        if (state != null) { webView.restoreState(state); } else { webView.loadUrl(restoreUrl); }
    }

    private TextView button(String text) {
        TextView t = new TextView(this); t.setText(text); t.setTextColor(Color.rgb(226, 233, 255)); t.setTextSize(12); t.setGravity(Gravity.CENTER);
        t.setPadding(dp(7), dp(9), dp(7), dp(9)); return t;
    }
    private LinearLayout.LayoutParams weight() { return new LinearLayout.LayoutParams(0, -2, 1); }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    private void resumeReading() {
        String url = prefs.getString("last_url", HOME); int y = prefs.getInt("last_scroll", 0);
        if (webView.getUrl() != null && webView.getUrl().equals(url)) { webView.scrollTo(0, y); }
        else { restoreUrl = url; restoreY = y; webView.loadUrl(url); }
        Toast.makeText(this, "Melanjutkan bacaan terakhir", Toast.LENGTH_SHORT).show();
    }
    private void saveBookmark() {
        String url = webView.getUrl(); if (url == null) { Toast.makeText(this, "Tunggu halaman terbuka dulu", Toast.LENGTH_SHORT).show(); return; }
        JSONArray list = readArray("bookmarks");
        try {
            for (int i=0;i<list.length();i++) if (url.equals(list.getJSONObject(i).optString("url"))) { Toast.makeText(this, "Halaman ini sudah di-bookmark", Toast.LENGTH_SHORT).show(); return; }
            JSONObject item = new JSONObject(); item.put("url", url); item.put("title", safeTitle()); item.put("scroll", webView.getScrollY()); item.put("time", System.currentTimeMillis()); list.put(item);
            prefs.edit().putString("bookmarks", list.toString()).apply(); Toast.makeText(this, "Bookmark tersimpan!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) { Toast.makeText(this, "Bookmark gagal disimpan", Toast.LENGTH_SHORT).show(); }
    }
    private String safeTitle() { String t = webView.getTitle(); return (t == null || t.trim().isEmpty()) ? "Halaman Re:Zero" : t; }
    private JSONArray readArray(String key) { try { return new JSONArray(prefs.getString(key, "[]")); } catch (Exception e) { return new JSONArray(); } }
    private void addHistory(String url, String title) {
        if (url == null || !url.startsWith("https://darktranslation.com/")) return;
        JSONArray old = readArray("history"), next = new JSONArray();
        try {
            JSONObject item = new JSONObject(); item.put("url", url); item.put("title", (title == null || title.isEmpty()) ? url : title); item.put("scroll", webView.getScrollY()); item.put("time", System.currentTimeMillis()); next.put(item);
            for (int i=0;i<old.length() && next.length()<40;i++) { JSONObject prev=old.getJSONObject(i); if (!url.equals(prev.optString("url"))) next.put(prev); }
            prefs.edit().putString("history", next.toString()).apply();
        } catch (Exception ignored) { }
    }
    private void showSaved(String key, String label) {
        JSONArray list = readArray(key); if (list.length()==0) { Toast.makeText(this, label + " masih kosong", Toast.LENGTH_SHORT).show(); return; }
        String[] names = new String[list.length()];
        for (int i=0;i<list.length();i++) { JSONObject o=list.optJSONObject(i); names[i] = (o==null ? "Halaman" : o.optString("title", "Halaman")) + "\n" + (o==null ? "" : o.optString("url")); }
        new AlertDialog.Builder(this).setTitle(label).setItems(names, (d, which) -> {
            JSONObject o=list.optJSONObject(which); if (o!=null) { restoreUrl=o.optString("url", HOME); restoreY=o.optInt("scroll",0); webView.loadUrl(restoreUrl); }
        }).setNegativeButton("Tutup", null).setNeutralButton(key.equals("bookmarks") ? "Hapus semua" : "Bersihkan", (d,w) -> new AlertDialog.Builder(this).setMessage("Hapus semua " + label.toLowerCase() + "?").setNegativeButton("Batal", null).setPositiveButton("Hapus", (dd,ww)-> { prefs.edit().putString(key,"[]").apply(); Toast.makeText(this, label + " dihapus", Toast.LENGTH_SHORT).show(); }).show()).show();
    }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); if (webView!=null) webView.saveState(out); }
    @Override protected void onPause() { if (webView!=null && webView.getUrl()!=null) prefs.edit().putString("last_url",webView.getUrl()).putInt("last_scroll",webView.getScrollY()).putString("last_title",safeTitle()).apply(); super.onPause(); }
    @Override protected void onDestroy() { if(webView!=null){webView.stopLoading();webView.destroy();} super.onDestroy(); }
}
