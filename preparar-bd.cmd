@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\preparar-bd.ps1" %*
