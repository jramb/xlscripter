# XLScripter

Sometimes I need to convert data in a spreadsheet file
(for example Excel) into some text version. This might be because I need to
load rows into a database, to convert data to HTML or otherwise produce code from
data.

Now any spreadsheet program is a wonderfull tool for
human beings to use, but mostly it is a pain for automated
tools. The most common work-arounds I used was:

  * export the data to CSV and load that one. That works quite
     well but is boring to parse. Also it still is manual work to
     export the file to CVS (and users don't like that format).
  * Create "code" within the spreadsheet with ugly commands like

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
you can easily develop and (re)use it for many spreadsheet files.

Well, you need to know enough Clojure to write the transformers,
but as the examples show you will probably not need to write
very much code. The examples are usefull as they are and I am
open to include more if you send me your stuff.

## Usage

You will need this:
  * xlscripter.jar (this)
  * Java (JRE is sufficient)
  * your transformer.clj (see examples for inspiration)
  * your input.xls(x)

Then

    java -jar xlscripter.jar transformer.clj input.xlsx output.txt

produces an output.txt according to the transformer.

## Example

This transformer converts the spreadsheet into tab-separated text:

    (ns xlscripter.custom)

    (defn process [data & args]
      (for [r (first data)]
        (println  (apply str (interpose "\t" r)))))


More examples are in the example directory.

## License


Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.

This does not apply to the used libraries themselves, they have their own
licenses, see Apache POI, http://poi.apache.org/

