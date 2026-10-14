package com.example.heritage.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.heritage.R;
import com.example.heritage.data.DbHelper;
import com.example.heritage.data.NotesStore;
import com.example.heritage.data.Site;
import com.example.heritage.util.Ui;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;

/** Personal notes for a site, written to and read from a text file (Practical 4B). */
public class NotesActivity extends AppCompatActivity {

    private static final String EXTRA_SITE_ID = "site_id";

    private Site site;
    private NotesStore store;
    private EditText etNotes;
    private TextView tvMeta;
    private MaterialToolbar toolbar;
    private String savedText = "";

    public static Intent intent(Context context, long siteId) {
        return new Intent(context, NotesActivity.class).putExtra(EXTRA_SITE_ID, siteId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        site = DbHelper.get(this).getSite(getIntent().getLongExtra(EXTRA_SITE_ID, -1));
        if (site == null) {
            finish();
            return;
        }
        store = new NotesStore(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notes);
        Ui.padForSystemBars(findViewById(R.id.bottom_bar), false, true);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(site.name);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                confirmDelete();
                return true;
            }
            return false;
        });

        etNotes = findViewById(R.id.et_notes);
        tvMeta = findViewById(R.id.tv_meta);
        findViewById(R.id.btn_save).setOnClickListener(v -> save(false));

        load(savedInstanceState == null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hasUnsavedChanges()) {
                    new MaterialAlertDialogBuilder(NotesActivity.this)
                            .setTitle(R.string.unsaved_title)
                            .setMessage(R.string.unsaved_message)
                            .setNegativeButton(R.string.discard, (d, w) -> finish())
                            .setPositiveButton(R.string.save, (d, w) -> save(true))
                            .show();
                } else {
                    finish();
                }
            }
        });
    }

    /** Reads the notes file. On rotation the EditText restores its own text instead. */
    private void load(boolean fillEditor) {
        try {
            savedText = store.read(site.id);
        } catch (IOException e) {
            savedText = "";
            Toast.makeText(this, R.string.notes_error, Toast.LENGTH_SHORT).show();
        }
        if (fillEditor) {
            etNotes.setText(savedText);
            etNotes.setSelection(savedText.length());
        }
        updateMeta();
    }

    private void save(boolean finishAfter) {
        String text = etNotes.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, R.string.notes_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            store.write(site.id, text);
            savedText = text;
            updateMeta();
            Toast.makeText(this, R.string.notes_saved, Toast.LENGTH_SHORT).show();
            if (finishAfter) finish();
        } catch (IOException e) {
            Toast.makeText(this, R.string.notes_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_notes_title)
                .setMessage(getString(R.string.delete_notes_message, site.name))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    store.delete(site.id);
                    savedText = "";
                    etNotes.setText(null);
                    updateMeta();
                    Toast.makeText(this, R.string.notes_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void updateMeta() {
        boolean exists = store.exists(site.id);
        tvMeta.setText(exists
                ? getString(R.string.notes_last_saved, Ui.dateTime(store.lastModified(site.id)))
                : getString(R.string.notes_not_saved));
        toolbar.getMenu().findItem(R.id.action_delete).setVisible(exists);
    }

    private boolean hasUnsavedChanges() {
        return !etNotes.getText().toString().trim().equals(savedText);
    }
}
