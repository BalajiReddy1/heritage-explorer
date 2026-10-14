package com.example.heritage.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.heritage.BuildConfig;
import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.NotesStore;
import com.example.heritage.data.SessionManager;
import com.example.heritage.data.Trip;
import com.example.heritage.util.Money;
import com.example.heritage.util.Ui;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/** Account details, stats, theme setting, export and sign out. */
public class ProfileFragment extends Fragment {

    private SessionManager session;
    private TextView tvPlanned, tvVisited, tvNotes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());

        String name = session.getName();
        ((TextView) view.findViewById(R.id.tv_avatar)).setText(initials(name));
        ((TextView) view.findViewById(R.id.tv_name)).setText(name);
        ((TextView) view.findViewById(R.id.tv_email)).setText(session.getEmail());
        ((TextView) view.findViewById(R.id.tv_version))
                .setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        tvPlanned = view.findViewById(R.id.tv_stat_planned);
        tvVisited = view.findViewById(R.id.tv_stat_visited);
        tvNotes = view.findViewById(R.id.tv_stat_notes);

        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggle_theme);
        int mode = session.getThemeMode();
        toggle.check(mode == AppCompatDelegate.MODE_NIGHT_NO ? R.id.btn_theme_light
                : mode == AppCompatDelegate.MODE_NIGHT_YES ? R.id.btn_theme_dark
                : R.id.btn_theme_system);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int newMode = checkedId == R.id.btn_theme_light ? AppCompatDelegate.MODE_NIGHT_NO
                    : checkedId == R.id.btn_theme_dark ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (newMode != session.getThemeMode()) session.setThemeMode(newMode);
        });

        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportTrips());
        view.findViewById(R.id.btn_sign_out).setOnClickListener(v -> confirmSignOut());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStats();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) refreshStats();
    }

    private void refreshStats() {
        if (getView() == null) return;
        DbHelper db = DbHelper.get(requireContext());
        tvPlanned.setText(String.valueOf(db.countTrips(Trip.STATUS_PLANNED)));
        tvVisited.setText(String.valueOf(db.countTrips(Trip.STATUS_VISITED)));
        tvNotes.setText(String.valueOf(new NotesStore(requireContext()).count()));
    }

    /** Writes all trips to a text file and opens the share sheet with it. */
    private void exportTrips() {
        List<Trip> trips = DbHelper.get(requireContext()).getTrips(null);
        if (trips.isEmpty()) {
            Toast.makeText(requireContext(), R.string.export_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Virasat trip list\n");
        sb.append(session.getName()).append('\n');
        sb.append("Exported ").append(Ui.dateTime(System.currentTimeMillis())).append("\n\n");
        int i = 1;
        for (Trip t : trips) {
            sb.append(i++).append(". ").append(t.siteName).append(", ").append(t.siteCity).append('\n');
            sb.append("   Date: ").append(Ui.date(t.visitDate)).append('\n');
            sb.append("   Visitors: ").append(t.visitors).append(" (")
                    .append(Trip.TICKET_FOREIGN.equals(t.ticketType) ? "foreign national" : "Indian citizen")
                    .append(")\n");
            sb.append("   Total: ").append(Money.inr(t.total)).append('\n');
            sb.append("   Status: ").append(t.isVisited()
                    ? String.format(Locale.US, "Visited, rated %.1f of 5", t.rating)
                    : "Planned").append("\n\n");
        }

        try {
            File dir = new File(requireContext().getCacheDir(), "exports");
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            File file = new File(dir, "virasat_trips.txt");
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider", file);
            Intent send = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(send, getString(R.string.export_chooser)));
        } catch (IOException e) {
            Toast.makeText(requireContext(), R.string.notes_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmSignOut() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.sign_out, (d, w) -> {
                    session.signOut();
                    startActivity(new Intent(requireContext(), LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .show();
    }

    private static String initials(String name) {
        StringBuilder sb = new StringBuilder();
        for (String part : name.trim().split("\\s+")) {
            if (!part.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(part.charAt(0)));
        }
        return sb.toString();
    }
}
