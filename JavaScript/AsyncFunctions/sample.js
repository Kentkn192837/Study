// ActiveXObjectを使用してファイル操作、Excel操作、HTTPリクエストを行うサンプルコード
var fso = new ActiveXObject("Scripting.FileSystemObject");
var file = fso.CreateTextFile("C:\\example.txt", true);
file.WriteLine("Hello, ActiveXObject!");
file.Close();

// Excel操作
var excel = new ActiveXObject("Excel.Application");
excel.Visible = true;
var workbook = excel.Workbooks.Add();
var sheet = workbook.ActiveSheet;
sheet.Cells(1, 1).Value = "Hello, Excel!";

// HTTPリクエスト
var xhr = new ActiveXObject("MSXML2.XMLHTTP");
xhr.open("GET", "https://example.com", false);
xhr.send();
if (xhr.status === 200) {
    console.log(xhr.responseText);
}
