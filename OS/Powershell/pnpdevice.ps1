Get-PnpDevice | Out-File -FilePath .\result.txt
Get-PnpDevice | Select-Object -Property InstanceId | Out-File -FilePath .\result.txt
Get-PnpDevice | Select-Object -Property FriendlyName, Status, InstanceId | Out-File -FilePath .\result.txt
