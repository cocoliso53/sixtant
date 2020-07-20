# sixtant

Sixtant's excercise repo

## Installation

Login to your ubuntu server using `ssh root@your.server`

Once you are there make sure you have java already installed by running
`java -version` on the terminal. If everything is fine you should see something
like `openjdk version "1.8.0_171"`, if nothing comes up and or if the number
after the first `1` is less than 8 the you need to install or update
respectively. Check the insturctions [here](https://adoptopenjdk.net/installation.html#linux-pkg).

Now we need to install [leiningen](https://leiningen.org/). Run the following
commands in your terminal:

1. Install curl command
   `sudo apt-get install -y curl`
2. Download the lein script
   `curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > lein`
3. Move the lein script to the user programs directory
   `sudo mv lein /usr/local/bin/lein`
4. Add execute permisions to the lein script
   `sudo chmod a+x /usr/local/bin/lein`
5. Lastly just check everything is fine
   `lein version`

It might take a little while.

Ok now the las step is just cloning the repo to your server. You can do it via
ssh or http.
   

## Usage

Justo go the the folder where the repo is stored and start the leiningen repl
`lein repl`. It will download a bunch of dependencies, once it's done run
the main function by typing `(-main)`.  And that's all!

 
## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
