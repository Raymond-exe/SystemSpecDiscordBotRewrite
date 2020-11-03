# SystemSpecDiscordBotRewrite
 An updated rewrite of the [System Specs Discord Bot](https://github.com/Raymond-exe/SystemSpecDiscordBot), designed to determine whether or not users can play any game, given their PC specs.

----------


# Commands
All commands are assuming the server is using the default prefix `~`
|Command|Arguments|Result|
|-------|---------|------|
|@mention |[game]     |An embed detailing why a user can/can't play the title given.|
|~help    |           |An embed listing all available commands|
|~search  |[CPU/GPU/GAME] [query]|Runs a search for the given query. If no hardware is specified, the search defaults to a search for games.|
|~setspecs|[CPU/GPU/RAM] [value] |Sets the user's hardware to the given value. The value must be findable using the `~search` command.|
|~myspecs |           |An embed detailing the user's specs.|
|~getspecs| [@user]   |An embed detailing the given user's specs, if their privacy setting is public.|
|~setprivacy|[PUBLIC/PRIVATE]|Sets the user's privacy setting so other users can/cannot view their system specs.|
|~compare | [@user]   |Compares this user's PC specs to the given user's PC specs. If the given user's privacy setting is private, PC scores will be compared.|
|~gameinfo| [game]    |Returns any publisher/development information relating to the given game.|
|~gamespecs|[game]    |Returns the minimum specifications to play the given game.|
|~feedback| [text]    |Sends feedback to a private channel on the bot's development server.|
|~ping|               |Tests the bot's connection.|


----------
# Branches
Version management details
| Branch | Use |
|---|---|
|[master](https://github.com/Raymond-exe/SystemSpecDiscordBotRewrite) | Latest stable build, fewest bugs in newest features. |
