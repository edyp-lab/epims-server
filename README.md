# epims-server

Rest server (Springboot) and JMS server (ActiveMQ) linked to ePims database (Hibernate)

Previously hosted on CEA Tuleap Projects.

## Usage

Before running `start_server.bat` you should

1.  be sure **Java 17 is installed** and set as default or modify start_server.bat with path to Java 17: change `java` by `<path_to_java>/bin/java`  
<br>
2. **configure epims-server** using the following files (in config folder):
 
* EpimsServerPreferences.properties
<pre>
 REPOSITORY_DEFAULT_ABC_LETTER=a : define in which sub repository project are created by default
 PIMS_ROOT=[PATH_TO_ROOT] : Path to epims root folder
 PIMS_SYSTEM_RELATIVE_PATH=[system] : relative path (from root) to system folder
 ARCHIVE_ROOT=[PATH_TO_ARCHIVE_ROOT] :  path to archive folder, used by server
 FTP_HOST=[PIMS_FTP_HOST]/FTP_LOG=[PIMS_FTP_USER]/FTP_PASSWORD=[PIMS_FTP_PWD] : FTP login information
 JMS_HOST=[...] : Use local jms server: **DO NOT CHANGE**
</pre>

*  application.properties
<pre>
############  PROPERTIES TO BE CHANGED  !!!!
#SETTINGS To acces ePims datastore
spring.datasource.url= jdbc:postgresql://[PG_HOST]:5432/[PimsDB]
spring.datasource.username= [PIMS_LOGIN]
spring.datasource.password= [PIMS_PWD]

#ePims paths
#epims.repository path to root of epims repository
epims.repository=/path/epims/data/repository
#epims.ftp.home path to entry point for FTP server
epims.ftp.home=epims_repo/repository

#Mandatory to connect to server. A Unique Key to authentify client 
jwt.secret=[A UNIQUE KEY]
############  PROPERTIES TO BE CHANGED END
</pre> 


## TODO

* merge some common properties between application.properties & EpimsServerPreferences.properties

## Releases
 
### Version 3.2.x

* Modify Build to add script and config file access
* Add controller for instrument Log file append
* Improve MGF controller to allow user to specify acquisition name
* Allow FTP port definition 
* Migrate to GitHub
* Upgrade Java to version 17, springboot to 3.5.5 and all dependencies

### Version 3.0.4 - 3.0.9 

* mgf upload support
* Archivage added
* 
### Version 3.0.2 - 3.0.4

* Bugs fixe 
* Added acquisition statistics
* Finalize analyses request management

### Version 3.0.0

* ePims Server based on SpringBoot. 
* First implementation of analyses request management 
