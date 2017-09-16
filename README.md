# pianola

A Clojure library/tool for generating music via cellular automata

## Usage

For live playing around:

Launch a repl:

```
lein repl
```

Note the port the nREPL is running on.

Open `src/pianola/playground.clj` in emacs.

Run Cider:

```
M-x cider-connect
```

Enter `localhost` and the port number of the repl.

In the REPL, run:

```clojure
(use 'overtone.live)
```

In the `playground.clj` buffer, move the cursor to the end of
`play-tune` and evaluate the sexp with `C-x C-e`.

To stop the music, move the cursor to the `(live/stop)` sexp and
evaluate it with `C-x C-e`

The first vector to `automate-music` is the definition of the cellular
automaton; the first element representing a rhythm and the second the
notes in a scale, which will be used to map from one generation to
another in the automaton.

The second vector to `automate-music` is the input generation of the
cellular automaton, in the same format as the definition.

## License

Copyright © 2017 Thomas Stephens

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
