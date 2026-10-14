package com.example.heritage.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.SessionManager;
import com.example.heritage.data.Site;
import com.example.heritage.data.Trip;
import com.example.heritage.util.FeeCalculator;
import com.example.heritage.util.Money;
import com.example.heritage.util.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Visit form using EditText, date picker, RadioButtons and CheckBoxes (Practical 1A).
 * Values are passed to {@link VisitSummaryActivity} through Intent extras.
 * Also used to edit an existing trip.
 */
public class PlanVisitActivity extends AppCompatActivity {

    static final String EXTRA_SITE_ID = "site_id";
    static final String EXTRA_TRIP_ID = "trip_id";

    private static final String STATE_DATE = "date";

    private Site site;
    private long tripId;
    private long visitDate;

    private TextInputLayout tilName, tilDate, tilVisitors;
    private TextInputEditText etName, etDate, etVisitors;
    private RadioGroup rgTicket;
    private CheckBox cbGuide, cbAudio;

    public static Intent newTrip(Context context, long siteId) {
        return new Intent(context, PlanVisitActivity.class).putExtra(EXTRA_SITE_ID, siteId);
    }

    public static Intent editTrip(Context context, Trip trip) {
        return new Intent(context, PlanVisitActivity.class)
                .putExtra(EXTRA_SITE_ID, trip.siteId)
                .putExtra(EXTRA_TRIP_ID, trip.id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        site = DbHelper.get(this).getSite(getIntent().getLongExtra(EXTRA_SITE_ID, -1));
        if (site == null) {
            finish();
            return;
        }
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, -1);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plan_visit);
        Ui.padForSystemBars(findViewById(R.id.bottom_bar), false, true);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        if (tripId > 0) toolbar.setTitle(R.string.edit_visit);

        ((ImageView) findViewById(R.id.iv_site)).setImageResource(site.imageRes());
        ((TextView) findViewById(R.id.tv_site_name)).setText(site.name);
        ((TextView) findViewById(R.id.tv_site_location)).setText(site.location());

        tilName = findViewById(R.id.til_name);
        tilDate = findViewById(R.id.til_date);
        tilVisitors = findViewById(R.id.til_visitors);
        etName = findViewById(R.id.et_name);
        etDate = findViewById(R.id.et_date);
        etVisitors = findViewById(R.id.et_visitors);
        rgTicket = findViewById(R.id.rg_ticket);
        cbGuide = findViewById(R.id.cb_guide);
        cbAudio = findViewById(R.id.cb_audio);

        ((RadioButton) findViewById(R.id.rb_indian))
                .setText(getString(R.string.ticket_indian, Money.inr(site.feeIndian)));
        ((RadioButton) findViewById(R.id.rb_foreign))
                .setText(getString(R.string.ticket_foreign, Money.inr(site.feeForeign)));
        cbGuide.setText(getString(R.string.extra_guide, Money.inr(FeeCalculator.GUIDE_FEE)));
        cbAudio.setText(getString(R.string.extra_audio, Money.inr(FeeCalculator.AUDIO_FEE_PER_PERSON)));

        if (savedInstanceState != null) {
            setDate(savedInstanceState.getLong(STATE_DATE, 0));
        } else if (tripId > 0) {
            fillFromTrip(DbHelper.get(this).getTrip(tripId));
        } else {
            etName.setText(new SessionManager(this).getName());
            etVisitors.setText("1");
        }

