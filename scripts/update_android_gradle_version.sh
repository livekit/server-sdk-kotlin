PACKAGE_VERSION=$(cat ./package.json | jq -r '.version')
echo "updating gradle version name to $PACKAGE_VERSION"
# sed command works only on linux based systems as macOS version expects a backup file passed additionally
sed -i '' -e "/VERSION_NAME=/ s/=.*/=$PACKAGE_VERSION/" ./gradle.properties
