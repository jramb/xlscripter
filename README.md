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

## Build

To build xlscripter I recommend lein.
Easiest to work with (for this tool, I think) is an uberjar,
so here is how you produce that.

    $ git clone https://github.com/jramb/xlscripter.git
    $ cd xlscripter
    $ lein uberjar

This assembles xlscripter.jar in the target directory.
You will then only need this jar file to use xlscripter.

For your convenience you can download the jar file here: https://www.dropbox.com/s/ghsmzu421aw1f2x/xlscripter.jar

## Usage

You will need this:
  * xlscripter.jar (this)
  * Java (JRE is sufficient)
  * your transformer.clj (see examples for inspiration)
  * your input.xls(x)

Then

    java -jar xlscripter.jar input.xlsx output.txt transformer.clj

produces an output.txt according to the transformer. The transformer might
take additional parameters following.

## Example

This transformer `tabsep.clj` converts the spreadsheet into tab-separated text:

    (ns xlscripter.custom)

    (defn process [data args]
      (doseq [r (first data)]
        (println  (apply str (interpose "\t" r)))))

## Templater

Another feature is the predefined "templater.clj" transformer which
uses a template file as a definition. This is a powerfull too to produce
output by defining a simple template format as an input.

If you have this template file "test.tmpl":

    Hello, before1
    --MODIFY:(replace-string "'" "''")--
    --MODIFY:(replace-string "Jörg" "Anton")--
    --BEGIN_DATA:[2-3]--
    Block1: '%s'
    --END_DATA--
    between 1
    between 2
    --BEGIN_DATA:[2-]--
    Block2: %3$tF
            '%1s' %2$d
    --END_DATA--
    also between 3
    --BEGIN_DATA--
      Block 3: '%s', %d, %tF
    --END_DATA--
    this is the last line.

then calling

     java -jar xlscripter.jar data.xlsx output.txt templater.clj

will reproduce the template to the output.

The `--MODIFY--` lines (as many as you like) will modify all input-cells
before they are produced. The blocks between `--BEGIN_DATA--` and `--END_DATA--` are replaced
with the given row-ranges (1 being the first row in the sheet).
The text between BEGIN_DATA and END_DATA is formatted using the formatter codes
as used in java.util.Formatter (which was inspired by C's `printf`).

You can have several BEGIN_DATA/END_DATA blocks.

More examples are in the example directory.


## Known problems
  * The transformer needs to be in the classpath. Basically it should be in the same dir where you run the command.
    I would like to rewrite that part completely.
  * Output needs to go into a file, I would like to be able to send the output to stdout.
  * Output is hardcoded to ISO-8859, should be configurable.
  * Formulas are not calculated. This will probably never change.

## License

Copyright © 2013

Distributed under the Eclipse Public License, the same as Clojure.

This does not apply to the used libraries themselves, they have their own
licenses, see Apache POI, http://poi.apache.org/

