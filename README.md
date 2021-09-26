# nrk-news-consumer
Spring Boot app with Spring MVC, with Thymeleaf engine for views.  
Integrates Spring MVC with React by using webpack to build a js-bundle and including it in Thymeleaf template.

Spring Data JDBC for data access.  
Flyway for db-migrations.

## Maven and npm
Integrates Maven and NPM through the exec-maven-plugin, by running NPM commands during the maven build.  
Node and NPM are not installed as part of the build, and the executables needs to be present on the path.

## Local Development
Get webpack to watch for changes in js files during development by running `npm run watch`.