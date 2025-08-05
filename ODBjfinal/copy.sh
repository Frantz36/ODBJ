rm -r -f ../target/servlet-be/WEB-INF/classes/odbj
cp -r ../target/classes/odbj/ ../target/servlet-be/WEB-INF/classes/
cp -r ../target/classes/odbj/ ./bin/
cp ../target/classes/BackendServer.class ./bin/app