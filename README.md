# LanguageRegressionTests
*********************
Oracle's Connector/j version 8.0.20 is required in the buildpath of the project in order for compilation!
It can be downloaded here: https://dev.mysql.com/downloads/connector/j/
*********************
This project is meant to help learners of spoken and written languages by generating vocabulary tests based on learned vocabulary. In this manner it is supposed to prevent language "regression", similarly to how software regression tests are written to detect and prevent regression in software functionality.
This project has two parts, a MySQL database and the client that commits data to the database, as well as generating language tests for the user, based on that data.

The project was created with support for the Japanese language in mind, however the project is meant to be able to cover other languages, and some inbuilt support for other languages may be present. However there is no guarantee that the characters of a foreign language will be supported, due to MySQL's strange handling of UTF-8. Read more here on this helpful webpage: https://medium.com/@adamhooper/in-mysql-never-use-utf8-use-utf8mb4-11761243e434 (Oracle appears to be intent on fixing this problem, so this info may be outdated)

# The Database
In my case I chose to host my database on Amazon's AWS RDS service with MySQL, however with a few small tweaks to the source, other SQL based databases should be usable. The proper schema can be set up via the SQL commands in the SQLCommands.txt file.

There are three different tables: word, wordsource, and symbols. The word table contains the romanization of the word, the meaning, the type of word (verb, noun, etc...), and the language the word is from. Thusly the database can support multiple languages, and queries for different types of words in those languages. There may be duplicates of a word in a language, however no word may have the same wid, word id.

The wordsource table allows the user to associate a name (source) with a vocabulary word. This allows the client program to generate tests that contain vocabulary originating from a single source. This may allow a user to prepare to reread past media in the future, via a special language regression test.

The symbols table allows the user to associate a foreign language's symbols with the vocabulary word. This was designed with Japanese in mind, so there is a "main" column for symbols such as kanji and katakana, and an "ancillary" column for the hiragana reading of kanji. In the event that the vocabulary is just hiragana such as もっと, the hiragana will be stored in main, and ancillary will be left empty. The same will occur for katakana words. The user may fit this structure to store their own language's symbols, or if the language is purely alphabetical, the user will not create any symbols entries for their words.

The basic life-cycle of the data goes like this, the user finds vocabulary they do not know, they enter it into the database, the database stores that data permanently or until the database is deleted (A delete option for individual words may be added at some point). When the user decides to generate tests via the client, the database may return the vocabulary and its associated data based on the SQL queries that are executed.

# Stretch Goals
If development goes well, I hope to create a GUI to provide easier usage. I also hope to support saving generated vocabulary tests as pdf documents that can be printed.
