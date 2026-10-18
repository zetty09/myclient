param([string]$CmdFile)
$ErrorActionPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Runtime.WindowsRuntime
$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]
function Await($op, $type) {
    $m = $asTask.MakeGenericMethod($type)
    $t = $m.Invoke($null, @($op))
    [void]$t.Wait(-1)
    $t.Result
}
[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
[void][Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]
$mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
if (-not $mgr) { Write-Output 'ERR'; exit }
$tab = [string][char]9
$inv = [System.Globalization.CultureInfo]::InvariantCulture
$tick = 0
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
            $pos = $tl.Position.TotalSeconds.ToString($inv)
            $end = $tl.EndTime.TotalSeconds.ToString($inv)
            Write-Output ($title + $tab + $artist + $tab + [string]$pb.PlaybackStatus + $tab + $pos + $tab + $end + $tab + [string]$s.SourceAppUserModelId)
        } else {
            Write-Output ($tab + $tab + 'None' + $tab + '0' + $tab + '0' + $tab)
        }
    }
    $tick++
    Start-Sleep -Milliseconds 250
}
