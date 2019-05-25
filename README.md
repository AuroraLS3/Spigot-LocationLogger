# Spigot-LocationLogger

This Spigot-plugin was written for gathering spatial data for my Bachelor's degree about locating player's bases with spatial clustering.

> **Notice to server owners**  
> The plugin was made with a single player in mind, so performance is not optimized for multiple players.
>
> The plugin is not recommended for use on a live server without modifications, as the database operations are performed on Spigot server thread, which blocks the thread from performing gameplay operations during database insert operations.

The included database.db contains data gathered during one hour of gameplay.

## How it works

- x, z and time logged every second for a player
- x, z and block type loggged for every placed/broken block
- SQLite database

## Building

Execute
```
mvn package
```
to obtain `<repo>/target/LocLog.jar`
