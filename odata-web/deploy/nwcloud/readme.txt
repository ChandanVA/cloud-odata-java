====
    (c) 2013 by SAP AG
====

Used by deploy build job on Jenkins:

http://libwww.wdf.sap.corp:8080/jenkins/job/com.sap.core.odata.deploy/   
	additional execution step running shell script
	Commando: odata-web\deploy\nwcloud\deploy.ref.cmd
	
Jenkins has to define env variable (depends on libwww server):

NEO_SDK_HOME=c:\.neosdk\neo-sdk-1.17.0

http://libwww.wdf.sap.corp:8080/jenkins/configure