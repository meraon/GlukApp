package com.example.glukdataapp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.glukdataapp.realm.IRealmControl;
import com.example.glukdataapp.realm.RealmControl;
import com.example.glukdataapp.realm.models.IModel;
import com.example.glukdataapp.realm.models.InsulinEntry;

import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.GlukDataViewHolder> {
    List<IModel> dataList;
    AdapterOnClickListener listener;
    Context context;

    IRealmControl realmControler;

    public EntryAdapter(Context context, List<? extends IModel> dataList) {
        this.context = context;
        this.dataList = (List<IModel>)dataList;
        this.listener = getOnClickListener();
        realmControler = new RealmControl();
    }

    private AdapterOnClickListener getOnClickListener() {
        EntryAdapter.AdapterOnClickListener listener = new EntryAdapter.AdapterOnClickListener() {
            @Override
            public void onDateClick(final int position) {
                try {
                    final IModel item = dataList.get(position);
                    Long ts = item.getTimestamp();
                    Float v = item.getValue();

                    int year = item.getYear();
                    int month = item.getMonth();
                    int day = item.getDayOfMonth();

                    DatePickerDialog datePicker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            realmControler.updateDate(item, year, month, dayOfMonth);
                            notifyItemChanged(position);
                        }
                    }, year, month, day);
                    datePicker.show();
                } catch(IndexOutOfBoundsException ex){
                    Toast.makeText(context, "Item no longer exists!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onTimeClick(final int position) {
                try {
                    final IModel item = dataList.get(position);
                    int hours = item.getHours();
                    int mins = item.getMinutes();

                    TimePickerDialog timePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            realmControler.updateTime(item, hourOfDay, minute);
                            notifyItemChanged(position);
                        }
                    }, hours, mins, true);
                    timePicker.show();
                } catch(IndexOutOfBoundsException ex){
                    Toast.makeText(context, "Item no longer exists!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onValueClick(final int position) {
                try {
                    final IModel item = dataList.get(position);
                    final AlertDialog d = new AlertDialog.Builder(context)
                            .setTitle("Edit")
                            .setView(R.layout.edit_value_layout)
                            .create();
                    d.show();

                    final EditText valueEditText = d.findViewById(R.id.editTextEditValue);
                    valueEditText.setText(item.getValueString());
                    Button save = d.findViewById(R.id.buttonSaveValue);
                    Button cancel = d.findViewById(R.id.buttonCancelValue);

                    save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            IModel item = dataList.get(position);
                            Float value = Float.parseFloat(valueEditText.getText().toString());
                            realmControler.updateValue(item, value);
                            d.dismiss();
                            notifyItemChanged(position);
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            d.cancel();
                        }
                    });
                } catch(IndexOutOfBoundsException ex){
                    Toast.makeText(context, "Item no longer exists!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }

            }

            @Override
            public void onDelete(int position) {
                try {
                    IModel item = dataList.get(position);
                    realmControler.deleteEntry(item);
                    notifyItemRemoved(position);
                } catch(IndexOutOfBoundsException ex){
                    Toast.makeText(context, "Item no longer exists!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
            }
        };

        return listener;
    }

    @NonNull
    @Override
    public GlukDataViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_layout, viewGroup, false);
        return new GlukDataViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull GlukDataViewHolder glukDataViewHolder, int i) {
        glukDataViewHolder.populateData(dataList.get(i));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class GlukDataViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView time;
        TextView value;
        ImageView delete;

        public GlukDataViewHolder(@NonNull View itemView, final AdapterOnClickListener listener) {
            super(itemView);

            date = itemView.findViewById(R.id.textViewDate);
            time = itemView.findViewById(R.id.textViewTime);
            value = itemView.findViewById(R.id.textViewValue);
            delete = itemView.findViewById(R.id.imageViewDelete);

            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener == null) return;
                    int position = getAdapterPosition();
                    if(position == RecyclerView.NO_POSITION) return;
                    listener.onDateClick(position);
                }
            });

            time.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener == null) return;
                    int position = getAdapterPosition();
                    if(position == RecyclerView.NO_POSITION) return;
                    listener.onTimeClick(position);
                }
            });

            value.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener == null) return;
                    int position = getAdapterPosition();
                    if(position == RecyclerView.NO_POSITION) return;
                    listener.onValueClick(position);
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener == null) return;
                    int position = getAdapterPosition();
                    if(position == RecyclerView.NO_POSITION) return;
                    listener.onDelete(position);
                }
            });

        }

        public void populateData(IModel entry){
            date.setText(entry.getDateString());
            time.setText(entry.getTimeString());
            value.setText(entry.getValueString());
            if(entry instanceof InsulinEntry) {
                InsulinEntry item = (InsulinEntry)entry;
                if(item.isDayDosage()){
                    value.setTextColor(context.getColor(R.color.colorDayDosage));
                } else {
                    value.setTextColor(context.getColor(R.color.colorNightDosage));
                }

            }
        }
    }

    public interface AdapterOnClickListener {
        void onDateClick(int position);
        void onTimeClick(int position);
        void onValueClick(int position);
        void onDelete(int position);
    }
}


