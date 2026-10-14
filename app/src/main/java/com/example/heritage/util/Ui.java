package com.example.heritage.util;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.heritage.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Small view helpers shared by several screens. */
public final class Ui {

    private Ui() {
    }

    public static String date(long millis) {
        return new SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(new Date(millis));
    }

    public static String dateTime(long millis) {
        return new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()).format(new Date(millis));
    }

    /** Adds a "label ........ value" row (layout/item_info_row.xml) to a vertical container. */
    public static void addRow(LinearLayout container, String label, String value, boolean emphasis) {
        View row = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_info_row, container, false);
        TextView tvLabel = row.findViewById(R.id.tv_label);
        TextView tvValue = row.findViewById(R.id.tv_value);
        tvLabel.setText(label);
        tvValue.setText(value);
        if (emphasis) {
            tvLabel.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
            tvValue.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        }
        container.addView(row);
    }

    /** Pads a view by the system bar insets so content is not drawn under them (edge to edge). */
    public static void padForSystemBars(View view, boolean top, boolean bottom) {
        final int l = view.getPaddingLeft(), t = view.getPaddingTop();
        final int r = view.getPaddingRight(), b = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(l + bars.left, t + (top ? bars.top : 0),
                    r + bars.right, b + (bottom ? bars.bottom : 0));
            return insets;
        });
    }
}
