package com.example.glukdataapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.glukdataapp.R;
import com.example.glukdataapp.realm.IRealmControl;
import com.example.glukdataapp.realm.RealmControl;
import com.example.glukdataapp.realm.models.GlucoseEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import main.java.gluklibrary.HelperMethods;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GlucoseEntryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GlucoseEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GlucoseEntryFragment extends Fragment {

    public static final String TITLE = "GlucoseEntry";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DATE = "date";
    private static final String TIME = "time";

    private String date;
    private String time;

    private Calendar date_time;

    private DatePickerDialog.OnDateSetListener onDateSetListener;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private OnFragmentInteractionListener mListener;

    Button saveButton;
    EditText valueEditText;
    TextView titleTextView;
    Button dateButton;
    Button timeButton;

    private IRealmControl realmController;

    public GlucoseEntryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GlucoseEntryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GlucoseEntryFragment newInstance() {
        GlucoseEntryFragment fragment = new GlucoseEntryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realmController = new RealmControl();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.create_entry_layout, container, false);

        saveButton = view.findViewById(R.id.create_saveButton);
        valueEditText = view.findViewById(R.id.create_entryEditText);
        titleTextView = view.findViewById(R.id.create_titleTextView);
        dateButton = view.findViewById(R.id.create_dateButton);
        timeButton = view.findViewById(R.id.create_timeButton);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        titleTextView.setText(TITLE);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Float value = getValue();
                if (value == null){
                    Toast.makeText(getContext(), "Unable to parse value", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                    String dateInString = dateButton.getText() + " " + timeButton.getText();
                    Date date = formatter.parse(dateInString);
                    GlucoseEntry item = new GlucoseEntry(realmController.generateGlucoseId(),
                            date.getTime(),
                            value);
                    realmController.saveValue(item);
                    valueEditText.setText("");
                    makeToast("Value saved!");

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initButtonsDateTime();
    }

    private void initButtonsDateTime(){
        date = HelperMethods.getCurrentDate();
        time = HelperMethods.getCurrentTime();
        String[] date_params = date.split("/");
        String[] time_params = time.split(":");
        Date dt = new Date(Integer.parseInt(date_params[0]) - 1900,
                Integer.parseInt(date_params[1]) - 1,
                Integer.parseInt(date_params[2]),
                Integer.parseInt(time_params[0]),
                Integer.parseInt(time_params[1]));
        date_time = Calendar.getInstance();
        date_time.setTime(dt);

        dateButton.setText(date);
        initOnDateSetListener();
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(),
                        R.style.MySpinnerDatePickerStyle,
                        onDateSetListener,
                        date_time.get(Calendar.YEAR),
                        date_time.get(Calendar.MONTH),
                        date_time.get(Calendar.DAY_OF_MONTH));
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));;
                dialog.show();
            }
        });
        timeButton.setText(time);
        initOnTimeSetListener();
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(getContext(),
                        R.style.MySpinnerTimePickerStyle,
                        onTimeSetListener,
                        date_time.get(Calendar.HOUR_OF_DAY),
                        date_time.get(Calendar.MINUTE),
                        true);
                //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));;
                dialog.show();
            }
        });
    }

    private void makeToast(final String message){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initOnDateSetListener(){
        onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                date_time.set(Calendar.YEAR, year);
                date_time.set(Calendar.MONTH, month);
                date_time.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String date = year + "/" + normalizeNumber((month + 1)) + "/" + normalizeNumber(dayOfMonth);
                dateButton.setText(date);
            }
        };
    }

    private void initOnTimeSetListener(){
        onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                date_time.set(Calendar.HOUR, hourOfDay);
                date_time.set(Calendar.MINUTE, minute);

                String time = normalizeNumber(hourOfDay) + ":" + normalizeNumber(minute);
                timeButton.setText(time);
            }
        };
    }

    private String normalizeNumber(int number){
        if(number < 10){
            return "0" + number;
        }
        else return String.valueOf(number);
    }

    private Date getDate(){
        String date_time_str = dateButton.getText().toString() + " " +
                timeButton.getText().toString();
        SimpleDateFormat df = new SimpleDateFormat(getString(R.string.date_format) +
                " " + getString(R.string.time_format));
        try {
            return df.parse(date_time_str);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    private Float getValue(){
        try {
            String val_str = valueEditText.getText().toString();
            return Float.parseFloat(val_str);
        }
        catch (NullPointerException e){
           return null;
        }
        catch (NumberFormatException ex){
            return null;
        }

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
