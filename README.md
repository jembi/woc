Web Ontology Classifier
===

The Web Ontology Classifier is developed using [Play Frameworks](http://www.playframework.com/) and Java.

Before checking out the source code, make sure that Java (JDK 6 or 7) is installed, and download and install the latest Classic Distribution (Standalone version) of [Play Frameworks](http://www.playframework.com/download).

Follow the instructions to [install Play Frameworks](http://www.playframework.com/documentation/2.2.x/Installing).

If the intention is to contribute to the project, you will need to check out the latest version of the WOC source code from the repository using [http://git-scm.com/](git), and sync any changes back in to the remote repository. [Git GUIs](http://git-scm.com/downloads/guis) are available to make using git simpler.
Alternatively, if you'd just like to make use of the application, you can [download the latest source code as a ZIP](https://github.com/jembi/woc/archive/master.zip) or [clone it to your desktop](github-mac://openRepo/https://github.com/jembi/woc).

Once the source code has been checked out, and assuming the play script has been added to your PATH:
```
export PATH=$PATH:/relativePath/to/play
```
the application can be accessed in a terminal by changing to the root directory of the application folder, and running the  `play` command:
```
> play
```

To get the application to run, the Fact++ Description Logic Reasoner is required. This can be [downloaded](https://code.google.com/p/factplusplus/downloads/list) as a precompiled binary distribution for [Mac OSX](https://code.google.com/p/factplusplus/downloads/detail?name=FaCTpp-OSX-v1.6.2.zip&can=2&q=), [Linux](https://code.google.com/p/factplusplus/downloads/detail?name=FaCTpp-linux-v1.6.2.tgz&can=2&q=), or  [Windows](https://code.google.com/p/factplusplus/downloads/detail?name=FaCTpp-win-v1.6.2.zip&can=2&q=).

Once this is downloaded, the relevant .dll or .jnilib file will need to be added to the java.library.path.

One way to do this is to specify the path of the file when launching the Play Framework:
```
> play -Djava.library.path=/$PATH_TO_FILE
```

If this is successful, the WOC application can be compiled
```
[woc] $ compile
```
and run:
```
[woc] $ run
```

Once running, the application should be accessible locally at [http://127.0.0.1:9000](http://127.0.0.1:9000).

The source code can be edited using any standard text editor, and has also been configured to work as an Eclipse Project using the [Eclipse IDE](https://www.eclipse.org).

To open the application in Eclipse, import the project as an existing project, specifying the root directory of the application.
