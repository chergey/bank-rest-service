# Bank rest service
Project featuring REST service using Jersey REST framework.
The premise that rest application can be developed in the same manner as Spring.
The app is kept as simple as possible.

[![Build Status](https://travis-ci.com/chergey/bank-rest-service.svg?branch=master)](https://travis-ci.com/chergey/bank-rest-service)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=chergey_bank-rest-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=chergey_bank-rest-service)

Stack
* JPMS - Java Modules
* [Jersey](https://jersey.github.io) Java REST Framework
* [HK2](https://javaee.github.io/hk2) Jersey IoC container
* [EclipseLink](http://www.eclipse.org/eclipselink) ORM
* [Apache DB](https://db.apache.org) aka Derby, in memory database
* [Shiro](https://shiro.apache.org) authentication and authorization framework
* [Hazelcast](https://hazelcast.com) caching framework

Features
* Tests are written in groovy
* Application is deployed in docker alongside with hazelcast
* Entirely modular - taking advantage of Java Modules
* HATEOAS
* Security


To run
```
mvn package
sudo docker-compose up --force-recreate
```

Sample requests
```
curl -X POST http://localhost:<port>/api/accounts/transfer?from=1&to=2&amount=10
curl http://localhost:<port>/api/accounts
curl http://localhost:<port>/api/accounts/somename?page=&size=20
curl -X DELETE http://localhost:<port>/api/accounts/2
```