        etDate.setOnClickListener(v -> showDatePicker());
        tilDate.setStartIconOnClickListener(v -> showDatePicker());
        findViewById(R.id.btn_review).setOnClickListener(v -> review());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(STATE_DATE, visitDate);
    }

    private void fillFromTrip(Trip trip) {
        if (trip == null) return;
        etName.setText(trip.visitorName);
        etVisitors.setText(String.valueOf(trip.visitors));
        setDate(trip.visitDate);
        rgTicket.check(Trip.TICKET_FOREIGN.equals(trip.ticketType) ? R.id.rb_foreign : R.id.rb_indian);
        cbGuide.setChecked(trip.guide);
        cbAudio.setChecked(trip.audio);
    }

    private void showDatePicker() {
        long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(todayUtc)
                .setValidator(DateValidatorPointForward.from(todayUtc))
                .build();
        long selection = visitDate > 0 ? Math.max(localToUtc(visitDate), todayUtc) : todayUtc;
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.hint_visit_date)
                .setSelection(selection)
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(picked -> {
            setDate(utcToLocal(picked));
            checkClosedDay();
        });
        picker.show(getSupportFragmentManager(), "date");
    }

    private void setDate(long localMidnight) {
        visitDate = localMidnight;
        etDate.setText(localMidnight > 0 ? Ui.date(localMidnight) : null);
        tilDate.setError(null);
    }

    /** @return true if the site is open on the chosen day */
    private boolean checkClosedDay() {
        if (site.closedDay == 0 || visitDate == 0) return true;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(visitDate);
        if (c.get(Calendar.DAY_OF_WEEK) == site.closedDay) {
            tilDate.setError(getString(R.string.error_closed_day, site.name, site.closedDayPlural()));
            return false;
        }
        return true;
    }

    private void review() {
        tilName.setError(null);
        tilVisitors.setError(null);

        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String visitorsText = etVisitors.getText() == null ? "" : etVisitors.getText().toString().trim();

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.error_name));
            valid = false;
        }
        if (visitDate == 0) {
            tilDate.setError(getString(R.string.error_date));
            valid = false;
        } else if (!checkClosedDay()) {
            valid = false;
        }
        if (TextUtils.isEmpty(visitorsText)) {
            tilVisitors.setError(getString(R.string.error_visitors));
            valid = false;
        }
        if (!valid) return;

        int visitors = Integer.parseInt(visitorsText);
        if (visitors > 50) {
            tilVisitors.setError(getString(R.string.error_visitors_max));
            return;
        }

        String ticketType = rgTicket.getCheckedRadioButtonId() == R.id.rb_foreign
                ? Trip.TICKET_FOREIGN : Trip.TICKET_INDIAN;
        double total = FeeCalculator.total(site, ticketType, visitors,
                cbGuide.isChecked(), cbAudio.isChecked());

        // The summary splits the cost per person, so 0 visitors would be a division by zero.
        try {
            FeeCalculator.perPerson(total, visitors);
        } catch (ArithmeticException e) {
            Toast.makeText(this, R.string.error_divide_zero, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, VisitSummaryActivity.class)
                .putExtra(VisitSummaryActivity.EXTRA_SITE_ID, site.id)
                .putExtra(VisitSummaryActivity.EXTRA_TRIP_ID, tripId)
                .putExtra(VisitSummaryActivity.EXTRA_NAME, name)
                .putExtra(VisitSummaryActivity.EXTRA_DATE, visitDate)
                .putExtra(VisitSummaryActivity.EXTRA_VISITORS, visitors)
                .putExtra(VisitSummaryActivity.EXTRA_TICKET, ticketType)
                .putExtra(VisitSummaryActivity.EXTRA_GUIDE, cbGuide.isChecked())
                .putExtra(VisitSummaryActivity.EXTRA_AUDIO, cbAudio.isChecked());
        startActivity(intent);
    }

    // The date picker works in UTC midnight; trips store local midnight.

    private static long utcToLocal(long utcMillis) {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.setTimeInMillis(utcMillis);
        Calendar local = Calendar.getInstance();
        local.clear();
        local.set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH));
        return local.getTimeInMillis();
    }

    private static long localToUtc(long localMillis) {
        Calendar local = Calendar.getInstance();
        local.setTimeInMillis(localMillis);
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        utc.set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH));
        return utc.getTimeInMillis();
    }
}
