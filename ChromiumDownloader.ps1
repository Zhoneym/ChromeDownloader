function Get-LatestChromiumVersion($platform) {
    $url = "https://storage.googleapis.com/chromium-browser-snapshots/" + $platform + "/LAST_CHANGE"
    return Invoke-RestMethod -Uri $url
}

function Get-LatestChromiumVersion($platform) {
    $url = "https://storage.googleapis.com/chromium-browser-snapshots/" + $platform + "/LAST_CHANGE"
    return Invoke-RestMethod -Uri $url
}

function Get-DownloadUrl($platform, $file) {
    $version = Get-LatestChromiumVersion $platform
    return "https://storage.googleapis.com/chromium-browser-snapshots/" + $platform + "/" + $version + "/" + $file
}

$fetcher = @{
    "1" = @{"platform" = "Win_x64"; "file" = "mini_installer.exe"};
    "2" = @{"platform" = "Linux_x64"; "file" = "chrome-linux.zip"};
    "3" = @{"platform" = "Mac"; "file" = "chrome-mac.zip"};
    "4" = @{"platform" = "Android"; "file" = "chrome-android.zip"};
}

Write-Output "Please select the Chromium version you want to download:"
Write-Output "1. Windows_x64"
Write-Output "2. Linux_x64"
Write-Output "3. MacOS"
Write-Output "4. Android"

$choice = Read-Host -Prompt 'Please enter your choice (1, 2, 3, 4)'
$platform = $fetcher[$choice]["platform"]
$file = $fetcher[$choice]["file"]
$url = Get-DownloadUrl $platform $file
Write-Output ("The download link for the latest " + $platform + " Chromium version is: " + $url)

$download = Read-Host -Prompt 'Do you want to download this file? (Yes/no)'
if ($download -match "^(Y|y|Yes|yes|YES)$") {
    $filename = $platform.ToLower() + "_latest_" + $file
    Invoke-WebRequest -Uri $url -OutFile $filename
    Write-Output ("The file has been downloaded to the current folder. The filename is: " + $filename)
}
