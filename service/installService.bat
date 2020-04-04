set SERVICE_NAME=JMemcachedServer
set SERVICE_HOME=E:\programms\JMemcachedServer
set PR_JVM=C:\Program Files\Java\jre1.8.0_241\bin\server\jvm.dll

set PR_INSTALL=%SERVICE_HOME%\bin\prunsrv.exe
 
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%SERVICE_HOME%\logs
set PR_STDOUTPUT=%SERVICE_HOME%\logs\stdout.txt
set PR_STDERROR=%SERVICE_HOME%\logs\stderr.txt
set PR_LOGLEVEL=Error

set PR_SERVER_VERSION=0.0.1
set PR_CLASSPATH=%SERVICE_HOME%\jmemcached-server-production-%PR_SERVER_VERSION%.jar
set PR_JVMMS=256
set PR_JVMMX=1024
set PR_JVMSS=4000
set PR_JVMOPTIONS=-Dserver-prop=%SERVICE_HOME%\conf\server.properties
 
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=ru.dev.jmemcached.server.ServiceWrapper
set PR_STARTMETHOD=start
set PR_STOPMODE=jvm
set PR_STOPCLASS=ru.dev.jmemcached.server.ServiceWrapper
set PR_STOPMETHOD=stop

..\bin\prunsrv.exe //IS//%SERVICE_NAME%