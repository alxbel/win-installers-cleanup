# Win Installers Cleanup
DESCRIPTION

WinInstallersCleanup is a tool for:

* Identify orphaned patches and files in %WINDIR%\Installer folder 
* Backup those files to disk drive different from system drive
* Delete orphaned patches and files

Whereby the system drive space will be freed up to several GYGABYTES on older windows 
installations. This tool works only with system drives labeled as "C:\". 



USAGE

To execute, RMB on Start.cmd and choose 'Run as administrator'. If backup is needed, check
the box and select backup folder. Then hit 'Scan' to identify orphaned patches. If found,
the amount and total size (in megabytes) will be displayed. Hit 'Delete' to delete files. If
backup was checked, the files will be copied first.
