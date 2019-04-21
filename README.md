# Bank rest service
Project featuring REST service <strong> without making use of Spring </strong>.
The premise that rest application can be developed in the same manner as Spring.
The app is kept as simple as possible.

[![Build Status](https://travis-ci.com/chergey/bank-rest-service.svg?branch=master)](https://travis-ci.com/chergey/bank-rest-service)

Stack
* [Jersey](https://jersey.github.io)
* [HK2](https://javaee.github.io/hk2) Jersey IoC container
* [EclipseLink](http://www.eclipse.org/eclipselink)
* [Apache DB](https://db.apache.org) aka Derby, in memory database
* [REST Assured](http://rest-assured.io) for tests


Features
* HATEOAS
* Security


To run
```
mvn package 
java -jar <app-name> -port=<port>
```

Sample requests
```
curl -X POST http://localhost:<port>/api/accounts/transfer?from=1&to=2&amount=10
curl http://localhost:<port>/api/accounts
curl http://localhost:<port>/api/accounts/somename?page=&size=20
curl -X DELETE http://localhost:<port>/api/accounts/2
```

