To build this you will need Maven and Hawkscope jar with dependencies.
You can check out Hawkscope from svn, make "mvn install -Dmaven.test.skip" to 
install it in a local repository.

Build command:
mvn package

Build result: target/gmail-X.X-jar-with-dependencies.jar

Copy to dist/gmail-X.X.jar to release