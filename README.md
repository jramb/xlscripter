# XLScripter

Sometimes have the need to convert data in a spreadsheet file
(for example Excel) into some text version. This might be because I need to
load rows into a database, to convert data to HTML or otherwise produce code from
data.

Now any spreadsheet program is a wonderfull tool for
human beings to use, but mostly it is a pain for automated
tools. The most common work-arounds I used was:

a) export the data to CSV and load that one. That works quite
well but is boring to parse.
b) Create "code" within the spreadsheet, like
    =CONCATENATE("insert into table xxx values('",A1,"','",B1,"')")
Kind of works, but, it is a manual process (with lots of copy paste).


There must be an easier way.
To make (specifically my own) life easier I cast together this
little tool which allows you to specify a "transformer" script
(based on Clojure) which you then can use together with *xlscripter*
to convert a spreadsheet file (xls or xlsx format) to some output
of your like. To read the spreadsheet I use Apache POI.

Since the configuration resides in a separate transformer script
(probably quite short, but with the full power of Clojure at hand)
you easily can reuse it on many spreadsheet files.

## Usage

You will need this:
* xlscripter.jar
* Java (not more than the JRE)
* a transformer.clj (see examples for inspiration)
* your input.xls(x)

Then
    java -jar xlscripter.jar transformer.clj input.xlsx output.txt
produces an output.txt according to the transformer.

## License


Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.

This does not apply to the used libraries with have their own
licenses, see Apache POI, http://poi.apache.org/

