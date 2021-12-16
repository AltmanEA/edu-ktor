curl -i -H "Content-Type: application/x-www-form-urlencoded" -d "username=admin&password=admin"  -X POST http://localhost:8000/form-login
curl -i -H "Content-Type: application/x-www-form-urlencoded" -d "username=tutor&password=tutor"  -X POST http://localhost:8000/form-login
curl -v -b "user_session=count%3D%2523i1%26name%3D%2523sadmin" http://localhost:8000/user
curl -v -b "user_session=count%3D%2523i1%26name%3D%2523sadmin" http://localhost:8000/admin
curl -v -b "user_session=count%3D%2523i1%26name%3D%2523stutor" http://localhost:8000/user
curl -v -b "user_session=count%3D%2523i1%26name%3D%2523stutor" http://localhost:8000/admin