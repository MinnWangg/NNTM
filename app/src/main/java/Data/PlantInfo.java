package Data;

public class PlantInfo {
    public String maKhachHang; // Mã Khách Hàng
    public String id;
    public String name; // Tên cây
    public String ngayTrong; // Ngày Trồng
    public String ngayThuHoach; // Ngày Thu Hoạch
    public String ngayBonPhan; // Ngày Bón Phân
    public String ngayPhunThuoc; // Ngày Phun Thuốc
    public String dkTuoi; // Điều kiện tưới
    public String dkDungTuoi; // Điều kiện dừng tưới

    // Constructor không tham số
    public PlantInfo() {
        // Cần thiết để Firebase sử dụng
    }

    // Constructor nhận tất cả tham số
    public PlantInfo(String maKhachHang, String id, String name, String ngayTrong, String ngayThuHoach,
                     String ngayBonPhan, String ngayPhunThuoc, String dkTuoi, String dkDungTuoi) {
        this.maKhachHang = maKhachHang;
        this.id = id;
        this.name = name;

        this.ngayTrong = ngayTrong;
        this.ngayThuHoach = ngayThuHoach;
        this.ngayBonPhan = ngayBonPhan;
        this.ngayPhunThuoc = ngayPhunThuoc;
        this.dkTuoi = dkTuoi;
        this.dkDungTuoi = dkDungTuoi;
    }

    // Constructor nhận chỉ hai tham số
    public PlantInfo(String maKhachHang, String name) {
        this.maKhachHang = maKhachHang;
        this.name = name;
        this.ngayTrong = null; // Giá trị mặc định
        this.ngayThuHoach = null; // Giá trị mặc định
        this.ngayBonPhan = null; // Giá trị mặc định
        this.ngayPhunThuoc = null; // Giá trị mặc định
        this.dkTuoi = null; // Giá trị mặc định
        this.dkDungTuoi = null; // Giá trị mặc định
    }

    public String getId() {
        return id; // Trả về ID
    }

    public void setId(String id) {
        this.id = id; // Gán ID
    }

    // Getter and Setter methods
    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNgayTrong() {
        return ngayTrong;
    }

    public void setNgayTrong(String ngayTrong) {
        this.ngayTrong = ngayTrong;
    }

    public String getNgayThuHoach() {
        return ngayThuHoach;
    }

    public void setNgayThuHoach(String ngayThuHoach) {
        this.ngayThuHoach = ngayThuHoach;
    }

    public String getNgayBonPhan() {
        return ngayBonPhan;
    }

    public void setNgayBonPhan(String ngayBonPhan) {
        this.ngayBonPhan = ngayBonPhan;
    }

    public String getNgayPhunThuoc() {
        return ngayPhunThuoc;
    }

    public void setNgayPhunThuoc(String ngayPhunThuoc) {
        this.ngayPhunThuoc = ngayPhunThuoc;
    }

    public String getDkTuoi() {
        return dkTuoi;
    }

    public void setDkTuoi(String dkTuoi) {
        this.dkTuoi = dkTuoi;
    }

    public String getDkDungTuoi() {
        return dkDungTuoi;
    }

    public void setDkDungTuoi(String dkDungTuoi) {
        this.dkDungTuoi = dkDungTuoi;
    }
}
