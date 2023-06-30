package com.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.main.room.AppDatabase;
import com.main.room.History;
import com.main.room.HistoryDao;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {
    private List<History> histories;
    private Context context;

    public HistoryAdapter(List<History> histories, Context context) {
        this.histories = histories;
        this.context = context;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private Context contextt;
        TextView urlHistory;
        TextView timeHistory;
        ImageView delete;
        ConstraintLayout constraintLayout;

        public Holder(@NonNull View itemView, Context context) {
             super(itemView);
             urlHistory = itemView.findViewById(R.id.urlInHistory);
             timeHistory = itemView.findViewById(R.id.timeURL);
             delete = itemView.findViewById(R.id.deleteUrlhistory);
             urlHistory.setSelected(true);
             urlHistory.setHorizontallyScrolling(true);
             urlHistory.setSingleLine(true);
             contextt = context;
             constraintLayout = itemView.findViewById(R.id.const_layout);
        }

        public void click(String url){
            constraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent =new Intent(context, MainActivity.class);
                    intent.putExtra("url", url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }

        public void bindBooks(String url, String time) {
            urlHistory.setText(url);
            timeHistory.setText(time);
        }

        public void deleteOne(int id) {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppDatabase db = Room.databaseBuilder(context,
                            AppDatabase.class, "my-database").build();
                    HistoryDao historyDao = db.historyDao();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            historyDao.deleteById(id);
                            Intent intent =new Intent(context, HistoryActivity.class);
                            context.startActivity(intent);
                        }
                    }).start();
                }
            });
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new Holder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String url =  histories.get(position).url;
        String time = histories.get(position).time;
        int id = histories.get(position).id;
        holder.bindBooks(url, time);
        holder.deleteOne(id);
        holder.click(url);
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}
