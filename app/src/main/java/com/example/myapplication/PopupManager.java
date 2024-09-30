package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;


import com.example.myapplication.databinding.PopupNoteBinding;

public class PopupManager {

    private final Context context;

    public PopupManager(Context context) {
        this.context = context;
    }

    // Phương thức để hiển thị popup cho NoteFragment
    public void showNotePopup() {
        // Sử dụng AlertDialog để tạo popup
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate layout cho popup từ file XML (popup_note.xml)
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_note, null);
        // Liên kết các view trong popup với mã Java
        TextInputEditText maKhachHangEditText = popupView.findViewById(R.id.edit_text_ma_khach_hang);
        TextInputEditText nameEditText = popupView.findViewById(R.id.edit_text_name);
        TextView plantingDateTextView = popupView.findViewById(R.id.edit_text_planting_date);
        TextView harvestDateTextView = popupView.findViewById(R.id.edit_text_harvest_date);
        TextView fertilizationDateTextView = popupView.findViewById(R.id.edit_text_fertilization_date);
        TextView pesticideDateTextView = popupView.findViewById(R.id.edit_text_pesticide_date);
        TextInputEditText wateringEditText = popupView.findViewById(R.id.edit_text_watering);
        TextInputEditText stopWateringEditText = popupView.findViewById(R.id.edit_text_stop_watering);

        // Cài đặt View cho AlertDialog
        builder.setView(popupView);

        // Hiển thị AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_background);
    }

}
