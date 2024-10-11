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
    public static void deletePlantFromSheet(Sheets service, String spreadsheetId, String plantId) throws IOException {
        // Tìm hàng chứa plantId
        int rowIndex = findRowIndexByPlantId(service, spreadsheetId, plantId);

        if (rowIndex != -1) {
            // Tạo yêu cầu xóa hàng
            List<Request> requests = new ArrayList<>();
            requests.add(new Request()
                    .setDeleteDimension(new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(0) // ID của sheet, 0 là sheet đầu tiên
                                    .setDimension("ROWS")
                                    .setStartIndex(rowIndex) // Vị trí hàng bắt đầu
                                    .setEndIndex(rowIndex + 1) // Vị trí hàng kết thúc
                            )));

            // Thực hiện yêu cầu xóa
            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest().setRequests(requests);
            service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute();
        }
    }
    private static int findRowIndexByPlantId(Sheets service, String spreadsheetId, String plantId) {
        try {
            List<List<Object>> rows = getDataFromSheet(service, spreadsheetId, "Note!A2:A"); // Lấy dữ liệu cột chứa plantId

            // Kiểm tra xem rows có null hoặc trống không
            if (rows == null || rows.isEmpty()) {
                Log.e("findRowIndex", "No data found in the specified range.");
                return -1;
            }

            for (int i = 0; i < rows.size(); i++) {

                if (!rows.get(i).isEmpty() && rows.get(i).get(0) != null && rows.get(i).get(0).toString().equals(plantId)) {
                    return i + 2;
                }
            }
        } catch (IOException e) {
            Log.e("findRowIndex", "Error retrieving data from sheet: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("findRowIndex", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }




    public static void deleteRow(Sheets service, String spreadsheetId, int rowIndex) throws IOException {
        List<Request> requests = new ArrayList<>();
        requests.add(new Request()
                .setDeleteDimension(new DeleteDimensionRequest()
                        .setRange(new DimensionRange()
                                .setSheetId(0)
                                .setDimension("ROWS")
                                .setStartIndex(rowIndex - 1)
                                .setEndIndex(rowIndex))));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        service.spreadsheets().batchUpdate(spreadsheetId, body).execute();
    }









}
