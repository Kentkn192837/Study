import java.sql.*;

public class Main {
    public static void main(String[] args) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT dept_code, dept_name FROM department";
        try {
            // JDBCドライバを明示的にロード
            Class.forName("com.mysql.cj.jdbc.Driver");

            // localhostではなく、mysqlサービス名を使用
            String url = "jdbc:mysql://mysql:3306/testdb?verifyServerCertificate=false&useSSL=false&serverTimezone=JST";
            con = DriverManager.getConnection(url, "user", "password");
            pstmt = con.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("dept_code:" + rs.getInt(1));
                System.out.println("dept_name:" + rs.getString(2));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }

                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
