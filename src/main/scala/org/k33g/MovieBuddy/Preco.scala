package org.k33g.MovieBuddy



class Preco {

  def sharedPreferences(preferences: Map[Double, Map[Double, Double]], user1: Int, user2: Int ) : List[Double] = {

    val user1Preferences = preferences(user1)
    val user2Preferences = preferences(user2)

    return user1Preferences.keys.toList intersect user2Preferences.keys.toList
  }

  def distance(preferences: Map[Double, Map[Double, Double]], user1: Int, user2: Int ) : Double = {

    val user1Preferences = preferences(user1)
    val user2Preferences = preferences(user2)

    val shared = user1Preferences.keys.toList intersect user2Preferences.keys.toList

    //java.lang.Double cannot be cast to java.lang.Integer
    //shared.foreach( pref => println(pref))

    if (shared isEmpty) 0

    else {
      val sumOfSquares = shared.foldLeft(0d) { (accu, movie) =>
        accu + (Math.pow(user1Preferences(movie) - user2Preferences(movie), 2))
      }
      1 / (1 + sumOfSquares)
    }

  }

}



