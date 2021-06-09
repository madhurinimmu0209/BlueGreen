#!/bin/bash
#
# ./$0 -d deployment -a app -b app_base -x green -r vaultRoleId -t vaultSecretId -o org -s space

## CONSTANTS
YAML_FILE=tenant-management.yml
SCRIPT_NAME=`basename $0`

## Print usage
function Usage() {
    cat<<USAGE

    $SCRIPT_NAME - Deploy TM apps

    Usage:
        $SCRIPT_NAME [-d deployment] [-a appName] [-b appSrcBase] [-x appNamePrefix] [-r vaultRoleId] [-t vaultSecretId] [-o org] [-s space]

    Where,
        -d    deployment          Tenant/datacenter
        -a    appName             Name of the App
        -b    appSrcBase          Source base of the app
        -x    appNamePrefix       (OPTIONAL) App name prefix to use
        -r    vaultRoleId         Vault Role Id
        -t    vaultSecretId       Vault Secret Id
        -o    org                 (OPTIONAL) Cloudfoundry organization. Defaults to the value of cf_org
        -s    space               (OPTIONAL) Cloudfoundry space. Defaults to cf_space
        -h                        Prints this help message

    Example:
        $SCRIPT_NAME -d produs1 -a org-management -b org-management -p green -r vaultRoleId -t vaultSecretId
USAGE
}

set -exv
source "install_vault.sh"

## MAIN
while getopts "d:D:a:A:b:B:x:X:r:R:t:T:o:O:s:S:hH" OPTION
do
    case $OPTION in
    d|D) deployment=$OPTARG
        ;;
    a|A) app=$OPTARG
        ;;
    b|B) app_base=$OPTARG
        ;;
    x|X) app_name_prefix=$OPTARG
        ;;
    r|R) vault_role_id=$OPTARG
        ;;
    t|T) vault_secret_id=$OPTARG
        ;;
    o|O) org_name=$OPTARG
        ;;
    s|S) space_name=$OPTARG
        ;;
    *)  Usage
        exit 0
        ;;
    esac
done


if [ -z "$deployment" -o -z "$app" -o -z "$app_base" -o -z "$vault_role_id" -o -z "$vault_secret_id" ]
then
    cat<<USAGE
    echo "ERROR: Required/mandatory option missing"
USAGE
    Usage
    exit 1
fi

source tmproperties/$deployment
org_name=${org_name:-$cf_org}
space_name=${space_name:-$cf_space}

# create a working copy of the manifest file
cd "$WORKSPACE/$app_base"

if [ -a $YAML_FILE ]
then
    rm $YAML_FILE
fi

cp ./manifest-$deployment.yml $YAML_FILE

# Change mediaSetupFqdn url if reqd.
#sed -i "s/mediaSetupFqdn: \".*/mediaSetupFqdn: \"${app_name_prefix}${app}.${domain}\"/" $YAML_FILE

# Change VAULT_ROLE_ID if reqd.
sed -i "/^\s*VAULT_ROLE_ID\s*:/ s/\(VAULT_ROLE_ID\).*/\1 : \"${vault_role_id}\"/" $YAML_FILE

# Change VAULT_SECRET_ID if reqd.
sed -i "/^\s*VAULT_SECRET_ID\s*:/ s/\(VAULT_SECRET_ID\).*/\1 : \"${vault_secret_id}\"/" $YAML_FILE

cd "${WORKSPACE}"

export CF_DIAL_TIMEOUT=60

#Install cf-cli and set it in the $PATH
export PATH=~/.local/bin:$PATH
if command -v cf
then
    echo "cf command is in PATH"
else
    mkdir -p ~/.local/bin/
    wget https://s3.amazonaws.com/ccbu-binrepo/cf-cli/cf -O cf
    chmod +x cf
    cp -p cf ~/.local/bin/
fi

export CF_HOME="$(mktemp -d)"

cf api ${cf_api_endpoint}
set +xv
cf login -u ${cf_username} -p ${cf_password} -a ${cf_api_endpoint} -o ${org_name} -s ${space_name}
set -exv

echo "${app_name_prefix}$app"
echo "$app_base/$YAML_FILE"
echo "$buildpack_url"
# cf push ${app_name_prefix}$app -f $app_base/$YAML_FILE -b $buildpack_url

exit 0
