package com.example.heritage.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heritage.R;
import com.example.heritage.data.SiteSeed;
import com.example.heritage.data.Trip;
import com.example.heritage.util.Money;
import com.example.heritage.util.Ui;
import com.google.android.material.color.MaterialColors;

public class TripAdapter extends ListAdapter<Trip, TripAdapter.Holder> {

    public interface OnTripClick {
        void onClick(Trip trip);
    }

    private final OnTripClick listener;

    public TripAdapter(OnTripClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    public Trip tripAt(int position) {
        return getItem(position);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Trip trip = getItem(position);
        Context c = h.itemView.getContext();

        h.image.setImageResource(SiteSeed.imageFor(trip.siteImageKey));
        h.name.setText(trip.siteName);
        h.date.setText(Ui.date(trip.visitDate));
        h.meta.setText(trip.visitors == 1
                ? c.getString(R.string.trip_meta_one, Money.inr(trip.total))
                : c.getString(R.string.trip_meta, trip.visitors, Money.inr(trip.total)));

        boolean visited = trip.isVisited();
        h.status.setText(visited ? R.string.status_visited : R.string.status_planned);
        int bg = MaterialColors.getColor(h.status, visited
                ? com.google.android.material.R.attr.colorTertiaryContainer
                : com.google.android.material.R.attr.colorSecondaryContainer);
        int fg = MaterialColors.getColor(h.status, visited
                ? com.google.android.material.R.attr.colorOnTertiaryContainer
                : com.google.android.material.R.attr.colorOnSecondaryContainer);
        h.status.setBackgroundTintList(ColorStateList.valueOf(bg));
        h.status.setTextColor(fg);

        h.rating.setVisibility(visited && trip.rating > 0 ? View.VISIBLE : View.GONE);
        h.rating.setRating(trip.rating);

        h.itemView.setOnClickListener(v -> listener.onClick(trip));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name, status, date, meta;
        final RatingBar rating;

        Holder(View v) {
            super(v);
            image = v.findViewById(R.id.iv_site);
            name = v.findViewById(R.id.tv_name);
            status = v.findViewById(R.id.tv_status);
            date = v.findViewById(R.id.tv_date);
            meta = v.findViewById(R.id.tv_meta);
            rating = v.findViewById(R.id.rating);
        }
    }

    private static final DiffUtil.ItemCallback<Trip> DIFF = new DiffUtil.ItemCallback<Trip>() {
        @Override
        public boolean areItemsTheSame(@NonNull Trip a, @NonNull Trip b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Trip a, @NonNull Trip b) {
            return a.equals(b);
        }
    };
}
