package com.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.main.room.AppDatabase;
import com.main.room.History;
import com.main.room.HistoryDao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    EditText inputText;
    ImageView clearText;
    WebView webView;
    ProgressBar progressBar;
    ImageView urlBack, urlForward, urlRefresh, urlShare, urlHistory, goHome;

    private static final String SHARED_PREFS_NAME = "WebViewPrefs";
    private static final String COOKIES_KEY = "cookies";
    private static final String WEBVIEW_URL_KEY = "webview_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").build();
        HistoryDao historyDao = db.historyDao();

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        inputText = findViewById(R.id.inputURL);
        clearText = findViewById(R.id.clearIcon);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        urlBack = findViewById(R.id.urlBack);
        urlForward = findViewById(R.id.urlForward);
        urlRefresh = findViewById(R.id.urlRefresh);
        urlShare = findViewById(R.id.urlShare);
        urlHistory = findViewById(R.id.urlHistory);
        goHome = findViewById(R.id.goHome);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                inputText.setText(webView.getUrl());
                progressBar.setVisibility(View.INVISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        History h = new History();
                        List<History> historiess = historyDao.getAll();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
                        String currentTime = sdf.format(new Date());
                        h.time = currentTime;
                        h.url = url;
                        boolean containsUrl = false;
                        for (History history : historiess) {
                            if (history.url.equals(url)) {
                                containsUrl = true;
                                break;
                            }
                        }
                        if (!containsUrl) {
                            historyDao.insert(h);
                        }
                    }
                }).start();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("https://www.google.com");
            }
        });

        if(getIntent().getExtras() != null){
            webView.loadUrl(getIntent().getExtras().getString("url"));
        }
        else{
            String lastUrl = sharedPreferences.getString(WEBVIEW_URL_KEY, null);
            if (lastUrl != null) {
                webView.loadUrl(lastUrl);
            } else {
                webView.loadUrl("https://www.google.com");
            }
        }

        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO || i == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
                    loadURL(inputText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        clearText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText.setText("");
            }
        });

        urlBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoBack()) webView.goBack();
            }
        });

        urlForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoForward()) webView.goForward();
            }
        });

        urlRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
            }
        });

        urlShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });
        urlHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(COOKIES_KEY, CookieManager.getInstance().getCookie(webView.getUrl()));
        editor.putString(WEBVIEW_URL_KEY, webView.getUrl());
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String cookies = sharedPreferences.getString(COOKIES_KEY, null);
        if (cookies != null) {
            CookieManager.getInstance().setCookie(webView.getUrl(), cookies);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    void loadURL(String url) {
        if (Patterns.WEB_URL.matcher(url).matches()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl("https://www.google.com/search?q=" + url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
