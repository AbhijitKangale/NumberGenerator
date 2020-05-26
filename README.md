                                            “Number Generator” App Documentation

This application is designed using Spring Boot version 2.2.1.RELEASE and Java version 11. It is backward compatible with Java version till Java 1.8. API’s of this app are designed using Spring MVC pattern. This application is designed to work in asynchronous mode using multithreading such that it can concurrently handle multiple requests at a time. It basically uses “ThreadPoolTaskExecutor” as an asynchronous executer. As application involves more IO based operation, I have considered below formula to decide size of thread pool.

		  Approx thread pool size = No of CPU cores + (1+ (wait time/total CPU time))
			                  = 8 + (1 + (500/2500)) = 9.6 or 10 Approx

(Processor is Octa core processor and assumptions – wait time = 500 ms & total CPU time = 2500 ms)
With above configuration the application handled 15 simultaneous request when deployed as a single local instance (I have done quick performance test using Jmeter with random combinations of all 3 requests). It looks slow considering the local env, it can perform better when deployed in VM or container with better CPU specification. It can be scaled further by adding multiple instances based on the load. 

Application stores the status of the task in “In Memory” cache. It uses redis as “In Memory” cache. So we need to start redis server before running the application. The advantage with this approach, at this point in time is, if the application shuts down for some reason, the task status is still persisted in redis “In Memory”. If the redis server is stopped, we will lose the all task status. We can persists the cache data in physical memory and it is one of the scope of improvement for this application. Relevant basic junit test cases are covered in the code. The configured port for application is 8081. The file storage path is c:/tmp. Configuration parameters are kept at bare minimum for simplicity.
As per the requirement following API’s are exposed.
 API_1

POST /api/generate
            {
               "Goal":"10",
               "Step":"2"
            }

 Return
            202 ACCEPTED
            {
                        "task":"UUID of the task",
 
            }
It covers following basic validations.
1)	Goal & Step cannot be null or empty.
2)	Goal & Step cannot be negative number.
3)	Goal & step cannot be a non-digit string.
4)	Step cannot be greater than goal.

API_2
 
            GET /api/tasks/{UUID of the task}/status
 
 Return
 
            { "result":"SUCCESS/IN_PROGRESS/ERROR" }

It covers following basic validations.
1)	UUID should be in correct format.

API_3
 
            GET /api/tasks/{UUID of the task}?action=get_numlist
            
Return 
            {
                        "result": "10,8,6,4,2,0"
            }

It covers following basic validations.
1)	UUID should be in correct format.
2)	“action” cannot be other than “get_numlist”

How to set up the application and run it? You can try any one of below 3 options.

OPTION 1

1)	Make sure java and maven are installed in the system and JAVA_HOME, MAVEN_HOME env variables are set to respective directories.
2)	Clone the project from github repo using the link – https://github.com/AbhijitKangale/NumberGenerator
3)	Import the project in eclipse as an existing maven project.
4)	Build the project using maven build command – “mvn clean install”
5)	Run the redis server.
6)	Locate the “NumGeneratorApp.java” file, then do right click -> Run As -> Java Application. This will deploy and run the application in Spring Boot’s embedded tomcat server.
7)	Launch the postman and test the following possible scenarios.

Testing Scenario -

Test 1 – Open postman and send request with below request parameters.
1)	Request type – POST
2)	Request url - http://localhost:8081/api/generate
3)	Headers – {Content Type=Application/json}
4)	Body - { "Goal":"12", "Step":"3" }

Sample Result –
{
    "task": "aa626b92-9937-4749-bbf5-81fa3c8cc2c3"
}

It will save this file aa626b92-9937-4749-bbf5-81fa3c8cc2c3_output.txt file in “C:/tmp” path which has data as 12,9,6,3,0

Test 2 - Open postman and send request with below request parameters.
1)	Request type – GET
2)	Request url - http://localhost:8081/api/tasks/aa626b92-9937-4749-bbf5-81fa3c8cc2c3/status
3)	Headers – {Content Type=Application/json}

While “Test 1” is in process, copy the UUID displayed on console from below logger statement.
2020-05-25 19:55:35.858  INFO 73444 --- [ NumGenThread-1] c.v.c.t.s.NumberGeneratorServiceImpl     : Persisted task wiht UUID : aa626b92-9937-4749-bbf5-81fa3c8cc2c3

Sample Result –
While “Test 1” is still in process, it will display below result.
{
    "result": "IN_PROGRESS"
}
Once the “Test 1” is completed, it will display below result.
{
    "result": "SUCCESS"
}
By any reason, “Test 1” fails to write data to file, it will display below result.
{
    "result": "ERROR"
}

Test 3 - Open postman and send request with below request parameters.
1)	Request type – GET
2)	Request url - http://localhost:8081/api/tasks/aa626b92-9937-4749-bbf5-81fa3c8cc2c3?action=get_numlist
3)	Headers – {Content Type=Application/json}

Sample Result –
{
    "result": "12,9,6,3,0"
}

Note – Other tests can be performed in similar way.

OPTION 2
1)	Make sure java and maven are installed in the system and JAVA_HOME, MAVEN_HOME env variables are set to respective directories.
2)	Clone the project from github repo using the link – https://github.com/AbhijitKangale/NumberGenerator
3)	Build the project using maven build command – “mvn clean install”
4)	Run the redis server.
5)	Download and install tomcat from Apache website.
6)	Set port as 8081 in file { tomcat.installation.directory }/conf/server.xml
7)	Copy war file from {project.build.directory}/target to {tomcat.installation.directory}/webapps
8)	Run the tomcat server by { tomcat.installation.directory }/bin/startup.bat
9)	Test various scenarios mentioned in OPTION 1.

OPTION 3
1)	Make sure java and maven are installed in the system and JAVA_HOME, MAVEN_HOME env variables are set to respective directories.
2)	Make sure docker is installed in your system.
3)	Make sure DOCKER_HOST environment variable is set to tcp://localhost:2375
4)	Clone the project from github repo using the link – https://github.com/AbhijitKangale/NumberGenerator
5)	Run the docker as demon process.
6)	Run the redis server.
7)	Clone the project from github repo using the link –
8)	Build the project and generate docker image by running this command in command prompt – “mvn clean package docker:build”. This will generate the war file and docker image file in path {project.build.directory}/target. Pom.xml file is configured with docker maven plugin and related configuration which will generate docker image file.
9)	Go to this path {project.build.directory}/target and execute docker command “docker run -d -p 8081:80 numberGeneratorApp”. 
10)	Step 7 will create docker container and run our application. Here application port 8081 is mapped to port 80 of docker. “numberGeneratorApp” is the image file generated from docker build command from step 6.
11)	Test various scenarios mentioned in OPTION 1.

Things considered as out of scope for this application.
1)	Security – Managing security of the application is not considered at this point in time.
2)	Code coverage – Although most of the code is tested using junit, code coverage report is not provided at this point in time.
3)	Swagger integration – For better testing and documentation purpose, it can be integrated with swagger, which is not done at this point in time.
		           

