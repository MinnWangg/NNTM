package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PopupManager {

    private Context context;
    private Sheets sheetsService;
    private String spreadsheetId = "1zZi8rKEgIWFsgs98PCOxiXldqpXgl7KW-69CREtL-rI";

    public PopupManager(Context context, Sheets sheetsService) {
        this.context = context;
        this.sheetsService = sheetsService;
    }

    public void showNotePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_note, null);
        builder.setView(popupView);

        EditText maKhachHang = popupView.findViewById(R.id.edit_text_ma_khach_hang);
        EditText tenCayTrong = popupView.findViewById(R.id.edit_text_name);
        Button btnSave = popupView.findViewById(R.id.btn_save);
        Button btnCancel = popupView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String maKhach = maKhachHang.getText().toString();
            String tenCay = tenCayTrong.getText().toString();

            if (!maKhach.isEmpty() && !tenCay.isEmpty()) {
                savePlantToGoogleSheet(tenCay, maKhach, tenCay);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void savePlantToGoogleSheet(String id, String maKhachHang, String tenCay) {
        new Thread(() -> {
            try {
                // Chuẩn bị dữ liệu để thêm vào Google Sheets
                List<List<Object>> rowData = new ArrayList<>();
                rowData.add(Arrays.asList(id, maKhachHang, tenCay));

                // Tạo một giá trị Range mới
                ValueRange appendBody = new ValueRange().setValues(rowData);

                sheetsService.spreadsheets().values()
                        .append(spreadsheetId, "Note!A2", appendBody)
                        .setValueInputOption("RAW")
                        .execute();

                ((MainActivity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Lưu thông tin thành công", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                ((MainActivity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
