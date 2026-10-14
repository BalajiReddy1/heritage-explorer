package com.example.heritage.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.Site;
import com.example.heritage.data.Trip;
import com.example.heritage.util.FeeCalculator;
import com.example.heritage.util.Money;
import com.example.heritage.util.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * Shows the values entered on the previous screen (Practical 1A), the cost
 * calculation (Practical 2B) and the total in other currencies (Practical 2C).
 * Saving inserts or updates the trip in SQLite (Practical 4A).
 */
public class VisitSummaryActivity extends AppCompatActivity {

    static final String EXTRA_SITE_ID = "site_id";
    static final String EXTRA_TRIP_ID = "trip_id";
    static final String EXTRA_NAME = "name";
    static final String EXTRA_DATE = "date";
    static final String EXTRA_VISITORS = "visitors";
    static final String EXTRA_TICKET = "ticket";
    static final String EXTRA_GUIDE = "guide";
    static final String EXTRA_AUDIO = "audio";

    private Site site;
    private Trip trip;
    private LinearLayout costContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        site = DbHelper.get(this).getSite(in.getLongExtra(EXTRA_SITE_ID, -1));
        if (site == null) {
            finish();
            return;
        }

        trip = new Trip();
        trip.id = in.getLongExtra(EXTRA_TRIP_ID, -1);
        trip.siteId = site.id;
        trip.visitorName = in.getStringExtra(EXTRA_NAME);
        trip.visitDate = in.getLongExtra(EXTRA_DATE, 0);
        trip.visitors = in.getIntExtra(EXTRA_VISITORS, 1);
        trip.ticketType = in.getStringExtra(EXTRA_TICKET);
        trip.guide = in.getBooleanExtra(EXTRA_GUIDE, false);
        trip.audio = in.getBooleanExtra(EXTRA_AUDIO, false);
        trip.total = FeeCalculator.total(site, trip.ticketType, trip.visitors, trip.guide, trip.audio);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_visit_summary);
        Ui.padForSystemBars(findViewById(R.id.bottom_bar), false, true);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        ((ImageView) findViewById(R.id.iv_site)).setImageResource(site.imageRes());
        ((TextView) findViewById(R.id.tv_site_name)).setText(site.name);
        ((TextView) findViewById(R.id.tv_site_location)).setText(site.location());

        LinearLayout details = findViewById(R.id.details_container);
        boolean foreign = Trip.TICKET_FOREIGN.equals(trip.ticketType);
        Ui.addRow(details, getString(R.string.label_visitor), trip.visitorName, false);
        Ui.addRow(details, getString(R.string.label_date), Ui.date(trip.visitDate), false);
        Ui.addRow(details, getString(R.string.label_visitors), String.valueOf(trip.visitors), false);
        Ui.addRow(details, getString(R.string.label_ticket),
                getString(foreign ? R.string.ticket_type_foreign : R.string.ticket_type_indian), false);
        Ui.addRow(details, getString(R.string.label_guide),
                getString(trip.guide ? R.string.yes : R.string.no), false);
        Ui.addRow(details, getString(R.string.label_audio),
                getString(trip.audio ? R.string.yes : R.string.no), false);

        costContainer = findViewById(R.id.cost_container);
        MaterialButtonToggleGroup toggle = findViewById(R.id.toggle_currency);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) showCost(unitFor(checkedId));
        });
        showCost(Money.Unit.INR);

        ((TextView) findViewById(R.id.tv_rate_note)).setText(getString(R.string.rate_note,
                Money.inr(Money.INR_PER_USD), Money.inr(Money.INR_PER_EUR)));

        MaterialButton btnSave = findViewById(R.id.btn_save);
        btnSave.setText(trip.id > 0 ? R.string.update_trip : R.string.save_trip);
        btnSave.setOnClickListener(v -> save());
    }

    private static Money.Unit unitFor(int buttonId) {
        if (buttonId == R.id.btn_usd) return Money.Unit.USD;
        if (buttonId == R.id.btn_eur) return Money.Unit.EUR;
        return Money.Unit.INR;
    }

    private void showCost(Money.Unit unit) {
        costContainer.removeAllViews();
        int price = FeeCalculator.ticketPrice(site, trip.ticketType);

        Ui.addRow(costContainer,
                getString(R.string.label_tickets_calc, trip.visitors, Money.inr(price)),
                Money.format(FeeCalculator.tickets(price, trip.visitors), unit), false);
        if (trip.guide) {
            Ui.addRow(costContainer, getString(R.string.label_guide),
                    Money.format(FeeCalculator.guide(true), unit), false);
        }
        if (trip.audio) {
            Ui.addRow(costContainer, getString(R.string.label_audio),
                    Money.format(FeeCalculator.audio(true, trip.visitors), unit), false);
        }
        Ui.addRow(costContainer, getString(R.string.label_total), Money.format(trip.total, unit), true);
        Ui.addRow(costContainer, getString(R.string.label_per_person),
                Money.format(FeeCalculator.perPerson(trip.total, trip.visitors), unit), false);
    }

    private void save() {
        DbHelper db = DbHelper.get(this);
        if (trip.id > 0) {
            Trip existing = db.getTrip(trip.id);
            if (existing != null) {
                trip.status = existing.status;
                trip.rating = existing.rating;
            }
            db.updateTrip(trip);
            Toast.makeText(this, R.string.trip_updated, Toast.LENGTH_SHORT).show();
        } else {
            trip.id = 0;
            db.insertTrip(trip);
            Toast.makeText(this, R.string.trip_saved, Toast.LENGTH_SHORT).show();
        }
        // Back to the home screen on the Trips tab, closing the form screens in between
        startActivity(MainActivity.intent(this, R.id.nav_trips));
    }
}
