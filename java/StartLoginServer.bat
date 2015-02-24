@echo off
title L2Phoenix: Login Server Console
:start
echo %DATE% %TIME% Login server is running !!! > login_is_running.tmp
echo Starting L2P Login Server.
echo.
java -server -Xms32m -Xmx32m -cp javolution.jar;c3p0-0.9.1.2.jar;mysql-connector-java-bin.jar;l2pserver.jar;jacksum.jar l2p.loginserver.L2LoginServer
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
del login_is_running.tmp
pause