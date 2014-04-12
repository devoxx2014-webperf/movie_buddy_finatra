package org.k33g.MovieBuddy

import com.twitter.finatra._
import com.twitter.finatra.ContentType._
import scala.util.parsing.json._

object App extends FinatraServer {

  
  class HttpApp extends Controller {

    System.setProperty("com.twitter.finatra.config.logLevel", "ERROR")
    System.setProperty("com.twitter.finatra.config.port", ":"+System.getProperty("app.port", "3000"))
    System.setProperty("com.twitter.finatra.config.env","production")
    System.setProperty("com.twitter.finatra.config.assetPath", "/public")

    //val path = new java.io.File(".").getCanonicalPath()

    //val sourceMovies = scala.io.Source.fromFile(path + "/jsondb/movies.json")
    val sourceMovies = scala.io.Source.fromURL(getClass.getResource("/jsondb/movies.json"))
    val linesMovies = sourceMovies.mkString
    sourceMovies.close()

    val moviesList = (JSON.parseFull(linesMovies).get match {
      case mList : List[Map[String,Any]] => mList
      case _ => Nil
    })

    println("Json Movies Loaded")

    //val sourceUsers = scala.io.Source.fromFile(path + "/jsondb/users.json")
    val sourceUsers = scala.io.Source.fromURL(getClass.getResource("/jsondb/users.json"))
    val linesUsers = sourceUsers.mkString
    sourceUsers.close()

    val usersList = (JSON.parseFull(linesUsers).get match {
      case uList : List[Map[String,Any]] => uList
      case _ => Nil
    })

    println("Json Users Loaded")

    println("Listening ...")
    

    var ratings: Map[Double, Map[Double, Double]] = Map()

    post("/rates") { request =>

      val rate = JSON.parseFull(request.getContentString()).get.asInstanceOf[Map[String, Double]]
      val userRates = ratings.get(rate("userId"))

      if (userRates.isEmpty) { // new
        ratings +=  rate("userId") ->  Map(rate("movieId") -> rate("rate"))
      } else {
        ratings += rate("userId") -> (ratings(rate("userId")) ++ Map(rate("movieId") -> rate("rate")))
      }
      render.header("location","/rates/"+rate("userId").toInt.toString).status(301).nothing.toFuture
    }

    get("/rates/:userid") { request =>
      val userid = request.routeParams.getOrElse("userid",0).toString()
      render.json(ratings(userid.toInt)).status(200).toFuture
    }

    get("/users/share/:userid1/:userid2") { request =>

      val userid1 = request.routeParams.getOrElse("userid1",0).toString()
      val userid2 = request.routeParams.getOrElse("userid2",0).toString()

      val preco = new Preco()
      render.json(preco.sharedPreferences(ratings,userid1.toInt,userid2.toInt)).status(200).toFuture
    }

    get("/users/distance/:userid1/:userid2") { request =>

      val userid1 = request.routeParams.getOrElse("userid1",0).toString()
      val userid2 = request.routeParams.getOrElse("userid2",0).toString()

      val preco = new Preco()
      render.json(Map("distance" -> preco.distance(ratings,userid1.toInt,userid2.toInt))).status(200).toFuture
    }

    get("/movies") { request =>
      render.json(moviesList).toFuture
    }

    get("/movies/:id") { request =>
      val id = request.routeParams.getOrElse("id","")
      val searchedMovie = moviesList.filter(movie => movie("_id") == id.toInt )(0)
      render.json(searchedMovie).toFuture
    }

    get("/movies/search/title/:title/:limit") { request =>
      val title = request.routeParams.getOrElse("title","my life")
      val limit = request.routeParams.getOrElse("limit",1).toString()

      val searchedMovies = moviesList.filter(movie => {
        movie("Title").toString().toLowerCase().contains(title)
      }).slice(0, limit.toInt)

      render.json(searchedMovies).toFuture
    }

    get("/movies/search/genre/:genre/:limit") { request =>
      val genre = request.routeParams.getOrElse("genre","?")
      val limit = request.routeParams.getOrElse("limit",1).toString()

      val searchedMovies = moviesList.filter(movie => {
        movie("Genre").toString().toLowerCase().contains(genre)
      }).slice(0, limit.toInt)

      render.json(searchedMovies).toFuture
    }

    get("/movies/search/actors/:actors/:limit") { request =>
      val actors = request.routeParams.getOrElse("actors","?")
      val limit = request.routeParams.getOrElse("limit",1).toString()

      val searchedMovies = moviesList.filter(movie => {
        movie("Actors").toString().toLowerCase().contains(actors)
      }).slice(0, limit.toInt)

      render.json(searchedMovies).toFuture
    }

    get("/users") { request =>
      render.json(usersList).toFuture
    }

    get("/users/:id") { request =>

      val id = request.routeParams.getOrElse("id","john doe").toString()
      val searchedUser = usersList.filter(user => user("_id") == id.toInt )(0)
      render.json(searchedUser).toFuture
    }

    get("/users/search/:name/:limit") { request =>
      val name = request.routeParams.getOrElse("name","john doe")
      val limit = request.routeParams.getOrElse("limit",1).toString()

      val searchedUsers = usersList.filter(user => {
        user("name").toString().toLowerCase().contains(name)
      }).slice(0, limit.toInt)

      render.json(searchedUsers).toFuture
    }

    get("/") { request =>
      render.static("index.html").toFuture
    }

    get("/error")   { request =>
      render.plain("we never make it here").toFuture
    }

    class Unauthorized extends Exception

    get("/unauthorized") { request =>
      throw new Unauthorized
    }

    error { request =>
      request.error match {
        case Some(e:ArithmeticException) =>
          render.status(500).plain("whoops, divide by zero!").toFuture
        case Some(e:Unauthorized) =>
          render.status(401).plain("Not Authorized!").toFuture
        case Some(e:UnsupportedMediaType) =>
          render.status(415).plain("Unsupported Media Type!").toFuture
        case _ =>
          render.status(500).plain("Something went wrong!").toFuture
      }
    }

    notFound { request =>
      render.status(404).plain("not found yo").toFuture
    }

  }

  register(new HttpApp())
}
