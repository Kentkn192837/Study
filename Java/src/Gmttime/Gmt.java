public class UTCTimeConversion {
    public static void main(String[] args) {
        // 初期のUTCタイム（例として 10000000 とする）
        long utcTime = 10000000;

        // エポックからの経過秒数を取得
        long elapsedSeconds = utcTime / 1000;

        // エポックからの経過秒数を基に年月日時分秒を計算
        int days = (int) (elapsedSeconds / (24 * 60 * 60));
        int secondsOfDay = (int) (elapsedSeconds % (24 * 60 * 60));

        int year = 1970;
        int month = 1;
        int day = 1;
        int hour = secondsOfDay / 3600;
        int minute = (secondsOfDay % 3600) / 60;
        int second = secondsOfDay % 60;

        // 年月日を計算
        while (days >= getDaysInYear(year)) {
            days -= getDaysInYear(year);
            year++;
        }

        while (days >= getDaysInMonth(year, month)) {
            days -= getDaysInMonth(year, month);
            month++;
        }

        day += days;

        // 曜日を計算
        int dayOfWeek = calculateDayOfWeek(year, month, day);

        // 結果の表示
        System.out.println("UTC Time: " + utcTime);
        System.out.println("Current Year: " + year);
        System.out.println("Current Month: " + month);
        System.out.println("Current Day: " + day);
        System.out.println("Current Time: " + hour + ":" + minute + ":" + second);
        System.out.println("Day of the Week: " + getDayOfWeek(dayOfWeek));
    }

    // うるう年かどうかを判定するメソッド
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // 年の日数を取得するメソッド
    private static int getDaysInYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    // 月の日数を取得するメソッド
    private static int getDaysInMonth(int year, int month) {
        int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (month == 2 && isLeapYear(year)) {
            return 29;
        }
        return daysInMonth[month];
    }

    // 曜日を計算するメソッド（0: 日曜日, 1: 月曜日, ..., 6: 土曜日）
    private static int calculateDayOfWeek(int year, int month, int day) {
        if (month < 3) {
            month += 12;
            year--;
        }
        int h = (day + (13 * (month + 1)) / 5 + year + year / 4 - year / 100 + year / 400) % 7;
        return (h + 5) % 7;
    }

    // 曜日の文字列を取得するメソッド
    private static String getDayOfWeek(int dayOfWeek) {
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        return days[dayOfWeek];
    }
}
