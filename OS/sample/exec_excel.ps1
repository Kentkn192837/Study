$filepath = Get-Content ".\excel.file.path"
$tables = Get-Content ".\tables.txt" | Where-Object { $_.Trim() -ne "" }


$excel = New-Object -ComObject Excel.Application
$excel.Visible = $false
$excel.DisplayAlerts = $false

$workbook = $excel.Workbooks.Open($filepath)
$sheet = $workbook.Sheets.Item("TableList")

foreach ($table in $tables) {
    foreach ($cell in $sheet.Range("C5:C13")) {
        if ($cell.Value2 -eq $table) {
            $sheet.Cells.Item($cell.Row, 7).Value2 = 1  # G列
        }
    }
}


$workbook.Save()
$excel.Run("SampleMacro")
$workbook.Close($true)
$excel.Quit()

[System.Runtime.InteropServices.Marshal]::ReleaseComObject($sheet) | Out-Null
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($workbook) | Out-Null
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($excel) | Out-Null
