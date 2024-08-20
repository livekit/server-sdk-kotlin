PACKAGE_VERSION=$(cat ./package.json | jq -r '.version')
echo "updating gradle version name to $PACKAGE_VERSION"
# sed command works only on linux based systems as macOS version expects a backup file passed additionally
if [ "$(uname)" == "Darwin" ]; then
  sed -i '' -e "/VERSION_NAME=/ s/=.*/=$PACKAGE_VERSION/" ./gradle.properties
else
  sed -i -e "/VERSION_NAME=/ s/=.*/=$PACKAGE_VERSION/" ./gradle.properties
fi