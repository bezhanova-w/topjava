 Meals API for current user
-
**GET all**

Request all meals for current user
<br>`curl -L -X GET "http://localhost:8080/topjava/rest/profile/meals"`

**GET filtered by dates/times**

Request all meals for current user filtered by dates or/and times
<br>PARAMS _startDate_, _endDate_, _startTime_, _endTime_ may be null, empty or absent (like ?startDate=&..)
<br>`curl -L -X GET "http://localhost:8080/topjava/rest/profile/meals/filter?startDate=2020-01-30&endDate=2020-01-31&startTime=10:00&endTime=13:00"`

**GET filtered by not specified dates/times**

Request all meals for current user filtered by empty/null dates or/and times
<br>PARAMS _startDate_, _endDate_, _startTime_, _endTime_ may be null, empty or absent
<br>`curl -L -X GET "http://localhost:8080/topjava/rest/profile/meals/filter?startDate&endDate&startTime&endTime"`

**GET by id**

Request a meal of current user, where 100002 is a meal's id
<br>`curl -L -X GET "http://localhost:8080/topjava/rest/profile/meals/100002"`

**DELETE**

Delete a meal of current user, where 100002 is a meal's id
<br>`curl -L -X DELETE "http://localhost:8080/topjava/rest/profile/meals/100002"`

**POST Create**

Create a new meal for current user
<br>BODY _{
    "dateTime": "2020-06-01T18:00:00",
    "description": "Created dinner",
    "calories": 300
}_
<br>`curl -L -X POST -H "Content-Type: application/json;charset=UTF-8" -d "{\"dateTime\": \"2020-06-01T18:00:00\", \"description\": \"Created dinner\", \"calories\": 300}" "http://localhost:8080/topjava/rest/profile/meals"`

**PUT Update**

Update a meal of current user, where 100002 is a meal's id
<br>BODY 
_{
    "id": 100002,
    "dateTime": "2020-01-30T10:00:00",
    "description": "Updated breakfast",
    "calories": 200
}_
<br>`curl -L -X PUT -H "Content-Type: application/json;charset=UTF-8" -d "{\"id\": 100002, \"dateTime\": \"2020-01-30T10:00:00\", \"description\": \"Updated breakfast\", \"calories\": 200}" "http://localhost:8080/topjava/rest/profile/meals/100002"`

