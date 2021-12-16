@REM Basic
curl -i http://localhost:8000/basic
curl -i -H "Authorization: Basic dHV0b3I6dHV0b3I=" http://localhost:8000/basic
@REM Digest
curl -i http://localhost:8000/digest
curl -i -H @digest-header.txt http://localhost:8000/digest
@REM Form
curl -i -X POST http://localhost:8000/form-login
curl -i -H "Content-Type: application/x-www-form-urlencoded" -d "username=tutor&password=tutor"  -X POST http://localhost:8000/form-login
@REM Session
curl -v -b "user_session=count%3D%2523i1%26name%3D%2523stutor" http://localhost:8000/session
@REM JWT
curl -d "@tutor.json" -X POST  -H "Content-Type: application/json" http://localhost:8000/jwt-login
curl -i -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwL2p3dCIsImlzcyI6Imh0dHA6Ly8wLjAuMC4wOjgwODAvIiwiZXhwIjoxNjM5NjQ5MjcxLCJ1c2VybmFtZSI6InR1dG9yIn0.jGlpEunPkJPbnggCqpsFi7qLJkOeVWpPMaUebb7eG0U" http://localhost:8000/jwt