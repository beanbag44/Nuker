package me.beanbag.nuker.chat

interface ICommandArgument {
    val placeholder:String
    val subArgumentCount:Int

    fun getMatch(toMatch: List<String>): MatchType

    fun getSuggestions(toMatch: List<String>): List<String>
}