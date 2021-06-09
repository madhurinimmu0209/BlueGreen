#!/bin/bash

#get vault and unpack it
export PATH=~/.local/bin:$PATH
if ! type "~/.local/bin/vault" > /dev/null;
then
	echo "vault is not installed yet!"
	wget https://s3.amazonaws.com/ccbu-binrepo/vault/vault.tar.gz
        tar -zxvf vault.tar.gz 
	mkdir -p ~/.local/bin 
        mv -f vault ~/.local/bin/vault
	chmod +x ~/.local/bin/vault

       	rm vault.tar.gz
fi
