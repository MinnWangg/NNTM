package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.google.api.services.sheets.v4.Sheets;

import java.io.InputStream;
import java.util.List;

import API.SheetsServiceUtil;

public class CropFragment extends Fragment {

    private static final String SPREADSHEET_ID = "1zZi8rKEgIWFsgs98PCOxiXldqpXgl7KW-69CREtL-rI";
    private static final String RANGE = "CayTrong!A1:A"; // Cột chứa ID cây

    private LinearLayout caytrongLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop, container, false);

        caytrongLayout = view.findViewById(R.id.Caytrong); // Cập nhật ID cho LinearLayout

        // Khởi tạo Google Sheets service
        try {
            InputStream credentialsStream = getResources().openRawResource(R.raw.service_account); // Đảm bảo bạn đã tạo tệp credentials.json và đặt trong thư mục res/raw
            Sheets sheetsService = SheetsServiceUtil.getSheetsService(credentialsStream);
            loadPlantData(sheetsService);
        } catch (Exception e) {
            Log.e("CropFragment", "Error initializing Sheets service: " + e.getMessage());
        }

        return view;
    }

    private void loadPlantData(Sheets sheetsService) {
        new Thread(() -> {
            try {
                // Lấy dữ liệu từ Google Sheets
                List<List<Object>> data = SheetsServiceUtil.getDataFromSheet(sheetsService, SPREADSHEET_ID, RANGE);

                // Hiển thị dữ liệu trên giao diện
                if (data != null && !data.isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        caytrongLayout.removeAllViews(); // Xóa các View cũ trước khi thêm mới
                        for (List<Object> row : data) {
                            String plantId = row.get(0).toString(); // ID cây

                            // Tạo CardView cho từng cây
                            View cardView = LayoutInflater.from(getActivity()).inflate(R.layout.list_crop_layout, null);
                            TextView textView = cardView.findViewById(R.id.TextView);
                            textView.setText(plantId); // Hiển thị tên cây

                            // Lấy bảng và thiết lập hiển thị
                            LinearLayout popupPanel = cardView.findViewById(R.id.popupPanel);
                            popupPanel.setVisibility(View.GONE); // Ẩn bảng ban đầu

                            // Xử lý sự kiện cho nút mũi tên xuống
                            ImageButton downArrowButton = cardView.findViewById(R.id.menuButton1);
                            downArrowButton.setOnClickListener(v -> {
                                if (popupPanel.getVisibility() == View.GONE) {
                                    popupPanel.setVisibility(View.VISIBLE); // Hiện bảng
                                } else {
                                    popupPanel.setVisibility(View.GONE); // Ẩn bảng
                                }
                            });

                            // Thiết lập các nút trong bảng
                            Button applyButton = popupPanel.findViewById(R.id.buttonApDung);
                            applyButton.setText("ÁP DỤNG"); // Đặt tên ban đầu
                            applyButton.setOnClickListener(v -> {
                                if (applyButton.getText().equals("ÁP DỤNG")) {
                                    applyButton.setText("NGỪNG ÁP DỤNG"); // Đổi thành "NGỪNG ÁP DỤNG"
                                    Toast.makeText(getActivity(), "Đã áp dụng", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Hiển thị hộp thoại xác nhận ngừng áp dụng
                                    new AlertDialog.Builder(getActivity())
                                            .setTitle("Xác nhận ngừng áp dụng")
                                            .setMessage("Bạn có chắc chắn muốn ngừng áp dụng không?")
                                            .setPositiveButton("Có", (dialog, which) -> {
                                                applyButton.setText("ÁP DỤNG"); // Đổi lại thành "ÁP DỤNG"
                                                Toast.makeText(getActivity(), "Đã ngừng áp dụng", Toast.LENGTH_SHORT).show();
                                            })
                                            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss()) // Đóng dialog
                                            .show();
                                }
                            });

                            Button detailsButton = popupPanel.findViewById(R.id.buttonChiTiet);
                            detailsButton.setOnClickListener(v -> {
                                Toast.makeText(getActivity(), "CHI TIẾT", Toast.LENGTH_SHORT).show();
                                popupPanel.setVisibility(View.GONE); // Ẩn bảng sau khi nhấn nút
                            });

                            Button deleteButton = popupPanel.findViewById(R.id.buttonXoa);
                            deleteButton.setOnClickListener(v -> {
                                // Hiển thị hộp thoại xác nhận xóa
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Xác nhận xóa")
                                        .setMessage("Bạn có chắc chắn muốn xóa không?")
                                        .setPositiveButton("XÓA", (dialog, which) -> {
                                            // Xóa cây từ Google Sheets
                                            try {
                                                SheetsServiceUtil.deletePlantFromSheet(sheetsService, SPREADSHEET_ID, plantId);
                                                Toast.makeText(getActivity(), "Đã xóa cây " + plantId, Toast.LENGTH_SHORT).show();
                                                caytrongLayout.removeView(cardView); // Xóa CardView khỏi layout
                                            } catch (Exception e) {
                                                Log.e("CropFragment", "Error deleting plant: " + e.getMessage());
                                                Toast.makeText(getActivity(), "Xóa cây không thành công!", Toast.LENGTH_SHORT).show();
                                            }
                                            popupPanel.setVisibility(View.GONE); // Ẩn bảng sau khi nhấn nút
                                        })
                                        .setNegativeButton("KHÔNG", (dialog, which) -> {
                                            dialog.dismiss(); // Đóng dialog
                                        })
                                        .show();
                            });

                            // Thêm CardView vào layout
                            caytrongLayout.addView(cardView);
                        }
                    });
                } else {
                    Log.e("CropFragment", "No data found in Google Sheets");
                }
            } catch (Exception e) {
                Log.e("CropFragment", "Error loading plant data: " + e.getMessage());
            }
        }).start();
    }
}
