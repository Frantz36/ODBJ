cp 4k* bin/out
cd bin/out
java app.Server lb 2003 localhost 2002 &
java app.Server inter 2002 localhost 2001 & 
java app.Server web 2001 &
cd ../..