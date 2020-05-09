# jmemcached-server

<h5>Установка сервера как Windows сервиса</h5>
<ol>
    <li>Выполнить mvn clean package сервера</li>
    <li>Создать рабочую директорию[HOME]</li>
    <li>Установить свои значения переменных в {projectDir}/service.installService.bat
        <ul>
            <li>SERVICE_HOME</li>
            <li>PR_JVM</li>
        </ul>
    </li>
    <li>Скопировать {projectDir}/service/prunsrv.exe -> HOME/bin/</li>
    <li>Скопировать {projectDir}/service/installService.bat  -> HOME/scripts/</li>
    <li>Скопировать {projectDir}/service/uninstallService.bat  -> HOME/scripts</li>
    <li>Скопировать {projectDir}/target/jmemcached-server-production-${project.version}.jar -> HOME/</li>
    <li>Запустить cmd под Администратором, перейти в HOME</li>
    <li>Выполнить .\installService.bat</li>
    <li>Перейти в оснастку services.msc, запустить сервис</li>
</ol>

<h5>Удаление windows сервиса</h5>
<ol>
    <li>Запустить cmd под Администратором, перейти в HOME</li>
    <li>Выполнить .\uninstallService.bat</li>
</ol>

<h5>Описание проекта</h5>
<ul>
    <li>
        основные компоненты
        <ul>
            <li>диспетчер соединений</li>
            <li>pool рабочих потоков</li>
            <li>парсер запроса</li>
            <li>диспетчер обработки комманд</li>
        </ul>
    </li>
    <li>данные хранятся в оперативной памяти в виде хэш-таблицы в виде массива байт с автоматическим удалением устаревших записей</li>
    <li>устанавливается как Windows сервис</li>
    <li>конфигурация настраивается в server.properties</li>
    <li>
        диспетчер соединений определяет ситуации превышения максимально допустимого 
        количества подключений и бросает исключение RejectedExecutionException
    </li>
    <li>
        в реализациях интерфейсов используются protected методы для создания компонентов для того, чтобы упростить 
        создание моков, тестирование + возможность изменить способ хранения данных
    </li>
    <li>в DefaultStorage внутренние классы static сделаны для организации слабой связи -> удобства тестирования</li>
    <li>в ConcurrentHashMap исключена возможность возникновения ConcurrentModificationException</li>
</ul>