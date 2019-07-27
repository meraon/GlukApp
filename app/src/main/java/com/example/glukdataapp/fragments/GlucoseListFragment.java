package com.example.glukdataapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.glukdataapp.EntryAdapter;
import com.example.glukdataapp.R;
import com.example.glukdataapp.realm.IRealmControl;
import com.example.glukdataapp.realm.RealmControl;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GlucoseListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GlucoseListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GlucoseListFragment extends Fragment {
    private final String TITLE = "Glucose values";

    private RecyclerView recyclerView;
    private TextView titleTextView;
    private ImageView refreshImageView;

    private EntryAdapter adapter;
    private IRealmControl realmController;

    private OnFragmentInteractionListener mListener;

    public GlucoseListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GlucoseListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GlucoseListFragment newInstance() {
        GlucoseListFragment fragment = new GlucoseListFragment();
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
        View view = inflater.inflate(R.layout.entry_list_layout, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewData);
        titleTextView = view.findViewById(R.id.textViewName);
        refreshImageView = view.findViewById(R.id.refreshImageView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        titleTextView.setText(TITLE);

        adapter = new EntryAdapter(getContext(), realmController.getGlucoseList());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layout_manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layout_manager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), layout_manager.getOrientation());
        recyclerView.addItemDecoration(itemDecoration);
        refreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
