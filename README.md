# Cadence (Server side application)

Uses pattern recognition algorithms on
[Cadence.js](https://github.com/RyanMcG/Cadence-js)
data for the purpose of recognizing users and authenticating them.

Analyze the "cadence" with which a user types a phrase to match and optionally
authenticate them.  This cadence data will be used in pattern recognition
algorithms to determine optimal classifiers for best separating the typing
patterns of users.  This project involves a web application and client side
library to process and capture user input.

## Usage

First you'll have to copy the config file.

```bash
cd resources
cp config.clj.sample config.clj
```

Then modify it with your favorite editor filling in the necessary fields.
Configuration values are first read from this file and then from system
environment variables.

After that's done you just have to launch the application (NOTE: I'm using
leiningen 2).

```bash
lein deps
lein run
```

### TODO

These are really just notes for contributors about what still needs doing.

*   Encrypt cadences? This might be impossible since we need to access cadences
    of arbitrary users when creating a classifier. Perhaps we can mask who's
    cadences they are though by overwritng the _id with a uuid encrypted with
    the user's password. Of course, this means password changes would
    be...interesting.

*   Set up logging utility ([Timbre](https://github.com/ptaoussanis/timbre)?).

*   Fill out user's profile page with data about available classifiers and
    trained phrases. Some graphs perhaps?

*   Create visualization of cadence data.

*   Write some tests!

## License

Copyright (C) 2012 Ryan McGowan

Distributed under the [Eclipse Public
License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
