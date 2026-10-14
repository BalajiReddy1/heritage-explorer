package com.example.heritage.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.Trip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Saved trips from SQLite. Tap a trip to edit, mark as visited or delete it.
 * Swiping a trip away deletes it, with Undo.
 */
public class TripsFragment extends Fragment {

    private DbHelper db;
    private TripAdapter adapter;
    private ChipGroup chipGroup;
    private TextView tvCount;
    private View empty;
    private View coordinator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = DbHelper.get(requireContext());
        chipGroup = view.findViewById(R.id.chip_group);
        tvCount = view.findViewById(R.id.tv_count);
        empty = view.findViewById(R.id.empty);
        coordinator = view.findViewById(R.id.coordinator);

        adapter = new TripAdapter(this::showOptions);
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int position = vh.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) delete(adapter.tripAt(position));
            }
        }).attachToRecyclerView(recycler);

        chipGroup.setOnCheckedStateChangeListener((group, ids) -> load());
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) load();
    }

    private void load() {
        if (getView() == null) return;
        String status = null;
        int checked = chipGroup.getCheckedChipId();
        if (checked == R.id.chip_planned) status = Trip.STATUS_PLANNED;
        else if (checked == R.id.chip_visited) status = Trip.STATUS_VISITED;

        List<Trip> trips = db.getTrips(status);
        adapter.submitList(new ArrayList<>(trips));
        empty.setVisibility(trips.isEmpty() ? View.VISIBLE : View.GONE);
        tvCount.setText(getString(R.string.trips_count,
                db.countTrips(Trip.STATUS_PLANNED), db.countTrips(Trip.STATUS_VISITED)));
    }

    private void showOptions(Trip trip) {
        List<String> labels = new ArrayList<>();
        List<Runnable> actions = new ArrayList<>();

        labels.add(getString(R.string.edit));
        actions.add(() -> startActivity(PlanVisitActivity.editTrip(requireContext(), trip)));
        if (!trip.isVisited()) {
            labels.add(getString(R.string.mark_visited));
            actions.add(() -> showRatingDialog(trip));
        }
        labels.add(getString(R.string.delete));
        actions.add(() -> delete(trip));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(trip.siteName)
                .setItems(labels.toArray(new String[0]), (d, which) -> actions.get(which).run())
                .show();
    }

    private void showRatingDialog(Trip trip) {
        View content = getLayoutInflater().inflate(R.layout.dialog_rating, null);
        ((TextView) content.findViewById(R.id.tv_message))
                .setText(getString(R.string.rate_message, trip.siteName));
        RatingBar ratingBar = content.findViewById(R.id.rating);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rate_title)
                .setView(content)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, (d, w) -> {
                    db.markVisited(trip.id, ratingBar.getRating());
                    load();
                })
                .show();
    }

    private void delete(Trip trip) {
        db.deleteTrip(trip.id);
        load();
        Snackbar.make(coordinator, R.string.trip_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    db.insertTrip(trip);
                    load();
                })
                .show();
    }
}
