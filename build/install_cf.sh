#!/bin/bash

#get cf cli and unpack it
if [ ! -f ~/.local/bin/cf ]
 then
     echo Installing CF
     mkdir -p ~/.local/bin/
     wget https://s3.amazonaws.com/ccbu-binrepo/cf-cli/cf -O cf
     chmod +x cf
     cp cf ~/.local/bin/
fi
export PATH=~/.local/bin:$PATH