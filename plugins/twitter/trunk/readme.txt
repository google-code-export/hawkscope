To build this you will need Maven and Hawkscope jar with dependencies.
You can check out Hawkscope from svn, make "mvn install -Dmaven.test.skip" to 
install it in a local repository.

Build command:
mvn package -Dmaven.test.skip

Build result: target/twitter-X.X-jar-with-dependencies.jar

Copy to dist/twitter-X.X.jar to release