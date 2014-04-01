package org.k33g.MovieBuddy

class Preco {

  def sharedPreferences(preferences: Map[Int, Map[Int, Int]], user1: Int, user2: Int ) : List[Int] = {

    val user1Preferences = preferences(user1)
    val user2Preferences = preferences(user2)

    return user1Preferences.keys.toList intersect user2Preferences.keys.toList
  }

}
