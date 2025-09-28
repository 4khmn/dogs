@echo off
chcp 65001

REM --- Переход в папку проекта ---
cd /d C:\Users\PC_DENIS\IdeaProjects\dogs

REM --- Пути ---
set CATALINA_HOME=C:\Users\PC_DENIS\Desktop\apache-tomcat-10.1.44
set PROJECT_WAR=target\ROOT.war

REM --- Сборка проекта через Maven ---
echo [1/4] Сборка проекта через Maven...
call mvn clean package

if %errorlevel% neq 0 (
    echo Ошибка сборки! Прекращение работы.
    pause
    exit /b
)

REM --- Удаление старого ROOT приложения ---
echo [2/4] Удаление старого ROOT приложения...
del /Q "%CATALINA_HOME%\webapps\ROOT.war"
rmdir /S /Q "%CATALINA_HOME%\webapps\ROOT"

REM --- Копирование нового WAR ---
echo [3/4] Копирование нового WAR в Tomcat...
copy /Y "%PROJECT_WAR%" "%CATALINA_HOME%\webapps\ROOT.war"

REM --- Перезапуск Tomcat ---
echo [4/4] Перезапуск Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat"
timeout /t 3
call "%CATALINA_HOME%\bin\startup.bat"

echo Деплой и запуск завершены.
pause