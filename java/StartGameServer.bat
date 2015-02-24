@echo off
title L2Phoenix: Game Server Console 
:start
set user=root
set pass=
set DBname=l2pdb
set DBHost=localhost
set ctime=%TIME:~0,2%
if "%ctime:~0,1%" == " " (
set ctime=0%ctime:~1,1%
)
set ctime=%ctime%'%TIME:~3,2%'%TIME:~6,2%
echo.
echo Making a full backup into %DATE%-%ctime%_backup_full.sql
echo.
mysqldump.exe %Ignore% --add-drop-table -h %DBHost% -u %user% --password=%pass% %DBname% > backup/%DATE%-%ctime%_backup_full.sql
echo.
echo Backup complite %DATE%-%ctime%_backup_full.sql
echo.
echo %DATE% %TIME% Game server is running !!! > gameserver_is_running.tmp
echo Starting L2P Game Server.
echo.
rem ======== Optimize memory settings =======
rem Minimal size with geodata is 1.5G, w/o geo 1G
rem Make sure -Xmn value is always 1/4 the size of -Xms and -Xmx.
rem -Xms and -Xmx should always be equal.
rem ==========================================
java -server -Dfile.encoding=UTF-8 -Xms1024m -Xmx1024m -cp bsf.jar;bsh-2.0.jar;javolution.jar;c3p0-0.9.1.2.jar;mysql-connector-java-bin.jar;l2pserver.jar;jython.jar;rrd4j-2.0.5.jar;jacksum.jar l2p.gameserver.GameServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
del gameserver_is_running.tmp
pause
