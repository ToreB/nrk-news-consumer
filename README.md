# nrk-news-consumer
App that continuously fetches new articles from NRK's RSS feeds (mainly the frontpage articles), and persists the 
article metadata to a local database.  
The article metadata (which includes the article link, title, description and image link among others) is then used to 
present a view of articles for later reading.  
This allows for reading articles at your own pace, without having to worry about them "disappearing" from the NRK front
page.  
Articles are hidden / marked as read when the article link is clicked (which opens the article in a new tab), but can 
also be manually hidden.  
Articles can also be marked for later reading, which moves them from the main article view to a read later view.  

## Libraries++
* Spring Boot app with Spring MVC, with Thymeleaf engine for views.  
* React as view technology.
* Integrates Spring MVC with React by using webpack to build a js-bundle and including it in a Thymeleaf template.
  This allows the Thymeleaf to initialize and serve the React app, while still being able to easily inject model 
  properties into the React app, if necessary.
* Spring JDBC for data access.
* Flyway for db-migrations.
* SQLite as embedded database.

## Maven and npm
Integrates Maven and NPM through the exec-maven-plugin, by running NPM commands during the maven build.  
Node and NPM are not installed as part of the build, and the executables needs to be present on the path.

## Local Development
Get webpack to watch for changes in js files during development by running `npm run watch`.