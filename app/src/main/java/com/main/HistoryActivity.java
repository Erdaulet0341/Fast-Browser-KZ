package com.main;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import androidx.room.Room;
import com.main.room.AppDatabase;
import com.main.room.History;
import com.main.room.HistoryDao;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ImageView exit;
    RecyclerView recyclerView;
    SearchView searchView;
    AlertDialog.Builder builder;
    TextView deleteAll;
    TextView empty;
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_history);

        exit = findViewById(R.id.exitHistory);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        deleteAll = findViewById(R.id.deleteAll);
        empty = findViewById(R.id.emptyText);
        builder = new AlertDialog.Builder(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "my-database").build();
        HistoryDao historyDao = db.historyDao();
        Context context = this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<History> histories = historyDao.getAll();
                Collections.reverse(histories);
                if(histories.isEmpty()){
                    empty.setText(getString(R.string.empty_history));
                }
                adapter = new HistoryAdapter(histories, context);
                recyclerView.setAdapter(adapter);
            }
        }).start();

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle(getString(R.string.title_delete))
                        .setMessage(getString(R.string.sentence_delete))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        historyDao.deleteAll();
                                        Intent intent = new Intent(HistoryActivity.this, HistoryActivity.class);
                                        startActivity(intent);
                                    }
                                }).start();
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    ArrayList<History> filteredList = new ArrayList<>();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<History> histories = historyDao.getAll();
                            Collections.reverse(histories);

                            for (History h : histories) {
                                if (h.url.toLowerCase().contains(newText)) {
                                    filteredList.add(h);
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (filteredList.size() == 0) {
                                        Toast.makeText(context, getString(R.string.can_not_find), Toast.LENGTH_SHORT).show();
                                        adapter = new HistoryAdapter(filteredList, context);
                                        recyclerView.setAdapter(adapter);
                                        empty.setText(getString(R.string.can_not_find));

                                    } else {
                                        adapter = new HistoryAdapter(filteredList, context);
                                        recyclerView.setAdapter(adapter);
                                        empty.setText("");
                                    }
                                }
                            });
                        }
                    }).start();
                }
                else{
                    empty.setText("");
                }
                return true;
            }
        });
    }
    void setLocale(String lan){
        Locale locale = new Locale(lan);
        Configuration configuration = new Configuration();
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
    }

    void loadLocale(){
        SharedPreferences pre = getSharedPreferences("Language_set", Activity.MODE_PRIVATE);
        String lan = pre.getString("app_language", Locale.getDefault().getLanguage());
        setLocale(lan);
        Log.d("fnds",lan );
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}