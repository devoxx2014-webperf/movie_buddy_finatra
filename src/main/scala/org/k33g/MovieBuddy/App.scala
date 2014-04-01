package org.k33g.MovieBuddy

import com.twitter.finatra._
import com.twitter.finatra.ContentType._
import scala.util.parsing.json._

object App extends FinatraServer {

  
  class HttpApp extends Controller {

    println("I am the constructor ...")

    val path = new java.io.File(".").getCanonicalPath()
    val sourceMovies = scala.io.Source.fromFile(path + "/json/movies.json")
    val linesMovies = sourceMovies.mkString
    sourceMovies.close()

    val moviesList = (JSON.parseFull(linesMovies).get match {
      case mList : List[Map[String,Any]] => mList
      case _ => Nil
    })

    println("Json Movies Loaded")

    val sourceUsers = scala.io.Source.fromFile(path + "/json/users.json")
    val linesUsers = sourceUsers.mkString
    sourceUsers.close()

    val usersList = (JSON.parseFull(linesUsers).get match {
      case uList : List[Map[String,Any]] => uList
      case _ => Nil
    })

    println("Json Users Loaded")

    type Ratings = Map[Int, Map[Int, Int]]

    var ratings: Ratings = Map()

    post("/rates") { request =>

      val rate = (JSON.parseFull(request.getContentString()).get match {
        case r : Map[String,Int] => r
        case _ => Nil
      }).toMap

      println("User Rate : " + rate)

      val userRates = ratings.get(rate("userId"))

      if (userRates.isEmpty) { // new
        ratings +=  rate("userId") ->  Map(rate("movieId")->rate("rate"))
      } else {
        ratings += rate("userId") -> (ratings(rate("userId")) ++ Map(rate("movieId")->rate("rate")))
      }
      println("Ratings : " + ratings)

      render.json(ratings).status(201).toFuture
    }

    ////$.getJSON("users/share/2164/452", function(data) { console.log(data); })
    get("/users/share/:userid1/:userid2") { request =>
      val userid1 = request.routeParams.getOrElse("userid1",0).toString()
      val userid2 = request.routeParams.getOrElse("userid2",0).toString()

      println(userid1.toInt)
      println(userid2.toInt)

      println(ratings.get(userid1.toInt)) //<-- nothing

      //val preco = new Preco()
      //render.json(preco.sharedPreferences(ratings,userid1.toInt,userid2.toInt)).status(200).toFuture

      render.json(null).toFuture
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


    /**
     * Custom Error Handling
     *
     * curl http://localhost:7070/error
     */
    get("/error")   { request =>
      1234/0
      render.plain("we never make it here").toFuture
    }

    /**
     * Custom Error Handling with custom Exception
     *
     * curl http://localhost:7070/unauthorized
     */
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

    /**
     * Custom 404s
     *
     * curl http://localhost:7070/notfound
     */
    notFound { request =>
      render.status(404).plain("not found yo").toFuture
    }

  }

  register(new HttpApp())
}