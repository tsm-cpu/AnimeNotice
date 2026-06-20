@echo off
setlocal

set MAVEN_VERSION=3.9.6
set MAVEN_DIR=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%

if not exist "%MAVEN_DIR%\bin\mvn.cmd" (
  echo Downloading Apache Maven %MAVEN_VERSION%...
  if not exist "%MAVEN_DIR%" (
    mkdir "%MAVEN_DIR%"
  )
  powershell -NoProfile -NonInteractive -Command "& { [Net.ServicePointManager]::SecurityProtocol = 'Tls12'; $url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; $out = '%TEMP%\maven.zip'; Invoke-WebRequest -Uri $url -OutFile $out; Expand-Archive $out -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force; Remove-Item $out }"
)

"%MAVEN_DIR%\bin\mvn.cmd" %*