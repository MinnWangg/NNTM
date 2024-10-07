package com.example.myapplication;

import Data.PlantInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.myapplication.databinding.PopupNoteBinding;

public class PopupManager {

    private final Context context;

    public PopupManager(Context context) {
        this.context = context;
    }

    // Phương thức để hiển thị popup cho NoteFragment
    public void showNotePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_note, null);

        TextInputEditText maKhachHangEditText = popupView.findViewById(R.id.edit_text_ma_khach_hang);
        TextInputEditText nameEditText = popupView.findViewById(R.id.edit_text_name);
        Button btnSave = popupView.findViewById(R.id.btn_save);
        Button btnCancel = popupView.findViewById(R.id.btn_cancel);

        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_background);

        btnSave.setOnClickListener(v -> {
            String maKhachHang = maKhachHangEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();

            if (maKhachHang.isEmpty() || name.isEmpty()) {
                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference plantsRef = database.getReference("Plants");

                // Sử dụng tên cây làm ID
                String plantId = name;


                PlantInfo plant = new PlantInfo(maKhachHang, plantId);


                plant.setId(plantId);
                Log.d("PopupManager", "Plant ID: " + plantId);
                plantsRef.child(plantId).setValue(plant).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("PopupManager", "Plant saved with ID: " + plantId);
                        Toast.makeText(context, "Dữ liệu đã được lưu thành công!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Lỗi khi lưu dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }



}
