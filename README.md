# XLScripter

Ready-to-run solution to convert spreadsheet files (Microsoft Office Excel)
into SQLite databases, scripts or any text format. Very easy to use, works in
any environment which has a JVM.


## Details

Now, this is my itch: Sometimes I need to convert data in a spreadsheet file
(for example Excel) into some text version. This might be because I need to
load rows into a database, to convert data to HTML or otherwise produce code from
data.

Spreadsheet programs are wonderful tools for human beings,
but mostly I think automated tools do not share this affection.
The Excel-format is not easy to read, not even in the XML-version.

The most common work-arounds I used so far were something like this:

  * export the data to CSV and work with the CSV instead. That works quite
     well but is boring to parse. Also it is still manual work to
     export the file to CVS (and users don't like that format).
  * Create "code" within the spreadsheet with ugly commands like

        =CONCATENATE("insert into table xxx values('",A1,"','",B1,"')")

This kind of works, but it also is a manual process (with lots of copy paste).

There must be an easier way.

To make (specifically my own) life easier I threw together this
little tool which allows me to use a "transformer" script
(based on Clojure) which together with *xlscripter* is used
to convert a spreadsheet file (xls or xlsx format) to some output
as needed. To read the spreadsheet I use Apache POI.

Since the configuration resides in a separate transformer script
(probably quite short, but with the full power of Clojure at hand)
you can easily develop and (re)use it for many spreadsheet files.

Even if you do not speak Clojure, the provided "template.clj" transformer
is quite powerfull on its own, but with a few lines of code you can
make your own allmighty transformer. Well, you can get quite long with either.

The examples in here are usefull as they are and I am open to include
more if you send me your stuff.

Of course you could have a processor that mainly have other side-effects
than outputing text, it's up to you.


## Build

To build xlscripter I recommend `lein`. The result should (in this case)
be an uberjar, since I think this makes it easiest to work with xlscripter.
Here is how you compile xlscripter.jar.

    $ git clone https://github.com/jramb/xlscripter.git
    $ cd xlscripter
    $ lein uberjar

This assembles xlscripter.jar in the `target` directory.
You will then only need this jar file to use xlscripter.

For your convenience you can download the jar file here: https://dl.dropboxusercontent.com/u/7679659/xlscripter.jar

## Usage

You will need this:
  * xlscripter.jar
  * Java (JRE is sufficient)
  * your input.xls(x)
  * optional: your transformer-clj (or just the templater.clj, also see examples for inspiration)

Then

    java -jar xlscripter.jar -i input.xlsx -o output.txt -t transformer.clj

produces an output.txt according to the transformer. The transformer might
take additional parameters.

Now also includes a transformer to SQLite.
With one command you can transform a spreadsheet into a SQLite databaser!
Every sheet in the input spreadsheet becomes a table.

## Examples

    java -jar xlscripter.jar -i input.xlsx -o output.txt -t :tabsep
    java -jar xlscripter.jar -i input.xlsx -o output.txt -t :emacs
    java -jar xlscripter.jar -i input.xlsx -o output.txt -t :template template.tmpl
    java -jar xlscripter.jar -i input.xlsx -t :sqlite output.db
    java -jar xlscripter.jar -i input.xlsx -o output-iso.txt -t :tabsep -e iso-8859-1
  

### Details

The transformer is specified by the `-t` parameter.
For the predefined transformers you can use the ":keyword" shortcuts as value:
  * `:emacs`  Convert to Emacs table
  * `:tabsep` Convert to a tab-separated file
  * `:template <template-file>` Use the template-file to produce a custom output.

For little more control instead of using the predefined transformers you can
name a fully qualified clojure function.
as the transformer parameter, e.g. `xlscripter.transformer/tabsep`.

Last not least you can simply point to a clojure file that defines the
transformer, e.g. `example/tabsep.clj`.

For example: This transformer `tabsep.clj` converts the spreadsheet into tab-separated text
(same as `:tabsep`):

    (ns xlscripter.custom)

    (defn process [data args]
      (doseq [r (first data)]
        (println  (apply str (interpose "\t" r)))))

The `process` function, which *is* the tranformer, deserves some explanation:
The `data` parameter is actually the whole spreadsheet. It is a
list of sheets (spreadsheet files can contain many sheets). Here, by using `(first data)`
we select only the first sheet, which is mostly sufficient.

Every sheet is a *list of rows* and every row is a *list of cells*.
The cells are normal Clojure data, strings, numbers, Java-Dates. However,
formulas are presented as strings and *not* evaluated.

## Templater

Another feature is the predefined `:template` transformer which
uses a template file as a definition. This is a powerful tool to produce
output by defining a simple template format as an input.

If you have this template file `test.tmpl`:

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

     java -jar xlscripter.jar data.xlsx -t :template test.tmpl

will produce the output using the template.

### MODIFY

The `--MODIFY--` lines (as many as you like) will modify all input-cells
before they are produced. Technically the thing after the colon is evaluated
and expected to return a function that takes one parameter and returns the same
value, possibly modified. (Note: `replace-string` is a predefined function that
returns(!) just such a function. It sounds more complicated than it is.)

The list of MODIFY functions is chained together and applied to every(!) cell.

### BEGIN_DATA/END_DATA

The blocks between `--BEGIN_DATA--` and `--END_DATA--` are replaced
with the given row-ranges (1 being the first row in the sheet).
The text between BEGIN_DATA and END_DATA is used as formatting template, it
uses the formatter codes as described in java.util.Formatter
(which was inspired by C's `printf`).

A formatting string could be as simple as

    %s, %s, %s, %s

which would output the first four columns as (s)trings.
Other usefull codes are `%d` for decimals, `%tf` for date.

To specify a specific column you can write `1$`, `2$` etc. after the `%`,
for example

    %3$s, %2$s, %1$s, %4$s

Note that you must use this notation for the complete formatting part
(all placeholders) if you decide to use it.

You can have several BEGIN_DATA/END_DATA blocks.

### More

More examples are in the example directory.


## Known problems
  * Formulas are not calculated. This will probably never change.

## Planned features
  * Maybe: New built-in: Convert to XML (needed?)
  * Very maybe: new built-in: convert to generic SQL (create-table and inserts)

## License

Created 2013, extended 2015 and 2016

Distributed under the Eclipse Public License, the same as Clojure.

This does not apply to the used libraries themselves, they have their own
licenses, see Clojure, SQLite and Apache POI, http://poi.apache.org/

