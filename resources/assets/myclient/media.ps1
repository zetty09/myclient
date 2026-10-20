param([string]$CmdFile, [string]$CoverFile)
$ErrorActionPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Runtime.WindowsRuntime
Add-Type -AssemblyName System.Drawing
$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
function Await($op, $type) {
    $m = $asTask.MakeGenericMethod($type)
    $t = $m.Invoke($null, @($op))
    [void]$t.Wait(-1)
    $t.Result
}
[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]
[void][Windows.Storage.Streams.IRandomAccessStreamWithContentType, Windows.Storage.Streams, ContentType = WindowsRuntime]
$mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
if (-not $mgr) { Write-Output 'ERR'; exit }

# Zapisuje okladke (miniature z sesji multimediow) jako PNG 128x128 z zaokraglonymi rogami.
function Save-Cover($thumb, $path) {
    try {
        if (-not $thumb) { return $false }
        $stream = Await ($thumb.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])
        if (-not $stream -or $stream.Size -le 0) { return $false }
        $net = [System.IO.WindowsRuntimeStreamExtensions]::AsStreamForRead($stream)
        $ms = New-Object System.IO.MemoryStream
        $net.CopyTo($ms)
        $ms.Position = 0
        $img = [System.Drawing.Image]::FromStream($ms)
        $side = [Math]::Min($img.Width, $img.Height)
        [single]$sx = ($img.Width - $side) / 2
        [single]$sy = ($img.Height - $side) / 2
        [single]$sd = $side
        $bmp = New-Object System.Drawing.Bitmap 128, 128, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $g = [System.Drawing.Graphics]::FromImage($bmp)
        $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $gp = New-Object System.Drawing.Drawing2D.GraphicsPath
        $d = 40
        $gp.AddArc(0, 0, $d, $d, 180, 90)
        $gp.AddArc(128 - $d, 0, $d, $d, 270, 90)
        $gp.AddArc(128 - $d, 128 - $d, $d, $d, 0, 90)
        $gp.AddArc(0, 128 - $d, $d, $d, 90, 90)
        $gp.CloseFigure()
        $g.SetClip($gp)
        $dest = New-Object System.Drawing.Rectangle 0, 0, 128, 128
        $g.DrawImage($img, $dest, $sx, $sy, $sd, $sd, [System.Drawing.GraphicsUnit]::Pixel)
        $g.Dispose()
        $tmp = $path + '.tmp'
        $bmp.Save($tmp, [System.Drawing.Imaging.ImageFormat]::Png)
        $bmp.Dispose()
        $img.Dispose()
        Move-Item -Force $tmp $path
        return $true
    } catch {
        return $false
    }
}

$tab = [string][char]9
$inv = [System.Globalization.CultureInfo]::InvariantCulture
$tick = 0
$lastKey = ''
$coverId = 0
$hasCover = $false
$tries = 0
while ($true) {
    $s = $mgr.GetCurrentSession()
    if ($CmdFile -and (Test-Path $CmdFile)) {
        $cmd = (Get-Content $CmdFile -Raw).Trim()
        Remove-Item $CmdFile -Force
        if ($s) {
            switch ($cmd) {
                'toggle' { [void]$s.TryTogglePlayPauseAsync() }
                'next'   { [void]$s.TrySkipNextAsync() }
                'prev'   { [void]$s.TrySkipPreviousAsync() }
            }
        }
        $tick = 0
    }
    if ($tick % 3 -eq 0) {
        if ($s) {
            $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
            $pb = $s.GetPlaybackInfo()
            $tl = $s.GetTimelineProperties()
            $title = ([string]$p.Title).Replace($tab, ' ')
            $artist = ([string]$p.Artist).Replace($tab, ' ')
            $key = $title + '|' + $artist
            if ($key -ne $lastKey) { $lastKey = $key; $hasCover = $false; $tries = 0 }
            if (-not $hasCover -and $tries -lt 6 -and $CoverFile) {
                $tries++
                if (Save-Cover $p.Thumbnail $CoverFile) { $hasCover = $true; $coverId++ }
            }
            $cv = '0'
            if ($hasCover) { $cv = [string]$coverId }
            $pos = $tl.Position.TotalSeconds.ToString($inv)
            $end = $tl.EndTime.TotalSeconds.ToString($inv)
            Write-Output ($title + $tab + $artist + $tab + [string]$pb.PlaybackStatus + $tab + $pos + $tab + $end + $tab + [string]$s.SourceAppUserModelId + $tab + $cv)
        } else {
            Write-Output ($tab + $tab + 'None' + $tab + '0' + $tab + '0' + $tab + $tab + '0')
        }
    }
    $tick++
    Start-Sleep -Milliseconds 250
}
