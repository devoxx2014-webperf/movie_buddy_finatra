package org.k33g.MovieBuddy

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.twitter.finatra.test._
import com.twitter.finatra.FinatraServer
import org.k33g.MovieBuddy._

class AppSpec extends FlatSpecHelper {

  val app = new App.ExampleApp
  override val server = new FinatraServer
  server.register(app)

  
  "GET /notfound" should "respond 404" in {
    get("/notfound")
    response.body   should equal ("not found yo")
    response.code   should equal (404)
  }

  "GET /error" should "respond 500" in {
    get("/error")
    response.body   should equal ("whoops, divide by zero!")
    response.code   should equal (500)
  }

  "GET /unauthorized" should "respond 401" in {
    get("/unauthorized")
    response.body   should equal ("Not Authorized!")
    response.code   should equal (401)
  }

  "GET /index.html" should "respond 200" in {
    get("/")
    response.body.contains("Finatra - The scala web framework") should equal(false)
    response.code should equal(200)
  }

  }

}
