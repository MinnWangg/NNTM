package API;

import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SheetsServiceUtil {

    private static final String APPLICATION_NAME = "Plant Management App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public static Sheets getSheetsService(InputStream credentialsStream) throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(credentialsStream)
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        return new Sheets.Builder(new NetHttpTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    // Lấy dữ liệu
    public static List<List<Object>> getDataFromSheet(Sheets sheetsService, String spreadsheetId, String range) throws Exception {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }

    // Phương thức để cập nhật dữ liệu trong Google Sheets
    public static void updateDataInSheet(Sheets sheetsService, String spreadsheetId, String range,
                                         List<Object> data) throws Exception {
        ValueRange body = new ValueRange().setValues(Arrays.asList(data));
        sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public static void updateGoogleSheet(Sheets sheetsService, String spreadsheetId, String plantId,
                                         String plantNameSheet, String plantingDate, String harvestDate,
                                         String fertilizationDate, String pesticideDate,
                                         String wateringCondition, String stopWateringCondition) {
        new Thread(() -> {
            try {
                // Lấy danh sách các hàng trong sheet
                List<List<Object>> rows = getDataFromSheet(sheetsService, spreadsheetId, "Note!A2:A"); // Giả sử ID cây nằm ở cột A
                int rowIndex = -1;


                for (int i = 0; i < rows.size(); i++) {
                    if (rows.get(i).get(0).toString().equals(plantId)) {
                        rowIndex = i + 2;
                        break;
                    }
                }

                if (rowIndex != -1) {
                    // Tạo danh sách dữ liệu cần cập nhật
                    List<Object> data = Arrays.asList(plantNameSheet, plantingDate, harvestDate,
                            fertilizationDate, pesticideDate, wateringCondition, stopWateringCondition);

                    // Cập nhật dữ liệu vào hàng tương ứng từ cột B đến I
                    updateDataInSheet(sheetsService, spreadsheetId, "Note!C" + rowIndex + ":I" + rowIndex, data);
                } else {
                    System.out.println("Không tìm thấy cây với ID: " + plantId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static List<Object> getPlantInfo(Sheets sheetsService, String spreadsheetId, String plantId) throws Exception {
        // Lấy danh sách các hàng trong sheet
        List<List<Object>> rows = getDataFromSheet(sheetsService, spreadsheetId, "Note!A2:I"); // Lấy toàn bộ dữ liệu trong khoảng A2:I
        for (List<Object> row : rows) {
            if (row.get(0).toString().equals(plantId)) {
                return row;
            }
        }
        return null;
    }



    //Xóa dữ liệu
    public static void deletePlantFromSheet(Sheets sheetsService, String spreadsheetId, String plantId) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "Note!A:C")  // Lấy dữ liệu từ cột A đến C
                .execute();
        List<List<Object>> values = response.getValues();

        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                // Kiểm tra giá trị trong cột C (TenCay)
                if (values.get(i).size() > 2 && values.get(i).get(2).equals(plantId)) {

                    DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(1655091081)  // Sử dụng ID của sheet
                                    .setDimension("ROWS")
                                    .setStartIndex(i)
                                    .setEndIndex(i + 1)
                            );

                    List<Request> requests = new ArrayList<>();
                    requests.add(new Request().setDeleteDimension(deleteRequest));

                    BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);

                    try {
                        sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute();
                        System.out.println("Đã xóa cây: " + plantId);
                    } catch (IOException e) {
                        System.err.println("Không thể xóa cây: " + e.getMessage());
                    }
                    break;
                }
            }
        } else {
            System.out.println("Không tìm thấy cây nào để xóa.");
        }
    }
















}
