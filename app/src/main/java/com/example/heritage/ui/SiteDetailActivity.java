package com.example.heritage.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.NotesStore;
import com.example.heritage.data.Site;
import com.example.heritage.util.Money;
import com.example.heritage.util.Ui;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import java.util.Locale;

/** Full information about one site, with implicit intents for maps, sharing and the browser. */
public class SiteDetailActivity extends AppCompatActivity {

    private static final String EXTRA_SITE_ID = "site_id";

    private Site site;
    private MaterialButton btnNotes;
    private boolean toolbarCollapsed;

    public static Intent intent(Context context, long siteId) {
        return new Intent(context, SiteDetailActivity.class).putExtra(EXTRA_SITE_ID, siteId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        site = DbHelper.get(this).getSite(getIntent().getLongExtra(EXTRA_SITE_ID, -1));
        if (site == null) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_site_detail);
        Ui.padForSystemBars(findViewById(R.id.bottom_bar), false, true);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        // The photo draws under the status bar; only the toolbar is pushed below it
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            toolbar.setPadding(toolbar.getPaddingLeft(), top, toolbar.getPaddingRight(), 0);
            return insets;
        });
        setupToolbarColors(toolbar);

        ImageView image = findViewById(R.id.iv_image);
        image.setImageResource(site.imageRes());
        ((TextView) findViewById(R.id.tv_category)).setText(site.category.toUpperCase(Locale.ROOT));
        ((TextView) findViewById(R.id.tv_name)).setText(site.name);
        ((TextView) findViewById(R.id.tv_location)).setText(site.location());
        ((TextView) findViewById(R.id.tv_about)).setText(site.about);

        LinearLayout info = findViewById(R.id.info_container);
        Ui.addRow(info, getString(R.string.label_built), site.built, false);
        Ui.addRow(info, getString(R.string.label_timings), site.timings, false);
        String closed = site.closedDayPlural();
        Ui.addRow(info, getString(R.string.label_closed),
                closed != null ? closed : getString(R.string.open_all_days), false);
        Ui.addRow(info, getString(R.string.label_fee_indian), Money.inr(site.feeIndian), false);
        Ui.addRow(info, getString(R.string.label_fee_foreign), Money.inr(site.feeForeign), false);

        findViewById(R.id.btn_directions).setOnClickListener(v -> openDirections());
        findViewById(R.id.btn_share).setOnClickListener(v -> share());
        findViewById(R.id.btn_read_more).setOnClickListener(v ->
                launch(new Intent(Intent.ACTION_VIEW, Uri.parse(site.wikiUrl))));

        btnNotes = findViewById(R.id.btn_notes);
        btnNotes.setOnClickListener(v -> startActivity(NotesActivity.intent(this, site.id)));
        findViewById(R.id.btn_plan).setOnClickListener(v ->
                startActivity(PlanVisitActivity.newTrip(this, site.id)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (btnNotes != null) {
            boolean hasNotes = new NotesStore(this).exists(site.id);
            btnNotes.setText(hasNotes ? R.string.view_notes : R.string.add_notes);
        }
    }

    /**
     * Back icon and status bar are white over the photo, and switch to the normal
     * theme colors once the photo has scrolled away.
     */
    private void setupToolbarColors(MaterialToolbar toolbar) {
        int onSurface = MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorOnSurface);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        boolean lightTheme = !getResources().getBoolean(R.bool.is_night);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        applyToolbarState(toolbar, controller, false, onSurface, lightTheme);
        appBar.addOnOffsetChangedListener((bar, offset) -> {
            boolean collapsed = bar.getTotalScrollRange() + offset < toolbar.getHeight();
            // Only touch the views when the state flips; setTitle triggers a layout,
            // and doing it on every offset change would loop forever.
            if (collapsed != toolbarCollapsed) {
                applyToolbarState(toolbar, controller, collapsed, onSurface, lightTheme);
            }
        });
        toolbar.setTitleTextColor(onSurface);
    }

    private void applyToolbarState(MaterialToolbar toolbar, WindowInsetsControllerCompat controller,
                                   boolean collapsed, int onSurface, boolean lightTheme) {
        toolbarCollapsed = collapsed;
        toolbar.setBackgroundColor(collapsed
                ? MaterialColors.getColor(toolbar, com.google.android.material.R.attr.colorSurfaceContainer)
                : Color.TRANSPARENT);
        toolbar.setNavigationIconTint(collapsed ? onSurface : Color.WHITE);
        toolbar.setTitle(collapsed ? site.name : null);
        controller.setAppearanceLightStatusBars(collapsed && lightTheme);
    }

    private void openDirections() {
        // geo: URI opens any installed maps app at the site
        Uri uri = Uri.parse(String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                site.lat, site.lng, site.lat, site.lng, Uri.encode(site.name)));
        launch(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void share() {
        StringBuilder text = new StringBuilder()
                .append(site.name).append(", ").append(site.location()).append("\n\n")
                .append(site.about).append("\n\n")
                .append(getString(R.string.label_timings)).append(": ").append(site.timings).append('\n');
        String closed = site.closedDayPlural();
        if (closed != null) {
            text.append(getString(R.string.label_closed)).append(": ").append(closed).append('\n');
        }
        text.append('\n').append(site.wikiUrl);
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_SUBJECT, site.name)
                .putExtra(Intent.EXTRA_TEXT, text.toString());
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    private void launch(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_SHORT).show();
        }
    }
}
