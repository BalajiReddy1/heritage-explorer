package com.example.heritage.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heritage.R;
import com.example.heritage.data.Site;
import com.example.heritage.util.Money;

public class SiteAdapter extends ListAdapter<Site, SiteAdapter.Holder> {

    public interface OnSiteClick {
        void onClick(Site site);
    }

    private final OnSiteClick listener;

    public SiteAdapter(OnSiteClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_site, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Site site = getItem(position);
        h.image.setImageResource(site.imageRes());
        h.category.setText(site.category);
        h.name.setText(site.name);
        h.location.setText(site.location());
        h.fee.setText(h.itemView.getContext().getString(R.string.fee_from, Money.inr(site.feeIndian)));
        h.itemView.setOnClickListener(v -> listener.onClick(site));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView category, name, location, fee;

        Holder(View v) {
            super(v);
            image = v.findViewById(R.id.iv_image);
            category = v.findViewById(R.id.tv_category);
            name = v.findViewById(R.id.tv_name);
            location = v.findViewById(R.id.tv_location);
            fee = v.findViewById(R.id.tv_fee);
        }
    }

    private static final DiffUtil.ItemCallback<Site> DIFF = new DiffUtil.ItemCallback<Site>() {
        @Override
        public boolean areItemsTheSame(@NonNull Site a, @NonNull Site b) {
            return a.id == b.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Site a, @NonNull Site b) {
            return a.equals(b);
        }
    };
}
