package com.example.myapplication;
import API.SheetsServiceUtil;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class PopupManager {

    private Context context;
    private SheetsServiceUtil sheetsServiceUtil;

    // Constructor để nhận context từ MainActivity
    public PopupManager(Context context) {
        this.context = context;
    }

    // Hàm để hiển thị popup_note
    public void showNotePopup() {
        // Khởi tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate layout popup_note.xml
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_note, null);
        builder.setView(popupView);

        // Tìm các View trong popup_note.xml
        EditText maKhachHang = popupView.findViewById(R.id.edit_text_ma_khach_hang);
        EditText tenCayTrong = popupView.findViewById(R.id.edit_text_name);
        Button btnSave = popupView.findViewById(R.id.btn_save);
        Button btnCancel = popupView.findViewById(R.id.btn_cancel);

        // Khởi tạo dialog từ builder
        AlertDialog dialog = builder.create();

        // Thiết lập hành động cho nút Hủy
        btnCancel.setOnClickListener(v -> {
            // Đóng dialog
            dialog.dismiss();
        });

        // Thiết lập hành động cho nút Lưu
        btnSave.setOnClickListener(v -> {
            // Lấy thông tin từ EditText
            String maKhach = maKhachHang.getText().toString();
            String tenCay = tenCayTrong.getText().toString();

            // Thực hiện lưu hoặc thao tác khác
            Toast.makeText(context, "Lưu: " + maKhach + ", " + tenCay, Toast.LENGTH_SHORT).show();
            dialog.dismiss(); // Đóng dialog sau khi lưu
        });

        // Hiển thị popup
        dialog.show();
    }
}
