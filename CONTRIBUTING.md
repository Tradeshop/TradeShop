# Contributing

* Depending on your changes there are certain rules you have to follow if you expect your Pull Request to be merged. The reason behind 
  this is that we want to keep our source code as clean as possible, in order to be able to debug and test efficiently.
* It is recommended to create a new remote branch for each Pull Request. Based on the current `upstream/master` changes! Good names for
  branches are a short description of the change: ``permission-bug``, ``cleanup`` and ``docs-update`` are appropriate names.
  ``patch-1``, ``fix`` and ``feature`` are not.
* It is also recommended that you ask us whether a potential (big) feature resides within the project's scope or whether it is already 
  in development (you can check whether it is present in our [TODO list](https://github.com/SparklingComet/TradeShop/projects/1?)).
  This will avoid wasting your time on something that will not get merged.
* Finally, although the following rules may seem strict and discouraging, we welcome *every single* contribution we get, from large pull-request to small README fixes.

1. Adding a new Method or Class
    - If you add something to the [Utils class](https://github.com/SparklingComet/TradeShop/blob/master/src/main/java/org/shanerx/tradeshop/Utils.java) you have to write documentation.
    - For that please follow the [JavaDoc template](https://github.com/DV8FromTheWorld/JDA/wiki/6%29-JDA-Structure-Guide#javadoc)
    - Keep your code consistent! [example](https://github.com/DV8FromTheWorld/JDA/wiki/5%29-contributing#examples)
    - Follow the guides provided at [JDA Structure Guide](https://github.com/DV8FromTheWorld/JDA/wiki/6%29-JDA-Structure-Guide)
    - Compare your code style to the one used all over JDA and ensure you
      do not break the consistency (if you find issues in the JDA style you can include and update it)
      
2. Making a Commit
    - While it may be tempting to create one big commit once all the changes have been tested,
      doing so may make reviewing your code more difficult. Furthermore, if we are forced to revert some of your changes but not all,
      several smaller commits ensure more efficiency.
    - When you commit your changes write a proper commit caption which explains what you have done.
      For more information please consult the [Contributing](https://github.com/DV8FromTheWorld/JDA/wiki/5%29-Contributing) section of our wiki.
      
3. Updating your Fork
    - Before you start committing make sure your fork is updated.
      (See [Syncing a Fork](https://help.github.com/articles/syncing-a-fork/) for more tips.)
    - We also expect that you have done some basic testing.


### Disclaimer

This file contains parts that were taken from [the JDA project](https://github.com/DV8FromTheWorld/JDA).We do **not** wish to take credit for those parts.Everything is licensed under the [Apache License v2.0](https://github.com/SparklingComet/TradeShop/blob/master/LICENSE).
