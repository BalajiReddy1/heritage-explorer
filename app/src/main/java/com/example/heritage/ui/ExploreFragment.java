package com.example.heritage.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.SessionManager;
import com.example.heritage.data.Site;
import com.example.heritage.data.SiteSeed;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/** List of heritage sites with text search and a category filter. */
public class ExploreFragment extends Fragment {

    private SiteAdapter adapter;
    private EditText etSearch;
    private ChipGroup chipGroup;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        String firstName = new SessionManager(requireContext()).getName().split(" ")[0];
        tvGreeting.setText(getString(R.string.greeting, firstName));

        etSearch = view.findViewById(R.id.et_search);
        chipGroup = view.findViewById(R.id.chip_group);
        tvEmpty = view.findViewById(R.id.tv_empty);

        adapter = new SiteAdapter(site ->
                startActivity(SiteDetailActivity.intent(requireContext(), site.id)));
        RecyclerView recycler = view.findViewById(R.id.recycler);
        recycler.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                load();
            }
        });
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> load());

        load();
    }

    private void load() {
        String query = etSearch.getText().toString();
        List<Site> sites = DbHelper.get(requireContext()).getSites(query, selectedCategory());
        adapter.submitList(sites);
        tvEmpty.setVisibility(sites.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Nullable
    private String selectedCategory() {
        int id = chipGroup.getCheckedChipId();
        if (id == R.id.chip_monument) return SiteSeed.MONUMENT;
        if (id == R.id.chip_fort) return SiteSeed.FORT;
        if (id == R.id.chip_temple) return SiteSeed.TEMPLE;
        if (id == R.id.chip_cave) return SiteSeed.CAVE;
        return null;
    }
}
